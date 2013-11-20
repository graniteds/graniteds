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
