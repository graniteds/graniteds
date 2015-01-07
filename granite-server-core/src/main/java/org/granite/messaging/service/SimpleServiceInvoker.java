/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.messaging.service;

import java.util.HashMap;
import java.util.Map;

import org.granite.config.flex.Destination;
import org.granite.logging.Logger;
import org.granite.util.TypeUtil;

import flex.messaging.messages.RemotingMessage;

/**
 * @author Franck WOLFF
 */
public class SimpleServiceInvoker extends ServiceInvoker<SimpleServiceFactory> {

    private static final Logger log = Logger.getLogger(SimpleServiceInvoker.class);

    private final Map<String, Object> sources;

    protected SimpleServiceInvoker(Destination destination, SimpleServiceFactory factory) throws ServiceException {
        super(destination, factory);

        String className = destination.getProperties().get("source");
        if (className == null)
            throw new ServiceException("No source property for destination: " + destination);
        className = className.trim();

        log.debug(">> New SimpleServiceInvoker constructing new: %s", className);

        // Invokee class set at runtime (RemoteObject.source).
        if ("*".equals(className))
            sources = new HashMap<String, Object>();
        else {
            try {
            	if (destination.getScannedClass() != null)
            		this.invokee = destination.getScannedClass().newInstance();
            	else
            		this.invokee = TypeUtil.newInstance(className);
            } catch (Exception e) {
                throw new ServiceException("Invalid source property for destination: " + destination, e);
            }
            sources = null;
        }
    }

    @Override
    protected Object adjustInvokee(RemotingMessage request, String methodName, Object[] args) throws ServiceException {

        if (sources == null)
            return super.adjustInvokee(request, methodName, args);

        String className = request.getSource();
        if (className == null)
            throw new ServiceException("No source property in request for '*' destination: " + destination);
        className = className.trim();

        Object invokee = null;

        synchronized (sources) {
            invokee = sources.get(className);
            if (invokee == null) {
                try {
                    invokee = TypeUtil.newInstance(className);
                } catch (Exception e) {
                    throw new ServiceException("Invalid source property in request for '*' destination: " + destination, e);
                }
                sources.put(className, invokee);
            }
        }

        return invokee;
    }
}
