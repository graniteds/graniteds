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
package org.granite.tide.seam21;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;

/**
 * StatusMessages implementation for Tide/Flex
 * May not be used if another view technology is used simultaneously
 * 
 * @author William DRAI
 */
@Scope(ScopeType.CONVERSATION)
@Name(StatusMessages.COMPONENT_NAME)
@Install(precedence=BUILT_IN-1, classDependencies="org.jboss.seam.international.StatusMessages")
@AutoCreate
@BypassInterceptors
public class TideStatusMessages extends StatusMessages {
   
    private static final long serialVersionUID = 1L;

    public void onBeforeRender() {
        doRunTasks();
    }
    
    public List<StatusMessage> getKeyedMessages(String id) {
        return instance().getKeyedMessages().get(id);
    }
    
    public List<StatusMessage> getGlobalMessages() {
        return instance().getMessages();
    } 
    
    public static TideStatusMessages instance() {
        if (!Contexts.isConversationContextActive())
            throw new IllegalStateException("No active conversation context");
        
        return (TideStatusMessages)Component.getInstance(StatusMessages.COMPONENT_NAME, ScopeType.CONVERSATION);
    }
}
