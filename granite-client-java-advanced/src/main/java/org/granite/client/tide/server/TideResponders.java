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
package org.granite.client.tide.server;

/**
 * Convenience classes to build Tide responders
 *
 * @author William DRAI
 */
public class TideResponders {
	
	private static class EmptyTideResponder<T> implements TideMergeResponder<T> {
		
		private final T mergeWith;
		
		public EmptyTideResponder(T mergeWith) {
			this.mergeWith = mergeWith;
		}
	
		@Override
		public final void result(TideResultEvent<T> event) {
			// Do nothing
		}
		
		@Override
		public final void fault(TideFaultEvent event) {
			// Do nothing
		}
		
		@Override
		public T getMergeResultWith() {
			return mergeWith;
		}
	}

    /**
     * Create an empty responder which does not implement any operation
     * @param <T> expected result type
     * @return a new responder
     */
	public static <T> TideResponder<T> noop() {
		return new EmptyTideResponder<T>(null);
	}

    /**
     * Create an empty responder which forces the merge of the result with an existing object
     * @param mergeWith object to merge the result with
     * @param <T> expected result type
     * @return a new responder
     */
	public static <T> TideMergeResponder<T> mergeWith(T mergeWith) {
		return new EmptyTideResponder<T>(mergeWith);
	}

}
