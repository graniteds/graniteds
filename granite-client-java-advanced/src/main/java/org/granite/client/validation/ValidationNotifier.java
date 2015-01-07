/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.validation;

import java.lang.reflect.Array;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.granite.client.util.WeakIdentityHashMap;
import org.granite.client.validation.NotifyingValidator.ConstraintViolationsHandler;

/**
 * @author William DRAI
 */
public class ValidationNotifier {
    
    private final WeakIdentityHashMap<Object, ConstraintViolationsHandler<?>[]> handlersMap = new WeakIdentityHashMap<Object, ConstraintViolationsHandler<?>[]>();
    
    
    public <T> void notifyConstraintViolations(T entity, Set<ConstraintViolation<T>> constraintViolations) {
        @SuppressWarnings("unchecked")
        ConstraintViolationsHandler<T>[] handlers = (ConstraintViolationsHandler<T>[])handlersMap.get(entity);
        if (handlers == null)
            return;
        
        for (ConstraintViolationsHandler<T> handler : handlers)
            handler.handle(entity, constraintViolations);
    }

    @SuppressWarnings("unchecked")
    public <T> void addConstraintViolationsHandler(T entity, ConstraintViolationsHandler<T> handler) {
        ConstraintViolationsHandler<T>[] handlers = (ConstraintViolationsHandler<T>[])handlersMap.get(entity);
        if (handlers == null) {
            handlers = (ConstraintViolationsHandler<T>[])Array.newInstance(ConstraintViolationsHandler.class, 1);
            handlers[0] = handler;
        }
        else {
            ConstraintViolationsHandler<T>[] newHandlers = (ConstraintViolationsHandler<T>[])Array.newInstance(ConstraintViolationsHandler.class, handlers.length+1);
            System.arraycopy(handlers, 0, newHandlers, 0, handlers.length);
            newHandlers[handlers.length] = handler;
            handlers = newHandlers;
        }
        handlersMap.put(entity, handlers);
    }

    public <T> void removeConstraintViolationsHandler(T entity, ConstraintViolationsHandler<T> handler) {
        @SuppressWarnings("unchecked")
        ConstraintViolationsHandler<T>[] handlers = (ConstraintViolationsHandler<T>[])handlersMap.get(entity);
        if (handlers == null)
            return;
        if (handlers.length == 1 && handlers[0] == handler) {
            handlersMap.remove(entity);
            return;
        }
        int index = -1;
        for (int i = 0; i < handlers.length; i++) {
            if (handlers[i] == handler) {
                index = i;
                break;
            }
        }
        if (index < 0)
            return; // Handler not found
        
        @SuppressWarnings({"rawtypes", "unchecked"})
        ConstraintViolationsHandler[] newHandlers = (ConstraintViolationsHandler<T>[])Array.newInstance(ConstraintViolationsHandler.class, handlers.length-1);
        if (index > 0)
            System.arraycopy(handlers, 0, newHandlers, 0, index);
        if (index < handlers.length-1)
            System.arraycopy(handlers, index+1, newHandlers, index, handlers.length-index-1);
        handlersMap.put(entity, newHandlers);
    }


}
