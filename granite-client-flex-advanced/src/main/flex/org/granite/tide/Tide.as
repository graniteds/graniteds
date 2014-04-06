/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide { 
	
	import flash.display.DisplayObject;
	import flash.display.DisplayObjectContainer;
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.system.ApplicationDomain;
	import flash.utils.Dictionary;
	import flash.utils.IExternalizable;
	import flash.utils.getDefinitionByName;
	import flash.utils.getQualifiedClassName;
	
	import mx.binding.BindabilityInfo;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.core.Application;
	import mx.core.IUIComponent;
    import mx.events.PropertyChangeEvent;
    import mx.logging.ILogger;
	import mx.logging.Log;
    import mx.utils.DescribeTypeCache;
	import mx.utils.DescribeTypeCacheRecord;
	import mx.utils.ObjectUtil;
	import mx.utils.RPCObjectUtil;
	
	import org.granite.reflect.Annotation;
	import org.granite.reflect.Method;
	import org.granite.reflect.Type;
	import org.granite.tide.events.IEventInterceptor;
	import org.granite.tide.events.TideContextEvent;
	import org.granite.tide.impl.ComponentInfo;
	import org.granite.tide.impl.ComponentStore;
	import org.granite.tide.impl.ContextManager;
	import org.granite.tide.impl.IComponentProducer;
    import org.granite.tide.service.ServerSession;


	[Bindable]
    /**
	 * 	Tide is the base implementation of the Tide application manager singleton
	 *
     * 	@author William DRAI
     */
	public class Tide extends EventDispatcher {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.Tide");
        
        public static const DEFAULT_CONTEXT:String = "__DEFAULT_CONTEXT__";
        
        public static const PLUGIN_ADD_COMPONENT:String = "org.granite.tide.plugin.addComponent";        
        public static const PLUGIN_SET_CREDENTIALS:String = "org.granite.tide.plugin.setCredentials";
        public static const PLUGIN_LOGIN_SUCCESS:String = "org.granite.tide.plugin.loginSuccess";
        public static const PLUGIN_LOGIN_FAULT:String = "org.granite.tide.plugin.loginFault";
        public static const PLUGIN_LOGOUT:String = "org.granite.tide.plugin.logout";
        
        public static const SCOPE_UNKNOWN:int = 0;
        public static const SCOPE_SESSION:int = 1;
        public static const SCOPE_CONVERSATION:int = 2;
		public static const SCOPE_EVENT:int = 3;
        
        public static const RESTRICT_UNKNOWN:int = 0;
        public static const RESTRICT_NO:int = 1;
        public static const RESTRICT_YES:int = 2;
        
        public static const SYNC_NONE:int = 0;
        public static const SYNC_SERVER_TO_CLIENT:int = 1;
        public static const SYNC_BIDIRECTIONAL:int = 2;
		
        public static const CONTEXT_CREATE:String = "org.granite.tide.contextCreate";
        public static const CONTEXT_DESTROY:String = "org.granite.tide.contextDestroy";
        public static const CONTEXT_RESULT:String = "org.granite.tide.contextResult";
        public static const CONTEXT_FAULT:String = "org.granite.tide.contextFault";
        
        public static const STARTUP:String = "org.granite.tide.startup";
        public static const LOGIN:String = "org.granite.tide.login";
        public static const LOGOUT:String = "org.granite.tide.logout";
        public static const LOGGED_OUT:String = "org.granite.tide.loggedOut";
		public static const SESSION_EXPIRED:String = "org.granite.tide.sessionExpired";
	    
        public static const CONVERSATION_TAG:String = "conversationId";
        public static const CONVERSATION_PROPAGATION_TAG:String = "conversationPropagation";
        public static const IS_LONG_RUNNING_CONVERSATION_TAG:String = "isLongRunningConversation";
        public static const WAS_LONG_RUNNING_CONVERSATION_ENDED_TAG:String = "wasLongRunningConversationEnded";
        public static const WAS_LONG_RUNNING_CONVERSATION_CREATED_TAG:String = "wasLongRunningConversationCreated";
        public static const IS_FIRST_CALL_TAG:String = "org.granite.tide.isFirstCall";
        public static const IS_FIRST_CONVERSATION_CALL_TAG:String = "org.granite.tide.isFirstConversationCall";
        
		public static const TYPE_SUBCONTEXT:Type = Type.forClass(Subcontext);
		public static const QCN_BASECONTEXT:String = getQualifiedClassName(BaseContext);
		public static const QCN_SUBCONTEXT:String = getQualifiedClassName(Subcontext);
		public static const QCN_ICOMPONENT:String = getQualifiedClassName(IComponent);
		public static const QCN_IENTITY:String = getQualifiedClassName(IEntity);
		public static const QCN_ILIST:String = getQualifiedClassName(IList);
		public static const QCN_IUICOMPONENT:String = getQualifiedClassName(IUIComponent);
		
		public static const WINDOW_CREATE:String = "globalNotifyWindowCreate";
		public static const WINDOW_CLOSE:String = "globalNotifyWindowClose";
		
        
	    private static var _tide:Tide;
	    
		private var _componentStore:ComponentStore = null;
		private var _managedInstances:Dictionary = new Dictionary(true);
		private var _contextManager:IContextManager;
		private var _entityDescriptors:Dictionary = new Dictionary(true);
        private var _componentClass:Class;

        private var _mainServerSession:ServerSession;
	    
        private var _registeredListeners:ArrayCollection = new ArrayCollection();
        private var _newListeners:ArrayCollection = new ArrayCollection();
        
        private var _currentModulePrefix:String = "";
        
        public static var showBusyCursor:Boolean = true;
        private var _busy:int = 0;
        public var disconnected:Boolean = false;


		public function Tide():void {
		    log.info("Initializing Tide proxy");

            _mainServerSession = initServerSession();

            DescribeTypeCache.registerCacheHandler("bindabilityInfo", bindabilityInfoHandler);
			DescribeTypeCache.registerCacheHandler("componentInfo", componentInfoHandler);
			
			try {
                // Register lazy-aware toString method for RPC logging
				RPCObjectUtil['externalToString'](tideToString);
			}
			catch (e:Error) {
				// Ignore: only supported in Apache Flex 4.10+
				log.debug("Could not override RPCObjectUtil.toString(). Consider upgrading to Apache Flex 4.10+.");
			}
            
		    init(Context, null);
			
		    log.info("Tide proxy initialized");
		}
		
		/**
		 *	@private
		 * 	Init context and component default types
		 */  
		protected function init(contextClass:Class, componentClass:Class):void {
			_componentClass = componentClass;
			_contextManager = new ContextManager(this, contextClass);
		    _componentStore = new ComponentStore(this, _contextManager);
		    
	        addComponent("meta_dirty", Boolean);
		}

        private var _toStringReentrant:Object = null;
		
		private function tideToString(value:Object, namespaceURIs:Array = null, exclude:Array = null):String {
            var saveToStringReentrant:Object = _toStringReentrant;
            try {
                if (_toStringReentrant === value) {
                    // toString() of this object uses RPCObjectUtil, don't use it to avoid infinite recursion...
                    BaseContext.avoidToString[getQualifiedClassName(value)] = true;
                }

                _toStringReentrant = value;
                return BaseContext.toString(value);
            }
            finally {
                _toStringReentrant = saveToStringReentrant;
            }
            return null;
		}


		/**
		 *	Factory for current instance Tide singleton
		 * 
     	 *	@param destination default destination (not used here)
		 *  @return current singleton instance
		 */
		public static function getInstance(destination:String = null, tideClass:Class = null):Tide {
		    if (!_tide) {
		    	if (tideClass == null)
		    		_tide = new Tide();
		    	else
		        	_tide = new tideClass();
		    }
            if (destination != null)
                _tide.mainServerSession.destination = destination;
		    return _tide;
		}
		
		/**
		 *	Clear Tide singleton (should be used only for testing)
		 */
		public static function resetInstance():void {
			if (_tide)
				_tide.resetApplication();
			
		    _tide = null;
		}
				
		/**
		 *	Return a context from its id
		 *  
		 * 	@param contextId context id
		 *  @param create should create when not existing
		 *  @return context
		 */ 
		public function getContext(contextId:String = null, parentContextId:String = null, create:Boolean = true):BaseContext {
			return _contextManager.getContext(contextId, parentContextId, create);
		}

        /**
         *  Return the main server session
         *
         *  @return server session
         */
        public function get mainServerSession():ServerSession {
            return _mainServerSession;
        }


        protected function initServerSession():ServerSession {
            return new ServerSession();
        }


        public function get busy():Boolean {
            return _busy > 0;
        }
        public function notifyBusy(busy:Boolean):void {
            var oldBusy:Boolean = this.busy;
            if (busy)
                _busy++;
            else
                _busy--;
            if (this.busy != oldBusy)
                dispatchEvent(PropertyChangeEvent.createUpdateEvent(this, "busy", oldBusy, this.busy));
        }


		/**
		 *  @private
		 *  Name of the current module in which observers and injections are handled
		 * 
		 *  @return module prefix
		 */
		public function get currentModulePrefix():String {
			return _currentModulePrefix;
		}
		public function set currentModulePrefix(prefix:String):void {
			_currentModulePrefix = prefix;
		}
		
		
		public function getManagedInstance(component:Object):Array {
			return _managedInstances[component] as Array;
		}
		public function setManagedInstance(component:Object, context:BaseContext, name:String):void {
			if (component is IUIComponent)
				_managedInstances[component] = [ context, name ];
		}
		public function removeManagedInstance(component:Object):void {
			delete _managedInstances[component];
		}
		
		
		/**
		 *  @private
		 *  Name of the current namespace in which observers and injections are handled
		 * 
		 *  @return namespace
		 */
		public function get currentNamespace():String {
			return _currentModulePrefix ? _currentModulePrefix.substring(0, _currentModulePrefix.length-1) : _currentModulePrefix;
		}
		
		
		/**
		 * 	Register a plugin instance
		 * 
		 *  @param plugin the plugin instance
		 */
		public function addPlugin(plugin:ITidePlugin):void {
		    plugin.tide = this;
		}
		
		
		/**
		 * 	Internal method to determine current application in both Flex 3 and Flex 4
		 * 
		 *  @return top level application
		 */
		public static function currentApplication():Object {
			var app:Object = null;
			// Application.application seems to break Flex 4.5 mobile applications
			CONFIG::flex40 {
				app = Application.application;
			}
			if (app == null) {
				// Flex 4 spark application
				var flexGlobals:Class = getDefinitionByName("mx.core.FlexGlobals") as Class;
				app = flexGlobals.topLevelApplication;
			}
			return app;
		}
		
		
		/**
		 * 	Init the Tide application
		 * 
		 * 	@param autoRegisterUIComponents enable automatic registration/unregistration of annotated UI components 
		 */
        public function initApplication(autoRegisterUIComponents:Boolean = true):void {
        	var ctx:BaseContext = getContext();
        	var app:Object = currentApplication();
			ctx.application = app;
			if (autoRegisterUIComponents) {
				app.addEventListener(Event.ADDED, addedHandler, 0, false, true);
	            app.systemManager.addEventListener(Event.ADDED, addedHandler, 0, false, true);
				app.addEventListener(Event.REMOVED, removedHandler, 0, false, true);
	            app.systemManager.addEventListener(Event.REMOVED, removedHandler, 0, false, true);
				app.addEventListener(WINDOW_CREATE, nativeAddedHandler, 0, false, true); 
				app.systemManager.addEventListener(WINDOW_CREATE, nativeAddedHandler, 0, false, true); 
				app.addEventListener(WINDOW_CLOSE, nativeRemovedHandler, 0, false, true); 
				app.systemManager.addEventListener(WINDOW_CLOSE, nativeRemovedHandler, 0, false, true); 	        
			}
	        
            ctx.raiseEvent(STARTUP);
		}
		
		/**
		 * 	Resets Tide application (mainly used in test cases)
		 */
		protected function resetApplication():void {
			_contextManager.destroyContexts(true);
            // Cleanup app domains so each unit test is not polluted with other registrations
            Type.resetRegisteredDomains();

			var app:Object = currentApplication();
			app.removeEventListener(Event.ADDED, addedHandler);
	        app.systemManager.removeEventListener(Event.ADDED, addedHandler);
			app.removeEventListener(Event.REMOVED, removedHandler);
	        app.systemManager.removeEventListener(Event.REMOVED, removedHandler);
			app.removeEventListener(WINDOW_CREATE, nativeAddedHandler); 
			app.systemManager.removeEventListener(WINDOW_CREATE, nativeAddedHandler); 
			app.removeEventListener(WINDOW_CLOSE, nativeRemovedHandler); 
			app.systemManager.removeEventListener(WINDOW_CLOSE, nativeRemovedHandler); 	        
		}
		
		
		/**
		 * 	@private
		 *  
		 * 	Get implementation for type
		 * 
		 *  @param type class required at injection point
		 *  @return name of implementation component
		 */
		public function getProducer(type:Class):IComponentProducer {
			return _componentStore.getProducer(type);
		}
		
		/**
		 * 	@private
		 *  
		 * 	Get all implementations for type
		 * 
		 *  @param type class required at injection point
		 *  @return array of names of implementation components 
		 */
		public function getAllProducers(type:Class):Array {
			return _componentStore.getAllProducers(type);
		}
		
				
		/**
		 * 	@private
		 *  
		 * 	Get injection points for type
		 * 
		 *  @param type type required at injection point
		 *  @return array of injection points [ componentName, propertyName ]
		 */
		public function getInjectionPoints(type:Type):Array {
			return _componentStore.getInjectionPoints(type);
		}
		
		
		private static var FLEX4:Boolean = true;
		private static var IVISUALELEMENTCONTAINER_CLASS:Class = null;
		private static var IDEFERREDCONTENTOWNER_CLASS:Class = null;
		
		/**
		 * @private
		 * Detects if we are on Flex 4+ 
		 */
		public static function detectFlex4():Boolean {
			if (IVISUALELEMENTCONTAINER_CLASS == null && FLEX4) {
				try {
					IVISUALELEMENTCONTAINER_CLASS = getDefinitionByName("mx.core.IVisualElementContainer") as Class;
					IDEFERREDCONTENTOWNER_CLASS = getDefinitionByName("mx.core.IDeferredContentOwner") as Class;
				}
				catch (e:ReferenceError) {
					FLEX4 = false;
				}
			}
			return FLEX4;
		}
		
		
		/**
		 *  @private
		 *  Builds the internal Tide component name for a UIComponent 
		 */
		public static function internalUIComponentName(component:Object):String {
			return component.parentDocument != null 
				? component.parentDocument.name + "_" + component.name 
				: component.name;
		}
		
		/**
		 * 	@private
		 * 	Find the context where the uiComponent is managed
		 * 
		 * 	@param uiComponent an UI component
		 * 	@param returnDefault also search in default context
		 *
		 * 	@return the context managing the uiComponent
		 */
		public function findContext(uiComponent:IUIComponent, returnDefault:Boolean = true):BaseContext {
			var ctxName:Array = getManagedInstance(uiComponent) as Array;
			if (ctxName != null)
				return ctxName[0] as BaseContext;
			
			if (uiComponent.parent != null && uiComponent.parent is IUIComponent)
				return findContext(uiComponent.parent as IUIComponent);
			
			return returnDefault ? getContext() : null;
		}
		
		
		/**
		 * 	@private
		 * 	Internal handler for ADDED events that scans UI components for [Name] annotations
		 * 
		 * 	@param event the ADDED event
		 */
		private function addedHandler(event:Event):void {
			internalAdd(event.target);
		}
		
		private function internalAdd(component:Object, parent:Object = null):void {
			var className:String = getQualifiedClassName(component);
			if (!_componentStore.isFxClass(className)) {
				var info:ComponentInfo = DescribeTypeCache.describeType(component)['componentInfo'] as ComponentInfo;
				if (info.name != null) {
					var name:String = null;
					var autoName:Boolean = false;
					if (info.name.length > 0)
						name = info.name;
					else if (info.module.length > 0) {
						name = info.module + "." + internalUIComponentName(component);
						autoName = true;
					}
					else {
						name = internalUIComponentName(component);
						autoName = true;
					}
	            	
	            	var saveModulePrefix:String = _currentModulePrefix;
	            	_currentModulePrefix = "";
					
		        	var ctx:BaseContext = findContext(parent != null ? parent as IUIComponent : component as IUIComponent);
		            if (!info.scope 
						|| (info.scope != "conversation" && ctx.meta_isGlobal())
		            	|| (info.scope == "conversation" && !ctx.meta_isGlobal())) {
						
						// If already present in the context with another name, remove it
						var existingCtxName:Array = getManagedInstance(component);
						var existingName:String = existingCtxName != null ? existingCtxName[1] as String : null; 
						
						var add:Boolean = true;
						if (existingName != null) {
							if (getDescriptor(existingName, false).autoUIName) {
								log.debug("component instance renamed from {0} to {1}", existingName, name);
								removeComponent(existingName);
							}
							else
								add = false;
						}
		            	
						if (add) {
			            	var instance:Object = ctx.meta_getInstance(name, false, true);
			            	if (instance !== component) {
				        		ctx[name] = component;
								if (autoName)
									getDescriptor(name).autoUIName = true;
				        		log.info("added component {0}", name);
				        	}
						}
			       	}
		        	
		        	_currentModulePrefix = saveModulePrefix;
				}
			}
			
			detectFlex4();
			var i:uint;
			if (FLEX4 && component is IVISUALELEMENTCONTAINER_CLASS) {
				try {
					if (!(component is IDEFERREDCONTENTOWNER_CLASS) || component.deferredContentCreated) {
						for (i = 0; i < component.numElements; i++)
							internalAdd(component.getElementAt(i), parent != null ? parent : component);
					}
				}
				catch (e:SecurityError) {
					// Stop here: component does not allow access to its children
				}
			}
			else if (component is DisplayObjectContainer) {
				try {
					for (i = 0; i < component.numChildren; i++)
						internalAdd(component.getChildAt(i), parent != null ? parent : component);
				}
				catch (e:SecurityError) {
					// Stop here: component does not allow access to its children
				}
			}			
		}
		
		/**
		 * 	@private
		 * 	Internal handler for REMOVED events that unregister UI components from the context
		 * 
		 * 	@param event the REMOVED event
		 */
		private function removedHandler(event:Event):void {
			internalRemove(event.target);
		}
		
		private function internalRemove(component:Object):void {
			detectFlex4();
			var i:uint;
			if (FLEX4 && component is IVISUALELEMENTCONTAINER_CLASS) {
				try {
					if (!(component is IDEFERREDCONTENTOWNER_CLASS) || component.deferredContentCreated) {
						for (i = 0; i < component.numElements; i++)
							internalRemove(component.getElementAt(i));
					}
				}
				catch (e:SecurityError) {
					// Stop here: component does not allow access to its children
				}
			}
			else if (component is DisplayObjectContainer) {
				try {
					for (i = 0; i < component.numChildren; i++)
						internalRemove(component.getChildAt(i));
				}
				catch (e:SecurityError) {
					// Stop here: component does not allow access to its children
				}
			}
			
			var className:String = getQualifiedClassName(component);
			if (_componentStore.isFxClass(className)) 
				return;	// Ignore framework components
			
			var info:ComponentInfo = DescribeTypeCache.describeType(component)['componentInfo'] as ComponentInfo;
			if (info.name != null) {
				var name:String = null;
				if (info.name.length > 0)
					name = info.name;
				else if (info.module.length > 0)
					name = info.module + "." + internalUIComponentName(component);
				else
					name = internalUIComponentName(component);
				
            	var saveModulePrefix:String = _currentModulePrefix;
            	_currentModulePrefix = "";
				
				if (getDescriptor(name).autoUIName) {
					removeComponent(name);
			        log.info("removed component {0}", name);
				}
				else {
		        	var ctx:BaseContext = findContext(component as IUIComponent, false);
		        	if (ctx != null) {
		            	var instance:Object = ctx.meta_getInstance(name, false, true);
		            	if (instance !== null) {
		        			ctx[name] = null;
			        		log.info("removed component instance {0}", name);
			        	}
			       	}
			   	}
			   	
			   	_currentModulePrefix = saveModulePrefix;
		    }
		}

		
		private function nativeAddedHandler(event:Event):void {
			var window:Object = Object(event).window;
			
			internalAdd(window); 
			window.addEventListener(Event.ADDED, addedHandler, false, 0, true); 
			window.systemManager.addEventListener(Event.ADDED, addedHandler, false, 0, true); 
			window.addEventListener(Event.REMOVED, removedHandler, false, 0, true); 
			window.systemManager.addEventListener(Event.REMOVED, removedHandler, false, 0, true); 
			window.addEventListener(WINDOW_CREATE, nativeAddedHandler, false, 0, true); 
			window.systemManager.addEventListener(WINDOW_CREATE, nativeAddedHandler, false, 0, true); 
			window.addEventListener(WINDOW_CLOSE, nativeRemovedHandler, false, 0, true); 
			window.systemManager.addEventListener(WINDOW_CLOSE, nativeRemovedHandler, false, 0, true); 
		} 

		private function nativeRemovedHandler(event:Event):void { 
			var window:Object = Object(event).window;
			
			window.removeEventListener(Event.ADDED, addedHandler); 
			window.systemManager.removeEventListener(Event.ADDED, addedHandler); 
			window.removeEventListener(Event.REMOVED, removedHandler); 
			window.systemManager.removeEventListener(Event.REMOVED, removedHandler); 
			window.removeEventListener(WINDOW_CREATE, nativeAddedHandler); 
			window.systemManager.removeEventListener(WINDOW_CREATE, nativeAddedHandler); 
			window.removeEventListener(WINDOW_CLOSE, nativeRemovedHandler); 
			window.systemManager.removeEventListener(WINDOW_CLOSE, nativeRemovedHandler); 
			internalRemove(window);
		}
		
		
		/**
		 * 	Register a Tide module
		 * 
		 * 	@param module the module class or instance
		 * 	@param appDomain the Flex application domain for modules loaded dynamically
		 */
		public function addModule(module:Object, appDomain:ApplicationDomain = null):void {
			_componentStore.addModule(module, appDomain);
		}
		
		/**
		 * 	Unregister a Tide module
		 * 
		 * 	@param module the module class or instance
		 */
		public function removeModule(module:Object):void {
			_componentStore.removeModule(module);
        }
		
		
		/**
		 * 	Register a Tide component with static dependency injections
		 *  Dynamic dependency injections are also scanned
		 * 
		 * 	@param name component name
		 * 	@param type component class
		 *  @param properties a factory object containing values to inject in the component instance
		 *  @param inConversation true if the component is conversation-scoped
		 *  @param autoCreate true if the component needs to be automatically instantiated
		 *  @param restrict true if the component needs to be cleared when the user is not logged in
		 *  @param overrideIfPresent allows to override an existing component definition (should normally only be used internally)
		 */
		public function addComponentWithFactory(name:String, type:Class, properties:Object, inConversation:Boolean = false, autoCreate:Boolean = true, restrict:int = RESTRICT_UNKNOWN, overrideIfPresent:Boolean = true):void {
		    _componentStore.internalAddComponent(name, Type.forClass(type), properties, inConversation, autoCreate, restrict, overrideIfPresent);
		}
		
		/**
		 * 	Register a Tide component with a specified definition and scan dynamic dependency injections
		 * 
		 * 	@param name component name
		 * 	@param type component class
		 *  @param inConversation true if the component is conversation-scoped
		 *  @param autoCreate true if the component needs to be automatically instantiated
		 *  @param restrict true if the component needs to be cleared when the user is not logged in
		 *  @param overrideIfPresent allows to override an existing component definition (should normally only be used internally)
		 */
		public function addComponent(name:String, type:Class, inConversation:Boolean = false, autoCreate:Boolean = true, restrict:int = RESTRICT_UNKNOWN, overrideIfPresent:Boolean = true):void {
		    _componentStore.internalAddComponent(name, Type.forClass(type), null, inConversation, autoCreate, restrict, overrideIfPresent);
		}

        /**
         * 	Register a Tide component from an existing instance
         *
         *  @param instance component instance
         * 	@param name component name
         * 	@param type component class
         *  @param inConversation true if the component is conversation-scoped
         */
        public function addComponentFromInstance(instance:Object, name:String, inConversation:Boolean = false):void {
            if (!isComponent(name)) {
                _componentStore.trackModuleCreation(instance);

                var componentClass:Class = Type.forInstance(instance).getClass();
                _tide.addComponent(name, componentClass, inConversation, false);
            }
        }
		
		/**
		 * 	Register many Tide components at once
		 *  The component definitions are scanned from annotations
		 * 
		 * 	@param types array of component classes
		 */
		public function addComponents(types:Array):void {
		    for each (var type:Class in types) {
		    	var t:Type = Type.forClass(type);
				var nameAnno:Annotation = t.getAnnotationNoCache("Name");
				var componentName:String = nameAnno != null ? nameAnno.getArgValue() : null;
		    	if (!componentName)
		    		componentName = Tide.internalNameForTypedComponent(t.name + '_' + t.id);

				var module:String = nameAnno ? nameAnno.getArgValue("module") : null;
				var scope:String = nameAnno ? nameAnno.getArgValue("scope") : null;
				var create:String = nameAnno ? nameAnno.getArgValue("create") : null;
				var restrict:String = nameAnno ? nameAnno.getArgValue("restrict") : null;
		    	var name:String = module ? module + "." + componentName : componentName;
	    		
	            _componentStore.internalAddComponent(name, t, null, 
	            	scope == "conversation", 
					create != "false", 
					restrict == "true" ? RESTRICT_YES : (restrict == "false" ? RESTRICT_NO : RESTRICT_UNKNOWN)
	            );
		    }
		}
		
		
		/**
		 * 	Unregister a Tide component and destroys all instances
		 * 
		 * 	@param name component name
		 */
		public function removeComponent(name:String):void {
			_componentStore.removeComponent(name);
		}
		
		/**
		 *  Remove descriptor for proxy when value is no more a proxy
		 *  
		 * 	@param name component name
		 *  @param value new value
		 */
		public function removeProxyDescriptor(name:*, value:*):void {
			if (isComponentDefaultProxy(name) && !isProxy(value))	// GDS-480 remove existing descriptor when a proxy becomes a 'real' instance 
				_componentStore.removeComponentDescriptor(name);
		}
		
		/**
		 *  Is the component a proxy
		 *  
		 * 	@param instance component instance
		 *  @return true if component is a remote proxy
		 */
		private function isProxy(instance:Object):Boolean {
			if (_componentClass == null)
				return false;
			return instance is _componentClass;
		}

		
		/**
		 * 	Checks a name is a registered Tide component
		 * 
		 * 	@param name component name
		 *  
		 *  @return true if there is a component with this name
		 */
		public function isComponent(name:String):Boolean {
		    return _componentStore.isComponent(name);
		}
		
		
		/**
		 * 	@private
		 * 	Internal implementation of component instantiation
		 * 
		 *  @param name component name
		 *  @param context context
		 *  @param noProxy don't create remote proxy by default
		 * 
		 *  @return component instance
		 */
		public function newComponentInstance(name:String, context:BaseContext, noProxy:Boolean = false):Object {
            var component:Object = null;
			
		    var componentName:String = name;
		    var descriptor:ComponentDescriptor = getDescriptor(name, false);
		    if ((descriptor == null || descriptor.factory == null) && name.lastIndexOf(".") > 0) {
		    	componentName = name.substring(name.lastIndexOf(".")+1);
				if (!isComponentGlobal(componentName))
		    		descriptor = getDescriptor(componentName, false);
		    }

			var scope:int = context.meta_isGlobal() ? Tide.SCOPE_SESSION : Tide.SCOPE_CONVERSATION;

		    if (descriptor == null || descriptor.factory == null) {
                if (_componentClass == null || noProxy)
                    return null;
                
		    	// No descriptor or factory : create remote proxy
        		component = new _componentClass();
        		component.meta_init(componentName, context);
                
				scope = Tide.SCOPE_EVENT;
                if (descriptor == null) {
                	descriptor = getDescriptor(componentName, true);
                	descriptor.proxy = true;
					descriptor.global = true;
                }
				else if (descriptor.scope != Tide.SCOPE_UNKNOWN)
					scope = descriptor.scope; 
                
                descriptor.restrict = RESTRICT_YES;
                descriptor.remoteSync = SYNC_BIDIRECTIONAL;
		   	}
		   	else {
				if (descriptor.scope != Tide.SCOPE_UNKNOWN)
					scope = descriptor.scope;
				
                if (context.meta_isGlobal()) {
                    if (isComponentInConversation(componentName))
                        return null;
                }
                else {
                    if (isComponentInSession(componentName))
                        return null;
                }
                
	            component = descriptor.factory.newInstance(name, context);
	        }
            
            setComponentScope(componentName, scope);

            return component;
        }

		
		/**
		 * 	@private
		 * 	Returns a component descriptor
		 * 
		 *  @param name component name
		 *  @param create descriptor should be created if it does not exist 
		 * 
		 *  @return component descriptor
		 */
		public function getDescriptor(name:String, create:Boolean = true):ComponentDescriptor {
			return _componentStore.getDescriptor(name, create);
		}
		
		
		/**
		 * 	Checks a name is a registered Tide subcontext
		 * 
		 * 	@param name component name
		 *  
		 *  @return true if there is a subcontext with this name
		 */
		public function isSubcontext(name:String):Boolean {
			var desc:ComponentDescriptor = _componentStore.getDescriptor(name, false);
			return desc != null && desc.factory != null ? desc.factory.type == TYPE_SUBCONTEXT : false;
		}
		
				
		/**
		 * 	@private
		 * 	Extracts the module prefix from a component name
		 * 
		 *  @param componentName qualified component name
		 * 
		 *  @return module prefix
		 */
		public static function getModulePrefix(componentName:String):String {
			var idx:int = componentName.lastIndexOf(".");
			return idx > 0 ? componentName.substring(0, idx+1) : "";
		}		        
		
		/**
		 * 	@private
		 * 	Extracts the module prefix from a component name
		 * 
		 *  @param componentName qualified component name
		 * 
		 *  @return namespace
		 */
		public static function getNamespace(componentName:String):String {
			var idx:int = componentName.lastIndexOf(".");
			return idx > 0 ? componentName.substring(0, idx) : "";
		}		        

		
		/**
		 * 	Return if the specific component is defined as global (not in a namespace) 
		 * 
		 *  @param name component name
		 */
		public function isComponentGlobal(name:String):Boolean {
			var descriptor:ComponentDescriptor = _componentStore.getDescriptor(name, false);
		    return descriptor ? descriptor.global : false;
		}
		
		/**
		 * 	Define if the component is defined as global (cannot be in a namespace/subcontext)
		 * 
		 *  @param name component name
		 *  @param global true if non component cannot be in a namespace/subcontext
		 */
		public function setComponentGlobal(name:String, global:Boolean):void {
		    _componentStore.getDescriptor(name).global = global;
		}
		
		/**
		 * 	Define the scope of a component
		 * 
		 *  @param name component name
		 *  @param scope component scope (see SCOPE_xxx constants) 
		 */
		public function setComponentScope(name:String, scope:int):void {
		    _componentStore.getDescriptor(name).scope = scope;
		}
		
		/**
		 * 	Returns the scope of a component
		 * 
		 *  @param name component name
		 *  @return component scope
		 */
		public function getComponentScope(name:String):int {
			return _componentStore.getDescriptor(name).scope;
		}
		
		/**
		 * 	Returns the scope of a component
		 * 
		 *  @param name component name
		 *  @return true is event scoped 
		 */
		public function isComponentInEvent(name:String):Boolean {
			return _componentStore.getDescriptor(name).scope == SCOPE_EVENT;
		}
		
		/**
		 * 	Returns the scope of a component
		 * 
		 *  @param name component name
		 *  @return true is conversation scoped 
		 */
		public function isComponentInConversation(name:String):Boolean {
		    return _componentStore.getDescriptor(name).scope == SCOPE_CONVERSATION;
		}
		
		/**
		 * 	Returns the scope of a component
		 * 
		 *  @param name component name
		 *  @return true if session scoped 
		 */
		public function isComponentInSession(name:String):Boolean {
		    return _componentStore.getDescriptor(name).scope == SCOPE_SESSION;
		}
		
		/**
		 * 	Define the creation policy of the component
		 * 
		 *  @param name component name
		 *  @param autoCreate true if component should be automatically instantiated 
		 */
		public function setComponentAutoCreate(name:String, autoCreate:Boolean):void {
		    _componentStore.getDescriptor(name).autoCreate = autoCreate;
		}
		
		/**
		 * 	Returns the creation policy of a component
		 * 
		 *  @param name component name
		 *  @return true if automatic instantiation 
		 */
		public function isComponentAutoCreate(name:String):Boolean {
		    return _componentStore.getDescriptor(name).autoCreate;
		}
		
		/**
		 * 	Define the remote synchronization of the component
		 *  If false, the component state will never be sent to the server
		 * 
		 *  @param name component name
		 *  @param remoteSync type of synchronization with the remote component
		 */
		public function setComponentRemoteSync(name:String, remoteSync:int):void {
		    _componentStore.getDescriptor(name).remoteSync = remoteSync;
		}
		
		/**
		 * 	Return the remote synchronization type of the component
		 * 
		 *  @param name component name
		 *  @return remote synchronization type
		 */
		public function getComponentRemoteSync(name:String):int {
		    return _componentStore.getDescriptor(name).remoteSync;
		}
		
		/**
		 * 	Define the security restriction of the component
		 *  If RESTRICT_YES, the component state will be cleared when user logs out
		 *  Default value is RESTRICT_UNKNOWN meaning if will be inferred from server definition when possible
		 * 
		 *  @param name component name
		 *  @param restrict RESTRICT_YES if the component is restricted, RESTRICT_NO if not
		 */
		public function setComponentRestrict(name:String, restrict:int):void {
		    _componentStore.getDescriptor(name).restrict = restrict;
		}
		
		/**
		 * 	Return the proxy status of the component
		 * 
		 *  @param name component name
		 *  @return true if the component is a default proxy
		 */
		public function isComponentDefaultProxy(name:String):Boolean {
		    var descriptor:ComponentDescriptor = _componentStore.getDescriptor(name, false);
		    return descriptor ? descriptor.proxy : false;
		}
		
		/**
		 * 	Return the security restriction of the component
		 * 
		 *  @param name component name
		 *  @return true if the component is restricted
		 */
		public function isComponentRestrict(name:String):Boolean {
		    var descriptor:ComponentDescriptor = _componentStore.getDescriptor(name, false);
		    return descriptor ? descriptor.restrict == RESTRICT_YES : false;
		}
		
		/**
		 * 	Return the security restriction of the component
		 * 
		 *  @param name component name
		 *  @return security restriction
		 */
		public function getComponentRestrict(name:String):int {
		    var descriptor:ComponentDescriptor = _componentStore.getDescriptor(name, false);
		    return descriptor ? descriptor.restrict : RESTRICT_UNKNOWN;
		}
		
				
		/**
		 * 	Define the custom XML descriptor for the component 
		 * 
		 *  @param name component name
		 *  @param xmlDescriptor XML descriptor
		 */
		public function setComponentCustomDescriptor(name:String, xmlDescriptor:XML):void {
		    var descriptor:ComponentDescriptor = _componentStore.getDescriptor(name, true);
		    descriptor.xmlDescriptor = xmlDescriptor;
		    _componentStore.setupDescriptor(name, descriptor); 
		}
		
		
		public static const TYPED_IMPL_PREFIX:String = "__typedImpl__";
		
		public static function internalNameForTypedComponent(name:String):String {
			return TYPED_IMPL_PREFIX + name.replace(/\./g, "_");
		}
        
        
		/**
		 *	@private
		 * 	Invoke the observers registered for an event
		 * 
		 * 	@param context source context of the event
		 *  @param modulePrefix source module prefix of the event
		 *  @param type event type
		 *  @param params params array
		 */
		public function invokeObservers(context:BaseContext, modulePrefix:String, type:String, params:Array):void {
			var interceptor:IEventInterceptor = null;
		    var localEvent:TideContextEvent = null;
		    
			var interceptors:Array = context.allByType(IEventInterceptor);
			if (interceptors != null) {
				for each (interceptor in interceptors) {
	        		if (localEvent == null)
	        			localEvent = new TideContextEvent(type, context, params);
					interceptor.beforeDispatch(localEvent);
					if (localEvent.isDefaultPrevented()) {
						log.debug("Event {0} prevented by interceptor {1}", localEvent.toString(), getQualifiedClassName(interceptor));
						return;
					}
				}
			}
					    
		    if (hasEventListener(type)) {
        		if (localEvent == null)
        			localEvent = new TideContextEvent(type, context, params);
        		super.dispatchEvent(localEvent);
		    }
		    if (context.hasEventListener(type)) {
        		if (localEvent == null)
        			localEvent = new TideContextEvent(type, context, params);
        		context.dispatchEvent(localEvent);
		    }
		    
		    var observerNames:ArrayCollection = _componentStore.getObserversByType(type);
		    if (observerNames == null) {
		    	if (type.indexOf("org.granite.tide") != 0)
		    		log.debug("no observer found for type: " + type);
		    }
			else {
			    var names:Array = observerNames.toArray();
			    
				var saveModulePrefix0:String = _currentModulePrefix;
				
			    var currentParams:Array = null;
			    for each (var o:Object in names) {
			    	if (observerNames.getItemIndex(o) < 0)
			    		continue;
			    	
			    	var localOnly:Boolean = o.localOnly;
			    	var currentEvent:Event;
			    	var observerModulePrefix:String = getModulePrefix(o.name);
			    	if (modulePrefix != "" && (observerModulePrefix == "" || 
			    		((!localOnly && modulePrefix.indexOf(observerModulePrefix) != 0) || (localOnly && modulePrefix != observerModulePrefix)))) {
			    		// Ignore events from other modules
			    		continue;
			    	}
			    	
					var saveModulePrefix:String = _currentModulePrefix;
					_currentModulePrefix = observerModulePrefix;
			    	
			        var component:Object = context.meta_getInstance(o.name, o.create, !o.create);
			        if (component != null) {
			        	var local:Boolean = true;
			        	var targetContext:Object = context.meta_getContext(o.name, component);
		            	if (!context.meta_isGlobal() && targetContext !== context) {
		            		if (localOnly) {
		            			// Ignore events from conversation conversation context if we don't observe bubbled events
		            			continue;
		            		}
		            		
		            		// If this is an observer for a event coming from a conversation context,
		            		// we have to convert the event parameters that are entities to the global context  
	                    	var targetParams:Array = new Array();
		            		for (var i:int = 0; i < params.length; i++) {
		            			if (params[i] is IEntity)
		            				targetParams.push(targetContext.meta_getCachedObject(params[i]));
		            			else if (params[i] is Event) {
									var clonedEvent:Object = null;
									if (params[i] is IExternalizable)
										clonedEvent = ObjectUtil.copy(params[i]);
									else
			            				clonedEvent = params[i].clone();
									
						            var cinfo:Object = ObjectUtil.getClassInfo(clonedEvent, null, { includeReadOnly: false, includeTransient: false });
						            for each (var p:String in cinfo.properties) {
						                var val:Object = clonedEvent[p];
						                if (val is IEntity)
						                	clonedEvent[p] = targetContext.meta_getCachedObject(val);
						            }
									
									targetParams.push(clonedEvent);
		            			}
		            			else if (params[i] is DisplayObject || params[i] is Class || params[i] is Function)
		            				targetParams.push(params[i]);
		            			else
		            				targetParams.push(ObjectUtil.copy(params[i]));
		            		}
			            	local = false;
			            	currentParams = targetParams;
		            	}
		            	else
				    		currentParams = params;
						
			            if (o.event) {
			            	if (local) {
			            		if (localEvent == null)
			            			localEvent = new TideContextEvent(type, context, currentParams);
			            		currentEvent = localEvent;
			                }
			                else
			            		currentEvent = new TideContextEvent(type, context, currentParams);
			                
			                component[o.methodName].call(component, currentEvent);
			            }
						else if (o.type != null && currentParams.length == 1) {
							if (currentParams[0].type == o.type)
								component[o.methodName].apply(component, currentParams);
						}
			            else // slice required by Franck Wolff
			                component[o.methodName].apply(component, currentParams.slice(0, o.argumentsCount));
			        }
			        
		            _currentModulePrefix = saveModulePrefix;
			    }
			    
			    _currentModulePrefix = saveModulePrefix0;
			}
	    	
			if (interceptors != null) {
				for each (interceptor in interceptors)
					interceptor.afterDispatch(localEvent);
			}
		}
		
    	
    	/**
    	 *	@private
    	 * 	Return the entity descriptor 
    	 * 
    	 *  @param entity an entity
    	 *  @return the entity descriptor for the entity class
    	 */ 
    	public function getEntityDescriptor(entity:Object):EntityDescriptor {
    	    var className:String = getQualifiedClassName(entity);
    	    var desc:EntityDescriptor = _entityDescriptors[className] as EntityDescriptor;
    	    if (desc == null) {
    	        desc = new EntityDescriptor(entity);
    	        _entityDescriptors[className] = desc;
    	    }
    	    return desc;
    	}

		
		/**
		 * 	Add an exception handler class
		 *  The class should implement IExceptionHandler
		 * 
		 * 	@param handlerClass handler class
		 */
		public function addExceptionHandler(handlerClass:Class):void {
			_componentStore.internalAddComponent("_exceptionHandler_" + getQualifiedClassName(handlerClass).replace(/\./g, '_'), Type.forClass(handlerClass), null, false, true, RESTRICT_NO);
		}
        
        /**
       	 *	Add a context listener for a particular event type
       	 * 
       	 *  @param type event type
       	 *  @param handler event handler
       	 *  @param remote true if the listener observes remote events
       	 */ 
        public function addContextEventListener(type:String, handler:Function = null, remote:Boolean = false):void {
            if (handler != null)
                addEventListener(type, handler, false, 0, true);
            
            if (remote)
                addRemoteEventListener(type, handler);
        }
        
		/**
		 * 	Register an event observer
		 * 
		 * 	@param eventType event type
		 *  @param name name of observer component
		 *  @param methodName name of observer method
		 *  @param remote observer for remote events
		 *  @param create target observer should be instantiated if not existent in context
		 *  @param localOnly target observer listens only from events from its own context, not from events bubbled from inner contexts
		 */
        public function addEventObserver(eventType:String, componentName:String, methodName:String, remote:Boolean = false, create:Boolean = true, localOnly:Boolean = true):void {
        	var descriptor:ComponentDescriptor = _componentStore.getDescriptor(componentName, false);
        	if (descriptor == null)
        		throw new Error("Could not add observer: target component not found with name: " + componentName);
        	
			var type:Type = descriptor.factory.type,
				method:Method = type.getInstanceMethodNoCache(methodName);
			if (method != null)
        		_componentStore.internalAddEventObserver(eventType, componentName, method, remote, create, localOnly);
			else
        		throw new Error("Could not add observer: target method not found: " + componentName + "." + methodName);
        }
        
        
        /**
         *	@private
         *  Register a remote observer
         *  
         *  @param type event type
         *  @param handler event handler
         */
        public function addRemoteEventListener(type:String, handler:Function = null):void {
        	if (type.indexOf("$TideEvent$") == 0)
        		type = type.substring("$TideEvent$".length).replace(/::/g, ".");
            var isNew:Boolean = _registeredListeners.getItemIndex(type) < 0;
            _registeredListeners.addItem(type);
            if (isNew)
                _newListeners.addItem(type);
        }
        
        /**
         *	Unregister a context event listener
         *  
         *  @param type event type
         *  @param handler handler function
         */
        public function removeContextEventListener(type:String, handler:Function):void {
            removeEventListener(type, handler);
        }
        
        
        /**
         *	@private
         * 	Internal implementation of event dispatching that invoke registered observers
         * 
         * 	@param event event
         * 
         *  @return return of standard dispatchEvent
         */
        public override function dispatchEvent(event:Event):Boolean {
            if (event is TideContextEvent) {
                invokeObservers(TideContextEvent(event).context, "", TideContextEvent(event).type, TideContextEvent(event).params);
                return true;
            }
            else
            	return super.dispatchEvent(event);
        }
        
        
        /**
         *	@private
         * 	List of listeners to send to the server for registration
         */
        public function get newListeners():ArrayCollection {
            return _newListeners;
        }

        /**
         *  Force send of all listeners as new
         *  Used during login
         */
        public function forceNewListeners():void {
            for (var i:int = 0; i < _registeredListeners.length; i++)
                _newListeners.addItem(_registeredListeners.getItemAt(i));
        }


		/**
		 * 	@private
		 * 	Hack to force the context to be [Managed]/[Bindable] and modify the Flex reflection cache
		 * 	
		 * 	@param record the cache record
		 */
    	private static function bindabilityInfoHandler(record:DescribeTypeCacheRecord):* {
    	    // Hideous hack to ensure all context variables and components will be correctly bindable
    	    if (record.typeDescription.@base == "org.granite.tide::BaseContext" 
    	        || record.typeDescription.@name == "org.granite.tide::Subcontext"
    	        || record.typeDescription.@name == "org.granite.tide::Component")
    	        record.typeDescription.appendChild(<metadata name="Managed"/>);
    	    
    	    // Another hack to force non public Out properties to be bindable
    	    for each (var out:XML in record.typeDescription.variable) {
    	    	if (out.metadata && out.metadata.(@name == 'Out') && out.@uri) {
	    	    	record.typeDescription.appendChild(<accessor name={out.@name} access="readwrite" type={out.@type}>
					    <metadata name="Bindable">
					      <arg key="event" value="propertyChange"/>
					    </metadata>
					</accessor>);
				}
    	    }
    	    
       		return new BindabilityInfo(record.typeDescription);
    	}
		
		/**
		 * 	@private
		 * 	Reflection cache for component info
		 * 	
		 * 	@param record the cache record
		 */
		private static function componentInfoHandler(record:DescribeTypeCacheRecord):* {
			return new ComponentInfo(record.typeDescription);
		}
	}
}
