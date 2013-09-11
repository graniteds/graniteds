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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.granite.client.tide.Context;
import org.granite.client.tide.impl.BindingUtil;

import android.app.Activity;
import android.view.View;


public class Binder {
	
	private final Map<View, ViewWatcher<? extends View>> viewWatchers = new WeakHashMap<View, ViewWatcher<? extends View>>();
	
	private final List<Binding> bindings = new ArrayList<Binding>();
	
	private final Activity activity;
	private final Context context;
	
	public Binder(Activity activity, Context context) {
		this.activity = activity;
		this.context = context;
	}
	
	
	@SuppressWarnings("unchecked")
	private <T> T resolveBean(Object ref) {
		if (ref instanceof String)
			return context.byName((String)ref);
		else if (ref instanceof Class<?>)
			return context.byType((Class<T>)ref);
		return (T)ref;
	}
	
	public void bind(View view, String viewProperty, Object ref, String beanProperty) {
		bindings.add(new UnidirectionalBinding<Object>(view, viewProperty, resolveBean(ref), beanProperty));
	}

	@SuppressWarnings("unchecked")
	public <T> void bind(View view, String viewProperty, Object ref, String beanProperty, Getter<T, ?> beanPropertyGetter) {
		bindings.add(new UnidirectionalBinding<T>(view, viewProperty, (T)resolveBean(ref), beanProperty, beanPropertyGetter));
	}

	public void unbind(View view, String viewProperty) {
		unbind(UnidirectionalBinding.class, view, viewProperty);
	}
	
	public void bind(int viewId, String viewProperty, Object ref, String beanProperty) {		
		bindings.add(new UnidirectionalBinding<Object>(activity.findViewById(viewId), viewProperty, resolveBean(ref), beanProperty));
	}
	
	@SuppressWarnings("unchecked")
	public <T> void bind(int viewId, String viewProperty, Object ref, String beanProperty, Getter<T, ?> beanPropertyGetter) {
		bindings.add(new UnidirectionalBinding<T>(activity.findViewById(viewId), viewProperty, (T)resolveBean(ref), beanProperty, beanPropertyGetter));
	}

	public void unbind(int viewId, String viewProperty) {
		unbind(UnidirectionalBinding.class, activity.findViewById(viewId), viewProperty);
	}
	
	public void bindBidirectional(View view, String viewProperty, Object ref, String beanProperty) {		
		bindings.add(new BidirectionalBinding(view, viewProperty, resolveBean(ref), beanProperty));
	}

	public void unbindBidirectional(View view, String viewProperty) {
		unbind(BidirectionalBinding.class, view, viewProperty);
	}
	
	public void bindBidirectional(int viewId, String viewProperty, Object ref, String beanProperty) {		
		bindings.add(new BidirectionalBinding(activity.findViewById(viewId), viewProperty, resolveBean(ref), beanProperty));
	}	

	public void unbindBidirectional(int viewId, String viewProperty) {
		unbind(BidirectionalBinding.class, activity.findViewById(viewId), viewProperty);
	}
	
	private void unbind(Class<? extends Binding> type, View view, String viewProperty) {
		for (Iterator<Binding> ibinding = bindings.iterator(); ibinding.hasNext(); ) {
			Binding binding = ibinding.next();
			if (binding.getClass() == type && binding.isFor(view, viewProperty)) {
				binding.unbind();
				ibinding.remove();
			}
		}
	}
	
	public void applyBindings() {
		for (Entry<View, ViewWatcher<? extends View>> entry : viewWatchers.entrySet())
			entry.getValue().apply();
	}
	
	
	private class MethodGetter<B, V> implements Getter<B, V> {
		
		private final Method getter;
		
		public MethodGetter(B bean, String beanProperty) {
			try {
				this.getter = bean.getClass().getMethod("get" + beanProperty.substring(0, 1).toUpperCase() + beanProperty.substring(1));
			} 
			catch (Exception e) {
				throw new RuntimeException("Could not find getter on bean " + bean.getClass().getName() + "." + beanProperty, e);
			}
		}
		
		@SuppressWarnings("unchecked")
		public V getValue(B instance) throws Exception {
			return (V)getter.invoke(instance);
		}
	}
	
	public interface Binding {
		
