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
package org.granite.tide.cdi;

import java.io.Serializable;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;


/**
 * TideEvents override to intercept CDI events handling
 * 
 * @author William DRAI
 */
public class TideEvents implements Serializable {
    
	private static final long serialVersionUID = 1L;

	@Inject
    private BeanManager manager;
    
    private boolean reentrant = false;
    
    
    public void processEvent(@Observes(notifyObserver=Reception.ALWAYS) @Any Object event) {
    	if (reentrant || TideInvocation.get() == null) {
    		// Ignore events outside of the current bean invocation
    		return;
    	}
    	
    	try {
    		reentrant = true;
    		// Cannot inject service context as this observer is called very early before the full app initialization
    		@SuppressWarnings("unchecked")
    		Bean<CDIServiceContext> scBean = (Bean<CDIServiceContext>)manager.getBeans(CDIServiceContext.class).iterator().next();
    		CDIServiceContext serviceContext = (CDIServiceContext)manager.getReference(scBean, CDIServiceContext.class, manager.createCreationalContext(scBean));
	    	if (serviceContext != null)
	    		serviceContext.processEvent(event);
    	}
    	catch (ContextNotActiveException e) {
    		// Ignore event, no session context
    	}
    	finally {
    		reentrant = false;
    	}
    }
}
