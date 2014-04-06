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
package org.granite.tide.seam.lazy;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.granite.config.ConvertersConfig;
import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.tide.TidePersistenceManager;
import org.jboss.seam.Entity;
import org.jboss.seam.util.Reflections;

/**
 * Manager responsible for the maintaining a refernce for the PersistenceContext(JPA). 
 * @author CIngram
 */
public class PersistenceContextManager implements TidePersistenceManager  {
	
	private EntityManager em;
	
	public PersistenceContextManager() {
	}
	
	public PersistenceContextManager(EntityManager em) {
		this.em = em;
	}
	
	/**
	 * Attach the passed in entity with the EntityManager.
	 * @param entity
	 * @return the attached entity object
	 */
	public Object attachEntity(Object entity, String[] propertyNames) {
		Object attachedEntity = null;
        ClassGetter getter = ((ConvertersConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getClassGetter();
		
		//the get is called to give the children a chance to override and
		//use the implemented method
		attachedEntity = fetchEntity(entity, propertyNames);

	    if (attachedEntity != null && propertyNames != null) {
	        for (int i = 0; i < propertyNames.length; i++) {
	        	try {
	        		Object initializedObj = Reflections.getGetterMethod(attachedEntity.getClass(), propertyNames[i]).invoke(attachedEntity);
		            
			        //This is here to make sure the list is forced to return a value while operating inside of a 
			        //session. Forcing the  initialization of object.
	                if (getter != null)
	                    getter.initialize(entity, propertyNames[i], initializedObj);
	        	}
	        	catch (Exception e) {
	        		throw new RuntimeException("Could not initialize entity " + attachedEntity, e);
	        	}
	        }
        }
		
		return attachedEntity;
	} 
	
	/**
	 * attaches the entity to the JPA context.
	 * @return the attached entity
	 */
	public Object fetchEntity(Object entity, String[] fetch) {
        Serializable id = (Serializable)Entity.forClass(entity.getClass()).getIdentifier(entity);
        if (id == null)
            return null;
        
        if (fetch == null || em.getDelegate().getClass().getName().indexOf(".hibernate.") < 0)
        	return em.find(entity.getClass(), id);
        
        for (String f : fetch) {
	        Query q = em.createQuery("select e from " + entity.getClass().getName() + " e left join fetch e." + f + " where e = :entity");
	        q.setParameter("entity", entity);
	        entity = q.getSingleResult();
        }
        return entity;
	}
}
