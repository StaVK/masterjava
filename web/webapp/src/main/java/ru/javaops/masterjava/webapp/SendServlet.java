package ru.javaops.masterjava.webapp;

import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.service.mail.Attachment;
import ru.javaops.masterjava.service.mail.GroupResult;
import ru.javaops.masterjava.service.mail.MailWSClient;
import sun.misc.IOUtils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@WebServlet("/send")
@Slf4j
@MultipartConfig
public class SendServlet extends HttpServlet {

    private static void pipe(ReadableByteChannel in, WritableByteChannel out)
            throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (in.read(buffer) >= 0 || buffer.position() > 0) {
            buffer.flip();
            out.write(buffer);
            buffer.compact();
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private String getFileName(final Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim()
                        .replace("\"", "");
            }
        }
        return null;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String result;
        try {
            log.info("Start sending");
            req.setCharacterEncoding("UTF-8");
            resp.setCharacterEncoding("UTF-8");
            Collection<Part> parts = req.getParts();

            String users = convertStreamToString(req.getPart("users").getInputStream());
            String subject = convertStreamToString(req.getPart("subject").getInputStream());
            String body = convertStreamToString(req.getPart("body").getInputStream());

            List<Attachment> attaches = new ArrayList<>();

            final String STORAGE_FOLDER_PATH = "/upload";
            final Part attach = req.getPart("fileToUpload");
            final String name = this.getFileName(attach);

            //creating the save directory if it doesn't exist
            File uploadDirectory = new File(STORAGE_FOLDER_PATH);
            if (!uploadDirectory.exists()) {
                uploadDirectory.mkdirs();
            }


            if (attach != null && attach.getSize() > 0
                    && attach.getInputStream() != null && name != null) {

                final Path outputFile = Paths.get(STORAGE_FOLDER_PATH, name);

                try (final ReadableByteChannel input = Channels.newChannel(attach
                        .getInputStream());
                     final WritableByteChannel output = Channels
                             .newChannel(new FileOutputStream(outputFile
                                     .toFile()));) {
                    pipe(input, output);
                }
            }


            DataHandler dh = new DataHandler(new FileDataSource(STORAGE_FOLDER_PATH + File.separator + name));

            Attachment attachment = new Attachment(name, dh);
            attaches.add(attachment);

            GroupResult groupResult = MailWSClient.sendBulk(MailWSClient.split(users), subject, body, attaches);

            File file = new File(STORAGE_FOLDER_PATH + File.separator + name);
            file.delete();

            result = groupResult.toString();
            log.info("Processing finished with result: {}", result);
        } catch (Exception e) {
            log.error("Processing failed", e);
            result = e.toString();
        }
        resp.getWriter().write(result);
    }
}
