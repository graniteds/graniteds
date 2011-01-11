

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IEventDispatcher;
import mx.core.IPropertyChangeNotifier;
import mx.events.PropertyChangeEvent;
import mx.utils.ObjectProxy;
import mx.utils.UIDUtil;

import mx.controls.DataGrid;
import test.granite.ejb3.entity.Person;

class BindableProperty
    implements flash.events.IEventDispatcher
{
	/**
	 * generated bindable wrapper for property persons (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'persons' moved to '_678441026persons'
	 */

    [Bindable(event="propertyChange")]
    public function get persons():DataGrid
    {
        return this._678441026persons;
    }

    public function set persons(value:DataGrid):void
    {
    	var oldValue:Object = this._678441026persons;
        if (oldValue !== value)
        {
            this._678441026persons = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "persons", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property person (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'person' moved to '_991716523person'
	 */

    [Bindable(event="propertyChange")]
    public function get person():Person
    {
        return this._991716523person;
    }

    public function set person(value:Person):void
    {
    	var oldValue:Object = this._991716523person;
        if (oldValue !== value)
        {
            this._991716523person = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "person", oldValue, value));
        }
    }


    //    IEventDispatcher implementation
    //
    private var _bindingEventDispatcher:flash.events.EventDispatcher =
        new flash.events.EventDispatcher(flash.events.IEventDispatcher(this));

    public function addEventListener(type:String, listener:Function,
                                     useCapture:Boolean = false,
                                     priority:int = 0,
                                     weakRef:Boolean = false):void
    {
        _bindingEventDispatcher.addEventListener(type, listener, useCapture,
                                                 priority, weakRef);
    }

    public function dispatchEvent(event:flash.events.Event):Boolean
    {
        return _bindingEventDispatcher.dispatchEvent(event);
    }

    public function hasEventListener(type:String):Boolean
    {
        return _bindingEventDispatcher.hasEventListener(type);
    }

    public function removeEventListener(type:String,
                                        listener:Function,
                                        useCapture:Boolean = false):void
    {
        _bindingEventDispatcher.removeEventListener(type, listener, useCapture);
    }

    public function willTrigger(type:String):Boolean
    {
        return _bindingEventDispatcher.willTrigger(type);
    }

}
