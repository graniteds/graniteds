/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/
package org.granite.cdi;

import java.lang.reflect.Method;

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.http.HttpSession;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.util.ClassUtil;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.manager.api.WeldManager;


public class Weld10ConversationManager implements CDIConversationManager {
	
	private static final Logger log = Logger.getLogger(Weld10ConversationManager.class);
	
	private Class<?> conversationManagerClass;
	private Class<Service> contextLifecycleClass;
	
	@SuppressWarnings("unchecked")
	public Weld10ConversationManager() {
		try {
			conversationManagerClass = ClassUtil.forName("org.jboss.weld.conversation.ConversationManager");
			contextLifecycleClass = (Class<Service>)ClassUtil.forName("org.jboss.weld.context.ContextLifecycle");
		}
		catch (Exception e) {
			log.error(e, "Could not load ConversationManager class");
		}
	}

	public Conversation initConversation(BeanManager beanManager, String conversationId) {
		try {
		    Bean<?> conversationManagerBean = beanManager.getBeans(conversationManagerClass).iterator().next();
		    Object conversationManager = beanManager.getReference(conversationManagerBean, conversationManagerClass, beanManager.createCreationalContext(conversationManagerBean));
		    conversationManagerClass.getMethod("beginOrRestoreConversation", String.class).invoke(conversationManager, conversationId);
		    
		    @SuppressWarnings("unchecked")
		    Bean<Conversation> conversationBean = (Bean<Conversation>)beanManager.getBeans(Conversation.class).iterator().next();
		    Conversation conversation = (Conversation)beanManager.getReference(conversationBean, Conversation.class, beanManager.createCreationalContext(conversationBean)); 
		    
		    HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
		    String cid = (String)conversation.getClass().getMethod("getUnderlyingId").invoke(conversation);
		    Object conversationContext = lookupConversationContext((WeldManager)beanManager);
		    Object beanStore = ClassUtil.newInstance("org.jboss.weld.servlet.ConversationBeanStore", new Class<?>[] { HttpSession.class, boolean.class, String.class },
		    		new Object[] { context.getSession(true), false, cid });
		    for (Method m : conversationContext.getClass().getMethods()) {
		    	if ("setBeanStore".equals(m.getName())) {
		    		m.invoke(conversationContext, beanStore);
		    		break;
		    	}
		    }
		    conversationContext.getClass().getMethod("setActive", boolean.class).invoke(conversationContext, true);
		    return conversation;
		}
		catch (Exception e) {
			throw new RuntimeException("Could not init conversation", e);
		}
	}
	
	public void destroyConversation(BeanManager beanManager) {
    	try {
		    Object conversationContext = lookupConversationContext((WeldManager)beanManager);
		    if ((Boolean)conversationContext.getClass().getMethod("isActive").invoke(conversationContext)) {
			    Bean<?> conversationManagerBean = beanManager.getBeans(conversationManagerClass).iterator().next();
			    Object conversationManager = beanManager.getReference(conversationManagerBean, conversationManagerClass, beanManager.createCreationalContext(conversationManagerBean));
			    conversationManagerClass.getMethod("cleanupConversation").invoke(conversationManager);
	    	}
	    }
		catch (Exception e) {
			throw new RuntimeException("Could not destroy conversation", e);
		}
	}
	
	private Object lookupConversationContext(WeldManager beanManager) throws Exception {
		Object contextLifecycle = beanManager.getServices().get(contextLifecycleClass);		
	    return contextLifecycle.getClass().getMethod("getConversationContext").invoke(contextLifecycle);
	}
}
