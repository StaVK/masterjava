package ru.javaops.masterjava.service.mail.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.service.mail.model.ResultSendEmail;


import java.util.List;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class ResultSendEmailDao implements AbstractDao {

    @SqlUpdate("TRUNCATE resultSendEmail CASCADE ")
    @Override
    public abstract void clean();

    @SqlQuery("SELECT * FROM resultSendEmail")
    public abstract List<ResultSendEmail> getAll();

/*    public Map<String, ResultSendEmail> getAsMap() {
        return StreamEx.of(getAll()).toMap(ResultSendEmail::getId, identity());
    }*/

    @SqlUpdate("INSERT INTO resultSendEmail (fromuser, touser, message) VALUES (:from, :to, :message)")
    public abstract void insert(@BindBean ResultSendEmail resultSendEmail);

}
