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

package org.granite.gravity.servlet3;

import java.io.ObjectOutput;
import java.io.OutputStream;

import org.granite.context.GraniteContext;
import org.granite.gravity.Gravity;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.messaging.jmf.SharedContext;
import org.granite.util.ContentType;

/**
 * @author Franck WOLFF
 */
public class JMFAsyncChannel extends AsyncChannel {

	private final SharedContext jmfSharedContext;

	public JMFAsyncChannel(Gravity gravity, String id, JMFAsyncChannelFactory factory, String clientType) {
        super(gravity, id, factory, clientType);
        
        this.jmfSharedContext = factory.getJmfSharedContext();
	}

	@Override
	public ObjectOutput newSerializer(GraniteContext context, OutputStream os) {
		return new JMFSerializer(os, jmfSharedContext);
	}

	@Override
	public String getSerializerContentType() {
		return ContentType.JMF_AMF.mimeType();
	}
}
