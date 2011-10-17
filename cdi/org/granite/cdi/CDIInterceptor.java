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

import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletRequestEvent;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.messaging.webapp.HttpServletRequestParamWrapper;
import org.granite.tide.cdi.ConversationState;
import org.granite.tide.cdi.EventState;
import org.granite.tide.cdi.SessionState;
import org.granite.util.ClassUtil;
import org.jboss.weld.servlet.WeldListener;

import flex.messaging.messages.Message;


public class CDIInterceptor implements AMF3MessageInterceptor {
	
	private static final Logger log = Logger.getLogger(CDIInterceptor.class);

    private static final String CONVERSATION_ID = "conversationId";
    private static final String IS_LONG_RUNNING_CONVERSATION = "isLongRunningConversation";
    private static final String WAS_LONG_RUNNING_CONVERSATION_CREATED = "wasLongRunningConversationCreated";
    private static final String WAS_LONG_RUNNING_CONVERSATION_ENDED = "wasLongRunningConversationEnded";
        
    private CDIConversationManager conversationManager;
    
    
    public CDIInterceptor() {
    	try {
    		Thread.currentThread().getContextClassLoader().loadClass("org.jboss.weld.context.http.HttpConversationContext");
    		conversationManager = ClassUtil.newInstance("org.granite.cdi.Weld11ConversationManager", CDIConversationManager.class);
    		log.info("Detected Weld 1.1");
    	}
    	catch (Exception e) {
    		try {
    			conversationManager = ClassUtil.newInstance("org.granite.cdi.Weld10ConversationManager", CDIConversationManager.class);
        		log.info("Detected Weld 1.0");
    		}
    		catch (Exception f) {
    			throw new RuntimeException("Could not load conversation manager for CDI implementation", f);
    		}
    	}
    }
    
    
	private WeldListener listener = new WeldListener();
	
	private static final String MESSAGECOUNT_ATTR = CDIInterceptor.class.getName() + "_messageCount";
	private static final String REQUESTWRAPPER_ATTR = CDIInterceptor.class.getName() + "_requestWrapper";
    
    
	public void before(Message amf3RequestMessage) {
		if (log.isTraceEnabled())
			log.trace("Pre processing of request message: %s", amf3RequestMessage);
		
		try {
			GraniteContext context = GraniteContext.getCurrentInstance();
			
			if (context instanceof HttpGraniteContext) {
				HttpGraniteContext httpContext = ((HttpGraniteContext)context);
				Integer wrapCount = (Integer)httpContext.getRequest().getAttribute(MESSAGECOUNT_ATTR);
				if (wrapCount == null) {
					log.debug("Clearing default Weld request context");
		    		ServletRequestEvent event = new ServletRequestEvent(httpContext.getServletContext(), httpContext.getRequest());
					listener.requestDestroyed(event);
					httpContext.getRequest().setAttribute(MESSAGECOUNT_ATTR, 1);
				}
				else
					httpContext.getRequest().setAttribute(MESSAGECOUNT_ATTR, wrapCount+1);
				
		        log.debug("Initializing wrapped AMF request");
		        
	            HttpServletRequestParamWrapper requestWrapper = new HttpServletRequestParamWrapper(httpContext.getRequest());
	            httpContext.getRequest().setAttribute(REQUESTWRAPPER_ATTR, requestWrapper);
				
        		// Now export the headers - copy the headers to request object
	    		Map<String, Object> headerMap = amf3RequestMessage.getHeaders();
	    		if (headerMap != null && headerMap.size() > 0) {
	    			Iterator<String> headerKeys = headerMap.keySet().iterator();
	    			while (headerKeys.hasNext()) {
	    				String key = headerKeys.next();
	    				String value = headerMap.get(key) == null ? null : headerMap.get(key).toString();
	    				if (value != null)
	    					requestWrapper.setParameter(key, value);
	    			}
	    		}
	            
	    		ServletRequestEvent event = new ServletRequestEvent(((HttpGraniteContext)context).getServletContext(), requestWrapper);
	    		listener.requestInitialized(event);
	            
        		// Initialize CDI Context
			    String conversationId = (String)amf3RequestMessage.getHeader(CONVERSATION_ID);
			    
			    BeanManager beanManager = CDIUtils.lookupBeanManager(((HttpGraniteContext)context).getServletContext());
			    
			    Conversation conversation = conversationManager.initConversation(beanManager, conversationId);
			    
			    @SuppressWarnings("unchecked")
			    Bean<EventState> eventBean = (Bean<EventState>)beanManager.getBeans(EventState.class).iterator().next();
			    EventState eventState = (EventState)beanManager.getReference(eventBean, EventState.class, beanManager.createCreationalContext(eventBean));
			    if (!conversation.isTransient())
			    	eventState.setWasLongRunning(true);
			    
			    if (conversationId != null && conversation.isTransient()) {
				    log.debug("Starting conversation " + conversationId);
				    conversation.begin(conversationId);
			    }
				
		        if (Boolean.TRUE.toString().equals(amf3RequestMessage.getHeader("org.granite.tide.isFirstCall"))) {
		        	@SuppressWarnings("unchecked")
		        	Bean<SessionState> ssBean = (Bean<SessionState>)beanManager.getBeans(SessionState.class).iterator().next();
		        	((SessionState)beanManager.getReference(ssBean, SessionState.class, beanManager.createCreationalContext(ssBean))).setFirstCall(true);
		        }
				
		        if (Boolean.TRUE.toString().equals(amf3RequestMessage.getHeader("org.granite.tide.isFirstConversationCall")) && !conversation.isTransient()) {
		        	@SuppressWarnings("unchecked")
		        	Bean<ConversationState> csBean = (Bean<ConversationState>)beanManager.getBeans(ConversationState.class).iterator().next();
		        	((ConversationState)beanManager.getReference(csBean, ConversationState.class, beanManager.createCreationalContext(csBean))).setFirstCall(true);
		        }
			}
		}
		catch(Exception e) {
            log.error(e, "Exception while pre processing the request message.");
            throw new ServiceException("Error while pre processing the request message - " + e.getMessage());
		}
	}


