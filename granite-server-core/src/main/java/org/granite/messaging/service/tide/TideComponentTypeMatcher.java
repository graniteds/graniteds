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
package org.granite.messaging.service.tide;

import java.util.Set;
import java.util.regex.Pattern;


/**
 * @author Franck WOLFF
 */
public class TideComponentTypeMatcher implements TideComponentMatcher {
    
    private final boolean disabled;
    private final Pattern pattern;
    
    public TideComponentTypeMatcher(String type, boolean disabled) {
        this.pattern = Pattern.compile(type);
        this.disabled = disabled;
    }
    
    public boolean matches(String name, Set<Class<?>> classes, Object instance, boolean disabled) {
    	for (Class<?> clazz : classes) {
    		if (disabled == this.disabled && pattern.matcher(clazz.getName()).matches())
    			return true;
    	}
    	return false;
    }
    
    @Override
    public String toString() {
    	return "Type matcher: " + pattern.pattern() + (disabled ? " (disabled)" : "");
    }
}
