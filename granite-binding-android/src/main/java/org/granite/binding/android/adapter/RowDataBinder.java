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