	public void after(Message amf3RequestMessage, Message amf3ResponseMessage) {		
		try {
			if (log.isTraceEnabled())
				log.trace("Post processing of response message: %s", amf3ResponseMessage);

			GraniteContext context = GraniteContext.getCurrentInstance();
			
			if (context instanceof HttpGraniteContext) {
			    BeanManager beanManager = CDIUtils.lookupBeanManager(((HttpGraniteContext)context).getServletContext());
				try {
					// Add conversation management headers to response
					if (amf3ResponseMessage != null) {
					    @SuppressWarnings("unchecked")
					    Bean<Conversation> conversationBean = (Bean<Conversation>)beanManager.getBeans(Conversation.class).iterator().next();
					    Conversation conversation = (Conversation)beanManager.getReference(conversationBean, Conversation.class, beanManager.createCreationalContext(conversationBean));
					    
					    @SuppressWarnings("unchecked")
					    Bean<EventState> eventBean = (Bean<EventState>)beanManager.getBeans(EventState.class).iterator().next();
					    EventState eventState = (EventState)beanManager.getReference(eventBean, EventState.class, beanManager.createCreationalContext(eventBean));
					    if (eventState.wasLongRunning() && !conversation.isTransient())
					    	amf3ResponseMessage.setHeader(WAS_LONG_RUNNING_CONVERSATION_ENDED, true);
						
			            if (eventState.wasCreated() && !conversation.isTransient())
			            	amf3ResponseMessage.setHeader(WAS_LONG_RUNNING_CONVERSATION_CREATED, true);
			            
			            amf3ResponseMessage.setHeader(CONVERSATION_ID, conversation.getId());
						
			            amf3ResponseMessage.setHeader(IS_LONG_RUNNING_CONVERSATION, !conversation.isTransient());
					}
				}
				finally {
					conversationManager.destroyConversation(beanManager);
					
					HttpGraniteContext httpContext = ((HttpGraniteContext)context);
				    
					// Destroy the CDI context
					HttpServletRequestParamWrapper requestWrapper = (HttpServletRequestParamWrapper)httpContext.getRequest().getAttribute(REQUESTWRAPPER_ATTR);
					httpContext.getRequest().removeAttribute(REQUESTWRAPPER_ATTR);
		    		ServletRequestEvent event = new ServletRequestEvent(httpContext.getServletContext(), requestWrapper);
		    		listener.requestDestroyed(event);
				    
			        log.debug("Destroying wrapped CDI AMF request");
			        
					Integer wrapCount = (Integer)httpContext.getRequest().getAttribute(MESSAGECOUNT_ATTR);
					if (wrapCount == 1) {
						log.debug("Restoring default Weld request context");
			    		event = new ServletRequestEvent(((HttpGraniteContext)context).getServletContext(), httpContext.getRequest());
						listener.requestInitialized(event);
						httpContext.getRequest().removeAttribute(MESSAGECOUNT_ATTR);
					}
					else
						httpContext.getRequest().setAttribute(MESSAGECOUNT_ATTR, wrapCount-1);
					
				}
			}
		}
		catch (Exception e) {
            log.error(e, "Exception while post processing the response message.");
            throw new ServiceException("Error while post processing the response message - " + e.getMessage());
		}
	}
}
