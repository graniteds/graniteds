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
	    private var _mergeWith:Object = null;
	    private var _first:int;
	    private var _max:int;
	    
	    
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
