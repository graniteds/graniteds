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

package org.granite.reflect.visitor.handlers {

	import org.granite.math.BigDecimal;
	import org.granite.math.BigInteger;
	import org.granite.math.Long;
	import org.granite.util.ClassUtil;
	import org.granite.reflect.visitor.IHandler;
	import org.granite.reflect.visitor.Visitable;

	/**
	 * Handles any <i>primitive</i> value of the following types:
	 * 
	 * <listing>
	 * int,
	 * uint,
	 * String,
	 * Boolean,
	 * Date,
	 * Number,
	 * BigDecimal,
	 * BigInteger,
	 * Long,
	 * Class,
	 * Function,
	 * Namespace,
	 * QName,
	 * XML,
	 * XMLList,
	 * RegExp,
	 * Error</listing>
	 * 
	 * @author Franck WOLFF
	 */
	public class PrimitiveHandler implements IHandler {

		private static const _types:Array = [
			int,
			uint,
			String,
			Boolean,
			Date,
			Number,
			BigDecimal,
			BigInteger,
			Long,
			Class,
			Function,
			Namespace,
			QName,
			XML,
			XMLList,
			RegExp,
			Error
		];
		
		public function canHandle(visitable:Visitable):Boolean {
			return _types.indexOf(ClassUtil.forInstance(visitable.value)) != -1;
		}
		
		public function handle(visitable:Visitable, filter:Function):Array {
			return null;
		}
	}
}