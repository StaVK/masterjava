package ru.javaops.masterjava.service.mail;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sun.xml.ws.developer.SchemaValidation;
import com.sun.xml.ws.developer.StreamingDataHandler;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import ru.javaops.masterjava.ExceptionType;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.service.mail.persist.MailCase;
import ru.javaops.masterjava.service.mail.persist.MailCaseDao;
import ru.javaops.web.WebStateException;


import javax.activation.DataHandler;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

@Slf4j
public class MailSender {
    private static final MailCaseDao MAIL_CASE_DAO = DBIProvider.getDao(MailCaseDao.class);

    static MailResult sendTo(Addressee to, String subject, String body, List<Attachment> attaches) throws WebStateException {
        val state = sendToGroup(ImmutableSet.of(to), ImmutableSet.of(), subject, body, attaches);
        return new MailResult(to.getEmail(), state);
    }

    static String sendToGroup(Set<Addressee> to, Set<Addressee> cc, String subject, String body, List<Attachment> attaches) throws WebStateException {
        log.info("Send mail to \'" + to + "\' cc \'" + cc + "\' subject \'" + subject + (log.isDebugEnabled() ? "\nbody=" + body : ""));

        String fileName=attaches.get(0).getName();
        StreamingDataHandler sdh = (StreamingDataHandler) attaches.get(0).getDataHandler();
        try {
            File file = new File(fileName);
            sdh.moveTo(file);
            sdh.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

/*        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedOutputStream outputStream = new BufferedOutputStream(fos);
            outputStream.write(dh);
            outputStream.close();

            System.out.println("Received file: " + fileName);

        } catch (IOException ex) {
            System.err.println(ex);
        }*/

        // Create the attachment
        EmailAttachment attachment = new EmailAttachment();
//        attachment.setPath(file.getPath());
        String path = fileName;
        attachment.setPath(path);
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
//        attachment.setDescription("Picture of John");
        attachment.setName(fileName);

        String state = MailResult.OK;
        try {
            val email = MailConfig.createHtmlEmail();
            email.setSubject(subject);
            email.setHtmlMsg(body);
            for (Addressee addressee : to) {
                email.addTo(addressee.getEmail(), addressee.getName());
            }
            for (Addressee addressee : cc) {
                email.addCc(addressee.getEmail(), addressee.getName());
            }

            //  https://yandex.ru/blog/company/66296
            email.setHeaders(ImmutableMap.of("List-Unsubscribe", "<mailto:kimask@yandex.ru?subject=Unsubscribe&body=Unsubscribe>"));

            // add the attachment
            email.attach(attachment);

            email.send();
        } catch (EmailException e) {
            log.error(e.getMessage(), e);
            state = e.getMessage();
        }
        try {
            MAIL_CASE_DAO.insert(MailCase.of(to, cc, subject, state));
        } catch (Exception e) {
            log.error("Mail history saving exception", e);
            throw new WebStateException(e, ExceptionType.DATA_BASE);
        }
        log.info("Sent with state: " + state);
        return state;
    }
}
