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

package test.granite.components {

    import flash.display.DisplayObjectContainer;
    import flash.events.Event;
    import flash.events.TextEvent;
    
    import mx.binding.utils.BindingUtils;
    import mx.containers.FormItem;
    import mx.controls.listClasses.IListItemRenderer;
    import mx.core.ClassFactory;
    import mx.core.Container;
    import mx.core.EventPriority;
    import mx.core.IFactory;
    import mx.core.UIComponent;
    import mx.events.FlexEvent;
    import mx.events.PropertyChangeEvent;
    import mx.events.ValidationResultEvent;
    import mx.validators.EmailValidator;
    import mx.validators.StringValidator;
    import mx.validators.Validator;
    
    import org.granite.meta;
    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    

    /**
     * @author William DRAI
     */
    public class FormInput extends FormItem {

        public var itemEditor:IFactory;
        public var dataField:String;
        
        private var delegate:UIComponent;
        

        public function FormInput() {
            super();
        }

        override protected function commitProperties():void {
            super.commitProperties();
        }

        private function parentForm(parent:DisplayObjectContainer):EntityForm {
            while (parent && !(parent is EntityForm))
                parent = parent.parent;

            return parent && parent is EntityForm ? EntityForm(parent) : null;
        }

        override public function parentChanged(parent:DisplayObjectContainer):void {
            super.parentChanged(parent);

            var form:EntityForm = parentForm(parent);
            if (form)
                data = form.data;
        }

        override public function set data(obj:Object):void {
        	if (super.data)
        		super.data.removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, propertyChangeHandler);
        	
            super.data = obj;
            
            if (obj) {
            	obj.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, propertyChangeHandler, false, 0, true);
	            initEditor(obj);
	       	}
            
            // Dispatch the dataChange event.
            dispatchEvent(new FlexEvent(FlexEvent.DATA_CHANGE));
        }
        
        private function propertyChangeHandler(event:PropertyChangeEvent):void {
        	if (itemEditor && delegate && event.property == dataField)
				delegate[Object(itemEditor).editorDataField] = event.newValue;
        }


        private function setPropertyValue(obj:Object, value:Object):void {
        	if (obj == null)
        		return;
            var properties:Array = dataField.split(/\./);
            var o:Object = obj;
            for (var i:uint = 0; i < properties.length-1 && o != null; i++)
                o = o[properties[i]];
            o[properties[properties.length-1]] = value;
        }

        override public function addEventListener(type:String, listener:Function, useCapture:Boolean = false, priority:int = 0, useWeakReference:Boolean = false):void {
            if (delegate)
                delegate.addEventListener(type, listener, useCapture, priority, useWeakReference);
        }

        private function updateDelegate():void {
            removeAllChildren();
            
            if (delegate) {
                addChild(delegate);
                delegate.addEventListener(FlexEvent.VALUE_COMMIT, valueChangeHandler);
            }
        }


        private function valueChangeHandler(event:Event):void {
            if (!itemEditor)
                initEditor(data);
            
            setPropertyValue(data, delegate[Object(itemEditor).editorDataField]);

            if (delegate)
                delegate.dispatchEvent(new ValidationResultEvent(ValidationResultEvent.VALID, true, false, Object(itemEditor).editorDataField, null));
        }

        
        private function initEditor(obj:Object):void {
            if (obj != null) {
                if (!itemEditor) {
                    var form:EntityForm = parentForm(parent);
                    if (!(form.itemEditor is ItemEditorFactory))
                        itemEditor = form.itemEditor;
                    else
                        itemEditor = new ItemEditorFactory();
                }
                if (itemEditor is ItemEditorFactory) {
                    var ief:ItemEditorFactory = ItemEditorFactory(itemEditor);
                    ief.data = obj;
                    ief.dataField = dataField;
                    ief.validHandler = validHandler;
                    ief.invalidHandler = invalidHandler;
                }
                
                delegate = itemEditor.newInstance();
                updateDelegate();
            }
        }


        private function validHandler(event:ValidationResultEvent):void {
            setPropertyValue(data, delegate[Object(itemEditor).editorDataField]);

            var form:EntityForm = parentForm(parent);
            if (form)
                form.dispatchEvent(event);
        }

        private function invalidHandler(event:ValidationResultEvent):void {
            var form:EntityForm = parentForm(parent);
            if (form)
                form.dispatchEvent(event);
        }
    }
}
