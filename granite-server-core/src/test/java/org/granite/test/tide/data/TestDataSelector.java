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
package org.granite.test.tide.data;

import java.util.ArrayList;
import java.util.List;

import org.granite.tide.data.DataObserveParams;
import org.granite.tide.data.DataPublishParams;
import org.granite.tide.data.DataTopicParams;
import org.junit.Assert;
import org.junit.Test;


public class TestDataSelector {
	
	@Test
	public void testSelector1() {
		DataObserveParams observe = new DataObserveParams();
		CompositePolicy policy1 = new CompositePolicy("someSite", "someUser");
		policy1.observes(observe);
		
		String selector = observe.updateDataSelector(null, new ArrayList<DataObserveParams>());
		
		Assert.assertEquals("type = 'DATA' AND ((site = 'someSite' AND user = 'someUser'))", selector);
	}
	
	@Test
	public void testSelector2() {
		List<DataObserveParams> selectors = new ArrayList<DataObserveParams>();
		
		DataObserveParams observe1 = new DataObserveParams();
		SitePolicy policy1 = new SitePolicy();
		policy1.observes(observe1);

		String selector = observe1.updateDataSelector(null, selectors);

		DataObserveParams observe2 = new DataObserveParams();
		UserPolicy policy2 = new UserPolicy("someUser");
		policy2.observes(observe2);
		
		selector = observe2.updateDataSelector(selector, selectors);
		
		Assert.assertEquals("type = 'DATA' AND ((site = 'someSite') OR (user = 'someUser'))", selector);
	}
	
	@Test
	public void testSelector3() {
		List<DataObserveParams> selectors = new ArrayList<DataObserveParams>();
		
		DataObserveParams observe1 = new DataObserveParams();
		CompositePolicy policy1 = new CompositePolicy("someSite", "someUser");
		policy1.observes(observe1);

		String selector = observe1.updateDataSelector(null, selectors);

		DataObserveParams observe2 = new DataObserveParams();
		UserPolicy policy2 = new UserPolicy("someUser");
		policy2.observes(observe2);
		
		selector = observe2.updateDataSelector(selector, selectors);
		
		Assert.assertEquals("type = 'DATA' AND ((site = 'someSite' AND user = 'someUser') OR (user = 'someUser'))", selector);
	}
	
	@Test
	public void testSelector4() {
		List<DataObserveParams> selectors = new ArrayList<DataObserveParams>();
		
		DataObserveParams observe1 = new DataObserveParams();
		CompositePolicy policy1 = new CompositePolicy("someSite", "someUser");
		policy1.observes(observe1);

		String selector = observe1.updateDataSelector(null, selectors);

		DataObserveParams observe2 = new DataObserveParams();
		UserPolicy policy2 = new UserPolicy("someUser2");
		policy2.observes(observe2);
		
		selector = observe2.updateDataSelector(selector, selectors);
		
		Assert.assertEquals("type = 'DATA' AND ((site = 'someSite' AND user = 'someUser') OR (user = 'someUser2'))", selector);
	}
	
	@Test
	public void testSelector5() {
		List<DataObserveParams> selectors = new ArrayList<DataObserveParams>();
		
		DataObserveParams observe1 = new DataObserveParams();
		CompositePolicy policy1 = new CompositePolicy("someSite", "someUser1");
		policy1.observes(observe1);

		String selector = observe1.updateDataSelector(null, selectors);
		
		DataObserveParams observe2 = new DataObserveParams();
		CompositePolicy policy2 = new CompositePolicy("someSite", "someUser2");
		policy2.observes(observe2);
		
		selector = observe2.updateDataSelector(selector, selectors);
		
		Assert.assertEquals("type = 'DATA' AND ((site = 'someSite' AND user = 'someUser1') OR (site = 'someSite' AND user = 'someUser2'))", selector);
	}
	
	@Test
	public void testSelector6() {
		List<DataObserveParams> selectors = new ArrayList<DataObserveParams>();
		
		DataObserveParams observe1 = new DataObserveParams();
		CompositePolicy policy1 = new CompositePolicy("someSite", "someUser");
		policy1.observes(observe1);

		String selector = observe1.updateDataSelector(null, selectors);
		
		DataObserveParams observe2 = new DataObserveParams();
		CompositePolicy policy2 = new CompositePolicy("someSite", "someUser");
		policy2.observes(observe2);
		
		selector = observe2.updateDataSelector(selector, selectors);
		
		Assert.assertEquals("type = 'DATA' AND ((site = 'someSite' AND user = 'someUser'))", selector);
	}
	
