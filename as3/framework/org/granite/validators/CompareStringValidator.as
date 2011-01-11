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

package org.granite.validators {

	import flash.events.Event;
	import flash.events.IEventDispatcher;
	import mx.validators.Validator;
	import mx.validators.ValidationResult;
	import mx.utils.ObjectUtil;

	/**
	 * A <code>mx.validators.Validator</code> implementation that compares a string to
	 * another string (somewhat deprecated).
	 * 
	 * @author Franck WOLFF
	 */
	public class CompareStringValidator extends Validator {

		public static const DEFAULT_COMPARE_ERROR:String = "The two values are not equal";

		private var _compareTo:String;
		private var _compareError:String = DEFAULT_COMPARE_ERROR;
		
		function CompareStringValidator() {
			super();
		}
		
		public function get compareTo():String {
			return _compareTo;
		}
		public function set compareTo(value:String):void {
			_compareTo = value;
			if (source && property && shouldValidate)
				validate();
		}
		
		public function get compareError():String {
			return _compareError;
		}
		public function set compareError(value:String):void {
			_compareError = (value && value.length ? value : DEFAULT_COMPARE_ERROR);
		}
		
		override public function set source(value:Object):void {
			var changed:Boolean = (value != source);
		
			super.source = value;
			
			if (changed && value is IEventDispatcher)
				IEventDispatcher(value).addEventListener(Event.CHANGE, sourceValueChanged);
		}
		
		private function sourceValueChanged(event:Event):void {
			if (source && property && shouldValidate)
				validate();
		}
		
		override protected function doValidation(value:Object):Array {
            var results:Array = super.doValidation(value);        
            
			if (results.length == 0 && shouldValidate) {
				if (ObjectUtil.stringCompare(String(value), compareTo) != 0)
					results.push(new ValidationResult(true, null, "notEquals", compareError));
			}

			return results;
		}
		
		private function get shouldValidate():Boolean {
			return !required || (getValueFromSource() is String && String(getValueFromSource()).length > 0);
		}
	}
}
