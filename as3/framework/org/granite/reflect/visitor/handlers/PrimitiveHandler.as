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