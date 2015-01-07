/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.context;

import java.util.Map;

/**
 * @author William DRAI
 */
public class SimpleGraniteContext extends GraniteContext {

    private Map<String, Object> applicationMap;


    public static SimpleGraniteContext createThreadInstance(
    	Object graniteConfig,
    	Object servicesConfig,
        Map<String, Object> applicationMap) {
    	
    	return createThreadInstance(graniteConfig, servicesConfig, null, applicationMap, null);
    }
    
    public static SimpleGraniteContext createThreadInstance(
    	Object graniteConfig,
    	Object servicesConfig,
        Map<String, Object> applicationMap,
        String clientType) {
    	
    	return createThreadInstance(graniteConfig, servicesConfig, null, applicationMap, clientType);
    }
    
    public static SimpleGraniteContext createThreadInstance(
    	Object graniteConfig,
    	Object servicesConfig,
        String sessionId,
        Map<String, Object> applicationMap,
        String clientType) {

        SimpleGraniteContext graniteContext = new SimpleGraniteContext(graniteConfig, servicesConfig, sessionId, applicationMap, clientType);
        setCurrentInstance(graniteContext);
        return graniteContext;
    }


    private SimpleGraniteContext(Object graniteConfig, Object servicesConfig, String sessionId, Map<String, Object> applicationMap, String clientType) {
        super(graniteConfig, servicesConfig, sessionId, clientType);
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
