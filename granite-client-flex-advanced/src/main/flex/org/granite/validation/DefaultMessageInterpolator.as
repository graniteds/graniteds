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

	import flash.utils.getQualifiedClassName;
	
	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	import mx.utils.ObjectUtil;
	
	import org.granite.util.ClassUtil;

	/**
	 * A default implementation of the <code>IMessageInterpolator</code>
	 * interface, that transform a constraint message key into a human
	 * readable error message. This implementation follows the JSR-303
	 * specification recommendations (section 4.3.1) for message parameters
	 * substitutions.
	 * 
	 * @author Franck WOLFF
	 * 
	 * @see ValidatorFactory#messageInterpolator
	 * @see ValidationMessages
	 */
	public class DefaultMessageInterpolator implements IMessageInterpolator {
		
		/**
		 * @private
		 */
		private static const OPEN_CURLY_BRACE:Number = "{".charCodeAt(0);
		
		/**
		 * @private
		 */
		private static const CLOSE_CURLY_BRACE:Number = "}".charCodeAt(0);
		
		/**
		 * @private
		 */
		private static const BACKSLASH:Number = "\\".charCodeAt(0);
		
		/**
		 * Constructs a new <code>DefaultMessageInterpolator</code> instance.
		 */
		function DefaultMessageInterpolator() {
		}
		
		/**
		 * Interpolate the message template based on the contraint validation context
		 * and substitute message parameters according to the JSR-303 specification
		 * (section 4.3.1). Standard or user specific validation bundles are used for
		 * parameters resolution.
		 *
		 * @param messageTemplate the message to interpolate.
		 * @param constraint the constraint related to the interpolation.
		 * @param value the invalid value (according to the current constraint).
		 * @param locale an optional locale used for message localization.
		 *
		 * @return the interpolated error message.
		 * 
	 	 * @see ValidationMessages
		 */
		public function interpolate(messageTemplate:String,
									constraint:IConstraint,
									value:*,
									locale:String = null):String {

			var s1:String = messageTemplate,
				s2:String,
				v:String,
				token:String,
				tokens:Array;
			
			// Spec. 4.3.1.1: steps 1, 2, 3.
			while (true) {
				s2 = "";
				tokens = split(s1);
				for each (token in tokens) {
					if (token.charCodeAt(0) == OPEN_CURLY_BRACE) {
						v = getBundle().getString(token.substring(1, token.length - 1), null, locale);
						s2 += (v == null ? token : v);
					}
					else
						s2 += token;
				}
				if (s1 == s2)
					break;
				s1 = s2;
			}
			
			// Spec. 4.3.1.1: steps 4.
			s2 = "";
			tokens = split(s1);
			for each (token in tokens) {
				if (token.charCodeAt(0) == OPEN_CURLY_BRACE) {
					try {
						s2 += constraint[token.substring(1, token.length - 1)];
					}
					catch (e:Error) {
						s2 += token;
					}
				}
				else
					s2 += token;
			}
			s1 = s2;
			
			return s1;
		}
		
		/**
		 * Returns the unique instance of the <code>ValidationMessages</code>
		 * class (shortcut for <code>ValidationMessages.getInstance()</code>).
		 * 
		 * @return the unique instance of the <code>ValidationMessages</code>
		 * 		class.
		 */
		public static function getBundle():ValidationMessages {
			return ValidationMessages.getInstance();
		} 

		/**
		 * Splits a message template into an array of string literals and
		 * parameters. For example, <code>split('Must be in [{min}, {max}]')</code>
		 * will return <code>['Must be in [', '{min}', ', ', '{max}', ']'])</code>.
		 * See JSR-303 specification section 4.3.1.
		 * 
		 * @param the message template to be split into tokens.
		 * 
		 * @return an array of string literals and parameters.
		 */
		public static function split(messageTemplate:String):Array {
			var tokens:Array = new Array(),
				backslash:Boolean = false,
				inparam:Boolean = false,
				start:int = 0,
				i:int;

			for (i = 0; i < messageTemplate.length; i++) {
				switch (messageTemplate.charCodeAt(i)) {
				case OPEN_CURLY_BRACE:
					if (!backslash) {
						if (inparam)
							throw new ValidationError("Nested unescaped opening curly barce: " + messageTemplate);
						if (start < i)
							tokens.push(messageTemplate.substring(start, i));
						start = i;
						inparam = true;
					}
					else
						backslash = false;
					break;
				case CLOSE_CURLY_BRACE:
					if (!backslash) {
						if (!inparam)
							throw new ValidationError("Closing curly brace found without a corresponding opening one: " + messageTemplate);
						inparam = false;
						tokens.push(messageTemplate.substring(start, i + 1));
						start = i + 1;
					}
					else
						backslash = false;
					break;
				case BACKSLASH:
					backslash = !backslash;
					break;
				default:
					backslash = false;
					break;
				}
			}
			if (inparam)
				throw new ValidationError("Unclosed curly brace in: " + messageTemplate);
			if (start < messageTemplate.length)
				tokens.push(messageTemplate.substring(start));
			
			return tokens;
		}
		
		/**
		 * Unescape the given string parameter, removing extraneous backslashs.
		 * See JSR-303 specification section 4.3.1.
		 * 
		 * @param message the message to be unescaped.
		 * 
		 * @return the unescaped message.
		 */
		public static function unescape(message:String):String {
			if (message.indexOf("\\\\") == -1)
				return message.replace(/\\\{/g, "{").replace(/\\\}/g, "}");
			
			var unescaped:String = "",
				token:String;
			
			for each (token in message.split(/(\\\\)/g)) {
				if (token == "\\\\")
					unescaped += "\\";
				else
					unescaped += token.replace(/\\\{/g, "{").replace(/\\\}/g, "}");
			}
			
			return unescaped;
		}
	}
}