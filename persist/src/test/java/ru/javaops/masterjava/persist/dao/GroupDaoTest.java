package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.CityTestData;
import ru.javaops.masterjava.persist.GroupTestData;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.Group;

import java.util.List;

import static ru.javaops.masterjava.persist.CityTestData.FIRST_CITY;
import static ru.javaops.masterjava.persist.CityTestData.SPB;
import static ru.javaops.masterjava.persist.GroupTestData.GROUPS;
import static ru.javaops.masterjava.persist.GroupTestData.TG6;

public class GroupDaoTest extends AbstractDaoTest<GroupDao> {


    public GroupDaoTest() {
        super(GroupDao.class);
    }

    @BeforeClass
    public static void init() throws Exception {
        GroupTestData.init();
    }

    @Before
    public void setUp() throws Exception {
        GroupTestData.setUp();
    }

    @Test
    public void insert() throws Exception {
        dao.clean();
        dao.insert(TG6);
        Assert.assertEquals(1, dao.getWithLimit(100).size());
    }

    @Test
    public void getWithLimit() {
        List<Group> groups = dao.getWithLimit(3);
        Assert.assertEquals(GROUPS, groups);
    }
}