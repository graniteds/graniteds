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
package org.granite.tide.seam.framework {
    
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
    import mx.utils.ObjectUtil;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Component;
    import org.granite.tide.IComponent;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.Tide;
    import org.granite.tide.collections.PagedCollection;
    import org.granite.tide.collections.PagedCollectionResponder;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.service.ServerSession;

    [Bindable]
	/**
	 * 	Implementation of the Tide paged collection backed by a Seam Query component.<br/>
	 *  <br/>
	 *  The Seam component should have the same name as the Tide client component.<br/>
	 *  It must have a max-results set either in components.xml or in its constructor, <br/>
	 *  and its value must be greater than the maximum number of elements displayed.<br/>
	 *  <br/>
	 *  This component also implements ListCollectionView and can be directly used as a data provider.<br/> 
	 * 	for Flex UI components such as DataGrid.<br/>
	 * 
     * 	@author William DRAI
     */
	public class PagedQuery extends PagedCollection implements IComponent, IPropertyHolder {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.seam.PagedQuery");

        protected var _serverSession:ServerSession = null
	    private var _component:Component = null;
		
		private var _remoteComponentName:String = null;
        
        private var _restrictionParameters:Array = new Array();
		
		
		public function PagedQuery(serverSession:ServerSession = null) {
            _serverSession = serverSession;
        }

        public function set serverSession(serverSession:ServerSession):void {
            _serverSession = serverSession;
        }
		
		public override function meta_init(componentName:String, context:BaseContext):void {
			super.meta_init(componentName, context);
			_component = new Component();
			_component.meta_init(componentName, _context);
			_component.meta_templateObject = this;
			Object(_component).firstResult = 0;
		}
		
		public function get meta_name():String {
		    return _component.meta_name;
		}
		
		public function meta_clear():void {
		    _component.meta_clear();
		    if (_remoteComponentName != null)
		    	_context[_remoteComponentName] = null;
		    super.clear();
		}
		
		public function set remoteComponentName(remoteComponentName:String):void {
			if (remoteComponentName != _componentName) {
				if (remoteComponentName != _remoteComponentName) {
					_remoteComponentName = remoteComponentName;
					_component = _context[remoteComponentName];
					Object(_component).firstResult = 0;
				}
			}
			else {
				if (_remoteComponentName != null) {
					_remoteComponentName = null;
					_context[_remoteComponentName] = null;
				}
				_component = new Component();
				_component.meta_init(_componentName, _context);
				_component.meta_templateObject = this;
				Object(_component).firstResult = 0;
			}
		}
    	
    	
		/**
		 * 	@private
		 *	Trigger a results query for the current filter
		 *	@param first	: index of first required result
		 *  @param last     : index of last required result
		 *  @param merge	: should merge result with current wrapped list
		 */
		protected override function find(first:int, last:int, merge:Boolean = false):void {
			super.find(first, last, merge);
			
			Object(_component).firstResult = first;
			// Force evaluation of max, results and count
			var max:* = Object(_component).maxResults;
			if (_initializing && _max > 0)
				Object(_component).maxResults = _max;
			else if (!_initializing)
			    Object(_component).maxResults = last-first;
			
			if (_initializing) {
				// Force retrieval of restrictions list
			    var restrictions:* = Object(_component).restrictions;
			}
			var results:* = Object(_component).resultList;
			_count = Object(_component).resultCount;
			var order:String = null;
			if (sort != null && sort.fields.length > 0) {
				order = "";
				for (var idx:int = 0; idx < sort.fields.length; idx++) {
					if (idx > 0)
						order += ", ";
			    	order += sort.fields[idx].name + (sort.fields[idx].descending ? " desc" : "");
			    }
			}
			if (order != Object(_component).order)
			    _fullRefresh = true;
			Object(_component).order = order;
			
			if (!_initializing) {
    			// Force send of restriction parameters
    			for each (var param:Array in _restrictionParameters) {
    			    if (param.length > 1)
		        		_context.meta_addUpdate(String(param[0]), String(param[1]), _context[param[0]][param[1]]);
    			}
            }
			
			var findResponder:PagedCollectionResponder = new PagedCollectionResponder(findResult, findFault, 
				_initializing ? null : this, first, last-first);
			Object(_component).refresh(findResponder);
		}
		
		
		/**
		 * 	Get the current result for the collection
		 *  Here we get it from the wrapped component
		 * 
		 *  @param event result event
		 *  @param first first index
		 *  @param max max number of elements
		 */
		protected override function getResult(event:TideResultEvent, first:int, max:int):Object {
		    var result:Object = new Object();
		    result.resultList = Object(_component).resultList;
		    result.resultCount = Object(_component).resultCount;
		    result.firstResult = Object(_component).firstResult;
		    result.maxResults = Object(_component).maxResults;
		    return result;
		}
		
		/**
		 * 	@private
		 *  Initialization of the collection
		 * 
		 *  @param event result event
		 */
		protected override function initialize(event:TideResultEvent):void {
		    var restrictions:ArrayCollection = Object(_component).restrictions as ArrayCollection;
		    for each (var r:String in restrictions) {
		        var idx:int = r.indexOf("#{");
		        while (idx >= 0) {
		            var nidx:int = r.indexOf("}", idx+2);
		            var expr:String = r.substring(idx+2, nidx);
		            var dot:int = expr.indexOf(".");
		            if (dot >= 0) {
		                var ex:Array = expr.split(".");
		                _restrictionParameters.push(ex);
		                var restrictionParameter:Object = _context[ex[0]];
		                if (restrictionParameter == null)
		                    throw new Error("Restriction parameter " + ex[0] + " not instantiated, add create='true' or autoCreate='true' for automatic instantiation");
            			_context.meta_tide.setComponentRemoteSync(ex[0], Tide.SYNC_BIDIRECTIONAL);
	                    _context[ex[0]].addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, restrictionParameterChangeHandler, false, 0, true);
	                }
	                
	                idx = r.indexOf("#{", nidx);
		        }
		    }
		}
		
		
		private function restrictionParameterChangeHandler(event:PropertyChangeEvent):void {
		    for each (var rest:Array in _restrictionParameters) {
		        if (rest.length > 1 && rest[1] == event.property) {
        		    _fullRefresh = true;
        		    _filterRefresh = true;
        		    break;
        		}
		    }
		}
		
		
		/**
		 * 	IPropertyHolder interface, wrapped object
		 */
		public function get object():Object {
		    return Object(_component).object;
		}
		
		/**
		 * 	IPropertyHolder interface, property change handler
		 * 
		 *  @param propName property name
		 *	@param event result event 
		 */
        public function meta_propertyResultHandler(propName:String, event:ResultEvent):void {
            if (propName == "order")
                _fullRefresh = true;
            
            Object(_component).meta_propertyResultHandler(propName, event);
        }
		
	}
}
