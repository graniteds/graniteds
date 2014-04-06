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
import java.util.Random;

import junit.framework.Assert;

import org.granite.tide.data.Utils;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class TestDataUtils {
	
	@Test
	public void testDiffAdd1() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		
		newList.add(c1);
		newList.add(c2);
		newList.add(c3);
		newList.add(c4);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
		
		Assert.assertEquals("2 ops", 2, ops.size());
		Assert.assertEquals("Op 1 add", 1, ops.get(0)[0]);
		Assert.assertEquals("Op 1 idx", 2, ops.get(0)[1]);
		Assert.assertEquals("Op 2 add", 1, ops.get(1)[0]);
		Assert.assertEquals("Op 2 idx", 3, ops.get(1)[1]);
	}
	
	@Test
	public void testDiffAdd2() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		
		newList.add(c1);
		newList.add(c3);
		newList.add(c2);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
		
		Assert.assertEquals("1 op", 1, ops.size());
		Assert.assertEquals("Op 1 add", 1, ops.get(0)[0]);
		Assert.assertEquals("Op 1 idx", 1, ops.get(0)[1]);
	}
	
	@Test
	public void testDiffAdd3() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		
		newList.add(c1);
		newList.add(c2);
		newList.add(c3);
		newList.add(c3);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
		
		Assert.assertEquals("2 ops", 2, ops.size());
		Assert.assertEquals("Op 1 add", 1, ops.get(0)[0]);
		Assert.assertEquals("Op 1 idx", 2, ops.get(0)[1]);
		Assert.assertEquals("Op 2 add", 1, ops.get(1)[0]);
		Assert.assertEquals("Op 2 idx", 3, ops.get(1)[1]);
	}
	
	@Test
	public void testDiffAdd4() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		
		newList.add(c1);
		newList.add(c2);
		newList.add(c1);
		newList.add(c2);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffAdd5() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		
		newList.add(c2);
		newList.add(c1);
		newList.add(c2);
		newList.add(c1);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	
	@Test
	public void testDiffSub1() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		
		newList.add(c1);
		newList.add(c2);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
		
		Assert.assertEquals("2 ops", 1, ops.size());
		Assert.assertEquals("Op 1 rem", -1, ops.get(0)[0]);
		Assert.assertEquals("Op 1 idx", 2, ops.get(0)[1]);
	}
	
	@Test
	public void testDiffSub2() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c3);
		oldList.add(c2);
		
		newList.add(c1);
		newList.add(c2);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
		
		Assert.assertEquals("1 op", 1, ops.size());
		Assert.assertEquals("Op 1 rem", -1, ops.get(0)[0]);
		Assert.assertEquals("Op 1 idx", 1, ops.get(0)[1]);
	}
	
	@Test
	public void testDiffOrder() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c1);
		newList.add(c2);
		newList.add(c4);
		newList.add(c3);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrder2() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c2);
		newList.add(c3);
		newList.add(c1);
		newList.add(c4);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffRevert() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		
		newList.add(c3);
		newList.add(c2);
		newList.add(c1);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffRevert2() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c4);
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		
		newList.add(c3);
		newList.add(c2);
		newList.add(c1);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffDoubles() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c5);
		oldList.add(c3);
		oldList.add(c2);
		oldList.add(c1);
		oldList.add(c5);
		
		newList.add(c3);
		newList.add(c3);
		newList.add(c5);
		newList.add(c4);
		newList.add(c4);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrder3() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c2);
		newList.add(c3);
		newList.add(c1);
		newList.add(c4);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrder3b() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c3);
		newList.add(c1);
		newList.add(c2);
		newList.add(c4);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrder4() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c2);
		newList.add(c1);
		newList.add(c4);
		newList.add(c3);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffRevert3() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c4);
		newList.add(c3);
		newList.add(c2);
		newList.add(c1);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrder6() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c3);
		newList.add(c4);
		newList.add(c2);
		newList.add(c1);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrder7() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		
		newList.add(c3);
		newList.add(c2);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrder8() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		oldList.add(c5);
		oldList.add(c6);
		
		newList.add(c6);
		newList.add(c5);
		newList.add(c4);
		newList.add(c3);
		newList.add(c2);
		newList.add(c1);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrder8a() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		oldList.add(c5);
		
		newList.add(c5);
		newList.add(c4);
		newList.add(c3);
		newList.add(c2);
		newList.add(c1);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrder8b() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		Classification c7 = new Classification(null, null, "C7");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		oldList.add(c5);
		oldList.add(c6);
		
		newList.add(c7);
		newList.add(c6);
		newList.add(c5);
		newList.add(c4);
		newList.add(c3);
		newList.add(c2);
		newList.add(c1);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrder9() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		oldList.add(c5);
		oldList.add(c6);
		
		newList.add(c3);
		newList.add(c2);
		newList.add(c1);
		newList.add(c6);
		newList.add(c5);
		newList.add(c4);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderRem() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c3);
		newList.add(c5);
		newList.add(c1);
		newList.add(c6);
		newList.add(c4);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderRem2() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		Classification c7 = new Classification(null, null, "C7");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		oldList.add(c5);
		oldList.add(c6);
		
		newList.add(c3);
		newList.add(c5);
		newList.add(c1);
		newList.add(c7);
		newList.add(c4);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderRem2b() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		Classification c7 = new Classification(null, null, "C7");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		oldList.add(c5);
		oldList.add(c6);
		
		newList.add(c3);
		newList.add(c7);
		newList.add(c1);
		newList.add(c6);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderRem2c() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		Classification c7 = new Classification(null, null, "C7");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		oldList.add(c5);
		oldList.add(c6);
		
		newList.add(c4);
		newList.add(c5);
		newList.add(c6);
		newList.add(c1);
		newList.add(c3);
		newList.add(c7);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderRem2d() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		Classification c7 = new Classification(null, null, "C7");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		oldList.add(c5);
		oldList.add(c6);
		
		newList.add(c3);
		newList.add(c1);
		newList.add(c4);
		newList.add(c5);
		newList.add(c7);
		newList.add(c6);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderRem2e() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		Classification c7 = new Classification(null, null, "C7");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c2);
		oldList.add(c5);
		oldList.add(c6);
		newList.add(c4);
		
		newList.add(c3);
		newList.add(c7);
		newList.add(c4);
		newList.add(c5);
		newList.add(c6);
		newList.add(c7);
		newList.add(c7);
		newList.add(c6);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderRem3() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c3);
		newList.add(c5);
		newList.add(c1);
		newList.add(c6);
		newList.add(c2);
		newList.add(c4);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderRem4() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c3);
		newList.add(c5);
		newList.add(c2);
		newList.add(c6);
		newList.add(c6);
		newList.add(c4);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderAdd() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		
		newList.add(c1);
		newList.add(c3);
		newList.add(c2);
		newList.add(c4);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderAdd2() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c3);
		newList.add(c5);
		newList.add(c4);
		newList.add(c6);
		newList.add(c2);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderAdd3() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		Classification c7 = new Classification(null, null, "C7");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c3);
		newList.add(c5);
		newList.add(c4);
		newList.add(c6);
		newList.add(c2);
		newList.add(c1);
		newList.add(c7);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderAdd4() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		Classification c7 = new Classification(null, null, "C7");
		Classification c8 = new Classification(null, null, "C8");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c3);
		newList.add(c5);
		newList.add(c4);
		newList.add(c6);
		newList.add(c8);
		newList.add(c2);
		newList.add(c7);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderAdd9() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		Classification c7 = new Classification(null, null, "C7");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c1);
		newList.add(c5);
		newList.add(c4);
		newList.add(c2);
		newList.add(c6);
		newList.add(c7);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderAdd10() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		Classification c7 = new Classification(null, null, "C7");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		newList.add(c5);
		
		newList.add(c1);
		newList.add(c2);
		newList.add(c6);
		newList.add(c7);
		newList.add(c4);
		newList.add(c3);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffOrderAdd11() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		Classification c6 = new Classification(null, null, "C6");
		Classification c7 = new Classification(null, null, "C7");
		Classification c8 = new Classification(null, null, "C8");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		newList.add(c5);
		
		newList.add(c6);
		newList.add(c7);
		newList.add(c1);
		newList.add(c4);
		newList.add(c3);
		newList.add(c8);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffSet() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c1);
		newList.add(c5);
		newList.add(c3);
		newList.add(c4);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffSet2() {
		Classification c1 = new Classification(null, null, "C1");
		Classification c2 = new Classification(null, null, "C2");
		Classification c3 = new Classification(null, null, "C3");
		Classification c4 = new Classification(null, null, "C4");
		Classification c5 = new Classification(null, null, "C5");
		
		List<Classification> oldList = new ArrayList<Classification>();
		List<Classification> newList = new ArrayList<Classification>();
		
		oldList.add(c1);
		oldList.add(c2);
		oldList.add(c3);
		oldList.add(c4);
		
		newList.add(c1);
		newList.add(c3);
		newList.add(c3);
		newList.add(c4);
		newList.add(c5);
		
		List<Object[]> ops = Utils.diffLists(oldList, newList);
		
		checkListDiff(oldList, newList, ops);
	}
	
	@Test
	public void testDiffRandom() {
		Random random = new Random(System.currentTimeMillis());
		
		for (int count = 0; count < 1000; count++) {
			int oldSize = random.nextInt(200);
			int newSize = random.nextInt(200);
			
			int max = Math.max(oldSize,  newSize);
			Classification[] c = new Classification[max];
			for (int i = 0; i < max; i++)
				c[i] = new Classification(null, null, "C" + i);
			
			List<Classification> oldList = new ArrayList<Classification>(oldSize);
			List<Classification> newList = new ArrayList<Classification>(newSize);
			
			for (int i = 0; i < oldSize; i++)
				oldList.add(c[random.nextInt(oldSize)]);
			for (int i = 0; i < newSize; i++)
				newList.add(c[random.nextInt(newSize)]);
			
			List<Object[]> ops = Utils.diffLists(oldList, newList);
			
			checkListDiff(oldList, newList, ops);
		}
	}
	
	
	private void checkListDiff(List<Classification> oldList, List<Classification> newList, List<Object[]> ops) {
		StringBuilder sb = new StringBuilder();
		for (Classification c : oldList)
			sb.append(c.getUid()).append(" ");
		System.out.println("Initial : " + sb);
		sb = new StringBuilder();
		for (Classification c : newList)
			sb.append(c.getUid()).append(" ");
		System.out.println("Expected: " + sb);
		System.out.printf("%d operations: ", ops.size());
		for (Object[] op : ops)
			System.out.printf("%d %d %s  ", op[0], op[1], ((Classification)op[2]).getUid());
		System.out.println();
		
		List<Classification> checkList = new ArrayList<Classification>(oldList);
		for (Object[] op : ops) {
			if (op[0].equals(-1))
				checkList.remove(((Integer)op[1]).intValue());
			else if (op[0].equals(1))
				checkList.add(((Integer)op[1]).intValue(), (Classification)op[2]);
			else if (op[0].equals(0))
				checkList.set(((Integer)op[1]).intValue(), (Classification)op[2]);
		}
		
		sb = new StringBuilder();
		for (Classification c : checkList)
			sb.append(c.getUid()).append(" ");
		System.out.println("Result  : " + sb);
		
		Assert.assertEquals("Expected list size", newList.size(), checkList.size());
		for (int i = 0; i < checkList.size(); i++)
			Assert.assertEquals("Element " + i, newList.get(i).getUid(), checkList.get(i).getUid());
	}

}
