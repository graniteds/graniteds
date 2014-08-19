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
package org.granite.binding.android;

import java.util.HashMap;
import java.util.Map;

import android.view.View;
import android.widget.TextView;

/**
 * @author William DRAI
 */
public class ViewBindingRegistry {
	
	private static Map<Class<? extends View>, ViewBinding<? extends View>> viewBindingsMap = new HashMap<Class<? extends View>, ViewBinding<? extends View>>();
	static {
		registerViewWatcherClass(TextView.class, TextViewWatcher.class);
	}
	
	private static class ViewBinding<V extends View> {
		
		private Class<V> viewClass;
		private Class<? extends ViewWatcher<V>> watcherClass;
		private Map<String, ViewSetter<V>> viewSettersMap = new HashMap<String, ViewSetter<V>>();
		
		public ViewBinding(Class<V> viewClass) {
			this.viewClass = viewClass;
		}
		
		public ViewWatcher<V> newWatcher(V view) {
			if (watcherClass == null)
				return null;
			
			try {
				return watcherClass.getConstructor(viewClass).newInstance(view);
			}
			catch (Exception e) {
				throw new RuntimeException("Could not build watcher for view " + viewClass);
			}
		}
		
		private ViewSetter<V> internalGetViewSetter(String property) {
			ViewSetter<V> viewSetter = viewSettersMap.get(property);
			if (viewSetter == null) {
				viewSetter = new ViewSetter<V>(viewClass, property);
				viewSettersMap.put(property, viewSetter);
			}
			return viewSetter;
		}
		
		public ViewSetter<V> getViewSetter(String property) {
			return internalGetViewSetter(property);
		}
		
		public void registerWatcherClass(Class<? extends ViewWatcher<V>> watcherClass) {
			this.watcherClass = watcherClass;
		}
		
		public void registerPropertySetter(String property, Setter<V> setter) {
			internalGetViewSetter(property).registerSetter(setter);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	private static <V extends View> ViewBinding<V> internalGetViewBinding(Class<V> viewClass) {
		ViewBinding<V> viewBinding = (ViewBinding<V>)viewBindingsMap.get(viewClass);
		if (viewBinding == null) {
			viewBinding = new ViewBinding<V>(viewClass);
			viewBindingsMap.put(viewClass, viewBinding);
		}
		return viewBinding;
	}
	
	public static <V extends View> ViewSetter<V> getViewSetter(Class<V> viewClass, String property) {
		return internalGetViewBinding(viewClass).getViewSetter(property);
	}
	
	@SuppressWarnings("unchecked")
	public static <V extends View> ViewWatcher<V> newWatcher(V view) {
		Class<? extends View> viewClass = view.getClass();
		Class<? extends ViewWatcher<?>> viewWatcherClass = null;
		while (viewWatcherClass == null && !viewClass.equals(Object.class) && !viewClass.equals(View.class)) {
			ViewWatcher<V> viewWatcher = internalGetViewBinding((Class<V>)viewClass).newWatcher(view);
			if (viewWatcher != null)
				return viewWatcher;
			
			viewClass = (Class<? extends View>)viewClass.getSuperclass();
		}
		
		return new ViewWatcher<V>(view);
	}
	
	
	public static <V extends View> void registerPropertySetter(Class<V> viewClass, String property, Setter<V> setter) {
		internalGetViewBinding(viewClass).registerPropertySetter(property, setter);
	}
	
	public static <V extends View, W extends ViewWatcher<V>> void registerViewWatcherClass(Class<V> viewClass, Class<W> viewWatcherClass) {
		internalGetViewBinding(viewClass).registerWatcherClass(viewWatcherClass);
	}
}