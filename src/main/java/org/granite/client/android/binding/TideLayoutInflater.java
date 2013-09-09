package org.granite.client.android.binding;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

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
