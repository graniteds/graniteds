/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.client.messaging.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.messaging.amf.AMF0Message;
import org.granite.messaging.jmf.JMFDeserializer;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.util.ContentType;

/**
 * @author Franck WOLFF
 */
public class JMFAMF0MessagingCodec implements MessagingCodec<AMF0Message> {
	
	private final ClientSharedContext sharedContext;

	public JMFAMF0MessagingCodec(ClientSharedContext sharedContext) {
		this.sharedContext = sharedContext;
	}

	@Override
	public ClientType getClientType() {
		return ClientType.JAVA;
	}

	@Override
	public String getContentType() {
		return ContentType.JMF_AMF.mimeType();
	}

	@Override
	public void encode(AMF0Message message, OutputStream output) throws IOException {
		JMFSerializer serializer = new JMFSerializer(output, sharedContext);
		serializer.writeObject(message);
	}

	@Override
	public AMF0Message decode(InputStream input) throws IOException {
		JMFDeserializer deserializer = new JMFDeserializer(input, sharedContext);
		try {
			return (AMF0Message)deserializer.readObject();
		}
		catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}
}
