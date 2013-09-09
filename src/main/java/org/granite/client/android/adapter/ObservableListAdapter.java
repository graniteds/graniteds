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
