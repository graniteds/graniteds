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

package org.granite.gravity.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.config.AbstractFrameworkGraniteConfig;
import org.granite.config.flex.Adapter;
import org.granite.config.flex.Channel;
import org.granite.config.flex.Destination;
import org.granite.config.flex.EndPoint;
import org.granite.config.flex.Service;
import org.granite.config.flex.ServicesConfig;
import org.granite.logging.Logger;
import org.granite.util.XMap;


public class AbstractMessagingDestination {
	
    private static final Logger log = Logger.getLogger(AbstractMessagingDestination.class);


    ///////////////////////////////////////////////////////////////////////////
    // Instance fields.
   
    private String id = null;
    private List<String> roles = null;
    private boolean noLocal = false;
    private boolean sessionSelector = false;
    

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public boolean isNoLocal() {
		return noLocal;
	}

	public void setNoLocal(boolean noLocal) {
		this.noLocal = noLocal;
	}

	public boolean isSessionSelector() {
		return sessionSelector;
	}

	public void setSessionSelector(boolean sessionSelector) {
		this.sessionSelector = sessionSelector;
	}

	
    protected void init(AbstractFrameworkGraniteConfig graniteConfig) {
    	ServicesConfig servicesConfig = graniteConfig.getServicesConfig();
    	initServices(servicesConfig);
    }
    
    public void initServices(ServicesConfig servicesConfig) {
    	Channel channel = servicesConfig.findChannelById("gravityamf");
    	if (channel == null) {
    		channel = new Channel("gravityamf", "org.granite.gravity.channels.GravityChannel",
    				new EndPoint("http://{server.name}:{server.port}/{context.root}/gravityamf/amf", "flex.messaging.endpoints.AMFEndpoint"),
    				new XMap());
    		servicesConfig.addChannel(channel);
    	}
    	
    	List<Service> services = servicesConfig.findServicesByMessageType("flex.messaging.messages.AsyncMessage");
    	Service service = null;
    	Adapter adapter = null;
    	if (services == null || services.isEmpty()) {
    		adapter = buildAdapter();
    		Map<String, Adapter> adapters = new HashMap<String, Adapter>();
    		adapters.put(adapter.getId(), adapter);
    		service = new Service("gravity-service", "flex.messaging.services.MessagingService", "flex.messaging.messages.AsyncMessage", 
    				adapter, adapters, new HashMap<String, Destination>());
    		servicesConfig.addService(service);
    	}
    	else {
    		service = services.get(0);
    		Adapter ad = buildAdapter();
			adapter = service.findAdapterById(ad.getId());
			if (adapter == null) {
				adapter = ad;
				service.addAdapter(adapter);
			}
    	}
    	
    	service.getDestinations().put(id, buildDestination(adapter));
    	
    	log.info("Registered messaging destination %s", id);
    }
	
	protected Adapter buildAdapter() {
		return new Adapter("simple-adapter", "org.granite.gravity.adapters.SimpleServiceAdapter", new XMap());
	}
	
	protected Destination buildDestination(Adapter adapter) {
    	List<String> channelIds = new ArrayList<String>();
    	channelIds.add("gravityamf");
    	Destination destination = new Destination(id, channelIds, new XMap(), roles, adapter, null);
    	destination.getProperties().put("no-local", String.valueOf(noLocal));
    	destination.getProperties().put("session-selector", String.valueOf(sessionSelector));
    	return destination;
	}
}
