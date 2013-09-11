/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.messaging.amf.io.util.instantiator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.granite.messaging.service.ServiceException;

/**
 * @author Franck WOLFF
 */
public class BigDecimalInstantiator extends AbstractInstantiator<BigDecimal> {

	private static final long serialVersionUID = 1L;

	private static final String VALUE = "value";
	
	private static final List<String> orderedFields;
    static {
        List<String> of = new ArrayList<String>(1);
        of.add(VALUE);
        orderedFields = Collections.unmodifiableList(of);
    }

    public BigDecimalInstantiator() {
    }

    @Override
    public List<String> getOrderedFieldNames() {
        return orderedFields;
    }

    @Override
    public BigDecimal newInstance() {
    	BigDecimal bigDecimal = null;

        String value = (String)get(VALUE);
        
        if (value != null && value.length() <= 2) {
        	if ("0".equals(value))
        		bigDecimal = BigDecimal.ZERO;
        	else if ("1".equals(value))
    			bigDecimal = BigDecimal.ONE;
        	else if ("10".equals(value))
    			bigDecimal = BigDecimal.TEN;
        }
        
        if (bigDecimal == null) try {
            bigDecimal = new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new ServiceException("Illegal BigDecimal value: " + value, e);
        }

        return bigDecimal;
    }
}