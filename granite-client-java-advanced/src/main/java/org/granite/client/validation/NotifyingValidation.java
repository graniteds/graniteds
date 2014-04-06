/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.validation;

import java.io.InputStream;

import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.ValidatorFactory;
import javax.validation.bootstrap.GenericBootstrap;
import javax.validation.bootstrap.ProviderSpecificBootstrap;
import javax.validation.spi.ValidationProvider;

/**
 * @author William DRAI
 */
public class NotifyingValidation {
    
    public static NotifyingValidatorFactory buildDefaultValidatorFactory() {
        return new DefaultNotifyingValidatorFactory(Validation.buildDefaultValidatorFactory());
    }
    
    public static GenericBootstrap byDefaultProvider() {
        return new GenericBootstrapWrapper(Validation.byDefaultProvider());
    }
    
    public static <T extends Configuration<T>, U extends ValidationProvider<T>> ProviderSpecificBootstrap<T> byProvider(Class<U> providerType) {
        ProviderSpecificBootstrap<T> bootstrap = Validation.byProvider(providerType);
        return new ProviderSpecificBootstrapWrapper<T>(bootstrap);
    }
    
    public static class GenericBootstrapWrapper implements GenericBootstrap {
        
        private GenericBootstrap bootstrap;
        
        private GenericBootstrapWrapper(GenericBootstrap bootstrap) {
            this.bootstrap = bootstrap;
        }

        @Override
        public GenericBootstrap providerResolver(ValidationProviderResolver resolver) {
            this.bootstrap = bootstrap.providerResolver(resolver);
            return this;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Configuration<?> configure() {
            return new ConfigurationWrapper(bootstrap.configure());
        }        
    }
    
    public static class ProviderSpecificBootstrapWrapper<T extends Configuration<T>> implements ProviderSpecificBootstrap<T> {
        
        private ProviderSpecificBootstrap<T> bootstrap;
        
        private ProviderSpecificBootstrapWrapper(ProviderSpecificBootstrap<T> bootstrap) {
            this.bootstrap = bootstrap;
        }

        @Override
        public ProviderSpecificBootstrap<T> providerResolver(ValidationProviderResolver resolver) {
            this.bootstrap = bootstrap.providerResolver(resolver);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T configure() {
            Configuration<T> configuration = new ConfigurationWrapper<T>(bootstrap.configure());
            return (T)configuration;
        }
    }
    
    public static class ConfigurationWrapper<T extends Configuration<T>> implements Configuration<T> {

        private Configuration<T> configuration;
        
        public ConfigurationWrapper(Configuration<T> configuration) {
            this.configuration = configuration;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public T ignoreXmlConfiguration() {
            configuration = configuration.ignoreXmlConfiguration();
            return (T)this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T messageInterpolator(MessageInterpolator interpolator) {
            configuration = configuration.messageInterpolator(interpolator);
            return (T)this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T traversableResolver(TraversableResolver resolver) {
            configuration = configuration.traversableResolver(resolver);
            return (T)this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
            configuration = configuration.constraintValidatorFactory(constraintValidatorFactory);
            return (T)this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T addMapping(InputStream stream) {
            configuration = configuration.addMapping(stream);
            return (T)this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T addProperty(String name, String value) {
            configuration = configuration.addProperty(name, value);
            return (T)this;
        }
        
        public Configuration<T> providerSpecific() {
            return configuration;
        }

        @Override
        public MessageInterpolator getDefaultMessageInterpolator() {
            return configuration.getDefaultMessageInterpolator();
        }
        
        @Override
        public TraversableResolver getDefaultTraversableResolver() {
            return configuration.getDefaultTraversableResolver();
        }
        
        @Override
        public ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
            return configuration.getDefaultConstraintValidatorFactory();
        }
        
        @Override
        public NotifyingValidatorFactory buildValidatorFactory() {
            ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
            return new DefaultNotifyingValidatorFactory(validatorFactory);
        }
    }
}
