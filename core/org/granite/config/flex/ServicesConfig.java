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

package org.granite.config.flex;

import org.granite.config.api.Configuration;

import java.util.List;

public interface ServicesConfig {

    public Service findServiceById(String id);

    public List<Service> findServicesByMessageType(String messageType);

    public void addService(Service service);

    public void removeService(String service);

    public Channel findChannelById(String id);

    public void addChannel(Channel channel);

    public void removeChannel(String channel);

    public Factory findFactoryById(String id);

    public void addFactory(Factory factory);

    public Factory removeFactory(String factory);

    public Destination findDestinationById(String messageType, String id);

    public void scan(Configuration configuration);
}
