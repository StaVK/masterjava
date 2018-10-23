package ru.javaops.masterjava.upload;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
public class GroupProcessor {
    private final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);

/*    public Map<String, Group> process(StaxStreamProcessor processor) throws XMLStreamException {
        val map = groupDao.getAsMap();
        val newGroups = new ArrayList<Group>();

        while (processor.startElement("Group", "Project")) {
            val ref = processor.getAttribute("name");
            if (!map.containsKey(ref)) {
                newGroups.add(new Group(ref, processor.getAttribute("type"),));
            }
        }
        log.info("Insert batch " + newCities);
        cityDao.insertBatch(newCities);
        return cityDao.getAsMap();
    }*/
}
