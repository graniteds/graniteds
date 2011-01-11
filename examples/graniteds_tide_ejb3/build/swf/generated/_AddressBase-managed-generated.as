

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

	//	public getter/setter 'zipcode' moved to '_281146226zipcode'

    [Bindable(event="propertyChange")]
    public function get zipcode():String
    {
        var currentValue:String = this._281146226zipcode;
        var managedValue:String = mx.data.utils.Managed.getProperty(this, "zipcode", currentValue);
        if (currentValue !== managedValue)
            this._281146226zipcode = managedValue;
        return managedValue;
    }
    public function set zipcode(value:String):void
    {
    	var oldValue:String = this._281146226zipcode;
    	this._281146226zipcode = value;
		mx.data.utils.Managed.setProperty(this, "zipcode", oldValue, _281146226zipcode);
    }

	//	public getter/setter 'address1' moved to '_1218714947address1'

    [Bindable(event="propertyChange")]
    public function get address1():String
    {
        var currentValue:String = this._1218714947address1;
        var managedValue:String = mx.data.utils.Managed.getProperty(this, "address1", currentValue);
        if (currentValue !== managedValue)
            this._1218714947address1 = managedValue;
        return managedValue;
    }
    public function set address1(value:String):void
    {
    	var oldValue:String = this._1218714947address1;
    	this._1218714947address1 = value;
		mx.data.utils.Managed.setProperty(this, "address1", oldValue, _1218714947address1);
    }

	//	public getter/setter 'address2' moved to '_1218714946address2'

    [Bindable(event="propertyChange")]
    public function get address2():String
    {
        var currentValue:String = this._1218714946address2;
        var managedValue:String = mx.data.utils.Managed.getProperty(this, "address2", currentValue);
        if (currentValue !== managedValue)
            this._1218714946address2 = managedValue;
        return managedValue;
    }
    public function set address2(value:String):void
    {
    	var oldValue:String = this._1218714946address2;
    	this._1218714946address2 = value;
		mx.data.utils.Managed.setProperty(this, "address2", oldValue, _1218714946address2);
    }

	//	public getter/setter 'country' moved to '_957831062country'

    [Bindable(event="propertyChange")]
    public function get country():Country
    {
        var currentValue:Country = this._957831062country;
        var managedValue:Country = mx.data.utils.Managed.getProperty(this, "country", currentValue);
        if (currentValue !== managedValue)
            this._957831062country = managedValue;
        return managedValue;
    }
    public function set country(value:Country):void
    {
    	var oldValue:Country = this._957831062country;
    	this._957831062country = value;
		mx.data.utils.Managed.setProperty(this, "country", oldValue, _957831062country);
    }

	//	public getter/setter 'city' moved to '_3053931city'

    [Bindable(event="propertyChange")]
    public function get city():String
    {
        var currentValue:String = this._3053931city;
        var managedValue:String = mx.data.utils.Managed.getProperty(this, "city", currentValue);
        if (currentValue !== managedValue)
            this._3053931city = managedValue;
        return managedValue;
    }
    public function set city(value:String):void
    {
    	var oldValue:String = this._3053931city;
    	this._3053931city = value;
		mx.data.utils.Managed.setProperty(this, "city", oldValue, _3053931city);
    }


    
}
