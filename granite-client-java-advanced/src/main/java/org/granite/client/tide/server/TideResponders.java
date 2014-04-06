/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
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
