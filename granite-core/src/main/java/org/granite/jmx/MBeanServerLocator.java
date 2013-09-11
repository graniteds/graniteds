/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.jmx;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

/**
 * Utility class for MBean server lookup (with a specific support for the
 * JBoss JMX console) and MBeans registration. 
 * 
 * @author Franck WOLFF
 */
public class MBeanServerLocator {

	private static MBeanServerLocator instance = null;
	
	private final MBeanServer server;
	
	private MBeanServerLocator() {
		this.server = findMBeanServer();
	}
	
	private static MBeanServer findMBeanServer() {
		
		// Initialize with default platform MBeanServer: must be called first
		// otherwise jconsole don't work...
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		// Try to find main JBoss MBeanServer.
		for (Object found : MBeanServerFactory.findMBeanServer(null)) {
			if (found instanceof MBeanServer && "jboss".equals(((MBeanServer)found).getDefaultDomain())) {
				server = (MBeanServer)found;
				break;
			}
		}
		
		return server;
	}
	
	/**
	 * Returns a singleton instance of the <tt>MBeanServerLocator</tt> class. The first call
	 * to this method performs an initial lookup of a {@link MBeanServer}.
	 * 
	 * @return a singleton instance of the MBeanServerLocator class.
	 */
	public static synchronized MBeanServerLocator getInstance() {
		if (instance == null)
			instance = new MBeanServerLocator();
		return instance;
	}
	
	/**
	 * Returns the {@link MBeanServer} wrapped by this <tt>MBeanServerLocator</tt> instance.
	 * 
	 * @return the wrapped {@link MBeanServer}.
	 */
	public MBeanServer getMBeanServer() {
		return server;
	}
	
	/**
	 * Register the <tt>mbean</tt> object with the supplied <tt>name</tt>. Calling this method is
	 * equivalent to calling {@link #register(Object, ObjectName, boolean)} with its
	 * last parameter set to <tt>false</tt>.
	 * 
	 * @param mbean the mbean to register.
	 * @param name the name used for registration.
	 * @throws MBeanRegistrationException rethrown from the wrapped {@link MBeanServer}
	 * @throws InstanceNotFoundException rethrown from the wrapped {@link MBeanServer}
	 * @throws InstanceAlreadyExistsException rethrown from the wrapped {@link MBeanServer}
	 * @throws NotCompliantMBeanException rethrown from the wrapped {@link MBeanServer}
	 */
	public void register(Object mbean, ObjectName name)
		throws MBeanRegistrationException, InstanceNotFoundException,
		       InstanceAlreadyExistsException, NotCompliantMBeanException {
		
		register(mbean, name, false);
	}
	
	/**
	 * Register the <tt>mbean</tt> object with the supplied <tt>name</tt>. If the
	 * <tt>replace</tt> parameter is set to true and if a MBean is already registered
	 * under the same name, it is first unregistered.
	 * 
	 * @param mbean the mbean to register.
	 * @param name the name used for registration.
	 * @param replace if true, a mbean registered under the same name will be first
	 * 		unregistered.
	 * @throws MBeanRegistrationException rethrown from the wrapped {@link MBeanServer}
	 * @throws InstanceNotFoundException rethrown from the wrapped {@link MBeanServer}
	 * @throws InstanceAlreadyExistsException rethrown from the wrapped {@link MBeanServer}
	 * @throws NotCompliantMBeanException rethrown from the wrapped {@link MBeanServer}
	 */
	public void register(Object mbean, ObjectName name, boolean replace)
		throws MBeanRegistrationException, InstanceNotFoundException,
			   InstanceAlreadyExistsException, NotCompliantMBeanException {
		
		if (server != null) {
            if (replace && server.isRegistered(name))
            	server.unregisterMBean(name);
			server.registerMBean(mbean, name);
		}
	}
	
	/**
	 * Returns <tt>true</tt> if a MBean is registered under the supplied <tt>name</tt>.
	 * 
	 * @param name the name to test for registration.
	 * @return true if a mbean is registered, false otherwise.
	 */
	public boolean isRegistered(ObjectName name) {
		return server != null && server.isRegistered(name);
	}
	
	/**
	 * Unregister any mbean registered under the supplied <tt>name</tt>.
	 * 
	 * @param name the name of mbean to unregister.
	 * @throws InstanceNotFoundException rethrown from the wrapped {@link MBeanServer}
	 * @throws MBeanRegistrationException rethrown from the wrapped {@link MBeanServer}
	 */
	public void unregister(ObjectName name)
		throws InstanceNotFoundException, MBeanRegistrationException {
		
		if (server != null)
			server.unregisterMBean(name);
	}
}
