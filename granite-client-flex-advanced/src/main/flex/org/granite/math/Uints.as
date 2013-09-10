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

package org.granite.math {

	import flash.utils.Proxy;
	import flash.utils.flash_proxy;

    /**
	 * Represent an array of uint (with range support).
	 * 
     * @author Franck WOLFF
	 * @private
     */
	public class Uints extends Proxy {
		
		///////////////////////////////////////////////////////////////////////
		// Fields.

		private var _uints:Array;
		private var _start:int;
		private var _end:int;
		
		///////////////////////////////////////////////////////////////////////
		// Constructor.
		
		function Uints(uints:Array = null, start:Number = NaN, end:Number = NaN) {
			_uints = (uints === null ? newUintArray(0) : uints);
			_start = (isNaN(start) ? 0 : start);
			_end = (isNaN(end) ? _uints.length : end); 
			
			if (_end < _start)
				throw new BigNumberError("end must be >= start");
		}
		
		///////////////////////////////////////////////////////////////////////
		// Properties.

		public function getAt(index:int):uint {
			index += _start;
			if (index < 0 || index >= _end || index >= _uints.length)
				return 0;
			return _uints[index];
		}
		
		public function setAt(value:uint, index:int):void {
			index += _start;
			if (index < 0 || index > _end || index > _uints.length)
				throw new BigNumberError("Index out of range: " + index);
			_uints[index] = value;
			if (index == _end)
				_end++;
		}
		
		public function get length():int {
			return (_end - _start);
		}
		public function set length(length:int):void {
			if (_start < 0 || _end > _uints.length)
				throw new BigNumberError("Cannot set length with start=" + _start + " and end=" + _end + " (normalize first)");
			
			_uints.length = _end = length + start;
		}
		
		public function get start():int {
			return _start;
		}
		public function set start(value:int):void {
			if (value > _end)
				throw new BigNumberError("start cannot be > end: " + _end);
			_start = value;
		}
		
		public function get end():int {
			return _end;
		}
		public function set end(value:int):void {
			if (value < _start)
				throw new BigNumberError("end cannot be < start: " + _start);
			_end = end;
		}
		
		public function isNormal():Boolean {
			return (
				_start == 0 &&
				_end == _uints.length && 
				(_end == 0 || _uints[_end - 1] != 0)
			);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Operations.
		
		public function normalize():void {
			
			if (isNormal())
				return;

			if (_start != 0 || _end != _uints.length) {
				// zero length uints or start/end out of significant range.
				if (_start >= _end || _end <= 0 || _start >= _uints.length)
					_uints = newUintArray(0);
				// start < 0 means: uints is the most significant part of the number,
				// followed by -start zeros ("45698987"..."000000" [-start * "0"]).
				else if (_start < 0)
					_uints = newUintArray(-_start).concat(_uints.slice(0, _end));
				else
					_uints = _uints.slice(_start, _end);
	
				_start = 0;
				_end = _uints.length;
			}
			
			// remove leading zeros (little endian order).
			while (_end > 0 && _uints[_end - 1] == 0)
				_end--;
			
			_uints.length = _end;
		}
		
		public function clone():Uints {
			return new Uints(_uints.concat(), _start, _end);
		}
		
		///////////////////////////////////////////////////////////////////////
		// Proxy implementation (partial).

	    override flash_proxy function getProperty(index:*):* {
	        return getAt(index);
	    }
	
	    override flash_proxy function setProperty(index:*, value:*):void {
	        setAt(value, index);
	    }
		
		override flash_proxy function nextNameIndex(index:int):int {
			var i:int = index + _start;
			if (i < 0 || i >= _end)
				return 0;
			return index + 1;
		}
		
		override flash_proxy function nextValue(index:int):* {
			return getAt(index - 1);
		}
		
		override flash_proxy function callProperty(name:*, ... rest):* {
			throw new BigNumberError("Not implemented");
		}
		
		override flash_proxy function deleteProperty(name:*):Boolean {
			throw new BigNumberError("Not implemented");
		}
		
		override flash_proxy function hasProperty(name:*):Boolean {
			throw new BigNumberError("Not implemented");
		}
		
		override flash_proxy function isAttribute(name:*):Boolean {
			throw new BigNumberError("Not implemented");
		}
		
		override flash_proxy function nextName(index:int):String {
			throw new BigNumberError("Not implemented");
		}
		
		///////////////////////////////////////////////////////////////////////
		// Static utility (initialized arrays).
		
		public static function newUintArray(length:int, def:uint = 0):Array {
			
			function init(e:*, i:int, a:Array):void {
	            a[i] = def;
	        }

			if (length <= 0)
				return new Array(0);
			
			var uints:Array = new Array(length);
			uints.forEach(init);
			return uints;
		}
	}
}