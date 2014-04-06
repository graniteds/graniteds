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

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

/**
 * @author William DRAI
 */
public class DefaultNotifyingValidatorFactory implements NotifyingValidatorFactory {
    
    private final ValidatorFactory validatorFactory;
    private final ValidationNotifier validationNotifier = new ValidationNotifier();
    
    public DefaultNotifyingValidatorFactory(ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    public NotifyingValidator getValidator() {
        return new DefaultNotifyingValidator(validatorFactory.getValidator(), validationNotifier);
    }

    public ValidatorContext usingContext() {
        return validatorFactory.usingContext();
    }

    public MessageInterpolator getMessageInterpolator() {
        return validatorFactory.getMessageInterpolator();
    }

    public TraversableResolver getTraversableResolver() {
        return validatorFactory.getTraversableResolver();
    }

    public ConstraintValidatorFactory getConstraintValidatorFactory() {
        return validatorFactory.getConstraintValidatorFactory();
    }

    public <T> T unwrap(Class<T> type) {
        return validatorFactory.unwrap(type);
    }

}
