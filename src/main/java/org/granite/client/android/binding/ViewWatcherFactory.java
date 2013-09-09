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