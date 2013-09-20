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
