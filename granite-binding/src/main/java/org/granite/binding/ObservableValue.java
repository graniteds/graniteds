package org.granite.binding;

import java.beans.PropertyChangeListener;

public interface ObservableValue {
	
	public void addChangeListener(PropertyChangeListener listener);
	
	public void removeChangeListener(PropertyChangeListener listener);

	public Object getValue();
}
