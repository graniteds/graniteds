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

package org.granite.collections {

    import flash.utils.getQualifiedClassName;
	import flash.utils.Dictionary;
	import mx.core.IUID;
    import mx.collections.ArrayCollection;
    import mx.collections.IList;
	
	
    /**
     *	Implementation of HashSet that holds weak references to UID entities 
     *  
     * 	@author Franck WOLFF
     */
	public class UIDWeakSet {
		
		[ArrayElementType("flash.utils.Dictionary")]
		private var _table:Array;
		
        
		public function UIDWeakSet(capacity:int = 64) {
			_table = new Array(capacity);
			
		}
		
		public function put(uidObject:IUID):IUID {
			var h:int = hash(getQualifiedClassName(uidObject) + ":" + uidObject.uid);
			
			var dic:Dictionary = _table[h];
			if (!dic) {
				dic = new Dictionary(true);
				_table[h] = dic;
			}
			
			var old:IUID = null;
			for (var o:Object in dic) {
			    if (o === uidObject)
			        return IUID(o);
			    
				if ((o as IUID).uid === uidObject.uid && getQualifiedClassName(o) === getQualifiedClassName(uidObject)) {
					old = (o as IUID);
					delete dic[o];
					break;
				}
			}
			
			dic[uidObject] = null;
			
			return old;
		}
		
		public function get(uid:String):IUID {
			var h:int = hash(uid);
			
			var uidObject:IUID = null;
			
			var dic:Dictionary = _table[h];
			if (dic) {
				for (var o:Object in dic) {
					if (getQualifiedClassName(o) + ":" + ((o as IUID).uid) === uid) {
						uidObject = (o as IUID);
						break;
					}
				}
			}
			
			return uidObject;
		}

        public function find(matcher:Function):Object {
            for (var i:int = 0; i < _table.length; i++) {
                var dic:Dictionary = _table[i];
                if (dic) {
                    for (var o:Object in dic) {
                        if (matcher(o))
                            return o;
                    }
                }
            }
            return null;
        }
		
		public function remove(uid:String):IUID {
			var h:int = hash(uid);
			
			var uidObject:IUID = null;
			
			var dic:Dictionary = _table[h];
			if (dic) {
				for (var o:Object in dic) {
					if (getQualifiedClassName(o) + ":" + ((o as IUID).uid) === uid) {
						uidObject = (o as IUID);
						delete dic[o];
						break;
					}
				}
			}
			
			return uidObject;
		}
		
		public function get size():uint {
			var size:uint = 0;
			
			for (var i:int = 0; i < _table.length; i++) {
				var dic:Dictionary = _table[i];
				if (dic) {
					for (var o:Object in dic)
						size++;
				}
			}
			
			return size;
		}
        
        public function get data():Array {
            var d:Array = new Array();
            
            for (var i:int = 0; i < _table.length; i++) {
                var dic:Dictionary = _table[i];
                if (dic) {
                    for (var o:Object in dic)
                        d.push(o);
                }
            }
            return d;
        }
		
		private function hash(uid:String):int {
			var h:int = 0;
			var max:int = uid.length;
			for (var i:int = 0; i < max; i++)
				h = (31 * h) + int(uid.charCodeAt(i));
		    return (Math.abs(h) % _table.length);
		}
	}
}
