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

package org.granite.client.test.javafx.tide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.messages.Message;
import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.requests.InvocationMessage;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.ResultMessage;
import org.granite.client.test.MockRemoteService;
import org.granite.client.test.ResponseBuilder;
import org.granite.client.test.tide.MockServiceFactory;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextManager;
import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.impl.DefaultApplication;
import org.granite.client.tide.impl.SimpleEventBus;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.client.tide.spring.SpringContextManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestSpringCall {
    
    private ContextManager contextManager;
    private Context ctx;
    private ServerSession serverSession;
    
    
    @Before
    public void setup() throws Exception {
    	contextManager = new SpringContextManager(new DefaultApplication(), new SimpleEventBus());
        ctx = contextManager.getContext();
        serverSession = new ServerSession("/test", "localhost", 8080);
        serverSession.setChannelFactoryClass(MockAMFChannelFactory.class);
        serverSession.setServiceFactory(new MockServiceFactory());
        ctx.set(serverSession);
        serverSession.start();
    }
    
    @After
    public void tearDown() throws Exception {
    	serverSession.stop();
    }
    
    @Test
    public void testSimpleSpringCall() throws Exception {        
        Component personService = new ComponentImpl(serverSession);
        ctx.set("personService", personService);
        MockRemoteService.setResponseBuilder(new ResponseBuilder() {
            @Override
            public Message buildResponseMessage(RemoteService service, RequestMessage request) {
            	InvocationMessage invocation = (InvocationMessage)request;
            	
                if (!invocation.getParameters()[0].equals("personService"))
                    return new FaultMessage();
                
                String method = (String)invocation.getParameters()[2];
                Object[] args = ((Object[])invocation.getParameters()[3]);
                
                if (method.equals("findAllPersons") && args.length == 0) {
                    List<Person> list = new ArrayList<Person>();
                    list.add(new Person());
                    list.add(new Person());
                    return new ResultMessage(null, null, list);
                }
                else if (method.equals("createPerson") && args.length == 1) {
                    Person person = (Person)args[0];
                    Person p = new Person();
                    p.setFirstName(person.getFirstName());
                    p.setLastName(person.getLastName());
                    return new ResultMessage(null, null, p);
                }
                return null;
            }
        });
        
        Future<List<Person>> fpersons = personService.call("findAllPersons", new TideResponder<List<Person>>() {
			@Override
			public void result(TideResultEvent<List<Person>> event) {
		        System.out.println("findAllPersons result(): " + event.getResult());
			}
			
			@Override
			public void fault(TideFaultEvent event) {
				event.getFault();
			}
        });
        
        List<Person> persons = fpersons.get();
        
        System.out.println("findAllPersons get(): " + persons);
        Assert.assertEquals("Persons result", 2, persons.size());
    }
    
    @Test
    public void testRetryAfterFaultSpringCall() throws Exception {        
        Component personService = new ComponentImpl(serverSession);
        ctx.set("personService", personService);
        
        MockRemoteService.setResponseBuilder(new ResponseBuilder() {
            @Override
            public Message buildResponseMessage(RemoteService service, RequestMessage request) {
            	InvocationMessage invocation = (InvocationMessage)request;
            	
                if (!invocation.getParameters()[0].equals("personService"))
                    return new FaultMessage();
                
                String method = (String)invocation.getParameters()[2];
                Object[] args = ((Object[])invocation.getParameters()[3]);
                
                if (method.equals("findAllPersons") && args.length == 0) {
                    List<Person> list = new ArrayList<Person>();
                    list.add(new Person());
                    list.add(new Person());
                    return new ResultMessage(null, null, list);
                }
                else if (method.equals("createPerson") && args.length == 1) {
                    Person person = (Person)args[0];
                    Person p = new Person();
                    p.setFirstName(person.getFirstName());
                    p.setLastName(person.getLastName());
                    return new ResultMessage(null, null, p);
                }
                return null;
            }
        });
        
        MockRemoteService.setShouldFail(true);
        
        final TideFaultEvent[] faultEvent = new TideFaultEvent[1];
        @SuppressWarnings("unchecked")
		final List<Person>[] result = new List[1];
        
        final Semaphore sem = new Semaphore(0);
        
        personService.call("findAllPersons", new TideResponder<List<Person>>() {
			@Override
			public void result(TideResultEvent<List<Person>> event) {
				result[0] = event.getResult();
				sem.release();
			}
			
			@Override
			public void fault(TideFaultEvent event) {
				faultEvent[0] = event;
				sem.release();
			}
        });
        
        sem.acquire();
        
        Assert.assertNotNull("Fault", faultEvent[0]);

        Thread.sleep(500);
                
        faultEvent[0].retry();
        
        sem.acquire();
        
        List<Person> persons = result[0];
        System.out.println("findAllPersons get(): " + persons);
        Assert.assertEquals("Persons result", 2, persons.size());
    }

}
