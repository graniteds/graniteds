/*
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
package org.granite.tide.collections {
    
    import mx.collections.IList;
    
    import org.granite.tide.ITideMergeResponder;
    import org.granite.tide.ITideResponder;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    
    
	/**
	 *  @private
	 * 	Support class for paged collection implementation
	 */
	public class PagedCollectionResponder implements ITideMergeResponder {
	    
	    private var _resultHandlers:Array = new Array();
	    private var _faultHandlers:Array = new Array();
	    private var _first:int;
	    private var _max:int;
		private var _mergeWith:Object;
	    
	    
	    public function PagedCollectionResponder(resultHandler:Function, faultHandler:Function, collection:IList, first:int, max:int):void {
	        if (resultHandler != null)
	            _resultHandlers.push(resultHandler);
	        if (faultHandler != null)
	            _faultHandlers.push(faultHandler);   
	        _first = first;
	        _max = max;
			if (collection != null)
				_mergeWith = { firstResult: first, maxResults: max, resultList: collection, resultCount: 0, __meta_nocache__: true };
			else
				_mergeWith = { __meta_nocache__: true };
	    }
	    
	    public function addHandlers(resultHandler:Function = null, faultHandler:Function = null):void {
	        if (resultHandler != null)
	            _resultHandlers.push(resultHandler);
	        if (faultHandler != null)
	            _faultHandlers.push(faultHandler);
	    }
	    
	    
	    public function result(event:TideResultEvent):void {
	        for each (var resultHandler:Function in _resultHandlers) 
	            resultHandler(event, _first, _max);
	    }
	    
	    public function fault(event:TideFaultEvent):void {
	        for each (var faultHandler:Function in _faultHandlers)
	            faultHandler(event, _first, _max);
	    }         
        
        public function get mergeResultWith():Object {
            return _mergeWith;
        }
	}
}
