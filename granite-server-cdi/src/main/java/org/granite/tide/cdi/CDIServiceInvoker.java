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

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.servlet.http.HttpSession;

import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.tide.TideServiceInvoker;
import org.granite.tide.cdi.lazy.CDIInitializer;


/**
 * @author William DRAI
 */
public class CDIServiceInvoker extends TideServiceInvoker<CDIServiceFactory> {

	
    public CDIServiceInvoker(Destination destination, CDIServiceFactory factory) {
        super(destination, factory);
    } 
    
    
    @Override
    public void logout() {
        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpSession session = context.getSession(false);
        if (session != null)
        	session.invalidate();
    }
    
    @Override
    public Object initializeObject(Object parent, String[] propertyNames) {
    	@SuppressWarnings("unchecked")
    	Bean<CDIInitializer> iBean = (Bean<CDIInitializer>)factory.getManager().getBeans(CDIInitializer.class).iterator().next();
    	CDIInitializer initializer = (CDIInitializer)factory.getManager().getReference(iBean, CDIInitializer.class, factory.getManager().createCreationalContext(iBean));
    	
    	return initializer.lazyInitialize(parent, propertyNames);
    }
    
    @Override
    protected CDIServiceContext lookupContext() {
    	Bean<?> scBean = factory.getManager().getBeans(CDIServiceContext.class).iterator().next();
    	CreationalContext<?> cc = factory.getManager().createCreationalContext(scBean);
    	return (CDIServiceContext)factory.getManager().getReference(scBean, CDIServiceContext.class, cc);
    }
}
