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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.granite.binding.ObservableValue;
import org.granite.binding.PropertyChangeHelper;
import org.granite.binding.WritableObservableValue;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author William DRAI
 */
public class Binder {
	
	private final Map<View, ViewWatcher<? extends View>> viewWatchers = new WeakHashMap<View, ViewWatcher<? extends View>>();
	
	private final List<Binding> bindings = new ArrayList<Binding>();
	
	private final Activity activity;
	
	private BeanResolver beanResolver = new DefaultBeanResolver();
	private List<IdGetter> idGetters = new ArrayList<IdGetter>();
	
	private final Map<Class<?>, IdGetter> idGettersByClass = new HashMap<Class<?>, IdGetter>();
	
	
	public Binder(Activity activity) {
		this.activity = activity;
		
		initListeners();
	}
	
	public void setBeanResolver(BeanResolver beanResolver) {
		this.beanResolver = beanResolver;
	}
	
	public void registerIdGetter(IdGetter idGetter) {
		idGetters.add(idGetter);
	}
	
	public interface BeanResolver {
		
		public <T> T resolveBean(Object ref);
		
		public <T> BeanSetter<T> getBeanSetter(T bean, String propertyName);
	}
	
	private static final class DefaultBeanResolver implements BeanResolver {
		@SuppressWarnings("unchecked")
		public <T> T resolveBean(Object ref) {
			return (T)ref;
		}
		
		public <T> BeanSetter<T> getBeanSetter(T bean, String propertyName) {
			return new MethodBeanSetter<T>(bean, propertyName);
		}
	}
	
	
	public long getId(Object instance) {
		if (instance == null)
			return 0L;
		
		IdGetter idGetter = idGettersByClass.get(instance.getClass());
		if (idGetter == null) {
			for (IdGetter g : idGetters) {
				if (g.accepts(instance)) {
					idGetter = g;
					break;
				}
			}
			if (idGetter != null)
				idGettersByClass.put(instance.getClass(), idGetter);
		}
		if (idGetter == null)
			throw new RuntimeException("Cannot get id for object " + instance + ", register an IdGetter implementation");
		
		return idGetter.getId(instance);
	}
	
	
	public void bind(View view, String viewPropertyName, Object ref, String beanPropertyName) {
		bindings.add(new UnidirectionalBinding<Object>(view, viewPropertyName, beanResolver.resolveBean(ref), beanPropertyName));
	}

	@SuppressWarnings("unchecked")
	public <T> void bind(View view, String viewPropertyName, Object ref, String beanProperty, Getter<T, ?> beanPropertyGetter) {
		bindings.add(new UnidirectionalBinding<T>(view, viewPropertyName, (T)beanResolver.resolveBean(ref), beanProperty, beanPropertyGetter));
	}

	public <T> void bind(View view, String viewPropertyName, ObservableValue observableValue) {
		bindings.add(new UnidirectionalBinding<T>(view, viewPropertyName, observableValue));
	}

	public void unbind(View view, String viewPropertyName) {
		unbind(UnidirectionalBinding.class, view, viewPropertyName);
	}
	
	public void bind(int viewId, String viewPropertyName, Object ref, String beanPropertyName) {		
		bindings.add(new UnidirectionalBinding<Object>(activity.findViewById(viewId), viewPropertyName, beanResolver.resolveBean(ref), beanPropertyName));
	}
	
	@SuppressWarnings("unchecked")
	public <T> void bind(int viewId, String viewPropertyName, Object ref, String beanPropertyName, Getter<T, ?> beanPropertyGetter) {
		bindings.add(new UnidirectionalBinding<T>(activity.findViewById(viewId), viewPropertyName, (T)beanResolver.resolveBean(ref), beanPropertyName, beanPropertyGetter));
	}

	public <T> void bind(int viewId, String viewPropertyName, ObservableValue observableValue) {
		bindings.add(new UnidirectionalBinding<T>(activity.findViewById(viewId), viewPropertyName, observableValue));
	}
	
	public void unbind(int viewId, String viewPropertyName) {
		unbind(UnidirectionalBinding.class, activity.findViewById(viewId), viewPropertyName);
	}
	
