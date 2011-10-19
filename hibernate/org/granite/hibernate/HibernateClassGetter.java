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

package org.granite.hibernate;

import java.io.Serializable;

import javax.persistence.Entity;

import org.granite.logging.Logger;
import org.granite.messaging.amf.io.util.DefaultClassGetter;
import org.granite.util.ClassUtil;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

/**
 * @author Franck WOLFF
 */
public class HibernateClassGetter extends DefaultClassGetter {

    private final static Logger log = Logger.getLogger(HibernateClassGetter.class);

    @Override
    public Class<?> getClass(Object o) {

        if (o instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy)o;
            LazyInitializer initializer = proxy.getHibernateLazyInitializer();
            
            String className = (
            	initializer.isUninitialized() ?
            	initializer.getEntityName() :
            	initializer.getImplementation().getClass().getName()
            ); 
            
            if (className != null && className.length() > 0) {
                try {
                    return ClassUtil.forName(className);
                } catch (Exception e) {
                    log.warn(e, "Could not get class with initializer: %s for: %s", initializer.getClass().getName(), className);
                }
            }
            // fallback...
            return initializer.getPersistentClass();
        }

        return super.getClass(o);
    }
    
    @Override
    public boolean isEntity(Object o) {
    	return o.getClass().isAnnotationPresent(Entity.class);    
    }

    @Override
    public Serializable getIdentifier(Object o) {
    	if (o instanceof HibernateProxy)
    		return ((HibernateProxy)o).getHibernateLazyInitializer().getIdentifier();
    	return null;
    }
    
    @Override
    public boolean isInitialized(Object owner, String propertyName, Object propertyValue) {
        return Hibernate.isInitialized(propertyValue);
    }
    
    @Override
    public void initialize(Object owner, String propertyName, Object propertyValue) {
        Hibernate.initialize(propertyValue);
    }
}