		public boolean isFor(View view, String viewProperty);
		
		public void unbind();
	}
	
	private class UnidirectionalBinding<B> implements Binding {
		
		private final WeakReference<View> viewRef;
		private final String viewProperty;
		private final Method[] viewSetters;
		private final WeakReference<B> beanRef;
		private final String beanProperty;
		private final Getter<B, ?> beanGetter;
		
		public UnidirectionalBinding(View view, String viewProperty, B bean, String beanProperty) {
			this(view, viewProperty, bean, beanProperty, null);
		}
		
		public UnidirectionalBinding(View view, String viewProperty, B bean, String beanProperty, Getter<B, ?> beanPropertyGetter) {
			this.viewRef = new WeakReference<View>(view);
			try {
				List<Method> setters = new ArrayList<Method>();
				for (Method s : view.getClass().getMethods()) {
					if (s.getParameterTypes().length == 1 && s.getName().equals("set" + viewProperty.substring(0, 1).toUpperCase() + viewProperty.substring(1)))
						setters.add(s);
				}
				this.viewSetters = setters.toArray(new Method[setters.size()]);
			}
			catch (Exception e) {
				throw new RuntimeException("Could not find setter on view " + view.getClass().getName() + "." + viewProperty, e);
			}
			this.viewProperty = viewProperty;
			
			this.beanRef = new WeakReference<B>(bean);
			if (beanPropertyGetter != null)
				this.beanGetter = beanPropertyGetter;
			else
				this.beanGetter = new MethodGetter<B, Object>(bean, beanProperty);
			this.beanProperty = beanProperty;
			
			BindingUtil.addPropertyChangeListener(bean, beanProperty, beanPropertyChangeListener);
			
			setViewValue(getBeanValue());
		}
		
		private Object getBeanValue() {
			try {
				return beanGetter.getValue(beanRef.get());
			} 
			catch (Exception e) {
				throw new RuntimeException("Could not get bean value", e);
			}
		}
		
		private void setViewValue(Object newValue) {
			try {
				for (Method viewSetter : viewSetters) {
					Class<?> type = viewSetter.getParameterTypes()[0];
					if (type.isPrimitive()) {
						if (type == boolean.class)
							type = Boolean.class;
						else if (type == short.class)
							type = Short.class;
						else if (type == int.class)
							type = Integer.class;
						else if (type == long.class)
							type = Long.class;
						else if (type == byte.class)
							type = Byte.class;
						else if (type == char.class)
							type = Character.class;
					}
					if (type.isInstance(newValue)) {
						viewSetter.invoke(viewRef.get(), newValue);
						return;
					}
				}
			}
			catch (Exception e) {
				throw new RuntimeException("Could not set view value", e);
			}
		}
		
		private PropertyChangeListener beanPropertyChangeListener = new PropertyChangeListener() {			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				Object newValue = event.getNewValue();
				setViewValue(newValue);
			}
		};
		
		public void unbind() {
			Object bean = beanRef.get();
			if (bean != null)
				BindingUtil.removePropertyChangeListener(beanRef.get(), beanProperty, beanPropertyChangeListener);
		}
		
