

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
{
	//	wrapped properties
	//

	//	public getter/setter 'name' moved to '_3373707name'

    [Bindable(event="propertyChange")]
    public function get name():String
    {
        var currentValue:String = this._3373707name;
        var managedValue:String = mx.data.utils.Managed.getProperty(this, "name", currentValue);
        if (currentValue !== managedValue)
            this._3373707name = managedValue;
        return managedValue;
    }
    public function set name(value:String):void
    {
    	var oldValue:String = this._3373707name;
    	this._3373707name = value;
		mx.data.utils.Managed.setProperty(this, "name", oldValue, _3373707name);
    }


    
}
