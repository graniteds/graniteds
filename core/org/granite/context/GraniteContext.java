/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.context;

import java.util.Map;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;

/**
 * @author Franck WOLFF
 */
public abstract class GraniteContext {

    private static ThreadLocal<GraniteContext> instance = new ThreadLocal<GraniteContext>() {
        @Override
        protected GraniteContext initialValue() {
            return (null);
        }
    };

    private final GraniteConfig graniteConfig;
    private final ServicesConfig servicesConfig;
    private final AMFContext amfContext;
    private final String sessionId;

    public GraniteContext(GraniteConfig graniteConfig, ServicesConfig servicesConfig, String sessionId) {
        this.servicesConfig = servicesConfig;
        this.graniteConfig = graniteConfig;
        this.amfContext = new AMFContextImpl();
        this.sessionId = sessionId;
    }

    public static GraniteContext getCurrentInstance() {
        return instance.get();
    }

    protected static void setCurrentInstance(GraniteContext context) {
        instance.set(context);
    }

    public static void release() {
        instance.set(null);
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public GraniteConfig getGraniteConfig() {
        return graniteConfig;
    }

    public AMFContext getAMFContext() {
        return amfContext;
    }
    
    public String getSessionId() {
    	return sessionId;
    }
    
    public abstract Object getSessionLock();

    public abstract Map<String, String> getInitialisationMap();
    public abstract Map<String, Object> getApplicationMap();
    public abstract Map<String, Object> getSessionMap();
    public abstract Map<String, Object> getSessionMap(boolean create);
    public abstract Map<String, Object> getRequestMap();
}
