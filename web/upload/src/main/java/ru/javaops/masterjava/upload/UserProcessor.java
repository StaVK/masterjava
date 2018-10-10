package ru.javaops.masterjava.upload;

import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.User;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.JaxbUnmarshaller;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.sql.BatchUpdateException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class UserProcessor {
    private static final JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
    private static UserDao userDao = DBIProvider.getDao(UserDao.class);

    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static class FiledImport {
        public String email;
        public String errorMessage;

        public FiledImport(String email, String errorMessage) {
            this.email = email;
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            return email + errorMessage;
        }
    }

    public List<User> process(final InputStream is) throws XMLStreamException, JAXBException {
        final StaxStreamProcessor processor = new StaxStreamProcessor(is);
        List<User> users = new ArrayList<>();

        JaxbUnmarshaller unmarshaller = jaxbParser.createUnmarshaller();
        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            ru.javaops.masterjava.xml.schema.User xmlUser = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.User.class);
            final User user = new User(xmlUser.getValue(), xmlUser.getEmail(), UserFlag.valueOf(xmlUser.getFlag().value()));
            users.add(user);
        }
        return users;
    }

    public List<UserProcessor.FiledImport> process(final InputStream is, final int batchChunkSize) throws XMLStreamException, JAXBException, ExecutionException, InterruptedException {


        return new Callable<List<FiledImport>>() {
            @Override
            public List<FiledImport> call() throws XMLStreamException, JAXBException{
                List<FiledImport> result = new ArrayList<>();
                List<User> chunkUserList = new ArrayList<>();
                final StaxStreamProcessor processor = new StaxStreamProcessor(is);
//                List<User> users = new ArrayList<>();

                JaxbUnmarshaller unmarshaller = jaxbParser.createUnmarshaller();

//                List<FiledImport> filedImportList = new ArrayList<>();
                while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
                    ru.javaops.masterjava.xml.schema.User xmlUser = unmarshaller.unmarshal(processor.getReader(), ru.javaops.masterjava.xml.schema.User.class);
                    final User user = new User(xmlUser.getValue(), xmlUser.getEmail(), UserFlag.valueOf(xmlUser.getFlag().value()));
                    chunkUserList.add(user);
//                    int[] updateCount=new int[chunkUserList.size()];
                    if (batchChunkSize == chunkUserList.size()) {
                        result.addAll(insertChunkList(chunkUserList));
                    }
                }
                if(!chunkUserList.isEmpty()){
                    result.addAll(insertChunkList(chunkUserList));
                }
//                executorService.shutdown();
//                result.addAll(filedImportList);
                return result;
            }
            private List<FiledImport> insertChunkList(List<User> chunkUserList){
                List<FiledImport> result = new ArrayList<>();
                try {
                    result.addAll(
                            executorService.submit(() -> {
                                List<FiledImport> resultChunkList = new ArrayList<>();
                                int[] tmp = userDao.insertBatch(chunkUserList, batchChunkSize);
                                for (int i = 0; i < tmp.length; i++) {
                                    if (tmp[i] <= 0) {
                                        FiledImport filedImport = new FiledImport(chunkUserList.get(i).getEmail(), " already present");
                                        resultChunkList.add(filedImport);
//                                                System.out.println(filedImport);
                                    }
                                }
                                chunkUserList.clear();
                                return resultChunkList;
                            }).get());
                } catch (Exception e) {
                    e.printStackTrace();
                    result.add(
                            new FiledImport(chunkUserList.get(0).getEmail()+
                                    (chunkUserList.size()>1 ? "-"+chunkUserList.get(chunkUserList.size()-1).getEmail() : ""),e.toString()));
                }
                return result;
            }
        }.call();
    }

}
