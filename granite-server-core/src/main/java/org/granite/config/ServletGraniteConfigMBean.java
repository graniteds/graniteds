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
package org.granite.config;

import java.io.IOException;

import org.granite.jmx.MBean;
import org.granite.jmx.MBeanAttribute;
import org.granite.jmx.MBeanOperation;
import org.granite.jmx.MBeanOperation.Impact;

/**
 * @author Franck WOLFF
 */
@MBean(description="MBean used for GraniteConfig operations")
public interface ServletGraniteConfigMBean {

	///////////////////////////////////////////////////////////////////////////
	// Attributes.
	
    @MBeanAttribute(description="Does this config use auto scanning?")
	public boolean getScan();
	
    @MBeanAttribute(description="The custom (webapp specific) granite-config.xml file location")
    public String getCustomConfigPath();
	
    @MBeanAttribute(description="The AMF3Serializer class")
    public String getAmf3SerializerClass();
	
    @MBeanAttribute(description="The AMF3Deserializer class")
    public String getAmf3DeserializerClass();
    
    @MBeanAttribute(description="The AMF3MessageInterceptor class")
    public String getAmf3MessageInterceptorClass();
    
    @MBeanAttribute(description="The MethodMatcher class")
    public String getMethodMatcherClass();

    @MBeanAttribute(description="The ServiceInvocationListener class")
    public String getServiceInvocationListenerClass();
    
    @MBeanAttribute(description="The ClassGetter class")
    public String getClassGetterClass();
    
    @MBeanAttribute(description="The SecurityService class")
    public String getSecurityServiceClass();
    
    @MBeanAttribute(description="The MessageSelector class")
    public String getMessageSelectorClass();
    
	///////////////////////////////////////////////////////////////////////////
	// Operations.
    
    @MBeanOperation(
    	description="Shows the custom (webapp specific) granite-config.xml file content",
    	impact=Impact.INFO
    )
	public String showCustomConfig() throws IOException;

    @MBeanOperation(
    	description="Shows the standard (built-in) granite-config.xml file content",
    	impact=Impact.INFO
    )
	public String showStandardConfig() throws IOException;

    @MBeanOperation(
    	description="Shows the list of all configured type converters",
    	impact=Impact.INFO
    )
	public String showConverters();

    @MBeanOperation(
    	description="Shows the list of all configured instantiators",
    	impact=Impact.INFO
    )
	public String showInstantiators();
    
    @MBeanOperation(
    	description="Shows the list of all configured exception converters",
    	impact=Impact.INFO
    )
    public String showExceptionConverters();
    
    @MBeanOperation(
    	description="Shows scanned externalizers",
    	impact=Impact.INFO
    )
    public String showScannedExternalizers();

    @MBeanOperation(
    	description="Shows which externalizer is used for a given serialized object (dynamic)",
    	impact=Impact.INFO
    )
    public String showExternalizersByType();

    @MBeanOperation(
    	description="Shows which externalizer is used for a given serialized instance (configured)",
    	impact=Impact.INFO
    )
    public String showExternalizersByInstanceOf();

    @MBeanOperation(
    	description="Shows which externalizer is used for a given serialized annotated object (configured)",
    	impact=Impact.INFO
    )
    public String showExternalizersByAnnotatedWith();

    @MBeanOperation(
    	description="Shows which Java descriptor is used for a given serialized object (dynamic)",
    	impact=Impact.INFO
    )
    public String showJavaDescriptorsByType();

    @MBeanOperation(
    	description="Shows which Java descriptor is used for a given serialized instance (configured)",
    	impact=Impact.INFO
    )
    public String showJavaDescriptorsByInstanceOf();

    @MBeanOperation(
    	description="Shows which ActionScript3 descriptor is used for a given serialized object (dynamic)",
    	impact=Impact.INFO
    )
    public String showAs3DescriptorsByType();

    @MBeanOperation(
    	description="Shows which ActionScript3 descriptor is used for a given serialized instance (configured)",
    	impact=Impact.INFO
    )
    public String showAs3DescriptorsByInstanceOf();

    @MBeanOperation(
    	description="Shows enabled Tide components (configured)",
    	impact=Impact.INFO
    )
    public String showEnabledTideComponentsByName();

    @MBeanOperation(
    	description="Shows disabled Tide components (configured)",
    	impact=Impact.INFO
    )
    public String showDisabledTideComponentsByName();

    @MBeanOperation(
    	description="Shows Tide component matchers (configured)",
    	impact=Impact.INFO
    )
    public String showTideComponentMatchers();
    
	@MBeanOperation(
    	description="Reload the granite-config.xml file",
    	impact=Impact.ACTION
    )
    public void reload();
}
