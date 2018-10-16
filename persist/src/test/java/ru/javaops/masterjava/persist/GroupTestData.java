package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.GroupType;

import java.util.List;

public class GroupTestData {

    public static Group TG6;
    public static Group TG7;
    public static Group TG8;
    public static Group MJ1;

    public static List<Group> GROUPS;

    public static void init() {

        TG6 = new Group("topjava06", GroupType.FINISHED);
        TG7 = new Group("topjava07", GroupType.FINISHED);
        TG8 = new Group("topjava08", GroupType.CURRENT);
        GROUPS = ImmutableList.of(TG6, TG7, TG8);
        MJ1=new Group("masterjava01",GroupType.CURRENT);
    }

    public static void setUp() {
        GroupDao dao = DBIProvider.getDao(GroupDao.class);
        dao.clean();
        DBIProvider.getDBI().useTransaction((conn, status) -> {
            GROUPS.forEach(dao::insert);
        });
    }
}
