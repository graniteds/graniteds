/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
