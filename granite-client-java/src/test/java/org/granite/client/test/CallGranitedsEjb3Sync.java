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

import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.channel.JMFChannelFactory;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.channel.UsernamePasswordCredentials;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.test.model.Person;
import org.granite.client.test.model.Person.Salutation;

/**
 * @author Franck WOLFF
 */
public class CallGranitedsEjb3Sync {

	public static void main(String[] args) throws Exception {		
		
		// Create and initialize a JMF channel factory.
		JMFChannelFactory channelFactory = new JMFChannelFactory();
		channelFactory.start();

		try {
			RemotingChannel channel = channelFactory.newRemotingChannel(
				"my-graniteamf",
				new URI("http://localhost:8080/graniteds-ejb3/graniteamf/amf"),
				2
			);
	
			// Login (credentials will be sent with the first call).
			channel.setCredentials(new UsernamePasswordCredentials("admin", "admin"));
	
			// Create a remote object with the channel and a destination.
			RemoteService ro = new RemoteService(channel, "person");
		
			System.out.println();
			System.out.println("Fetching all persons and countries at once...");
			
			ResponseMessage message = ro.newInvocation("findAllPersons").appendInvocation("findAllCountries").invoke().get();
			for (ResponseMessage response : message)
				System.out.println(response);
			
			System.out.println();
			System.out.println("Creating new person...");
			
			// Create a new Person entity.
			Person person  = new Person();
			person.setSalutation(Salutation.Mr);
			person.setFirstName("John");
			person.setLastName("Doe");
			
			// Call the createPerson method on the destination (PersonService) with
			// the new person as its parameter.
			message = ro.newInvocation("createPerson", person).invoke().get();
			System.out.println(message);
			
			System.out.println();
			System.out.println("Fetching all persons...");

			message = ro.newInvocation("findAllPersons").invoke().get();
			System.out.println(message);
		}
		finally {
			// Stop channel factory (must be done!)
			channelFactory.stop();
		}
		
		System.out.println("Done.");
	}
}
