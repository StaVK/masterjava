package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.CityDao;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.Project;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static ru.javaops.masterjava.persist.GroupTestData.GROUPS;
import static ru.javaops.masterjava.persist.GroupTestData.MJ1;

public class ProjectTestData {
    public static Project TOPJAVA;
    public static Project MASTERJAVA;
    public static List<Project> PROJECTS;

    public static void init() {
        GroupTestData.init();
        GroupTestData.setUp();

        TOPJAVA = new Project("topjava", "Topjava");
        MASTERJAVA = new Project("masterjava", "Masterjava");
        PROJECTS = ImmutableList.of(MASTERJAVA, TOPJAVA);
    }

    public static void setUp() {
        ProjectDao dao = DBIProvider.getDao(ProjectDao.class);
        dao.clean();
        DBIProvider.getDBI().useTransaction((conn, status) -> {
            PROJECTS.forEach(dao::insert);
        });
    }
}
