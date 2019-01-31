package ru.javaops.masterjava.service.mail.listeners;


import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.BlobMessage;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.commons.io.IOUtils;
import ru.javaops.masterjava.web.FileAsByteArrayManager;


import javax.jms.*;
import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

@WebListener
@Slf4j
public class JmsMailListener implements ServletContextListener {
    private Thread listenerThread = null;
    private QueueConnection connection;

    private FileAsByteArrayManager fileManager = new FileAsByteArrayManager();


    private void writeFile(byte[] bytes, String fileName) throws IOException {
        File file = new File(fileName);
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            accessFile.write(bytes);
        }
    }


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "*");
            InitialContext initCtx = new InitialContext();
            QueueConnectionFactory connectionFactory =
                    (QueueConnectionFactory) initCtx.lookup("java:comp/env/jms/ConnectionFactory");
            connection = connectionFactory.createQueueConnection();
            QueueSession queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = (Queue) initCtx.lookup("java:comp/env/jms/queue/MailQueue");
            QueueReceiver receiver = queueSession.createReceiver(queue);
            connection.start();
            log.info("Listen JMS messages ...");
            listenerThread = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Message m = receiver.receive();

                        if (m instanceof ObjectMessage) {
                            ObjectMessage objectMessage = (ObjectMessage) m;
                            ru.javaops.masterjava.service.mail.Message message=(ru.javaops.masterjava.service.mail.Message) objectMessage.getObject();
                            String text=message.toString();
                            log.info("Received Object Message: "+text);
                        }
                        if(m instanceof BytesMessage){
                            ActiveMQBytesMessage bytesMessage=(ActiveMQBytesMessage) m;
                            fileManager.writeFile(bytesMessage.getContent().getData(), "e://received_"+bytesMessage.getStringProperty("fileName"));
                            log.info("Received BytesMessage");
                        }
                        if (m instanceof TextMessage) {
                            TextMessage tm = (TextMessage) m;
                            String text = tm.getText();
                            log.info("Received TextMessage with text '{}'", text);
                        }
                    }
                } catch (Exception e) {
                    log.error("Receiving messages failed: " + e.getMessage(), e);
                }
            });
            listenerThread.start();
        } catch (Exception e) {
            log.error("JMS failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException ex) {
                log.warn("Couldn't close JMSConnection: ", ex);
            }
        }
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }
}