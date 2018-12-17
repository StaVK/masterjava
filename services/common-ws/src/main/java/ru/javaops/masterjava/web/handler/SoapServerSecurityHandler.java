package ru.javaops.masterjava.web.handler;

import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.typesafe.config.Config;
import ru.javaops.masterjava.config.Configs;
import ru.javaops.masterjava.web.AuthUtil;

import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;

import static ru.javaops.masterjava.web.AuthUtil.encodeBasicAuthHeader;

public class SoapServerSecurityHandler extends SoapBaseHandler {

    private String authHeader;

    public SoapServerSecurityHandler(String user, String password) {
        this(encodeBasicAuthHeader(user, password));
    }

    public SoapServerSecurityHandler(String authHeader) {
        this.authHeader = authHeader;
    }

    @Override
    public boolean handleMessage(MessageHandlerContext context) {

        Config mail = Configs.getConfig("hosts.conf", "hosts").getConfig("mail");

        Map<String, List<String>> headers = (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);

        int code = AuthUtil.checkBasicAuth(headers, encodeBasicAuthHeader(mail.getString("user"), mail.getString("password")));
        if (code != 0) {
            context.put(MessageContext.HTTP_RESPONSE_CODE, code);
            throw new SecurityException();
        }
        return true;
    }

    @Override
    public boolean handleFault(MessageHandlerContext context) {
        return true;
    }
}
