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
package org.granite.cdi;

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.manager.BeanManagerImpl;


public class Weld11ConversationManager implements CDIConversationManager {

	public Conversation initConversation(BeanManager beanManager, String conversationId) {
	    ConversationContext conversationContext = ((BeanManagerImpl)beanManager).instance().select(HttpConversationContext.class).get();
	    conversationContext.activate(conversationId);
	    
    	@SuppressWarnings("unchecked")
    	Bean<Conversation> conversationBean = (Bean<Conversation>)beanManager.getBeans(Conversation.class).iterator().next();
    	Conversation conversation = (Conversation)beanManager.getReference(conversationBean, Conversation.class, beanManager.createCreationalContext(conversationBean));
    	return conversation;
	}
	
	public void destroyConversation(BeanManager beanManager) {
	    ConversationContext conversationContext = ((BeanManagerImpl)beanManager).instance().select(HttpConversationContext.class).get();
	    conversationContext.deactivate();
	}
}
