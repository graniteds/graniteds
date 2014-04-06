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
package org.granite.client.messaging.channel.amf;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.granite.client.messaging.channel.AbstractHTTPChannel;
import org.granite.client.messaging.channel.Credentials;
import org.granite.client.messaging.channel.UsernamePasswordCredentials;
import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.push.TopicMessage;
import org.granite.client.messaging.messages.requests.InvocationMessage;
import org.granite.client.messaging.messages.requests.LoginMessage;
import org.granite.client.messaging.messages.requests.PublishMessage;
import org.granite.client.messaging.messages.requests.ReplyMessage;
import org.granite.client.messaging.messages.requests.SubscribeMessage;
import org.granite.client.messaging.messages.requests.UnsubscribeMessage;
import org.granite.client.messaging.messages.responses.AbstractResponseMessage;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.FaultMessage.Code;
import org.granite.client.messaging.messages.responses.ResultMessage;
import org.granite.client.messaging.transport.Transport;
import org.granite.messaging.service.security.SecurityServiceException;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractAMFChannel extends AbstractHTTPChannel {

	public AbstractAMFChannel(Transport transport, String id, URI uri, int maxConcurrentRequests) {
		super(transport, id, uri, maxConcurrentRequests);
	}

	protected Message[] convertToAmf(RequestMessage request) throws UnsupportedEncodingException {
		Message[] messages = null;
		
		switch (request.getType()) {
			case PING: {
				CommandMessage commandMessage = new CommandMessage();
				commandMessage.setOperation(CommandMessage.CLIENT_PING_OPERATION);
				commandMessage.setMessageId(request.getId());
				commandMessage.setTimestamp(request.getTimestamp());
				commandMessage.setTimeToLive(request.getTimeToLive());
				commandMessage.setHeaders(request.getHeaders());
				messages = new Message[]{commandMessage};
				break;
			}
			case LOGIN: {
				LoginMessage login = (LoginMessage)request;
				CommandMessage commandMessage = new CommandMessage();
				commandMessage.setOperation(CommandMessage.LOGIN_OPERATION);
				commandMessage.setMessageId(request.getId());
				commandMessage.setTimestamp(request.getTimestamp());
				commandMessage.setTimeToLive(request.getTimeToLive());
				commandMessage.setHeaders(request.getHeaders());
				Credentials credentials = login.getCredentials();
				if (credentials instanceof UsernamePasswordCredentials)
					commandMessage.setBody(((UsernamePasswordCredentials)credentials).encodeBase64());
				else
					throw new UnsupportedOperationException("Unsupported credentials type: " + credentials);
				messages = new Message[]{commandMessage};
				break;
			}
			case LOGOUT: {
				CommandMessage commandMessage = new CommandMessage();
				commandMessage.setOperation(CommandMessage.LOGOUT_OPERATION);
				commandMessage.setMessageId(request.getId());
				commandMessage.setTimestamp(request.getTimestamp());
				commandMessage.setTimeToLive(request.getTimeToLive());
				commandMessage.setHeaders(request.getHeaders());
				messages = new Message[]{commandMessage};
				break;
			}
			case PUBLISH: {
				PublishMessage publish = (PublishMessage)request;
				AsyncMessage asyncMessage = new AsyncMessage();
				asyncMessage.setMessageId(publish.getId());
				asyncMessage.setTimestamp(publish.getTimestamp());
				asyncMessage.setTimeToLive(publish.getTimeToLive());
				asyncMessage.setHeaders(publish.getHeaders());
				asyncMessage.setDestination(publish.getDestination());
				asyncMessage.setHeader(AsyncMessage.SUBTOPIC_HEADER, publish.getTopic());
				asyncMessage.setBody(publish.getBody());
				messages = new Message[]{asyncMessage};
				break;
			}
            case REPLY: {
                ReplyMessage reply = (ReplyMessage)request;
                AsyncMessage asyncMessage = new AsyncMessage();
                asyncMessage.setMessageId(reply.getId());
                asyncMessage.setTimestamp(reply.getTimestamp());
                asyncMessage.setTimeToLive(reply.getTimeToLive());
                asyncMessage.setHeaders(reply.getHeaders());
                asyncMessage.setDestination(reply.getDestination());
                asyncMessage.setHeader(AsyncMessage.SUBTOPIC_HEADER, reply.getTopic());
                asyncMessage.setCorrelationId(reply.getCorrelationId());
                asyncMessage.setBody(reply.getBody());
                messages = new Message[]{asyncMessage};
                break;
            }
			case SUBSCRIBE: {
				SubscribeMessage subscribe = (SubscribeMessage)request;
				CommandMessage commandMessage = new CommandMessage();
				commandMessage.setOperation(CommandMessage.SUBSCRIBE_OPERATION);
				commandMessage.setMessageId(subscribe.getId());
				commandMessage.setTimestamp(subscribe.getTimestamp());
				commandMessage.setTimeToLive(subscribe.getTimeToLive());
				commandMessage.setHeaders(subscribe.getHeaders());
				commandMessage.setDestination(subscribe.getDestination());
				commandMessage.setHeader(AsyncMessage.SUBTOPIC_HEADER, subscribe.getTopic());
				if (subscribe.getSelector() != null)
					commandMessage.setHeader(CommandMessage.SELECTOR_HEADER, subscribe.getSelector());
				messages = new Message[]{commandMessage};
				break;
			}
			case UNSUBSCRIBE: {
				UnsubscribeMessage unsubscribe = (UnsubscribeMessage)request;
				CommandMessage commandMessage = new CommandMessage();
				commandMessage.setOperation(CommandMessage.UNSUBSCRIBE_OPERATION);
				commandMessage.setMessageId(unsubscribe.getId());
				commandMessage.setTimestamp(unsubscribe.getTimestamp());
				commandMessage.setTimeToLive(unsubscribe.getTimeToLive());
				commandMessage.setHeaders(unsubscribe.getHeaders());
				commandMessage.setDestination(unsubscribe.getDestination());
				commandMessage.setHeader(AsyncMessage.SUBTOPIC_HEADER, unsubscribe.getTopic());
				commandMessage.setHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER, unsubscribe.getSubscriptionId());
				messages = new Message[]{commandMessage};
				break;
			}
			case INVOCATION: {
				List<Message> remotingMessages = new ArrayList<Message>();
				
				for (InvocationMessage invocation : (InvocationMessage)request) {
					RemotingMessage remotingMessage = new RemotingMessage();
					remotingMessage.setDestination(invocation.getServiceId());
					remotingMessage.setOperation(invocation.getMethod());
					remotingMessage.setBody(invocation.getParameters() != null ? invocation.getParameters() : new Object[0]);
					remotingMessage.setMessageId(invocation.getId());
					remotingMessage.setTimestamp(invocation.getTimestamp());
					remotingMessage.setTimeToLive(invocation.getTimeToLive());
					remotingMessage.setHeaders(invocation.getHeaders());
					remotingMessages.add(remotingMessage);
				}
				
				messages = remotingMessages.toArray(new Message[remotingMessages.size()]);
				break;
			}
			case DISCONNECT: {
				CommandMessage commandMessage = new CommandMessage();
				commandMessage.setOperation(CommandMessage.DISCONNECT_OPERATION);
				commandMessage.setMessageId(request.getId());
				commandMessage.setTimestamp(request.getTimestamp());
				commandMessage.setTimeToLive(request.getTimeToLive());
				commandMessage.setHeaders(request.getHeaders());
				messages = new Message[]{commandMessage};
				break;
			}
			default:
				throw new IllegalArgumentException("Unsupported message type: " + request);
		}
		
		for (Message message : messages) {
			message.setClientId(getClientId());
			message.setHeader(Message.ENDPOINT_HEADER, getId());
		}
		
		return messages;
	}
	
	protected TopicMessage convertFromAmf(AsyncMessage message) {
		return new TopicMessage(
			message.getMessageId(),
			(String)message.getClientId(),
			message.getTimestamp(),
			message.getTimeToLive(),
			message.getHeaders(),
			message.getBody()
		);
	}
	
	protected AbstractResponseMessage convertFromAmf(AcknowledgeMessage message) {
		if (message instanceof ErrorMessage) {
			ErrorMessage errorMessage = (ErrorMessage)message;
			
			Code code = Code.UNKNOWN;

			String flexCode = errorMessage.getFaultCode();
			if (ErrorMessage.CODE_SERVER_CALL_FAILED.equals(flexCode))
				code = Code.SERVER_CALL_FAILED;
			else if (SecurityServiceException.CODE_ACCESS_DENIED.equals(flexCode))
				code = Code.ACCESS_DENIED;
			else if (SecurityServiceException.CODE_INVALID_CREDENTIALS.equals(flexCode)) {
				authenticated = false;
				code = Code.INVALID_CREDENTIALS;
			}
			else if (SecurityServiceException.CODE_AUTHENTICATION_FAILED.equals(flexCode)) {
				authenticated = false;
				code = Code.AUTHENTICATION_FAILED;
			}
			else if (SecurityServiceException.CODE_NOT_LOGGED_IN.equals(flexCode)) {
				authenticated = false;
				code = Code.NOT_LOGGED_IN;
			}
			else if (SecurityServiceException.CODE_SESSION_EXPIRED.equals(flexCode)) {
				authenticated = false;
				code = Code.SESSION_EXPIRED;
			}
			else if ("Validation.Failed".equals(flexCode))
				code = Code.VALIDATION_FAILED;
			else if ("Persistence.OptimisticLock".equals(flexCode))
				code = Code.OPTIMISTIC_LOCK;
				
			FaultMessage fault = new FaultMessage( 
				errorMessage.getMessageId(),
				(String)errorMessage.getClientId(),
				errorMessage.getTimestamp(),
				errorMessage.getTimeToLive(),
				errorMessage.getHeaders(),
				errorMessage.getCorrelationId(),
				code,
				errorMessage.getFaultString(),
				errorMessage.getFaultDetail(),
				errorMessage.getRootCause(),
				errorMessage.getExtendedData()
			);
			
			if (code == Code.UNKNOWN)
				fault.setUnknownCode(flexCode);
			
			return fault;
		}
		
		return new ResultMessage( 
			message.getMessageId(),
			(String)message.getClientId(),
			message.getTimestamp(),
			message.getTimeToLive(),
			message.getHeaders(),
			message.getCorrelationId(),
			message.getBody()
		);
	}
}
