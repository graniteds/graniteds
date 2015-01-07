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
package org.granite.hazelcast;
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


import org.granite.config.AbstractFrameworkGraniteConfig;
import org.granite.config.flex.Adapter;
import org.granite.config.flex.Destination;
import org.granite.config.flex.ServicesConfig;
import org.granite.gravity.config.AbstractMessagingDestination;
import org.granite.util.XMap;


public class AbstractHazelcastDestination extends AbstractMessagingDestination {

    ///////////////////////////////////////////////////////////////////////////
    // Instance fields.
   
    private String instanceName = null;
    

	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
	
	protected void init(AbstractFrameworkGraniteConfig graniteConfig) {
    	ServicesConfig servicesConfig = graniteConfig.getServicesConfig();
    	initServices(servicesConfig);
    }
    
	protected Adapter buildAdapter() {
		return new Adapter("hazelcast-adapter", "org.granite.hazelcast.adapters.HazelcastServiceAdapter", new XMap());
	}
	
	protected Destination buildDestination(Adapter adapter) {
		Destination destination = super.buildDestination(adapter);
		destination.getProperties().put("hazelcast-instance-name", instanceName);
    	return destination;
	}
}
