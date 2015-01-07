/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.validation {

	import org.granite.reflect.IAnnotatedElement;
	import org.granite.reflect.Type;

	/**
	 * Contract determining if a property can be accessed by the Bean Validation provider.
	 * This contract is called for each property that is being either validated or cascaded.
	 *
	 * A traversable resolver implementation must be thread-safe.
	 *
	 * @author Franck WOLFF
	 */
	public interface ITraversableResolver {
		
		/**
		 * Determine if the Bean Validation provider is allowed to reach the property state
		 *
		 * @param traversableObject object hosting <code>traversableProperty</code> or null
		 * 		if <code>validateValue</code> is called
		 * @param traversableProperty the traversable property.
		 * @param rootBeanType type of the root object passed to the Validator.
		 * @param pathToTraversableObject path from the root object to
		 * 		<code>traversableObject</code>
		 * 		(using the path specification defined by Bean Validator).
		 * @param elementType either <code>FIELD</code> or <code>METHOD</code>.
		 *
		 * @return <code>true</code> if the Bean Validation provider is allowed to
		 * 		reach the property state, <code>false</code> otherwise.
		 */
		function isReachable(traversableObject:Object,
							 traversableProperty:INode,
							 rootBeanType:Type,
							 pathToTraversableObject:IPath,
							 elementType:IAnnotatedElement):Boolean;
		/**
		 * Determine if the Bean Validation provider is allowed to cascade validation on
		 * the bean instance returned by the property value
		 * marked as <code>[Valid]</code>.
		 * Note that this method is called only if <code>isReachable</code> returns true
		 * for the same set of arguments and if the property is marked as <code>[Valid]</code>
		 *
		 * @param traversableObject object hosting <code>traversableProperty</code> or null
		 * 		if <code>validateValue</code> is called
		 * @param traversableProperty the traversable property.
		 * @param rootBeanType type of the root object passed to the Validator.
		 * @param pathToTraversableObject path from the root object to
		 * 		<code>traversableObject</code>
		 * 		(using the path specification defined by Bean Validator).
		 * @param elementType either <code>FIELD</code> or <code>METHOD</code>.
		 *
		 * @return <code>true</code> if the Bean Validation provider is allowed to
		 * 		cascade validation, <code>false</code> otherwise.
		 */
		function isCascadable(traversableObject:Object,
							  traversableProperty:INode,
							  rootBeanType:Type,
							  pathToTraversableObject:IPath,
							  elementType:IAnnotatedElement):Boolean;
	}
}