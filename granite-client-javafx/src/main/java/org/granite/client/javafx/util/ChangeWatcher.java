package org.granite.client.javafx.util;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;


public class ChangeWatcher<T> implements ChangeListener<T> {
	
	public static <T> ChangeWatcher<T> watch(ObservableValue<T> sourceObservableValue) {
		return new ChangeWatcher<T>(sourceObservableValue);
	}
	
	public static interface Trigger<T, X> {
		
		X beforeChange(T oldSource);
		
		void afterChange(T newSource, X value);		
	}
	
	private List<Trigger<T, ?>> triggers = new ArrayList<Trigger<T, ?>>();
	
	private final ObservableValue<T> sourceObservableValue;
	
	public ChangeWatcher(ObservableValue<T> sourceObservableValue) {
		this.sourceObservableValue = sourceObservableValue;
		sourceObservableValue.addListener(this);
	}
	
	public void addTrigger(Trigger<T, ?> trigger) {
		triggers.add(trigger);
		
		if (sourceObservableValue.getValue() != null)
			trigger.afterChange(sourceObservableValue.getValue(), null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void changed(ObservableValue<? extends T> source, T oldSource, T newSource) {
		Object[] results = new Object[triggers.size()];
		int i = 0;
		for (Trigger<T, ?> trigger : triggers)
			results[i++] = trigger.beforeChange(oldSource);
		
		i = 0;
		for (Trigger<T, ?> trigger : triggers)
			((Trigger<T, Object>)trigger).afterChange(newSource, results[i++]);
	}
}	
