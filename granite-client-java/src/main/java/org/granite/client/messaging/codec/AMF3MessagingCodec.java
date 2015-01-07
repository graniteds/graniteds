/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.messaging.codec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.channel.Channel;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.messaging.amf.io.AMF3Deserializer;
import org.granite.messaging.amf.io.AMF3Serializer;
import org.granite.util.ContentType;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class AMF3MessagingCodec implements MessagingCodec<Message[]> {

	private final Configuration config;
	
	public AMF3MessagingCodec(Configuration config) {
		this.config = config;
	}

	@Override
	public ClientType getClientType() {
		return config.getClientType();
	}

	@Override
	public String getContentType() {
		return ContentType.AMF.mimeType();
	}

	@Override
	public void encode(Message[] messages, OutputStream output) throws IOException {
		SimpleGraniteContext.createThreadInstance(config.getGraniteConfig(), config.getServicesConfig(), new HashMap<String, Object>(0), getClientType().toString());
		try {
			AMF3Serializer serializer = new AMF3Serializer(output);
			serializer.writeObject(messages);
			serializer.close();
		}
		finally {
			GraniteContext.release();
		}
	}

	@Override
	public Message[] decode(InputStream input) throws IOException {
		SimpleGraniteContext.createThreadInstance(config.getGraniteConfig(), config.getServicesConfig(), new HashMap<String, Object>(0), getClientType().toString());
		try {
			AMF3Deserializer deserializer = new AMF3Deserializer(input);
			Object[] objects = (Object[])deserializer.readObject();
			deserializer.close();
			
			if (objects != null) {
				Message[] messages = new Message[objects.length];
				System.arraycopy(objects, 0, messages, 0, objects.length);
				
				for (Message message : messages) {
					if (message != null && Boolean.TRUE.equals(message.getHeader(Channel.BYTEARRAY_BODY_HEADER))) {
						byte[] body = (byte[])message.getBody();
						deserializer = new AMF3Deserializer(new ByteArrayInputStream(body));
						message.setBody(deserializer.readObject());
						deserializer.close();
					}
				}
				
				return messages;
			}
			return new Message[0];
		}
		finally {
			GraniteContext.release();
		}
	}
}
