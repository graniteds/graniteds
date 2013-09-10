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

package org.granite.seam;

import org.granite.config.flex.Destination;
import org.granite.logging.Logger;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceInvocationContext;
import org.granite.messaging.service.ServiceInvoker;

/**
 * @author Cameron INGRAM
 * @author Venkat DANDA
 */
public class SeamServiceInvoker extends ServiceInvoker<SeamServiceFactory> {

    private static final Logger log = Logger.getLogger(SeamServiceInvoker.class);

    public static final String CAPITALIZED_DESTINATION_ID = "{capitalized.destination.id}";
    public static final String DESTINATION_ID = "{destination.id}";

    public SeamServiceInvoker(Destination destination, SeamServiceFactory factory, Object instance)
        throws ServiceException {
        super(destination, factory);

        this.invokee = instance;
    }

    @Override
    protected void beforeInvocation(ServiceInvocationContext context) {
        log.debug("Before Invocation");
    }


    @Override
    protected Object afterInvocation(ServiceInvocationContext context, Object result) {
        log.debug("After Invocation");
        return result;
    }
}
