

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

	//	public getter/setter 'phone' moved to '_106642798phone'

    [Bindable(event="propertyChange")]
    public function get phone():String
    {
        var currentValue:String = this._106642798phone;
        var managedValue:String = mx.data.utils.Managed.getProperty(this, "phone", currentValue);
        if (currentValue !== managedValue)
            this._106642798phone = managedValue;
        return managedValue;
    }
    public function set phone(value:String):void
    {
    	var oldValue:String = this._106642798phone;
    	this._106642798phone = value;
		mx.data.utils.Managed.setProperty(this, "phone", oldValue, _106642798phone);
    }

	//	public getter/setter 'person' moved to '_991716523person'

    [Bindable(event="propertyChange")]
    public function get person():Person
    {
        var currentValue:Person = this._991716523person;
        var managedValue:Person = mx.data.utils.Managed.getProperty(this, "person", currentValue);
        if (currentValue !== managedValue)
            this._991716523person = managedValue;
        return managedValue;
    }
    public function set person(value:Person):void
    {
    	var oldValue:Person = this._991716523person;
    	this._991716523person = value;
		mx.data.utils.Managed.setProperty(this, "person", oldValue, _991716523person);
    }

	//	public getter/setter 'fax' moved to '_101149fax'

    [Bindable(event="propertyChange")]
    public function get fax():String
    {
        var currentValue:String = this._101149fax;
        var managedValue:String = mx.data.utils.Managed.getProperty(this, "fax", currentValue);
        if (currentValue !== managedValue)
            this._101149fax = managedValue;
        return managedValue;
    }
    public function set fax(value:String):void
    {
    	var oldValue:String = this._101149fax;
    	this._101149fax = value;
		mx.data.utils.Managed.setProperty(this, "fax", oldValue, _101149fax);
    }

	//	public getter/setter 'email' moved to '_96619420email'

    [Bindable(event="propertyChange")]
    public function get email():String
    {
        var currentValue:String = this._96619420email;
        var managedValue:String = mx.data.utils.Managed.getProperty(this, "email", currentValue);
        if (currentValue !== managedValue)
            this._96619420email = managedValue;
        return managedValue;
    }
    public function set email(value:String):void
    {
    	var oldValue:String = this._96619420email;
    	this._96619420email = value;
		mx.data.utils.Managed.setProperty(this, "email", oldValue, _96619420email);
    }

	//	public getter/setter 'address' moved to '_1147692044address'

    [Bindable(event="propertyChange")]
    public function get address():Address
    {
        var currentValue:Address = this._1147692044address;
        var managedValue:Address = mx.data.utils.Managed.getProperty(this, "address", currentValue);
        if (currentValue !== managedValue)
            this._1147692044address = managedValue;
        return managedValue;
    }
    public function set address(value:Address):void
    {
    	var oldValue:Address = this._1147692044address;
    	this._1147692044address = value;
		mx.data.utils.Managed.setProperty(this, "address", oldValue, _1147692044address);
    }

	//	public getter/setter 'mobile' moved to '_1068855134mobile'

    [Bindable(event="propertyChange")]
    public function get mobile():String
    {
        var currentValue:String = this._1068855134mobile;
        var managedValue:String = mx.data.utils.Managed.getProperty(this, "mobile", currentValue);
        if (currentValue !== managedValue)
            this._1068855134mobile = managedValue;
        return managedValue;
    }
    public function set mobile(value:String):void
    {
    	var oldValue:String = this._1068855134mobile;
    	this._1068855134mobile = value;
		mx.data.utils.Managed.setProperty(this, "mobile", oldValue, _1068855134mobile);
    }


    
}
