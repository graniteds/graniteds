

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IEventDispatcher;
import mx.core.IPropertyChangeNotifier;
import mx.events.PropertyChangeEvent;
import mx.utils.ObjectProxy;
import mx.utils.UIDUtil;

import org.granite.tide.ejb.Context;
import test.granite.ejb3.entity.Person;
import test.granite.ctl.*;
import test.granite.ejb3.entity.Contact;
import test.granite.ejb3.service.PersonService;

class BindableProperty
    implements flash.events.IEventDispatcher
{
	/**
	 * generated bindable wrapper for property context (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'context' moved to '_951530927context'
	 */

    [Bindable(event="propertyChange")]
    public function get context():Context
    {
        return this._951530927context;
    }

    public function set context(value:Context):void
    {
    	var oldValue:Object = this._951530927context;
        if (oldValue !== value)
        {
            this._951530927context = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "context", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property personService (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'personService' moved to '_858278304personService'
	 */

    [Bindable(event="propertyChange")]
    public function get personService():PersonService
    {
        return this._858278304personService;
    }

    public function set personService(value:PersonService):void
    {
    	var oldValue:Object = this._858278304personService;
        if (oldValue !== value)
        {
            this._858278304personService = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "personService", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property application (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'application' moved to '_1554253136application'
	 */

    [Bindable(event="propertyChange")]
    public function get application():Object
    {
        return this._1554253136application;
    }

    public function set application(value:Object):void
    {
    	var oldValue:Object = this._1554253136application;
        if (oldValue !== value)
        {
            this._1554253136application = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "application", oldValue, value));
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

	/**
	 * generated bindable wrapper for property contact (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'contact' moved to '_951526432contact'
	 */

    [Bindable(event="propertyChange")]
    public function get contact():Contact
    {
        return this._951526432contact;
    }

    public function set contact(value:Contact):void
    {
    	var oldValue:Object = this._951526432contact;
        if (oldValue !== value)
        {
            this._951526432contact = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "contact", oldValue, value));
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
