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
package org.granite.config.flex;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.config.api.Configuration;
import org.granite.logging.Logger;
import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.messaging.service.security.RemotingDestinationSecurizer;
import org.granite.scan.ScannedItem;
import org.granite.scan.ScannedItemHandler;
import org.granite.scan.Scanner;
import org.granite.scan.ScannerFactory;
import org.granite.util.TypeUtil;
import org.granite.util.XMap;
import org.xml.sax.SAXException;

import flex.messaging.messages.RemotingMessage;


/**
 * @author Franck WOLFF
 */
public class ServicesConfig implements ChannelConfig, ScannedItemHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    private static final Logger log = Logger.getLogger(ServicesConfig.class);
    private static final String SERVICES_CONFIG_PROPERTIES = "META-INF/services-config.properties";

    private final Map<String, Service> services = new HashMap<String, Service>();
    private final Map<String, Channel> channels = new HashMap<String, Channel>();
    private final Map<String, Factory> factories = new HashMap<String, Factory>();

    
    ///////////////////////////////////////////////////////////////////////////
    // Classpath scan initialization.
    
    private void scanConfig(String serviceConfigProperties, List<ScannedItemHandler> handlers) {
        Scanner scanner = ScannerFactory.createScanner(this, serviceConfigProperties != null ? serviceConfigProperties : SERVICES_CONFIG_PROPERTIES);
        scanner.addHandlers(handlers);
        try {
            scanner.scan();
        } catch (Exception e) {
            log.error(e, "Could not scan classpath for configuration");
        }
    }

    public boolean handleMarkerItem(ScannedItem item) {
    	return false;
    }

    public void handleScannedItem(ScannedItem item) {
        if ("class".equals(item.getExtension()) && item.getName().indexOf('$') == -1) {
            try {
                handleClass(item.loadAsClass());
            } catch (Throwable t) {
                log.error(t, "Could not load class: %s", item);
            }
        }
    }

    public void handleClass(Class<?> clazz) {
        RemoteDestination anno = clazz.getAnnotation(RemoteDestination.class); 
        if (anno != null && !("".equals(anno.id()))) {
            XMap props = new XMap("properties");

            // Owning service.
            Service service = null;
            if (anno.service().length() > 0) {
            	log.info("Configuring service from RemoteDestination annotation: service=%s (class=%s, anno=%s)", anno.service(), clazz, anno);
                service = this.services.get(anno.service());
            }
            else if (this.services.size() > 0) {
                // Lookup remoting service
            	log.info("Looking for service(s) with RemotingMessage type (class=%s, anno=%s)", clazz, anno);
                int count = 0;
                for (Service s : this.services.values()) {
                    if (RemotingMessage.class.getName().equals(s.getMessageTypes())) {
                    	log.info("Found service with RemotingMessage type: service=%s (class=%s, anno=%s)", s, clazz, anno);
                        service = s;
                        count++;
                    }
                }
                if (count == 1 && service != null)
                    log.info("Service " + service.getId() + " selected for destination in class: " + clazz.getName());
                else {
                	log.error("Found %d matching services (should be exactly 1, class=%s, anno=%s)", count, clazz, anno);
                	service = null;
                }
            }
            if (service == null)
                throw new RuntimeException("No service found for destination in class: " + clazz.getName());
            
            // Channel reference.
            List<String> channelIds = null;
            if (anno.channels().length > 0)
                channelIds = Arrays.asList(anno.channels());
            else if (anno.channel().length() > 0)
                channelIds = Collections.singletonList(anno.channel());
            else if (this.channels.size() == 1) {
                channelIds = new ArrayList<String>(this.channels.keySet());
                log.info("Channel " + channelIds.get(0) + " selected for destination in class: " + clazz.getName());
            }
            else {
                log.warn("No (or ambiguous) channel definition found for destination in class: " + clazz.getName());
                channelIds = Collections.emptyList();
            }
            
            // Factory reference.
            String factoryId = null;
            if (anno.factory().length() > 0)
                factoryId = anno.factory();
            else if (this.factories.isEmpty()) {
                props.put("scope", anno.scope());
                props.put("source", clazz.getName());
                log.info("Default POJO factory selected for destination in class: " + clazz.getName() + " with scope: " + anno.scope());
            }
            else if (this.factories.size() == 1) {
                factoryId = this.factories.keySet().iterator().next();
                log.info("Factory " + factoryId + " selected for destination in class: " + clazz.getName());
            }
            else
                throw new RuntimeException("No (or ambiguous) factory definition found for destination in class: " + clazz.getName());
            
            if (factoryId != null)
            	props.put("factory", factoryId);
            if (!(anno.source().equals("")))
                props.put("source", anno.source());
            
            // Security roles.
            List<String> roles = null;
            if (anno.securityRoles().length > 0) {
            	roles = new ArrayList<String>(anno.securityRoles().length);
            	for (String role : anno.securityRoles())
            		roles.add(role);
            }
            
            // Securizer
            if (anno.securizer() != RemotingDestinationSecurizer.class)
            	props.put("securizer", anno.securizer().getName());
            
            Destination destination = new Destination(anno.id(), channelIds, props, roles, null, clazz);
            
            service.getDestinations().put(destination.getId(), destination);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Static ServicesConfig loaders.
    
    public ServicesConfig(InputStream customConfigIs, Configuration configuration, boolean scan) throws IOException, SAXException {
    	if (customConfigIs != null)
    		loadConfig(customConfigIs);
        
        if (scan)
        	scan(configuration);
    }
    
    public void scan(Configuration configuration) {
    	List<ScannedItemHandler> handlers = new ArrayList<ScannedItemHandler>();
    	for (Factory factory : factories.values()) {
    		try {
    			Class<?> clazz = TypeUtil.forName(factory.getClassName());
    			Method method = clazz.getMethod("getScannedItemHandler");
    			if ((Modifier.STATIC & method.getModifiers()) != 0 && method.getParameterTypes().length == 0) {
    				ScannedItemHandler handler = (ScannedItemHandler)method.invoke(null);
    				handlers.add(handler);
    			}
    			else
    				log.warn("Factory class %s contains an unexpected signature for method: %s", factory.getClassName(), method);
    		}
    		catch (NoSuchMethodException e) {
    			// ignore
    		}
    		catch (ClassNotFoundException e) {
    			log.error(e, "Could not load factory class: %s", factory.getClassName());
    		}
    		catch (Exception e) {
    			log.error(e, "Error while calling %s.getScannedItemHandler() method", factory.getClassName());
    		}
    	}
        scanConfig(configuration != null ? configuration.getFlexServicesConfigProperties() : null, handlers);
    }

    private void loadConfig(InputStream configIs) throws IOException, SAXException {
    	XMap doc = new XMap(configIs);
        forElement(doc);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Services.

    public Service findServiceById(String id) {
        return services.get(id);
    }

    public List<Service> findServicesByMessageType(String messageType) {
        List<Service> services = new ArrayList<Service>();
        for (Service service : this.services.values()) {
            if (messageType.equals(service.getMessageTypes()))
                services.add(service);
        }
        return services;
    }

    public void addService(Service service) {
        services.put(service.getId(), service);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Channels.

    public Channel findChannelById(String id) {
        return channels.get(id);
    }

    public void addChannel(Channel channel) {
        channels.put(channel.getId(), channel);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Factories.

    public Factory findFactoryById(String id) {
        return factories.get(id);
    }

    public void addFactory(Factory factory) {
        factories.put(factory.getId(), factory);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Destinations.

    public Destination findDestinationById(String messageType, String id) {
        for (Service service : services.values()) {
            if (messageType == null || messageType.equals(service.getMessageTypes())) {
                Destination destination = service.findDestinationById(id);
                if (destination != null)
                    return destination;
            }
        }
        return null;
    }

    public List<Destination> findDestinationsByMessageType(String messageType) {
        List<Destination> destinations = new ArrayList<Destination>();
        for (Service service : services.values()) {
            if (messageType.equals(service.getMessageTypes()))
                destinations.addAll(service.getDestinations().values());
        }
        return destinations;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Static helper.

    private void forElement(XMap element) {
        XMap services = element.getOne("services");
        if (services != null) {
            for (XMap service : services.getAll("service")) {
                Service serv = Service.forElement(service);
                this.services.put(serv.getId(), serv);
            }

            /* TODO: service-include...
            for (Element service : (List<Element>)services.getChildren("service-include")) {
                config.services.add(Service.forElement(service));
            }
            */
        }

        XMap channels = element.getOne("channels");
        if (channels != null) {
            for (XMap channel : channels.getAll("channel-definition")) {
                Channel chan = Channel.forElement(channel);
                this.channels.put(chan.getId(), chan);
            }
        }
        else {
            log.info("No channel definition found, using defaults");
            EndPoint defaultEndpoint = new EndPoint("http://{server.name}:{server.port}/{context.root}/graniteamf/amf", "flex.messaging.endpoints.AMFEndpoint");
            Channel defaultChannel = new Channel("my-graniteamf", "mx.messaging.channels.AMFChannel", defaultEndpoint, XMap.EMPTY_XMAP);
            this.channels.put(defaultChannel.getId(), defaultChannel);
        }

        XMap factories = element.getOne("factories");
        if (factories != null) {
            for (XMap factory : factories.getAll("factory")) {
                Factory fact = Factory.forElement(factory);
                this.factories.put(fact.getId(), fact);
            }
        }
    }
    
    
    /**
     * Remove service (new addings for osgi).
     * @param clazz service class.
     */
    public void handleRemoveService(Class<?> clazz){
    	 RemoteDestination anno = clazz.getAnnotation(RemoteDestination.class);
    	 if(anno!=null){
    		 Service service=null;
     		 if (anno.service().length() > 0){
    			  service=services.get(anno.service());
    		 }
     		 else if (services.size() > 0) {
                // Lookup remoting service
                for (Service s :  services.values()) {
                    if (RemotingMessage.class.getName().equals(s.getMessageTypes())) {
                        service = s;
                        log.info("Service " + service.getId() + " selected for destination in class: " + clazz.getName());
                        break;
                    }
                }
            }
     		if(service!=null){
     			Destination dest=service.getDestinations().remove(anno.id());
     			if (dest != null) {
     				dest.remove();
     				log.info("RemoteDestination:"+dest.getId()+" has been removed");
     			}
     		}else{
     			log.info("Service NOT Found!!");
     		}
    	 }
    }

	@Override
	public boolean getChannelProperty(String channelId, String propertyName) {
		if (channelId == null)
			return false;
		Channel channel = findChannelById(channelId);
		if (channel == null) {
			log.debug("Could not get channel for channel id: %s", channelId);
			return false;
		}
		if ("legacyXmlSerialization".equals(propertyName))
			return channel.isLegacyXmlSerialization();
		else if ("legacyCollectionSerialization".equals(propertyName))
			return channel.isLegacyCollectionSerialization();
		return false;
	}
    
}
