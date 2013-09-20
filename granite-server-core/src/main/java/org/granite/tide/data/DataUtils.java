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
package org.granite.tide.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataUtils {
	
	public static List<Object[]> diffLists(List<?> oldList, List<?> newList) {
		ListDiff listDiff = new ListDiff(oldList, newList);
		listDiff.diff();
		return listDiff.getOps();
	}
	
	private static class ListDiff {
		
		private final List<?> oldList;
		private final List<?> newList;
		
		private int oldi = 0, newi = 0;
		
		private List<Object[]> ops = new ArrayList<Object[]>();
		
		private List<Integer> skipOld = new ArrayList<Integer>();
		private Map<Integer, Object[]> delayedNew = new HashMap<Integer, Object[]>();
		
		public ListDiff(List<?> oldList, List<?> newList) {
			this.oldList = oldList;
			this.newList = newList;
		}
		
		public List<Object[]> getOps() {
			return ops;
		}
		
		private void moveNew() {
			newi++;
			while (delayedNew.containsKey(newi)) {
				for (Object op : delayedNew.get(newi))
					ops.add((Object[])op);
				newi++;
			}
		}
		
		private int nextOld() {
			int i = oldi+1;
			while (skipOld.contains(i) && i < oldList.size())
				i++;
			return i;
		}
		
		private int nextNew() {
			return newi+1;
		}
		
		private int getIndex(int index) {
			for (Object[] op : ops) {
				if (op[0].equals(-1) && (Integer)op[1] <= index)
					index--;
				else if (op[0].equals(1) && (Integer)op[1] <= index)
					index++;
			}
			return index;
		}
		
		public void diff() {
			for (oldi = 0; oldi < oldList.size(); oldi++) {
				if (skipOld.contains(oldi))
					continue;
				
				// Same value for current indices on old and new : next and reset current offset
				if (oldi < oldList.size() && newi < newList.size() && oldList.get(oldi).equals(newList.get(newi))) {
					moveNew();
					continue;
				}
				
				// Lookup same element in new list
				int foundNext = -1;
				if (newi < newList.size()-1) {
					for (int i = newi+1; i < newList.size(); i++) {
						if (newList.get(i).equals(oldList.get(oldi)) && !delayedNew.containsKey(i)) {
							foundNext = i;
							break;
						}
					}
				}
				if (foundNext == -1) {
					int oi = nextOld();
					int ni = nextNew();
					if (oi < oldList.size() && ni < newList.size() && oldList.get(oi).equals(newList.get(ni))) {
						// Element not found in new list but next one matches: update
						ops.add(new Object[] { 0, getIndex(oldi), newList.get(newi) });
						moveNew();
					}
					else {
						// Element not found in new list: remove
						ops.add(new Object[] { -1, getIndex(oldi), oldList.get(oldi) });
					}
				}
//				else if (foundNext == newi+1 && oldi+1 < oldList.size() && oldList.get(oldi+1).equals(newList.get(newi))) {
//					// Immediate permutation
//					int index = getIndex(oldi);
//					ops.add(new Object[] { -1, index, oldList.get(oldi) });
//					ops.add(new Object[] { 1, index+1, oldList.get(oldi) });
//					nextNew();
//					nextNew();
//					oldi++;
//				}
//				else if (foundNext > newi+1 && oldi+foundNext-newi < oldList.size() && oldList.get(oldi+foundNext-newi).equals(newList.get(newi))) {
//					// Distant permutation
//					int index = getIndex(oldi);
//					Object p1 = oldList.get(oldi);
//					Object p2 = newList.get(newi);
//					ops.add(new Object[] { -1, index, p1 });
//					ops.add(new Object[] { 1, index, p2 });
//					skipOld.add(oldi+foundNext-newi);
//					nextNew();
//					delayedNew.put(foundNext, new Object[] { new Object[] { -1, foundNext, p2 }, new Object[] { 1, foundNext, p1 } });
//				}
				else if (oldi < oldList.size()-1 && oldList.get(oldi+1).equals(newList.get(newi))) {
					// Move of element to another position, remove here and schedule an add for final position
					ops.add(new Object[] { -1, getIndex(oldi), oldList.get(oldi) });
					delayedNew.put(foundNext, new Object[] { new Object[] { 1, foundNext, oldList.get(oldi) } });
				}
				else {
					// Add elements of new list from current index to found index
					while (newi < foundNext) {
						int foundOld = -1;
						// Lookup if the element is present later in the old list
						if (oldi < oldList.size()-1) {
							for (int i = oldi+1; i < oldList.size(); i++) {
								if (newList.get(newi).equals(oldList.get(i)) && !skipOld.contains(i)) {
									foundOld = i;
									break;
								}
							}
						}
						if (foundOld >= 0) {
							// Found later, push a remove
							ops.add(new Object[] { -1, getIndex(foundOld), oldList.get(foundOld) });
							skipOld.add(foundOld);
						}
						
						ops.add(new Object[] { 1, newi, newList.get(newi) });
						moveNew();
					}
					oldi--;
				}
			}
			// Add missing elements from new list
			while (newi < newList.size()) {
				ops.add(new Object[] { 1, newi, newList.get(newi) });
				moveNew();
			}
		}
	}
}
