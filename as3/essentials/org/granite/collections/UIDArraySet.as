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

    import flash.utils.Dictionary;
    import mx.core.IUID;
    import org.granite.UIDComparator;

    /**
     *	Implementation of set that compares values by UID
     *  
     * 	@author Franck WOLFF
     */
    public class UIDArraySet extends ComparatorArrayList {

        private var dictionary:Dictionary = new Dictionary();

        public function UIDArraySet(source:Array=null) {
            super(source, new UIDComparator());
        }

        override public function set source(s:Array):void {
            var dic:Dictionary = new Dictionary();
            var set:Array = new Array();

            if (s) {
                var n:int = s.length;
                for (var i:int = 0; i < n; i++) {
                    var item:Object = s[i];
                    if (item is IUID) {
                        if (dictionary[IUID(item).uid] === undefined) {
                            dic[IUID(item).uid] = item;
                            set.push(item);
                        }
                    } else if (dictionary[item] === undefined) {
                        dic[item] = item;
                        set.push(item);
                    }
                }
            }

            super.source = set;
            dictionary = dic;
        }

        override public function addItemAt(item:Object, index:int):void {
            if ((item is IUID) && dictionary[IUID(item).uid] === undefined) {
                super.addItemAt(item, index);
                dictionary[IUID(item).uid] = item;
            } else if (dictionary[item] === undefined) {
                super.addItemAt(item, index);
                dictionary[item] = item;
            }
        }

        override public function setItemAt(item:Object, index:int):Object {
            var old:Object = super.setItemAt(item, index);

            if (old is IUID) {
                if (dictionary[IUID(old).uid] !== undefined)
                    delete dictionary[IUID(old).uid];
            } else if (dictionary[old] !== undefined)
                delete dictionary[old];

            if (item is IUID)
                dictionary[IUID(item).uid] = item;
            else
                dictionary[item] = item;

            return old;
        }

        override public function removeItemAt(index:int):Object {
            var old:Object = super.removeItemAt(index);

            if (old is IUID) {
                if (dictionary[IUID(old).uid] !== undefined)
                    delete dictionary[IUID(old).uid];
            } else if (dictionary[old] !== undefined)
                delete dictionary[old];

            return old;
        }

        override public function removeAll():void {
            super.removeAll();
            dictionary = new Dictionary();
        }
    }
}