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
package org.granite.gravity.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.granite.logging.Logger;


public class PolicyFileServer implements Runnable {
	
	private static final Logger log = Logger.getLogger(PolicyFileServer.class);
	
	
	private int serverPort = 843;
	private String[] allowDomains = {};
	private String[] allowPorts = {};
	private Thread policyServer = null;
	
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	
	public void setAllowDomains(String[] domains) {
		this.allowDomains = domains;
	}
	
	public void setAllowPorts(String[] ports) {
		this.allowPorts = ports;
	}
	
	public void start() {
		policyServer = new Thread(this, "FlashPolicyFileServer:" + serverPort);
		policyServer.start();
	}
	
	public void stop() {
		if (policyServer != null)
			policyServer.interrupt();
	}

	public void run() {
		ServerSocketChannel server = null;
		try {
			server = ServerSocketChannel.open();
			server.socket().bind(new java.net.InetSocketAddress(serverPort));
			log.info("Flash socket policy server started on port " + serverPort);
		}
		catch (IOException e) {
			log.error(e, "Could not init flash socket policy server on port " + serverPort);
			return;
		}
	    while (true) {
	    	SocketChannel socket = null;
		    try {
		    	socket = server.accept();
		    	
			    ByteBuffer buf = ByteBuffer.allocate(100);
			    int size = socket.read(buf);
			    if (size == 23) {
			    	byte[] req = new byte[size];
			    	buf.get(req, 0, size);
			    	String request = new String(req, "UTF-8");
			    	
			    	log.info("Received policy file request %s", request);
			    	
			    	String policyFile = "<?xml version=\"1.0\"?>\n"
			        	+ "<!DOCTYPE cross-domain-policy SYSTEM \"http://www.macromedia.com/xml/dtds/cross-domain-policy.dtd\">"
			        	+ "<cross-domain-policy>";
			    	for (int i = 0; i < allowDomains.length; i++)
			        	policyFile += "  <allow-access-from domain=\"" + allowDomains[i] + "\" to-ports=\"" + allowPorts[i] + "\"/>";
			        policyFile += "</cross-domain-policy>";		    	
			    	byte[] bytes = policyFile.getBytes("UTF-8");
			    	
			    	socket.write(ByteBuffer.wrap(bytes));
			    }
		    }
		    catch (ClosedByInterruptException e) {
				log.info("Flash socket policy server stopped");
		    	break;
		    }
			catch (IOException e) {
				log.error(e, "Could not send policy file");
			}
		    finally {
		    	if (socket != null) {
		    		try {
		    			socket.close();
		    		}
		    		catch (IOException e) {
						log.error(e, "Could not close socket");
		    		}
		    	}
		    }
	    }
	}
	
}