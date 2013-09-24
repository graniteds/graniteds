/*
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
package org.granite.reflect.visitor {

	/**
	 * A "two-phases" visitor pattern interface. Exploring a bean is a sequence of
	 * two-phases processes: first, the visitor is asked if it accepts to visit each
	 * property of a bean; second, for each accepted property, the property is visited
	 * and cascaded.
	 * 
	 * @author Franck WOLFF
	 * 
	 * @see Guide
	 */
	public interface IVisitor {
		
		/**
		 * Returns true if this <code>IVisitor</code> implementation "accepts"
		 * to visit the supplied visitable, false otherwise.
		 * 
		 * @param visitable the <code>Visitable</code> to be accepted or not.
		 * @return true if this <code>IVisitor</code> implementation "accepts"
		 * 		to visit the supplied visitable, false otherwise.
		 */
		function accept(visitable:Visitable):Boolean;

		/**
		 * Returns true if this <code>IVisitor</code> implementation, after
		 * visiting the supplied visitable, wants it to be cascaded (so its
		 * properties will be visited), false otherwise.
		 * 
		 * <p>
		 * This method is not called if this <code>IVisitor</code> implementation
		 * didn't return true after the call to the "accept" method. 
		 * </p>
		 * 
		 * @param visitable the <code>Visitable</code> to be visited and then
		 * 		cascaded or not.
		 * @return true if this <code>IVisitor</code> implementation wants
		 * 		to cascade the supplied visitable value, false otherwise.
		 */
		function visit(visitable:Visitable):Boolean;
	}
}