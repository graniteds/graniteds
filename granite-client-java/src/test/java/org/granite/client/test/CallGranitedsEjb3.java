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
package org.granite.client.test;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.ResultFaultIssuesResponseListener;
import org.granite.client.messaging.channel.JMFChannelFactory;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.channel.UsernamePasswordCredentials;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.persistence.Persistence;
import org.granite.client.platform.Platform;
import org.granite.messaging.reflect.Property;


/**
 * @author Franck WOLFF
 */
public class CallGranitedsEjb3 {

	public static void main(String[] args) throws Exception {		

		// Create and initialize a JMF channel factory.
		JMFChannelFactory channelFactory = new JMFChannelFactory();
		channelFactory.start();
		
		try {
			// Create a remoting channel bound to the server uri and with maximum two concurrent requests. 
			RemotingChannel channel = channelFactory.newRemotingChannel(
				"my-graniteamf",
				new URI("http://localhost:8080/graniteds-ejb3/graniteamf/amf"),
				2
			);
	
			// Login (credentials will be sent with the first call).
			channel.setCredentials(new UsernamePasswordCredentials("admin", "admin"));
	
			// Create a remote object with the channel and a destination.
			RemoteService ro = new RemoteService(channel, "person");
	
			final Semaphore sem = new Semaphore(0);
			
			ResponseListener listener = new ResultFaultIssuesResponseListener() {
				
				@Override
				public void onResult(ResultEvent event) {
					Persistence persistence = Platform.persistence();
					
					StringBuilder sb = new StringBuilder("onResult {");
					for (ResponseMessage response : event.getResponse()) {
						sb.append("\n    response=").append(response.toString().replace("\n", "\n    "));
	
						List<?> data = (List<?>)response.getData();
						
						for (Object entity : data) {
							sb.append("\n\n    " + entity.getClass().getName() + " {");
							try {
								sb.append("\n        boolean initialized: " + persistence.isInitialized(entity));
								Property property = persistence.getIdProperty(entity.getClass());
								sb.append("\n        * id: " + property.getType().getName() + " " + property.getName() + ": " + property.getObject(entity));
								property = persistence.getVersionProperty(entity.getClass());
								sb.append("\n        " + property.getType().getName() + " " + property.getName() + ": " + property.getObject(entity));
								property = persistence.getUidProperty(entity.getClass());
								sb.append("\n        " + property.getType().getName() + " " + property.getName() + ": " + property.getObject(entity));
							}
							catch (Exception e) {
								e.printStackTrace();
							}
							sb.append("\n    }");
						}
	
					}
					sb.append("\n}");
					System.out.println(sb);
					
					sem.release();
				}
	
				@Override
				public void onFault(FaultEvent event) {
					StringBuilder sb = new StringBuilder("onFault {");
					for (ResponseMessage response : event.getResponse())
						sb.append("\n    response=").append(response.toString().replace("\n", "\n    "));
					sb.append("\n}");
					System.out.println(sb);
	
					sem.release();
				}
				
				@Override
				public void onIssue(IssueEvent event) {
					System.out.println(event);
					sem.release();
				}
			};
			
			ro.newInvocation("findAllPersons").addListener(listener).appendInvocation("findAllCountries").invoke();
			ro.newInvocation("findAllPersons").addListener(listener).setTimeToLive(5, TimeUnit.MINUTES).invoke();
			ro.newInvocation("findAllPersons").addListener(listener).invoke();
			ro.newInvocation("findAllPersons").addListener(listener).invoke();
			ro.newInvocation("findAllPersons").setTimeToLive(10, TimeUnit.MILLISECONDS).addListener(listener).invoke();
	
			sem.acquire(5);
		}
		finally {
			// Stop channel factory (must be done!)
			channelFactory.stop();
		}
		
		System.out.println("Done.");
	}
}
