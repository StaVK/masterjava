package ru.javaops.masterjava.service.mail;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import ru.javaops.masterjava.config.Configs;
import ru.javaops.masterjava.service.mail.dao.ResultSendEmailDao;
import ru.javaops.masterjava.service.mail.model.ResultSendEmail;


import java.sql.DriverManager;
import java.util.List;

@Slf4j
public class MailSender {
    static {
        initDBI();
    }

    static void sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) {
        log.info("Send mail to \'" + to + "\' cc \'" + cc + "\' subject \'" + subject + (log.isDebugEnabled() ? "\nbody=" + body : ""));

        ResultSendEmailDao resultSendEmailDao = DBIProvider.getDao(ResultSendEmailDao.class);

        Config mail = Configs.getConfig("mail.conf", "mail");

        for(Addressee adr:to) {
            Email email = new SimpleEmail();
            email.setHostName(mail.getString("host"));
            email.setSmtpPort(mail.getInt("port"));
            email.setAuthenticator(new DefaultAuthenticator(mail.getString("username"), mail.getString("password")));
            email.setSSLOnConnect(mail.getBoolean("useSSL"));
//            String from = "user@gmail.com";
            String from = "kimask@yandex.ru";
            String toUser = adr.getEmail();
            try {
                email.setFrom(from);
                email.setSubject(subject);
                email.setMsg(body);
                email.addTo(toUser);
                email.send();
                resultSendEmailDao.insert(new ResultSendEmail(from,toUser,"Mail sent succsessful"));
            } catch (EmailException e) {
                e.printStackTrace();
                resultSendEmailDao.insert(new ResultSendEmail(from,toUser,"Mail not sent"));

            }
        }

    }
    private static void initDBI() {
        Config db = Configs.getConfig("persist.conf","db");
        initDBI(db.getString("url"), db.getString("user"), db.getString("password"));
    }

    private static void initDBI(String dbUrl, String dbUser, String dbPassword) {
        DBIProvider.init(() -> {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("PostgreSQL driver not found", e);
            }
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        });
    }
}
