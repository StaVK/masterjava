package ru.javaops.masterjava.service.mail.rest;


import org.glassfish.jersey.media.multipart.*;
import org.hibernate.validator.constraints.NotBlank;
import ru.javaops.masterjava.service.mail.Attachment;
import ru.javaops.masterjava.service.mail.GroupResult;
import ru.javaops.masterjava.service.mail.MailServiceExecutor;
import ru.javaops.masterjava.service.mail.MailWSClient;
import ru.javaops.masterjava.service.mail.util.Attachments;
import ru.javaops.masterjava.web.WebStateException;

import javax.activation.DataHandler;
import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path("/")

public class MailRS {
    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "Test";
    }

    @POST
    @Path("send")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
/*    public GroupResult send(@NotBlank @FormParam("users") String users,
                            @FormParam("subject") String subject,
                            @NotBlank @FormParam("body") String body,
                            @FormDataParam("attachName") InputStream fileInputStream,
                            @FormDataParam("attach") FormDataMultiPart formData) throws WebStateException {*/
    public GroupResult send(FormDataMultiPart formData) throws WebStateException {
        String users = formData.getField("users").getValue();
        String subject = formData.getField("subject").getValue();
        String body = formData.getField("body").getValue();
        //TODO Починить вложения


        FormDataBodyPart filePart = formData.getField("attach");
        List<Attachment> attachmentList = new ArrayList<>();
        if (filePart != null) {
            ContentDisposition headerOfFilePart = filePart.getContentDisposition();

            InputStream fileInputStream = filePart.getValueAs(InputStream.class);

            Attachment attachment = Attachments.getAttachment(headerOfFilePart.getFileName(), fileInputStream);

            attachmentList.add(attachment);

        }

        return MailServiceExecutor.sendBulk(MailWSClient.split(users), subject, body, attachmentList);
    }


}