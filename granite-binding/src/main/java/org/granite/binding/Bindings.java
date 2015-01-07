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
package org.granite.binding;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Bindings {
	
	public static ObservableValue not(ObservableValue source) {
		return new UniOperatorBinding<Boolean>(source) {
			@Override
			protected Boolean evaluate(Object val) {
				return val != null ? !(Boolean)val : null;
			}
		};
	}
	
	public static ObservableValue and(ObservableValue value1, ObservableValue value2) {
		return new BiOperatorBinding<Boolean>(value1, value2) {
			@Override
			protected Boolean evaluate(Object val1, Object val2) {
				return val1 == null || val2 == null ? null : (Boolean.TRUE.equals(val1) && Boolean.TRUE.equals(val2));
			}
		};
	}

	public static ObservableValue or(ObservableValue value1, ObservableValue value2) {
		return new BiOperatorBinding<Boolean>(value1, value2) {
			@Override
			protected Boolean evaluate(Object val1, Object val2) {
				return val1 == null || val2 == null ? null : (Boolean.TRUE.equals(val1) || Boolean.TRUE.equals(val2));
			}
		};
	}
	
	public static ObservableValue equals(ObservableValue value1, ObservableValue value2) {
		return new BiOperatorBinding<Boolean>(value1, value2) {
			@Override
			protected Boolean evaluate(Object val1, Object val2) {
				return val1 == null || val2 == null ? false : val1.equals(val2);
			}
		};
	}

	
	private static abstract class UniOperatorBinding<T> implements ObservableValue {
		
		private final ObservableValue source;
		private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
		private T currentValue;
		
		public UniOperatorBinding(ObservableValue source) {
			this.source = source;
			this.currentValue = evaluate(source.getValue());
			this.source.addChangeListener(notChangeListener);
		}
		
		public Object getValue() {
			return currentValue;
		}
		
		protected abstract T evaluate(Object val);
		
		public void addChangeListener(PropertyChangeListener listener) {
			pcs.addPropertyChangeListener(listener);
		}
		public void removeChangeListener(PropertyChangeListener listener) {
			pcs.addPropertyChangeListener(listener);
		}
		
		private final PropertyChangeListener notChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent pce) {
				T oldValue = currentValue;
				currentValue = evaluate(pce.getNewValue());
				pcs.firePropertyChange(pce.getPropertyName(), oldValue, currentValue);
			}
		};
	}

	private static abstract class BiOperatorBinding<T> implements ObservableValue {
		
		private final ObservableValue value1;
		private final ObservableValue value2;
		private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
		private T currentValue;
		
		public BiOperatorBinding(ObservableValue value1, ObservableValue value2) {
			this.value1 = value1;
			this.value2 = value2;
			currentValue = evaluate(value1.getValue(), value2.getValue());
			value1.addChangeListener(value1ChangeListener);
			value2.addChangeListener(value2ChangeListener);
		}
		
		public Object getValue() {
			return currentValue;
		}
		
		protected abstract T evaluate(Object val1, Object val2);
		
		public void addChangeListener(PropertyChangeListener listener) {
			pcs.addPropertyChangeListener(listener);
		}
		public void removeChangeListener(PropertyChangeListener listener) {
			pcs.addPropertyChangeListener(listener);
		}
		
		private final PropertyChangeListener value1ChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent pce) {
				T oldValue = currentValue;
				currentValue = evaluate(pce.getNewValue(), value2.getValue());
				if (currentValue != oldValue)
					pcs.firePropertyChange(pce.getPropertyName(), oldValue, currentValue);
			}
		};
		
		private final PropertyChangeListener value2ChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent pce) {
				T oldValue = currentValue;
				currentValue = evaluate(value1.getValue(), pce.getNewValue());
				if (currentValue != oldValue)
					pcs.firePropertyChange(pce.getPropertyName(), oldValue, currentValue);
			}
		};
	}
}
