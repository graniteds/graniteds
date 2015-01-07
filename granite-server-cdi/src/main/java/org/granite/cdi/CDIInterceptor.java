/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.messaging.webapp.HttpServletRequestParamWrapper;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.tide.cdi.ConversationState;
import org.granite.tide.cdi.EventState;
import org.granite.tide.cdi.SessionState;
import org.granite.util.TypeUtil;

import flex.messaging.messages.Message;


public class CDIInterceptor implements AMF3MessageInterceptor {
	
	private static final Logger log = Logger.getLogger(CDIInterceptor.class);

    private static final String CONVERSATION_ID = "conversationId";
    private static final String IS_LONG_RUNNING_CONVERSATION = "isLongRunningConversation";
    private static final String WAS_LONG_RUNNING_CONVERSATION_CREATED = "wasLongRunningConversationCreated";
    private static final String WAS_LONG_RUNNING_CONVERSATION_ENDED = "wasLongRunningConversationEnded";
    
    private final CDIConversationManager conversationManager;
    private final ServletRequestListener requestListener;
    
    
    public CDIInterceptor() {
    	/**
    	 * Awful pile of hacks to detect the CDI container
    	 * and implement the corresponding behaviour
    	 */
        CDIConversationManager conversationManager = null;
        ServletRequestListener listener = null;
        try {
    		Thread.currentThread().getContextClassLoader().loadClass("org.jboss.weld.manager.BeanManagerLookupService");
            listener = TypeUtil.newInstance("org.jboss.weld.servlet.WeldListener", ServletRequestListener.class);
			if (listener instanceof ServletContextListener) {
				ServletGraniteContext graniteContext = (ServletGraniteContext)GraniteContext.getCurrentInstance();			
				((ServletContextListener)listener).contextInitialized(new ServletContextEvent(graniteContext.getServletContext()));
			}
    		conversationManager = TypeUtil.newInstance("org.granite.cdi.Weld11ConversationManager", CDIConversationManager.class);
    		log.info("Detected JBoss Weld 2.0+");
        }
        catch (Exception e1) {
	    	try {
	    		Thread.currentThread().getContextClassLoader().loadClass("org.jboss.weld.context.http.HttpConversationContext");
	    		conversationManager = TypeUtil.newInstance("org.granite.cdi.Weld11ConversationManager", CDIConversationManager.class);
	            listener = TypeUtil.newInstance("org.jboss.weld.servlet.WeldListener", ServletRequestListener.class);
	    		log.info("Detected JBoss Weld 1.1+");
	    	}
	    	catch (Exception e2) {
	            try {
	                conversationManager = null;
	                // Hacky way to initialize a request listener for OWB
	                listener = TypeUtil.newInstance("org.apache.webbeans.servlet.WebBeansConfigurationListener", ServletRequestListener.class);
	                Field wbcField = listener.getClass().getDeclaredField("webBeansContext");
	                wbcField.setAccessible(true);
	                Object webBeansContext = wbcField.get(listener);
	                Field lcField = listener.getClass().getDeclaredField("lifeCycle");
	                lcField.setAccessible(true);
	                Method wbcGetService = webBeansContext.getClass().getMethod("getService", Class.class);
	                Object lifecycle = wbcGetService.invoke(webBeansContext, TypeUtil.forName("org.apache.webbeans.spi.ContainerLifecycle"));
	                lcField.set(listener, lifecycle);
	                log.info("Detected Apache OpenWebBeans, conversation support disabled");
	            }
	            catch (Exception f2) {
	                log.warn("Unsupported CDI container, conversation support disabled");
	            }
	    	}
        }
        this.conversationManager = conversationManager;
        this.requestListener = listener;
    }
    
    
	private static final String MESSAGECOUNT_ATTR = CDIInterceptor.class.getName() + "_messageCount";
	private static final String REQUESTWRAPPER_ATTR = CDIInterceptor.class.getName() + "_requestWrapper";
    
    
	public void before(Message amf3RequestMessage) {
		if (log.isTraceEnabled())
			log.trace("Pre processing of request message: %s", amf3RequestMessage);
		
		try {
			GraniteContext context = GraniteContext.getCurrentInstance();
			
			if (context instanceof HttpGraniteContext) {
				HttpGraniteContext httpContext = ((HttpGraniteContext)context);

                BeanManager beanManager = CDIUtils.lookupBeanManager(((HttpGraniteContext)context).getServletContext());

				ServletRequestListener requestListener = getRequestListener();
                if (requestListener != null) {
                	try {
	                    Integer wrapCount = (Integer)httpContext.getRequest().getAttribute(MESSAGECOUNT_ATTR);
	                    if (wrapCount == null) {
	                        log.debug("Clearing default container request context");
	                        ServletRequestEvent event = new ServletRequestEvent(httpContext.getServletContext(), httpContext.getRequest());
	                        requestListener.requestDestroyed(event);
	                        httpContext.getRequest().setAttribute(MESSAGECOUNT_ATTR, 1);
	                    }
	                    else
	                        httpContext.getRequest().setAttribute(MESSAGECOUNT_ATTR, wrapCount+1);
                	}
                	catch (IllegalStateException e) {
                		// Weld 2.x ?
                	}
                	
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
                    requestListener.requestInitialized(event);
                }

                if (conversationManager != null) {
                    // Initialize CDI conversation context
                    String conversationId = (String)amf3RequestMessage.getHeader(CONVERSATION_ID);

                    Conversation conversation = conversationManager.initConversation(beanManager, conversationId);
                    
                    if (conversation != null) {
	                    @SuppressWarnings("unchecked")
	                    Bean<EventState> eventBean = (Bean<EventState>)beanManager.getBeans(EventState.class).iterator().next();
	                    EventState eventState = (EventState)beanManager.getReference(eventBean, EventState.class, beanManager.createCreationalContext(eventBean));
	                    if (!conversation.isTransient())
	                        eventState.setWasLongRunning(true);
	
	                    if (conversationId != null && conversation.isTransient()) {
	                        log.debug("Starting conversation " + conversationId);
	                        conversation.begin(conversationId);
	                    }
	
	                    if (Boolean.TRUE.toString().equals(amf3RequestMessage.getHeader("org.granite.tide.isFirstConversationCall")) && !conversation.isTransient()) {
	                        @SuppressWarnings("unchecked")
	                        Bean<ConversationState> csBean = (Bean<ConversationState>)beanManager.getBeans(ConversationState.class).iterator().next();
	                        ((ConversationState)beanManager.getReference(csBean, ConversationState.class, beanManager.createCreationalContext(csBean))).setFirstCall(true);
	                    }
                    }
                }

                if (Boolean.TRUE.toString().equals(amf3RequestMessage.getHeader("org.granite.tide.isFirstCall"))) {
                    @SuppressWarnings("unchecked")
                    Bean<SessionState> ssBean = (Bean<SessionState>)beanManager.getBeans(SessionState.class).iterator().next();
                    ((SessionState)beanManager.getReference(ssBean, SessionState.class, beanManager.createCreationalContext(ssBean))).setFirstCall(true);
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
					if (conversationManager != null && amf3ResponseMessage != null) {
					    @SuppressWarnings("unchecked")
					    Bean<Conversation> conversationBean = (Bean<Conversation>)beanManager.getBeans(Conversation.class).iterator().next();
					    Conversation conversation = (Conversation)beanManager.getReference(conversationBean, Conversation.class, beanManager.createCreationalContext(conversationBean));
					    
					    if (conversation != null) {
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
				}
				finally {
                    if (conversationManager != null)
					    conversationManager.destroyConversation(beanManager);
					
					HttpGraniteContext httpContext = ((HttpGraniteContext)context);
					
					ServletRequestListener requestListener = getRequestListener();
                    if (requestListener != null) {
                        // Destroy the CDI context
                        HttpServletRequestParamWrapper requestWrapper = (HttpServletRequestParamWrapper)httpContext.getRequest().getAttribute(REQUESTWRAPPER_ATTR);
                        httpContext.getRequest().removeAttribute(REQUESTWRAPPER_ATTR);
                        ServletRequestEvent event = new ServletRequestEvent(httpContext.getServletContext(), requestWrapper);
                        requestListener.requestDestroyed(event);
                        
                        log.debug("Destroying wrapped CDI AMF request");
                        
                        Integer wrapCount = (Integer)httpContext.getRequest().getAttribute(MESSAGECOUNT_ATTR);
                        if (wrapCount == 1) {
                            log.debug("Restoring default container request context");
                            event = new ServletRequestEvent(((HttpGraniteContext)context).getServletContext(), httpContext.getRequest());
                            requestListener.requestInitialized(event);
                            httpContext.getRequest().removeAttribute(MESSAGECOUNT_ATTR);
                        }
                        else
                            httpContext.getRequest().setAttribute(MESSAGECOUNT_ATTR, wrapCount-1);
                    }
				}
			}
		}
		catch (Exception e) {
            log.error(e, "Exception while post processing the response message.");
            throw new ServiceException("Error while post processing the response message - " + e.getMessage());
		}
	}
		
	private ServletRequestListener getRequestListener() {
		return requestListener;
	}
}
