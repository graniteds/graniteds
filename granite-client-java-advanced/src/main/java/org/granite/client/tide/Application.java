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
package org.granite.client.tide;

import java.util.Map;

/**
 * SPI for platform-specific integration
 *
 * Allows to define default components available in all contexts or apply specific configurations on components annotated with {@link org.granite.client.tide.ApplicationConfigurable}
 *
 * @author William DRAI
 * @see org.granite.client.tide.ApplicationConfigurable
 */
public interface Application {

    /**
     * Define a map of beans that will be setup in the context before initialization
     * @param context Tide context
     * @param initialBeans map of bean instances keyed by name
     */
	public void initContext(Context context, Map<String, Object> initialBeans);

    /**
     * Configure a bean instance for platform-specific behaviour
     * @param instance bean instance
     */
	public void configure(Object instance);

    /**
     * Integration with deferred execution of a runnable on the UI thread
     * @param runnable runnable to execute in the UI thread
     */
	public void execute(Runnable runnable);
}
