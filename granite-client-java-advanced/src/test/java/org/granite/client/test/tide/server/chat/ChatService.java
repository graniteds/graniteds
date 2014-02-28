package org.granite.client.test.tide.server.chat;

import org.granite.context.GraniteContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.messaging.webapp.HttpGraniteContext;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by william on 13/02/14.
 */
@RemoteDestination(id="chatService")
public class ChatService {

    public Set<String> getConnectedUsers() {
        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        Gravity gravity = GravityManager.getGravity(graniteContext.getServletContext());
        Set<String> users = new HashSet<String>();
        for (Principal principal : gravity.getConnectedUsers())
            users.add(principal.getName());
        return users;
    }
}
