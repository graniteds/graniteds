/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.config;

import java.io.ByteArrayInputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.granite.clustering.DistributedDataFactory;
import org.granite.config.api.Configuration;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.AliasRegistry;
import org.granite.messaging.DefaultAliasRegistry;
import org.granite.messaging.amf.io.AMF3Deserializer;
import org.granite.messaging.amf.io.AMF3DeserializerSecurizer;
import org.granite.messaging.amf.io.AMF3Serializer;
import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.ActionScriptClassDescriptor;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.messaging.amf.io.util.DefaultClassGetter;
import org.granite.messaging.amf.io.util.JavaClassDescriptor;
import org.granite.messaging.amf.io.util.externalizer.BigDecimalExternalizer;
import org.granite.messaging.amf.io.util.externalizer.BigIntegerExternalizer;
import org.granite.messaging.amf.io.util.externalizer.Externalizer;
import org.granite.messaging.amf.io.util.externalizer.LongExternalizer;
import org.granite.messaging.amf.io.util.externalizer.MapExternalizer;
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.reflect.Reflection;
import org.granite.messaging.service.DefaultMethodMatcher;
import org.granite.messaging.service.ExceptionConverter;
import org.granite.messaging.service.MethodMatcher;
import org.granite.messaging.service.ServiceInvocationListener;
import org.granite.messaging.service.security.SecurityService;
import org.granite.messaging.service.tide.TideComponentMatcher;
import org.granite.scan.ScannedItem;
import org.granite.scan.ScannedItemHandler;
import org.granite.scan.Scanner;
import org.granite.scan.ScannerFactory;
import org.granite.util.StreamUtil;
import org.granite.util.TypeUtil;
import org.granite.util.XMap;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Franck WOLFF
 */
public class GraniteConfig implements ScannedItemHandler {

	public enum JMF_EXTENSIONS_MODE {
		PREPPEND,
		APPEND,
		REPLACE
	}
	
    ///////////////////////////////////////////////////////////////////////////
    // Static fields.

    private static final Logger log = Logger.getLogger(GraniteConfig.class);
    
    private static final String GRANITE_CONFIG_PUBLIC_ID = "-//Granite Data Services//DTD granite-config internal//EN";
    private static final String GRANITE_CONFIG_PROPERTIES = "META-INF/granite-config.properties";

    final ExternalizerFactory EXTERNALIZER_FACTORY = new ExternalizerFactory();
    private static final Externalizer LONG_EXTERNALIZER = new LongExternalizer();
    private static final Externalizer BIGINTEGER_EXTERNALIZER = new BigIntegerExternalizer();
    private static final Externalizer BIGDECIMAL_EXTERNALIZER = new BigDecimalExternalizer();
    private static final Externalizer MAP_EXTERNALIZER = new MapExternalizer();
    
    final ActionScriptClassDescriptorFactory ASC_DESCRIPTOR_FACTORY = new ActionScriptClassDescriptorFactory();
    final JavaClassDescriptorFactory JC_DESCRIPTOR_FACTORY = new JavaClassDescriptorFactory();
    final TideComponentMatcherFactory TIDE_COMPONENT_MATCHER_FACTORY = new TideComponentMatcherFactory();

    ///////////////////////////////////////////////////////////////////////////
    // Instance fields.

    // Should we scan classpath for auto-configured services/externalizers?
    private boolean scan = false;
    
    private AliasRegistry aliasRegistry = new DefaultAliasRegistry();
    
    private String MBeanContextName = null;

    // Custom AMF3 (De)Serializer configuration.
    private Constructor<AMF3Serializer> amf3SerializerConstructor = null;
    private Constructor<AMF3Deserializer> amf3DeserializerConstructor = null;
    
    private AMF3DeserializerSecurizer amf3DeserializerSecurizer = null;

    // Custom AMF3 message interceptor configuration.
    private AMF3MessageInterceptor amf3MessageInterceptor = null;

    // Converters configuration.
    private List<Class<? extends Converter>> converterClasses = new ArrayList<Class<? extends Converter>>();
    private Converters converters = null;

    // MethodMatcher configuration.
    private MethodMatcher methodMatcher = new DefaultMethodMatcher();

    // Invocation listener configuration.
    private ServiceInvocationListener invocationListener = null;

    // Instantiators configuration.
    private final Map<String, String> instantiators = new HashMap<String, String>();

    // Class getter configuration.
    private ClassGetter classGetter = new DefaultClassGetter();
    private boolean classGetterSet = false;

    // Externalizers configuration.
    private XMap externalizersConfiguration = null;
    private final List<Externalizer> scannedExternalizers = new ArrayList<Externalizer>();
    private final ConcurrentHashMap<String, Externalizer> externalizersByType
        = new ConcurrentHashMap<String, Externalizer>();
    private final Map<String, String> externalizersByInstanceOf = new HashMap<String, String>();
    private final Map<String, String> externalizersByAnnotatedWith = new HashMap<String, String>();

