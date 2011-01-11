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

package org.granite.jmx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Retention(value=RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
/**
 * The MBeanParameter annotation may be placed on any method parameter that
 * will act as an OpenMBeanOperation.
 * 
 * @author Franck WOLFF
 */
public @interface MBeanParameter {

	/**
	 * The parameter name.
	 * 
	 * @return the parameter name.
	 */
	String name();

	/**
	 * The parameter description.
	 * 
	 * @return the parameter description.
	 */
	String description();
}
