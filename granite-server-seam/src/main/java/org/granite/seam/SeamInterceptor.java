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
package org.granite.seam;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.messaging.webapp.HttpServletRequestParamWrapper;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.core.ConversationPropagation;
import org.jboss.seam.core.Manager;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.servlet.ServletRequestSessionMap;
import org.jboss.seam.util.Reflections;
import org.jboss.seam.web.ServletContexts;

import flex.messaging.messages.Message;


public class SeamInterceptor implements AMF3MessageInterceptor {
	
	private static final Logger log = Logger.getLogger(SeamInterceptor.class);

    private static final String CONVERSATION_ID = "conversationId";
    private static final String PARENT_CONVERSATION_ID = "parentConversationId";
    private static final String IS_LONG_RUNNING_CONVERSATION = "isLongRunningConversation";
    private static final String WAS_LONG_RUNNING_CONVERSATION_CREATED = "wasLongRunningConversationCreated";
    private static final String WAS_LONG_RUNNING_CONVERSATION_ENDED = "wasLongRunningConversationEnded";
	private static final String MESSAGE_HEADER = "MESSAGE_HEADER";
	private static final String MSG_SEP = ":;:";
    
	public void before(Message amfReqMessage) {
		if (log.isTraceEnabled())
			log.trace("Pre processing of request message: %s", amfReqMessage);

		try {
			GraniteContext context = GraniteContext.getCurrentInstance();
			
			if (context instanceof HttpGraniteContext) {
	            log.debug("Creating custom HttpServletRequest wrapper");
	            HttpServletRequestParamWrapper request = new HttpServletRequestParamWrapper(((HttpGraniteContext)context).getRequest());

	            //Initialize the FacesContext for each body request
				initializeFacesContext(request, (HttpGraniteContext)context);
				
        		//Now export the headers - copy the headers to request object
        		exportHeaders(request, amfReqMessage);
				
        		//Time to initialize Seam Context
        		initializeSeamContext(request);
			}
		}
		catch(Exception e) {
            log.error(e, "Exception while pre processing the request message.");
            throw new ServiceException("Error while pre processing the request message - " + e.getMessage());
		}
	}

	public void after(Message amfReqMessage, Message amfRespMessage) {		
		try {
			if (log.isTraceEnabled())
				log.trace("Post processing of response message: %s", amfReqMessage);

			if (GraniteContext.getCurrentInstance() instanceof HttpGraniteContext) {
				try {
					//Now time to set back the headers, always has one body
					importHeaders(amfRespMessage);
				}
				finally {
					//Time to destroy the seam context
					destroySeamContext();
				}
			}
		}
		catch (Exception e) {
            log.error(e, "Exception while post processing the response message.");
            throw new ServiceException("Error while post processing the response message - " + e.getMessage());
		}
		finally {
            //Release the FacesContext - clears messages etc.,
			FacesContext context = FacesContext.getCurrentInstance();
			if (context != null)
				context.release();		
		}
	}

	/**
	 * Reads the AMF request header and populate them in the request object
	 * @param request - HttpServletRequestParamWrapper
	 * @param amf3RequestMessage
	 */
	private void exportHeaders(HttpServletRequestParamWrapper request, Message amf3RequestMessage) {
		//Read the headers from first body
		Map<String, Object> headerMap = amf3RequestMessage.getHeaders();
		if (headerMap != null && headerMap.size() > 0) {
			Iterator<String> headerKeys = headerMap.keySet().iterator();
			while (headerKeys.hasNext()) {
				String key = headerKeys.next();
				String value = headerMap.get(key) == null ? null : headerMap.get(key).toString();
				if( value != null) {
					request.setParameter(key, value);
				}
			}
		}
	}

	/**
     * Update the AMF response message with the conversationId and other parameters.
     * @param amf3ResponseMessage
     */
	private void importHeaders(Message amf3ResponseMessage) {
		if (amf3ResponseMessage != null) {
			Conversation conversation = Conversation.instance();
            if (Contexts.getEventContext().isSet("org.granite.tide.conversation.wasLongRunning") && !conversation.isLongRunning())
            	amf3ResponseMessage.setHeader(WAS_LONG_RUNNING_CONVERSATION_ENDED, true);
			
            if (Contexts.getEventContext().isSet("org.granite.tide.conversation.wasCreated") && conversation.isLongRunning())
            	amf3ResponseMessage.setHeader(WAS_LONG_RUNNING_CONVERSATION_CREATED, true);
            
			log.debug("CONVERSATION_ID: %s", conversation.getId());
			amf3ResponseMessage.setHeader(CONVERSATION_ID, conversation.getId());
			
			log.debug("PARENT_CONVERSATION_ID: %s", conversation.getParentId());
			amf3ResponseMessage.setHeader(PARENT_CONVERSATION_ID, conversation.getParentId());
			
			log.debug("IS_LONG_RUNNING_CONVERSATION: %s", conversation.isLongRunning());
			amf3ResponseMessage.setHeader(IS_LONG_RUNNING_CONVERSATION, conversation.isLongRunning());
			
			log.debug("Processing the Faces messages.");
			processFacesMessages(amf3ResponseMessage);
		}
	}
		
