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
package org.granite.osgi.adaptor;

import java.util.Iterator;
import java.util.Set;

import org.granite.config.flex.ServicesConfig;
import org.granite.logging.Logger;
import org.granite.osgi.constants.OSGIConstants;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class ServiceEventHandler implements EventHandler {

	private static final Logger log = Logger.getLogger(ServiceEventHandler.class);

    private ServicesConfig servicesConfig;
    
    public ServiceEventHandler(ServicesConfig servicesConfig) {
    	this.servicesConfig = servicesConfig;
    }
    
	public void handleEvent(Event event) {
		Class<?> c=(Class<?>) event.getProperty(OSGIConstants.SERVICE_CLASS);
		@SuppressWarnings("unchecked")
		Set<Class<?>>classes=(Set<Class<?>>)event.getProperty(OSGIConstants.SERVICE_CLASS_SET);
		//add service
		if(event.getTopic().equals(OSGIConstants.TOPIC_GDS_ADD_SERVICE)){
			if(c!=null)
				servicesConfig.handleClass(c);
			if(classes!=null){
				Iterator<Class<?>> it=classes.iterator();
				 while(it.hasNext()){
					 servicesConfig.handleClass(it.next());
				 }
			}else{
				log.warn("Class NOT Found!!");
			}
		}
		//remove service
	   if(event.getTopic().equals(OSGIConstants.TOPIC_GDS_REMOVE_SERVICE)){
			if(c!=null)
				servicesConfig.handleRemoveService(c);
			if(classes!=null){
				Iterator<Class<?>> it=classes.iterator();
				 while(it.hasNext()){
					 servicesConfig.handleRemoveService(it.next());
				 }
			}else{
				log.warn("Class NOT Found!!");
			}
		}
	}

}
