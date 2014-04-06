/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.config.api.GraniteConfigException;
import org.granite.messaging.service.tide.TideComponentAnnotatedWithMatcher;
import org.granite.messaging.service.tide.TideComponentInstanceOfMatcher;
import org.granite.messaging.service.tide.TideComponentMatcher;
import org.granite.messaging.service.tide.TideComponentNameMatcher;
import org.granite.messaging.service.tide.TideComponentTypeMatcher;

/**
 * @author Franck WOLFF
 */
public class TideComponentMatcherFactory {

    public TideComponentMatcher getTypeMatcher(String type, boolean disabled) throws GraniteConfigException {
        try {
            return new TideComponentTypeMatcher(type, disabled);
        } catch (Exception e) {
            throw new GraniteConfigException("Could not instantiate Tide component matcher for type: " + type, e);
        }
    }
    
    public TideComponentMatcher getNameMatcher(String name, boolean disabled) throws GraniteConfigException {
        try {
            return new TideComponentNameMatcher(name, disabled);
        } catch (Exception e) {
            throw new GraniteConfigException("Could not instantiate Tide component matcher for name: " + name, e);
        }
    }
    
    public TideComponentMatcher getInstanceOfMatcher(String type, boolean disabled) throws GraniteConfigException {
        try {
            return new TideComponentInstanceOfMatcher(type, disabled);
        } catch (Exception e) {
            throw new GraniteConfigException("Could not instantiate Tide component matcher for instance of: " + type, e);
        }
    }
    
    public TideComponentMatcher getAnnotatedWithMatcher(String type, boolean disabled) throws GraniteConfigException {
        try {
            return new TideComponentAnnotatedWithMatcher(type, disabled);
        } catch (Exception e) {
            throw new GraniteConfigException("Could not instantiate Tide component matcher for annotated with: " + type, e);
        }
    }

    
    public static boolean isComponentTideEnabled(
        Map<String, Object[]> tideComponentsByName,
        List<TideComponentMatcher> tideComponentMatchers,
        String componentName, Set<Class<?>> componentClasses, Object componentInstance) throws GraniteConfigException {
        
    	String key = componentName != null ? componentName : componentClasses.toString();
        if (tideComponentsByName.containsKey(key)) {
        	if ((Integer)tideComponentsByName.get(key)[1] == componentClasses.hashCode())
        		return (Boolean)tideComponentsByName.get(key)[0];
        }

        boolean enabled = false;
        for (TideComponentMatcher matcher : tideComponentMatchers) {
            if (matcher.matches(componentName, componentClasses, componentInstance, false)) {
                enabled = true;
                break;
            }
        }
        
        tideComponentsByName.put(key, new Object[] { enabled, componentClasses.hashCode()});
        return enabled;
    }
    
    public static boolean isComponentTideDisabled(
        Map<String, Object[]> tideComponentsByName,
        List<TideComponentMatcher> tideComponentMatchers,
        String componentName, Set<Class<?>> componentClasses, Object componentInstance) throws GraniteConfigException {
        
    	String key = componentName != null ? componentName : componentClasses.toString();
        if (tideComponentsByName.containsKey(key)) {
        	if ((Integer)tideComponentsByName.get(key)[1] == componentClasses.hashCode())
        		return (Boolean)tideComponentsByName.get(key)[0];
        }

        boolean disabled = false;
        for (TideComponentMatcher matcher : tideComponentMatchers) {
            if (matcher.matches(componentName, componentClasses, componentInstance, true)) {
                disabled = true;
                break;
            }
        }
        
        tideComponentsByName.put(key, new Object[] { disabled, componentClasses.hashCode()});
        return disabled;
    }
}
