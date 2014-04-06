/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.util {

	import flash.utils.Dictionary;

	/**
	 * Static utility methods for <code>Dictionary</code> manipulation.
	 * 
	 * @author Franck WOLFF 
	 */
	public class DictionaryUtil {

		public static function getValues(dictionary:Dictionary):Array {
			var values:Array = new Array(), value:*;
			for each (value in dictionary)
				values.push(value);
			return values;
		}

		public static function getKeys(dictionary:Dictionary):Array {
			var keys:Array = new Array(), key:*;
			for (key in dictionary)
				keys.push(key);
			return keys;
		}

		public static function getLength(dictionary:Dictionary):uint {
			var length:uint = 0, key:*;
			for (key in dictionary)
				length++;
			return length;
		}

		public static function getEntries(dictionary:Dictionary):Array {
			var entries:Array = new Array(), key:*;
			for (key in dictionary)
				entries.push([key, dictionary[key]]);
			return entries;
		}
	}
}
