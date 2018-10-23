package ru.javaops.masterjava.upload;

import com.google.common.base.Splitter;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import one.util.streamex.StreamEx;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.dao.UserGroupDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserGroup;
import ru.javaops.masterjava.persist.model.type.UserFlag;
import ru.javaops.masterjava.upload.PayloadProcessor.FailedEmails;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
public class UserProcessor {
    private static final int NUMBER_THREADS = 4;

    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
    private static UserDao userDao = DBIProvider.getDao(UserDao.class);
    private static GroupDao groupDao = DBIProvider.getDao(GroupDao.class);
    private static UserGroupDao userGroupDao = DBIProvider.getDao(UserGroupDao.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_THREADS);

    /*
     * return failed users chunks
     */
    public List<FailedEmails> process(final StaxStreamProcessor processor, Map<String, City> cities, int chunkSize) throws XMLStreamException, JAXBException {
        log.info("Start processing with chunkSize=" + chunkSize);

        Map<String, Future<List<String>>> chunkFutures = new LinkedHashMap<>();  // ordered map (emailRange -> chunk future)

        int id = userDao.getSeqAndSkip(chunkSize);
        List<User> chunk = new ArrayList<>(chunkSize);
        val unmarshaller = jaxbParser.createUnmarshaller();
        List<FailedEmails> failed = new ArrayList<>();
        Map<String, Group> groupMap = groupDao.getAsMap();
        List<UserGroup> chunkUserGroup = new ArrayList<>();

        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            String cityRef = processor.getAttribute("city");  // unmarshal doesn't get city ref
            String groupRefs = processor.getAttribute("groupRefs");
            ru.javaops.masterjava.xml.schema.User xmlUser = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.User.class);
            if (cities.get(cityRef) == null) {
                failed.add(new FailedEmails(xmlUser.getEmail(), "City '" + cityRef + "' is not present in DB"));
            }

            List<String> groupNames = (groupRefs==null) ? Collections.EMPTY_LIST : Splitter.on(' ').splitToList(groupRefs);

            if (!groupMap.keySet().containsAll(groupNames)) {
                failed.add(new FailedEmails(xmlUser.getEmail(), "One of group '" + groupRefs + "' is not present in DB"));
            } else {
                final User user = new User(id++, xmlUser.getValue(), xmlUser.getEmail(), UserFlag.valueOf(xmlUser.getFlag().value()), cityRef);
                List<UserGroup> userGroups = StreamEx.of(groupNames).map(name -> new UserGroup(user.getId(), groupMap.get(name).getId())).toList();
                chunkUserGroup.addAll(userGroups);
                chunk.add(user);
                if (chunk.size() == chunkSize) {
                    addChunkFutures(chunkFutures, chunk, chunkUserGroup);
                    chunk = new ArrayList<>(chunkSize);
                    chunkUserGroup = new ArrayList<>();
                    id = userDao.getSeqAndSkip(chunkSize);
                }
            }
        }

        if (!chunk.isEmpty()) {
            addChunkFutures(chunkFutures, chunk, chunkUserGroup);
        }

        List<String> allAlreadyPresents = new ArrayList<>();
        chunkFutures.forEach((emailRange, future) -> {
            try {
                List<String> alreadyPresentsInChunk = future.get();
                log.info("{} successfully executed with already presents: {}", emailRange, alreadyPresentsInChunk);
                allAlreadyPresents.addAll(alreadyPresentsInChunk);
            } catch (InterruptedException | ExecutionException e) {
                log.error(emailRange + " failed", e);
                failed.add(new FailedEmails(emailRange, e.toString()));
            }
        });
        if (!allAlreadyPresents.isEmpty()) {
            failed.add(new FailedEmails(allAlreadyPresents.toString(), "already presents"));
        }
        return failed;
    }


    private void addChunkFutures(Map<String, Future<List<String>>> chunkFutures, List<User> chunk, List<UserGroup> chunkUserGroup) {
        String emailRange = String.format("[%s-%s]", chunk.get(0).getEmail(), chunk.get(chunk.size() - 1).getEmail());

        Future<List<String>> future = executorService.submit(() -> {
            List<String> presentsEmails = DBIProvider.getDBI().inTransaction((handle, status) -> {
                UserDao hUserDao = handle.attach(UserDao.class);
                UserGroupDao hUserGroupDao = handle.attach(UserGroupDao.class);

                List<User> alreadyPresentUsers = hUserDao.insertAndGetConflictEmails(chunk);
                Set<Integer> alreadyPresentUsersId = StreamEx.of(alreadyPresentUsers).map(User::getId).toSet();
                hUserGroupDao.insertBatch(StreamEx.of(chunkUserGroup).filter(ug -> !alreadyPresentUsersId.contains(ug.getUserId())).toList());
                return StreamEx.of(alreadyPresentUsers).map(User::getEmail).toList();
            });
            return presentsEmails;

        });

        chunkFutures.put(emailRange, future);
        log.info("Submit chunk: " + emailRange);
    }
}
