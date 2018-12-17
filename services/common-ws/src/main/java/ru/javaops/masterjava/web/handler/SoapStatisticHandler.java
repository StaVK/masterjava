package ru.javaops.masterjava.web.handler;

import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.sun.xml.ws.api.message.Message;
import ru.javaops.masterjava.web.Statistics;

public class SoapStatisticHandler extends SoapBaseHandler {

    private static final String START="START";
    private static final String METHOD="METHOD";

    @Override
    public boolean handleMessage(MessageHandlerContext context) {
        Message message = context.getMessage();
        if(isOutbound(context)){
            Statistics.count((String)context.get(METHOD),(Long)context.get(START),Statistics.RESULT.SUCCESS);
        }
        else {
            context.put(START,System.currentTimeMillis());
            context.put(METHOD,message.getPayloadLocalPart());
        }
        return true;
    }


    @Override
    public boolean handleFault(MessageHandlerContext context) {
        return false;
    }
}