    // JMF extended codecs.
    private JMF_EXTENSIONS_MODE jmfExtendedCodecsMode = JMF_EXTENSIONS_MODE.APPEND;
    private final List<ExtendedObjectCodec> jmfExtendedCodecs = new ArrayList<ExtendedObjectCodec>();

    // JMF default stored strings.
    private JMF_EXTENSIONS_MODE jmfDefaultStoredStringsMode = JMF_EXTENSIONS_MODE.APPEND;
    private final List<String> jmfDefaultStoredStrings = new ArrayList<String>();
    
    // JMF reflection.
    private Reflection jmfReflection = null;
    
    // Java descriptors configuration.
    private final ConcurrentHashMap<String, Class<? extends JavaClassDescriptor>> javaDescriptorsByType
        = new ConcurrentHashMap<String, Class<? extends JavaClassDescriptor>>();
    private final Map<String, String> javaDescriptorsByInstanceOf = new HashMap<String, String>();

    // AS3 descriptors configuration.
    private final ConcurrentHashMap<String, Class<? extends ActionScriptClassDescriptor>> as3DescriptorsByType
        = new ConcurrentHashMap<String, Class<? extends ActionScriptClassDescriptor>>();
    private final Map<String, String> as3DescriptorsByInstanceOf = new HashMap<String, String>();
    
    // Exception converters
    private final List<ExceptionConverter> exceptionConverters = new ArrayList<ExceptionConverter>();

    // Tide-enabled Components configuration.
    private final ConcurrentHashMap<String, Object[]> enabledTideComponentsByName = new ConcurrentHashMap<String, Object[]>();
    private final ConcurrentHashMap<String, Object[]> disabledTideComponentsByName = new ConcurrentHashMap<String, Object[]>();
    private final List<TideComponentMatcher> tideComponentMatchers = new ArrayList<TideComponentMatcher>();

    // Security service configuration.
    private SecurityService securityService = null;

    // MessageSelector configuration.
    private Constructor<?> messageSelectorConstructor;
    
    // Gravity configuration.
    private XMap gravityConfig;
    
