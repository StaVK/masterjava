package ru.javaops.masterjava.service.mail;

import lombok.AllArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@ToString
public class Message implements Serializable {
    private String users;
    private String subject;
    private String body;
//    private List<Attachment> attachments;

}
