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
package org.granite.tide.data;

import org.granite.config.ConvertersConfig;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.tide.TidePersistenceManager;
import org.granite.tide.TideTransactionManager;
import org.granite.util.Reflections;


/**
 * Responsible for attaching a entity with the entity mangager
 * @author cingram
 *
 */
public abstract class AbstractTidePersistenceManager implements TidePersistenceManager {
	
	private static final Logger log = Logger.getLogger(AbstractTidePersistenceManager.class);
	
	protected TideTransactionManager tm;

	
	public AbstractTidePersistenceManager(TideTransactionManager tm) {
		this.tm = tm;
    	if (this.tm == null)
    		throw new RuntimeException("transaction manager is null");
	}
	
	
	public Object attachEntity(Object entity, String[] propertyNames) {
		return attachEntity(this, entity, propertyNames);
	}
	
	/**
	 * Attach the passed in entity with the EntityManager.
	 * @param entity
	 * @return the attached entity object
	 */
	public Object attachEntity(TidePersistenceManager pm, Object entity, String[] propertyNames) {
		Object attachedEntity = null;
        ClassGetter getter = ((ConvertersConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getClassGetter();
        
        try {
			Object tx = tm.begin(pm instanceof TideTransactionPersistenceManager ? (TideTransactionPersistenceManager)pm : null);
			if (tx == null)
			    throw new RuntimeException("Could not initiate transaction for lazy initialization");
			
			try {
	            //the get is called to give the children a chance to override and
	            //use the implemented method
				if (propertyNames != null)
					attachedEntity = fetchEntity(entity, propertyNames);
				else
					attachedEntity = entity;
	            
	            if (attachedEntity != null && propertyNames != null) {
	                for (int i = 0; i < propertyNames.length; i++) {
	                	Object initializedObj = attachedEntity;
	                	String[] pnames = propertyNames[i].split("\\.");
	                	for (int j = 0; j < pnames.length; j++)
	                		initializedObj = Reflections.getGetterMethod(initializedObj.getClass(), pnames[j]).invoke(initializedObj);
	                	
	                    //This is here to make sure the list is forced to return a value while operating inside of a 
	                    //session. Forcing the  initialization of object.
	                    if (getter != null)
	                        getter.initialize(entity, propertyNames[i], initializedObj);
	                }
	            }
			    
	            tm.commit(tx);
		    }
		    catch (Exception e) {
		    	String propertyName = propertyNames != null && propertyNames.length > 0 ? propertyNames[0] : "";
		    	log.error(e, "Error during lazy-initialization of collection: %s", propertyName);
		        tm.rollback(tx);
		    }
        }
        finally {
        	close();
        }
        
        return attachedEntity;
	} 
	
	protected abstract void close();
	
    /**
     * Fetch the entity with its lazy properties from the persistence context.
     * @param entity entity to load
     * @param fetch array of property names to fetch
     * @return the entity with the persistence context.
     */
	public abstract Object fetchEntity(Object entity, String[] fetch);
	
}
