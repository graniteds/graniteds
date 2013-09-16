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
package org.granite.test.tide.seam.home;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.framework.EntityHome;


@Name("baseHome")
@AutoCreate
@BypassInterceptors
public class BaseHome extends EntityHome<BaseEntity> {

	private static final long serialVersionUID = 1L;

	
	@Override
	public void create() {
	}	
	
	@Override
	public BaseEntity find() {
		if (Long.valueOf(1200L).equals(getId())) {
	    	Entity1 entity1 = new Entity1();
	    	entity1.setId(1200L);
	    	entity1.setSomeObject("$$Proxy$$test");
	    	return entity1;
		}
		if (Long.valueOf(1201L).equals(getId())) {
	    	Entity2 entity2 = new Entity2();
	    	entity2.setId(1201L);
	    	return entity2;
		}
		return null;
	}
	
	@Override
	public String update() {
		return "test";
	}
	
	@Override
	protected void joinTransaction() {
	}
	
}
