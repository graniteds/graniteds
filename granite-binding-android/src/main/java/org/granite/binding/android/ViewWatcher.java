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
package org.granite.binding.android;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.view.View;

/**
 * @author William DRAI
 */
public class ViewWatcher<V extends View> {
	
	private final WeakReference<V> view;
	protected final Map<String, Object> currentValues = new HashMap<String, Object>();
	protected PropertyChangeSupport pcs;
	
	public ViewWatcher(V view) {
		this.view = new WeakReference<V>(view);
		this.pcs = new PropertyChangeSupport(view);
	}
	
	protected V getView() {
		if (view.get() == null)
			clear();
		
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
		if (value instanceof CharSequence)
			value = ((CharSequence)value).toString();
		currentValues.put(property, value);
	}
	protected Object getCurrentValue(String property) {
		return currentValues.get(property);
	}
	
	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		setCurrentValue(property, evaluate(property));
		pcs.addPropertyChangeListener(property, listener);
	}
	
	public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(property, listener);
		currentValues.remove(property);
	}
	
	protected Object evaluate(String property) {
		try {
			Method getter = getView().getClass().getMethod("get" + property.substring(0, 1).toUpperCase() + property.substring(1));
			Object value = getter.invoke(getView());
			if (value instanceof CharSequence)
				value = ((CharSequence)value).toString();
			return value;
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