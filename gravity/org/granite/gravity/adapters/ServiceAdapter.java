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

package org.granite.gravity.adapters;

import org.granite.config.flex.Destination;
import org.granite.gravity.Channel;
import org.granite.gravity.Gravity;
import org.granite.messaging.service.ServiceException;
import org.granite.util.XMap;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

/**
 * @author William DRAI
 */
public abstract class ServiceAdapter {

    private String id;
    private Gravity gravity;
    private Destination destination;
    private Object adapterState;
    private SecurityPolicy securityPolicy = new DefaultPolicy();


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public Gravity getGravity() {
        return gravity;
    }
    public void setGravity(Gravity gravity) {
        this.gravity = gravity;
    }

    public Destination getDestination() {
        return destination;
    }
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Object getAdapterState() {
        return adapterState;
    }
    public void setAdapterState(Object adapterState) {
        this.adapterState = adapterState;
    }

    public SecurityPolicy getSecurityPolicy() {
        return securityPolicy;
    }
    public void setSecurityPolicy(SecurityPolicy securityPolicy) {
        this.securityPolicy = securityPolicy;
    }


    public void configure(XMap adapterProperties, XMap destinationProperties) throws ServiceException {
    }

    public void start() throws ServiceException {
    }

    public void stop() throws ServiceException {
    }


    public abstract Object manage(Channel fromClient, CommandMessage message);

    public abstract Object invoke(Channel fromClient, AsyncMessage message);


    private static class DefaultPolicy implements SecurityPolicy {

        public boolean canCreate(Channel client, String channel, Message message)  {
            return client != null;
        }

        public boolean canSubscribe(Channel client, String channel, Message message) {
            return client != null;
        }

        public boolean canPublish(Channel client, String channel, Message message) {
            return client != null;
        }
    }
}
