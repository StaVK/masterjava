package ru.javaops.masterjava.xml.web;

import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static com.google.common.base.Strings.nullToEmpty;

@WebServlet("/UploadServlet")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50)   // 50MB
public class UploadServlet extends HttpServlet {
    /**
     * Name of the directory where uploaded files will be saved, relative to
     * the web application directory.
     */
    private static final String SAVE_DIR = "uploadFiles";
    private static final Comparator<User> USER_COMPARATOR = Comparator.comparing(User::getValue).thenComparing(User::getEmail);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("index.jsp");
    }

    /**
     * handles file upload
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        // gets absolute path of the web application
        String appPath = request.getServletContext().getRealPath("");
        // constructs path of the directory to save uploaded file
        String savePath = appPath + SAVE_DIR;

        // creates the save directory if it does not exists
        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }
        String fileName = "";
        for (Part part : request.getParts()) {
            fileName = extractFileName(part);
            // refines the fileName in case it is an absolute path
            fileName = new File(fileName).getName();
            part.write(savePath + File.separator + fileName);
        }
        Set<User> userSet = null;
        try {
            userSet = processor(savePath + File.separator + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.setAttribute("users", userSet);
        request.setAttribute("message", "Upload has been done successfully!");
        request.getRequestDispatcher("/users.jsp").forward(
                request, response);
    }

    /**
     * Extracts file name from HTTP header content-disposition
     */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length() - 1);
            }
        }
        return "";
    }

    private Set<User> processor(String filePath) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        ConcurrentHashMap concurrentHashMap=new ConcurrentHashMap();

        Set<User> userSet=new TreeSet<>(USER_COMPARATOR);
        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(new FileInputStream(filePath))) {
            XMLStreamReader reader = processor.getReader();

            List<Callable<Void>> tasks = new ArrayList<>();
            JaxbParser parser = new JaxbParser(User.class);
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    if ("User".equals(reader.getLocalName())) {
                        tasks.add(() -> {
                            User user = parser.unmarshal(reader, User.class);
                            System.out.println(user.getValue());
//                            concurrentHashMap.put(user,"");
                            userSet.add(user);
                            return null;
                        });

/*                        user.setFlag(FlagType.fromValue(reader.getAttributeValue(0)));
                        user.setEmail(reader.getAttributeValue(2));
                        user.setValue(reader.getElementText());*/

                    }
                }
            }
            executor.invokeAll(tasks);
            executor.shutdown();
        }
//        userSet= concurrentHashMap.newKeySet();
        return userSet;
    }
}
