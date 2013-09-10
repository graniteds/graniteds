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

package org.granite.tide.ejb;

import java.lang.reflect.Method;
import java.util.Set;

import org.granite.tide.util.AbstractContext;

/**
 * @author Franck WOLFF
 */
public class Context extends AbstractContext {

	private static final long serialVersionUID = 1L;
	
	private final EjbServiceContext service;

	private Context(EjbServiceContext service) {
		super(EjbScannedItemHandler.instance().getObservers());
		this.service = service;
	}
	
	public static Context create(EjbServiceContext service) {
		return new Context(service);
	}

	@Override
	protected Object callMethod(Method method, Object... args) throws Exception {
		return service.callComponent(method, args);
	}

	@Override
	protected Set<String> getRemoteObservers() {
		return service.getRemoteObservers();
	}
}
