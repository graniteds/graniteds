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
package org.granite.spring;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.Channel;
import org.granite.config.flex.Destination;
import org.granite.config.flex.EndPoint;
import org.granite.config.flex.Factory;
import org.granite.config.flex.Service;
import org.granite.config.flex.ServicesConfig;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.util.externalizer.BigDecimalExternalizer;
import org.granite.messaging.amf.io.util.externalizer.BigIntegerExternalizer;
import org.granite.messaging.amf.io.util.externalizer.Externalizer;
import org.granite.messaging.amf.io.util.externalizer.LongExternalizer;
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.service.ExceptionConverter;
import org.granite.messaging.service.security.RemotingDestinationSecurizer;
import org.granite.messaging.service.security.SecurityService;
import org.granite.messaging.service.tide.TideComponentAnnotatedWithMatcher;
import org.granite.messaging.service.tide.TideComponentInstanceOfMatcher;
import org.granite.messaging.service.tide.TideComponentNameMatcher;
import org.granite.messaging.service.tide.TideComponentTypeMatcher;
import org.granite.messaging.webapp.AMFEndpoint;
import org.granite.util.TypeUtil;
import org.granite.util.XMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;


public class ServerFilter implements InitializingBean, DisposableBean, ApplicationContextAware, ServletContextAware, HandlerAdapter {
	
    private static final Logger log = Logger.getLogger(ServerFilter.class);
	
    @Autowired(required=false)
    private ServletContext servletContext = null;
    private ApplicationContext context = null;
    
    private GraniteConfig graniteConfig = null;
    private ServicesConfig servicesConfig = null;
    
    private List<String> tideRoles = null;
    private List<String> tideAnnotations = null;
    private List<String> tideInterfaces = null;
    private List<String> tideNames = null;
    private List<String> tideTypes = null;
    private List<Class<? extends ExceptionConverter>> exceptionConverters = null;
    private AMF3MessageInterceptor amf3MessageInterceptor;
    private boolean useLong = false;
    private boolean useBigInteger = false;
    private boolean useBigDecimal = false;
    private boolean enableExceptionLogging = true;
    private boolean tide = false;
    private String type = "server";
    
