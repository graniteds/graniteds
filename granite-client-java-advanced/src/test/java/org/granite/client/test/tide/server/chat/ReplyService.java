package org.granite.client.test.tide.server.chat;

import org.granite.context.GraniteContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.messaging.webapp.HttpGraniteContext;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;

/**
 * Created by william on 13/02/14.
 */
@RemoteDestination(id="replyService")
public class ReplyService {

    public String requestReply(String name) {
        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        Gravity gravity = GravityManager.getGravity(graniteContext.getServletContext());
        AsyncMessage message = new AsyncMessage();
        message.setDestination("secureChat");
        message.setHeader(AsyncMessage.SUBTOPIC_HEADER, "chat");
        message.setBody(name);
        message.setTimeToLive(10000L);
        Message reply = gravity.sendRequest(null, message);
        if (reply instanceof ErrorMessage)
            throw new RuntimeException(((ErrorMessage)reply).getFaultCode());
        return (String)reply.getBody();
    }
}
