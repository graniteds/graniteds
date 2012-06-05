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
 * @author William DRAI
 */
public class SimpleGraniteContext extends GraniteContext {

    private Map<String, Object> applicationMap;


    public static SimpleGraniteContext createThreadInstance(
        GraniteConfig graniteConfig,
        ServicesConfig servicesConfig,
        Map<String, Object> applicationMap) {

        SimpleGraniteContext graniteContext = new SimpleGraniteContext(graniteConfig, servicesConfig, applicationMap);
        setCurrentInstance(graniteContext);
        return graniteContext;
    }


    private SimpleGraniteContext(GraniteConfig graniteConfig, ServicesConfig servicesConfig, Map<String, Object> applicationMap) {
        super(graniteConfig, servicesConfig);
        this.applicationMap = applicationMap;
    }

    @Override
	public Object getSessionLock() {
		return null;
	}

	@Override
    public Map<String, String> getInitialisationMap() {
        return null;
    }
    @Override
    public Map<String, Object> getApplicationMap() {
        return applicationMap;
    }
    @Override
	public Map<String, Object> getSessionMap(boolean create) {
		return null;
	}
	@Override
    public Map<String, Object> getSessionMap() {
        return null;
    }
    @Override
    public Map<String, Object> getRequestMap() {
        return null;
    }
}
