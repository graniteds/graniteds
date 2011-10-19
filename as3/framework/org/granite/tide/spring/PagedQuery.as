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

package org.granite.tide.spring {
    
    import flash.events.Event;
    
    import mx.binding.utils.BindingUtils;
    import mx.collections.*;
    import mx.collections.errors.ItemPendingError;
    import mx.core.IPropertyChangeNotifier;
    import mx.core.IUID;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.PropertyChangeEvent;
    import mx.events.PropertyChangeEventKind;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.rpc.IResponder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ArrayUtil;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Component;
    import org.granite.tide.IComponent;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.collections.PagedCollection;
    import org.granite.tide.collections.PagedCollectionResponder;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
	
	
	[Bindable]
	/**
	 * 	Implementation of the Tide paged collection for Spring services
	 * 
     * 	@author William DRAI
     */
	public class PagedQuery extends org.granite.tide.collections.PagedQuery {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.spring.PagedQuery");
        
        private var _useController:Boolean = false;
        private var _useGrailsController:Boolean = false;
		
		
		public function PagedQuery() {
			super();
		}
		
		
		public function set useController(useController:Boolean):void {
			_useController = useController;
		}
		
		public function set useGrailsController(useGrailsController:Boolean):void {
			_useGrailsController = useGrailsController;
			if (_useGrailsController && !_methodNameSet)
				_methodName = "list";
		}
    	
    	
		CONFIG::flex40 {
			protected override function doFind(filter:Object, first:int, max:int, sort:Sort, findResponder:PagedCollectionResponder):void {			
				var order:* = null;
				var desc:* = null;
				if (this.multipleSort && !_useGrailsController) {
					if (sort != null) {
						order = new Array();
						desc = new Array();
						for each (var s:SortField in sort.fields) {
							order.push(s.name);
							desc.push(s.descending);
						}
					}
				}
				else {
					order = sort != null && sort.fields.length > 0 ? sort.fields[0].name : null;
					desc = sort != null && sort.fields.length > 0 ? sort.fields[0].descending : false;
				}
				
				if (_useGrailsController) {
					_context.meta_callComponent(_component, _methodName, [{ filter: filter, 
							offset: first, 
							max: max, 
							sort: order, 
							order: desc ? "desc" : "asc"
						}, 
						true, 	// Use local binding
						findResponder]
					);
					return;
				}
				else if (_useController) {
					_context.meta_callComponent(_component, _methodName, [{ filter: filter, 
							first: first, 
							max: max, 
							order: order, 
							desc: desc 
						}, 
						true, 	// Use local binding
						findResponder]
					);
				}
				else { 
					super.doFind(filter, first, max, sort, findResponder);
				}
			}
		}
    	
		CONFIG::flex45 {
			protected override function doFind(filter:Object, first:int, max:int, sort:ISort, findResponder:PagedCollectionResponder):void {			
				var order:* = null;
				var desc:* = null;
				if (this.multipleSort && !_useGrailsController) {
					if (sort != null) {
						order = new Array();
						desc = new Array();
						for each (var s:SortField in sort.fields) {
							order.push(s.name);
							desc.push(s.descending);
						}
					}
				}
				else {
					order = sort != null && sort.fields.length > 0 ? sort.fields[0].name : null;
					desc = sort != null && sort.fields.length > 0 ? sort.fields[0].descending : false;
				}
				
				if (_useGrailsController) {
					_context.meta_callComponent(_component, _methodName, [{ filter: filter, 
							offset: first, 
							max: max, 
							sort: order, 
							order: desc ? "desc" : "asc"
						}, 
						true, 	// Use local binding
						findResponder]
					);
					return;
				}
				else if (_useController) {
					_context.meta_callComponent(_component, _methodName, [{ filter: filter, 
							first: first, 
							max: max, 
							order: order, 
							desc: desc 
						}, 
						true, 	// Use local binding
						findResponder]
					);
				}
				else { 
					super.doFind(filter, first, max, sort, findResponder);
				}
			}
		}
		
		
		protected override function getResult(event:TideResultEvent, first:int, max:int):Object {
			if (_useGrailsController || _useController) {
				var result:Object = new Object();
		    	result.firstResult = Object(_component).hasOwnProperty("firstResult") ? Object(_component).firstResult : first;
		    	result.maxResults = Object(_component).hasOwnProperty("maxResults") ? Object(_component).maxResults : max;
		    	var count:Boolean = true;
		    	var list:Boolean = true;
		    	if (Object(_component).hasOwnProperty("resultCount")) {
		    		result.resultCount = Object(_component).resultCount;
		    		count = false;
		    	}
		    	if (Object(_component).hasOwnProperty("resultList")) {
		    		result.resultList = Object(_component).resultList;
		    		list = false;
		    	}
		    	if (count || list) {
			    	for (var p:Object in _component) {
			    		if (count && (p.match(/.*Count/) || p.match(/.*Total/)))
			    			result.resultCount = _component[p];
			    		else if (list && p.match(/.*List/))
			    			result.resultList = _component[p];
			    	}
			    }
		    	return result;
			}
			else {
				return super.getResult(event, first, max);
		    }
		}
	}
}