	@Test
	public void testSelector7() {
		List<DataObserveParams> selectors = new ArrayList<DataObserveParams>();
		
		DataObserveParams observe1 = new DataObserveParams();
		CompositePolicy policy1 = new CompositePolicy("someSite", "someUser1");
		policy1.observes(observe1);

		String selector = observe1.updateDataSelector(null, selectors);
		
		DataObserveParams observe2 = new DataObserveParams();
		CompositePolicy policy2 = new CompositePolicy("someSite", "someUser1", "someUser2");
		policy2.observes(observe2);
		
		selector = observe2.updateDataSelector(selector, selectors);
		
		Assert.assertEquals("type = 'DATA' AND ((site = 'someSite' AND user IN ('someUser1', 'someUser2')))", selector);
	}
	
	@Test
	public void testSelector8() {
		List<DataObserveParams> selectors = new ArrayList<DataObserveParams>();
		
		DataObserveParams observe1 = new DataObserveParams();
		CustomPolicy policy1 = new CustomPolicy("someSite", "someUser");
		policy1.observes(observe1);

		String selector = observe1.updateDataSelector(null, selectors);
		
		Assert.assertEquals("type = 'DATA' AND (((site = 'someSite' OR user = 'someUser')))", selector);
		
		DataObserveParams observe2 = new DataObserveParams();
		CompositePolicy policy2 = new CompositePolicy("someSite2", "someUser2");
		policy2.observes(observe2);
		
		selector = observe2.updateDataSelector(selector, selectors);
		
		Assert.assertEquals("type = 'DATA' AND (((site = 'someSite' OR user = 'someUser')) OR (site = 'someSite2' AND user = 'someUser2'))", selector);
	}
	
	@Test
	public void testSelector9() {
		List<DataObserveParams> selectors = new ArrayList<DataObserveParams>();
		
		DataObserveParams observe1 = new DataObserveParams();
		CustomPolicy policy1 = new CustomPolicy("someSite", "someUser");
		policy1.observes(observe1);

		String selector = observe1.updateDataSelector(null, selectors);
		
		Assert.assertEquals("type = 'DATA' AND (((site = 'someSite' OR user = 'someUser')))", selector);
		
		DataObserveParams observe2 = new DataObserveParams();
		CustomPolicy policy2 = new CustomPolicy("someSite", "someUser");
		policy2.observes(observe2);
		
		selector = observe2.updateDataSelector(selector, selectors);
		
		Assert.assertEquals("type = 'DATA' AND (((site = 'someSite' OR user = 'someUser')))", selector);
		
		DataObserveParams observe3 = new DataObserveParams();
		CustomPolicy policy3 = new CustomPolicy("someSite2", "someUser2");
		policy3.observes(observe3);
		
		selector = observe3.updateDataSelector(selector, selectors);
		
		Assert.assertEquals("type = 'DATA' AND (((site = 'someSite' OR user = 'someUser')) OR ((site = 'someSite2' OR user = 'someUser2')))", selector);
	}
	
	public static class CompositePolicy implements DataTopicParams {
		
		private String site;
		private String[] user;
		
		public CompositePolicy(String site, String... user) {
			this.site = site;
			this.user = user;
		}

		@Override
		public void observes(DataObserveParams params) {
			params.addValue("site", site);
			for (String user : this.user)
				params.addValue("user", user);
		}

		@Override
		public void publishes(DataPublishParams params, Object entity) {
		}
		
	}
	
	public static class SitePolicy implements DataTopicParams {

		@Override
		public void observes(DataObserveParams params) {
			params.addValue("site", "someSite");
		}

		@Override
		public void publishes(DataPublishParams params, Object entity) {
		}		
	}
	
	public static class UserPolicy implements DataTopicParams {
		
		private String user;
		
		public UserPolicy(String user) {
			this.user = user;
		}

		@Override
		public void observes(DataObserveParams params) {
			params.addValue("user", user);
		}

		@Override
		public void publishes(DataPublishParams params, Object entity) {
		}
		
	}
	
	public static class CustomPolicy implements DataTopicParams {
		
		private String site;
		private String user;
		
		public CustomPolicy(String site, String user) {
			this.site = site;
			this.user = user;
		}

		@Override
		public void observes(DataObserveParams params) {
			params.setSelector("site = '" + site + "' OR user = '" + user + "'");
		}

		@Override
		public void publishes(DataPublishParams params, Object entity) {
		}
		
	}

}
