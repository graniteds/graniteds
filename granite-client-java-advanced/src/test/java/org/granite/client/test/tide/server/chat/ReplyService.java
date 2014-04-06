/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
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
