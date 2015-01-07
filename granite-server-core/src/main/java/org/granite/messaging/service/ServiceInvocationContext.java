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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.granite.config.flex.Destination;
import org.granite.messaging.service.security.AbstractSecurityContext;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class ServiceInvocationContext extends AbstractSecurityContext {

    private final Object bean;
    private final Method method;
    private Object[] parameters;

    public ServiceInvocationContext(
        Message message,
        Destination destination,
        Object bean,
        Method method,
        Object[] parameters) {

        super(message, destination);
        this.bean = bean;
        this.method = method;
        this.parameters = parameters;
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getParameters() {
        return parameters;
    }
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    @Override
    public Object invoke() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return method.invoke(bean, parameters);
    }

    @Override
    public String toString() {
        return getClass().getName() + '{' +
            "\n  message=" + getMessage() +
            "\n  destination=" + getDestination() +
            "\n  bean=" + bean +
            "\n  method=" + method +
            "\n  parameters=" + Arrays.toString(parameters) +
        '}';
    }
}