		public boolean isFor(View view, String viewProperty) {
			return viewRef.get() == view && viewProperty.equals(this.viewProperty);
		}
	}
	
	private class BidirectionalBinding implements Binding {
		
		private final WeakReference<View> viewRef;
		private final Method[] viewSetters;
		private final String viewProperty;
		private final WeakReference<Object> beanRef;
		private final Method beanGetter;
		private final Method beanSetter;
		private final String beanProperty;
		
		private boolean changing = false;
		
		public BidirectionalBinding(View view, String viewProperty, Object bean, String beanProperty) {
			this.viewRef = new WeakReference<View>(view);
			try {
				List<Method> setters = new ArrayList<Method>();
				for (Method s : view.getClass().getMethods()) {
					if (s.getParameterTypes().length == 1 && s.getName().equals("set" + viewProperty.substring(0, 1).toUpperCase() + viewProperty.substring(1)))
						setters.add(s);
				}
				this.viewSetters = setters.toArray(new Method[setters.size()]);
			}
			catch (Exception e) {
				throw new RuntimeException("Could not find setter on view " + view.getClass().getName() + "." + viewProperty, e);
			}
			
			this.beanRef = new WeakReference<Object>(bean);
			try {
				this.beanGetter = beanRef.get().getClass().getMethod("get" + beanProperty.substring(0, 1).toUpperCase() + beanProperty.substring(1));
			} 
			catch (Exception e) {
				throw new RuntimeException("Could not find getter on bean " + bean.getClass().getName() + "." + beanProperty, e);
			}
			Method beanSetter = null;
			try {
				for (Method m : beanRef.get().getClass().getMethods()) {
					if (m.getName().equals("set" + beanProperty.substring(0, 1).toUpperCase() + beanProperty.substring(1)) && m.getParameterTypes().length == 1) {
						beanSetter = m;
						break;
					}
				}
			}
			catch (Exception e) {
				throw new RuntimeException("Could not find setter on bean " + bean.getClass().getName() + "." + beanProperty, e);
			}
			if (beanSetter == null)
				throw new RuntimeException("Could not find setter on bean " + bean.getClass().getName() + "." + beanProperty);
			this.beanSetter = beanSetter;
			this.beanProperty = beanProperty;
			
			BindingUtil.addPropertyChangeListener(bean, beanProperty, beanPropertyChangeListener);
			
			setViewValue(getBeanValue());
			
			ViewWatcher<? extends View> viewWatcher = viewWatchers.get(view);
			if (viewWatcher == null) {
				viewWatcher = ViewWatcherFactory.newWatcher(view, viewProperty);
				viewWatchers.put(view, viewWatcher);
			}
			else
				viewWatcher.addProperty(viewProperty);
			this.viewProperty = viewProperty;
			
			viewWatcher.addPropertyChangeListener(viewProperty, viewPropertyChangeListener);
		}
		
		public void unbind() {
			Object bean = beanRef.get();
			if (bean != null)
				BindingUtil.removePropertyChangeListener(bean, beanProperty, beanPropertyChangeListener);
			
			View view = viewRef.get();
			if (view != null) {
				ViewWatcher<? extends View> viewWatcher = viewWatchers.get(view);
				if (viewWatcher != null) {
					viewWatcher.removePropertyChangeListener(viewProperty, viewPropertyChangeListener);
					viewWatcher.removeProperty(viewProperty);
				}
				if (viewWatcher.isEmpty())
					viewWatchers.remove(view);
			}
		}
		
		private Object getBeanValue() {
			try {
				return beanGetter.invoke(beanRef.get());
			} 
			catch (Exception e) {
				throw new RuntimeException("Could not get view value", e);
			}
		}
		
		private void setViewValue(Object newValue) {
			try {
				for (Method viewSetter : viewSetters) {
					Class<?> type = viewSetter.getParameterTypes()[0];
					if (type.isPrimitive()) {
						if (type == boolean.class)
							type = Boolean.class;
						else if (type == short.class)
							type = Short.class;
						else if (type == int.class)
							type = Integer.class;
						else if (type == long.class)
							type = Long.class;
						else if (type == byte.class)
							type = Byte.class;
						else if (type == char.class)
							type = Character.class;
					}
					if (type.isInstance(newValue)) {
						viewSetter.invoke(viewRef.get(), newValue);
						return;
					}
				}
			}
			catch (Exception e) {
				throw new RuntimeException("Could not set bean value", e);
			}
		}
		private void setBeanValue(Object newValue) {
			try {
				beanSetter.invoke(beanRef.get(), newValue);
			}
			catch (Exception e) {
				throw new RuntimeException("Could not set bean value", e);
			}
		}
		
		public boolean isFor(View view, String viewProperty) {
			return viewRef.get() == view && viewProperty.equals(this.viewProperty);
		}
		
		private PropertyChangeListener viewPropertyChangeListener = new PropertyChangeListener() {			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (changing)
					return;
				
				try {
					changing = true;
					Object newValue = event.getNewValue();
					setBeanValue(newValue);
				}
				finally {
					changing = false;
				}
			}
		};
		
		private PropertyChangeListener beanPropertyChangeListener = new PropertyChangeListener() {			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (changing)
					return;
				
				try {
					changing = true;
					Object newValue = event.getNewValue();
					setViewValue(newValue);
				}
				finally {
					changing = false;
				}
			}
		};
	}
}
