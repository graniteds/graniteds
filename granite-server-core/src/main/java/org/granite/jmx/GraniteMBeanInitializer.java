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
package org.granite.jmx;

import javax.management.ObjectName;
import javax.servlet.ServletContext;

import org.granite.config.ServletGraniteConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.logging.Logger;

/**
 * An utility class that register/unregister GraniteDS MBeans.
 * 
 * @author Franck WOLFF
 */
public class GraniteMBeanInitializer {
	
	private static final Logger log = Logger.getLogger(GraniteMBeanInitializer.class);

	public static void registerMBeans(ServletContext context, ServletGraniteConfig gConfig, ServletServicesConfig sConfig) {
		String appName = ServletGraniteConfig.getConfig(context).getMBeanContextName();
        try {
            ObjectName name = new ObjectName("org.graniteds:type=GraniteDS,app=" + appName);
	        log.info("Registering MBean: %s", name);
            OpenMBean mBean = OpenMBean.createMBean(gConfig);
        	MBeanServerLocator.getInstance().register(mBean, name);
        }
        catch (Exception e) {
        	log.error(e, "Could not register GraniteDS MBean for context: %s", appName);
        }
	}	

	public static void unregisterMBeans(ServletContext context) {
		String appName = ServletGraniteConfig.getConfig(context).getMBeanContextName();
        try {
            ObjectName name = new ObjectName("org.graniteds:type=GraniteDS,app=" + appName);
	        log.info("Unregistering MBean: %s", name);
        	MBeanServerLocator.getInstance().unregister(name);
        }
        catch (Exception e) {
        	log.error(e, "Could not unregister GraniteDS MBean for context: %s", appName);
        }
	}	
}
