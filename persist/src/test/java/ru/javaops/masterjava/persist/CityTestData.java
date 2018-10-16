package ru.javaops.masterjava.persist;

import com.google.common.collect.ImmutableList;
import ru.javaops.masterjava.persist.dao.CityDao;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

public class CityTestData {
    public static City SPB;
    public static City MOW;
    public static City MNSK;
    public static List<City> FIRST_CITY;

    public static void init() {
        SPB = new City("spb", "Санкт-Петербург");
        MOW = new City("mow", "Москва");
        MNSK = new City("mnsk", "Минск");
        FIRST_CITY = ImmutableList.of(MNSK, MOW, SPB);
    }

    public static void setUp() {
        CityDao dao = DBIProvider.getDao(CityDao.class);
        dao.clean();
        DBIProvider.getDBI().useTransaction((conn, status) -> {
            FIRST_CITY.forEach(dao::insert);
        });
    }
}
