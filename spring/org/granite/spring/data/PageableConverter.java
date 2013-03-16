/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.spring.data;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.convert.Reverter;
import org.granite.tide.data.model.PageInfo;
import org.granite.tide.data.model.SortInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;


public class PageableConverter extends Converter implements Reverter {
	
	public PageableConverter(Converters converters) {
		super(converters);
	}

	@Override
	protected boolean internalCanConvert(Object value, Type targetType) {
		return (value instanceof PageInfo && targetType.equals(Pageable.class))
			|| (value instanceof SortInfo && targetType.equals(Sort.class));
	}

	@Override
	protected Object internalConvert(Object value, Type targetType) {
		if (targetType.equals(Pageable.class)) {
			PageInfo pageInfo = (PageInfo)value;
			Sort sort = null;
			if (pageInfo.getSortInfo() != null && pageInfo.getSortInfo().getOrder() != null && pageInfo.getSortInfo().getOrder().length > 0) {
				List<Order> orders = new ArrayList<Order>(pageInfo.getSortInfo().getOrder().length);
				for (int i = 0; i < pageInfo.getSortInfo().getOrder().length; i++)
					orders.add(new Order(pageInfo.getSortInfo().getDesc()[i] ? Direction.DESC : Direction.ASC, pageInfo.getSortInfo().getOrder()[i]));			
				sort = new Sort(orders);			
			}
			return new PageRequest(pageInfo.getFirstResult(), pageInfo.getMaxResults(), sort);
		}
		return null;
	}

	public boolean canRevert(Object value) {
		return value instanceof Page;
	}

	public Object revert(Object value) {
		@SuppressWarnings("unchecked")
		Page<Object> page = (Page<Object>)value;
		int offset = page.getNumber() * page.getSize();
		try {
			Field f = page.getClass().getDeclaredField("pageable");
			f.setAccessible(true);
			Pageable pageable = (Pageable)f.get(page);
			offset = pageable.getOffset();
		}
		catch (Exception e) {
		}
		return new org.granite.tide.data.model.Page<Object>(offset, page.getSize(), Long.valueOf(page.getTotalElements()).intValue(), page.getContent());
	}
	
	
	public static class PageRequest implements Pageable {
		
		private int offset;
		private int pageSize;
		private Sort sort;
		
		public PageRequest(int offset, int pageSize, Sort sort) {
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
	}
}
