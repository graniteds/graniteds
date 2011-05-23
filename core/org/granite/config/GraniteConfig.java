package org.granite.config;

import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.ActionScriptClassDescriptor;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.messaging.amf.io.util.JavaClassDescriptor;
import org.granite.messaging.amf.io.util.externalizer.Externalizer;
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.service.ExceptionConverter;
import org.granite.messaging.service.MethodMatcher;
import org.granite.messaging.service.ServiceInvocationListener;
import org.granite.messaging.service.security.SecurityService;
import org.granite.messaging.service.tide.TideComponentMatcher;
import org.granite.util.XMap;

import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GraniteConfig {
    public boolean getScan();

    public boolean isRegisterMBeans();

    public String getMBeanContextName();

    public ObjectOutput newAMF3Serializer(OutputStream out);

    public Constructor<?> getAmf3SerializerConstructor();

    public ObjectInput newAMF3Deserializer(InputStream in);

    public Constructor<?> getAmf3DeserializerConstructor();

    public AMF3MessageInterceptor getAmf3MessageInterceptor();

    public void setAmf3MessageInterceptor(AMF3MessageInterceptor amf3MessageInterceptor);

    public Map<String, String> getInstantiators();

    public Converters getConverters();

    public MethodMatcher getMethodMatcher();

    public ServiceInvocationListener getInvocationListener();

    public String getInstantiator(String type);

    public ClassGetter getClassGetter();

    public XMap getExternalizersConfiguration();

    public void setExternalizersConfiguration(XMap externalizersConfiguration);

    public Externalizer getExternalizer(String type);

    public Map<String, Externalizer> getExternalizersByType();

    public Map<String, String> getExternalizersByInstanceOf();

    public Map<String, String> getExternalizersByAnnotatedWith();

    public List<Externalizer> getScannedExternalizers();

    public Class<? extends ActionScriptClassDescriptor> getActionScriptDescriptor(String type);

    public Map<String, Class<? extends ActionScriptClassDescriptor>> getAs3DescriptorsByType();

    public Map<String, String> getAs3DescriptorsByInstanceOf();

    public Class<? extends JavaClassDescriptor> getJavaDescriptor(String type);

    public Map<String, Class<? extends JavaClassDescriptor>> getJavaDescriptorsByType();

    public Map<String, String> getJavaDescriptorsByInstanceOf();

    public boolean isComponentTideEnabled(String componentName, Set<Class<?>> componentClasses, Object instance);

    public boolean isComponentTideDisabled(String componentName, Set<Class<?>> componentClasses, Object instance);

    public List<ExceptionConverter> getExceptionConverters();

    public void registerExceptionConverter(Class<? extends ExceptionConverter> exceptionConverterClass);

    public boolean hasSecurityService();

    public SecurityService getSecurityService();

    public List<TideComponentMatcher> getTideComponentMatchers();

    public Map<String, Object[]> getEnabledTideComponentsByName();

    public Map<String, Object[]> getDisabledTideComponentsByName();

    public XMap getGravityConfig();

    public Constructor<?> getMessageSelectorConstructor();

    public Externalizer setExternalizersByType(String type, String externalizerType);

    public String putExternalizersByInstanceOf(String instanceOf, String externalizerType);

    public String putExternalizersByAnnotatedWith(String annotatedWith, String externalizerType);

    public void setSecurityService(SecurityService securityService);
}
