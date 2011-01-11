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

    import mx.collections.ArrayList;
    import org.granite.IComparator;

    /**
     *	ArrayList with modified getItemIndex that can compare values with a comparator class
     *  
     * 	@author Franck WOLFF
     */
    public class ComparatorArrayList extends ArrayList {

        private var _comparator:IComparator = null;

        public function ComparatorArrayList(source:Array=null, comparator:IComparator=null) {
            super(source);
            _comparator = comparator;
        }

        public function get comparator():IComparator {
            return _comparator;
        }

        protected function set comparator(comparator:IComparator):void {
            _comparator = comparator;
        }

        override public function getItemIndex(item:Object):int {
            var n:int = source.length;
            for (var i:int = 0; i < n; i++) {
                var o:Object = source[i];
                if (_comparator) {
                    if (_comparator.equals(o, item))
                        return i;
                } else if (o === item)
                    return i;
            }
            return -1;
        }
    }
}