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

package org.granite.spring;

import org.granite.spring.security.SecurityServiceBeanDefinitionParser;
import org.granite.tide.spring.TideDataPublishingAdviceBeanDefinitionParser;
import org.granite.tide.spring.TideIdentityBeanDefinitionParser;
import org.granite.tide.spring.TidePersistenceBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;


public class GranitedsNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("server-filter", new ServerFilterBeanDefinitionParser());
        registerBeanDefinitionParser("security-service", new SecurityServiceBeanDefinitionParser());
        registerBeanDefinitionParser("remote-destination", new RemoteDestinationBeanDefinitionParser());
        registerBeanDefinitionParser("messaging-destination", new MessagingDestinationBeanDefinitionParser());
        registerBeanDefinitionParser("jms-topic-destination", new JmsTopicDestinationBeanDefinitionParser());
        registerBeanDefinitionParser("activemq-topic-destination", new ActiveMQTopicDestinationBeanDefinitionParser());
        registerBeanDefinitionParser("tide-persistence", new TidePersistenceBeanDefinitionParser());
        registerBeanDefinitionParser("tide-identity", new TideIdentityBeanDefinitionParser());
        registerBeanDefinitionParser("tide-data-publishing-advice", new TideDataPublishingAdviceBeanDefinitionParser());
    }

}