    private AMFEndpoint amfEndpoint = null;

	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}
    
	public void setServletContext(ServletContext servletContext) throws BeansException {
		this.servletContext = servletContext;
	}
    
	public void afterPropertiesSet() {
		SpringGraniteConfig springGraniteConfig = context.getBeansOfType(SpringGraniteConfig.class).values().iterator().next();
		
        this.graniteConfig = springGraniteConfig.getGraniteConfig();
        
        Map<String, SecurityService> securityServices = context.getBeansOfType(SecurityService.class);
        if (securityServices.size() > 1)
        	log.error("More than one Security Service bean defined");
        else if (!securityServices.isEmpty()) {
        	log.debug("Security Service bean " + securityServices.keySet().iterator().next() + " selected");
        	SecurityService securityService = securityServices.values().iterator().next();
        	this.graniteConfig.setSecurityService(securityService);
        }

        Map<String, Externalizer> externalizers = context.getBeansOfType(Externalizer.class);
        for (Externalizer externalizer : externalizers.values())
            this.graniteConfig.registerExternalizer(externalizer);

        if (useBigDecimal)
            graniteConfig.setExternalizersByType(BigDecimal.class.getName(), BigDecimalExternalizer.class.getName());

        if (useBigInteger)
            graniteConfig.setExternalizersByType(BigInteger.class.getName(), BigIntegerExternalizer.class.getName());

        if (useLong)
            graniteConfig.setExternalizersByType(Long.class.getName(), LongExternalizer.class.getName());

        if (tideAnnotations != null) {
        	for (String ta : tideAnnotations) {
        		try {
        			this.graniteConfig.getTideComponentMatchers().add(new TideComponentAnnotatedWithMatcher(ta, false));
        			log.debug("Enabled components annotated with %s for Tide remoting", ta);
        		}
        		catch (Exception e) {
        			log.error(e, "Could not add tide-component annotation %s", ta);
        		}
        	}
        }
        if (tideInterfaces != null) {
        	for (String ti : tideInterfaces) {
        		try {
        			this.graniteConfig.getTideComponentMatchers().add(new TideComponentInstanceOfMatcher(ti, false));
        			log.debug("Enabled components extending %s for Tide remoting", ti);
        		}
        		catch (Exception e) {
        			log.error(e, "Could not add tide-component interface %s", ti);
        		}
        	}
        }
        if (tideNames != null) {
        	for (String tn : tideNames) {
        		try {
        			this.graniteConfig.getTideComponentMatchers().add(new TideComponentNameMatcher(tn, false));
        			log.debug("Enabled components named like %s for Tide remoting", tn);
        		}
        		catch (Exception e) {
        			log.error(e, "Could not add tide-component name %s", tn);
        		}
        	}
        }
        if (tideTypes != null) {
        	for (String tt : tideTypes) {
        		try {
        			this.graniteConfig.getTideComponentMatchers().add(new TideComponentTypeMatcher(tt, false));
        			log.debug("Enabled components with type %s for Tide remoting", tt);
        		}
        		catch (Exception e) {
        			log.error(e, "Could not add tide-component type %s", tt);
        		}
        	}
        }
        if (exceptionConverters != null) {
        	for (Class<? extends ExceptionConverter> ec : exceptionConverters) {
        		this.graniteConfig.registerExceptionConverter(ec);
    			log.debug("Registered exception converter %s", ec);
        	}
        }
        if (amf3MessageInterceptor != null)
        	this.graniteConfig.setAmf3MessageInterceptor(amf3MessageInterceptor);
        
        // If Spring Data available, automatically enable Page/Pageable converter
        try {
        	TypeUtil.forName("org.springframework.data.domain.Page");
        	Class<Converter> converterClass = TypeUtil.forName("org.granite.spring.data.PageableConverter", Converter.class);
        	this.graniteConfig.getConverters().addConverter(converterClass);
        }
        catch (Exception e) {
        	// Spring Data not present, ignore
        }
        
        servicesConfig = springGraniteConfig.getServicesConfig();
        
        Channel channel = servicesConfig.findChannelById("graniteamf");
        if (channel == null) {
        	channel = new Channel("graniteamf", "mx.messaging.channels.AMFChannel", 
        		new EndPoint("http://{server.name}:{server.port}/{context.root}/graniteamf/amf", "flex.messaging.endpoints.AMFEndpoint"), 
        		new XMap());
        	servicesConfig.addChannel(channel);
        }
        
        if (tide) {
        	Factory factory = servicesConfig.findFactoryById("tide-spring-factory");
        	if (factory == null) {
        		XMap factoryProperties = new XMap();
        		factoryProperties.put("enable-exception-logging", String.valueOf(enableExceptionLogging));
        		factory = new Factory("tide-spring-factory", "org.granite.tide.spring.SpringServiceFactory", factoryProperties);
            	servicesConfig.addFactory(factory);
        	}
        	
        	Service service = servicesConfig.findServiceById("granite-service");
        	if (service == null) {
        		service = new Service("granite-service", "flex.messaging.services.RemotingService", 
        			"flex.messaging.messages.RemotingMessage", null, null, new HashMap<String, Destination>());
        	}
        	Destination destination = servicesConfig.findDestinationById("flex.messaging.messages.RemotingMessage", type);
        	if (destination == null) {
        		List<String> channelIds = new ArrayList<String>();
        		channelIds.add("graniteamf");
        		destination = new Destination(type, channelIds, new XMap(), tideRoles, null, null);
        		destination.getProperties().put("factory", factory.getId());
        		destination.getProperties().put("validator-name", "tideValidator");
        		service.getDestinations().put(destination.getId(), destination);
        		servicesConfig.addService(service);
        	}
        	
        	if (destination.getSecurizer() == null) {
                Map<String, RemotingDestinationSecurizer> securizers = context.getBeansOfType(RemotingDestinationSecurizer.class);
                if (securizers.size() > 1)
                	log.error("More than one Remoting Destination Securizer bean defined");
                else if (!securizers.isEmpty()) {
                	log.debug("Remoting Destination Securizer bean " + securizers.keySet().iterator().next() + " selected");
                	RemotingDestinationSecurizer securizer = securizers.values().iterator().next();
                	destination.setSecurizer(securizer);
                }
        	}
        	
        	log.info("Registered Tide/Spring service factory and destination %s", type);
        }
        else {
        	Factory factory = servicesConfig.findFactoryById("spring-factory");
        	if (factory == null) {
        		XMap factoryProperties = new XMap();
        		factoryProperties.put("enable-exception-logging", String.valueOf(enableExceptionLogging));
        		factory = new Factory("spring-factory", "org.granite.spring.SpringServiceFactory", factoryProperties);
        		servicesConfig.addFactory(factory);
        	}
        	
        	Service service = servicesConfig.findServiceById("granite-service");
        	if (service == null) {
        		service = new Service("granite-service", "flex.messaging.services.RemotingService", 
        			"flex.messaging.messages.RemotingMessage", null, null, new HashMap<String, Destination>());
        		servicesConfig.addService(service);
        	}
            
            servicesConfig.scan(null);
        	
        	log.info("Registered Spring service factory");
        }
        
        amfEndpoint = new AMFEndpoint();
        amfEndpoint.init(servletContext);
	}
	
	public void destroy() throws Exception {
		amfEndpoint.destroy();
		amfEndpoint = null;
	}

	public void setTideRoles(List<String> tideRoles) {
		this.tideRoles = tideRoles;
	}
	
	public void setTideAnnotations(List<String> tideAnnotations) {
		this.tideAnnotations = tideAnnotations;
	}
	
	public void setTideInterfaces(List<String> tideInterfaces) {
		this.tideInterfaces = tideInterfaces;
	}
	
	public void setTideNames(List<String> tideNames) {
		this.tideNames = tideNames;
	}
	
	public void setTideTypes(List<String> tideTypes) {
		this.tideTypes = tideTypes;
	}
	
	public void setExceptionConverters(List<Class<? extends ExceptionConverter>> exceptionConverters) {
		this.exceptionConverters = exceptionConverters;
	}
	
	public void setAmf3MessageInterceptor(AMF3MessageInterceptor amf3MessageInterceptor) {
		this.amf3MessageInterceptor = amf3MessageInterceptor;
	}

    public void setUseLong(boolean useLong) {
        this.useLong = useLong;
    }

    public void setUseBigInteger(boolean useBigInteger) {
        this.useBigInteger = useBigInteger;
    }

    public void setUseBigDecimal(boolean useBigDecimal) {
        this.useBigDecimal = useBigDecimal;
    }
    
    public void setEnableExceptionLogging(boolean enableLogging) {
    	this.enableExceptionLogging = enableLogging;
    }

    public void setTide(boolean tide) {
		this.tide = tide;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	
	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}
	
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	amfEndpoint.service(graniteConfig, servicesConfig, servletContext, request, response);
		return null;
    }

	public boolean supports(Object handler) {
		return handler instanceof ServerFilter;
	}
}