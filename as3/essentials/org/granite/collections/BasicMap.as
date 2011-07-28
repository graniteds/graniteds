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

package org.granite.collections {

    import flash.events.Event;
    import flash.events.EventDispatcher;
    import flash.events.IEventDispatcher;
    import flash.utils.Dictionary;
    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;
    import flash.utils.Proxy;
    import flash.utils.flash_proxy;
    
    import mx.collections.ArrayCollection;
    import mx.collections.ArrayList;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.PropertyChangeEvent;
    import mx.utils.ObjectUtil;
    
    import org.granite.util.Enum;

    use namespace flash_proxy;


    [RemoteClass(alias="org.granite.collections.BasicMap")]
    /**
     *	Basic implementation of HashMap
     *  
     * 	@author Franck WOLFF
     */
    public dynamic class BasicMap extends Proxy implements IMap, IExternalizable, IEventDispatcher {
    
        private var dispatcher:EventDispatcher;
        
        private var _keySet:ArrayList = null;
        private var _values:ArrayList = new ArrayList();
        private var _entrySet:Dictionary = new Dictionary();

        ///////////////////////////////////////////////////////////////////////
        // Constructor.

        public function BasicMap(keySet:ArrayList = null) {
			super();
			dispatcher = new EventDispatcher(this);
			
            if (keySet) {
                keySet.removeAll(); // ensure consistency...
                _keySet = keySet;
            }
            else
                _keySet = new ArrayList();
        }

        ///////////////////////////////////////////////////////////////////////
        // Proxy implementation.

        override flash_proxy function callProperty(name:*, ... rest):* {
            name = unpack(name);
            return _entrySet[name].apply(_entrySet, rest);
        }

        override flash_proxy function deleteProperty(name:*):Boolean {
            name = unpack(name);
            return remove(name) !== null;
        }

        override flash_proxy function getProperty(name:*):* {
            name = unpack(name);
            return get(name);
        }

        override flash_proxy function hasProperty(name:*):Boolean {
            name = unpack(name);
            return containsKey(name);
        }

        override flash_proxy function setProperty(name:*, value:*):void {
            name = unpack(name);
            put(name, value);
        }

        override flash_proxy function nextName(index:int):String {
            return _keySet.getItemAt(index - 1) as String;
        }

        override flash_proxy function nextNameIndex(index:int):int {
            return (index < _keySet.length ? (index + 1) : 0);
        }

        override flash_proxy function nextValue(index:int):* {
            return _entrySet[_keySet.getItemAt(index - 1)];
        }

        ///////////////////////////////////////////////////////////////////////
        // IMap implementation.

        public function get keySet():ArrayCollection {
            return new ArrayCollection(_keySet.toArray());
        }

        public function get values():ArrayCollection {
            return new ArrayCollection(_values.toArray());
        }

        public function get(key:*):* {
            var value:* = _entrySet[key];
            return (value ? (value as Array)[1] : null);
        }

        public function containsKey(o:*):Boolean {
            return _keySet.getItemIndex(o) != -1;
        }

        public function containsValue(o:*):Boolean {
            return _values.getItemIndex(o) != -1;
        }

        public function remove(key:*):Object {
            var oldValue:Object = null;
            if (containsKey(key)) {
                oldValue = (_entrySet[key] as Array)[1];
                _values.removeItemAt(_values.getItemIndex(oldValue));
                _keySet.removeItemAt(_keySet.getItemIndex(key));
                delete _entrySet[key];
                
                var items:Array = new Array();
                items.push([key, oldValue]);
                var ce:CollectionEvent = new CollectionEvent(CollectionEvent.COLLECTION_CHANGE, false, false, 
                    CollectionEventKind.REMOVE, -1, -1, items);
                dispatcher.dispatchEvent(ce);
            }
            return oldValue;
        }

        public function put(key:*, value:*):* {
            var oldValue:Object = null;
            if (!containsKey(key)) {
                _keySet.addItem(key);
                _values.addItem(value);
                _entrySet[key] = [key, value];
                
                var items:Array = new Array();
                items.push([key, value]);
                var ce:CollectionEvent = new CollectionEvent(CollectionEvent.COLLECTION_CHANGE, false, false, 
                    CollectionEventKind.ADD, -1, -1, items);
                dispatcher.dispatchEvent(ce);
                
                return oldValue;
            }
            oldValue = (_entrySet[key] as Array)[1];
            _values.removeItemAt(_values.getItemIndex(oldValue));
            _values.addItem(value);
            (_entrySet[key] as Array)[1] = value;
            
            items = new Array();
            var pce:PropertyChangeEvent = PropertyChangeEvent.createUpdateEvent(this, key, oldValue, value);
            items.push(pce);
            ce = new CollectionEvent(CollectionEvent.COLLECTION_CHANGE, false, false, 
                CollectionEventKind.REPLACE, -1, -1, items);
            dispatcher.dispatchEvent(ce);
            
            return oldValue;
        }

        public function clear():void {
            _keySet.removeAll();
            _values.removeAll();
            _entrySet = new Dictionary();
            
            var ce:CollectionEvent = new CollectionEvent(CollectionEvent.COLLECTION_CHANGE, false, false, 
                CollectionEventKind.RESET, -1, -1, null);
            dispatcher.dispatchEvent(ce);
        }

        public function get length():int {
            return _keySet.length;
        }

        ///////////////////////////////////////////////////////////////////////
        // IExternalizable implementation.

        public function readExternal(input:IDataInput):void {
            var elements:Array = input.readObject() as Array;
            for each (var pair:Array in elements) {
				pair[0] = Enum.checkForConstant(pair[0]);
				pair[1] = Enum.checkForConstant(pair[1]);
                _keySet.addItem(pair[0]);
                _values.addItem(pair[1]);
                _entrySet[pair[0]] = pair;
            }
        }

        public function writeExternal(output:IDataOutput):void {
            var elements:Array = new Array(_keySet.length);
            var i:int = 0;
            for each (var value:Array in _entrySet)
                elements[i++] = value;
            output.writeObject(elements);
        }

        ///////////////////////////////////////////////////////////////////////
        // Utility.

        private function unpack(name:*):* {
            return (name is QName ? (name as QName).localName : name);
        }
        
        
        public function dispatchEvent(event:Event):Boolean {
            return dispatcher.dispatchEvent(event);
        }
        
        public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false):void {
            dispatcher.addEventListener(type, listener, useCapture, priority, useWeakReference);
        }
        
        public function removeEventListener(type:String, listener:Function, useCapture:Boolean = false):void {
            dispatcher.removeEventListener(type, listener, useCapture);
        }
        
        public function hasEventListener(type:String):Boolean {
            return dispatcher.hasEventListener(type);
        }
        
        public function willTrigger(type:String):Boolean {
            return dispatcher.willTrigger(type);
        }

        public function toString():String {
            return _entrySet.toString();
        }
    }
}