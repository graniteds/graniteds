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
package org.granite.test.tide.spring;

import javax.inject.Inject;

import org.granite.test.tide.data.Job;
import org.granite.test.tide.data.JobApplication;
import org.granite.test.tide.data.Meeting;
import org.granite.test.tide.spring.service.JobService;
import org.granite.tide.data.DataContext;
import org.granite.tide.invocation.InvocationResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class AbstractTestTideDataPublish extends AbstractTideTestCase {

	@Inject
	private JobService jobService;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		jobService.init();
		initGravity();
		DataContext.remove();
	}
    
	@Test
    public void testPublish1() {
		Object[] result = jobService.apply(1L, 1L);
		
        Assert.assertNull("Thread cleaned up", DataContext.get());
        
		Assert.assertNotNull(getLastMessage());
		Object[] updates = (Object[])getLastMessage().getBody();
		Assert.assertTrue("Updates count", updates.length >= 1);
		JobApplication application = (JobApplication)((Object[])updates[0])[1];
		// For some reason, 2 updates with JPA (PERSIST JobApplication + UPDATE Job)
		
		Job job = (Job)result[0];
		
		Assert.assertEquals("Job version", job.getVersion(), application.getJob().getVersion());
    }
    
	@Test
    public void testPublish2() {
		jobService.createMeeting(1L, 1L);
		
        Assert.assertNull("Thread cleaned up", DataContext.get());
        
		Assert.assertNotNull(getLastMessage());
		Object[] updates = (Object[])getLastMessage().getBody();
		
		// Different behaviours for H3.x/4.0/4.1/4.2 and H4.3+
		if (getClass().getSimpleName().indexOf("Hibernate3") >= 0)
			Assert.assertEquals("Updates count", 3, updates.length);
		else
			Assert.assertEquals("Updates count", 1, updates.length);
		Assert.assertTrue("Meeting", ((Object[])updates[0])[1] instanceof Meeting);
    }
    
	@Test
    public void testPublish3() {
        InvocationResult result = invokeComponent(null, JobService.class, "newMeeting", new Object[] { 1, 1 });
        
        Assert.assertNull("Thread cleaned up", DataContext.get());
        
        Object[][] resultUpdates = result.getUpdates();
        
        Assert.assertNotNull("Result updates", resultUpdates);
		// Different behaviours for H3.x/4.0/4.1/4.2 and H4.3+
		if (getClass().getSimpleName().indexOf("Hibernate3") >= 0)
			Assert.assertEquals("Result updates count", 3, resultUpdates.length);
		else
			Assert.assertEquals("Result updates count", 1, resultUpdates.length);
		Assert.assertTrue("Meeting", resultUpdates[0][1] instanceof Meeting);
        
		Assert.assertNotNull(getLastMessage());
		Object[] publishedUpdates = (Object[])getLastMessage().getBody();
		
		// Different behaviours for H3.x/4.0/4.1/4.2 and H4.3+
		if (getClass().getSimpleName().indexOf("Hibernate3") >= 0)
			Assert.assertEquals("Published updates count", 3, publishedUpdates.length);
		else
			Assert.assertEquals("Published updates count", 1, publishedUpdates.length);
		Assert.assertTrue("Meeting", ((Object[])publishedUpdates[0])[1] instanceof Meeting);
    }
}
