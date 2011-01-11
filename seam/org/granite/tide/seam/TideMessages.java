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

package org.granite.tide.seam;

import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

import org.granite.context.GraniteContext;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;

/**
 * TideMessages override to avoid problem with UIComponent targetted messages
 * Cannot be used with Seam 2.1.x
 * 
 * @author William DRAI
 */
@Scope(ScopeType.CONVERSATION)
@Name("org.jboss.seam.faces.facesMessages")
@AutoCreate
@Install(precedence=FRAMEWORK, classDependencies="javax.faces.context.FacesContext")
@BypassInterceptors
public class TideMessages extends FacesMessages {

    private static final long serialVersionUID = -5395975397632138270L;
    
    private List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();
    private Map<String, List<FacesMessage>> keyedFacesMessages = new HashMap<String, List<FacesMessage>>();
    
    
    /**
     * Add a FacesMessage that will be used
     * the next time a page is rendered.
     */
    @Override
    public void add(FacesMessage facesMessage) {
        GraniteContext context = GraniteContext.getCurrentInstance();
        if (context == null) {
            super.add(facesMessage);
            return;
        }
        
        if (facesMessage != null)
            facesMessages.add(facesMessage);
    }
    
    /**
     * Add a templated FacesMessage that will be used
     * the next time a page is rendered.
     */
    @Override
    public void add(Severity severity, String messageTemplate, Object... params) {
        GraniteContext context = GraniteContext.getCurrentInstance();
        if (context == null) {
            super.add(severity, messageTemplate, params);
            return;
        }
        
        facesMessages.add(createFacesMessage(severity, messageTemplate, params));
    }
    

    /**
     * Add a FacesMessage instance to a particular component id
     * @param clientId component client id (same as Flex component Id)
     * @param facesMessage message to add
     */
    @Override
    public void addToControl(String clientId, FacesMessage facesMessage) {
        GraniteContext context = GraniteContext.getCurrentInstance();
        if (context == null) {
            super.addToControl(clientId, facesMessage);
            return;
        }
        
        if (facesMessage != null) {
            List<FacesMessage> list = keyedFacesMessages.get(clientId);
            if (list == null) {
                list = new ArrayList<FacesMessage>();
                keyedFacesMessages.put(clientId, list);
            }
            list.add(facesMessage);
        }
    }
    
    
    /**
     * Get all faces messages that have already been added
     * to the context.
     * 
     * @return a list of messages
     */
    @Override
    public List<FacesMessage> getCurrentMessages() {
        GraniteContext context = GraniteContext.getCurrentInstance();
        if (context == null)
            return super.getCurrentMessages();
        
        List<FacesMessage> msgs = new ArrayList<FacesMessage>(facesMessages);
        for (List<FacesMessage> km : keyedFacesMessages.values())
            msgs.addAll(km);
        return msgs;
    }
    
    /**
     * Get all faces global messages that have already been added
     * to the context.
     * 
     * @return a list of global messages
     */
    @Override
    public List<FacesMessage> getCurrentGlobalMessages() {
        GraniteContext context = GraniteContext.getCurrentInstance();
        if (context == null)
            return super.getCurrentGlobalMessages();
        
        return new ArrayList<FacesMessage>(facesMessages);
    }
    
    /**
     * Get all faces messages that have already been added
     * to the control.
     * 
     * @return a list of messages
     */
    @Override
    public List<FacesMessage> getCurrentMessagesForControl(String clientId) {
        GraniteContext context = GraniteContext.getCurrentInstance();
        if (context == null)
            return super.getCurrentMessagesForControl(clientId);
        
        return new ArrayList<FacesMessage>(keyedFacesMessages.get(clientId));
    }
    
    
    /**
     * Clear facesMessages storing specific to Tide
     */
    public void clearTideMessages() {
        //Clear facesMessages storing specific to Tide
        if (facesMessages != null && facesMessages.size() > 0)
            facesMessages.clear();

        if (keyedFacesMessages != null && keyedFacesMessages.size() > 0)
            keyedFacesMessages.clear();
    }

    
    /**
     * Get messages
     */
    @Override
    public void beforeRenderResponse() {
        GraniteContext context = GraniteContext.getCurrentInstance();
        if (context != null) {
            for (Map.Entry<String, List<FacesMessage>> entry : keyedFacesMessages.entrySet()) {
                for (FacesMessage msg: entry.getValue())
                    FacesContext.getCurrentInstance().addMessage(entry.getKey(), msg);
            }
        }
        
        super.beforeRenderResponse();
    }
}
