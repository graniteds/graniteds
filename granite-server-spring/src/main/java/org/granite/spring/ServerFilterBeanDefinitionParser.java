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
package org.granite.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author William Drai
 */
public class ServerFilterBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String SERVER_FILTER_BEAN_NAME = "org.granite.spring.ServerFilter";

    private static final String GRANITE_CONFIG_BEAN_NAME = "org.granite.spring.SpringGraniteConfig";
    
    public static final String GRAVITY_FACTORY_BEAN_NAME = "org.granite.spring.gravityFactory";

    private static final String DEFAULT_HANDLER_MAPPING_CLASS_NAME = "org.springframework.web.servlet.handler.SimpleUrlHandlerMapping";


    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        CompositeComponentDefinition componentDefinition = new CompositeComponentDefinition(element.getLocalName(),
            parserContext.extractSource(element));
        parserContext.pushContainingComponent(componentDefinition);

        element.setAttribute(ID_ATTRIBUTE, SERVER_FILTER_BEAN_NAME);
        
        mapOptionalAttributes(element, parserContext, builder, "tide", "type", "use-long", "use-big-decimal", "use-big-integer", "enable-exception-logging");
        
        Object source = parserContext.extractSource(element);

        ManagedList<String> roles = new ManagedList<String>();
        roles.setSource(source);
        List<Element> rolesElements = DomUtils.getChildElementsByTagName(element, "tide-roles");
        for (Element rolesElement : rolesElements) {
            List<Element> valueElements = DomUtils.getChildElementsByTagName(rolesElement, "value");
            for (Element valueElement : valueElements)
            	roles.add(valueElement.getTextContent());
        }
        if (!roles.isEmpty())
        	builder.addPropertyValue("tideRoles", roles);
        
        ManagedList<String> tideAnnotations = new ManagedList<String>();
        tideAnnotations.setSource(source);
        List<Element> tideAnnotationsElements = DomUtils.getChildElementsByTagName(element, "tide-annotations");
        for (Element tideAnnotationsElement : tideAnnotationsElements) {
            List<Element> valueElements = DomUtils.getChildElementsByTagName(tideAnnotationsElement, "value");
            for (Element valueElement : valueElements)
                tideAnnotations.add(valueElement.getTextContent());
        }
        builder.addPropertyValue("tideAnnotations", tideAnnotations);
        
        ManagedList<String> tideInterfaces = new ManagedList<String>();
        tideInterfaces.setSource(source);
        List<Element> tideInterfacesElements = DomUtils.getChildElementsByTagName(element, "tide-interfaces");
        for (Element tideInterfacesElement : tideInterfacesElements) {
            List<Element> valueElements = DomUtils.getChildElementsByTagName(tideInterfacesElement, "value");
            for (Element valueElement : valueElements)
            	tideInterfaces.add(valueElement.getTextContent());
        }
        builder.addPropertyValue("tideInterfaces", tideInterfaces);
        
        ManagedList<String> tideNames = new ManagedList<String>();
        tideNames.setSource(source);
        List<Element> tideNamesElements = DomUtils.getChildElementsByTagName(element, "tide-names");
        for (Element tideNamesElement : tideNamesElements) {
            List<Element> valueElements = DomUtils.getChildElementsByTagName(tideNamesElement, "value");
            for (Element valueElement : valueElements)
                tideNames.add(valueElement.getTextContent());
        }
        builder.addPropertyValue("tideNames", tideNames);
        
        ManagedList<String> tideTypes = new ManagedList<String>();
        tideTypes.setSource(source);
        List<Element> tideTypesElements = DomUtils.getChildElementsByTagName(element, "tide-types");
        for (Element tideTypesElement : tideTypesElements) {
            List<Element> valueElements = DomUtils.getChildElementsByTagName(tideTypesElement, "value");
            for (Element valueElement : valueElements)
            	tideTypes.add(valueElement.getTextContent());
        }
        builder.addPropertyValue("tideTypes", tideTypes);

        ManagedList<String> exceptionConverters = new ManagedList<String>();
        exceptionConverters.setSource(source);
        List<Element> exceptionConvertersElements = DomUtils.getChildElementsByTagName(element, "exception-converters");
        for (Element exceptionConvertersElement : exceptionConvertersElements) {
            List<Element> valueElements = DomUtils.getChildElementsByTagName(exceptionConvertersElement, "value");
            for (Element valueElement : valueElements)
            	exceptionConverters.add(valueElement.getTextContent());
        }
        builder.addPropertyValue("exceptionConverters", exceptionConverters);
        
        Element amf3MessageInterceptor = DomUtils.getChildElementByTagName(element, "amf3-message-interceptor");
        if (amf3MessageInterceptor != null)
        	builder.addPropertyReference("amf3MessageInterceptor", amf3MessageInterceptor.getTextContent());
        
        configureGraniteDS(element, parserContext, DomUtils.getChildElementByTagName(element, "granite-config"));
        
        registerHandlerMappings(element, parserContext, element.getAttribute("url-pattern"));

        parserContext.popAndRegisterContainingComponent();
    }

    @Override
    protected String getBeanClassName(Element element) {
        return ServerFilter.class.getName();
    }

    private void configureGraniteDS(Element parent, ParserContext parserContext, Element graniteConfigElement) {
        if (parserContext.getRegistry().containsBeanDefinition(GRANITE_CONFIG_BEAN_NAME))
        	return;
        
        Element source = graniteConfigElement != null ? graniteConfigElement : parent;

        BeanDefinitionBuilder graniteConfigBuilder = BeanDefinitionBuilder.genericBeanDefinition(SpringGraniteConfig.class);
        registerInfrastructureComponent(source, parserContext, graniteConfigBuilder, GRANITE_CONFIG_BEAN_NAME);
        
        BeanDefinitionBuilder gravityFactoryBuilder = BeanDefinitionBuilder.rootBeanDefinition(SpringGravityFactoryBean.class);
        registerInfrastructureComponent(source, parserContext, gravityFactoryBuilder, GRAVITY_FACTORY_BEAN_NAME);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void registerHandlerMappings(Element parent, ParserContext parserContext, String urlPattern) {
        if (urlPattern == null)
        	return;
        
        BeanDefinitionBuilder handlerMappingBuilder = BeanDefinitionBuilder.genericBeanDefinition(DEFAULT_HANDLER_MAPPING_CLASS_NAME);
        
        Map mappings = new HashMap();
            mappings.put(urlPattern, parent.getAttribute(ID_ATTRIBUTE));
            
        handlerMappingBuilder.addPropertyValue("urlMap", mappings);
        registerInfrastructureComponent(parent, parserContext, handlerMappingBuilder, 
    		parent.getAttribute(ID_ATTRIBUTE) + "_handlerMapping");
    }

    
    // From Spring-Flex
    
    static void registerInfrastructureComponent(Element element, ParserContext parserContext, BeanDefinitionBuilder componentBuilder, String beanName) {
        componentBuilder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
        componentBuilder.getRawBeanDefinition().setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        parserContext.registerBeanComponent(new BeanComponentDefinition(componentBuilder.getBeanDefinition(), beanName));
    }

    static void mapOptionalAttributes(Element element, ParserContext parserContext, BeanDefinitionBuilder builder, String... attrs) {
        for (String attr : attrs) {
            String value = element.getAttribute(attr);
            if (StringUtils.hasText(value)) {
                String propertyName = Conventions.attributeNameToPropertyName(attr);
                if (validateProperty(element, parserContext, propertyName, attr)) {
                    builder.addPropertyValue(propertyName, value);
                }
            }
        }
    }

    private static boolean validateProperty(Element element, ParserContext parserContext, String propertyName, String attr) {
        if (!StringUtils.hasText(propertyName)) {
            parserContext.getReaderContext().error(
                "Illegal property name trying to convert from attribute '" + attr + "' : cannot be null or empty.",
                parserContext.extractSource(element));
            return false;
        }
        return true;
    }
}
