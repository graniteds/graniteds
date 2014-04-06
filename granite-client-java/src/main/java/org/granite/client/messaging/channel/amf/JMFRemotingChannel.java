/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.client.messaging.channel.amf;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.granite.client.messaging.channel.AsyncToken;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.codec.JMFAMF0MessagingCodec;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.messages.responses.AbstractResponseMessage;
import org.granite.client.messaging.transport.DefaultTransportMessage;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.messaging.amf.AMF0Body;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.amf.AMF3Object;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class JMFRemotingChannel extends AbstractAMFChannel implements RemotingChannel {
	
	protected final MessagingCodec<AMF0Message> codec;
	protected volatile int index = 1;

	public JMFRemotingChannel(Transport transport, ClientSharedContext sharedContext, String id, URI uri, int maxConcurrentRequests) {
		super(transport, id, uri, maxConcurrentRequests);
		
		this.codec = new JMFAMF0MessagingCodec(sharedContext);
	}

    public JMFRemotingChannel(Transport transport, MessagingCodec<AMF0Message> codec, String id, URI uri, int maxConcurrentRequests) {
        super(transport, id, uri, maxConcurrentRequests);

        this.codec = codec;
    }

	@Override
	protected TransportMessage createTransportMessage(AsyncToken token) throws UnsupportedEncodingException {
		AMF0Message amf0Message = new AMF0Message();
		for (Message message : convertToAmf(token.getRequest())) {
		    AMF0Body body = new AMF0Body("", "/" + (index++), new Object[]{message}, AMF0Body.DATA_TYPE_AMF3_OBJECT);
		    amf0Message.addBody(body);
		}
		return new DefaultTransportMessage<AMF0Message>(token.getId(), false, clientId, null, amf0Message, codec);
	}

	@Override
	protected ResponseMessage decodeResponse(InputStream is) throws IOException {
		final AMF0Message amf0Message = codec.decode(is);
		final int messagesCount = amf0Message.getBodyCount();
		
		AbstractResponseMessage response = null, previous = null;
		
		for (int i = 0; i < messagesCount; i++) {
			AMF0Body body = amf0Message.getBody(i);
			
			if (!(body.getValue() instanceof AMF3Object))
				throw new RuntimeException("Message should be an AMF3Object: " + body.getValue());
			
			AMF3Object bodyObject = (AMF3Object)body.getValue();
			if (!(bodyObject.getValue() instanceof AcknowledgeMessage))
				throw new RuntimeException("Message should be an AcknowledgeMessage: " + bodyObject.getValue());
			
			AcknowledgeMessage message = (AcknowledgeMessage)bodyObject.getValue();
			AbstractResponseMessage current = convertFromAmf(message);
			
			if (response == null)
				response = previous = current;
			else {
				previous.setNext(current);
				previous = current;
			}
		}
		
		return response;
	}
}
