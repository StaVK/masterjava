package ru.javaops.masterjava.service.mail;

import com.google.common.collect.ImmutableSet;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.StreamingAttachmentFeature;
import ru.javaops.web.WebStateException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MailServiceClient {

    public static void main(String[] args) throws MalformedURLException, WebStateException {
        Service service = Service.create(
                new URL("http://localhost:8080/mail/mailService?wsdl"),
                new QName("http://mail.javaops.ru/", "MailServiceImplService"));
        StreamingAttachmentFeature stf = new StreamingAttachmentFeature("/tmp", true, 3072);
        MailService mailService = service.getPort(MailService.class, new MTOMFeature(),stf);

        String fileName = "toAttach.jpg";
        String filePath="e:/"+fileName;

        Map<String, Object> ctxt=((BindingProvider)mailService).getRequestContext();
        ctxt.put(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192);
        DataHandler dh = new DataHandler(new
                FileDataSource(filePath));

        List<Attachment> attaches=new ArrayList<>();
        attaches.add(new Attachment("toAttach.jpg",dh));


        String state = mailService.sendToGroup(ImmutableSet.of(new Addressee("kimask@yandex.ru", null)), null,
                "Group mail subject", "Group mail body", attaches);
        System.out.println("Group mail state: " + state);


        GroupResult groupResult = mailService.sendBulk(ImmutableSet.of(
                new Addressee("Мастер Java <masterjava@javaops.ru>"),
                new Addressee("Bad Email <bad_email.ru>")), "Bulk mail subject", "Bulk mail body",
                null);
        System.out.println("\nBulk mail groupResult:\n" + groupResult);
    }
}
