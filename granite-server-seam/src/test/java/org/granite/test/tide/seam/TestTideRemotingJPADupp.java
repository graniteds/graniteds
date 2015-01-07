/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.seam;

import javax.servlet.ServletContext;

import org.granite.test.tide.TestDataUpdatePostprocessor.WrappedUpdate;
import org.granite.test.tide.data.Person;
import org.granite.tide.data.DataContext.EntityUpdateType;
import org.granite.tide.invocation.InvocationResult;
import org.junit.Assert;
import org.junit.Test;


public class TestTideRemotingJPADupp extends AbstractTideTestCase {
	
	@Override
	protected ServletContext initServletContext() {
		ServletContext servletContext = super.initServletContext();
		servletContext.setAttribute("dupp", true);
		return servletContext;
	}
    
	@Test
    public void testPersistCall() {
        InvocationResult result = invokeComponent("personAction", "create", new Object[] { 13L, "joe" });
        
        Assert.assertEquals("Update count", 1, result.getUpdates().length);
        Assert.assertEquals("Update type", EntityUpdateType.PERSIST.name(), result.getUpdates()[0][0]);
        Assert.assertEquals("Update class", WrappedUpdate.class, result.getUpdates()[0][1].getClass());
        Assert.assertEquals("Update class 2", Person.class, ((WrappedUpdate)result.getUpdates()[0][1]).getEntity().getClass());
    }
}
