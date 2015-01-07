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
package org.granite.validation.helper {

	import flash.utils.Dictionary;
	
	import mx.utils.StringUtil;
	
	import org.granite.math.BigDecimal;
	import org.granite.math.Long;
	import org.granite.reflect.Arg;
	import org.granite.reflect.Type;
	import org.granite.util.ClassUtil;
	import org.granite.validation.ConstraintDefinitionError;
	import org.granite.validation.ConstraintExecutionError;
	import org.granite.validation.IConstraint;

	/**
	 * Static methods that simplify constraint annotation arguments parsing. See
	 * standard constraint implementations for usage.
	 * 
	 * @author Franck WOLFF
	 * 
	 * @see ParameterDefinition
	 */
	public class ConstraintHelper {

		public static function parseParameters(constraint:IConstraint, args:Array, definitions:Array):Dictionary {
			var params:Dictionary = new Dictionary(),
				def:ParameterDefinition,
				definition:ParameterDefinition,
				arg:Arg,
				found:Boolean;
			
			// check for required parameters (and duplicated parameters).
			for each (def in definitions) {
				if (!def.optional) {
					found = false;
					for each (arg in args) {
						if (arg.key == def.name) {
							found = true;
							break;
						}
					}
					if (!found)
						throw new ConstraintDefinitionError(constraint, "Parameter '" + def.name + "' is mandatory");
				}

				if (params.hasOwnProperty(def.name))
					throw new ConstraintDefinitionError(constraint, "Duplicated '" + def.name + "' parameter");

				params[def.name] = def.defaultValue;
			}
			
			// check for unknown parameters & parse actual parameters.
			for each (arg in args) {
				definition = null;
				
				for each (def in definitions) {
					if (def.name == arg.key) {
						definition = def;
						break;
					}
				}
				
				if (definition == null)
					throw new ConstraintDefinitionError(constraint, "Illegal parameter name: " + arg.key);
				
				var values:Array = null;
				
				if (!definition.array)
					values = [arg.value];
				else
					values = arg.value.split(/,/g);
				
				values = values.map(function(item:String, index:int, array:Array):* {
					item = StringUtil.trim(item).replace(/&comma;/g, ",").replace(/&space;/g, " ");
					return convert(constraint, definition, arg, item, definition.type);
				});

				if (definition.array)
					params[definition.name] = values;
				else
					params[definition.name] = values[0];
			}
			
			return params;
		}
		
		private static function convert(constraint:IConstraint, definition:ParameterDefinition, arg:Arg, value:String, type:Class):* {
			var converted:* = null, className:String = null;
			
			switch (type) {
				case String:
					converted = value;
					break;

				case Class:
					try {
						className = definition.getAlias(value);
						className = constraint.factory.namespaceResolver.resolve(className);
						converted = Type.forName(className).getClass();
					}
					catch (e:Error) {
						throw new ConstraintDefinitionError(constraint, "Class not found: '" + value + "' (resolved to '" + className + "') in " + arg);
					}
					break;

				case Number:
				case int:
				case uint:
					if (value == "")
						throw new ConstraintDefinitionError(constraint, "Empty numeric value in " + arg);
					converted = Number(value);
					if (isNaN(converted))
						throw new ConstraintDefinitionError(constraint, "Illegal numeric value: '" + value + "' in " + arg);
					
					if (type === int) {
						if (converted < int.MIN_VALUE || converted > int.MAX_VALUE)
							throw new ConstraintDefinitionError(constraint, "Illegal numeric value: '" + value + "' in " + arg + " (too big for an int)");
						if (int(value) != Number(value))
							throw new ConstraintDefinitionError(constraint, "Illegal numeric value: '" + value + "' in " + arg + " (not an int)");
					}
					else if (type === uint) {
						if (converted < 0)
							throw new ConstraintDefinitionError(constraint, "Illegal numeric value: '" + value + "' in " + arg + " (uint cannot be negative)");
						if (converted > uint.MAX_VALUE)
							throw new ConstraintDefinitionError(constraint, "Illegal numeric value: '" + value + "' in " + arg + " (too big for an uint)");
						if (uint(value) != Number(value))
							throw new ConstraintDefinitionError(constraint, "Illegal numeric value: '" + value + "' in " + arg + " (not an uint)");
					}
					break;

				case BigDecimal:
					try {
						converted = new BigDecimal(value);
					}
					catch (e:Error) {
						throw new ConstraintDefinitionError(constraint, "Illegal BigDecimal value: '" + value + "' in " + arg);
					}
					break;

				case Long:
					try {
						converted = new Long(value);
					}
					catch (e:Error) {
						throw new ConstraintDefinitionError(constraint, "Illegal Long value: '" + value + "' in " + arg);
					}
					break;

				default:
					throw new ArgumentError("Unsupported parameter type: " + type);
			}
			
			return converted;
		}
		
		public static function checkValueType(constraint:IConstraint, value:*, acceptedClasses:Array):void {
			const clazz:Class = ClassUtil.forInstance(value);
			if (acceptedClasses.indexOf(clazz) == -1)
				throw new ConstraintExecutionError(constraint, value, "Illegal value type (should be in " + acceptedClasses + ")");
		}
	}
}