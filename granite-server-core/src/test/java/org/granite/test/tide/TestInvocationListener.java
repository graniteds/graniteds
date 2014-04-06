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
package org.granite.test.tide;

import org.granite.messaging.service.ServiceInvocationContext;
import org.granite.messaging.service.ServiceInvocationListener;


public class TestInvocationListener implements ServiceInvocationListener {
	
	private boolean failBefore = false;
	
	public void setFailBefore(boolean failBefore) {
		this.failBefore = failBefore;
	}
	
	@Override
	public Object[] beforeMethodSearch(Object invokee, String methodName, Object[] args) {
		return args;
	}

	@Override
	public void beforeInvocation(ServiceInvocationContext context) {
		if (failBefore)
			throw new RuntimeException("ForceFail");
	}

	@Override
	public void afterInvocationError(ServiceInvocationContext context, Throwable t) {
	}

	@Override
	public Object afterInvocation(ServiceInvocationContext context, Object result) {
		return result;
	}

}
