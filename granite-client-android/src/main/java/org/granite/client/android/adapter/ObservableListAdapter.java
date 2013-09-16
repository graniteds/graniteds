/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.android.adapter;

import org.granite.client.collection.CollectionChangeEvent;
import org.granite.client.collection.CollectionChangeListener;
import org.granite.client.collection.ObservableList;
import org.granite.client.persistence.Persistence;
import org.granite.client.platform.Platform;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * @author William DRAI
 */
public class ObservableListAdapter<E> extends BaseAdapter {
	
	private Persistence persistence = Platform.persistence();

    private CollectionChangeListener listChangeListener = new CollectionChangeListener() {
		@Override
		public void collectionChange(CollectionChangeEvent event) {
    		notifyDataSetChanged();
		}
    };
    
    private final LayoutInflater inflater;
    private final ObservableList<E> list;
    private final int rowView;
    private final DataBinder<E> rowBinder;
    
    public ObservableListAdapter(Context context, ObservableList<E> list, int rowView, DataBinder<E> rowBinder) {
        super();

        this.inflater = LayoutInflater.from(context);
        this.list = list;
        this.rowView = rowView;
        this.rowBinder = rowBinder;
    	list.addCollectionChangeListener(listChangeListener);
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
		Object item = getItem(position);
		return persistence.getUid(item).hashCode();
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
        
        if (convertView == null)
            convertView = inflater.inflate(rowView, null);
        else
        	rowBinder.unbind(convertView, item);
        
        rowBinder.bind(convertView, item);
        
        return convertView;
    }
}
