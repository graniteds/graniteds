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
package org.granite.client.tide;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * SPI to integrate with DI containers
 *
 * @author William DRAI
 */
public interface InstanceStore {

    /**
     * Set a named bean in the container
     * Not applicable to statically configured containers (Spring/CDI)
     * @param name bean name
     * @param instance bean instance
     * @param <T> bean type
     * @return the attached bean instance (usually the same instance as the one provided)
     */
    public <T> T set(String name, T instance);

    /**
     * Set a bean in the container without specifying a name
     * Not applicable to statically configured containers (Spring/CDI)
     * The bean will be accessible only by its type
     * @param instance bean instance
     * @param <T> bean type
     * @return the attached bean instance (usually the same instance as the one provided)
     */
    public <T> T set(T instance);

    /**
     * Remove a bean from the container
     * Not applicable to statically configured containers (Spring/CDI)
     * @param name bean name
     */
    public void remove(String name);

    /**
     * Remove a bean from the container
     * Not applicable to statically configured containers (Spring/CDI)
     * @param instance bean instance
     */
    public void remove(Object instance);
    
    /**
     * Inject context value into provided instance
     * @param instance instance to inject into
     * @param name component name
     * @param properties properties defined on the context
     */
    public void inject(Object instance, String name, Map<String, Object> properties);

    /**
     * Clear all beans from the container
     * Not applicable to statically configured containers (Spring/CDI)
     */
    public void clear();

    /**
     * Return all bean names set in this container
     * @return list of bean names
     */
    public List<String> allNames();
    
    /**
     * Indicate if a bean of the specified name exists in the store
     * @param name component name
     * @return true if exists
     */
    public boolean exists(String name);

    /**
     * Lookup a bean by its name
     * The implementation is free to create and return a default instance (such as a service proxy) if no bean exists
     * @param name bean name
     * @param context context to lookup
     * @param <T> expected bean type
     * @return bean instance or null if not found
     */
    public <T> T byName(String name, Context context);

    /**
     * Lookup a bean by its name
     * Does not create a proxy if no bean found
     * @param name bean name
     * @param context context to lookup
     * @param <T> bean type
     * @return bean instance or null if not found
     */
    public <T> T getNoProxy(String name, Context context);

    /**
     * Lookup a bean by its type
     * If more than one instance is found, should throw a runtime exception
     * @param type expected bean type
     * @param context context to lookup
     * @param <T> expected bean type
     * @return bean instance
     */
    public <T> T byType(Class<T> type, Context context);

    /**
     * Return an array of all bean instances implementing the expected type
     * @param type expected bean type
     * @param context context to lookup
     * @param create if true, should create an instance if none is existing
     * @param <T> expected bean type
     * @return array of bean instances or null if no bean found
     */
    public <T> T[] allByType(Class<T> type, Context context, boolean create);

    /**
     * Return a map of all bean instances annotated with the specified annotation
     * @param annotationClass annotation
     * @param context context to lookup
     * @return map of bean instances keyed by name
     */
	public Map<String, Object> allByAnnotatedWith(Class<? extends Annotation> annotationClass, Context context);
}