	/**
	 * Process the faces messages and sets to the response header.
	 * @param amf3ResponseMessage
	 */
	private void processFacesMessages(Message amf3ResponseMessage) {
		if (amf3ResponseMessage != null) {
	        //Prepare for the messages. First step is convert the tasks to Seam FacesMessages
	        FacesMessages.afterPhase();
	
	        //Second step is add the Seam FacesMessages to JSF FacesContext Messages
	        FacesMessages.instance().beforeRenderResponse();
	
	        //TODO - Work on how effective we can pass the messages to Flex side
	        Iterator<FacesMessage> messageItr = FacesContext.getCurrentInstance().getMessages();
	        log.debug("Found Messages: %b", messageItr.hasNext());
	        StringBuilder messagesBuf = new StringBuilder();
	        while (messageItr.hasNext()) {
	            FacesMessage msg = messageItr.next();
	            log.debug("FacesMessages %s - %s", msg.getDetail(), msg.getSummary());
	            messagesBuf.append(msg.getSummary());
	            messagesBuf.append(MSG_SEP);
	        }
	
	        String messageStr = messagesBuf.toString().trim();
	
	        if (messageStr.length() > 0) {
	            messageStr = messageStr.substring(0, messageStr.lastIndexOf(MSG_SEP));
	            amf3ResponseMessage.setHeader(MESSAGE_HEADER, messageStr);
	        }
		}
	}

	/**
	 * Initialize the Seam Context
	 * @param request - HttpServletRequest
	 */
	private void initializeSeamContext(HttpServletRequest request) {
		log.debug("beginning request");
		
		ServletLifecycle.beginRequest(request);
		ServletContexts.instance().setRequest(request);
        
        Manager.instance().setConversationIdParameter(CONVERSATION_ID);
		restoreConversationId();
        String conversationId = ConversationPropagation.instance().getConversationId();
        Manager.instance().restoreConversation();
        ServletLifecycle.resumeConversation(request);
        handleConversationPropagation();
        if (conversationId != null && !conversationId.equals(Manager.instance().getCurrentConversationId()))
            Manager.instance().updateCurrentConversationId(conversationId);
        if (Manager.instance().isLongRunningConversation())
        	Contexts.getEventContext().set("org.granite.tide.conversation.wasLongRunning", true);
		
		// Force creation of the session
		if (request.getSession(false) == null)
			request.getSession(true);
		
        if (Boolean.TRUE.toString().equals(request.getParameter("org.granite.tide.isFirstCall")))
        	Contexts.getSessionContext().set("org.granite.tide.isFirstCall", Boolean.TRUE);
		
        if (Boolean.TRUE.toString().equals(request.getParameter("org.granite.tide.isFirstConversationCall")) && Manager.instance().isLongRunningConversation())
        	Contexts.getConversationContext().set("org.granite.tide.isFirstConversationCall", Boolean.TRUE);
	}
	
	/**
	 * Destroy the Seam Context
	 * @param request - HttpServletRequest
	 */
	private void destroySeamContext() {
        // Flush current conversation metadata if needed
        if (Manager.instance().isLongRunningConversation()) {
        	Conversation conversation = Conversation.instance();
        	try {
	        	Method method = conversation.getClass().getDeclaredMethod("flush");
	        	method.setAccessible(true);
	        	Reflections.invoke(method, conversation);
        	}
        	catch (Exception e) {
        		log.error("Could not flush current long-running conversation " + conversation.getId(), e);
        	}
        }
        
		//Retrieve the stored request from Seam Servlet Context
        Manager.instance().endRequest( new ServletRequestSessionMap(ServletContexts.getInstance().getRequest()) );
        ServletLifecycle.endRequest(ServletContexts.getInstance().getRequest());
        
        log.debug("ended request");
	}

	/**
	 * Returns facesContext if already exists otherwise creates a new one.
	 * @param request - HttpServletRequest
	 * @param context - HttpGraniteContext
	 * @return FacesContext
	 * @throws ServletException
	 */
    private FacesContext initializeFacesContext(HttpServletRequest request, HttpGraniteContext context) throws ServletException {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();

            if (facesContext != null)
                return facesContext;
            
            //Use the FactoryFinder to find the Lifecycle object
            FacesContextFactory contextFactory = (FacesContextFactory)
            FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            LifecycleFactory lifecycleFactory = (LifecycleFactory)
            FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

            facesContext = contextFactory.getFacesContext(context.getServletContext(), request, context.getResponse(), lifecycle);
            //Set as current FacesContext
            InnerFacesContext.setFacesContextAsCurrentInstance(facesContext);

            return facesContext;
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    /**
     * 
     */
    protected void handleConversationPropagation() {
       Manager.instance().handleConversationPropagation( ServletContexts.getInstance().getRequest().getParameterMap() );
    }

    /**
     * 
     */
    protected void restoreConversationId() {
       ConversationPropagation.instance().restoreConversationId( ServletContexts.getInstance().getRequest().getParameterMap() );
    }
    
    /**
     * Create a inner class to make a call to setCurrentInstance. 
     */
    private abstract static class InnerFacesContext extends FacesContext {
        protected static void setFacesContextAsCurrentInstance(FacesContext facesContext) {
            FacesContext.setCurrentInstance(facesContext);
        }
    }
}
