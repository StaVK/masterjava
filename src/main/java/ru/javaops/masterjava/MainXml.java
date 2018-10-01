package ru.javaops.masterjava;

import com.google.common.io.Resources;
import com.sun.xml.internal.fastinfoset.stax.events.StAXEventWriter;
import javafx.collections.transformation.SortedList;
import ru.javaops.masterjava.xml.schema.*;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;
import ru.javaops.masterjava.xml.util.XsltProcessor;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;

public class MainXml {
    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));
    }

    private String projectName;

    public MainXml(String projectName) {
        this.projectName = projectName;
    }

    public void printJaxB() throws Exception {
        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());
        List<Project> projectList = payload.getProject();
        List<Group> groupList;
        List<User> userList = payload.getUser();

        List<User> forSortingUserList = new ArrayList<>();

        for (Project project : projectList) {
            groupList = project.getGroup();
            if (projectName.equals(project.getName())) {
                for (Group group : groupList) {
                    forSortingUserList.addAll(group.getUser());
                }
            }
            break;
        }
        forSortingUserList.sort(Comparator.comparing(User::getFullName));
        for (User user : forSortingUserList) {
            System.out.println(user.getFullName());
        }
    }

    public void printStAX() throws Exception {
        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource("payload.xml").openStream())) {
            Map<String,String> users=new TreeMap<>();
            String email, fullName;
            while (processor.startElement("User", "Project")) {
                email=processor.getAttribute("email");
                processor.startElement("fullName", "User");
                fullName=processor.getText();
                users.put(email,fullName);
            }
            for (Map.Entry<String, String> map:users.entrySet()){
                System.out.println(map.getKey()+" "+map.getValue());
            }
        }
    }
    public void printXSL() throws Exception{
        try (InputStream xslInputStream = Resources.getResource("users_template.xsl").openStream();
             InputStream xmlInputStream = Resources.getResource("payload.xml").openStream()) {

            XsltProcessor processor = new XsltProcessor(xslInputStream);
            System.out.println(processor.transform(xmlInputStream));
        }

    }
    public void transform(String xmlFileName, String xslFileName, String resultHtmlFileName) throws Exception {
        try {
            // Установка входных документов
            Source inputXML = new StreamSource(
                    new File(Resources.getResource(xmlFileName).toURI()));

            Source inputXSL = new StreamSource(
                    new File(Resources.getResource(xslFileName).toURI()));
//                    new File(Resources.getResource("users_template.xsl").toURI()));

            // Установка выходного объекта
            Result outputXHTML = new StreamResult(
                    new File(".\\src\\main\\resources\\"+resultHtmlFileName));

            // Настройка конструктора для преобразований
            TransformerFactory factory = TransformerFactory.newInstance();

            // Предкомпиляция команд
            Templates templates = factory.newTemplates(inputXSL);

            // Получить transformer для этого XSL
            Transformer transformer = templates.newTransformer();
            transformer.setParameter("project", projectName);

            // Выполнить преобразование
            transformer.transform(inputXML, outputXHTML);

        } catch (TransformerConfigurationException e) {
            System.out.println("The underlying XSL processor " +
                    "does not support the requested features.");
        } catch (TransformerException e) {
            System.out.println("Error occurred obtaining " +
                    "XSL processor.");
        }
    }

    public static void main(String[] args) throws Exception {
        MainXml mainXml = new MainXml("topjava");
//        mainXml.printJaxB();
//        mainXml.printStAX();
//        mainXml.printXSL();
//        mainXml.transform("payload.xml","users.xsl","output-users");
        mainXml.transform("payload.xml","groups.xsl","output-groups.html");
    }
}
