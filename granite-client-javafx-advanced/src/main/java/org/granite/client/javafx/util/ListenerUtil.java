/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.javafx.util;

import java.lang.reflect.Array;
import java.util.Arrays;

import javafx.beans.WeakListener;

/**
 * @author William DRAI
 */
public class ListenerUtil {

    public static <T> T[] add(Class<?> listenerInterface, T[] listeners, T listener) {
		if (listeners == null) {
			@SuppressWarnings("unchecked")
			T[] newListeners = (T[])Array.newInstance(listenerInterface, 1);
			newListeners[0] = listener;
			return newListeners;
		}
		else {
			for (T l : listeners) {
				if (listener.equals(l))
					return listeners;
			}
		}
		
    	int newSize = 0;
    	int length = listeners.length;
        for (int i = 0; i < length; i++) {
            final T l = listeners[i];
            if (l instanceof WeakListener && ((WeakListener)l).wasGarbageCollected()) {
            	if (i < length-1)
            		System.arraycopy(listeners, i+1, listeners, i, length-i-1);            	
        		length--;
            	i--;
            }
            else
            	newSize++;
        }
        T[] newListeners = Arrays.copyOf(listeners, newSize+1);
        newListeners[newSize] = listener;
        return newListeners;
    }

    public static <T> T[] remove(Class<?> listenerClass, T[] listeners, T listener) {
		if (listeners == null)
			return null;
		
		int index = -1;
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i].equals(listener)) {
				index = i;
				break;
			}
		}
		if (index < 0)
			return listeners;
		
		if (listeners.length == 1)
			return null;
		
    	int newSize = 0;
    	int length = listeners.length;
        for (int i = 0; i < length; i++) {
            final T l = listeners[i];
            if ((l instanceof WeakListener && ((WeakListener)l).wasGarbageCollected()) || l.equals(listener)) {
            	if (i < length-1)
            		System.arraycopy(listeners, i+1, listeners, i, length-i-1);
        		length--;
            	i--;
            }
            else 
            	newSize++;
        }
        if (newSize == 0)
        	return null;
        
        return Arrays.copyOf(listeners, newSize);
    }

}
