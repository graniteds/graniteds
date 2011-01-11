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

package org.granite.spring;

import java.util.ArrayList;
import java.util.List;

import org.granite.config.AbstractRemoteDestination;
import org.granite.config.flex.Destination;
import org.granite.util.XMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class RemoteDestination extends AbstractRemoteDestination implements InitializingBean, ApplicationContextAware {

    private ApplicationContext context = null;
    
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	
    public void afterPropertiesSet() {
		SpringGraniteConfig springGraniteConfig = (SpringGraniteConfig)context.getBeansOfType(SpringGraniteConfig.class).values().iterator().next();
		
		init(springGraniteConfig);
    }
	
    @Override
	protected Destination buildDestination() {
    	List<String> channelIds = new ArrayList<String>();
    	channelIds.add("graniteamf");
    	XMap props = new XMap("properties");
    	props.put("factory", "spring-factory");
    	props.put("source", getSource());
    	Class<?> beanClass = context.getType(getSource());
    	Destination destination = new Destination(getSource(), channelIds, props, getRoles(), null, beanClass);
    	return destination;
	}
}
