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
package org.granite.client.test.websocket;

import org.granite.client.messaging.Producer;
import org.granite.client.messaging.channel.amf.AMFMessagingChannel;
import org.granite.client.messaging.transport.HTTPTransport;
import org.granite.client.messaging.transport.apache.ApacheAsyncTransport;
import org.junit.Test;

public class TestSendMessage {

	@Test
	public void testSendMessage() throws Exception {
		HTTPTransport transport = new ApacheAsyncTransport();		
		AMFMessagingChannel channel = null; //new AMFMessagingChannel(transport, "<id>", new URI("http://localhost:8080/shop-admin/gravityamf/amf"));
		
		transport.start();
		try {
			Producer producer = new Producer(channel, "wineshopTopic", "tideDataTopic");
			producer.publish("Hello world").get();
		}
		finally {
			transport.stop();
		}
	}
}
