package ru.javaops.masterjava.webapp;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQSession;
import org.apache.commons.io.IOUtils;
import ru.javaops.masterjava.service.mail.util.Attachments;
import ru.javaops.masterjava.web.FileAsByteArrayManager;
import ru.javaops.masterjava.service.mail.Message;

import javax.activation.DataHandler;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.IllegalStateException;

@WebServlet("/sendJms")
@Slf4j
@MultipartConfig
public class JmsSendServlet extends HttpServlet {
    private Connection connection;
    private ActiveMQSession session;
    private MessageProducer producer;

    private FileAsByteArrayManager fileManager = new FileAsByteArrayManager();


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            InitialContext initCtx = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) initCtx.lookup("java:comp/env/jms/ConnectionFactory");
            connection = connectionFactory.createConnection();
            session = (ActiveMQSession) connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            producer = session.createProducer((Destination) initCtx.lookup("java:comp/env/jms/queue/MailQueue"));
        } catch (Exception e) {
            throw new IllegalStateException("JMS init failed", e);
        }
    }

    @Override
    public void destroy() {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException ex) {
                log.warn("Couldn't close JMSConnection: ", ex);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String result;
        try {
            log.info("Start sending");
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            String users = req.getParameter("users");
            String subject = req.getParameter("subject");
            String body = req.getParameter("body");

            Part filePart = req.getPart("attach");
            result=sendJms(users,subject,body,filePart);
            log.info("Processing finished with result: {}", result);
        } catch (Exception e) {
            log.error("Processing failed", e);
            result = e.toString();
        }
        resp.getWriter().write(result);
    }

    private synchronized String sendJms(String users, String subject, String body, Part filePart) throws JMSException, IOException {

        Message message = new Message(users, subject, body);
        ObjectMessage testMessage = session.createObjectMessage();
        testMessage.setObject(message);
        producer.send(testMessage);

        if(filePart!=null) {

            final File file = File.createTempFile("stream2file", ".tmp");
            file.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(file)) {
                IOUtils.copy(filePart.getInputStream(), out);
            }

            BytesMessage bytesMessage=session.createBytesMessage();
            bytesMessage.setStringProperty("fileName", filePart.getSubmittedFileName());
            bytesMessage.writeBytes(fileManager.readfileAsBytes(file));

            producer.send(bytesMessage);
            log.info("Bytes message sent");
        }

        return "Successfully sent JMS message";
    }
}