    // Clustering
    private DistributedDataFactory distributedDataFactory;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    public GraniteConfig(String stdConfig, InputStream customConfigIs, Configuration configuration, String MBeanContextName) throws IOException, SAXException {
        try {
            amf3SerializerConstructor = TypeUtil.getConstructor(AMF3Serializer.class, new Class<?>[]{OutputStream.class});
            amf3DeserializerConstructor = TypeUtil.getConstructor(AMF3Deserializer.class, new Class<?>[]{InputStream.class});
        } catch (Exception e) {
            throw new GraniteConfigException("Could not get constructor for AMF3 (de)serializers", e);
        }
        
        this.MBeanContextName = MBeanContextName;
        
    	ClassLoader loader = GraniteConfig.class.getClassLoader();
        
    	final ByteArrayInputStream dtd = StreamUtil.getResourceAsStream("org/granite/config/granite-config.dtd", loader);
        final EntityResolver resolver = new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                if (GRANITE_CONFIG_PUBLIC_ID.equals(publicId)) {
                    dtd.reset();
                    InputSource source = new InputSource(dtd);
                    source.setPublicId(publicId);
                    return source;
                }
                return null;
            }
        };

        // Load standard config.
        InputStream is = null;
        try {
            is = StreamUtil.getResourceAsStream("org/granite/config/granite-config.xml", loader);
            XMap doc = new XMap(is, resolver);
            forElement(doc, false, null);
        } finally {
            if (is != null)
                is.close();
        }
        
        if (stdConfig != null) {
            try {
                is = StreamUtil.getResourceAsStream(stdConfig, loader);
                XMap doc = new XMap(is, resolver);
                forElement(doc, false, null);
            } finally {
                if (is != null)
                    is.close();
            }
        }

        // Load custom config (override).
        if (customConfigIs != null) {
        	XMap doc = new XMap(customConfigIs, resolver);
            forElement(doc, true, configuration != null ? configuration.getGraniteConfigProperties() : null);
        }
        
        if (amf3DeserializerSecurizer == null)
        	log.warn("You should configure a deserializer securizer in your granite-config.xml file in order to prevent potential security exploits!");
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Classpath scan initialization.
    
    private void scanConfig(String graniteConfigProperties) {
    	//if config overriding exists
        Scanner scanner = ScannerFactory.createScanner(this, graniteConfigProperties != null ? graniteConfigProperties : GRANITE_CONFIG_PROPERTIES);
        try {
            scanner.scan();
        } catch (Exception e) {
            log.error(e, "Could not scan classpath for configuration");
        }
    }

    public boolean handleMarkerItem(ScannedItem item) {
        try {
            return handleProperties(item.loadAsProperties());
        } catch (Exception e) {
            log.error(e, "Could not load properties: %s", item);
        }
        return true;
    }

    public void handleScannedItem(ScannedItem item) {
        if ("class".equals(item.getExtension()) && item.getName().indexOf('$') == -1) {
            try {
                handleClass(item.loadAsClass());
            } catch (NoClassDefFoundError e) {
                // Ignore errors with Tide classes depending on Gravity
            } catch (LinkageError e) {
                // Ignore errors with GraniteDS/Hibernate classes depending on Hibernate 3 when using Hibernate 4
            } catch (Throwable t) {
                log.error(t, "Could not load class: %s", item);
            }
        }
    }

    private boolean handleProperties(Properties properties) {
    	if (properties.getProperty("dependsOn") != null) {
    		String dependsOn = properties.getProperty("dependsOn");
    		try {
    			TypeUtil.forName(dependsOn);
    		}
    		catch (ClassNotFoundException e) {
    			// Class not found, skip scan for this package
    			return true;
    		}
    	}
    	
        String classGetterName = properties.getProperty("classGetter");
        if (!classGetterSet && classGetterName != null) {
            try {
                classGetter = TypeUtil.newInstance(classGetterName, ClassGetter.class);
            } catch (Throwable t) {
                log.error(t, "Could not create instance of: %s", classGetterName);
            }
        }

        String amf3MessageInterceptorName = properties.getProperty("amf3MessageInterceptor");
        if (amf3MessageInterceptor == null && amf3MessageInterceptorName != null) {
            try {
                amf3MessageInterceptor = TypeUtil.newInstance(amf3MessageInterceptorName, AMF3MessageInterceptor.class);
            } catch (Throwable t) {
                log.error(t, "Could not create instance of: %s", amf3MessageInterceptorName);
            }
        }
        
        for (Map.Entry<?, ?> me : properties.entrySet()) {
            if (me.getKey().toString().startsWith("converter.")) {
                String converterName = me.getValue().toString();
                try {
                    converterClasses.add(TypeUtil.forName(converterName, Converter.class));
                } catch (Exception e) {
                    throw new GraniteConfigException("Could not get converter class for: " + converterName, e);
                }
            }
        }
        
        return false;
    }

    private void handleClass(Class<?> clazz) {
        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            if (Externalizer.class.isAssignableFrom(clazz)) {
                try {
                    scannedExternalizers.add(TypeUtil.newInstance(clazz, Externalizer.class));
                } catch (Exception e) {
                    log.error(e, "Could not create new instance of: %s", clazz);
                }
            }
            
            if (ExceptionConverter.class.isAssignableFrom(clazz)) {
                try {
                    exceptionConverters.add(TypeUtil.newInstance(clazz, ExceptionConverter.class));
                } catch (Exception e) {
                	if (!clazz.getName().equals("org.granite.tide.hibernate.HibernateValidatorExceptionConverter"))	// GDS-582
                		log.error(e, "Could not create new instance of: %s", clazz);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Property getters.

    public boolean getScan() {
    	return scan;
    }
    
    public boolean isRegisterMBeans() {
    	return MBeanContextName != null;
    }
    
    public String getMBeanContextName() {
    	return MBeanContextName;
    }

    
	public ObjectOutput newAMF3Serializer(OutputStream out) {
        try {
            return amf3SerializerConstructor.newInstance(new Object[]{out});
        } catch (Exception e) {
            throw new GraniteConfigException("Could not create serializer instance with: " + amf3SerializerConstructor, e);
        }
    }
	
	public Constructor<?> getAmf3SerializerConstructor() {
		return amf3SerializerConstructor;
	}

    public ObjectInput newAMF3Deserializer(InputStream in) {
        try {
            return amf3DeserializerConstructor.newInstance(new Object[]{in});
        } catch (Exception e) {
            throw new GraniteConfigException("Could not create deserializer instance with: " + amf3DeserializerConstructor, e);
        }
    }
	
	public Constructor<?> getAmf3DeserializerConstructor() {
		return amf3DeserializerConstructor;
	}

    public AMF3DeserializerSecurizer getAmf3DeserializerSecurizer() {
		return amf3DeserializerSecurizer;
	}
	public void setAmf3DeserializerSecurizer(
			AMF3DeserializerSecurizer amf3DeserializerSecurizer) {
		this.amf3DeserializerSecurizer = amf3DeserializerSecurizer;
	}

	public AMF3MessageInterceptor getAmf3MessageInterceptor() {
        return amf3MessageInterceptor;
    }
    public void setAmf3MessageInterceptor(AMF3MessageInterceptor amf3MessageInterceptor) {
    	this.amf3MessageInterceptor = amf3MessageInterceptor;
    }
    
    public Map<String, String> getInstantiators() {
    	return instantiators;
    }

    public Converters getConverters() {
        return converters;
    }

    public MethodMatcher getMethodMatcher() {
        return methodMatcher;
    }

    public ServiceInvocationListener getInvocationListener() {
        return invocationListener;
    }

    public String getInstantiator(String type) {
        return instantiators.get(type);
    }

    public ClassGetter getClassGetter() {
        return classGetter;
    }

    public XMap getExternalizersConfiguration() {
		return externalizersConfiguration;
	}

	public void setExternalizersConfiguration(XMap externalizersConfiguration) {
		this.externalizersConfiguration = externalizersConfiguration;
	}

	public Externalizer getExternalizer(String type) {
        Externalizer externalizer = getElementByType(
            type,
            EXTERNALIZER_FACTORY,
            externalizersByType,
            externalizersByInstanceOf,
            externalizersByAnnotatedWith,
            scannedExternalizers
        );
        if (externalizer != null)
        	return externalizer;
        
        if ("java".equals(GraniteContext.getCurrentInstance().getClientType())) {
        	// Force use of number externalizers when serializing from/to a Java client
        	if (Long.class.getName().equals(type))
        		return LONG_EXTERNALIZER;
        	else if (BigInteger.class.getName().equals(type))
        		return BIGINTEGER_EXTERNALIZER;
        	else if (BigDecimal.class.getName().equals(type))
        		return BIGDECIMAL_EXTERNALIZER;
        	else {
            	try {
            		Class<?> clazz = TypeUtil.forName(type);
	        		if (Map.class.isAssignableFrom(clazz) && !Externalizable.class.isAssignableFrom(clazz))
	        			return MAP_EXTERNALIZER;
            	}
            	catch (Exception e) {
            		
            	}
        	}
        }
        
        return null;
    }
	
	public Map<String, Externalizer> getExternalizersByType() {
		return externalizersByType;
	}
	
	public Map<String, String> getExternalizersByInstanceOf() {
		return externalizersByInstanceOf;
	}
	
	public Map<String, String> getExternalizersByAnnotatedWith() {
		return externalizersByAnnotatedWith;
	}
	
	public List<Externalizer> getScannedExternalizers() {
		return scannedExternalizers;
	}
	
    public JMF_EXTENSIONS_MODE getJmfExtendedCodecsMode() {
		return jmfExtendedCodecsMode;
	}

	public List<ExtendedObjectCodec> getJmfExtendedCodecs() {
		return jmfExtendedCodecs;
	}

	public JMF_EXTENSIONS_MODE getJmfDefaultStoredStringsMode() {
		return jmfDefaultStoredStringsMode;
	}

	public Reflection getJmfReflection() {
		return jmfReflection;
	}

	public List<String> getJmfDefaultStoredStrings() {
		return jmfDefaultStoredStrings;
	}

	public Class<? extends ActionScriptClassDescriptor> getActionScriptDescriptor(String type) {
        return getElementByType(type, ASC_DESCRIPTOR_FACTORY, as3DescriptorsByType, as3DescriptorsByInstanceOf, null, null);
    }

    public Map<String, Class<? extends ActionScriptClassDescriptor>> getAs3DescriptorsByType() {
    	return as3DescriptorsByType;
    }

    public Map<String, String> getAs3DescriptorsByInstanceOf() {
    	return as3DescriptorsByInstanceOf;
    }
    
    
    public Class<? extends JavaClassDescriptor> getJavaDescriptor(String type) {
        return getElementByType(type, JC_DESCRIPTOR_FACTORY, javaDescriptorsByType, javaDescriptorsByInstanceOf, null, null);
    }

    public Map<String, Class<? extends JavaClassDescriptor>> getJavaDescriptorsByType() {
    	return javaDescriptorsByType;
    }

    public Map<String, String> getJavaDescriptorsByInstanceOf() {
    	return javaDescriptorsByInstanceOf;
    }    
    
    
    public boolean isComponentTideEnabled(String componentName, Set<Class<?>> componentClasses, Object instance) {
        return TideComponentMatcherFactory.isComponentTideEnabled(enabledTideComponentsByName, tideComponentMatchers, componentName, componentClasses, instance);
    }
    
    public boolean isComponentTideDisabled(String componentName, Set<Class<?>> componentClasses, Object instance) {
        return TideComponentMatcherFactory.isComponentTideDisabled(disabledTideComponentsByName, tideComponentMatchers, componentName, componentClasses, instance);
    }
    
    
    public List<ExceptionConverter> getExceptionConverters() {
        return exceptionConverters;
    }
    
    public void registerExceptionConverter(Class<? extends ExceptionConverter> exceptionConverterClass) {
        registerExceptionConverter(exceptionConverterClass, false);
    }
    public void registerExceptionConverter(Class<? extends ExceptionConverter> exceptionConverterClass, boolean first) {
    	for (ExceptionConverter ec : exceptionConverters) {
    		if (ec.getClass() == exceptionConverterClass)
    			return;
    	}
		try {
			ExceptionConverter exceptionConverter = TypeUtil.newInstance(exceptionConverterClass, ExceptionConverter.class);
	        if (first)
	            exceptionConverters.add(0, exceptionConverter);
	        else
	            exceptionConverters.add(exceptionConverter);
		} 
		catch (Exception e) {
			log.error(e, "Could not instantiate exception converter: %s", exceptionConverterClass);
		}
    }
    
    public void registerExceptionConverter(ExceptionConverter exceptionConverter, boolean first) {
    	for (ExceptionConverter ec : exceptionConverters) {
    		if (ec.getClass() == exceptionConverter.getClass())
    			return;
    	}
    	if (first)
            exceptionConverters.add(0, exceptionConverter);
    	else
    	    exceptionConverters.add(exceptionConverter);
    }

    public boolean hasSecurityService() {
        return securityService != null;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }
    
    public List<TideComponentMatcher> getTideComponentMatchers() {
    	return tideComponentMatchers;
    }
    
    public Map<String, Object[]> getEnabledTideComponentsByName() {
    	return enabledTideComponentsByName;
    }
    
    public Map<String, Object[]> getDisabledTideComponentsByName() {
    	return disabledTideComponentsByName;
    }
    
	
	public XMap getGravityConfig() {
		return gravityConfig;
	}
	
	public DistributedDataFactory getDistributedDataFactory() {
		return distributedDataFactory;
	}

    public Constructor<?> getMessageSelectorConstructor() {
        return messageSelectorConstructor;
    }
    public Externalizer setExternalizersByType(String type, String externalizerType) {
    	return externalizersByType.put(type, EXTERNALIZER_FACTORY.getInstance(externalizerType, this));
    }

    public String putExternalizersByInstanceOf(String instanceOf, String externalizerType) {
    	return externalizersByInstanceOf.put(instanceOf, externalizerType);
    }

    public String putExternalizersByAnnotatedWith(String annotatedWith, String externalizerType) {
    	return externalizersByAnnotatedWith.put(annotatedWith, externalizerType);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Static GraniteConfig loading helpers.

    private void forElement(XMap element, boolean custom, String graniteConfigProperties) {
        String scan = element.get("@scan");

        this.scan = Boolean.TRUE.toString().equals(scan);

        loadCustomAMF3Serializer(element, custom);
        loadCustomAMF3DeserializerSecurizer(element, custom);
        loadCustomAMF3MessageInterceptor(element, custom);
        loadCustomConverters(element, custom);
        loadCustomMethodMatcher(element, custom);
        loadCustomInvocationListener(element, custom);
        loadCustomInstantiators(element, custom);
        loadCustomClassGetter(element, custom);
        loadCustomExternalizers(element, custom);
        loadCustomJMFExtendedCodecs(element, custom);
        loadCustomJMFDefaultStoredStrings(element, custom);
        loadCustomJMFReflection(element, custom);
        loadCustomDescriptors(element, custom);
        loadCustomExceptionConverters(element, custom);
        loadCustomTideComponents(element, custom);
        loadCustomSecurity(element, custom);
        loadCustomMessageSelector(element, custom);
        loadCustomGravity(element, custom);
        loadCustomDistributedDataFactory(element, custom);

        if (this.scan)
            scanConfig(graniteConfigProperties);

        finishCustomConverters(custom);
    }

    private void loadCustomAMF3Serializer(XMap element, boolean custom) {
        XMap amf3Serializer = element.getOne("amf3-serializer");
        if (amf3Serializer != null) {
            String type = amf3Serializer.get("@type");
            try {
                Class<AMF3Serializer> amf3SerializerClass = TypeUtil.forName(type, AMF3Serializer.class);
                amf3SerializerConstructor = TypeUtil.getConstructor(amf3SerializerClass, new Class<?>[]{OutputStream.class});
            } catch (Exception e) {
                throw new GraniteConfigException("Could not get constructor for AMF3 serializer: " + type, e);
            }
        }

        XMap amf3Deserializer = element.getOne("amf3-deserializer");
        if (amf3Deserializer != null) {
            String type = amf3Deserializer.get("@type");
            try {
                Class<AMF3Deserializer> amf3DeserializerClass = TypeUtil.forName(type, AMF3Deserializer.class);
                amf3DeserializerConstructor = TypeUtil.getConstructor(amf3DeserializerClass, new Class<?>[]{InputStream.class});
            } catch (Exception e) {
                throw new GraniteConfigException("Could not get constructor for AMF3 deserializer: " + type, e);
            }
        }
    }

    private void loadCustomAMF3DeserializerSecurizer(XMap element, boolean custom) {
        XMap securizer = element.getOne("amf3-deserializer-securizer");
        if (securizer != null) {
            String type = securizer.get("@type");
            try {
                amf3DeserializerSecurizer = (AMF3DeserializerSecurizer)TypeUtil.newInstance(type);
            } catch (Exception e) {
                throw new GraniteConfigException("Could not construct amf3 deserializer securizer: " + type, e);
            }
            String param = securizer.get("@param");
            try {
                amf3DeserializerSecurizer.setParam(param);
            } catch (Exception e) {
                throw new GraniteConfigException("Could not set param of amf3 deserializer securizer: " + type + ", param: " + param, e);
            }
        }
    }

    private void loadCustomAMF3MessageInterceptor(XMap element, boolean custom) {
        XMap interceptor = element.getOne("amf3-message-interceptor");
        if (interceptor != null) {
            String type = interceptor.get("@type");
            try {
                amf3MessageInterceptor = (AMF3MessageInterceptor)TypeUtil.newInstance(type);
            } catch (Exception e) {
                throw new GraniteConfigException("Could not construct amf3 message interceptor: " + type, e);
            }
        }
    }
    
    private void loadCustomDistributedDataFactory(XMap element, boolean custom) {
        XMap distributedDataFactory = element.getOne("distributed-data-factory");
        if (distributedDataFactory != null) {
            String type = distributedDataFactory.get("@type");
            try {
            	this.distributedDataFactory = (DistributedDataFactory)TypeUtil.newInstance(type);
            } catch (Exception e) {
                throw new GraniteConfigException("Could not construct build distributed data factory: " + type, e);
            }
        }
    }

    private void loadCustomConverters(XMap element, boolean custom) {
        XMap converters = element.getOne("converters");
        if (converters != null) {
            // Should we override standard config converters?
            String override = converters.get("@override");
            if (Boolean.TRUE.toString().equals(override))
                converterClasses.clear();

            int i = 0;
            for (XMap converter : converters.getAll("converter")) {
                String type = converter.get("@type");
                try {
                    // For custom config, shifts any standard converters to the end of the list...
                    converterClasses.add(i++, TypeUtil.forName(type, Converter.class));
                } catch (Exception e) {
                    throw new GraniteConfigException("Could not get converter class for: " + type, e);
                }
            }
        }
    }
    
    private void finishCustomConverters(boolean custom) {
        try {
            converters = new Converters(converterClasses);
        } catch (Exception e) {
            throw new GraniteConfigException("Could not construct new Converters instance", e);
        }
        
        // Cleanup...
        if (custom)
            converterClasses = null;
    }

    private void loadCustomMethodMatcher(XMap element, boolean custom) {
        XMap methodMatcher = element.getOne("method-matcher");
        if (methodMatcher != null) {
            String type = methodMatcher.get("@type");
            try {
                this.methodMatcher = (MethodMatcher)TypeUtil.newInstance(type);
            } catch (Exception e) {
                throw new GraniteConfigException("Could not construct method matcher: " + type, e);
            }
        }
    }

    private void loadCustomInvocationListener(XMap element, boolean custom) {
        XMap invocationListener = element.getOne("invocation-listener");
        if (invocationListener != null) {
            String type = invocationListener.get("@type");
            try {
            	this.invocationListener = (ServiceInvocationListener)TypeUtil.newInstance(type);
            } catch (Exception e) {
                throw new GraniteConfigException("Could not instantiate ServiceInvocationListener: " + type, e);
            }
        }
    }

    private void loadCustomInstantiators(XMap element, boolean custom) {
        XMap instantiators = element.getOne("instantiators");
        if (instantiators != null) {
            for (XMap instantiator : instantiators.getAll("instantiator"))
                this.instantiators.put(instantiator.get("@type"), instantiator.get("."));
        }
    }

    private void loadCustomClassGetter(XMap element, boolean custom) {
        XMap classGetter = element.getOne("class-getter");
        if (classGetter != null) {
            String type = classGetter.get("@type");
            try {
            	this.classGetter = (ClassGetter)TypeUtil.newInstance(type);
                classGetterSet = true;
            } catch (Exception e) {
                throw new GraniteConfigException("Could not instantiate ClassGetter: " + type, e);
            }
        }
    }

    private void loadCustomExternalizers(XMap element, boolean custom) {
    	externalizersConfiguration = element.getOne("externalizers/configuration");
        
    	for (XMap externalizer : element.getAll("externalizers/externalizer")) {
            String externalizerType = externalizer.get("@type");

            for (XMap include : externalizer.getAll("include")) {
                String type = include.get("@type");
                if (type != null)
                    externalizersByType.put(type, EXTERNALIZER_FACTORY.getInstance(externalizerType, this));
                else {
                    String instanceOf = include.get("@instance-of");
                    if (instanceOf != null)
                        externalizersByInstanceOf.put(instanceOf, externalizerType);
                    else {
                        String annotatedWith = include.get("@annotated-with");
                        if (annotatedWith == null)
                            throw new GraniteConfigException(
                                "Element 'include' has no attribute 'type', 'instance-of' or 'annotated-with'");
                        externalizersByAnnotatedWith.put(annotatedWith, externalizerType);
                    }
                }
            }
        }
    }

    private void loadCustomJMFExtendedCodecs(XMap element, boolean custom) {
    	String jmfExtendedCodecsMode = element.get("jmf-extended-codecs/@mode");
    	if (jmfExtendedCodecsMode != null) {
	    	try {
	    		this.jmfExtendedCodecsMode = JMF_EXTENSIONS_MODE.valueOf(jmfExtendedCodecsMode.toLowerCase());
	    	}
	    	catch (Exception e) {
	        	throw new GraniteConfigException("Illegal JMF extended codecs mode: " + jmfExtendedCodecsMode, e);
	    	}
    	}
    	
    	for (XMap codec : element.getAll("jmf-extended-codecs/jmf-extended-codec")) {
            String codecType = codec.get("@type");

            try {
				jmfExtendedCodecs.add((ExtendedObjectCodec)TypeUtil.newInstance(codecType));
			}
            catch (Exception e) {
            	throw new GraniteConfigException("Could not instantiate JMF extended codec: " + codecType, e);
			}
        }
    }

    private void loadCustomJMFDefaultStoredStrings(XMap element, boolean custom) {
    	String jmfDefaultStoredStringsMode = element.get("jmf-default-stored-strings/@mode");
    	if (jmfDefaultStoredStringsMode != null) {
    		try {
	    		this.jmfDefaultStoredStringsMode = JMF_EXTENSIONS_MODE.valueOf(jmfDefaultStoredStringsMode.toLowerCase());
	    	}
	    	catch (Exception e) {
	        	throw new GraniteConfigException("Illegal JMF default stored strings mode: " + jmfDefaultStoredStringsMode, e);
	    	}
    	}
    	
    	for (XMap codec : element.getAll("jmf-default-stored-strings/jmf-default-stored-string"))
			jmfDefaultStoredStrings.add(codec.get("@value"));
    }

    private void loadCustomJMFReflection(XMap element, boolean custom) {
    	String jmfReflection = element.get("jmf-reflection/@type");
    	if (jmfReflection == null)
    		this.jmfReflection = new Reflection(null);
    	else {
	    	try {
	    		this.jmfReflection = (Reflection)TypeUtil.newInstance(jmfReflection);
	    	}
	    	catch (Exception e) {
            	throw new GraniteConfigException("Could not instantiate JMF reflection: " + jmfReflection, e);
	    	}
    	}
    }

    /**
     * Read custom class descriptors.
     * Descriptor must have 'type' or 'instanceof' attribute
     * and one of 'java' or 'as3' attributes specified.
     */
    private void loadCustomDescriptors(XMap element, boolean custom) {
        for (XMap descriptor : element.getAll("descriptors/descriptor")) {
            String type = descriptor.get("@type");
            if (type != null) {
                String java = descriptor.get("@java");
                String as3 = descriptor.get("@as3");
                if (java == null && as3 == null)
                    throw new GraniteConfigException(
                        "Element 'descriptor' has no attributes 'java' or 'as3'\n" + descriptor
                    );
                if (java != null)
                    javaDescriptorsByType.put(type, JC_DESCRIPTOR_FACTORY.getInstance(java, this));
                if (as3 != null)
                    as3DescriptorsByType.put(type, ASC_DESCRIPTOR_FACTORY.getInstance(as3, this));
            } else {
                String instanceOf = descriptor.get("@instance-of");
                if (instanceOf == null)
                    throw new GraniteConfigException(
                        "Element 'descriptor' has no attribute 'type' or 'instance-of'\n" + descriptor
                    );
                String java = descriptor.get("@java");
                String as3 = descriptor.get("@as3");
                if (java == null && as3 == null) {
                    throw new GraniteConfigException(
                        "Element 'descriptor' has no attributes 'java' or 'as3' in:\n" + descriptor
                    );
                }
                if (java != null)
                    javaDescriptorsByInstanceOf.put(instanceOf, java);
                if (as3 != null)
                    as3DescriptorsByInstanceOf.put(instanceOf, as3);
            }
        }
    }
    
    public void setAliasRegistry(AliasRegistry aliasRegistry) {
    	this.aliasRegistry = aliasRegistry;
    }
    
    public AliasRegistry getAliasRegistry() {
    	return aliasRegistry;
    }
    
    /**
     * Read custom class exception converters
     * Converter must have 'type' attribute
     */
    private void loadCustomExceptionConverters(XMap element, boolean custom) {
        for (XMap exceptionConverter : element.getAll("exception-converters/exception-converter")) {
            String type = exceptionConverter.get("@type");
            ExceptionConverter converter = null;
            try {
                converter = (ExceptionConverter)TypeUtil.newInstance(type);
                exceptionConverters.add(converter);
            } catch (Exception e) {
                throw new GraniteConfigException("Could not construct exception converter: " + type, e);
            }
        }
    }

    private void loadCustomTideComponents(XMap element, boolean custom) {
        for (XMap component : element.getAll("tide-components/tide-component")) {
            boolean disabled = Boolean.TRUE.toString().equals(component.get("@disabled"));
            String type = component.get("@type");
            if (type != null)
                tideComponentMatchers.add(TIDE_COMPONENT_MATCHER_FACTORY.getTypeMatcher(type, disabled));
            else {
                String name = component.get("@name");
                if (name != null)
                    tideComponentMatchers.add(TIDE_COMPONENT_MATCHER_FACTORY.getNameMatcher(name, disabled));
                else {
                    String instanceOf = component.get("@instance-of");
                    if (instanceOf != null)
                        tideComponentMatchers.add(TIDE_COMPONENT_MATCHER_FACTORY.getInstanceOfMatcher(instanceOf, disabled));
                    else {
                        String annotatedWith = component.get("@annotated-with");
                        if (annotatedWith == null)
                            throw new GraniteConfigException(
                                "Element 'component' has no attribute 'type', 'name', 'instance-of' or 'annotated-with'");
                        tideComponentMatchers.add(TIDE_COMPONENT_MATCHER_FACTORY.getAnnotatedWithMatcher(annotatedWith, disabled));
                    }
                }
            }
        }
    }

    private void loadCustomSecurity(XMap element, boolean custom) {
        XMap security = element.getOne("security");
        if (security != null) {
            String type = security.get("@type");
            try {
                securityService = (SecurityService)TypeUtil.newInstance(type);
            } catch (Exception e) {
                throw new GraniteConfigException("Could not instantiate SecurityService: " + type, e);
            }

            Map<String, String> params = new HashMap<String, String>();
            for (XMap param : security.getAll("param")) {
                String name = param.get("@name");
                String value = param.get("@value");
                params.put(name, value);
            }
            try {
                securityService.configure(params);
            } catch (Exception e) {
                throw new GraniteConfigException("Could not configure SecurityService " + type + " with: " + params, e);
            }
        }
    }
    
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    private void loadCustomMessageSelector(XMap element, boolean custom) {
        XMap selector = element.getOne("message-selector");
        if (selector != null) {
            String type = selector.get("@type");
            try {
                messageSelectorConstructor = TypeUtil.getConstructor(type, new Class<?>[]{ String.class });
            } catch (Exception e) {
                throw new GraniteConfigException("Could not construct message selector: " + type, e);
            }
        }
    }

    private void loadCustomGravity(XMap element, boolean custom) {
        gravityConfig = element.getOne("gravity");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Other helpers.

	private <T> T getElementByType(
        String type,
        ConfigurableFactory<T> factory,
        ConcurrentHashMap<String, T> elementsByType,
        Map<String, String> elementsByInstanceOf,
        Map<String, String> elementsByAnnotatedWith,
        List<T> scannedConfigurables) {

        // This NULL object is a Java null placeholder: ConcurrentHashMap doesn't allow
        // null values...
        final T NULL = factory.getNullInstance();

        T element = elementsByType.get(type);
        if (element != null)
            return (NULL == element ? null : element);
        element = NULL;

        Class<?> typeClass = null;
        try {
            typeClass = TypeUtil.forName(type);
        } catch (Exception e) {
            throw new GraniteConfigException("Could not load class: " + type, e);
        }

        if (elementsByAnnotatedWith != null && NULL == element) {
            for (Map.Entry<String, String> entry : elementsByAnnotatedWith.entrySet()) {
                String annotation = entry.getKey();
                try {
                    Class<Annotation> annotationClass = TypeUtil.forName(annotation, Annotation.class);
                    if (typeClass.isAnnotationPresent(annotationClass)) {
                        element = factory.getInstance(entry.getValue(), this);
                        break;
                    }
                } catch (Exception e) {
                    throw new GraniteConfigException("Could not load class: " + annotation, e);
                }
            }
        }

        if (elementsByInstanceOf != null && NULL == element) {
	        for (Map.Entry<String, String> entry : elementsByInstanceOf.entrySet()) {
	            String instanceOf = entry.getKey();
	            try {
	                Class<?> instanceOfClass = TypeUtil.forName(instanceOf);
	                if (instanceOfClass.isAssignableFrom(typeClass)) {
	                    element = factory.getInstance(entry.getValue(), this);
	                    break;
	                }
	            } catch (Exception e) {
	                throw new GraniteConfigException("Could not load class: " + instanceOf, e);
	            }
	        }
        }

        if (NULL == element)
            element = factory.getInstanceForBean(scannedConfigurables, typeClass, this);

        T previous = elementsByType.putIfAbsent(type, element);
        if (previous != null)
            element = previous;

        return (NULL == element ? null : element);
    }
}
