

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IEventDispatcher;

import mx.core.mx_internal;
import mx.data.IManaged;
import mx.data.utils.Managed;
import mx.events.PropertyChangeEvent;
import mx.utils.UIDUtil;

import test.granite.ejb3.entity.*;
import mx.collections.ListCollectionView;

class ManagedProperty
{
	//	wrapped properties
	//

	//	public getter/setter 'mainContact' moved to '_66487335mainContact'

    [Bindable(event="propertyChange")]
    public function get mainContact():Contact
    {
        var currentValue:Contact = this._66487335mainContact;
        var managedValue:Contact = mx.data.utils.Managed.getProperty(this, "mainContact", currentValue);
        if (currentValue !== managedValue)
            this._66487335mainContact = managedValue;
        return managedValue;
    }
    public function set mainContact(value:Contact):void
    {
    	var oldValue:Contact = this._66487335mainContact;
    	this._66487335mainContact = value;
		mx.data.utils.Managed.setProperty(this, "mainContact", oldValue, _66487335mainContact);
    }

	//	public getter/setter 'lastName' moved to '_1459599807lastName'

    [Bindable(event="propertyChange")]
    public function get lastName():String
    {
        var currentValue:String = this._1459599807lastName;
        var managedValue:String = mx.data.utils.Managed.getProperty(this, "lastName", currentValue);
        if (currentValue !== managedValue)
            this._1459599807lastName = managedValue;
        return managedValue;
    }
    public function set lastName(value:String):void
    {
    	var oldValue:String = this._1459599807lastName;
    	this._1459599807lastName = value;
		mx.data.utils.Managed.setProperty(this, "lastName", oldValue, _1459599807lastName);
    }

	//	public getter/setter 'salutation' moved to '_1230813672salutation'

    [Bindable(event="propertyChange")]
    public function get salutation():Person$Salutation
    {
        var currentValue:Person$Salutation = this._1230813672salutation;
        var managedValue:Person$Salutation = mx.data.utils.Managed.getProperty(this, "salutation", currentValue);
        if (currentValue !== managedValue)
            this._1230813672salutation = managedValue;
        return managedValue;
    }
    public function set salutation(value:Person$Salutation):void
    {
    	var oldValue:Person$Salutation = this._1230813672salutation;
    	this._1230813672salutation = value;
		mx.data.utils.Managed.setProperty(this, "salutation", oldValue, _1230813672salutation);
    }

	//	public getter/setter 'firstName' moved to '_132835675firstName'

    [Bindable(event="propertyChange")]
    public function get firstName():String
    {
        var currentValue:String = this._132835675firstName;
        var managedValue:String = mx.data.utils.Managed.getProperty(this, "firstName", currentValue);
        if (currentValue !== managedValue)
            this._132835675firstName = managedValue;
        return managedValue;
    }
    public function set firstName(value:String):void
    {
    	var oldValue:String = this._132835675firstName;
    	this._132835675firstName = value;
		mx.data.utils.Managed.setProperty(this, "firstName", oldValue, _132835675firstName);
    }

	//	public getter/setter 'contacts' moved to '_567451565contacts'

    [Bindable(event="propertyChange")]
    public function get contacts():ListCollectionView
    {
        var currentValue:ListCollectionView = this._567451565contacts;
        var managedValue:ListCollectionView = mx.data.utils.Managed.getProperty(this, "contacts", currentValue);
        if (currentValue !== managedValue)
            this._567451565contacts = managedValue;
        return managedValue;
    }
    public function set contacts(value:ListCollectionView):void
    {
    	var oldValue:ListCollectionView = this._567451565contacts;
    	this._567451565contacts = value;
		mx.data.utils.Managed.setProperty(this, "contacts", oldValue, _567451565contacts);
    }


    
}
