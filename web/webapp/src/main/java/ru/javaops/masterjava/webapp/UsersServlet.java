package ru.javaops.masterjava.webapp;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.service.mail.Addressee;
import ru.javaops.masterjava.service.mail.MailService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import static ru.javaops.masterjava.common.web.ThymeleafListener.engine;

@WebServlet("")
public class UsersServlet extends HttpServlet {
    private UserDao userDao = DBIProvider.getDao(UserDao.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale(),
                ImmutableMap.of("users", userDao.getWithLimit(20)));
        engine.process("users", webContext, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
/*        Service service = Service.create(
                new URL("http://localhost:8080/mail/mailService?wsdl"),
                new QName("http://mail.javaops.ru/", "MailServiceImplService"));*/

        // Надо запустить MailServicePublisher? Ругается что порт занят
        String subject=req.getParameter("subject");
        String body=req.getParameter("body");
        Enumeration<String> attributeNames = req.getAttributeNames();


        System.out.println(subject+" "+body);

/*        MailService mailService = service.getPort(MailService.class);
        mailService.sendToGroup(ImmutableSet.of(
                new Addressee("masterjava@javaops.ru", null)), null, "Subject", "Body");*/
    }
}
