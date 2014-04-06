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
package org.granite.client.javafx.tide.collections;

import org.granite.tide.data.model.SortInfo;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;

/**
 * @author William DRAI
 */
public class TableViewSortAdapter<S> implements SortAdapter {
	
	private TableView<S> tableView;
	private S exampleData;

	public TableViewSortAdapter(final TableView<S> tableView, final Class<S> exampleDataClass) {
		this.tableView = tableView;
		try {
			this.exampleData = exampleDataClass.newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException("Could not instantiate example data class " + exampleDataClass, e);
		}
	}
	
	public void apply(SortInfo sortInfo) {
		String[] order = sortInfo.getOrder();
		boolean[] desc = sortInfo.getDesc();
		
		if (order != null && desc != null) {
			// Apply current sort to attached TableView
			tableView.getSortOrder().clear();
			for (int i = 0; i < order.length; i++) {
				for (TableColumn<S, ?> column : tableView.getColumns()) {
					ObservableValue<?> property = column.getCellObservableValue(exampleData);
					if (property instanceof ReadOnlyProperty<?> && ((ReadOnlyProperty<?>)property).getName().equals(order[i])) {
						tableView.getSortOrder().add(column);
						column.setSortType(desc[i] ? SortType.DESCENDING : SortType.ASCENDING);
					}
				}
			}
		}
	}
	
	public void retrieve(SortInfo sortInfo) {
		int i = 0;
		String[] order = new String[tableView.getSortOrder().size()];
		boolean[] desc = new boolean[tableView.getSortOrder().size()];
		for (TableColumn<S, ?> column : tableView.getSortOrder()) {
			ObservableValue<?> property = column.getCellObservableValue(exampleData);
			if (property instanceof ReadOnlyProperty<?>) {
				order[i] = ((ReadOnlyProperty<?>)property).getName();
				desc[i] = column.getSortType() == SortType.DESCENDING;
				i++;
			}
			else
				throw new IllegalArgumentException("Sortable cell values must implement Property to apply TableViewSort adapter");
		}
		sortInfo.setOrder(order.length > 0 ? order : null);
		sortInfo.setDesc(desc.length > 0 ? desc : null);
	}
}
