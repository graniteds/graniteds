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
package org.granite.binding.android.adapter;

import java.util.ArrayList;
import java.util.List;

import org.granite.binding.android.Binder;

import android.view.View;

/**
 * @author William DRAI
 */
public abstract class RowDataBinder<E> implements DataBinder<E> {
	
	private final Binder binder;
	
	public RowDataBinder(Binder binder) {
		this.binder = binder;
	}
	
	private List<RowBinding> rowBindings = new ArrayList<RowBinding>();
	
	public RowDataBinder<E> bind(int viewId, String viewProperty, String beanProperty) {
		rowBindings.add(new RowBinding(viewId, viewProperty, beanProperty));
		return this;
	}
	
	public void bind(View view, E item) {
		for (RowBinding rb : rowBindings)
			binder.bind(view, rb.viewId, rb.viewProperty, item, rb.beanProperty);
	}
	
	public void unbind(View view, E item) {
		for (RowBinding rb : rowBindings)
			binder.unbind(view, rb.viewId, rb.viewProperty);
	}
	
	
	public static final class RowBinding {
		
		public int viewId;
		public String viewProperty;
		public String beanProperty;
		
		public RowBinding(int viewId, String viewProperty, String beanProperty) {
			this.viewId = viewId;
			this.viewProperty = viewProperty;
			this.beanProperty = beanProperty;
		}
	}
}
