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
package org.granite.hazelcast.servlet3;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.granite.config.ConfigProvider;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.servlet3.GraniteServlet3Initializer;
import org.granite.config.servlet3.Servlet3Configurator;
import org.granite.hazelcast.AbstractHazelcastDestination;

public class HazelcastServlet3Configurator implements Servlet3Configurator {
	
	public boolean handleField(Field field, ConfigProvider configProvider, ServicesConfig servicesConfig) {
		if (!field.isAnnotationPresent(HazelcastDestination.class))
			return false;
		
		HazelcastDestination hd = field.getAnnotation(HazelcastDestination.class);
		AbstractHazelcastDestination messagingDestination = new AbstractHazelcastDestination();
		messagingDestination.setId(field.getName());
		messagingDestination.setInstanceName(hd.instanceName());
		messagingDestination.setNoLocal(hd.noLocal());
		messagingDestination.setSessionSelector(hd.sessionSelector());
        if (hd.securityRoles().length > 0)
            messagingDestination.setRoles(Arrays.asList(hd.securityRoles()));
        GraniteServlet3Initializer.initSecurizer(messagingDestination, hd.securizer(), configProvider);
		messagingDestination.initServices(servicesConfig);
		return true;
	}

}
