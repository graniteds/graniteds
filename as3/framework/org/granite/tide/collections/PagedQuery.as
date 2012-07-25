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
    
    import org.granite.reflect.Method;
    import org.granite.reflect.Parameter;
    import org.granite.reflect.Type;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Component;
    import org.granite.tide.IComponent;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.data.model.PageInfo;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
	
	
	[Bindable]
	/**
	 * 	Implementation of the Tide paged collection with an generic service backend.<br/>
	 *  <br/>
	 *  By default the corresponding service should have the same name and expose a 'find' method<br/>
	 *  that returns a Map with the following properties :<br/>
	 *  <pre>
	 *  resultCount
	 *  resultList
	 *  firstResult
	 *  maxResults
	 *  </pre>
	 * 
	 *  The name of the remote service can be overriden by setting the remoteComponentName property.
	 *  The name of the remote method can by set by the remoteMethodName property.
	 * 
     * 	@author William DRAI
     */
	public class PagedQuery extends PagedCollection implements IComponent, IPropertyHolder {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.collections.PagedQuery");
		
	    protected var _component:Component = null;
        
        private var _remoteComponentName:String = null;
		
        protected var _methodName:String = "find";
        protected var _methodNameSet:Boolean = false;
		
		protected var _usePage:Boolean = false;
        
        private var _internalFilter:Object = new Object();
        private var _filter:IPropertyChangeNotifier = new ObjectProxy(_internalFilter);
		
		
		public override function meta_init(componentName:String, context:BaseContext):void {
			super.meta_init(componentName, context);
			_remoteComponentName = componentName;
			_component = new Component();
			_component.meta_init(_remoteComponentName, context);
			_component.meta_templateObject = this;
			filter.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, filterChangedHandler);
		}
		
		public function get filter():Object {
			return _filter;
		}
		public function set filter(filter:Object):void {
			if (_filter != null)
				_filter.removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, filterChangedHandler);
			
			if (filter is IPropertyChangeNotifier) {
				_internalFilter = filter;
				_filter = IPropertyChangeNotifier(filter);
			}
			else {
				_internalFilter = filter;
				_filter = new ObjectProxy(_internalFilter);
			}
			_filter.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, filterChangedHandler, false, 0, true);
		}
		public function set filterClass(filterClass:Class):void {
			filter = new filterClass();
		}
	
	
		public function get meta_name():String {
		    return _componentName;
		}
		
		public function meta_clear():void {
	    	_component.meta_clear();
		    super.clear();
		}
		
		public function set remoteComponentName(remoteComponentName:String):void {
			if (remoteComponentName != _componentName) {
				_component = _context[remoteComponentName] as Component;
				if (_component == null) {
					_component = new Component();
					_component.meta_init(remoteComponentName, _context);
					_component.meta_templateObject = this;
					_context[remoteComponentName] = _component;
				}
			}
			else {
				_component = new Component();
				_component.meta_init(remoteComponentName, _context);
				_component.meta_templateObject = this;
			}
		}
		
		public function set remoteComponentClass(remoteComponentClass:Class):void {
			_component = new remoteComponentClass() as Component;
		}
		
		public function set methodName(methodName:String):void {
			_methodName = methodName;
			_methodNameSet = true;
		}
		
		public function set usePage(usePage:Boolean):void {
			_usePage = usePage;
		}
    	
    	
		/**
		 *	Trigger a results query for the current filter
		 *	@param first	: index of first required result
		 *  @param last     : index of last required result
		 */
		protected override function find(first:int, last:int):void {
			super.find(first, last);
			
			var max:int = 0;
			if (_initializing && _max > 0)
				max = _max;
			else if (!_initializing)
			    max = last-first;
			var findResponder:PagedCollectionResponder = new PagedCollectionResponder(findResult, findFault, 
				_initializing ? null : this, first, max);
				
			var filter:Object = _internalFilter;
			
			doFind(filter, first, max, sort, findResponder);
		}
		
		CONFIG::flex40 {
			protected function doFind(filter:Object, first:int, max:int, sort:Sort, findResponder:PagedCollectionResponder):void { 			
				// Force evaluation of max, results and count
				var order:* = null;
				var desc:* = null;
				if (this.multipleSort) {
					if (sort != null && sort.fields.length > 0) {
						order = [];
						desc = [];
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
				
				var usePage:Boolean = _usePage;
				var method:Method = Type.forInstance(_component).getMethod(function(m:Method):Boolean { return m.name == _methodName; });
				if (method != null && method.parameters.length >= 2 && Parameter(method.parameters[1]).type.equals(Type.forClass(PageInfo)))
					usePage = true;
				
				if (usePage)
					_context.meta_callComponent(_component, _methodName, [filter, new PageInfo(first, max, 
						order is Array || order == null ? order : [ order ], 
						desc is Array || desc == null ? desc : [ desc ]), findResponder]);
				else
					_context.meta_callComponent(_component, _methodName, [filter, first, max, order, desc, findResponder]);
			}
		}
		
		CONFIG::flex45 {
			protected function doFind(filter:Object, first:int, max:int, sort:ISort, findResponder:PagedCollectionResponder):void { 			
				// Force evaluation of max, results and count
				var order:* = null;
				var desc:* = null;
				if (this.multipleSort) {
					if (sort != null && sort.fields.length > 0) {
						order = [];
						desc = [];
						for each (var s:ISortField in sort.fields) {
							order.push(s.name);
							desc.push(s.descending);
						}
					}
				}
				else {
					order = sort != null && sort.fields.length > 0 ? sort.fields[0].name : null;
					desc = sort != null && sort.fields.length > 0 ? sort.fields[0].descending : false;
				}
				
				var usePage:Boolean = _usePage;
				var method:Method = Type.forInstance(_component).getMethod(function(m:Method):Boolean { return m.name == _methodName; });
				if (method != null && method.parameters.length >= 2 && Parameter(method.parameters[1]).type.equals(Type.forClass(PageInfo)))
					usePage = true;

				if (usePage)
					_context.meta_callComponent(_component, _methodName, [filter, new PageInfo(first, max, order, desc), findResponder]);
				else
					_context.meta_callComponent(_component, _methodName, [filter, first, max, order, desc, findResponder]);
			}
		}
		
		
		
		protected override function getResult(event:TideResultEvent, first:int, max:int):Object {
	    	if (!event.result.hasOwnProperty("firstResult"))
	    		event.result.firstResult = first;
	    	if (!event.result.hasOwnProperty("maxResults"))
	    		event.result.maxResults = max;
		    return event.result;
		}
		
		
		private function filterChangedHandler(event:Event):void {
		    _fullRefresh = true;
		    _filterRefresh = true;
		}
		
		
		/**
		 * IPropertyHolder interface
		 */
		public function get object():Object {
			if (_component is IPropertyHolder)
		    	return IPropertyHolder(_component).object;
		    return null;
		}
		
        public function meta_propertyResultHandler(propName:String, event:ResultEvent):void {
        	if (_component is IPropertyHolder)
        		IPropertyHolder(_component).meta_propertyResultHandler(propName, event);
        }
		
	}
}
