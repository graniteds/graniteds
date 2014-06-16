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
package org.granite.client.javafx.util;

import java.util.ArrayList;
import java.util.List;

import org.granite.client.javafx.util.ChangeWatcher.Trigger;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;


public class ChainedObservableValue<T, P> implements ObservableValue<P>, ChangeListener<T>, Trigger<T, P> {
	
	private final ObservableValueGetter<T, P> targetObservableValueGetter;
	
	private ObservableValue<P> targetObservableValue = null;
	
	private List<ChangeListener<? super P>> changeListeners = new ArrayList<ChangeListener<? super P>>();
	private List<InvalidationListener> invalidationListeners = new ArrayList<InvalidationListener>();
	
	
	public static <T, P> ChainedObservableValue<T, P> chain(ObservableValue<T> sourceObservableValue, ObservableValueGetter<T, P> targetPropertyGetter) {
		return new ChainedObservableValue<T, P>(sourceObservableValue, targetPropertyGetter);
	}

	public static <T, P> ChainedObservableValue<T, P> chain(ChangeWatcher<T> sourceWatcher, ObservableValueGetter<T, P> targetPropertyGetter) {
		return new ChainedObservableValue<T, P>(sourceWatcher, targetPropertyGetter);
	}

	
	public ChainedObservableValue(ObservableValue<T> sourceObservableValue, ObservableValueGetter<T, P> targetPropertyGetter) {
		this.targetObservableValueGetter = targetPropertyGetter;
		
		sourceObservableValue.addListener(this);
		
		afterChange(sourceObservableValue.getValue(), null);
	}
	
	public ChainedObservableValue(ChangeWatcher<T> sourceWatcher, ObservableValueGetter<T, P> targetPropertyGetter) {
		this.targetObservableValueGetter = targetPropertyGetter;
		
		sourceWatcher.addTrigger(this);
	}
	
	public <X> ChainedObservableValue<P, X> chain(ObservableValueGetter<P, X> nextPropertyGetter) {
		return new ChainedObservableValue<P, X>(this, nextPropertyGetter);
	}
	
	public P beforeChange(T oldSource) {
		P oldValue = targetObservableValue != null ? targetObservableValue.getValue() : null;
		
		if (targetObservableValue != null) {
			targetObservableValue.removeListener(targetChangeListener);
			targetObservableValue.removeListener(targetInvalidationListener);
		}
		
		return oldValue;
	}
	
	public void afterChange(T newSource, P oldValue) {
		if (newSource != null)
			targetObservableValue = targetObservableValueGetter.getObservableValue(newSource);
		
		P newValue = targetObservableValue != null ? targetObservableValue.getValue() : null;
		
		if (targetObservableValue != null) {
			targetObservableValue.addListener(targetChangeListener);
			targetObservableValue.addListener(targetInvalidationListener);
		}
		
		if (newValue != oldValue) {
			targetInvalidationListener.invalidated(ChainedObservableValue.this);
			targetChangeListener.changed(ChainedObservableValue.this, oldValue, newValue);
		}
	}
	
	@Override
	public void changed(ObservableValue<? extends T> source, T oldSource, T newSource) {
		P oldValue = beforeChange(oldSource);
		
		afterChange(newSource, oldValue);
	}
	
	private ChangeListener<? super P> targetChangeListener = new ChangeListener<P>() {
		@Override
		public void changed(ObservableValue<? extends P> target, P oldTarget, P newTarget) {
			for (ChangeListener<? super P> listener : changeListeners) {
				listener.changed(target, oldTarget, newTarget);
			}
		}			
	};
	
	private InvalidationListener targetInvalidationListener = new InvalidationListener() {
		@Override
		public void invalidated(Observable observable) {
			for (InvalidationListener listener : invalidationListeners)
				listener.invalidated(observable);
		}			
	};

	@Override
	public P getValue() {
		if (targetObservableValue == null)
			return null;
		return targetObservableValue.getValue();
	}
	
	@Override
	public void addListener(ChangeListener<? super P> listener) {
		changeListeners.add(listener);
	}
	
	@Override
	public void removeListener(ChangeListener<? super P> listener) {
		changeListeners.remove(listener);
	}
	
	@Override
	public void addListener(InvalidationListener listener) {
		invalidationListeners.add(listener);
	}
	
	@Override
	public void removeListener(InvalidationListener listener) {
		invalidationListeners.remove(listener);
	}
	
	
	public static interface ObservableValueGetter<B, T> {
		
		ObservableValue<T> getObservableValue(B bean);
	}
}	
