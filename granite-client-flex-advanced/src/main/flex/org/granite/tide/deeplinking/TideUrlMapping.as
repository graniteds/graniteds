/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.deeplinking {

	import flash.events.Event;

	import mx.events.BrowserChangeEvent;
	import mx.managers.BrowserManager;
	import mx.managers.IBrowserManager;
	import org.granite.reflect.Type;
	import org.granite.reflect.Annotation;
	import org.granite.reflect.Method;
	import org.granite.tide.ComponentDescriptor;
	import org.granite.tide.ITidePlugin;
	import org.granite.tide.Tide;
	import org.granite.tide.events.TidePluginEvent;

	public class TideUrlMapping implements ITidePlugin {

	    private static var _tideUrlMapping:TideUrlMapping;
        public var _tide:Tide;
        private var _urlMapping:Object = new Object();

		public static function getInstance():TideUrlMapping {
		    if (!_tideUrlMapping)
		        _tideUrlMapping = new TideUrlMapping();
		    return _tideUrlMapping;
		}

        public function set tide(tide:Tide):void {
        	_tide = tide;
        	_tide.getContext().urlMapping = this;
        	_tide.addEventListener(Tide.PLUGIN_ADD_COMPONENT, addComponent);
        }

        private function addComponent(event:TidePluginEvent):void {
        	var descriptor:ComponentDescriptor = event.params.descriptor as ComponentDescriptor;
        	var type:Type = event.params.type as Type;
			var anno:Annotation = type.getAnnotationNoCache('Path');

        	if (anno != null)
        		_urlMapping[anno.getArgValue()] = descriptor.name;
        }

        public function init(url:String, title:String = ""):void {
 			var browserManager:IBrowserManager = BrowserManager.getInstance();     			
 			browserManager.init(url, title);        
 			browserManager.addEventListener(BrowserChangeEvent.BROWSER_URL_CHANGE, parseURL);
 			_tide.getContext().application.callLater(parseURL);            
        }

        public function addMapping(path:String, componentName:String):void {
        	_urlMapping[path] = componentName;
        }

    	private var _isParsing:Boolean = false;

		private function parseURL(event:Event = null):void{
 			var browserManager:IBrowserManager = BrowserManager.getInstance();     			
			_tide.currentModulePrefix = "";
		    _isParsing = true;
		    var parts:Array = browserManager.fragment.split("/");

		    if (parts.length >= 2 && _urlMapping[parts[0]]) {
		    	var component:Object = _tide.getContext()[_urlMapping[parts[0]]];
				var type:Type = Type.forInstance(component);

				for each (var method:Method in type.getAnnotatedMethodsNoCache('Path')) {
					var pathAnnotation:Annotation = method.getAnnotationNoCache('Path');
					var path:Array = pathAnnotation.getArgValue('', '').split('/');

		    		if (path.length != parts.length-1)
		    			continue;

		    		var params:Array = new Array();
		    		var match:Boolean = true;

		    		for (var i:int = 0; i < path.length; i++) {
		    			if (path[i].match(/{.*}/)) {
		    				if (method.returnType.getClass() === Number)
		    					params.push(new Number(parts[i+1]));
		    				else
		    					params.push(parts[i+1]);
		    				break;
		    			}
		    			else if (path[i] != parts[i+1]) {
		    				match = false;
		    				break;
		    			}
		    		}

		    		if (!match)
		    			continue;

	    			component[method.name].apply(component, params);
				}

//		    	var desc:XML = describeType(component);
//		    	var methods:XMLList = desc.method;
//		    	for each (var m:XML in methods) {
//		    		if (m.metadata.length() == 0 || m.metadata.(@name == 'Path').length() == 0)
//		    			continue; 
//		    		var path:Array = m.metadata.(@name == 'Path').arg.(@key == '').@value.toXMLString().split("/");
//		    		if (path.length != parts.length-1)
//		    			continue;
//		    		var params:Array = new Array();
//		    		var match:Boolean = true;
//		    		for (var i:int = 0; i < path.length; i++) {
//		    			if (path[i].match(/{.*}/)) {
//		    				if (m.@type == 'Number')
//		    					params.push(new Number(parts[i+1]));
//		    				else
//		    					params.push(parts[i+1]);
//		    				break;
//		    			}
//		    			else if (path[i] != parts[i+1]) {
//		    				match = false;
//		    				break;
//		    			}
//		    		}
//		    		if (!match)
//		    			continue;
//		    		
//	    			component[m.@name].apply(component, params);
//		    	}
		    }

			_isParsing = false;
		}

		public function updateURL(url:String):void{
			if (!_isParsing)
				_tide.getContext().application.callLater(doUpdateURL, [url]);
		}

		private function doUpdateURL(url:String):void {
			BrowserManager.getInstance().setFragment(url);
		}
	}
}