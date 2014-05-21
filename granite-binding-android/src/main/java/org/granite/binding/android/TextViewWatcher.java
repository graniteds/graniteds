/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
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