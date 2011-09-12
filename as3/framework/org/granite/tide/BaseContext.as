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
package org.granite.tide {

    import flash.display.DisplayObject;
    import flash.events.Event;
    import flash.events.IEventDispatcher;
    import flash.system.ApplicationDomain;
    import flash.utils.Dictionary;
    import flash.utils.flash_proxy;
    import flash.utils.getQualifiedClassName;

    import mx.binding.utils.BindingUtils;
    import mx.binding.utils.ChangeWatcher;
    import mx.collections.IList;
    import mx.core.IUIComponent;
    import mx.data.utils.Managed;
    import mx.events.CollectionEvent;
    import mx.events.PropertyChangeEvent;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.AbstractOperation;
    import mx.rpc.AsyncToken;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    import mx.utils.object_proxy;

    import org.granite.collections.IPersistentCollection;
    import org.granite.meta;
    import org.granite.reflect.Annotation;
    import org.granite.reflect.Field;
    import org.granite.reflect.Method;
    import org.granite.reflect.Type;
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.data.EntityManager;
    import org.granite.tide.events.IConversationEvent;
    import org.granite.tide.events.TideUIEvent;
    import org.granite.tide.impl.ContextEventDispatcher;
    import org.granite.tide.impl.ContextVariable;
    import org.granite.tide.impl.IComponentProducer;
    import org.granite.tide.impl.TypeScanner;
    import org.granite.util.ClassUtil;

    use namespace flash_proxy;
    use namespace object_proxy;
    use namespace meta;


    [Bindable]
	/**
	 * 	BaseContext is the base implementation of the component/entity container context
	 *  It is mostly an abstract class that is not exposed directly.
	 *  Instead the user will get one of its subclasses.
	 * 
     * 	@author William DRAI
	 */
    public dynamic class BaseContext extends ObjectProxy implements IEntityManager {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.BaseContext");
    
        protected var _tide:Tide;
        
        private var _contextId:String;
        private var _contextManager:IContextManager;
		private var _parentContext:BaseContext = null;
        private var _isContextIdFromServer:Boolean = false;
        private var _firstCall:Boolean = true; 
        private var _finished:Boolean = false;
        private var _tracking:Boolean = true;
        private var _destroyScheduled:Boolean = false;
        private var _destroying:Boolean = false;
        
        private var _entityManager:EntityManager = null;
        
        private var _componentListeners:Dictionary = new Dictionary(true);        
        private var _inWatchers:Dictionary = new Dictionary();
        private var _outWatchers:Dictionary = new Dictionary();
        


        public function BaseContext(tide:Tide = null, parentContext:BaseContext = null) {
            super();
            _tide = tide;
            _parentContext = parentContext;
        }
        
        /**
		 * 	@private
		 *  Init the context
		 *  @param contextId context id
		 */
        public function meta_init(contextId:String, contextManager:IContextManager):void {
            _contextId = contextId;
            _contextManager = contextManager;
            dispatcher = new ContextEventDispatcher(this);
            _entityManager = new EntityManager(this);
        }
        
        /**
		 * 	@private
		 *  @return the context id
		 */
        public function get contextId():String {
            return _contextId;
        }
        
        /**
		 *  @return true if this is the global context
		 */
        public function meta_isGlobal():Boolean {
            return _parentContext == null;
        }
		
		/**
		 *  @return parent context
		 */
		public function get meta_parentContext():BaseContext {
			return _parentContext;
		}
		
		/**
		 * 	@return context manager
		 */
		public function get meta_contextManager():IContextManager {
			return _contextManager;
		}
        
        /**
		 * 	@private
		 *  @return true when context id has been received from the server
		 */
        public function get meta_isContextIdFromServer():Boolean {
            return _isContextIdFromServer;
        }
        /**
		 * 	@private
		 *  Update the context id
		 *  @param contextId context id
		 *  @param fromServer is this id received from the server ?
		 */
        public function meta_setContextId(contextId:String, fromServer:Boolean = false):void {
        	var previousContextId:String = _contextId;
            _contextId = contextId;
            _isContextIdFromServer = fromServer;
            _contextManager.updateContextId(previousContextId, this);
        }
        
        
        /**
		 *  Returns the RemoteObject destination for this context
		 *  @return destination name
		 */
        public function get meta_destination():String {
            return _tide.destination;
        }
        
        
        /**
		 *  Returns the Tide singleton for this context
		 *  @return Tide singleton
		 */
        public function get meta_tide():Tide {
            return _tide;
        }
        
		
		/**
		 * 	@private
		 * 	Is it the first remote call ?
		 * 
		 * 	@return is first call
		 */
		public function get meta_firstCall():Boolean {
			return _firstCall;
		} 

        
        /**
         * 	@private
         */
        public function meta_scheduleDestroy():void {
        	_destroyScheduled = true;
			_contextManager.addToContextsToDestroy(contextId);
        }


        private static var tmpContextId:uint = 1;

        public function newTemporaryContext():BaseContext {
            var tmpContext:BaseContext = new BaseContext(_tide, this);
            tmpContext.meta_init("$$TMP$$" + (tmpContextId++), _contextManager);
            return tmpContext;
        }
        
        /**
         * 	@private
         *  Clear the current context
         *  Destroys all components/context variables
         * 
         *  @param force force complete destruction of context (all event listeners...), used for testing
         */ 
        public function meta_clear(force:Boolean = false):void {
            _destroying = true;
            _tracking = false;
            var saveModulePrefix:String = _tide.currentModulePrefix;
            _tide.currentModulePrefix = "";
            
            for (var name:String in object)
                meta_destroy(name, force);
            
            if (_entityManager != null)
            	_entityManager.clear();
        	
            _destroying = false;
            _finished = false;
            _contextId = null;    // Must be after components destruction
            _isContextIdFromServer = false;
            _tracking = true;
            _tide.currentModulePrefix = saveModulePrefix;
        }
        
        /**
         *  Ends the current context
         *  Only applicable to conversation contexts 
         *  
         *  @param merge merge conversation context in global context
         */
        public function meta_end(merge:Boolean = true):void {
            if (_parentContext == null)
                throw new Error("Cannot end global context");
            
            if (merge)
                meta_mergeInParentContext();
            
            _contextManager.destroyContext(_contextId);
        }
        
        /**
         *  Continues the current context: restart a new conversation with the same context content when the server has ended the current conversation 
         *  Only applicable to conversation contexts 
         *  
         *  @param merge merge conversation context in global context
         */
        public function meta_continue(merge:Boolean = true):void {
            if (_parentContext == null)
                throw new Error("Cannot continue global context");
            
            if (merge)
                meta_mergeInParentContext();
            
            if (_finished) {
	            _finished = false;
	            _contextManager.removeFromContextsToDestroy(_contextId);
	            _destroyScheduled = false;
	            _isContextIdFromServer = false;
	        }
        }
        
        /**
         *  Merge conversation context variables in global context 
         *  Only applicable to conversation contexts 
         */
        public function meta_mergeInGlobalContext():void {
        	log.debug("merge conversation context {0} in global context", contextId);
        	meta_mergeInParentContext();
        	if (_parentContext != null && !_parentContext.meta_isGlobal())
        		_parentContext.meta_mergeInGlobalContext(); 
        }
		
		/**
		 *  Merge conversation context variables in parent context 
		 *  Only applicable to conversation contexts 
		 */
		public function meta_mergeInParentContext():void {
			log.debug("merge conversation context {0} in parent context {1}", contextId, _parentContext.contextId); 
			_entityManager.mergeInContext(_parentContext);
			_parentContext.meta_clearCache();
		}
        
                
        private var _instancesInitializing:Dictionary = new Dictionary();
        
        /**
         *	@private
         *  Inits a component
         * 
         *  @param name component name
         *  @param instance component instance
         */ 
        private function meta_initInstance(name:String, instance:Object):void {
            if (_instancesInitializing[instance] != null)
                return;
            
            _instancesInitializing[instance] = instance;
            
            var saveTracking:Boolean = _tracking;
            _tracking = false;
            
            var descriptor:ComponentDescriptor = _tide.getDescriptor(name);
            if (instance is DisplayObject && instance.loaderInfo != null) {
            	var appDomain:ApplicationDomain = instance.loaderInfo.applicationDomain;
            	if (appDomain != null && appDomain !== Type.systemDomain)
					Type.registerDomain(appDomain);
            }
            
			var type:Type = Type.forInstance(instance);
			var sourcePropName:String, destPropName:String;
			var inout:XMLList, obj:Object;
			
            if (!ClassUtil.isTopLevel(type.getClass())
            	&& !ClassUtil.isFlashClass(type.getClass()) && !ClassUtil.isMxClass(type.getClass())
            	&& !ClassUtil.isMathClass(type.getClass())) {
	
				TypeScanner.scanOutjections(this, type, 
					function(context:BaseContext, field:Field, annotation:Annotation, sourcePropName:Object, destPropName:String, global:String, remote:String):void {
						// Handle case of [In] [Out]: write existing value before outjection
						annotation = field.getAnnotationNoCache('In');
						if (annotation != null) {
							var inPropName:String = field.name;
							var argValue:String = annotation.getArgValue();
							if (argValue != null && argValue.length > 0)
								inPropName = argValue;
							
							if (inPropName == (destPropName is QName ? QName(destPropName).localName : String(destPropName))) {
		               			var obj:Object = meta_getInstance(inPropName);
		               			if (obj)
									field.setValue(instance, obj);
		               		}
						}
		                
						bindOut(name, instance, sourcePropName, destPropName);
					}
				);
	//			
	//			TypeScanner.scanProducerMethods(this, type, 
	//				function(context:BaseContext, name:String, method:Method, annotation:Annotation):void {
	//					registerProducerMethod();
	//				}
	//			);
	            
				if (descriptor.xmlDescriptor) {
		            for each (var out:XML in descriptor.xmlDescriptor.Out) {
	                    sourcePropName = out.@property.toXMLString();
	                    destPropName = sourcePropName;
	                    if (out.@target.toXMLString() != "")
	                        destPropName = out.@target.toXMLString();
	                    
	                    // Handle case of [In] [Out]
	                    // Overwrite component property with the context variable before binding setup (the context has priority over outjections)
	                    inout = descriptor.xmlDescriptor.In.(@source == destPropName || (@source == '' && @property == destPropName));
	                    if (inout.length() > 0) {
	               			obj = meta_getInstance(destPropName);
	               			if (obj)
	               				instance[destPropName] = obj;
	                    }
	                    
	                    bindOut(name, instance, sourcePropName, destPropName);
		            }
		      	}
	
				TypeScanner.scanInjections(this, type, 
					function(context:BaseContext, field:Field, annotation:Annotation, sourcePropName:String, destPropName:Object, create:String, global:String):void {
						if (field.type.getClass() === BaseContext || field.type.fastExtendsClass(Tide.QCN_BASECONTEXT))
							instance[destPropName] = context;
		                else if (field.type.getClass() === Subcontext) {
		                	var ns:String = Tide.getNamespace(name);
		                	instance[destPropName] = ns ? meta_getSubcontext(ns) : null;
		                }
		                else {
							if (annotation.name == 'Inject')
								injectByType(field.type.getClass(), instance, name, destPropName, create != 'false');
							else
		                    	bindIn(name, instance, destPropName, create != 'false', sourcePropName);
						}
					}
				);      
	
				if (descriptor.xmlDescriptor) {
		            for each (var inj:XML in descriptor.xmlDescriptor.In) {
	                    destPropName = inj.@property.toXMLString();
	                    sourcePropName = destPropName;
	                    if (inj.@source.toXMLString() != "")
	                        sourcePropName = inj.@source.toXMLString();
	                    var create:Boolean = !(inj.@create.toXMLString() == "false");
	                    
	                    bindIn(name, instance, destPropName, create, sourcePropName);
		            }
		      	}
		      	
		      	if (descriptor.postConstructMethodName != null)
		      		instance[descriptor.postConstructMethodName].call();
		    }
            
            _tracking = saveTracking;
            delete _instancesInitializing[instance];
        }
        
        /**
         *  Destroys/reset a component
         * 
         *  @param name component name
         *  @param force true for forcing complete removal of instance
         */ 
        public function meta_destroy(name:String, force:Boolean = false):void {        	
        	if (object[name] is Subcontext) {
        		for (var n:String in object[name])
        			meta_destroy(name + "." + n, force);
        	}

            var remove:Boolean = true;
            
            var obj:Object = meta_getInstance(name, false, true);
            if (obj == null)
                return;
            
            var restrict:Boolean = false;
            if (_tide.isComponent(name)) {
                var descriptor:ComponentDescriptor = _tide.getDescriptor(name);
                if (descriptor.destroyMethodName != null)
                    obj[descriptor.destroyMethodName].call(obj);
                restrict = (descriptor.restrict == Tide.RESTRICT_YES);
                remove = _contextId != null || force || restrict;
            }
            else
                restrict = _tide.isComponentRestrict(name);
            
            if (obj is IComponent) {
                if (restrict)
                    IComponent(obj).meta_clear();
                remove = _contextId != null || force;
            }
            else if (obj is IUIComponent || obj is IEntity || obj is IList || (obj != null && ObjectUtil.isSimple(obj))) {
                remove = _contextId != null || force || restrict;
            }
            
            if (remove) {
                if (obj != null)
                    _entityManager.removeReference(obj, null, null, new ContextVariable(name));
                
                if (obj is IEventDispatcher) {
                	meta_unregisterComponent(name, obj);
                	
                    // IEventDispatcher(obj).removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, meta_componentPropertyChangeHandler);
                }
                
                delete notifiers[name];
                this[name] = null;
                
                unbindIns(name);
                unbindOuts(name);
            }
            
            if (force)
                delete object[name];
            
            log.debug("destroyed component instance {0} (force: {1}, remove: {2})", name, force, remove);
        }
        
        
        /**
         *	@private 	
         *  Marks the current context for deletion at next remote operation
         *  This is called when the underlying conversation is ended
         */ 
        public function meta_markAsFinished():void {
            _finished = true;
        }
        
        /**
         *  Indicates if the current conversation has been marked as finished by the server
         *	@return conversation is ended
         */ 
        public function get meta_finished():Boolean {
        	return _finished;
       	}
        
        
        /**
         * 	@private
         *  Clears entity cache
         */ 
        public function meta_clearCache():void {
        	_entityManager.clearCache();
        }
        
        
        /**
         *	@private 
         *	Tracking on managed entities is enabled
         *
         *  @return tracking enabled
         */
        public function get meta_tracking():Boolean {
            return _tracking;
        }
        /**
         * 	@private
         *	Define tracking on managed entities
         *
         *  @param tracking
         */
        public function set meta_tracking(tracking:Boolean):void {
            _tracking = tracking;
        }
        
        
		/**
         * 	@private
		 *  @return allow uninitialize of collections
		 */
		public function get meta_uninitializeAllowed():Boolean {
			return _entityManager.uninitializeAllowed;
		}
		
        /**
         * 	@private
         *	Allow uninitialize of persistence collections
         *
         *  @param allowed allow uninitialize of collections
         */
        public function set meta_uninitializeAllowed(allowed:Boolean):void {
        	_entityManager.uninitializeAllowed = allowed;
        }
		
        
        /**
         *	Entity manager is dirty when any entity/collection/map has been modified
         *
         *  @return is dirty
         */
        public function get meta_dirty():Boolean {
            return _entityManager.dirty;
        }
		
		/**
		 * 	Enables or disabled dirty checking in this context
		 *  
		 *  @param enabled
		 */
		public function set meta_dirtyCheckEnabled(enabled:Boolean):void {
			_entityManager.dirtyCheckEnabled = enabled;
		}
		
		/**
		 *	Return the current set of dirty properties
		 *
		 *  @return map of saved properties
		 */
		public function meta_getSavedProperties():Dictionary {
			return _entityManager.savedProperties;
		}
        
        /**
         *  @private 
         *  Registers a reference to the provided object with either a parent or res
         * 
         *  @param obj an entity
         *  @param parent the parent entity
         *  @param res the context expression
         */ 
        public function meta_addReference(obj:Object, parent:Object, propName:String, res:IExpression = null):void {
        	_entityManager.addReference(obj, parent, propName, res);
        }
        
		/**
		 *  @private
		 *  Attach an entity to this context
		 * 
		 *  @param entity an entity
		 *  @param putInCache put entity in cache
		 */
		public function meta_attachEntity(entity:IEntity, putInCache:Boolean = true):void {
        	_entityManager.attachEntity(entity, putInCache);
        }
        
        
        /**
         * 	@private
         *  Abstract method: add an update to the current context
         * 
         *  @param componentName name of the component/context variable
         *  @param expr EL expression to evaluate
         *  @param value value to send to server
		 *  @param typed component name represents a typed component instance  
         */
        public function meta_addUpdate(componentName:String, expr:String, value:*, typed:Boolean = false):void {
        }

        /**
         *	@private
         *  Abstract method: Add result evaluator in current context
         * 
         *  @param componentName name of the component/context variable
         *  @param expr EL expression to evaluate
         * 
         *  @return true if the result was not already present in the current context 
         */
        public function meta_addResult(componentName:String, propertyName:String):Boolean {
            return false;
        }
        
        
        private var _noCreate:String = null;
        
        /**
         *  @private
         *  Overriden proxy method: instantiates client components when needed or defines a remote proxy
         * 
         *  @param name name of the component/context variable
         */
        override flash_proxy function getProperty(name:*):* {
        	return meta_getInstance(name, !_tide.isComponent(name) || _tide.isComponentAutoCreate(name));
        }
		
		/**
		 *  Returns the component instance in this context but never creates proxies
		 * 
		 *  @param name name of the component/context variable
		 */
		public function meta_getInstanceNoProxy(name:*):* {
			return meta_getInstance(name, !_tide.isComponent(name) || _tide.isComponentAutoCreate(name), false, true);
		}
		
		/**
		 *  Returns the current component instance implementation in this context for the specified class/interface
		 * 
		 *  @param serviceClass class/interface to implement
		 *  @param create create instance of component if not instantiated
		 *  @return the current implementation in the context
		 */
		public function byType(serviceClass:Class, create:Boolean = false):Object {
			var producer:IComponentProducer = _tide.getProducer(serviceClass);
			if (producer == null)
				return null;
			
			return producer.produce(this, create);
		}
		
		/**
		 *  Returns all component instances in this context implementing the specified class/interface
		 * 
		 *  @param serviceClass class/interface to implement
		 *  @param create create instance of component if not instantiated
		 *  @return the current implementation in the context
		 */
		public function allByType(serviceClass:Class, create:Boolean = false):Array {
			var producers:Array = _tide.getAllProducers(serviceClass);
			if (producers == null || producers.length == 0)
				return [];
			
			var context:BaseContext = this;
			return producers.map(function(producer:*, index:int, array:Array):Object {
				return producer.produce(context, create);
			}).filter(function(producer:*, index:int, array:Array):Boolean {
				return producer != null;
			});
		}
		
        
        /**
         *  Returns the component instance in this context
         * 
         *  @param name name of the component/context variable
         *  @param create instantiate if not present
         *  @param forceNoCreate never instantiate a new component in any case
		 *  @param noProxy don't create remote proxy if not found
         */
        public function meta_getInstance(name:*, create:Boolean = false, forceNoCreate:Boolean = false, noProxy:Boolean = false):* {
        	var base:Subcontext = null;
        	var baseName:String = null;
        	var checkBase:Boolean = false;
        	
        	if (!_tide.isComponentGlobal(name)) {
	        	if (_tide.currentModulePrefix != "" && name.toString().indexOf(_tide.currentModulePrefix) < 0 && _tide.currentModulePrefix != name.toString() + ".") {
	    			if (name.toString().indexOf(".") > 0)
	    				throw new ReferenceError("Cannot access component " + name + " from module " + _tide.currentModulePrefix);
			
					if (!_tide.isComponent(name)) {
						name = _tide.currentModulePrefix + name;
						checkBase = true;
					}
	        	}
	        	else
	        		checkBase = true;
	       	}
        	
        	if (checkBase) {
	        	var idx:int = name.toString().lastIndexOf(".");
	        	if (idx > 0) {
					base = meta_getSubcontext(name.toString().substring(0, idx));
					baseName = name.toString().substring(idx+1);
	        	}
	        }
        	
            var result:Object = base ? base.meta_internalGetProperty(baseName) : super.getProperty(name);
            if (result != null)
                return result;
            
            if (_parentContext != null && _tide.isComponent(name) && !_tide.isSubcontext(name)) {
				var parentContext:BaseContext = null;
				if (_tide.isComponentInSession(name))
					parentContext = _tide.getContext();
				else if (!_parentContext.meta_isGlobal() && _parentContext.meta_getInstance(name, false, true) != null)
					parentContext = _parentContext;
				
				if (parentContext != null) {
					var saveTracking:Boolean = parentContext.meta_tracking;
					parentContext.meta_tracking = false;
					result = parentContext.meta_getInstance(name, create, forceNoCreate);
					parentContext.meta_tracking = saveTracking;
				}
            }
            
            if (result != null || _finished || _destroying || forceNoCreate || _setting == name.toString() || _noCreate == name.toString() 
            	|| (!create && !(_tide.isComponent(name) && _tide.isComponentAutoCreate(name))))
                return result;
            
            var instance:Object = _tide.newComponentInstance(name, this, noProxy);
            
            if (instance != null) {
				log.debug("getInstance (create) {0}", name);
        		setProperty(name, instance);
			}

            return instance;
        }
        
        /**
         *  Returns the context where the component instance is stored
         * 
         *  @param name component name
         *  @param component component instance
         *  @return context
         */
        public function meta_getContext(name:*, component:Object):BaseContext {
			var base:Subcontext = null;
			var baseName:String = null;
			var checkBase:Boolean = false;
			
			if (!_tide.isComponentGlobal(name)) {
				if (_tide.currentModulePrefix != "" && name.toString().indexOf(_tide.currentModulePrefix) < 0 && _tide.currentModulePrefix != name.toString() + ".") {
					if (name.toString().indexOf(".") > 0)
						throw new ReferenceError("Cannot access component " + name + " from module " + _tide.currentModulePrefix);
					
					if (!_tide.isComponent(name)) {
						name = _tide.currentModulePrefix + name;
						checkBase = true;
					}
				}
				else
					checkBase = true;
			}
			
			if (checkBase) {
				var idx:int = name.toString().lastIndexOf(".");
				if (idx > 0) {
					base = meta_getSubcontext(name.toString().substring(0, idx));
					baseName = name.toString().substring(idx+1);
				}
			}
			
			var instance:Object = base ? base.meta_internalGetProperty(baseName) : super.getProperty(name);
       		if (instance === component)
       			return this;
       		
       		if (_parentContext == null)
       			return null;
			
       		return _parentContext.meta_getContext(name, component);
        }
        

        /**
         *	@private 
         *  Overriden from proxy
         * 
         *  @param name property name
         *  @param value previous value
         * 
         *  @return new value
         */
        override object_proxy function getComplexProperty(name:*, value:*):* {

            if (getQualifiedClassName(value) == "Object") {
                var component:Object = _tide.newComponentInstance(name, this);
                if (component) {
                    if (component is IEventDispatcher)
                        IEventDispatcher(component).addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, propertyChangeHandler, false, 0, true);
                    notifiers[name] = component;
                }
                return component;
            }
            else
                return super.getComplexProperty(name, value);
        }


        private var _setting:String = null;
        
        
		/**
		 * 	@private
		 *  
		 * 	Unregister listeners for this component
		 * 
		 *  @param name component name
		 *  @param obj component instance
		 */
        public function meta_unregisterComponent(name:String, obj:Object):void {
            if (_componentListeners[obj] != null) {
            	if (obj is IEventDispatcher)
                	IEventDispatcher(obj).removeEventListener(TideUIEvent.TIDE_EVENT, meta_componentEventHandler);
				var eventTypes:Array = _tide.getDescriptor(name).events;
                for each (var eventType:String in eventTypes)
                	IEventDispatcher(obj).removeEventListener(eventType, meta_componentEventHandler);
                delete _componentListeners[obj];
                
				unbindIns(name);
				unbindOuts(name);
            }
		}
		
		
		/**
		 *  @private
		 *  Attach an object to this context
		 * 
		 *  @param object an object
		 */
		public function meta_attach(object:Object):void {
			var cache:Dictionary = new Dictionary(true);
			var saveTracking:Boolean = _tracking;
			_tracking = false;
			_entityManager.attach(object, cache);
			_tracking = saveTracking;
		}
		
		
        /**
         *  @private 
         *  Get or build a subcontext with the specified name
         * 
         *  @param ns namespace
         * 
         *  @return the corresponding subcontext
         */ 
		private function meta_getSubcontext(ns:String):Subcontext {
			var sc:Subcontext = super.getProperty(ns) as Subcontext;
			if (sc == null) {
				sc = new Subcontext(ns, this);
				super.setProperty(ns, sc);
			}
			return sc;
		}
		
		
        /**
         *  @private 
         *  Overriden from proxy: set current property value
         * 
         *  @param name name of the component/context variable
         *  @param value new value
         */ 
        override flash_proxy function setProperty(name:*, value:*):void {
        	var base:Subcontext = null;
        	var baseName:String = null;
        	var checkBase:Boolean = false;
        	
        	var saveTracking:Boolean;
        	
        	if (!_destroying)
        		_tide.removeProxyDescriptor(name, value);
			
        	if (!_tide.isComponentGlobal(name)) {
	        	if (_tide.currentModulePrefix != "" && name.toString().indexOf(_tide.currentModulePrefix) < 0 && _tide.currentModulePrefix != name.toString() + ".") {
	    			if (name.toString().indexOf(".") > 0)
	    				throw new ReferenceError("Cannot access component " + name + " from module " + _tide.currentModulePrefix);
			
					if (!_tide.isComponent(name)) {
						name = _tide.currentModulePrefix + name;
						checkBase = true;
					}
	        	}
	        	else
        			checkBase = true;
        	}
        	
        	if (checkBase) {
	        	var idx:int = name.toString().lastIndexOf(".");
	        	if (idx > 0) {
	        		base = meta_getSubcontext(name.toString().substring(0, idx));
					baseName = name.toString().substring(idx+1);
	        	}
	        }
			
			var isset:Boolean = false;
            if (_parentContext != null && _tide.isComponent(name) && !_tide.isSubcontext(name)) {
                // Delegate to parent/global context if the name is not bound to a conversation scope
				var parentContext:BaseContext = null;
				if (_tide.isComponentInSession(name))
					parentContext = _tide.getContext();
				else if (!_parentContext.meta_isGlobal() && _parentContext.meta_getInstance(name, false, true) != null)
					parentContext = _parentContext;
				
				if (parentContext != null) {
					saveTracking = parentContext.meta_tracking;
					parentContext.meta_tracking = false;
					parentContext.setProperty(name, value); 
					parentContext.meta_tracking = saveTracking;
					isset = true;
				}
            }
            if (!isset) {
            	var eventTypes:Array;
            	var eventType:String;
                var saveSetting:String = _setting;
                _setting = name.toString();
                
                var prev:Object = base ? base.meta_internalGetProperty(baseName) : object[name];
                
                if (prev is IEventDispatcher) {
                	meta_unregisterComponent(name.toString(), prev);
                    
                    meta_disinject(name.toString());
                    // IEventDispatcher(prev).removeEventListener(PropertyChangeEvent.PROPERTY_CHANGE, meta_componentPropertyChangeHandler);
                }
                
                if (prev != null)
                    _entityManager.removeReference(prev, null, null, new ContextVariable(n));
                
                saveTracking = _tracking;
                _tracking = true;
                
                if (base)
                	base.meta_internalSetProperty(baseName, value);
                else
                	super.setProperty(name, value);
                
                _tracking = saveTracking;
                
                if (value != null) {
                    var n:String = name is QName ? QName(name).localName : name;
                    if (value is IEntity) {
                        // Setup context for entity context variable
                        _entityManager.addReference(value, null, null, new ContextVariable(n));
                        meta_addUpdate(n, null, value);
                    }
                    else if ((value is IList || ObjectUtil.isSimple(value)) && !(value is IComponent || value is IUIComponent)) {
                        _entityManager.addReference(value, null, null, new ContextVariable(n));
                        meta_addUpdate(n, null, value);
                    }
                    else if (value is Subcontext) {
                    	Subcontext(value).meta_init(n, this);
                        if (_componentListeners[value] == null) {
                        	_contextManager.unregisterComponent(n, value);
                        	
                        	value.addEventListener(TideUIEvent.TIDE_EVENT, meta_componentEventHandler, false, 0, true);
                            _componentListeners[value] = n;
						}
                    }
                    else if (!(value is Component)) {
                        _tide.addComponentFromInstance(value, n, _parentContext != null);

	                    if (value is IEventDispatcher) { 
	                        if (_componentListeners[value] == null) {
	                        	_contextManager.unregisterComponent(n, value);
	                        	
	                            if (value is IEventDispatcher) {
	                            	IEventDispatcher(value).addEventListener(TideUIEvent.TIDE_EVENT, meta_componentEventHandler, false, 0, true);
		                            _componentListeners[value] = Tide.getModulePrefix(n);
								}
								eventTypes = _tide.getDescriptor(n).events;
								if (eventTypes != null && eventTypes.length > 0) {
		                            for each (eventType in eventTypes)
		                            	IEventDispatcher(value).addEventListener(eventType, meta_componentEventHandler, false, 0, true);
	                            	_componentListeners[value] = Tide.getModulePrefix(n);
		                        }
	                        }
	                        
	                        // Inject new instance in all corresponding [Inject] typed injection points
	                        meta_inject(n, value);
							
                        	// IEventDispatcher(value).addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, meta_componentPropertyChangeHandler, false, int.MAX_VALUE, true);
	                    }
                       	
                       	var savePrefix:String = _tide.currentModulePrefix;
                       	if (base)
                       		_tide.currentModulePrefix = base.meta_name + '.';
                       	
                        meta_initInstance(n, value);
                        
                        _tide.currentModulePrefix = savePrefix;
                    }
                }
                
                _setting = saveSetting;
            }
        }


	    public static function bindProperty(
                                site:Object, prop:Object,
                                host:Object, chain:Object,
                                commitOnly:Boolean = false):ChangeWatcher {
        	var w:ChangeWatcher =
            	ChangeWatcher.watch(host, chain, null, commitOnly);
        	
        	if (w != null) {
            	var assign:Function = function(event:*):void {
                	site[prop] = w.getValue();
            	};
            	w.setHandler(assign);
            	assign(null);
        	}
        	
        	return w;
    	}
    	
    	
    	private function meta_disinject(name:String):void {
    		var descriptor:ComponentDescriptor = _tide.getDescriptor(name, false);
    		if (descriptor == null)
    			return;
    		
			var value:Object = meta_getInstance(name, false);
			for each (var type:Type in descriptor.types) {
				var ips:Array = _tide.getInjectionPoints(type);
				if (ips != null) {
					for each (var ip:Array in ips) {
						var comp:Object = meta_getInstance(String(ip[0]), false);
						if (comp != null && comp[ip[1]] === value)
							comp[ip[1]] = null;
					}
				}
			}
		}
    	
    	private function meta_inject(name:String, instance:Object):void {
			for each (var type:Type in _tide.getDescriptor(name).types) {
				var ips:Array = _tide.getInjectionPoints(type);
				for each (var ip:Array in ips) {
					var comp:Object = this[ip[0]];
					if (comp != null)
						comp[ip[1]] = instance;
				}
			}
		}    		
    	
        
        /**
         *	Binds a component property from a context variable (should be done with [In])
         *  
         *  @param componentName component name
         *  @param component component instance
         *  @param propName property name
         *  @param create no instance created when create = false
         *  @param contextPropName context variable name
         */ 
        public function bindIn(componentName:String, component:Object, propName:Object, create:Boolean = true, contextPropName:Object = null):void {
            var w:ChangeWatcher;
            var exprPath:Array = null;
            
            if (contextPropName == null)
                contextPropName = propName;
            
            if (contextPropName.match(/#{.*}/)) {
                var expr:String = contextPropName.substring(2, contextPropName.length-1);
                exprPath = expr.split(".");
                var obj:Object = this;
                for (var i:int = 0; i < exprPath.length-1; i++)
                    obj = obj[exprPath[i]];
            }
            else
                exprPath = [ contextPropName ];
           	
           	var oldNoCreate:String = _noCreate;
           	if (!create && exprPath.length == 1)
           		_noCreate = exprPath[0];
           	
           	var base:Object = this;
           	if (!_tide.isComponent(exprPath[0])) {
           		var ns:String = Tide.getNamespace(componentName);
           		if (ns) {
           			base = meta_getSubcontext(ns);
					_noCreate = ns + "." + _noCreate;
				}
           	}
            
            // Tracking must not be enabled here: it's enabled later in setProperty
//            var saveTracking:Boolean = _tracking;
//            _tracking = true;
            w = bindProperty(component, propName, base, exprPath);
            var name:String = exprPath[0];
            var watchers:Array = _inWatchers[componentName] as Array;
            if (watchers == null) {
                watchers = new Array();
                _inWatchers[componentName] = watchers;
            }
            watchers.push(w);
//            _tracking = saveTracking;

            log.debug("context.{0} bound to [In] {1}.{2}", contextPropName, componentName, propName);

			_noCreate = oldNoCreate;
        }
        
        /**
         *	Unbinds all [In] component properties
         *  
         *  @param componentName component name
         */ 
        public function unbindIns(componentName:String):void {
            var inWatchers:Array = _inWatchers[componentName] as Array;
            if (inWatchers != null) {
                for each (var w:ChangeWatcher in inWatchers)
                    w.reset(null);
                delete _inWatchers[componentName];
                
                log.debug("{0} [In] bindings removed from {1}", inWatchers.length, componentName);
            }
        }
        
        /**
         *	Binds a component property from a context variable (should be done with [In])
         *  
         *  @param type injection point class
         *  @param component target component instance
         *  @param componentName target component name
         *  @param propName property name
         *  @param create no instance created when create = false
         *  @param contextPropName context variable name
         */ 
        public function injectByType(type:Class, component:Object, componentName:String, propName:Object, create:Boolean = true):void {
        	var producer:IComponentProducer = _tide.getProducer(type);
        	if (producer == null)
        		return;
        	
        	component[propName] = producer.produce(this, create);
            
            log.debug("context.{0} injected into [Inject] {1}.{2}", producer.componentName, componentName, propName);
        }
        
        /**
         *	Binds a component property to a context variable (should be done with [Out])
         *  
         *  @param componentName component name
         *  @param component component instance
         *  @param propName property name
         *  @param contextPropName context variable name
         */
        public function bindOut(componentName:String, component:Object, propName:Object, contextPropName:Object = null):void {
            if (contextPropName == null)
                contextPropName = propName;
            var sourceName:String = componentName + "." + propName;
            
            var watchers:Array = _outWatchers[componentName] as Array;
            if (watchers == null) {
                watchers = new Array();
                _outWatchers[componentName] = watchers;
            }
            var outWatcher:Object = null; 
            for each (var ow:Object in watchers) {
                if (ow.sourceName == sourceName) {
                    outWatcher = ow;
                    break;
                }
            }
            if (outWatcher == null) {
            // Tracking must not be enabled here: it's enabled later in setProperty
//                var saveTracking:Boolean = _tracking;
//                _tracking = true;
           	
           		var base:Object = this;
	           	if (!_tide.isComponent(contextPropName is QName ? QName(contextPropName).localName : String(contextPropName))) {
	           		var ns:String = Tide.getNamespace(componentName);
	           		if (ns)
	           			base = meta_getSubcontext(ns);
	           	}
	           	
                var w:ChangeWatcher = BindingUtils.bindProperty(base, contextPropName is QName ? QName(contextPropName).localName : String(contextPropName), 
                	component, 
                	propName is QName ? { name: propName.localName, getter: function(host:*):* { return host[propName] }} : propName);
                watchers.push({ sourceName: sourceName, watcher: w });
//                _tracking = saveTracking;

                log.debug("context.{0} bound from [Out] {1}.{2}", contextPropName, componentName, propName);
            }
            else
                outWatcher.watcher.reset(component);
        }
        
        /**
         *	Unbinds all [Out] component properties
         *  
         *  @param componentName component name
         */ 
        public function unbindOuts(componentName:String):void {
            var outWatchers:Array = _outWatchers[componentName] as Array;
            if (outWatchers != null) {
                for each (var ow:Object in outWatchers) {
                    if (ow.watcher != null)
                        ow.watcher.reset(null);
                }
                
                log.debug("{0} [Out] bindings reset for {1}", outWatchers.length, componentName);
            }
        }
        
        
        /**
         *   Registers a tide context event listener (should be done with [Observer])
         * 
         *   @param type event type
         *   @param handler function callback
         *   @param remote listener for remote events
         */
        public function addContextEventListener(type:String, handler:Function, remote:Boolean = false):void {
            if (remote || _parentContext == null)
                _tide.addContextEventListener(type, handler, remote);
            else
                addEventListener(type, handler, false, 0, true);
        }
        
        /**
         *  Dispatches a context event (possibly in a subcontext)
         *   
         *  @param type event type
         *  @param params optional event parameters
         */
        public function raiseEvent(type:String, ...params:Array):void {
            _tide.invokeObservers(this, _tide.currentModulePrefix, type, params);
        }
        
        protected function meta_internalRaiseEvent(type:String, params:Array):void {
            _tide.invokeObservers(this, _tide.currentModulePrefix, type, params);
        }
        
        /**
         *  Dispatches a context event (always in the global context)
         *   
         *  @param type event type
         *  @param params optional event parameters
         */
        public function raiseGlobalEvent(type:String, ...params:Array):void {
            _tide.invokeObservers(this, "", type, params);
        }
                
        /**
         *  Dispatches a context event 
         *   
         *  @param type event type
         *  @param params optional event parameters
         */
        public override function dispatchEvent(event:Event):Boolean {
        	if (event.type == TideUIEvent.TIDE_EVENT) {
        		meta_dispatchEvent(event);
        		return true;
        	}
        	return super.dispatchEvent(event);
        }
        
        
        /**
         * 	@private
         *  Internal event handler for TideUIEvent
         * 	Redispatches the event to registered observers
         *
         *  @param event UI event intercepted from a managed UI component 
         */ 
        private function meta_componentEventHandler(event:Event):void {
        	var modulePrefix:String = _componentListeners[event.currentTarget]; 
            if (modulePrefix != null && !_destroyScheduled) {
            	meta_dispatchEvent(event, modulePrefix);
                event.stopPropagation();
	      	}
        }
        
        /**
         * 	@private
         *  Internal event dispatcher for TideUIEvent
         *
         *  @param event Tide UI event
         *  @param modulePrefix subcontext in which the event should be dispatched 
         */ 
        public function meta_dispatchEvent(event:Event, modulePrefix:String = ""):void {
        	var eventType:String = null;
        	var params:Array = null;
        	var eventParam:Event = null;
        	if (event is TideUIEvent) {
	        	eventType = TideUIEvent(event).eventType; 
        		params = TideUIEvent(event).params;
        	}
        	else {
        		eventType = "$TideEvent$" + getQualifiedClassName(event);
        		eventParam = event;
        	}
    		
            var ctx:BaseContext = this;
            if (event is IConversationEvent) {
                ctx = _contextManager.newContext(IConversationEvent(event).conversationId, contextId);
                // Clone parameters to pass in conversation context
                if (eventParam != null) {
                	var clonedEvent:Event = eventParam.clone();
		            var cinfo:Object = ObjectUtil.getClassInfo(clonedEvent, null, { includeReadOnly: false, includeTransient: false });
		            for each (var p:String in cinfo.properties) {
		                var o:Object = clonedEvent[p];
		                if (o === eventParam[p] && !(o is Class) && !(o is IUIComponent))
		                	clonedEvent[p] = ctx.meta_mergeExternal(ObjectUtil.copy(o), null);
		            }
                	params = [ clonedEvent ];
                }
                else {
                	params = new Array();
                	for each (o in TideUIEvent(event).params) {
                		if (ObjectUtil.isSimple(o) || o is Class || o is Function || o is DisplayObject)
                			params.push(o);
                		else
                			params.push(ctx.meta_mergeExternal(ObjectUtil.copy(o), null)); 
                	}
                }
            }
            else if (eventParam != null)
        		params = [ eventParam ];
        	
        	_tide.invokeObservers(ctx, modulePrefix, eventType, params);	
		}
		    	
        
        /**
         * 	@private
         *  @return true is the context is already listening for this UI component  
         */ 
        public function meta_listensTo(uiComponent:IUIComponent):Boolean {
        	return _componentListeners[uiComponent] != null;
        }
        
        
        /**
         * 	@private
         * 
         * 	Reset current call context and returns saved context
         * 
         *  @return saved call context
         */
        public function meta_saveAndResetCallContext():Object {
        	return null;
        }
        
        /**
         *	@private
         * 
         * 	Restore call context
         * 
         *  @param callContext object containing the current call context
         */ 
        public function meta_restoreCallContext(callContext:Object):void {
		}
        
        
        
        /**
         *	@private 	
         *  Abstract method: constructs a call object and prepares the remote operation
         * 
         *  @param operation current operation
         *  @param withContext include additional context with call
         * 
         *  @return the call object
         */
        public function meta_prepareCall(operation:AbstractOperation, withContext:Boolean = true):IInvocationCall {
            return null;
        }
        
        
        /**
         *	@private 	
         *  Calls a remote component
         * 
         *  @param component the target component
         *  @param op name of the called metho
         *  @param arg method parameters
         *  @param withContext add context sync data to call
         * 
         *  @return the operation token
         */
        public function meta_callComponent(component:IComponent, op:String, args:Array, withContext:Boolean = true):AsyncToken {
            if (_finished)
		        throw new InvalidContextError(_contextId);
		    
		    var responder:ITideResponder = null;
		    if (args != null && args.length > 0 && args[args.length-1] is ITideResponder)
		        responder = args.pop() as ITideResponder;
            else {
                var resultHandler:Function = null;   
                if (args && args.length > 0 && args[args.length-1] is Function)
                    resultHandler = args.pop() as Function;
                
                var faultHandler:Function = null;
                if (args && args.length > 0 && args[args.length-1] is Function) {
                    faultHandler = resultHandler;
                    resultHandler = args.pop() as Function;
                }
                responder = new TideResponder(resultHandler, faultHandler);
            }
            
			for (var i:int = 0; i < args.length; i++) {
				if (args[i] is IPropertyHolder)
					args[i] = IPropertyHolder(args[i]).object;
				meta_mergeExternal(args[i]);
			}

            var method:Method = Type.forInstance(component).getInstanceMethodNoCache(op);
            for each (var app:IArgumentPreprocessor in allByType(IArgumentPreprocessor, true))
                args = app.preprocess(method, args);

            _tracking = false;
            var token:AsyncToken = _tide.invokeComponent(this, component, op, args, responder, withContext);
            _tracking = true;
            
            _firstCall = false;
            return token;
        }
        
        
        /**
         *  Resync current context with server (similar to invoke with no operation)
         * 
         *  @param resultHandler result handler
         *  @param faultHandler fault handler
         * 
         *  @return the operation token
         */
        public function meta_resync(resultHandler:Function = null, faultHandler:Function = null):AsyncToken {
            if (_finished)
		        throw new InvalidContextError(_contextId);
		    
		    var responder:ITideResponder = new TideResponder(resultHandler, faultHandler);
            
            _tracking = false;
            var token:AsyncToken = _tide.resyncContext(this, responder);
            _tracking = true;
            
            _firstCall = false;
            return token;
        }
        
                
        /**
         *  Indicates if an object is initialized
         *  For non managed objects, always return true
         * 
         *  @return true when initialized
         */
		public function meta_isObjectInitialized(object:Object):Boolean {
			if (object == null)
				return true;
			if (object is IEntity)
				return object.meta::isInitialized();
			if (object is IPersistentCollection)
				return IPersistentCollection(object).isInitialized();
			return true;
		}
        
        
        /**
         * 	@private
         *  Calls an object initializer
         * 
         *  @param obj collection to initialize
         * 
         *  @return the operation token
         */
        public function meta_initializeObject(obj:Object):void {
            if (_finished)
            	return;
            
            log.debug("initialize {0}", toString(obj));
            
            var path:IExpression = null;
	        if (obj is PersistentCollection && obj.entity is IEntity) {
                if (_contextId != null && meta_isContextIdFromServer)
                    path = _entityManager.getReference(obj.entity, false);
                
	            _entityManager.attachEntity(IEntity(obj.entity));
	        }
	        
            _tracking = false;
            _tide.initializeObject(this, obj, path);
            _tracking = true;
        }
        
        
		/**
		 * 	Allows to disable automatic loading of lazy collections
		 */
        public var meta_lazyInitializationDisabled:Boolean = false;
        
        
        /**
         *  @private 
         *  Calls an object validator
         * 
         *  @param obj entity to validate
         *  @param propertyName property to validate
         *  @param value value to validate
         * 
         *  @return the operation token
         */
        public function meta_validateObject(obj:IEntity, propertyName:String, value:Object):AsyncToken {
	      	_entityManager.attachEntity(obj);
	       	
            _tracking = false;
            var token:AsyncToken = _tide.validateObject(this, obj, propertyName, value);
            _tracking = true;
            return token;
        }
        
        
        /**
         *  @private 
         *  Initializes the result handler
         */
        protected function meta_preResult():void {
        }
        
        
        /**
         *  @private  
         *  (Almost) abstract method: manages a remote call result
         *  This should be called by the implementors at the end of the result processing
         * 
         *  @param componentName name of the target component
         *  @param operation name of the called operation
         *  @param ires invocation result object
         *  @param result result object
         *  @param mergeWith previous value with which the result will be merged
         */
        public function meta_result(componentName:String, operation:String, invocationResult:IInvocationResult, result:Object, mergeWith:Object = null):void {
        }

        /**
         *  @private 
         *  Abstract method: manages a remote call fault
         * 
         *  @param componentName name of the target component
         *  @param operation name of the called operation
         *  @param emsg error message
         */
        public function meta_fault(componentName:String, operation:String, emsg:ErrorMessage):void {
        }
        
        
        /**
         *  @private 
         *  Abstract method: manages a login call
         * 
         *  @param identity identity component
         *  @param username username
         *  @param password password
         *  @param resultHandler optional result handler method
         *  @param faultHandler optional fault handler method
         *  
         *  @return operation token
         */
        public function meta_login(identity:IIdentity, username:String, password:String, resultHandler:Function = null, faultHandler:Function = null):AsyncToken {
            var responder:TideResponder = new TideResponder(resultHandler, faultHandler);

            return _tide.login(this, identity, username, password, responder);
        }
        
        /**
         *  @private 
         *  Abstract method: manages a login check call
         * 
         *  @param identity identity component
         *  @param resultHandler optional result handler method
         *  @param faultHandler optional fault handler method
         *  
         *  @return operation token
         */
        public function meta_isLoggedIn(identity:IIdentity, resultHandler:Function = null, faultHandler:Function = null):AsyncToken {
            var responder:TideResponder = new TideResponder(resultHandler, faultHandler);

            return _tide.checkLoggedIn(this, identity, responder);
        }
        
        
        /**
         *  @private 
         *  Abstract method: manages a logout call
         * 
         *  @param identity identity component
         */ 
        public function meta_logout(identity:IIdentity):void {
            _tide.logout(this, identity);
        }


        /**
         *  Convenient toString implementation which avoids triggering collection initialization
         *
         *  @param object object to describe
         *  @return toString
         */ 
        public static function toString(object:Object):String {
        	if (object == null)
        		return "null";
        	
            if (object is IEntity && object.hasOwnProperty("id")) {
            	var em:IEntityManager = Managed.getEntityManager(IEntity(object));
            	var cid:String = (em != null 
            		? (Object(em).contextId != null 
            			? " (" + Object(em).contextId + ")" 
            			: " (global)")
            		: " (no ctx)");

				try {
	            	if (!object.meta::isInitialized())
		                return "Uninitialized entity: " + getQualifiedClassName(object) + ":" + object.id + cid;
	            }
	            catch (e:ReferenceError) {
	            	// Entity does not implement meta:isInitialized()
	            }
            	return getQualifiedClassName(object) + ":" + object.id + cid;
            }
            
            var s:String;
            var i:int;
            if (object == null)
                return "null";
            else if (object is Array) {
                s = "Array [";
                var a:Array = object as Array;
                for (i = 0; i < a.length; i++) {
                    if (i > 0)
                        s += ", ";
                    s += BaseContext.toString(a[i]);
                }
                s += "]";
                return s;
            }
            else if (object is IPersistentCollection && !IPersistentCollection(object).isInitialized()) {
                return object.toString();
            }
            else if (object is IList) {
                s = getQualifiedClassName(object) + " [";
                var l:IList = object as IList;
                for (i = 0; i < l.length; i++) {
                    if (i > 0)
                        s += ", ";
                    s += BaseContext.toString(l.getItemAt(i));
                }
                s += "]";
                return s;
            }
            return object.toString();
        }
        
        
        /**
         *  Equality for objects, using uid property when possible
         *
         *  @param obj1 object
         *  @param obj2 object
         * 
         *  @return true when objects are instances of the same entity
         */ 
        public function meta_objectEquals(obj1:Object, obj2:Object):Boolean {
        	return _entityManager.objectEquals(obj1, obj2); 
        }
        
        /** 
         *  @private 
         * 	Retrives an entity in the cache from its uid
         *   
         *  @param obj an entity
         *  @param nullIfAbsent return null if entity not cached in context
         */
        public function meta_getCachedObject(object:Object, nullIfAbsent:Boolean = false):Object {
        	return _entityManager.getCachedObject(object, nullIfAbsent);
        }
        
        /** 
         *  @private 
         * 	Retrives the owner entity of the provided object (collection/map/entity)
         *   
         *  @param obj an entity
         */
        public function meta_getOwnerEntity(object:Object):Object {
        	return _entityManager.getOwnerEntity(object);
        }
        
        /**
         *  @private 
         *  Retrieves context expression path for the specified entity
         *   
         *  @param obj an entity
         *  @param recurse should recurse until 'real' context path, otherwise object reference can be returned
         *  @return the path from the entity context (or null is no path found)
         */
        public function meta_getReference(obj:Object, recurse:Boolean = true):IExpression {
            var cache:Dictionary = new Dictionary();
            return _entityManager.getReference(obj, recurse, cache);
        }

    
        /**
         *  Merge an object coming from the server in the context
         *
         *  @param obj external object
         *  @param prev existing object on which incoming data has to be merged 
         *  @param sourceSessionId sessionId of the incoming data (null when from current session) 
         *
         *  @return merged object (should === prev when prev not null)
         */
        public function meta_mergeExternalData(obj:Object, prev:Object = null, sourceSessionId:String = null, removals:Array = null):Object {
            _entityManager.initMerge();

        	if (sourceSessionId && sourceSessionId != _tide.sessionId)
        		_entityManager.externalData = true;
        	
        	var next:Object = meta_mergeExternal(obj, prev);

            _entityManager.handleRemovals(removals);
        	
        	_entityManager.handleMergeConflicts();
        	
        	meta_clearCache();
        	
    		_entityManager.externalData = false;
    		return next;
        }

		
		/**
		 * 	@private
		 *  Handle data updates
		 *
		 *  @param sourceSessionId sessionId from which data updates come (null when from current session) 
		 *  @param updates list of data updates
		 */
		public function meta_handleUpdates(sourceSessionId:String, updates:Array):void {
			var merges:Array = [], removals:Array = [];
			for each (var update:Array in updates) {
                if (update[0] == 'PERSIST' || update[0] == 'UPDATE')
				    merges.push(update[1]);
                else if (update[0] == 'REMOVE')
                    removals.push(update[1]);
            }
			
			meta_mergeExternalData(merges, null, sourceSessionId, removals);
		}
		
		/**
		 * 	@private
		 *  Dispatch update events for data updates
		 *
		 *  @param updates list of data updates
		 */
		public function meta_handleUpdateEvents(updates:Array):void {
			var refreshes:Array = new Array();
			
			for each (var update:Array in updates) {
				var entity:Object = meta_getCachedObject(update[1], true);
				
				if (entity) {
					var updateType:String = String(update[0]).toLowerCase();
					
					var entityName:String = ClassUtil.getUnqualifiedClassName(update[1]);
					var eventType:String = "org.granite.tide.data." + updateType + "." + entityName;
					raiseEvent(eventType, entity);
					
					if (updateType == "persist" || updateType == "remove") {
						if (refreshes.indexOf(entityName) < 0)
							refreshes.push(entityName);
					} 
				}
			}
			
			for each (var refresh:String in refreshes)
				raiseEvent("org.granite.tide.data.refresh." + refresh);
		}
    
		
        /**
         *  @private 
         *  Merge an object coming from another context (in general the global context) in the local context
         *
         *  @param sourceContext source context of incoming data
         *  @param obj external object
		 *  @param externalData is merge from external data
         *
         *  @return merged object (should === previous when previous not null)
         */
        public function meta_mergeFromContext(sourceContext:BaseContext, obj:Object, externalData:Boolean = false, uninitializing:Boolean = false):Object {
        	var saveSourceContext:BaseContext = _entityManager.sourceContext;
    		_entityManager.sourceContext = sourceContext;
			_entityManager.uninitializing = uninitializing;
        	
        	var next:Object = externalData
				? meta_mergeExternalData(obj, null, _tide.sessionId + '$')	// Force handling of external data
				: meta_mergeExternal(obj);
        	
    		_entityManager.sourceContext = saveSourceContext;
			_entityManager.uninitializing = false;
    		return next;
        }
        
        
        /**
         *  @private 
         *  Merge an object coming from the server in the context
         *
         *  @param obj external object
         *  @param previous previously existing object in the context (null if no existing object)
         *  @param expr current path from the context
         *  @param parent parent object for collections
         *  @param propertyName property name of the current object in the parent object
		 *  @param setter setter function for private property
		 *  @param forceUpdate force update (for externalized properties) 
         *
         *  @return merged object (should === previous when previous not null)
         */
        public function meta_mergeExternal(obj:Object, previous:Object = null, expr:IExpression = null, 
										   parent:Object = null, propertyName:String = null, setter:Function = null, forceUpdate:Boolean = false):Object {
            var saveTracking:Boolean = _tracking;
            _tracking = false;
            
        	var next:Object = _entityManager.mergeExternal(obj, previous, expr, parent, propertyName, setter);
        	 
            _tracking = saveTracking;
            
            return next;
        }
        
        
        /**
         *  @private 
         *  Evaluate an expression in the current context (very basic version of EL)
         * 
         *  @param res expression to evaluate
         * 
         *  @return value
         */ 
        protected function meta_evaluate(res:IExpression):Object {
            var obj:Object = this;
            
            if (res.componentName != null)
                obj = obj[res.componentName];
            
            if (res.expression != null) {
                var p:Array = res.expression.split(".");
                for (var i:int = 0; i < p.length; i++)
                    obj = obj[p[i] as String];
            }

            return obj;
        }
        
        
        /**
         *  Check if entity property has been changed since last remote call
         *
         *  @param entity entity to check
         *  @param propertyName property to check
         *  @param value current value to compare with saved value
         *   
         *  @return true is value has been changed
         */ 
        public function meta_isEntityPropertyChanged(entity:IEntity, propertyName:String, value:Object):Boolean {
        	return _entityManager.isEntityPropertyChanged(entity, propertyName, value);
        }
        
        
        /**
         *  Check if entity has changed since last save point
         *
         *  @param entity entity to restore
         *  @param propName property name
         *  @param value
         *   
         *  @return entity is dirty
         */ 
        public function meta_isEntityChanged(entity:IEntity, propName:String = null, value:* = null):Boolean {
            var saveTracking:Boolean = _tracking;
            _tracking = false;
            
            var dirty:Boolean = _entityManager.isEntityChanged(entity, propName, value);
            
            _tracking = saveTracking;
            return dirty;
        }
        
        
        /**
         *  Discard changes of entity from last version received from the server
         *
         *  @param entity entity to restore
         */ 
        public function meta_resetEntity(entity:IEntity):void {
            var cache:Dictionary = new Dictionary(true);
            var saveTracking:Boolean = _tracking;
            try {
				_tracking = false;
                _entityManager.resetEntity(entity, cache);
            }
            catch (e:Error) {
                log.error("Error resetting entity", e);
            }
			finally {
            	_tracking = saveTracking;
			}
        }


        /**
         *  @private 
         *  Interceptor for managed entity setters
         *
         *  @param entity entity to intercept
         *  @param propName property name
         *  @param oldValue old value
         *  @param newValue new value
         */ 
        public function meta_setEntityProperty(entity:IEntity, propName:String, oldValue:*, newValue:*):void {
        	_entityManager.setEntityProperty(entity, propName, oldValue, newValue);
        }


        /**
         *  @private 
         *  Interceptor for managed entity getters
         *
         *  @param entity entity to intercept
         *  @param propName property name
         *  @param value value
         * 
         *  @return value
         */ 
        public function meta_getEntityProperty(entity:IEntity, propName:String, value:*):* {
        	return _entityManager.getEntityProperty(entity, propName, value);
        }


        /**
         *  @private 
         *  Collection event handler to save changes on managed collections
         *
         *  @param event collection event
         */ 
        public function meta_collectionChangeHandler(event:CollectionEvent):void {
        }

        /**
         *  @private 
         *  Collection event handler to save changes on managed collections
         *
         *  @param event collection event
         */ 
        public function meta_entityCollectionChangeHandler(event:CollectionEvent):void {
        }
        
        
        /**
         *  @private 
         *  Collection event handler to save changes on managed maps
         *
         *  @param event map event
         */ 
        public function meta_mapChangeHandler(event:CollectionEvent):void {
        }

        /**
         *  @private 
         *  Collection event handler to save changes on managed maps
         *
         *  @param event map event
         */ 
        public function meta_entityMapChangeHandler(event:CollectionEvent):void {
        }
    }
}
