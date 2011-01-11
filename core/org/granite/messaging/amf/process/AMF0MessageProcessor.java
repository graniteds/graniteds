/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.messaging.amf.process;

import java.util.Iterator;
import java.util.List;

import org.granite.context.AMFContextImpl;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.AMF0Body;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.amf.AMF3Object;
import org.granite.util.UUIDUtil;

import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public abstract class AMF0MessageProcessor {

    private static final Logger log = Logger.getLogger(AMF0MessageProcessor.class);

    public static AMF0Message process(AMF0Message amf0RequestMessage) {

        log.debug(">> Processing AMF0 request:%s", amf0RequestMessage);

        GraniteContext context = GraniteContext.getCurrentInstance();
        AMFContextImpl amf = (AMFContextImpl)context.getAMFContext();

        AMF0Message amf0ResponseMessage = new AMF0Message();
        amf0ResponseMessage.setVersion(amf0RequestMessage.getVersion());

        ErrorMessage loginError = null;
        String dsId = null;
        for (Iterator<AMF0Body> bodies = amf0RequestMessage.getBodies(); bodies.hasNext(); ) {
            AMF0Body requestBody = bodies.next();

            Object value = requestBody.getValue();
                       
            Message amf3RequestMessage = null;
            
            if (value instanceof List<?>)
            	amf3RequestMessage = (Message)((List<?>)value).get(0);
            else
            	amf3RequestMessage = (Message)((Object[])value)[0];           	            	
            
            log.debug(">> Processing AMF3 request:\n%s", amf3RequestMessage);

            // If we get a login error (setCredentials on flex side), we don't execute subsequent requests and
            // just copy the initial login error (GDS specific, otherwise the FaultEvent dispatched by the
            // RemoteObject is not the login error but an authorization error after actual service call).
            Message amf3ResponseMessage = null;
            if (loginError == null) {
                amf.setCurrentAmf3Message(amf3RequestMessage);

                amf.getCustomResponseHeaders().clear();
                amf3ResponseMessage = AMF3MessageProcessor.process(amf3RequestMessage);

                if ((amf3ResponseMessage instanceof ErrorMessage) && ((ErrorMessage)amf3ResponseMessage).loginError())
                    loginError = (ErrorMessage)amf3ResponseMessage;

                // For SDK 2.0.1_Hotfix2+ (LCDS 2.5+).
                if ("nil".equals(amf3ResponseMessage.getHeader(Message.DS_ID_HEADER))) {
                    amf3ResponseMessage.getHeaders().put(
                        Message.DS_ID_HEADER,
                        (dsId == null ? (dsId = UUIDUtil.randomUUID()) : dsId)
                    );
                }
                amf3ResponseMessage.getHeaders().putAll(amf.getCustomResponseHeaders());
            }
            else
                amf3ResponseMessage = loginError.copy(amf3RequestMessage);

            log.debug("<< Got AMF3 response:\n%s", amf3ResponseMessage);

            AMF3Object data = new AMF3Object(amf3ResponseMessage);
            AMF0Body responseBody = new AMF0Body(
                getResponseTarget(requestBody, amf3ResponseMessage), "", data, AMF0Body.DATA_TYPE_AMF3_OBJECT
            );
            amf0ResponseMessage.addBody(responseBody);
        }

        log.debug("<< Returning AMF0 response:%s", amf0ResponseMessage);

        return amf0ResponseMessage;
    }

    private static String getResponseTarget(AMF0Body requestBody, Message responseMessage) {
        if (responseMessage instanceof ErrorMessage)
            return requestBody.getResponse() + "/onStatus";
        return requestBody.getResponse() + "/onResult";
    }
}
