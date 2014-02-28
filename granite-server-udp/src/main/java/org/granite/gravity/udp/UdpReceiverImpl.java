/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.gravity.udp;

import java.io.IOException;
import java.io.ObjectOutput;
import java.util.HashMap;

import org.granite.context.AMFContextImpl;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.gravity.AbstractChannel;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityInternal;
import org.granite.gravity.MessageReceivingException;
import org.granite.gravity.udp.UdpReceiver;
import org.granite.logging.Logger;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class UdpReceiverImpl implements UdpReceiver {

	private static final Logger log = Logger.getLogger(UdpReceiverImpl.class);
    
	public static final String GDS_CLIENT_UPD_PORT = "GDS_CLIENT_UDP_PORT";
	public static final String GDS_SERVER_UDP_PORT = "GDS_SERVER_UDP_PORT";

	private UdpChannel udpChannel;
	private Message connect;

	public UdpReceiverImpl(UdpChannel udpChannel, Message connect) {
		if (udpChannel == null || connect == null)
			throw new NullPointerException();
		
		this.udpChannel = udpChannel;
		this.connect = connect;
	}
	
	@Override
	public AsyncMessage acknowledge(Message connectMessage) {
		AcknowledgeMessage ack = new AcknowledgeMessage(connectMessage, true);
		ack.setHeader(GDS_SERVER_UDP_PORT, Double.valueOf(udpChannel.getServerPort()));
		return ack;
	}

	public void receive(AsyncMessage message) throws MessageReceivingException {
		AbstractChannel gravityChannel = udpChannel.getGravityChannel();
		GravityInternal gravity = gravityChannel.getGravity();
		
		message.setCorrelationId(connect.getMessageId());
        GraniteContext context = SimpleGraniteContext.createThreadInstance(
            gravity.getGraniteConfig(),
            gravity.getServicesConfig(),
            new HashMap<String, Object>()
        );
        try {
	        ((AMFContextImpl)context.getAMFContext()).setCurrentAmf3Message(connect);
	
	        UdpOutputStream os = new UdpOutputStream();
	        ObjectOutput out = gravityChannel.newSerializer(context, os);
	        out.writeObject(new Message[] {message});
	        
	        int sent = udpChannel.write(os.buffer(), 0, os.size());
	        if (sent != os.size())
	        	log.debug("Partial data sent: %d of %d", sent, os.size());
        }
        catch (IOException e) {
        	throw new MessageReceivingException(message, e);
        }
        finally {
        	GraniteContext.release();
        }
	}

	public boolean isClosed() {
		return udpChannel == null;
	}

	public void close(boolean timeout) {
		try {
			udpChannel.close();
		}
		finally {
			udpChannel = null;
			connect = null;
		}
	}
}
