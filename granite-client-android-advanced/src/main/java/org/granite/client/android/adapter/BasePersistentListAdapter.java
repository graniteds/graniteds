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
package org.granite.client.android.adapter;

import java.util.List;

import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentCollection.ChangeListener;

import android.widget.BaseAdapter;

/**
 * @author William DRAI
 */
public abstract class BasePersistentListAdapter<E> extends BaseAdapter {

    @SuppressWarnings("rawtypes")
	private ChangeListener listChangeListener = new ChangeListener() {
		@Override
		public void changed(PersistentCollection coll) {
			notifyDataSetChanged();
		}
    };

    @SuppressWarnings("unchecked")
	public BasePersistentListAdapter(List<E> list) {
        super();

        if (list instanceof PersistentCollection)
        	((PersistentCollection<?>)list).addListener(listChangeListener);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}