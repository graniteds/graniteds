package org.granite.context;

public abstract class GraniteManager {

    private static ThreadLocal<GraniteContext> instance = new ThreadLocal<GraniteContext>() {
        @Override
        protected GraniteContext initialValue() {
            return (null);
        }
    };

    public static GraniteContext getCurrentInstance() {
        return instance.get();
    }

    public static void setCurrentInstance(GraniteContext context) {
        instance.set(context);
    }

    public static void release() {
        instance.set(null);
    }
}
