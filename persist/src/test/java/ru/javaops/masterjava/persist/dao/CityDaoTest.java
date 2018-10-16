package ru.javaops.masterjava.persist.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.javaops.masterjava.persist.CityTestData;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

import static ru.javaops.masterjava.persist.CityTestData.FIRST_CITY;
import static ru.javaops.masterjava.persist.CityTestData.SPB;

public class CityDaoTest extends AbstractDaoTest<CityDao> {


    public CityDaoTest() {
        super(CityDao.class);
    }

    @BeforeClass
    public static void init() throws Exception {
        CityTestData.init();
    }

    @Before
    public void setUp() throws Exception {
        CityTestData.setUp();
    }

    @Test
    public void insert() throws Exception {
        dao.clean();
        dao.insert(SPB);
        Assert.assertEquals(1, dao.getWithLimit(100).size());
    }

    @Test
    public void getWithLimit() {
        List<City> cities = dao.getWithLimit(3);
        Assert.assertEquals(FIRST_CITY, cities);
    }
}