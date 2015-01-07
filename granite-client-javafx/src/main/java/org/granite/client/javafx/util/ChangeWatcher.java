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