	public void bind(View rootView, int viewId, String viewPropertyName, Object ref, String beanPropertyName) {		
		bindings.add(new UnidirectionalBinding<Object>(rootView.findViewById(viewId), viewPropertyName, beanResolver.resolveBean(ref), beanPropertyName));
	}
	
	@SuppressWarnings("unchecked")
	public <T> void bind(View rootView, int viewId, String viewPropertyName, Object ref, String beanPropertyName, Getter<T, ?> beanPropertyGetter) {
		bindings.add(new UnidirectionalBinding<T>(rootView.findViewById(viewId), viewPropertyName, (T)beanResolver.resolveBean(ref), beanPropertyName, beanPropertyGetter));
	}
	
	public <T> void bind(View rootView, int viewId, String viewPropertyName, ObservableValue observableValue) {
		bindings.add(new UnidirectionalBinding<T>(rootView.findViewById(viewId), viewPropertyName, observableValue));
	}

	public void unbind(View rootView, int viewId, String viewPropertyName) {
		unbind(UnidirectionalBinding.class, rootView.findViewById(viewId), viewPropertyName);
	}
	
	public void bindBidirectional(View view, String viewPropertyName, Object ref, String beanPropertyName) {		
		bindings.add(new BidirectionalBinding(view, viewPropertyName, beanResolver.resolveBean(ref), beanPropertyName));
	}

	public void unbindBidirectional(View view, String viewPropertyName) {
		unbind(BidirectionalBinding.class, view, viewPropertyName);
	}
	
	public void bindBidirectional(int viewId, String viewPropertyName, Object ref, String beanPropertyName) {		
		bindings.add(new BidirectionalBinding(activity.findViewById(viewId), viewPropertyName, beanResolver.resolveBean(ref), beanPropertyName));
	}	

	public void unbindBidirectional(int viewId, String viewPropertyName) {
		unbind(BidirectionalBinding.class, activity.findViewById(viewId), viewPropertyName);
	}
	
	public void bindBidirectional(View rootView, int viewId, String viewPropertyName, Object ref, String beanPropertyName) {		
		bindings.add(new BidirectionalBinding(rootView.findViewById(viewId), viewPropertyName, beanResolver.resolveBean(ref), beanPropertyName));
	}	

	public void unbindBidirectional(View rootView, int viewId, String viewPropertyName) {
		unbind(BidirectionalBinding.class, rootView.findViewById(viewId), viewPropertyName);
	}
	
	public void unbindAll(View view) {
		for (Iterator<Binding> ibinding = bindings.iterator(); ibinding.hasNext(); ) {
			Binding binding = ibinding.next();
			if (binding.isFor(view)) {
				binding.unbind();
				ibinding.remove();
			}
		}
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
	
	
	public void bind(View rootView) {
		bind(rootView, null);
	}
	
	public void bind(View rootView, Object item) {
		if (rootView == null)
			return;
		if (rootView.getTag() instanceof String)
			internalBind(rootView, item);
		
		if (rootView instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup)rootView;
			for (int i = 0; i < viewGroup.getChildCount(); i++)
				bind(viewGroup.getChildAt(i), item);
		}
	}
	
	public void bind(Menu menu) {
		for (int i = 0; i < menu.size(); i++) {
			MenuItem menuItem = menu.getItem(i);
			if (menuItem.getSubMenu() != null)
				bind(menuItem.getSubMenu());
			if (menuItem.getActionView() != null)
				bind(menuItem.getActionView());
		}
	}
	
