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
	
    import flash.utils.Dictionary;
    import flash.utils.describeType;
    import flash.utils.getDefinitionByName;
    
    import mx.controls.CheckBox;
    import mx.controls.ComboBox;
    import mx.controls.TextInput;
    import mx.core.IFactory;
    import mx.events.ValidationResultEvent;
    import mx.validators.EmailValidator;
    import mx.validators.StringValidator;
    import mx.validators.Validator;
    
    import org.granite.util.Enum;
    import org.granite.tide.IEntity;
    import org.granite.tide.validators.TideEntityValidator;
    

    /**
     * @author William DRAI
     */
    public class ItemEditorFactory implements IFactory {
        
        public var data:Object;
        public var dataField:String;
        public var editorDataField:String = "text";
        public var validHandler:Function;
        public var invalidHandler:Function;
        
        private static var _validators:Dictionary = new Dictionary(true);
        
        
        public function ItemEditorFactory() {
        }
        
        
        public function newInstance():* {
            if (data == null || dataField == null) {
                trace("Trying to create item editor with non initialized data");
                return new TextInput();    // Default
            }
            
            var o:Object = getProperty(data);
            var propertyName:String = getPropertyName(data);
            trace("Initializing editor for property " + propertyName + " current " + o[propertyName]);
            var description:XML = describeType(o);
            var type:String = description..accessor.(@name == propertyName).@type.toXMLString();
            
            var delegate:Object = null;
            var typeClass:Class = getDefinitionByName(type) as Class;
            var typeDescr:XML = describeType(typeClass);
            if (type == "Boolean") {
            	delegate = new CheckBox();
            	editorDataField = "selected";
            }
            else if (typeDescr..factory..extendsClass.(@type == 'org.granite.util::Enum').length() > 0) {
                delegate = new ComboBox();
                delegate.labelField = "name";
                var constants:Array = typeClass['constants'];
                var constantsProvider:Array = new Array(constants.length);
                for (var i:int = 0; i < constants.length; i++) {
                    var enum:Enum = constants[i] as Enum;
                    constantsProvider[i] = enum;
                }
                delegate.dataProvider = constantsProvider;
                editorDataField = "selectedItem"; 
            }
            else {
                delegate = new TextInput();
                editorDataField = "text";
            }
            
            // Initialize value before adding validators to avoid premature validation triggers
            delegate[editorDataField] = getPropertyValue(data);
            
            var required:Boolean = true;
            var notEmpty:XMLList = description..accessor.(@name == propertyName).metadata.(@name == 'NotEmpty');
            required = (notEmpty && notEmpty.length() > 0);
            
            var hasValidator:Boolean = false;
            var length:XMLList = description..accessor.(@name == propertyName).metadata.(@name == 'Length');
            if (length && length.length() > 0) {
                var stringValidator:StringValidator = new StringValidator();
                stringValidator.minLength = length..arg.(@key == 'min').@value.toXMLString();
                stringValidator.maxLength = length..arg.(@key == 'max').@value.toXMLString();
                stringValidator.required = true;        // True because Hibernate Length validator also requires notEmpty
                hasValidator ||= registerValidator(stringValidator, delegate);
            }
            var email:XMLList = description..accessor.(@name == propertyName).metadata.(@name == 'Email');
            if (email && email.length() > 0) {
                var emailValidator:EmailValidator = new EmailValidator();
                emailValidator.required = true;        // True because Hibernate Length validator also requires notEmpty
                hasValidator ||= registerValidator(emailValidator, delegate);
            }
            if (!hasValidator && required) {
                var validator:Validator = new Validator();
                hasValidator ||= registerValidator(validator, delegate);
            }
            
            if (delegate && data is IEntity) {
                var sv:TideEntityValidator = new TideEntityValidator();
                sv.entity = IEntity(data);
                sv.property = propertyName;
                sv.listener = delegate;
            }
            
            return delegate;
        }


        private function getProperty(obj:Object):Object {
            var properties:Array = dataField.split(/\./);
            var o:Object = obj;
            for (var i:uint = 0; i < properties.length-1 && o != null; i++)
                o = o[properties[i]];
            return o;
        }

        private function getPropertyName(obj:Object):String {
            var properties:Array = dataField.split(/\./);
            return properties[properties.length-1];
        }

        private function getPropertyValue(obj:Object):Object {
            var properties:Array = dataField.split(/\./);
            var value:Object = obj;
            for (var i:uint = 0; i < properties.length && value != null; i++)
                value = value[properties[i]];
            return value;
        }
        
        
        private function registerValidator(validator:Validator, delegate:Object):Boolean {
            validator.source = delegate;
            validator.property = editorDataField;
            
            var validators:Array = _validators[delegate];
            if (validators == null) {
                validators = new Array();
                _validators[delegate] = validators;
            }
            validators.push(validator);
            
            var hasValidator:Boolean = false;
            if (validHandler != null) {
                validator.addEventListener(ValidationResultEvent.VALID, validHandler);
                hasValidator = true;
            }
            if (invalidHandler != null) {
                validator.addEventListener(ValidationResultEvent.INVALID, invalidHandler);
                hasValidator = true;
            }
            return hasValidator;
        }
        
        
        public static function validateAll(component:Object):Array {
            var validators:Array = _validators[component] as Array;
            if (validators)
                return Validator.validateAll(validators);
            return null;
        }
    }
}