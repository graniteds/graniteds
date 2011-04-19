package org.granite.context;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.messaging.service.MainFactory;

import java.util.Map;

public interface GraniteContext {
    public ServicesConfig getServicesConfig();

    public GraniteConfig getGraniteConfig();

    public AMFContext getAMFContext();

    public Class<?> forName(String type) throws ClassNotFoundException;

    public MainFactory getMainFactory();

    public Object getSessionLock();

    public Map<String, String> getInitialisationMap();

    public Map<String, Object> getApplicationMap();

    public Map<String, Object> getSessionMap();

    public Map<String, Object> getSessionMap(boolean create);

    public Map<String, Object> getRequestMap();
}
