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
package org.granite.spring.data;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetPageRequest implements Pageable {
    
    private int offset;
    private int pageSize;
    private Sort sort;
    
    public OffsetPageRequest(int offset, int pageSize, Sort sort) {
        this.offset = offset;
        this.pageSize = pageSize;
        this.sort = sort;
    }

    public int getOffset() {
        return offset;
    }

    public int getPageNumber() {
        return pageSize > 0 ? offset / pageSize : 0;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Sort getSort() {
        return sort;
    }

	public boolean hasPrevious() {
		return offset > 0;
	}

	public Pageable first() {
		return new OffsetPageRequest(0, pageSize, sort);
	}

	public Pageable previousOrFirst() {
		if (offset > pageSize)
			return new OffsetPageRequest(offset-pageSize, pageSize, sort);
		else
			return new OffsetPageRequest(0, pageSize, sort);
	}
	
	public Pageable next() {
		return new OffsetPageRequest(offset+pageSize, pageSize, sort);
	}
}