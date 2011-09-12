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

package org.granite.persistence {

import flash.events.Event;
import flash.utils.IDataInput;
    import flash.utils.IDataOutput;

import mx.core.EventPriority;

import mx.core.IUID;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.PropertyChangeEvent;
    
    import org.granite.IValue;
    import org.granite.collections.IPersistentCollection;
    import org.granite.collections.UIDList;


    /**
     * @author Franck WOLFF
     */
    public class AbstractPersistentList extends UIDList implements IPersistentCollection {

        private var _initializing:Boolean = false;
        private var _initialized:Boolean = false;
        private var _metadata:String = null;
        private var _dirty:Boolean = false;


        public function AbstractPersistentList(initialized:Boolean = true):void {
            _initialized = initialized;
            if (_initialized)
    	        addEventListener(CollectionEvent.COLLECTION_CHANGE, dirtyCheckHandler, false, 1000);
        }


        final public function isInitialized():Boolean {
            return _initialized;
        }

        final public function initializing():void {
            removeAll();
            _initializing = true;
            _dirty = false;
            removeEventListener(CollectionEvent.COLLECTION_CHANGE, dirtyCheckHandler);
        }

        final public function initialize():void {
            _initializing = false;
            _initialized = true;
            _dirty = false;
            addEventListener(CollectionEvent.COLLECTION_CHANGE, dirtyCheckHandler);
        }

        final public function uninitialize():void {
			removeEventListener(CollectionEvent.COLLECTION_CHANGE, dirtyCheckHandler);
            _initialized = false;
			removeAll();
            _dirty = false;
        }
        
        protected function internalClone(clazz:Class):AbstractPersistentList {
        	var coll:AbstractPersistentList = new clazz(_initialized) as AbstractPersistentList;
        	coll._metadata = _metadata;
        	coll._dirty = _dirty;
        	if (_initialized) {
        		for each (var obj:Object in this)
        			coll.addItem(obj);
        	} 
        	return coll;
        }
        
        private function dirtyCheckHandler(event:CollectionEvent):void {
            if (!_initialized)
                return;
			if (event.kind == CollectionEventKind.ADD)
				_dirty = true;
			else if (event.kind == CollectionEventKind.REMOVE)
				_dirty = true;
			else if (event.kind == CollectionEventKind.RESET)
				_dirty = true;
			else if (event.kind == CollectionEventKind.REPLACE)
				_dirty = true;
        }


        override public function readExternal(input:IDataInput):void {
            _initialized = input.readObject() as Boolean;
            _metadata = input.readObject() as String;
            if (_initialized) {
                _dirty = input.readObject() as Boolean;
                super.readExternal(input);
            }
        }

        override public function writeExternal(output:IDataOutput):void {
            output.writeObject(_initialized);
            output.writeObject(_metadata);
            if (_initialized) {
                output.writeObject(_dirty);
                super.writeExternal(output);
            }
        }
    }
}
