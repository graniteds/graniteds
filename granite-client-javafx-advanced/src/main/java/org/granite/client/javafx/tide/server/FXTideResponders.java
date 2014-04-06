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
package org.granite.client.javafx.tide.server;

import javafx.beans.property.Property;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideMergeResponder;
import org.granite.client.tide.server.TideResultEvent;

/**
 * Convenience classes to build Tide responders for use with JavaFX applications
 */
public class FXTideResponders {

    public static class FXTideResponder<T> implements TideMergeResponder<T> {

        private final Property<T> mergeWith;

        public FXTideResponder(Property<T> mergeWith) {
            this.mergeWith = mergeWith;
        }

        @Override
        public final void result(TideResultEvent<T> event) {
            mergeWith.setValue(event.getResult());
        }

        @Override
        public final void fault(TideFaultEvent event) {
            // Do nothing
        }

        @Override
        public T getMergeResultWith() {
            return mergeWith.getValue();
        }
    }

    /**
     * Create an empty responder which forces the merge of the result with the value of a property
     * @param mergeWith property to merge the result with
     * @param <T> expected result type
     * @return a new responder
     */
    public static <T> TideMergeResponder<T> mergeWith(Property<T> mergeWith) {
        return new FXTideResponder<T>(mergeWith);
    }

}
