package org.granite.client.android.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.text.GetChars;
import android.view.View;

public class ViewWatcher<V extends View> {
	
	private final WeakReference<V> view;
	protected final Map<String, Object> currentValues = new HashMap<String, Object>();
	protected PropertyChangeSupport pcs;
	
	public ViewWatcher(V view, String property) {
		this.view = new WeakReference<V>(view);
		this.pcs = new PropertyChangeSupport(view);
		
		addProperty(property);
	}
	
	protected V getView() {
		if (view.get() == null) {
			clear();
		}
		return view.get();
	}
	
	protected void clear() {
		currentValues.clear();
		pcs = null;
	}
	
	public boolean isEmpty() {
		return currentValues.isEmpty();
	}
	
	protected void setCurrentValue(String property, Object value) {
		if (value instanceof GetChars)
			value = ((GetChars)value).toString();
		currentValues.put(property, value);
	}
	protected Object getCurrentValue(String property) {
		return currentValues.get(property);
	}
	
	public void addProperty(String property) {
		setCurrentValue(property, evaluate(property));
	}
	
	public void removeProperty(String property) {
		currentValues.remove(property);
	}
	
	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(property, listener);
	}
	
	public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(property, listener);
	}
	
	private Object evaluate(String property) {
		try {
			Method getter = view.getClass().getMethod("get" + property.substring(0, 1).toUpperCase() + property.substring(1));
			return getter.invoke(view);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not evaluate " + view + "." + property);
		}
	}
	
	public void apply() {
		for (Entry<String, Object> entry : currentValues.entrySet()) {
			String property = entry.getKey();
			Object newValue = evaluate(property);
			
			if (newValue != entry.getValue())
				pcs.firePropertyChange(new PropertyChangeEvent(view, property, entry.getValue(), newValue));
			
			entry.setValue(newValue);
		}
	}
}