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

package org.granite.messaging.service;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.granite.config.flex.Destination;
import org.granite.config.flex.Factory;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.util.ClassUtil;
import org.granite.util.XMap;

import flex.messaging.messages.RemotingMessage;

/**
 * @author Franck WOLFF
 */
public interface ServiceFactory extends Serializable {

    public void configure(XMap properties) throws ServiceException;

    public ServiceInvoker<?> getServiceInstance(RemotingMessage request) throws ServiceException;

    public ServiceExceptionHandler getServiceExceptionHandler();
}
