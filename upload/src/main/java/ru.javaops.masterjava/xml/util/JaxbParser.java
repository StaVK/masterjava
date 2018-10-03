package ru.javaops.masterjava.xml.util;

import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import java.io.*;
import java.util.concurrent.Callable;


/**
 * Marshalling/Unmarshalling JAXB helper
 * XML Facade
 */
public class JaxbParser {

    private ThreadLocal<JaxbMarshaller> jaxbMarshaller;
    private ThreadLocal<JaxbUnmarshaller> jaxbUnmarshaller;
    protected Schema schema;
    private JAXBContext ctx;

/*    public JaxbParser(JAXBContext jaxb) {
        this.jaxbUnmarshaller = ThreadLocal.withInitial(() -> safe(jaxb::createUnmarshaller));
        this.jaxbMarshaller = ThreadLocal.withInitial(() -> safe(jaxb::createMarshaller));
    }

    public JaxbParser(Class ...classes) {
        this(safe(() -> JAXBContext.newInstance(classes)));
    }*/

    public JaxbParser(Class... classesToBeBound) {
        try {
            ctx=JAXBContext.newInstance(classesToBeBound);
            init(ctx);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    //    http://stackoverflow.com/questions/30643802/what-is-jaxbcontext-newinstancestring-contextpath
/*    public JaxbParser(String context) {
        try {
            init(JAXBContext.newInstance(context));
        } catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }*/

    private void init(JAXBContext ctx){
/*        try {
            jaxbMarshaller=ctx.createMarshaller();
            jaxbUnmarshaller=new JaxbUnmarshaller(ctx);
        } catch (JAXBException e) {
            e.printStackTrace();
        }*/

        jaxbMarshaller = new ThreadLocal<JaxbMarshaller>() {
            protected synchronized JaxbMarshaller initialValue() {
                try {
                    return new JaxbMarshaller(ctx);
                } catch (JAXBException e) {
                    throw new IllegalStateException("Unable to create unmarshaller");
                }
            }
        };
        jaxbUnmarshaller = new ThreadLocal<JaxbUnmarshaller>() {
            protected synchronized JaxbUnmarshaller initialValue() {
                try {
                    return new JaxbUnmarshaller(ctx);
                } catch (JAXBException e) {
                    throw new IllegalStateException("Unable to create unmarshaller");
                }
            }
        };
    }

    private static <T> T safe(Callable<T> fn) {
        try {
            return fn.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Unmarshaller
    public <T> T unmarshal(InputStream is) throws JAXBException {
        return (T) jaxbUnmarshaller.get().unmarshal(is);
    }

    public <T> T unmarshal(Reader reader) throws JAXBException {
        return (T) jaxbUnmarshaller.get().unmarshal(reader);
    }

    public <T> T unmarshal(String str) throws JAXBException {
        return (T) jaxbUnmarshaller.get().unmarshal(str);
    }

    public <T> T unmarshal(XMLStreamReader reader, Class<T> elementClass) throws JAXBException {
        return jaxbUnmarshaller.get().unmarshal(reader, elementClass);
    }

    // Marshaller
    public void setMarshallerProperty(String prop, Object value) {
        try {
            jaxbMarshaller.get().setProperty(prop, value);
        } catch (PropertyException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String marshal(Object instance) throws JAXBException {
        return jaxbMarshaller.get().marshal(instance);
    }

    public void marshal(Object instance, Writer writer) throws JAXBException {
        jaxbMarshaller.get().marshal(instance, writer);
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
        jaxbUnmarshaller.get().setSchema(schema);
        jaxbMarshaller.get().setSchema(schema);
    }

    public void validate(String str) throws IOException, SAXException {
        validate(new StringReader(str));
    }

    public void validate(Reader reader) throws IOException, SAXException {
        schema.newValidator().validate(new StreamSource(reader));
    }
}
