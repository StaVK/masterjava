package ru.javaops.masterjava.web.handler;

import org.slf4j.event.Level;

public class ServerHandler extends SoapLoggingHandlers {
    @Override
    protected boolean isRequest(boolean isOutbound) {
        return false;
    }

    public ServerHandler(Level loggingLevel) {
        super(loggingLevel);
    }
}
