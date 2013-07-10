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
    import flash.events.IEventDispatcher;
    
    import mx.collections.*;
    import mx.collections.errors.ItemPendingError;
    import mx.core.IUID;
    import mx.core.mx_internal;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.PropertyChangeEvent;
    import mx.logging.ILogger;
    import mx.logging.Log;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideContextEvent;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.util.ClassUtil;
    
    use namespace mx_internal;
	
	
	[Bindable]
	/**
	 * 	Base implementation of a paged collection that supports ItemPendingError.<br/>
	 *  The collection holds two complete pages of data.<br/>
	 * 	<br/>
	 * 	Subclassed have to implement a getResult method that will retrieve a range of elements.
	 */
	public class PagedCollection extends ListCollectionView {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.collections.PagedCollection");
        
        
        public static const COLLECTION_PAGE_CHANGE:String = "collectionPageChange";

        public static const RESULT:String = "result";
        public static const FAULT:String = "fault";
        
		
		/**
		 * 	@private
		 */
		protected var _componentName:String = null;
		protected var _context:BaseContext = null;
        protected var _initializing:Boolean = false;
        private var _initSent:Boolean = false;
        private var _sortFieldListenersSet:int = 0;
        
		/**
		 * 	@private
		 */
		protected var _first:int;			// Current first index of local data
		/**
		 * 	@private
		 */
        protected var _last:int;			// Current last index of local data
		/**
		 * 	@private
		 */
        protected var _max:int;           // Page size
		protected var _clientMax:int	  // Page size as specified on the client
		protected var _clientMaxSet:Boolean = false;
		/**
		 * 	@private
		 */
        protected var _count:int;         // Result count
		/**
		 * 	@private
		 */
		protected var _list:IList;		  // Internal wrapped list
		/**
		 * 	@private
		 */
		protected var _fullRefresh:Boolean = false;
		/**
		 * 	@private
		 */
		protected var _filterRefresh:Boolean = false;
		private var _ipes:Array;		// Array of ItemPendingErrors

		/**
		 * @private
		 */
		protected var _autoRefresh:Boolean = true;
		
		// GDS-523
		public var uidProperty:String = "uid";
		
		// GDS-712
		public var multipleSort:Boolean = true;
		
		public var dispatchResetOnInit:Boolean = Tide.detectFlex4();		
		public var throwIPEDuringResponders:Boolean = false;
		public var triggerRefreshOnIPEWithoutResponder:Boolean = false;
		public var dispatchReplaceOnResult:Boolean = Tide.detectFlex4();
		
		
		public function PagedCollection() {
		    log.debug("create collection");
			super();
			var sort:Sort = new Sort();
			sort.compareFunction = nullCompare;
			sort.fields = [];
			this.sort = sort;
			_ipes = null;
			_first = 0;
			_last = 0;
			_count = 0;
			list = null;
			_initializing = true;
			addEventListener("sortChanged", sortChangedHandler);
		}
		
		
		public override function toString():String {
			if (_initializing)
				return "[Initializing paged collection]";
			
			var s:String = "PagedCollection(" + _first + "-" + _last + ") [";
			if (localIndex == null)
				s += "No local index";
			else {
				for (var i:int = 0; i < localIndex.length; i++) {
					if (i > 0)
						s += ", ";
					s += BaseContext.toString(localIndex[i]);
				}
			}
			s += "]";
			return s;
		}
		
		
		public function meta_init(componentName:String, context:BaseContext):void {
			_componentName = componentName;
			_context = context;
		}
		
		
		/**
		 *	Get total number of elements
		 *  
		 *  @return collection total length
		 */
		public override function get length():int {
		    if (_initializing) {
		    	if (!_initSent) {
		    		log.debug("initial find");
			    	find(0, _max);
			    	_initSent = true;
			    }
		        return 0;
		    }
		    else if (localIndex == null)
		        return 0;
			return _count;
		}
		
		/**
		 *  Set the page size. The collection will store in memory twice this page size, and each server call
		 *  will return at most the page size.
		 * 
		 *  @param max maximum number of requested elements
		 */
		public function set maxResults(max:int):void {
			_max = max;
			_clientMax = max;
			_clientMaxSet = true;
		}
		
		
		private var _elementName:String;
		
		public function set elementClass(elementClass:Class):void {
			if (_elementName != null)
	        	_context.removeEventListener("org.granite.tide.data.refresh." + _elementName, refreshHandler);
			
			_elementName = elementClass != null ? ClassUtil.getUnqualifiedClassName(elementClass) : null;
			
			if (_autoRefresh && _elementName != null)
	        	_context.addEventListener("org.granite.tide.data.refresh." + _elementName, refreshHandler, false, 0, true);
		}
		
		/**
		 *  Set whether or not the collection will subscribe to org.granite.tide.data.refresh.<Entity Name> 
		 *  events and automatically refresh when they are dispatched
		 * 
		 * 	@param autoRefresh enable automatic refresh on external data events
		 */
		public function set autoRefresh(autoRefresh:Boolean):void {
			_autoRefresh = autoRefresh;
			
			// In case _elementName is already set
			if (!autoRefresh && _elementName != null)
	        	_context.removeEventListener("org.granite.tide.data.refresh." + _elementName, refreshHandler);
		}
		
		/**
		 * 	Clear collection content
		 */
		public function clear():void {
			_ipes = null;
			clearLocalIndex();
			list = null;
			_first = 0;
			_last = 0;
			_count = 0;
			if (_clientMaxSet)
				_max = _clientMax;
			_fullRefresh = false;
			_filterRefresh = false;
			_initializing = true;
			_initSent = false;
		}
    	
    	
    	// GDS-524
		public var beforeFindCall:Function = null;
		
		
		/**
		 *	Abstract method: trigger a results query for the current filter
		 *	@param first	: index of first required result
		 *  @param last     : index of last required result
		 *  @param merge	: should merge the result with the current wrapped list
		 */
		protected function find(first:int, last:int, merge:Boolean = false):void {
			log.debug("find from {0} to {1}", first, last);
			
			if (beforeFindCall != null)
				beforeFindCall(first, last);
		}
		
		
		/**
		 *	Force refresh of collection when filter/sort have been changed
		 * 
		 *  @return always false
		 */
		public function fullRefresh():Boolean {
		    _fullRefresh = true;
		    return refresh();
		}
		
		/**
		 *	Refresh collection with new filter/sort parameters
		 * 
		 *  @return always false
		 */
		public override function refresh():Boolean {
			// Recheck sort fields to listen for asc/desc change events
			// (AdvancedDataGrid does not dispatch sortChanged when only asc/desc is changed)
			if (sort != null) {
				sort.compareFunction = nullCompare;
				if (sort.fields != null && sort.fields.length != _sortFieldListenersSet) {
					for each (var field:Object in sort.fields)
						field.addEventListener("descendingChanged", sortFieldChangeHandler, false, 0, true);
					_sortFieldListenersSet = sort.fields.length;
				}
			}
			
			// _ipes = null;
			
			if (_fullRefresh) {
				log.debug("full refresh");
				
				clearLocalIndex();

    			_fullRefresh = false;
    			if (_filterRefresh) {
    			    _first = 0;
    			    _last = _first+_max;
    			    _filterRefresh = false;
    			}
            }
            else
				log.debug("refresh");			
            
			if (!initialFind())
				find(_first, _last, true);
			return true;
		}
		
		private function initialFind():Boolean {
			if (_max > 0 && !_initializing)
				return false;
			
			if (!_initSent) {
				log.debug("initial find");
				find(0, _max);
				_initSent = true;
			}
			return true;
		}
		
		private function clearLocalIndex():void {
			// Force complete refresh after changing sorting or filtering
			localIndex = null;
		}

		
		private var _refreshFilter:Function = null;
		
		public function set refreshFilter(filter:Function):void {
			_refreshFilter = filter;
		}
		
		private function refreshHandler(event:TideContextEvent):void {
			if (_refreshFilter != null && !_refreshFilter(event.params[0]))
				return;
			
			fullRefresh();
		}
		
		
		/**
		 * 	Internal handler for sort events
		 * 
		 * 	@param event sort event
		 */
		private function sortChangedHandler(event:Event):void {
			if (this.sort != null)
				this.sort.compareFunction = nullCompare;	// Force compare function to do nothing as we use server-side sorting
		    _fullRefresh = true;
		}
		
        /**
 		 * Internal handler for sort field descending events
		 * 
		 * @param event descendingChange event
		 */
		private function sortFieldChangeHandler(event:Event):void {
			_fullRefresh = true;
		}
				
		/**
		 *  Build a result object from the result event
		 *  
		 *  @param event the result event
		 *  @param first first index requested
		 *  @param max max elements requested
		 *   
		 *  @return an object containing data from the collection
		 *      resultList   : the retrieved data
		 *      resultCount  : the total count of elements (non paged)
		 *      firstResult  : the index of the first retrieved element
		 *      maxResults   : the maximum count of retrieved elements 
		 */
		protected function getResult(event:TideResultEvent, first:int, max:int):Object {
		    throw new Error("Abstract method, must be implemented by subclasses");
		}
		
		/**
		 * 	@private
		 *  Initialize collection after first find
		 *   
		 *  @param the result event of the first find
		 */
		protected function initialize(event:TideResultEvent):void {
		}
		
		/**
		 * 	@private
		 *	Event handler for results query
		 * 
		 *  @param event the result event
		 *  @param first first requested index
		 *  @param max max elements requested
		 */
		protected function findResult(event:TideResultEvent, first:int, max:int):void {
		    var result:Object = getResult(event, first, max);
		    
		    handleResult(result, event);
		}
		
		/**
		 * 	@private
		 *	Event handler for results query
		 * 
		 *  @param result the result object
		 *  @param event the result event
		 */
		protected function handleResult(result:Object, event:TideResultEvent = null):void {
			var l:IList = result.resultList as IList;
			list = l is ListCollectionView ? ListCollectionView(l).list : l;
			
			if (this.sort != null)
				this.sort.compareFunction = nullCompare;	// Prevent automatic sorting
			
			var expectedFirst:int = 0;
			var expectedLast:int = 0;
			
			var dispatchReset:Boolean = _initializing && this.dispatchResetOnInit;
			if (_initializing && event) {
				if (_max == 0 && result.hasOwnProperty("maxResults"))
			    	_max = result.maxResults;
			    initialize(event);
			}
			
			var nextFirst:int = result.firstResult;
			var nextLast:int = nextFirst + result.maxResults;
			log.debug("handle result {0} - {1}", nextFirst, nextLast);
			
			if (!_initializing) {
			    expectedFirst = nextFirst;
			    expectedLast = nextLast;
			}
			var page:int = nextFirst / _max;
			// log.debug("findResult page {0} ({1} - {2})", page, nextFirst, nextLast);
			
			var newCount:int = result.resultCount;
			if (newCount != _count) {
			    var pce:PropertyChangeEvent = PropertyChangeEvent.createUpdateEvent(this, "length", _count, newCount); 
				_count = newCount;
				dispatchEvent(pce);
			}
		
		    _initializing = false;
			
			var i:int;
            var obj:Object;
            var dispatchRefresh:Boolean = (localIndex == null);
		    
		    var entityName:String;
		    var entityNames:Array = null;
		    if (localIndex != null) {
		    	entityNames = [];
		        for (i = 0; i < localIndex.length; i++) {
					entityName = ClassUtil.getUnqualifiedClassName(localIndex[i]);
					if (entityName != _elementName && entityNames.indexOf(entityName) < 0)
						entityNames.push(entityName);
		        }
		        for each (entityName in entityNames)
		        	_context.removeEventListener("org.granite.tide.data.refresh." + entityName, refreshHandler);
		    }
			localIndex = list.toArray();
		    if (localIndex != null) {
		    	entityNames = [];
		        for (i = 0; i < localIndex.length; i++) {
					entityName = ClassUtil.getUnqualifiedClassName(localIndex[i]);
					if (entityName != _elementName && entityNames.indexOf(entityName) < 0)
						entityNames.push(entityName);
		        }
		        if (_autoRefresh) {
		        	for each (entityName in entityNames)
		        		_context.addEventListener("org.granite.tide.data.refresh." + entityName, refreshHandler, false, 0, true);
		        }
		    }
		    
			// Must be before collection event dispatch because it can trigger a new getItemAt
			_first = nextFirst;
			_last = nextLast;
		    
		    if (_ipes != null) {
		        var nextIpes:Array = [];
		        
    		    while (_ipes.length > 0) {
    		        // Must pop the ipe before calling result
    		        var a0:Array = _ipes.pop() as Array;
    		        if (int(a0[1]) == expectedFirst && int(a0[2]) == expectedLast) {
    		            var ipe:ItemPendingError = a0[0] as ItemPendingError;
    		            if (ipe.responders != null) {
							log.debug("call IPE responders (received {0} - {1} expected {2} - {3})", int(a0[1]), int(a0[2]), expectedFirst, expectedLast);
    		            	var saveThrowIpe:Boolean = _throwIpe;
    		            	_throwIpe = throwIPEDuringResponders;
							try {
	            				// Trigger responders for current pending item
	            				for (var j:int = 0; j < ipe.responders.length; j++)
	            					ipe.responders[j].result(event);
							}
							finally {
            					_throwIpe = saveThrowIpe;
							}
    		            }
						else {
							// Client did not set a responder on the IPE ?
							log.debug("no responder attached to IPE (received {0} - {1} expected {2} - {3})", int(a0[1]), int(a0[2]), expectedFirst, expectedLast);
							if (triggerRefreshOnIPEWithoutResponder)
								dispatchRefresh = true;
						}
						if (dispatchReplaceOnResult) {
							for each (var it:int in a0[3]) {
								var ipce:PropertyChangeEvent = PropertyChangeEvent.createUpdateEvent(this, it, null, localIndex[it-_first]);
								dispatchEvent(new CollectionEvent(CollectionEvent.COLLECTION_CHANGE, false, false, CollectionEventKind.REPLACE, it, -1, [ipce]));
							}
						}
    		        }
    		        else
    		            nextIpes.push(a0);
    		    }
    		    
    		    _ipes = nextIpes;
    		}
			
//		    _ipes = null;
		    
		    if (event != null)
		    	dispatchEvent(new CollectionEvent(COLLECTION_PAGE_CHANGE, false, false, RESULT, -1, -1, [ event ]));
		    
		    if (dispatchRefresh) {
		        _tempSort = new NullSort();
	        	saveThrowIpe = _throwIpe;
				try {
		            _throwIpe = false;
			        super.refresh();
				}
				finally {
			    	_throwIpe = saveThrowIpe;
					_tempSort = null;
				}
		    }
		    
		    _maxGetAfterHandle = -1;
		    _firstGetNext = -1;
		    
		    if (dispatchReset)
		    	dispatchEvent(new CollectionEvent(CollectionEvent.COLLECTION_CHANGE, false, false, CollectionEventKind.RESET));
		}
    	
		
    	// All this ugly hack because ListCollectionView.revision is private
        mx_internal override function getBookmarkIndex(bookmark:CursorBookmark):int {
        	var saveThrowIpe:Boolean = _throwIpe;
			try {
	            _throwIpe = false;
	            return super.getBookmarkIndex(bookmark);
			}
			finally {
				_throwIpe = saveThrowIpe;
			}
			return -1;
        }
 

		[Bindable("listChanged")]
		public override function get list():IList {
			return _list;
		}
		
		public override function set list(value:IList):void {
			if (value == _list)
				return;
				
			if (_list)
				_list.removeEventListener(CollectionEvent.COLLECTION_CHANGE, listChangeHandler);
			
			_list = value;
			
			if (value)
				value.addEventListener(CollectionEvent.COLLECTION_CHANGE, listChangeHandler, false, 0, true);
		}
		
		private function listChangeHandler(event:CollectionEvent):void {
			// Propagate underlying list events to paged collection listeners 
			dispatchEvent(event);
		}
		
		
		// Override sort to disable client side sorting
		// while keeping the ability to define the sort fields
		private var _tempSort:NullSort = null;
		
        CONFIG::flex40 {
            [Bindable("sortChanged")]
            public override function get sort():Sort {
                if (_tempSort && !_tempSort.sorted)
                    return _tempSort;
                return super.sort;
            }

            public override function set sort(newSort:Sort):void {
                // Track changes on sort fields
                if (sort != null && sort.fields != null) {
                    for each (var field:Object in sort.fields)
                        field.removeEventListener("descendingChanged", sortFieldChangeHandler);
                }
                _sortFieldListenersSet = 0;
                super.sort = newSort;
            }
        }

        CONFIG::flex45 {
            [Bindable("sortChanged")]
            public override function get sort():ISort {
                if (_tempSort && !_tempSort.sorted)
                    return _tempSort;
                return super.sort;
            }

            public override function set sort(newSort:ISort):void {
                // Track changes on sort fields
                if (sort != null && sort.fields != null) {
                    for each (var field:Object in sort.fields)
                        field.removeEventListener("descendingChanged", sortFieldChangeHandler);
                }
                _sortFieldListenersSet = 0;
                super.sort = newSort;
            }
        }
		
		/**
		 *  @private
		 *	Event handler for results fault
		 *  
		 *  @param event the fault event
		 *  @param first first requested index
		 *  @param max max elements requested
		 */
		protected function findFault(event:TideFaultEvent, first:int, max:int):void {
			handleFault(event);
		}
		
		/**
		 * 	@private
		 *	Event handler for results query fault
		 * 
		 *  @param event the fault event
		 */
		protected function handleFault(event:TideFaultEvent):void {
			log.debug("findFault: {0} => {1} ({2})", event.target, event.fault);
			
			var nextIpes:Array = new Array();
			if (_ipes != null) {
				while (_ipes.length > 0) {
					var a:Array = _ipes.pop() as Array;
					var ipe:ItemPendingError = ItemPendingError(a[0]);
					
					if ((_max == 0 && int(a[1]) == 0 && int(a[2]) == 0) || (int(a[1]) == _first && int(a[2]) == _last)) {
						if (ipe.responders != null) {
				        	var saveThrowIpe:Boolean = _throwIpe;
							try {
					            _throwIpe = throwIPEDuringResponders;
								for (var j:int = 0; j < ipe.responders.length; j++)
									ipe.responders[j].fault(event);
							}
							finally {
    		            		_throwIpe = saveThrowIpe;
							}
						}
					}
					else
						nextIpes.push(a);
				}
				_ipes = nextIpes;
			}
		    
	    	dispatchEvent(new CollectionEvent(COLLECTION_PAGE_CHANGE, false, false, FAULT, -1, -1, [ event ]));
		    
		    _maxGetAfterHandle = -1;
		    _firstGetNext = -1;
		}
		
		
		private var _throwIpe:Boolean = true;		
		private var _maxGetAfterHandle:int = -1;
		private var _firstGetNext:int = -1;
		
		
		/**
		 * 	Override of getItemAt with ItemPendingError management
		 * 
		 *	@param index index of requested item
		 *	@param prefetch not used
		 *  @return object at specified index
		 */
		public override function getItemAt(index:int, prefetch:int = 0):Object {
			if (index < 0)
				return null;
		
			var ipe:ItemPendingError;
			
			// log.debug("get item at {0}", index);
			
			var a:Array;
			var i:int;
			if (_max == 0 || _initializing) {
				if (!_initSent) {
					log.debug("initial find");
				    find(0, _max);
				    _initSent = true;
				}
			    return null;
			}
			else {
				if (_firstGetNext == -1) {
					if (_maxGetAfterHandle == -1)
						_maxGetAfterHandle = index;
					else if (index > _maxGetAfterHandle)
						_maxGetAfterHandle = index;
						
					if (index < _maxGetAfterHandle)
						_firstGetNext = index;
				}
				else if (index > _maxGetAfterHandle && _firstGetNext < _maxGetAfterHandle)
					_firstGetNext = index;
			
    			if (localIndex && _ipes != null) {
    				// Check if requested page is already pending, and rethrow existing error
    				// Always rethrow when data is after (workaround for probable bug of Flex DataGrid)
    				for (i = 0; i < _ipes.length; i++) {
    					a = _ipes[i] as Array;
    					ipe = ItemPendingError(a[0]);
    					if (index >= int(a[1]) && index < int(a[2]) && int(a[2]) > _last && ipe.responders == null && index != _firstGetNext) {
    					    log.debug("forced rethrow of existing IPE for index {0} ({1} - {2})", index, int(a[1]), int(a[2]));
							a[3].push(index);
						    // log.debug("stacktrace {0}", ipe.getStackTrace());
    					    if (ListCollectionView.mx_internal::VERSION.substring(0, 1) != "2")
    						    throw ipe;
    					}
    				}
    			}
        	    
    			if (localIndex && index >= _first && index < _last) {	// Local data available for index
    			    var j:int = index-_first;
    			    // log.debug("getItemAt index {0} (current {1} to {2})", index, _first, _last);
    				return localIndex[j];
    			}
    			
    			if (!_throwIpe)
    			    return null;
    			
    			if (_ipes != null) {
    				// Check if requested page is already pending, and rethrow existing error
    				for (i = 0; i < _ipes.length; i++) {
    					a = _ipes[i] as Array;
    					ipe = ItemPendingError(a[0]);
    					if (index >= int(a[1]) && index < int(a[2])) {
    					    log.debug("rethrow existing IPE for index {0} ({1} - {2})", index, int(a[1]), int(a[2]));
							a[3].push(index);
    						throw ipe;
    					}
    				}
    			}
        	    
			    var page:int = index / _max;
			    
    			// Trigger a results query for requested page
    			var nfi:int = 0;
    			var nla:int = 0;
    			var idx:int = page * _max;
    			if (index >= _last && index < _last + _max) {
    				nfi = _first;
    				nla = _last + _max;
    				if (nla > nfi + 2*_max)
    				    nfi = nla - 2*_max;
    				if (nfi < 0)
    				    nfi = 0;
    				if (nla > _count)
    				    nla = _count;
    			}
    			else if (index < _first && index >= _first - _max) {
    				nfi = _first - _max;
    				if (nfi < 0)
    					nfi = 0;
    				nla = _last;
    				if (nla > nfi + 2*_max)
    				    nla = nfi + 2*_max;
    				if (nla > _count)
    				    nla = _count;
    			}
    			else {
    				nfi = index - _max;
    				nla = nfi + 2 * _max;
    				if (nfi < 0)
    					nfi = 0;
    				if (nla > _count)
    				    nla = _count;
    			}
				log.debug("request find for index " + index);
    			find(nfi, nla);
            }
			
			// Throw ItemPendingError for requested index
			// log.debug("ItemPendingError for index " + index + " triggered " + nfi + " to " + nla);
			ipe = new ItemPendingError("Items pending from " + nfi + " to " + nla + " for index " + index);
			if (_ipes == null)
				_ipes = new Array();
			_ipes.push([ipe, nfi, nla, [index]]);
		    log.debug("throw IPE for index {0} ({1} - {2})", index, nfi, nla);
		    // log.debug("stacktrace {0}", ipe.getStackTrace());
			throw ipe;
		}
		
		
		/**
		 * 	Override of getItemIndex
		 * 
		 * 	@param obj searched object
		 *  @return index of object in collection
		 */
		public override function getItemIndex(obj:Object):int {
			if (obj == null)
				return -1;
			
            // log.debug("getItemIndex {0}", obj);
			if (localIndex) {
				var idx:int;
				if (obj is IUID) {
					// Local data available: lookup by id
					for (idx = 0; idx < localIndex.length; idx++) {
						if (IUID(obj).uid == IUID(localIndex[idx]).uid)
							return _first + idx;
					}
				}
				else if (obj.hasOwnProperty(uidProperty)) {
					// GDS-523
					for (idx = 0; idx < localIndex.length; idx++) {
						if (obj[uidProperty] == localIndex[idx][uidProperty])
							return _first + idx;
					}
				}
			}
			
			return -1;
		}
		
		
		private function nullCompare(o1:Object, o2:Object, fields:Array = null):int {
			if (o1 is IUID && o2 is IUID) {
				if (IUID(o1).uid == IUID(o2).uid)
					return 0;
				return 1;
			}
			// GDS-523
			if (o1 != null && o2 != null && o1.hasOwnProperty(uidProperty) && o2.hasOwnProperty(uidProperty)) {
				if (o1[uidProperty] == o2[uidProperty])
					return 0;
				return 1;
			}
			return 0;
		}
	}
}


import mx.collections.Sort;

class NullSort extends Sort {
    
    private var _sorted:Boolean = false;
    
    public function get sorted():Boolean {
        return _sorted;
    }
    
    public override function sort(array:Array):void {
        _sorted = true;
    }
	
	public override function propertyAffectsSort(propertyName:String):Boolean {
		return false;
	}
}
