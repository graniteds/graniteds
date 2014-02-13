package org.granite.util;

/**
 * Created by william on 13/02/14.
 */
public interface ThrowableCallable<V> {

    V call() throws Throwable;
}
