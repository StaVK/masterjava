package ru.javaops.masterjava.upload;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.Project;
import ru.javaops.masterjava.persist.model.type.GroupType;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import java.util.Map;

@Slf4j
public class ProjectProcessor {
    private final ProjectDao projectDao = DBIProvider.getDao(ProjectDao.class);
    private final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);

    public Map<String, Project> process(StaxStreamProcessor processor) throws XMLStreamException {
        val map = projectDao.getAsMap();

        while (processor.startElement("Project", "Projects")) {
            val ref = processor.getAttribute("name");
            Project project=null;
            if (!map.containsKey(ref)) {
                project = new Project(ref, processor.getElementValue("description"));
                log.info("Insert project " + project);
                project.setId(projectDao.insertGeneratedId(project));

                val mapGroup=groupDao.getAsMap();
                while (processor.startElement("Group", "Project")) {
                    val refGroup = processor.getAttribute("name");
                    if (!mapGroup.containsKey(ref)) {
                        Group group = new Group(refGroup, GroupType.valueOf(processor.getAttribute("type")), project.getId());
                        groupDao.insert(group);
                    }
                }
            }

        }
        return projectDao.getAsMap();
    }

}
