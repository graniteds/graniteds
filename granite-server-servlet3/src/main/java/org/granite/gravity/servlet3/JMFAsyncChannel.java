/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.gravity.servlet3;

import org.granite.context.GraniteContext;
import org.granite.gravity.GravityInternal;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.util.ContentType;

import java.io.ObjectOutput;
import java.io.OutputStream;

/**
 * @author Franck WOLFF
 */
public class JMFAsyncChannel extends AsyncChannel {

	public JMFAsyncChannel(GravityInternal gravity, String id, JMFAsyncChannelFactory factory, String clientType) {
        super(gravity, id, factory, clientType);
	}

	@Override
	public ObjectOutput newSerializer(GraniteContext context, OutputStream os) {
		return new JMFSerializer(os, getGravity().getSharedContext());
	}

	@Override
	public String getSerializerContentType() {
		return ContentType.JMF_AMF.mimeType();
	}
}
