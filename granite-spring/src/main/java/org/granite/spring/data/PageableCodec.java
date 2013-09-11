/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.ExtendedObjectOutput;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

/**
 * @author Franck WOLFF
 */
public class PageableCodec implements ExtendedObjectCodec {
    
	private static final Field pageableField;
	static {
		Field pageableFieldTmp = null;
		try {
			pageableFieldTmp = PageImpl.class.getDeclaredField("pageable");
			pageableFieldTmp.setAccessible(true);
		}
		catch (NoSuchFieldException e) {
			// Other exception mean that Spring Data is not present
		    // Don't catch so codec is not installed
		}
		pageableField = pageableFieldTmp;
	}

	public PageableCodec() {
	}

	public boolean canEncode(ExtendedObjectOutput out, Object v) {
		return v instanceof Page;
	}

	public String getEncodedClassName(ExtendedObjectOutput out, Object v) {
		return org.granite.tide.data.model.Page.class.getName();
	}

	public void encode(ExtendedObjectOutput out, Object v) throws IOException, IllegalAccessException, InvocationTargetException {
		@SuppressWarnings("unchecked")
		Page<Object> springPage = (Page<Object>)v;
		
		int offset;
		if (springPage instanceof PageImpl && pageableField != null) {
            Pageable springPageable = (Pageable)pageableField.get(springPage);
            offset = springPageable.getOffset();
        }
		else
			offset = springPage.getNumber() * springPage.getSize();
		
		out.writeObject(Integer.valueOf(offset));
		out.writeObject(Integer.valueOf(springPage.getSize()));
		out.writeObject(Integer.valueOf((int)springPage.getTotalElements()));
		out.writeObject(new ArrayList<Object>(springPage.getContent()));
	}

	public boolean canDecode(ExtendedObjectInput in, String className) throws ClassNotFoundException {
		return org.granite.tide.data.model.PageInfo.class.getName().equals(className);
	}

	public String getDecodedClassName(ExtendedObjectInput in, String className) {
		return OffsetPageRequest.class.getName();
	}

	public Object newInstance(ExtendedObjectInput in, String className)
			throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		
		int firstResult = ((Integer)in.readObject()).intValue();
		int maxResults = ((Integer)in.readObject()).intValue();
		String[] orderBys = (String[])in.readObject();
		boolean[] orderDescs = (boolean[])in.readObject();
		
		Sort sort = null;
		if (checkSort(orderBys, orderDescs)) {
			List<Order> orders = new ArrayList<Order>(orderBys.length);
			for (int i = 0; i < orderBys.length; i++)
				orders.add(new Order(orderDescs[i] ? Direction.DESC : Direction.ASC, orderBys[i]));
			sort = new Sort(orders);
		}
		
		return new OffsetPageRequest(firstResult, maxResults, sort);
	}

	public void decode(ExtendedObjectInput in, Object v) throws IOException,
			ClassNotFoundException, IllegalAccessException,
			InvocationTargetException {
	}
	
	private boolean checkSort(String[] orderBys, boolean[] orderDescs) {
		if (orderBys != null) {
			if (orderDescs == null)
				throw new IllegalArgumentException("orderBys == " + Arrays.toString(orderBys) + " but sortDescs == null");
			if (orderDescs.length != orderBys.length)
				throw new IllegalArgumentException("orderBys == " + Arrays.toString(orderBys) + " but sortDescs == " + Arrays.toString(orderBys));
			return orderBys.length > 0;
		}
		else if (orderDescs != null)
			throw new IllegalArgumentException("orderBys == null but sortDescs != null");
		return false;
	}
}
