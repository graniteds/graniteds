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

package org.granite.tide.impl {
	
	import org.granite.reflect.Annotation;
	import org.granite.reflect.Field;
	import org.granite.reflect.Method;
	import org.granite.reflect.Type;
	import org.granite.tide.BaseContext;
	

    /**
     * @author William DRAI
     */
	[ExcludeClass]
    public class TypeScanner {
        
        public static function scanInjections(context:BaseContext, type:Type, callback:Function):void {
            var sourcePropName:String,
            	destPropName:Object,
            	uri:String;
            var create:String, global:String;
            var annotation:Annotation;
            var argValue:String;

			var fields:Array = type.getAnnotatedFieldsNoCache('In');
			fields = fields.concat(type.getAnnotatedFieldsNoCache('Inject'));
			fields = fields.filter(function (f:Field, i:int, a:Array):Boolean {
				if (f.isWriteable() && !f.isStatic()) {
					if (f.name.match(/_[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9].*/)) {
						var field:Field = type.getInstanceFieldNoCache(f.name.substring(10));
						if (field != null && field.isWriteable())
							return false;
					}
					return true;
				}
				return false;
			});

			for each (var field:Field in fields) {
				sourcePropName = field.name;
				destPropName = field.uri ? field.qName : field.name;
				
				annotation = field.getAnnotationNoCache('Inject');
				if (annotation == null)
					annotation = field.getAnnotationNoCache('In');
				argValue = annotation.getArgValue();
				if (argValue != null && argValue.length > 0)
					sourcePropName = argValue;
				
                create = annotation.getArgValue('create', '');
                global = annotation.getArgValue('global', '');
				
				callback(context, field, annotation, sourcePropName, destPropName, create, global);
			}
        }
        
        
        public static function scanOutjections(context:BaseContext, type:Type, callback:Function):void {
            var sourcePropName:Object, destPropName:String;
            var remote:String, global:String;
            var annotation:Annotation;
            var argValue:String;

			var fields:Array = type.getAnnotatedFieldsNoCache('Out');
			fields = fields.filter(function (f:Field, i:int, a:Array):Boolean {
				if (f.isReadable() && !f.isStatic()) {
					if (f.name.match(/_[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9].*/)) {
						var field:Field = type.getInstanceFieldNoCache(f.name.substring(10));
						if (field != null && field.isReadable())
							return false;
					}
					return true;
				}
				return false;
			});
			
			for each (var field:Field in fields) {
				sourcePropName = field.uri ? field.qName : field.name;
				destPropName = field.name;
                	
				annotation = field.getAnnotationNoCache('Out');
				argValue = annotation.getArgValue();
				if (argValue != null && argValue.length > 0)
					destPropName = argValue;
                
				global = annotation.getArgValue('global', '');
                remote = annotation.getArgValue('remote', '');
				
				callback(context, field, annotation, sourcePropName, destPropName, global, remote);
            }
        }
        
        
        public static function scanProducerProperties(context:BaseContext, type:Type, callback:Function):void {
            var annotation:Annotation;

			var fields:Array = type.getAnnotatedFieldsNoCache('Produces');
			fields = fields.filter(function (f:Field, i:int, a:Array):Boolean {
				return !f.isStatic() && f.isReadable() && f.isWriteable();
			});
			
			for each (var field:Field in fields) {
				annotation = field.getAnnotationNoCache('Produces');
				
				callback(context, field, annotation);
            }
        }
        
        public static function scanProducerMethods(context:BaseContext, type:Type, callback:Function):void {
            var annotation:Annotation;

			var methods:Array = type.getAnnotatedMethodsNoCache('Produces');
			methods = methods.filter(function (m:Method, i:int, a:Array):Boolean {
				return !m.isStatic();
			});
			
			for each (var method:Method in methods) {
				annotation = method.getAnnotationNoCache('Produces');
				
				callback(context, method, annotation);
            }
        }
    }
}