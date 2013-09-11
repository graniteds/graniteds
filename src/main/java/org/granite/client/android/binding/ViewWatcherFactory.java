/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.android.binding;

import java.util.HashMap;
import java.util.Map;

import org.granite.util.TypeUtil;

import android.view.View;
import android.widget.TextView;

public class ViewWatcherFactory {
	
	private static Map<Class<? extends View>, Class<? extends ViewWatcher<?>>> viewWatcherClassesMap = new HashMap<Class<? extends View>, Class<? extends ViewWatcher<?>>>();
	static {
		viewWatcherClassesMap.put(TextView.class, TextViewWatcher.class);
	}
	
	public static <V extends View> void registerViewWatcherClass(Class<V> viewClass, Class<ViewWatcher<V>> viewWatcherClass) {
		viewWatcherClassesMap.put(viewClass, viewWatcherClass);
	}
	
	
	@SuppressWarnings("unchecked")
	public static ViewWatcher<?> newWatcher(View view, String property) {
		Class<? extends View> viewClass = view.getClass();
		Class<? extends ViewWatcher<?>> viewWatcherClass = null;
		while (viewWatcherClass == null && !viewClass.equals(Object.class) && !viewClass.equals(View.class)) {
			viewWatcherClass = viewWatcherClassesMap.get(viewClass);
			if (viewWatcherClass != null) {
				try {
					return TypeUtil.newInstance(viewWatcherClass, new Class<?>[] { viewClass, String.class }, new Object[] { view, property });
				}
				catch (Exception e) {
					throw new RuntimeException("Could not build watcher for view " + viewClass);
				}
				
			}
			viewClass = (Class<? extends View>)viewClass.getSuperclass();
		}
		
		return new ViewWatcher<View>(view, property);
	}
}