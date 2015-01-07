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
package org.granite.tide.spring;

import org.granite.spring.ServerFilterBeanDefinitionParser;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author William Drai
 */
public class TideDataPublishingAdviceBeanDefinitionParser implements BeanDefinitionParser {

	public static final String DATA_PUBLISHING_ADVISOR_BEAN_NAME = "org.granite.tide.spring.DataPublishingAdvisor";


	public BeanDefinition parse(Element element, ParserContext parserContext) {
	    String mode = element.getAttribute("mode");
	    if ("proxy".equals(mode))
	        AopAutoProxyConfigurer.configureAutoProxyCreator(element, parserContext);
	    else if ("aspectj".equals(mode))
	        AspectJAopAutoProxyConfigurer.configureAutoProxyCreator(element, parserContext);
		return null;
	}


	/**
	 * Inner class to just introduce an AOP framework dependency when actually in proxy mode.
	 */
	private static class AopAutoProxyConfigurer {

		public static void configureAutoProxyCreator(Element element, ParserContext parserContext) {
			AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);

			if (!parserContext.getRegistry().containsBeanDefinition(DATA_PUBLISHING_ADVISOR_BEAN_NAME)) {
				// Create the interceptor definition.
				RootBeanDefinition interceptorDef = new RootBeanDefinition(TideDataPublishingInterceptor.class);
				interceptorDef.setSource(parserContext.extractSource(element));
				interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				interceptorDef.getPropertyValues().addPropertyValue("gravity", new RuntimeBeanReference(ServerFilterBeanDefinitionParser.GRAVITY_FACTORY_BEAN_NAME));
				String postprocessorRef = element.getAttribute("data-update-postprocessor");
				if (postprocessorRef != null && postprocessorRef.trim().length() > 0)
				    interceptorDef.getPropertyValues().addPropertyValue("dataUpdatePostprocessor", new RuntimeBeanReference(postprocessorRef));

				RootBeanDefinition advisorDef = new RootBeanDefinition(TideDataPublishingAdvisor.class);
				advisorDef.setSource(parserContext.extractSource(element));
				advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				advisorDef.getPropertyValues().addPropertyValue("dataPublishingInterceptor", interceptorDef);
                String order = element.getAttribute("order");
                if (order != null && order.trim().length() > 0)
                    advisorDef.getPropertyValues().addPropertyValue("order", Integer.parseInt(order));                 

				parserContext.registerBeanComponent(new BeanComponentDefinition(advisorDef, DATA_PUBLISHING_ADVISOR_BEAN_NAME));
			}
		}
	}
	
	
    /**
     * Inner class to just introduce an AOP framework dependency when actually in proxy mode.
     */
    private static class AspectJAopAutoProxyConfigurer {

        public static void configureAutoProxyCreator(Element element, ParserContext parserContext) {
            AopNamespaceUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext, element);

            if (!parserContext.getRegistry().containsBeanDefinition(DATA_PUBLISHING_ADVISOR_BEAN_NAME)) {
                // Create the TransactionInterceptor definition.
                RootBeanDefinition aspectDef = new RootBeanDefinition(TideDataPublishingAspect.class);
                aspectDef.setSource(parserContext.extractSource(element));
                aspectDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                aspectDef.getPropertyValues().addPropertyValue("gravity", new RuntimeBeanReference(ServerFilterBeanDefinitionParser.GRAVITY_FACTORY_BEAN_NAME));
                String postprocessorRef = element.getAttribute("data-update-postprocessor");
                if (postprocessorRef != null && postprocessorRef.trim().length() > 0)
                    aspectDef.getPropertyValues().addPropertyValue("dataUpdatePostprocessor", new RuntimeBeanReference(postprocessorRef));
                String order = element.getAttribute("order");
                if (order != null && order.trim().length() > 0)
                    aspectDef.getPropertyValues().addPropertyValue("order", Integer.parseInt(order));                 

                parserContext.registerBeanComponent(new BeanComponentDefinition(aspectDef, DATA_PUBLISHING_ADVISOR_BEAN_NAME));
            }
        }
    }
}