	public void unbind(View rootView) {
		if (rootView.getTag() instanceof String)
			internalUnbind(rootView);
		
		if (rootView instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup)rootView;
			for (int i = 0; i < viewGroup.getChildCount(); i++)
				unbind(viewGroup.getChildAt(i));
		}
	}
	
	public void unbind(Menu menu) {
		for (int i = 0; i < menu.size(); i++) {
			MenuItem menuItem = menu.getItem(i);
			if (menuItem.getSubMenu() != null)
				unbind(menuItem.getSubMenu());
			if (menuItem.getActionView() != null)
				unbind(menuItem.getActionView());
		}
	}
	
	private void internalBind(View view, Object item) {
		String[] bindings = ((String)view.getTag()).split("\\,");
		
		for (String binding : bindings) {
			int idx = binding.indexOf("=");
			if (idx < 0)
				continue;
			
			String viewProperty = binding.substring(0, idx);
			if (binding.charAt(idx+1) == '#' || binding.charAt(idx+1) == '$') {
				// Data binding
				boolean bidir = binding.charAt(idx+1) == '#';
				binding = binding.substring(idx+2);
				
				int idx2 = binding.lastIndexOf(".");
				
				Object beanRef = binding.substring(0, idx2);
				String beanProperty = binding.substring(idx2+1);
				
				if ("item".equals(beanRef) && item != null)
					beanRef = item;
				
				if (bidir)
					bindBidirectional(view, viewProperty, beanRef, beanProperty);
				else
					bind(view, viewProperty, beanRef, beanProperty);
			}
			else {
				// Listener binding
				binding = binding.substring(idx+1);
				int idx2 = binding.lastIndexOf(".");
				
				Object beanRef = binding.substring(0, idx2);
				String beanMethodName = binding.substring(idx2+1);
				
				bindListener(view, viewProperty, beanRef, beanMethodName);
			}
		}
	}
	
	private void internalUnbind(View view) {
		String[] bindings = ((String)view.getTag()).split("\\,");
		
		for (String binding : bindings) {
			int idx = binding.indexOf("=");
			if (idx < 0)
				continue;
			
			String viewProperty = binding.substring(0, idx);
			if (binding.charAt(idx+1) == '#' || binding.charAt(idx+1) == '$') {
				// Data binding
				unbind(view, viewProperty);
			}
			else {
				// Listener binding
				unbindListener(view, viewProperty);
			}
		}
	}
	
	
	private int ACTION_BINDING_TAG = 348623214;
	
	private Map<Class<?>, Object> listenersMap = new HashMap<Class<?>, Object>();
	
	private void initListeners() {
		listenersMap.put(View.OnClickListener.class, new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				applyBindings();
				
				MethodAction action = (MethodAction)v.getTag(ACTION_BINDING_TAG);
				if (action != null)
					action.invokeAction();
			}
		});
	}
	
	private InvocationHandler listenerInvocationHandler = new InvocationHandler() {		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getDeclaringClass() == Object.class || !method.getName().startsWith("on"))
				return method.invoke(proxy, args);
			
			View v = (View)args[0];
			MethodAction action = (MethodAction)v.getTag(ACTION_BINDING_TAG);
			if (action != null && method.getName().equals(action.getListenerMethod())) {
				applyBindings();
				
				Object[] args2 = new Object[args.length-1];
				if (args.length > 1)
					System.arraycopy(args, 1, args2, 0, args.length-1);
				return action.invokeAction(args2);
			}
			
			return null;
		}
	};
	
	private class MethodAction implements InvocationHandler {
		
		private final Object bean;
		private final String beanMethod;
		private final String listenerMethod;
		
		public MethodAction(Object bean, String beanMethod, String listenerMethod) {
			this.bean = bean;
			this.beanMethod = beanMethod;
			this.listenerMethod = listenerMethod;
		}
		
		public String getListenerMethod() {
			return listenerMethod;
		}
		
		public Object invokeAction(Object... args) {
			try {
				for (Method m : bean.getClass().getMethods()) {
					if (m.getName().equals(beanMethod)) {
						if (m.getParameterTypes().length == args.length)
							return m.invoke(bean, args);
						else if (m.getParameterTypes().length < args.length) {
							Object[] args2 = new Object[m.getParameterTypes().length];
							if (args2.length > 0)
								System.arraycopy(args, 0, args2, 0, args2.length);
							return m.invoke(bean, args2);
						}
					}
				}
				return null; 
			}
			catch (Exception e) {
				throw new RuntimeException("Cannot invoke method action " + beanMethod, e);
			}
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getDeclaringClass() == Object.class)
				return method.invoke(this, args);
			
			if (!method.getName().equals(listenerMethod))
				return false;	// Indicate not handled
			
			applyBindings();
			
			Object ret = invokeAction(args);
			if (ret == null)
				return true;
			return ret;
		}
	}
	
	
	public void bindListener(int viewId, String viewEvent, Object ref, String beanMethodName) {
		bindListener(activity.findViewById(viewId), viewEvent, ref, beanMethodName);
	}

	public void bindListener(View rootView, int viewId, String viewEvent, Object ref, String beanMethodName) {
		bindListener(rootView.findViewById(viewId), viewEvent, ref, beanMethodName);
	}
	
	public void bindListener(View view, String viewEvent, Object ref, String beanMethodName) {
		String listenerMethodName = viewEvent;
		int idx = viewEvent.indexOf(".");
		if (idx > 0) {
			listenerMethodName = viewEvent.substring(0, idx) + viewEvent.substring(idx+1, idx+2).toUpperCase() + viewEvent.substring(idx+2);
			viewEvent = viewEvent.substring(0, idx);
		}
		listenerMethodName = "on" + listenerMethodName.substring(0, 1).toUpperCase() + listenerMethodName.substring(1);
		
		String methodName = "setOn" + viewEvent.substring(0, 1).toUpperCase() + viewEvent.substring(1) + "Listener";
		Method method = null;
		for (Method m : view.getClass().getMethods()) {
			if (m.getName().equals(methodName)) {
				method = m;
				break;
			}
		}
		if (method == null)
			throw new RuntimeException("Not setter found for event " + viewEvent + " on view " + view);
		
		Object listener = null;
		MethodAction methodAction = new MethodAction(beanResolver.resolveBean(ref), beanMethodName, listenerMethodName);
		
		boolean withView = false;
		Class<?> listenerClass = method.getParameterTypes()[0];
		for (Method m : listenerClass.getMethods()) {
			if (m.getName().equals(listenerMethodName)) {
				withView = m.getParameterTypes().length > 0 && View.class.isAssignableFrom(m.getParameterTypes()[0]);
				break;
			}
		}
		
		if (withView) {
			view.setTag(ACTION_BINDING_TAG, methodAction);
			
			listener = listenersMap.get(method.getParameterTypes()[0]);
			if (listener == null) {
				listener = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { method.getParameterTypes()[0] }, listenerInvocationHandler);
				listenersMap.put(method.getParameterTypes()[0], listener);
			}
		}
		else
			listener = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { method.getParameterTypes()[0] }, methodAction);
		
		try {
			method.invoke(view, listener);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not set listener for event " + viewEvent + " on view " + view.getClass().getSimpleName(), e);
		}
	}
	
	public void unbindListener(int viewId, String viewEvent) {
		unbindListener(activity.findViewById(viewId), viewEvent);
	}
	
	public void unbindListener(View rootView, int viewId, String viewEvent) {
		unbindListener(rootView.findViewById(viewId), viewEvent);
	}
	
	public void unbindListener(View view, String viewEvent) {
		String methodName = "setOn" + viewEvent.substring(0, 1).toUpperCase() + viewEvent.substring(1) + "Listener";
		Method method = null;
		for (Method m : view.getClass().getMethods()) {
			if (m.getName().equals(methodName)) {
				method = m;
				break;
			}
		}
		if (method == null)
			throw new RuntimeException("Not setter found for event " + viewEvent + " on view " + view);
		
		view.setTag(ACTION_BINDING_TAG, null);
		
		try {
			method.invoke(view, (Object)null);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not unset listener for event " + viewEvent + " on view " + view.getClass().getSimpleName(), e);
		}
	}
	
	
	private static class MethodGetter<B, V> implements Getter<B, V> {
		
		private final Method getter;
		
		public MethodGetter(B bean, String beanPropertyName) {
			Method getter = null;
			try {
				getter = bean.getClass().getMethod("get" + beanPropertyName.substring(0, 1).toUpperCase() + beanPropertyName.substring(1));
			} 
			catch (NoSuchMethodException e) {
				try {
					getter = bean.getClass().getMethod("is" + beanPropertyName.substring(0, 1).toUpperCase() + beanPropertyName.substring(1));
				} 
				catch (NoSuchMethodException e2) {
					throw new RuntimeException("Could not find getter on bean " + bean.getClass().getName() + "." + beanPropertyName, e);
				}
			}
			this.getter = getter;
		}
		
		@SuppressWarnings("unchecked")
		public V getValue(B instance) throws Exception {
			return (V)getter.invoke(instance);
		}
	}
	
	private static class MethodBeanSetter<B> implements BeanSetter<B> {
		
		private final Method setter;
		
		public MethodBeanSetter(B bean, String beanPropertyName) {
			Method beanSetter = null;
			try {
				for (Method m : bean.getClass().getMethods()) {
					if (m.getName().equals("set" + beanPropertyName.substring(0, 1).toUpperCase() + beanPropertyName.substring(1)) && m.getParameterTypes().length == 1) {
						beanSetter = m;
						break;
					}
				}
			}
			catch (Exception e) {
				throw new RuntimeException("Could not find setter on bean " + bean.getClass().getName() + "." + beanPropertyName, e);
			}
			if (beanSetter == null)
				throw new RuntimeException("Could not find setter on bean " + bean.getClass().getName() + "." + beanPropertyName);
			this.setter = beanSetter;
		}
		
		@Override
		public void setValue(B instance, String beanPropertyName, Object value) throws Exception {
			setter.invoke(instance, value);
		}		
	}	
	
	
	public interface Binding {
		
		public boolean isFor(View view, String viewProperty);
		
		public boolean isFor(View view);
		
		public void unbind();
	}
	
	private class UnidirectionalBinding<B> implements Binding {
		
		private final WeakReference<View> viewRef;
		private final String viewPropertyName;
		private final ViewSetter<View> viewSetter;
		private final ObservableValue observableValue;
		
		public UnidirectionalBinding(View view, String viewPropertyName, B bean, String beanPropertyName) {
			this(view, viewPropertyName, bean, beanPropertyName, null);
		}
		
		public UnidirectionalBinding(View view, String viewPropertyName, B bean, String beanPropertyName, Getter<B, ?> beanPropertyGetter) {
			this(view, viewPropertyName, beanPropertyGetter != null 
					? new ObservableBeanProperty<B>(bean, beanPropertyName, beanPropertyGetter) 
					: new ObservableBeanProperty<B>(bean, beanPropertyName)
			);
		}
		
		@SuppressWarnings("unchecked")
		public UnidirectionalBinding(View view, String viewPropertyName, ObservableValue observableValue) {
			if (view == null)
				throw new NullPointerException("Cannot bind on null view");
			if (viewPropertyName == null)
				throw new NullPointerException("Cannot bind on null viewPropertyName");
			if (observableValue == null)
				throw new NullPointerException("Cannot bind on null observableValue");
			
			this.viewRef = new WeakReference<View>(view);
			this.viewSetter = (ViewSetter<View>)ViewBindingRegistry.getViewSetter(view.getClass(), viewPropertyName);
			this.viewPropertyName = viewPropertyName;
			
			this.observableValue = observableValue;
			this.observableValue.addChangeListener(observableValueChangeListener);
			
			setViewValue(this.observableValue.getValue());
		}
		
		public void unbind() {
			this.observableValue.removeChangeListener(observableValueChangeListener);
		}
		
		private void setViewValue(Object newValue) {
			View view = viewRef.get();
			if (view != null)
				viewSetter.setValue(view, newValue);
		}
		
		private PropertyChangeListener observableValueChangeListener = new PropertyChangeListener() {			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				Object newValue = event.getNewValue();
				setViewValue(newValue);
			}
		};
		
		public boolean isFor(View view, String viewPropertyName) {
			return viewRef.get() == view && viewPropertyName.equals(this.viewPropertyName);
		}
		
		public boolean isFor(View view) {
			return viewRef.get() == view;
		}
	}
	
	private class BidirectionalBinding implements Binding {
		
		private final WeakReference<View> viewRef;
		private final ViewSetter<View> viewSetter;
		private final String viewPropertyName;
		private final WritableObservableValue beanProperty;
		
		private boolean changing = false;
		
		@SuppressWarnings("unchecked")
		public BidirectionalBinding(View view, String viewPropertyName, Object bean, String beanPropertyName) {
			if (view == null)
				throw new NullPointerException("Cannot bind on null view");
			if (viewPropertyName == null)
				throw new NullPointerException("Cannot bind on null viewPropertyName");
			if (bean == null)
				throw new NullPointerException("Cannot bind on null bean");
			if (beanPropertyName == null)
				throw new NullPointerException("Cannot bind on null beanPropertyName");
			
			this.viewRef = new WeakReference<View>(view);
			this.viewSetter = (ViewSetter<View>)ViewBindingRegistry.getViewSetter(view.getClass(), viewPropertyName);
			
			this.beanProperty = new BeanProperty<Object>(bean, beanPropertyName);
			this.beanProperty.addChangeListener(beanPropertyChangeListener);
			
			setViewValue(beanProperty.getValue());
			
			ViewWatcher<? extends View> viewWatcher = viewWatchers.get(view);
			if (viewWatcher == null) {
				viewWatcher = ViewBindingRegistry.newWatcher(view);
				viewWatchers.put(view, viewWatcher);
			}
			
			viewWatcher.addPropertyChangeListener(viewPropertyName, viewPropertyChangeListener);
			this.viewPropertyName = viewPropertyName;
		}
		
		public void unbind() {
			beanProperty.removeChangeListener(beanPropertyChangeListener);
			
			View view = viewRef.get();
			if (view != null) {
				ViewWatcher<? extends View> viewWatcher = viewWatchers.get(view);
				if (viewWatcher != null)
					viewWatcher.removePropertyChangeListener(viewPropertyName, viewPropertyChangeListener);
				
				if (viewWatcher.isEmpty())
					viewWatchers.remove(view);
			}
		}
		
		private void setViewValue(Object newValue) {
			if (viewRef.get() != null)
				viewSetter.setValue(viewRef.get(), newValue);
		}
		
		public boolean isFor(View view, String viewPropertyName) {
			return viewRef.get() == view && viewPropertyName.equals(this.viewPropertyName);
		}
		
		public boolean isFor(View view) {
			return viewRef.get() == view;
		}
		
		private PropertyChangeListener viewPropertyChangeListener = new PropertyChangeListener() {			
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (changing)
					return;
				
				try {
					changing = true;
					Object newValue = event.getNewValue();
					beanProperty.setValue(newValue);
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
	
	
	private class ObservableBeanProperty<B> implements ObservableValue {
		
		protected final WeakReference<B> beanRef;
		protected final String beanPropertyName;
		private final Getter<B, ?> beanGetter;
		
		public ObservableBeanProperty(B bean, String beanPropertyName) {
			this.beanRef = new WeakReference<B>(bean);
			this.beanPropertyName = beanPropertyName;
			if (bean instanceof Map<?, ?>)
				this.beanGetter = null;
			else
				this.beanGetter = new MethodGetter<B, Object>(bean, beanPropertyName);
		}
		
		public ObservableBeanProperty(B bean, String beanPropertyName, Getter<B, ?> beanGetter) {
			this.beanRef = new WeakReference<B>(bean);
			this.beanPropertyName = beanPropertyName;
			this.beanGetter = beanGetter;
		}
		
		public void addChangeListener(PropertyChangeListener listener) {
			if (beanRef.get() != null)
				PropertyChangeHelper.addPropertyChangeListener(beanRef.get(), beanPropertyName, listener);
		}
		
		public void removeChangeListener(PropertyChangeListener listener) {
			if (beanRef.get() != null)
				PropertyChangeHelper.removePropertyChangeListener(beanRef.get(), beanPropertyName, listener);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Object getValue() {
			if (beanRef.get() != null) {
				try {
					if (beanGetter == null)
						return ((Map<String, ?>)beanRef.get()).get(beanPropertyName);
					return beanGetter.getValue(beanRef.get());
				}
				catch (Exception e) {
					throw new RuntimeException("Could not get bean property value " + beanPropertyName, e);
				}
			}
			return null;
		}		
	}
	
	private class BeanProperty<B> extends ObservableBeanProperty<B> implements WritableObservableValue {
		
		private final BeanSetter<B> beanSetter;
		
		public BeanProperty(B bean, String beanPropertyName) {
			super(bean, beanPropertyName);
			if (bean instanceof Map<?, ?>)
				this.beanSetter = null;
			else
				this.beanSetter = beanResolver.getBeanSetter(bean, beanPropertyName);
		}
		
		@SuppressWarnings("unchecked")
		public void setValue(Object value) {
			if (beanRef.get() != null) {
				try {
					if (beanSetter == null)
						((Map<String, Object>)beanRef.get()).put(beanPropertyName, value);
					else
						this.beanSetter.setValue(beanRef.get(), beanPropertyName, value);
				}
				catch (Exception e) {
					throw new RuntimeException("Could not set bean property value " + beanPropertyName, e);
				}
			}
		}
	}
}
