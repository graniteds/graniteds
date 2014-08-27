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
package org.granite.cdi;

import java.lang.reflect.Method;

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.manager.BeanManagerImpl;


public class Weld11ConversationManager implements CDIConversationManager {

	public Conversation initConversation(BeanManager beanManager, String conversationId) {
		BeanManagerImpl beanManagerImpl = getBeanManagerImpl(beanManager);
		if (beanManagerImpl == null)
			return null;
		
	    ConversationContext conversationContext = beanManagerImpl.instance().select(HttpConversationContext.class).get();
	    if (!conversationContext.isActive())
	    	conversationContext.activate(conversationId);
	    
    	@SuppressWarnings("unchecked")
    	Bean<Conversation> conversationBean = (Bean<Conversation>)beanManager.getBeans(Conversation.class).iterator().next();
    	Conversation conversation = (Conversation)beanManager.getReference(conversationBean, Conversation.class, beanManager.createCreationalContext(conversationBean));
    	return conversation;
	}
	
	public void destroyConversation(BeanManager beanManager) {
		BeanManagerImpl beanManagerImpl = getBeanManagerImpl(beanManager);		
		if (beanManagerImpl == null)
			return;
		
	    ConversationContext conversationContext = beanManagerImpl.instance().select(HttpConversationContext.class).get();
	    conversationContext.deactivate();
	}
	
	private BeanManagerImpl getBeanManagerImpl(BeanManager beanManager) {
		if (beanManager instanceof BeanManagerImpl)
			return (BeanManagerImpl)beanManager;
		
		// Quick hack for Weld 2+
		try {
			Method m = beanManager.getClass().getMethod("delegate");
			return (BeanManagerImpl)m.invoke(beanManager);
		}
		catch (Exception e) {
			// Not Weld ??
		}
		return null;
	}
}
