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

package org.granite.tide.validators {

	import org.granite.validation.IPath;
	import org.granite.validation.INode;
	import org.granite.validation.IConstraint;
	import org.granite.validation.ConstraintViolation;
	import org.granite.tide.validators.InvalidValue;

	/**
	 * Represents a constraint violation received from the server.
	 * 
	 * @author William DRAI
	 */
	public class ServerConstraintViolation extends ConstraintViolation {
		
		private var _invalidValue:InvalidValue;
		private var _rootBean:Object;
		private var _bean:Object;
		private var _propertyPath:IPath;
		private var _constraint:IConstraint;
		
		/**
		 * Constructs a new <code>ServerConstraintViolation</code> instance.
		 * 
		 * @param invalidValue serialized server-side ConstraintViolation
		 * @param rootBean root bean
		 * @param bean leaf bean
		 */
		function ServerConstraintViolation(invalidValue:InvalidValue, rootBean:Object, bean:Object) {
			super(null, null, null, null, null, null, null);
			_invalidValue = invalidValue;
			_rootBean = rootBean;
			_bean = bean;
			_propertyPath = new Path(_invalidValue.path);
			_constraint = new ServerConstraint();
		}
		
		/**
		 * The error message, with all parameters resolved by the current message
		 * interpolator.
		 * 
		 * @see ValidatorFactory#messageInterpolator
		 * @see DefaultMessageInterpolator
		 * @see IMessageInterpolator
		 */
		public override function get message():String {
			return _invalidValue.message;
		}
		
		/**
		 * The raw error message key.
		 */
		public override function get messageKey():String {
			return _invalidValue.message;
		}
		
		/**
		 * The <code>IConstraint</code> that failed.
		 */
		public override function get constraint():IConstraint {
			return _constraint;
		}
		
		/**
		 * The root bean of the invalid property.
		 */
		public override function get rootBean():* {
			return _rootBean;
		}
		
		/**
		 * The root bean type of the invalid property.
		 */
		public override function get rootBeanType():* {
			return _invalidValue.beanClass;
		}
		
		/**
		 * The path from the root bean to the invalid property.
		 */
		public override function get propertyPath():IPath {
			return _propertyPath;
		}
		
		/**
		 * The property name (fields), index (collections) or key (map)
		 * of the invalid property.
		 */
		public override function get property():* {
			return null;
		}
		
		/**
		 * The leaf bean type of the invalid property.
		 */
		public override function get leafBean():* {
			return _bean;
		}
		
		/**
		 * The value of the invalid property.
		 */
		public override function get invalidValue():* {
			return _invalidValue.value;
		}
		
		/**
		 * Returns the message property of this <code>ConstraintViolation</code>
		 * instance.
		 * 
		 * @return the message property of this <code>ConstraintViolation</code>
		 * 		instance.
		 * 
		 * @see #message
		 */
		public override function toString():String {
			return message;
		}
	}
}


import org.granite.reflect.Annotation;
import org.granite.validation.IPath;
import org.granite.validation.INode;
import org.granite.validation.IConstraint;
import org.granite.validation.ValidatorFactory;

class Path implements IPath {
	
	private var _path:String;
	
	function Path(path:String):void {
		_path = path;
	}
	
	public function get nodes():Array {
		return [];
	}
	
	public function get lastNode():INode {
		return null;
	}
	
	public function toString():String {
		return _path;
	}
}

class ServerConstraint implements IConstraint {
	
	function ServerConstraint():void {		
	}
	
	public function get message():String {
		return null;
	}
	
	public function get groups():Array {
		return [];
	}
	
	public function get payload():Array {
		return [];
	}
	
	public function get properties():Array {
		return null;
	}
	
	public function get factory():ValidatorFactory {
		return null;
	}
	
	public function initialize(annotation:Annotation, factory:ValidatorFactory):void {
	}
	
	public function validate(value:*):String {
		return null;
	}
}
