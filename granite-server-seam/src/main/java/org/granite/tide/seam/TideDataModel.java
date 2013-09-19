/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.seam;

import java.io.Serializable;

import javax.faces.model.DataModel;


/**
 * TideDataModel wrapper
 * 
 * @author William DRAI
 */
public class TideDataModel extends DataModel implements Serializable {
    
	private static final long serialVersionUID = 1L;
	
	
	private Object rowData;
    private Object wrappedData;

    
    public TideDataModel(Object object) {
        this.wrappedData = object;
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public Object getRowData() {
        return rowData;
    }
    
    public void setRowData(Object data) {
        this.rowData = data;
    }

    @Override
    public int getRowIndex() {
        return 0;
    }

    @Override
    public Object getWrappedData() {
        return wrappedData;
    }

    @Override
    public boolean isRowAvailable() {
        return false;
    }

    @Override
    public void setRowIndex(int rowIndex) {
    }

    @Override
    public void setWrappedData(Object data) {
        wrappedData = data;
    }
}
