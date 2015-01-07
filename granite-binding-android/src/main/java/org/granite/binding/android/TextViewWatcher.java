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

import java.beans.PropertyChangeListener;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

/**
 * @author William DRAI
 */
public class TextViewWatcher extends ViewWatcher<TextView> {
	
	public TextViewWatcher(TextView view) {
		super(view);
	}
	
	@Override
	public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
		if ("text".equals(property)) {
			getView().addTextChangedListener(textWatcher);
			setCurrentValue(property, getView().getText() != null ? getView().getText().toString() : null);
			pcs.addPropertyChangeListener(property, listener);
			return;
		}
		
		super.addPropertyChangeListener(property, listener);
	}
	
	@Override
	public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
		if ("text".equals(property)) {
			pcs.removePropertyChangeListener(property, listener);
			getView().removeTextChangedListener(textWatcher);
			return;
		}
		
		super.removePropertyChangeListener(property, listener);		
	}
	
	@Override
	protected Object evaluate(String property) {
		if ("text".equals(property))
			return getView().getText().toString();
		
		return super.evaluate(property);
	}
	
	
	private final TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String newValue = s != null ? s.toString() : null;
			pcs.firePropertyChange("text", getCurrentValue("text"), newValue);
			setCurrentValue("text", newValue);
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
		}
	};
}