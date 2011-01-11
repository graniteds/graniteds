

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IEventDispatcher;

import mx.core.mx_internal;
import mx.data.IManaged;
import mx.data.utils.Managed;
import mx.events.PropertyChangeEvent;
import mx.utils.UIDUtil;

import test.granite.ejb3.entity.*;

class ManagedProperty
    implements mx.data.IManaged
{
	//	wrapped properties
	//

	//	public getter/setter 'uid' moved to '_115792uid'

	//	special uid getter/setter for IManaged implementation, see below


	//	IManaged implementation
	//
    [Bindable(event="propertyChange")]
    public function get uid():String
    {
        if (_115792uid == null)
        {
            _115792uid = mx.utils.UIDUtil.createUID();
        }
        return _115792uid;
    }

    public function set uid(value:String):void
    {
        var oldValue:String = _115792uid;
        if (oldValue !== value)
        {
	        _115792uid = value;
            dispatchEvent(
                mx.events.PropertyChangeEvent.createUpdateEvent(
                				this, "uid", oldValue, value));
        }
    }
    
    
    mx_internal var referencedIds:Object = {};
    mx_internal var destination:String;

    
	//	IEventDispatcher implementation
	//
    private var _eventDispatcher:flash.events.EventDispatcher = 
        new flash.events.EventDispatcher(flash.events.IEventDispatcher(this));

    public function addEventListener(type:String, listener:Function,
                                     useCapture:Boolean = false,
                                     priority:int = 0,
									 weakRef:Boolean = false):void
    {
        _eventDispatcher.addEventListener(type, listener, useCapture,
										  priority, weakRef);
    }

    public function dispatchEvent(event:flash.events.Event):Boolean
    {
        return _eventDispatcher.dispatchEvent(event);
    }

    public function hasEventListener(type:String):Boolean
    {
        return _eventDispatcher.hasEventListener(type);
    }

    public function removeEventListener(type:String,
                                        listener:Function,
                                        useCapture:Boolean = false):void
    {
        _eventDispatcher.removeEventListener(type, listener, useCapture);
    }

    public function willTrigger(type:String):Boolean
    {
        return _eventDispatcher.willTrigger(type);
    }
}
