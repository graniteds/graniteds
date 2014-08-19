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

import org.granite.binding.android.Binder;
import org.granite.binding.collection.CollectionChangeEvent;
import org.granite.binding.collection.CollectionChangeListener;
import org.granite.binding.collection.ObservableList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * @author William DRAI
 */
public class ObservableListAdapter<E> extends BaseAdapter {

    private CollectionChangeListener collChangeListener = new CollectionChangeListener() {
		@Override
		public void collectionChange(CollectionChangeEvent event) {
    		notifyDataSetChanged();
		}
    };
    
    private final LayoutInflater inflater;
    private final Binder binder;
    private final ObservableList<E> list;
    private final int rowViewId;
    private final DataBinder<E> rowBinder;
    
    public ObservableListAdapter(Context context, ObservableList<E> list, int rowViewId, DataBinder<E> rowBinder) {
    	this.inflater = LayoutInflater.from(context);
    	this.binder = null;
        this.list = list;
        this.rowViewId = rowViewId;
        this.rowBinder = rowBinder;
    	list.addCollectionChangeListener(collChangeListener);
    }

    public ObservableListAdapter(Context context, ObservableList<E> list, int rowViewId, Binder binder) {
        this.inflater = LayoutInflater.from(context);
        this.binder = binder;
        this.list = list;
        this.rowViewId = rowViewId;
        this.rowBinder = null;
    	list.addCollectionChangeListener(collChangeListener);
    }
    
	@Override
	public int getCount() {
		return list.size();
	}
	
	@Override
	public E getItem(int position) {
		return list.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		E item = getItem(position);
		if (rowBinder != null)
			return rowBinder.getItemId(item);
		return binder.getId(item);
	}
	
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	E item = getItem(position);
    	
    	if (rowBinder != null) {
	        if (convertView == null)
	            convertView = inflater.inflate(rowViewId, null);
	        else
	        	rowBinder.unbind(convertView, item);
	        
	        rowBinder.bind(convertView, item);
    	}
    	else if (binder != null) {
	        if (convertView == null)
	            convertView = inflater.inflate(rowViewId, null);
	        else
	        	binder.unbind(convertView);
	        
	        binder.bind(convertView, item);
    	}
        
        return convertView;
    }
}
