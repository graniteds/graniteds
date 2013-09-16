/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.tide.validation;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.FaultMessage.Code;
import org.granite.client.tide.Context;
import org.granite.client.tide.server.ExceptionHandler;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.validation.InvalidValue;
import org.granite.client.validation.NotifyingValidatorFactory;
import org.granite.client.validation.ServerConstraintViolation;

/**
 * @author William DRAI
 */
public class ValidationExceptionHandler implements ExceptionHandler {

	@Override
	public boolean accepts(FaultMessage emsg) {
		return emsg.getCode().equals(Code.VALIDATION_FAILED);
	}

	@Override
	public void handle(Context context, FaultMessage emsg, TideFaultEvent faultEvent) {
		Object[] invalidValues = emsg.getExtended() != null ? (Object[])emsg.getExtended().get("invalidValues") : null;
		if (invalidValues != null) {
			Set<ConstraintViolation<?>> constraintViolations = new HashSet<ConstraintViolation<?>>();
			
			for (Object v : invalidValues) {
				InvalidValue iv = (InvalidValue)v;
				Object rootBean = context.getEntityManager().getCachedObject(iv.getRootBean(), true);
				Object leafBean = null;
				if (iv.getBean() != null) {
					leafBean = context.getEntityManager().getCachedObject(iv.getBean(), true);
					if (leafBean == null) {
						// Embedded ?
						Object bean = rootBean;
						String[] path = iv.getPath().split("\\.");
						for (int i = 0; i < path.length-1; i++)
							bean = context.getDataManager().getPropertyValue(bean, path[i]);
						leafBean = bean;
					}
				}
				
				ServerConstraintViolation violation = new ServerConstraintViolation(iv, rootBean, leafBean);
				constraintViolations.add(violation);
			}
			
			NotifyingValidatorFactory notifyingValidatorFactory = context.byType(NotifyingValidatorFactory.class);
			if (notifyingValidatorFactory == null)
			    throw new RuntimeException("No suitable validator factory defined, cannot process validation events");
			
		    notifyingValidatorFactory.getValidator().notifyConstraintViolations(null, constraintViolations);
		}
	}

}
