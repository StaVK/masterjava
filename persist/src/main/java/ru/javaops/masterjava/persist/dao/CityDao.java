package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class CityDao implements AbstractDao{

    @SqlUpdate("INSERT INTO city (code, name) VALUES (:code, :name) ")
    public abstract void insert(@BindBean City city);

    @SqlUpdate("TRUNCATE city CASCADE")
    @Override
    public abstract void clean();

    @SqlQuery("SELECT * FROM city ORDER BY code LIMIT :it")
    public abstract List<City> getWithLimit(@Bind int limit);

    @SqlQuery("SELECT * FROM city WHERE code=:it")
    public abstract City get(@Bind String code);
}
