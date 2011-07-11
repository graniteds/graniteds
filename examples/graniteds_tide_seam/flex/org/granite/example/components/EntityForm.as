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

package org.granite.example.components {
	
    import flash.display.DisplayObject;
    import flash.events.Event;
    
    import mx.containers.Form;
    import mx.core.Container;
    import mx.core.IFactory;
    import mx.events.ChildExistenceChangedEvent;
    import mx.events.FlexEvent;
    import mx.events.ValidationResultEvent;
    
    import org.granite.tide.IEntity;
    import mx.data.utils.Managed;
    

    /**
     * @author William DRAI
     */
    [Event(name="valid", type="mx.events.ValidationResultEvent")]
     
    [Event(name="invalid", type="mx.events.ValidationResultEvent")]
    
    public class EntityForm extends Form {
        
        private var _validationErrors:Array = new Array();
        
        public var itemEditor:IFactory;
        
        
        public function EntityForm() {
            super();
            
            itemEditor = new ItemEditorFactory();
            
            addEventListener(ChildExistenceChangedEvent.CHILD_ADD, childAddHandler);
        }
        
        override public function set data(obj:Object):void {
            trace("data changed: " + obj);
            super.data = obj;
            
            _validationErrors = new Array();
            
            getChildren().forEach(updateChildData);
            
            dispatchEvent(new FlexEvent(FlexEvent.DATA_CHANGE));
        }
        
        private function childAddHandler(event:ChildExistenceChangedEvent):void {
            getChildren().forEach(updateChildData);
        }
        
        private function updateChildData(element:*, index:int, arr:Array):void {
            if (element is FormInput)
                FormInput(element).data = this.data;
            else if (element is Container)
                Container(element).getChildren().forEach(updateChildData);
        }
        
        
        public override function dispatchEvent(event:Event):Boolean {
            var idx:int;
            if (event.type == ValidationResultEvent.INVALID) {
                idx = _validationErrors.indexOf(event.currentTarget);
                if (idx < 0)
                    _validationErrors.push(event.currentTarget);
                return super.dispatchEvent(event);
            }
            else if (event.type == ValidationResultEvent.VALID) {
                idx = _validationErrors.indexOf(event.currentTarget);
                if (idx >= 0)
                    _validationErrors.splice(idx, 1);
                
                if (_validationErrors.length == 0)
                    return super.dispatchEvent(event);
                
                return false;
            }
            else
                return super.dispatchEvent(event);
        }
        
        
        public function cancel():void {
            if (data is IEntity)
                Managed.resetEntity(IEntity(data));
        }
    }
}
