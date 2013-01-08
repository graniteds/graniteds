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

package org.granite.tide.data {

    import org.granite.tide.*;
    
	
	/**
	 * 	Interface for custom data mergers
	 *
	 * 	@author William DRAI
	 */
	public interface ICustomMerger {
	    
	    /**
	     * 	Should return true if this merger is able to handle the specified object
	     *
	     *  @param obj an object
	     *  @return true if object can be handled
	     */
	    function accepts(obj:Object):Boolean;
		
        /**
         *  @private
         *  Merge an entity coming from the server in the entity manager
         *
         *  @param mergeContext the current merge context
         *  @param obj external entity
         *  @param previous previously existing object in the context (null if no existing object)
         *  @param expr current path from the context
         *  @param parent parent object for collections
         *  @param propertyName property name of the collection in the owner object
         *
         *  @return merged entity (=== previous when previous not null)
         */
	    function merge(mergeContext:MergeContext, obj:Object, previous:Object, expr:IExpression, parent:Object, propertyName:String):Object;

	}
}
