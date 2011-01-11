/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.tide.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author William Drai
 */
public class TideIdentityBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    	
        element.setAttribute(ID_ATTRIBUTE, "identity");
    	
    	builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        
        String aclService = element.getAttribute("acl-service");
        if (aclService != null && aclService.trim().length() > 0)
        	builder.addPropertyReference("aclService", aclService);
        
        String sidRetrievalStrategy = element.getAttribute("sid-retrieval-strategy");
        if (sidRetrievalStrategy != null && sidRetrievalStrategy.trim().length() > 0)
        	builder.addPropertyReference("sidRetrievalStrategy", sidRetrievalStrategy);
        
        String objectIdentityRetrievalStrategy = element.getAttribute("object-identity-retrieval-strategy");
        if (objectIdentityRetrievalStrategy != null && objectIdentityRetrievalStrategy.trim().length() > 0)
        	builder.addPropertyReference("objectIdentityRetrievalStrategy", objectIdentityRetrievalStrategy);
    }

    @Override
    protected String getBeanClassName(Element element) {
    	// Determine Spring Security version
    	boolean spring3 = false;
    	try {
    		getClass().getClassLoader().loadClass("org.springframework.security.access.AccessDeniedException");
    		spring3 = true;
    	}
    	catch (ClassNotFoundException e) {
    		// Assume Spring 2
    	}
    	
        String aclService = element.getAttribute("acl-service");
        return "org.granite.tide.spring.security." 
        	+ (aclService != null && aclService.trim().length() > 0 ? "AclIdentity" : "Identity") 
        	+ (spring3 ? "3" : "");
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
