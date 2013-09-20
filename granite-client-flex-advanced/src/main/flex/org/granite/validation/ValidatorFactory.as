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

package org.granite.validation {

	import flash.utils.Dictionary;
	
	import org.granite.reflect.Type;
	import org.granite.util.ClassUtil;
	import org.granite.util.DefaultNamespaceResolver;
	import org.granite.util.INamespaceResolver;
	import org.granite.validation.constraints.AssertFalse;
	import org.granite.validation.constraints.AssertTrue;
	import org.granite.validation.constraints.DecimalMax;
	import org.granite.validation.constraints.DecimalMin;
	import org.granite.validation.constraints.Digits;
	import org.granite.validation.constraints.Future;
	import org.granite.validation.constraints.Max;
	import org.granite.validation.constraints.Min;
	import org.granite.validation.constraints.NotNull;
	import org.granite.validation.constraints.Null;
	import org.granite.validation.constraints.Past;
	import org.granite.validation.constraints.Pattern;
	import org.granite.validation.constraints.Size;

	/**
	 * The <code>ValidatorFactory</code> class is the entry point of the validation
	 * framework. It serves as validator instance factory and holds all
	 * specific validation configuration such as constraint registration, message
	 * interpolator configuration, traversable resolver configuration, namespace
	 * resolver configuration, etc.
	 * 
	 * <p>
	 * All standard constraints (as specified by the JSR-303) are automatically
	 * registered and may be used in your bean without any specific configuration.
	 * For user defined constraints, they must be registered with the
	 * <code>registerConstraintClass</code> method, otherwise they will be ignored.
	 * </p>
	 * 
	 * <p>
	 * A typical usage would be:
	 * </p>
	 * 
	 * <listing>
	 * 
	 * public class MyAnnotatedBean {
	 * 
	 *     [NotNull] [Size(min="4")]
	 *     public var property:String;
	 * }
	 * <hr />
	 * var factory:ValidatorFactory = ValidatorFactory.getInstance();
	 * 
	 * var violations:Array = factory.validate(myAnnotatedBean);
	 * violations = factory.validateProperty(myAnnotatedBean, "property");
	 * violations = factory.validateValue(Type.forClass(MyAnnotatedBean), "property", "value");
	 * </listing>
	 * 
	 * @author Franck WOLFF
	 * 
	 * @see FormValidator
	 */
	public class ValidatorFactory {
		
		///////////////////////////////////////////////////////////////////////
		// Static fields.

		private static const _DEFAULT_CONSTRAINTS:Array = [
			AssertFalse,
			AssertTrue,
			DecimalMax,
			DecimalMin,
			Digits,
			Future,
			Max,
			Min,
			NotNull,
			Null,
			Past,
			Pattern,
			Size
		];
		
		private static var _defaultValidatorFactory:ValidatorFactory = null;
		
		///////////////////////////////////////////////////////////////////////
		// Instance fields.
		
		private var _constraints:Dictionary = new Dictionary();
		private var _defaultValidatorClass:Class = GenericValidator;
		private var _validators:Dictionary = new Dictionary(true);
		private var _namespaceResolver:INamespaceResolver = new DefaultNamespaceResolver(
			"org.granite.validation.groups.*", false
		);
		
		private var _traversableResolver:ITraversableResolver = new DefaultTraversableResolver();
		private var _messageInterpolator:IMessageInterpolator = new DefaultMessageInterpolator();
		
		private var _visitorHandlers:Array;
		private var _dispatchEvents:Boolean = true;
		
		///////////////////////////////////////////////////////////////////////
		// Constructor.
		
		/**
		 * @private
		 */
		function ValidatorFactory() {
			// Register standard constraints.
			for each (var constraint:Class in _DEFAULT_CONSTRAINTS)
				_constraints[ClassUtil.getSimpleName(constraint)] = constraint;
		}
		
		/**
		 * Returns a unique instance of this <code>ValidatorFactory</code> class.
		 */
		public static function getInstance():ValidatorFactory {
			if (_defaultValidatorFactory == null)
				_defaultValidatorFactory = new ValidatorFactory();
			return _defaultValidatorFactory;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Properties.
		
		/**
		 * The default <code>IValidator</code> implementation used for validation
		 * operations. Default value is the <code>GenericValidator</code> class.
		 * 
		 * @see GenericValidator
		 */
		public function get defaultValidatorClass():Class {
			return _defaultValidatorClass;
		}
		public function set defaultValidatorClass(value:Class):void {
			if (value == null)
				throw new ArgumentError("defaultValidator cannot be null");
			
			if (!Type.forClass(value).isSubclassOf(Type.forClass(IValidator)))
				throw new ArgumentError("defaultValidator must implement the " + IValidator + " interface");

			_defaultValidatorClass = value;
		}
		
		/**
		 * The <code>INamespaceResolver</code> implementation used for validation
		 * operations. Default value is the <code>DefaultNamespaceResolver</code> class.
		 * 
		 * <p>
		 * Sample usage (the "g" namespace is used in the Size annotation):
		 * </p>
		 * 
		 * <listing>
		 * 
		 * package path.to.groups {
		 *     public interface MyGroup {}
		 * }
		 * <hr />
		 * var validatorFactory = ValidatorFactory.getInstance();
		 * validatorFactory.namespaceResolver.registerNamespace("g", "path.to.groups.*");
		 * <hr />
		 *  // instead of [Size(min="2", groups="path.to.groups.MyGroup")]
		 * [Size(min="2", groups="g:MyGroup")]
		 * public var property:String;</listing>
		 * 
		 * <p>
		 * Note that the built-in <code>Default</code> group package is registered in the
		 * default namespace, so you may use it without prefix (eg:
		 * <code>[Size(min="2", groups="Default")]</code>).
		 * </p>
		 * 
		 * @see org.granite.util.DefaultNamespaceResolver
		 */
		public function get namespaceResolver():INamespaceResolver {
			return _namespaceResolver;
		}		
		public function set namespaceResolver(value:INamespaceResolver):void {
			if (value == null)
				throw new ArgumentError("namespaceResolver cannot be null");
			_namespaceResolver = value;
		}
		
		/**
		 * The default <code>IMessageInterpolator</code> implementation used for
		 * validation operations. Default value is the
		 * <code>DefaultMessageInterpolator</code> class.
		 * 
		 * @see DefaultMessageInterpolator
		 */
		public function get messageInterpolator():IMessageInterpolator {
			return _messageInterpolator;
		}		
		public function set messageInterpolator(value:IMessageInterpolator):void {
			if (value == null)
				throw new ArgumentError("messageInterpolator cannot be null");
			_messageInterpolator = value;
		}
		
		
		/**
		 * The default <code>ITraversableResolver</code> implementation used for
		 * validation operations. Default value is the
		 * <code>DefaultTraversableResolver</code> class.
		 * 
		 * @see DefaultTraversableResolver
		 */
		public function get traversableResolver():ITraversableResolver {
			return _traversableResolver;
		}
		public function set traversableResolver(value:ITraversableResolver):void {
			_traversableResolver = value;
		}
		
		/**
		 * The specific <code>IHandler</code> classes to be used when visitating
		 * a bean for validation. Default is null: no specific IHandler classes
		 * other than the default ones.
		 * 
		 * @see org.granite.reflect.visitor.IHandler
		 * @see org.granite.reflect.visitor.Guide
		 */
		public function get visitorHandlers():Array {
			return _visitorHandlers;
		}
		public function set visitorHandlers(value:Array):void {
			_visitorHandlers = value;
		}
		
		/**
		 * Should the validation process dispatch validation events? Default is
		 * <code>true</code>.
		 */
		public function get dispatchEvents():Boolean {
			return _dispatchEvents;
		}
		public function set dispatchEvents(value:Boolean):void {
			_dispatchEvents = value;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Constraints.
		
		/**
		 * Register a <code>IConstraint</code> implementation class, so
		 * annotations with its unqualified class name will be recognized as
		 * constraint annotations. This method also allow to override a built-in
		 * constraint definition with a specific implementation.
		 * 
		 * @param constraintClass the <code>IConstraint</code> implementation
		 * 		class to be registered.
		 */
		public function registerConstraintClass(constraintClass:Class):void {
			if (!Type.forClass(constraintClass).isSubclassOf(Type.forClass(IConstraint)))
				throw new ArgumentError("Parameter constraintClass must implement the " + IConstraint + " interface");
			
			// Register (and possibly override) the constraint with the unqualified name
			// of its class.
			_constraints[ClassUtil.getSimpleName(constraintClass)] = constraintClass;
		}

		/**
		 * Unregister a <code>IConstraint</code> implementation class, so
		 * annotations with its unqualified class name won't be recognized as
		 * constraint annotations anymore. This method also allow to unregister a
		 * built-in constraint definition.
		 * 
		 * @param constraintClass the <code>IConstraint</code> implementation
		 * 		class to be unregistered.
		 */
		public function unregisterConstraintClass(constraintClass:Class):void {
			// Unregister the constraint with the unqualified name of its class.
			delete _constraints[ClassUtil.getSimpleName(constraintClass)];
		}
		
		/**
		 * Returns a <code>IConstraint</code> implementation class, based on
		 * its unqualified class name.
		 * 
		 * @param name the unqualified class name of a <code>IConstraint</code>
		 * 		implementation class.
		 * 
		 * @return the corresponding <code>IConstraint</code> implementation
		 * 		class, or null if no such constraint is registered.
		 */
		public function getConstraintClass(name:String):Class {
			return _constraints[name];
		}
		
		///////////////////////////////////////////////////////////////////////
		// Validators.
		
		/**
		 * Register a <code>IValidator</code> implementation class for a specific
		 * bean type, so this <code>IValidator</code> implementation will be used
		 * when encountering a bean of this type.
		 * 
		 * @param type the type of the bean that should be validated by the supplied
		 * 		<code>IValidator</code> implementation.
		 * @param validatorClass the <code>IValidator</code> implementation
		 * 		class to be registered.
		 */
		public function registerValidatorClass(type:Type, validatorClass:Class):void {
			if (type == null || validatorClass == null)
				throw new ArgumentError("Parameters type and validatorClass cannot be null");
			
			if (!Type.forClass(validatorClass).isSubclassOf(Type.forClass(IValidator)))
				throw new ArgumentError("Parameter validatorClass must implement the " + IValidator + " interface");

			_validators[type] = validatorClass;
		}

		/**
		 * Unregister a <code>IValidator</code> implementation class for a specific
		 * bean type, so no specific <code>IValidator</code> implementation will be
		 * used when encountering a bean of this type.
		 * 
		 * @param type the type of the bean that shouldn't be validated by a supplied
		 * 		<code>IValidator</code> implementation anymore.
		 */
		public function unregisterValidatorClass(type:Type):void {
			if (type == null)
				throw new ArgumentError("Parameter type cannot be null");
			delete _validators[type];
		}
		
		/**
		 * Returns a <code>IValidator</code> implementation class, based on
		 * bean type.
		 * 
		 * @param type the type of a bean.
		 * 
		 * @return a specific <code>IValidator</code> implementation class,
		 * 		or the default <code>IValidator</code> implementation class
		 * 		if no specific validator can be found.
		 */
		public function getValidatorClass(type:Type):Class {
			if (type == null)
				throw new ArgumentError("Parameter type cannot be null");

			if (_validators.hasOwnProperty(type))
				return (_validators[type] as Class);
			
			return _defaultValidatorClass;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Utilities.
		
		/**
		 * Returns a new <code>IValidator</code> implementation instance, based
		 * on the supplied bean type.
		 * 
		 * @param the bean type for which a validator instance is created.
		 * 
		 * @return a new validator instance.
		 * 
		 * @see GenericValidator
		 */
		public function getValidator(type:Type):IValidator {
			var validatorClass:Class = getValidatorClass(type);
			return new validatorClass(this);
		}

		/**
		 * Validate a bean. This method is a shortcut for:
		 * 
		 * <listing>
		 * var validator:IValidator = getValidator(Type.forInstance(bean));
		 * var violations:Array = validator.validate(bean, groups);
		 * </listing>
		 * 
		 * @param bean the bean to be validated.
		 * @param groups an array of groups targeted for validation (default is
		 * 		null, meaning that the <code>Default</code> group will be used).
		 * 
		 * @return an array of <code>ConstraintViolation</code>, possibly null or
		 * 		empty.
		 * 
		 * @see IValidator
		 * @see ConstraintViolation
		 */
		public function validate(bean:*, groups:Array = null):Array {
			if (bean == null || bean == undefined)
				return null;

			var validator:IValidator = getValidator(Type.forInstance(bean));
			return validator.validate(bean, groups);
		}

		/**
		 * Validates all constraints placed on the property of <code>bean</code>
		 * named <code>propertyName</code>. This method is a shortcut for:
		 * 
		 * <listing>
		 * var validator:IValidator = getValidator(Type.forInstance(bean));
		 * var violations:Array = validator.validateProperty(bean, propertyName, groups);
		 * </listing>
		 * 
		 * @param bean the bean holding the property to validate.
		 * @param propertyName the property name to validate.
		 * @param groups an array of groups targeted for validation (default is
		 * 		null, meaning that the <code>Default</code> group will be used).
		 * 
		 * @return an array of <code>ConstraintViolation</code>, possibly null or
		 * 		empty.
		 * 
		 * @see IValidator
		 * @see ConstraintViolation
		 */
		public function validateProperty(bean:Object, propertyName:String, groups:Array = null):Array {
			if (bean == null)
				return null;

			var validator:IValidator = getValidator(Type.forInstance(bean));
			return validator.validateProperty(bean, propertyName, groups);
		}

		/**
		 * Validates all constraints placed on the property named <code>propertyName</code>
		 * of the class <code>type</code> would the property value be <code>value</code>.
		 * This method is a shortcut for:
		 * 
		 * <listing>
		 * var validator:IValidator = getValidator(type);
		 * var violations:Array = validator.validateValue(type, propertyName, value, groups);
		 * </listing>
		 * 
		 * @param type the bean type holding the property to validate.
		 * @param propertyName the property name to validate.
		 * @param value the property value to validate.
		 * @param groups an array of groups targeted for validation (default is
		 * 		null, meaning that the <code>Default</code> group will be used).
		 * 
		 * @return an array of <code>ConstraintViolation</code>, possibly null or
		 * 		empty.
		 * 
		 * @see IValidator
		 * @see ConstraintViolation
		 */
		public function validateValue(type:Type, propertyName:String, value:*, groups:Array = null):Array {
			var validator:IValidator = getValidator(type);
			return validator.validateValue(type, propertyName, value, groups);
		}
	}
}
