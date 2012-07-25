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

import org.granite.config.GraniteConfig;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceFactory;
import org.granite.messaging.service.ServiceInvoker;
import org.granite.messaging.service.security.SecurityService;
import org.granite.messaging.service.security.SecurityServiceException;
import org.granite.util.UUIDUtil;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;

/**
 * @author Franck WOLFF
 */
public abstract class AMF3MessageProcessor {

    private static final Logger log = Logger.getLogger(AMF3MessageProcessor.class);

    public static Message process(Message request) {
        GraniteContext context = GraniteContext.getCurrentInstance();
        AMF3MessageInterceptor interceptor = context.getGraniteConfig().getAmf3MessageInterceptor();
        
        Message response = null;
        try {
	        if (interceptor != null)
	            interceptor.before(request);
	        
	        if (request instanceof RemotingMessage)
	            response = processRemotingMessage((RemotingMessage)request);
	        else if (request instanceof CommandMessage)
	            response = processCommandMessage((CommandMessage)request);
	        else
	            throw new IllegalArgumentException("Unknown request message type: " + request);
        }
        finally {
	        if (interceptor != null)
	            interceptor.after(request, response);
        }
        
        if (context.getSessionId() != null)
            response.setHeader("org.granite.sessionId", context.getSessionId());
        
        return response;
    }

    public static Message processCommandMessage(CommandMessage request) {

        log.debug(">> Processing AMF3 request:\n%s", request);

        Message response = null;
        if (request.isSecurityOperation()) {
            GraniteContext context = GraniteContext.getCurrentInstance();
            GraniteConfig config = context.getGraniteConfig();

            if (!config.hasSecurityService())
                log.warn("Ignored security operation (no security settings in granite-config.xml): %s", request);
            else {
                SecurityService securityService = config.getSecurityService();
                try {
                    if (request.isLoginOperation())
                        securityService.login(request.getBody(), (String)request.getHeader(Message.CREDENTIALS_CHARSET_HEADER));
                    else if (request.isLogoutOperation())
                        securityService.logout();
                    else
                        log.warn("Unknown security operation: %s", request);
                } catch (Exception e) {
                    if (e instanceof SecurityServiceException) {
                        securityService.handleSecurityException((SecurityServiceException)e);
                        log.debug(e, "Could not process security operation: %s", request);
                    }
                    else
                        log.error(e, "Could not process security operation: %s", request);
                    response = new ErrorMessage(request, e);
                }
            }
        }

        if (response == null) {
            response = new AcknowledgeMessage(request);
            // For SDK 2.0.1_Hotfix2.
            if (request.isSecurityOperation())
                response.setBody("success");
        }

        // For SDK 2.0.1_Hotfix2.
        if ("nil".equals(request.getHeader(Message.DS_ID_HEADER)))
            response.getHeaders().put(Message.DS_ID_HEADER, UUIDUtil.randomUUID());

        log.debug("<< Returning AMF3 response:\n%s", response);

        return response;
    }

    public static Message processRemotingMessage(RemotingMessage request) {

        log.debug(">> Processing AMF3 request:\n%s", request);
        
        Message response = null;
        try {
            // Execute method on service.
            ServiceFactory factory = ServiceFactory.getFactoryInstance(request);
            ServiceInvoker<?> service = factory.getServiceInstance(request);
            Object result = service.invoke(request);

            response = new AcknowledgeMessage(request);
            response.setBody(result);
        } catch (ServiceException e) {
            log.debug(e, "Could not process remoting message: %s", request);
            response = new ErrorMessage(request, e);
        }

        log.debug("<< Returning AMF3 response:\n%s", response);

        return response;
    }
}
