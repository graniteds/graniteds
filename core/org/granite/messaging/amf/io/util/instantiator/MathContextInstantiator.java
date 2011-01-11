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

package org.granite.messaging.amf.io.util.instantiator;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Franck WOLFF
 */
public class MathContextInstantiator extends AbstractInstantiator<MathContext> {

	private static final long serialVersionUID = 1L;

	private static final String PRECISION = "precision";
	private static final String ROUNDING_MODE = "roundingMode";
	
	private static final List<String> orderedFields;
    static {
        List<String> of = new ArrayList<String>(1);
        of.add(PRECISION);
        of.add(ROUNDING_MODE);
        orderedFields = Collections.unmodifiableList(of);
    }

    public MathContextInstantiator() {
    }

    @Override
    public List<String> getOrderedFieldNames() {
        return orderedFields;
    }

    @Override
    public MathContext newInstance() {
    	int precision = ((Integer)get(PRECISION)).intValue();
    	RoundingMode roundingMode = (RoundingMode)get(ROUNDING_MODE);

        return new MathContext(precision, roundingMode);
    }
}