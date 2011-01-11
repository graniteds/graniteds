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

import org.granite.logging.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author William Drai
 */
public class TidePersistenceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {
	
	private static final Logger log = Logger.getLogger(TidePersistenceBeanDefinitionParser.class);

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    	
    	builder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);
    	
        // Set the default ID if necessary
        if (!StringUtils.hasText(element.getAttribute(ID_ATTRIBUTE)))
            element.setAttribute(ID_ATTRIBUTE, "org.granite.tide.spring.SpringPersistenceManager");
        
    	builder.setScope("request");
    	
    	builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    	
        String transactionManager = element.getAttribute("transaction-manager");
        if (transactionManager != null)
        	builder.addConstructorArgReference(transactionManager);
        else {
        	log.info("No transaction-manager specified, use 'transactionManager' default");
        	builder.addConstructorArgReference("transactionManager");
        }
    }

    @Override
    protected String getBeanClassName(Element element) {
        return "org.granite.tide.spring.SpringPersistenceManager";
    }
}
