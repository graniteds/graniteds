

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IEventDispatcher;
import mx.core.IPropertyChangeNotifier;
import mx.events.PropertyChangeEvent;
import mx.utils.ObjectProxy;
import mx.utils.UIDUtil;

import flash.filters.*;
import flash.profiler.*;
import org.granite.tide.ejb.Identity;
import flash.external.*;
import flash.display.*;
import flash.net.*;
import flash.debugger.*;
import flash.utils.*;
import flash.printing.*;
import flash.text.*;
import flash.geom.*;
import flash.events.*;
import flash.accessibility.*;
import mx.binding.*;
import mx.controls.TextInput;
import flash.ui.*;
import flash.media.*;
import flash.xml.*;
import mx.styles.*;
import flash.system.*;
import flash.errors.*;

class BindableProperty
{
	/**
	 * generated bindable wrapper for property password (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'password' moved to '_1216985755password'
	 */

    [Bindable(event="propertyChange")]
    public function get password():mx.controls.TextInput
    {
        return this._1216985755password;
    }

    public function set password(value:mx.controls.TextInput):void
    {
    	var oldValue:Object = this._1216985755password;
        if (oldValue !== value)
        {
            this._1216985755password = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "password", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property username (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'username' moved to '_265713450username'
	 */

    [Bindable(event="propertyChange")]
    public function get username():mx.controls.TextInput
    {
        return this._265713450username;
    }

    public function set username(value:mx.controls.TextInput):void
    {
    	var oldValue:Object = this._265713450username;
        if (oldValue !== value)
        {
            this._265713450username = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "username", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property identity (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'identity' moved to '_135761730identity'
	 */

    [Bindable(event="propertyChange")]
    public function get identity():Identity
    {
        return this._135761730identity;
    }

    public function set identity(value:Identity):void
    {
    	var oldValue:Object = this._135761730identity;
        if (oldValue !== value)
        {
            this._135761730identity = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "identity", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property message (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'message' moved to '_954925063message'
	 */

    [Bindable(event="propertyChange")]
    public function get message():String
    {
        return this._954925063message;
    }

    public function set message(value:String):void
    {
    	var oldValue:Object = this._954925063message;
        if (oldValue !== value)
        {
            this._954925063message = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "message", oldValue, value));
        }
    }



}
