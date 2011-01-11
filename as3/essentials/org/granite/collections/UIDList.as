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

	import mx.collections.IList;
	
    [RemoteClass(alias="org.granite.collections.UIDList")]
    /**
     *	Serializable implementation of a list of UID elements
     *  
     * 	@author Franck WOLFF
     */
    public class UIDList extends CheckedArrayCollection {

        override public function set source(s:Array):void {
            list = new UIDArrayList(s);
        }

        override public function set list(value:IList):void {
            if (value && !(value is UIDArrayList))
                value = new UIDArrayList(value.toArray());
            super.list = value;
        }
    }
}
