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
package org.granite.client.android.binding;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

/**
 * @author William DRAI
 */
public class TideLayoutInflater extends LayoutInflater {
	
	private final org.granite.client.tide.Context context;
	private final LayoutInflater wrappedInflater;
	
	public TideLayoutInflater(Context appContext, org.granite.client.tide.Context tideContext) {
		super(appContext);
		this.context = tideContext;
		this.wrappedInflater = LayoutInflater.from(appContext);
		this.setFactory(new TideFactory());
	}
	
	public TideLayoutInflater(LayoutInflater wrappedInflater, org.granite.client.tide.Context tideContext) {
		super(wrappedInflater.getContext());
		this.context = tideContext;
		this.wrappedInflater = wrappedInflater;
		this.setFactory(new TideFactory());
	}
	
	public static class TideFactory implements Factory {
		@Override
		public View onCreateView(String name, Context context, AttributeSet attrs) {
			try {
				Log.i("inflated", "Name: " + name);
				for (int i = 0; i < attrs.getAttributeCount(); i++) {
					String attrName = attrs.getAttributeName(i);
					Log.i("inflater", "Attribute: " + attrName + " = " + attrs.getAttributeValue(i));
				}
				return null;
			}
			catch (Exception e) {
				throw new RuntimeException("Fuck", e);
			}
		}
	}

	@Override
	public LayoutInflater cloneInContext(Context newContext) {
		return new TideLayoutInflater(this.wrappedInflater.cloneInContext(newContext), this.context);
	}
}
