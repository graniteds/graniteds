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
package org.granite.client.test.javafx.tide;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
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
import org.granite.client.test.tide.MockAMFChannelFactory;
import org.granite.client.test.tide.MockInstanceStoreFactory;
import org.granite.client.test.tide.MockServiceFactory;
import org.granite.client.tide.Context;
import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.impl.DefaultApplication;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;
import org.granite.tide.invocation.InvocationResult;
import org.granite.util.ContentType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import flex.messaging.io.ArrayCollection;


public class TestSimpleCall {
    
    private SimpleContextManager contextManager;
    private Context ctx;
    private ServerSession serverSession;
    
    
    @Before
    public void setup() throws Exception {
    	contextManager = new SimpleContextManager(new DefaultApplication());
        contextManager.setInstanceStoreFactory(new MockInstanceStoreFactory());
        ctx = contextManager.getContext();
        serverSession = new ServerSession("/test", "localhost", 8080);
        serverSession.setContentType(ContentType.AMF);
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
    public void testSimpleCall() throws Exception {        
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
        
        // Create a new Person entity.
        Person person  = new Person();
        person.setFirstName("Franck");
        person.setLastName("Wolff");
        
        // Call the createPerson method on the destination (PersonService) with
        // the new person as its parameter.
        Future<Person> fperson = personService.call("createPerson", person);
        person = fperson.get();
        
        System.out.println("createPerson get(): " + person);
        Assert.assertEquals("Person", "Wolff", person.getLastName());
        
        System.out.println("Done.");
    }

    @Test
    public void testSimpleSetCall() throws Exception {        
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

//        		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//        		byte[] buf = new byte[10000];
//        		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
//        		ObjectInput in = graniteConfigHibernate.newAMF3Deserializer(bais);
//        		Object entity = in.readObject();
                
                Object result = null;
                if (method.equals("findAllPersons") && args.length == 0) {
                    ArrayCollection set = new ArrayCollection();
                    set.add(new Person());
                    set.add(new Person());
                    result = set;
                }
                else if (method.equals("findMapPersons") && args.length == 0) {
                	
                    ArrayCollection set = new ArrayCollection();
                    set.add(new Person());
                    set.add(new Person());
                    result = set;
                }
                else if (method.equals("createPerson") && args.length == 1) {
                    Person person = (Person)args[0];
                    Person p = new Person();
                    p.setFirstName(person.getFirstName());
                    p.setLastName(person.getLastName());
                    result = p;
                }
                
                InvocationResult invResult = new InvocationResult(result);
                return new ResultMessage(null, null, invResult);
            }
        });
        
        final Object[] res = new Object[1];
        Future<Set<Person>> fpersons = personService.call("findAllPersons", new TideResponder<Set<Person>>() {
			@Override
			public void result(TideResultEvent<Set<Person>> event) {
		        System.out.println("findAllPersons result(): " + event.getResult());
				res[0] = event.getResult();
			}

			@Override
			public void fault(TideFaultEvent event) {
				event.getFault();
			}
        });
        
        Set<Person> persons = fpersons.get();
        Object persons2 = res[0];
        
        // (Set<Person>)res[0];
        System.out.println("findAllPersons get(): " + persons);
        Assert.assertEquals("Persons result", 2, persons.size());
        Assert.assertSame("get() == TideResultEvent", persons, persons2);
        
        res[0] = null;
        final Semaphore sem = new Semaphore(0);
        
        personService.call("findAllPersons", new TideResponder<Set<Person>>() {
			@Override
			public void result(TideResultEvent<Set<Person>> event) {
				System.out.println("findAllPersons result(): " + event.getResult());
				res[0] = event.getResult();
				sem.release();
			}

			@Override
			public void fault(TideFaultEvent event) {
				event.getFault();
			}
        });
        
        sem.acquire();
        persons2 = res[0];
        Assert.assertTrue("Persons set", persons2 instanceof Set);
    }
    
    @Test
    public void testIOErrorCall() throws Exception {        
        Component personService = new ComponentImpl(serverSession);
        ctx.set("personService", personService);
        MockRemoteService.setShouldFail(true);
        
        final Semaphore sem = new Semaphore(0);
        
        final TideFaultEvent[] faultEvent = new TideFaultEvent[1];
        
        personService.call("findAllPersons", new TideResponder<List<Person>>() {
			@Override
			public void result(TideResultEvent<List<Person>> event) {
		        System.out.println("findAllPersons result(): " + event.getResult());
			}
			
			@Override
			public void fault(TideFaultEvent event) {
		        System.out.println("findAllPersons fault(): " + event.getFault());
				faultEvent[0] = event;
				sem.release();
			}
        });
        
        sem.acquire();
        
        Assert.assertNotNull("Fault async", faultEvent[0]);
    }
        
    @Test
    public void testIOErrorCall2() throws Exception {        
        Component personService = new ComponentImpl(serverSession);
        ctx.set("personService", personService);
        
        MockRemoteService.setShouldFail(true);
        final TideFaultEvent[] faultEvent = new TideFaultEvent[1];
        
        final Semaphore sem = new Semaphore(0);
        
        boolean exceptionThrown = false;
        try {
	        Future<List<Person>> res = personService.call("findAllPersons", new TideResponder<List<Person>>() {
				@Override
				public void result(TideResultEvent<List<Person>> event) {
				}
				
				@Override
				public void fault(TideFaultEvent event) {
			        System.out.println("findAllPersons fault(): " + event.getFault());
					faultEvent[0] = event;
					sem.release();
				}
	        });
	        
	        sem.acquire();
	        
	        res.get();
        }
        catch (ExecutionException e) {
        	exceptionThrown = true;
        }
        
        Assert.assertTrue("Exception sync", exceptionThrown);
        Assert.assertNotNull("Fault sync", faultEvent[0]);
    }
}
