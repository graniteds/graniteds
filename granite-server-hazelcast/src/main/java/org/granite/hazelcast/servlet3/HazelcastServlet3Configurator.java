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
