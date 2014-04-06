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
		@SuppressWarnings("all")
		JMFSerializer serializer = new JMFSerializer(output, sharedContext);
		serializer.writeObject(message);
	}

	@Override
	public AMF0Message decode(InputStream input) throws IOException {
		@SuppressWarnings("all")
		JMFDeserializer deserializer = new JMFDeserializer(input, sharedContext);
		try {
			return (AMF0Message)deserializer.readObject();
		}
		catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}
}
