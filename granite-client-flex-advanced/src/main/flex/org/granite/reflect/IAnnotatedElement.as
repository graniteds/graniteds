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
package org.granite.reflect {

	/**
	 * This interface is implemented by all reflection API classes that describe an element supporting
	 * metadata annotations.
	 * 
	 * <p>
	 * Possible annotated elements in ActionScript 3 are classes/interfaces, constansts, variables
	 * accessors (get/set) and methods. Annotations on constructors are not supported.
	 * </p>
	 * 
	 * @author Franck WOLFF
	 */
	public interface IAnnotatedElement {
		
		/**
		 * An array that contains all annotations directly attached to this <code>IAnnotatedElement</code>.
		 * 
		 * @see Annotation
		 */
		function get annotations():Array;
		
		/**
		 * Returns an array (possibly empty) that contains all annotations attached to this
		 * <code>IAnnotatedElement</code>.
		 * 
		 * @param recursive should we look for annotations attached to overridden elements (non-static
		 * 		accessors or methods) or superclasses/superinterfaces ?
		 * @param pattern a regexp expression used as a filter. Default is to eliminate all annotations
		 * 		prefixed with a double '_' character. You may use this parameter to find all
		 * 		annotations with a specific name.
		 * @return an array that contains all annotations attached to this <code>IAnnotatedElement</code>.
		 * 
		 * @see Annotation
		 */
		function getAnnotations(recursive:Boolean = false, pattern:String = "^_?[^_]"):Array;
		
		
		/**
		 * Returns the first <code>Annotation</code> whose name is equal to the supplied type parameter,
		 * attached to this <code>IAnnotatedElement</code>.
		 * 
		 * @param type the name of the annotation to look for.
		 * @param recursive should we look for annotations attached to overridden elements (non-static
		 * 		accessors or methods) or superclasses/superinterfaces ?
		 * @return an <code>Annotation</code> instance or null if it cannot be found.
		 * 
		 * @see Annotation
		 */
		function getAnnotation(type:String, recursive:Boolean = false):Annotation;
		
		/**
		 * Tests if an <code>Annotation</code>, whose name is equal to the supplied type parameter, is attached
		 * to this <code>IAnnotatedElement</code>.
		 * 
		 * @param type the name of the annotation to look for.
		 * @param recursive should we look for annotations attached to overridden elements (non-static
		 * 		accessors or methods) or superclasses/superinterfaces ?
		 * @return <code>true</code> if the annotation can be found, <code>false</code> otherwise.
		 * 
		 * @see Annotation
		 */
		function isAnnotationPresent(type:String, recursive:Boolean = false):Boolean;
		
		/**
		 * @private
		 */
		function get declaredBy():Type;
		
		/**
		 * @private
		 */
		function equals(o:*):Boolean;
	}
}