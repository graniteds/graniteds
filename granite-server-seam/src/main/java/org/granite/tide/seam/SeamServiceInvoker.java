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
package org.granite.tide.seam;

import javax.servlet.http.HttpSession;

import org.granite.config.flex.Destination;
import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.tide.TideServiceInvoker;
import org.granite.tide.hibernate.HibernateValidator;
import org.granite.tide.seam.lazy.SeamInitializer;
import org.granite.tide.validators.InvalidValue;
import org.hibernate.validator.ClassValidator;
import org.jboss.seam.Component;
import org.jboss.seam.core.Validators;
import org.jboss.seam.security.Identity;


/**
 * @author William DRAI
 */
public class SeamServiceInvoker extends TideServiceInvoker<SeamServiceFactory> {
    
     
    public SeamServiceInvoker(Destination destination, SeamServiceFactory factory) {
        super(destination, factory);
    }
    
    @Override
    protected void initValidator() {    	
    }
    
    
    @Override
    public void logout() {
        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpSession session = context.getSession(false);
        if (session != null) {
            Identity identity = Identity.instance();
            if (identity.isLoggedIn())
                identity.logout();
        }
    }
    
    @Override
    public Object initializeObject(Object parent, String[] propertyNames) {
    	AbstractSeamServiceContext context = (AbstractSeamServiceContext)getTideContext();
    	
    	SeamInitializer s = (SeamInitializer)context.findComponent("org.granite.tide.seam.seamInitializer", SeamInitializer.class, null);
    	
    	return s.lazyInitialize(parent, propertyNames);
    }
    
    @Override
    public InvalidValue[] validateObject(Object entity, String propertyName, Object value) {
        ClassValidator<?> validator = Validators.instance().getValidator(entity);
        
        org.hibernate.validator.InvalidValue[] invalidValues = validator.getPotentialInvalidValues(propertyName, value);
        return HibernateValidator.convertInvalidValues(invalidValues);
    }
    
    @Override
    protected AbstractSeamServiceContext lookupContext() {
        return (AbstractSeamServiceContext)Component.getInstance(AbstractSeamServiceContext.COMPONENT_NAME, true);
    }
}
