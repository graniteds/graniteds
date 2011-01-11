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

package test.granite.components
{

import mx.collections.Sort;

/**
 * @author Franck WOLFF
 */
public class PropertySort extends Sort
{

    private var _comparator:Function = null;

    public function PropertySort( comparator:Function )
    {
        super();
        this._comparator = comparator;
    }

    override public function findItem(
            items:Array, values:Object, mode:String, returnInsertionIndex:Boolean = false, compareFunction:Function = null ):int
    {
        return super.findItem( items, values, mode, returnInsertionIndex, _comparator );
    }
}
}
