package org.granite.client.android.adapter;

import android.view.View;


public interface DataBinder<E> {
	
	public void bind(View view, E item);
	
	public void unbind(View view, E item);
}
