

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IEventDispatcher;
import mx.core.IPropertyChangeNotifier;
import mx.events.PropertyChangeEvent;
import mx.utils.ObjectProxy;
import mx.utils.UIDUtil;

import mx.containers.ApplicationControlBar;
import Login;
import mx.containers.Panel;
import test.granite.components.EntityForm;
import org.granite.tide.ejb.Identity;
import mx.containers.Form;
import test.granite.components.DataGrid;
import mx.controls.Button;
import mx.containers.VBox;
import mx.controls.TextInput;
import test.granite.ejb3.entity.Person;
import test.granite.ejb3.entity.Contact;
import mx.containers.ViewStack;
import mx.collections.ListCollectionView;

class BindableProperty
{
	/**
	 * generated bindable wrapper for property acb (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'acb' moved to '_96384acb'
	 */

    [Bindable(event="propertyChange")]
    public function get acb():mx.containers.ApplicationControlBar
    {
        return this._96384acb;
    }

    public function set acb(value:mx.containers.ApplicationControlBar):void
    {
    	var oldValue:Object = this._96384acb;
        if (oldValue !== value)
        {
            this._96384acb = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "acb", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property appView (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'appView' moved to '_793890714appView'
	 */

    [Bindable(event="propertyChange")]
    public function get appView():mx.containers.ViewStack
    {
        return this._793890714appView;
    }

    public function set appView(value:mx.containers.ViewStack):void
    {
    	var oldValue:Object = this._793890714appView;
        if (oldValue !== value)
        {
            this._793890714appView = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "appView", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property cancelPerson (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'cancelPerson' moved to '_1918864785cancelPerson'
	 */

    [Bindable(event="propertyChange")]
    public function get cancelPerson():mx.controls.Button
    {
        return this._1918864785cancelPerson;
    }

    public function set cancelPerson(value:mx.controls.Button):void
    {
    	var oldValue:Object = this._1918864785cancelPerson;
        if (oldValue !== value)
        {
            this._1918864785cancelPerson = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "cancelPerson", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property contacts (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'contacts' moved to '_567451565contacts'
	 */

    [Bindable(event="propertyChange")]
    public function get contacts():test.granite.components.DataGrid
    {
        return this._567451565contacts;
    }

    public function set contacts(value:test.granite.components.DataGrid):void
    {
    	var oldValue:Object = this._567451565contacts;
        if (oldValue !== value)
        {
            this._567451565contacts = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "contacts", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property fCreatePerson (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'fCreatePerson' moved to '_1738041079fCreatePerson'
	 */

    [Bindable(event="propertyChange")]
    public function get fCreatePerson():test.granite.components.EntityForm
    {
        return this._1738041079fCreatePerson;
    }

    public function set fCreatePerson(value:test.granite.components.EntityForm):void
    {
    	var oldValue:Object = this._1738041079fCreatePerson;
        if (oldValue !== value)
        {
            this._1738041079fCreatePerson = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "fCreatePerson", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property fEditContact (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'fEditContact' moved to '_489020016fEditContact'
	 */

    [Bindable(event="propertyChange")]
    public function get fEditContact():test.granite.components.EntityForm
    {
        return this._489020016fEditContact;
    }

    public function set fEditContact(value:test.granite.components.EntityForm):void
    {
    	var oldValue:Object = this._489020016fEditContact;
        if (oldValue !== value)
        {
            this._489020016fEditContact = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "fEditContact", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property fEditContact1 (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'fEditContact1' moved to '_2020248639fEditContact1'
	 */

    [Bindable(event="propertyChange")]
    public function get fEditContact1():mx.containers.Form
    {
        return this._2020248639fEditContact1;
    }

    public function set fEditContact1(value:mx.containers.Form):void
    {
    	var oldValue:Object = this._2020248639fEditContact1;
        if (oldValue !== value)
        {
            this._2020248639fEditContact1 = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "fEditContact1", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property fEditContact2 (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'fEditContact2' moved to '_2020248638fEditContact2'
	 */

    [Bindable(event="propertyChange")]
    public function get fEditContact2():mx.containers.Form
    {
        return this._2020248638fEditContact2;
    }

    public function set fEditContact2(value:mx.containers.Form):void
    {
    	var oldValue:Object = this._2020248638fEditContact2;
        if (oldValue !== value)
        {
            this._2020248638fEditContact2 = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "fEditContact2", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property fEditPerson (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'fEditPerson' moved to '_933026565fEditPerson'
	 */

    [Bindable(event="propertyChange")]
    public function get fEditPerson():test.granite.components.EntityForm
    {
        return this._933026565fEditPerson;
    }

    public function set fEditPerson(value:test.granite.components.EntityForm):void
    {
    	var oldValue:Object = this._933026565fEditPerson;
        if (oldValue !== value)
        {
            this._933026565fEditPerson = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "fEditPerson", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property fNewContact (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'fNewContact' moved to '_639308646fNewContact'
	 */

    [Bindable(event="propertyChange")]
    public function get fNewContact():test.granite.components.EntityForm
    {
        return this._639308646fNewContact;
    }

    public function set fNewContact(value:test.granite.components.EntityForm):void
    {
    	var oldValue:Object = this._639308646fNewContact;
        if (oldValue !== value)
        {
            this._639308646fNewContact = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "fNewContact", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property fNewContact1 (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'fNewContact1' moved to '_1656268405fNewContact1'
	 */

    [Bindable(event="propertyChange")]
    public function get fNewContact1():mx.containers.Form
    {
        return this._1656268405fNewContact1;
    }

    public function set fNewContact1(value:mx.containers.Form):void
    {
    	var oldValue:Object = this._1656268405fNewContact1;
        if (oldValue !== value)
        {
            this._1656268405fNewContact1 = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "fNewContact1", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property fNewContact2 (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'fNewContact2' moved to '_1656268404fNewContact2'
	 */

    [Bindable(event="propertyChange")]
    public function get fNewContact2():mx.containers.Form
    {
        return this._1656268404fNewContact2;
    }

    public function set fNewContact2(value:mx.containers.Form):void
    {
    	var oldValue:Object = this._1656268404fNewContact2;
        if (oldValue !== value)
        {
            this._1656268404fNewContact2 = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "fNewContact2", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property fSearch (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'fSearch' moved to '_1491407442fSearch'
	 */

    [Bindable(event="propertyChange")]
    public function get fSearch():mx.controls.TextInput
    {
        return this._1491407442fSearch;
    }

    public function set fSearch(value:mx.controls.TextInput):void
    {
    	var oldValue:Object = this._1491407442fSearch;
        if (oldValue !== value)
        {
            this._1491407442fSearch = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "fSearch", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property loggedInView (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'loggedInView' moved to '_406922868loggedInView'
	 */

    [Bindable(event="propertyChange")]
    public function get loggedInView():mx.containers.VBox
    {
        return this._406922868loggedInView;
    }

    public function set loggedInView(value:mx.containers.VBox):void
    {
    	var oldValue:Object = this._406922868loggedInView;
        if (oldValue !== value)
        {
            this._406922868loggedInView = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "loggedInView", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property loginView (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'loginView' moved to '_1719221842loginView'
	 */

    [Bindable(event="propertyChange")]
    public function get loginView():Login
    {
        return this._1719221842loginView;
    }

    public function set loginView(value:Login):void
    {
    	var oldValue:Object = this._1719221842loginView;
        if (oldValue !== value)
        {
            this._1719221842loginView = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "loginView", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property modContact (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'modContact' moved to '_2094436030modContact'
	 */

    [Bindable(event="propertyChange")]
    public function get modContact():mx.controls.Button
    {
        return this._2094436030modContact;
    }

    public function set modContact(value:mx.controls.Button):void
    {
    	var oldValue:Object = this._2094436030modContact;
        if (oldValue !== value)
        {
            this._2094436030modContact = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "modContact", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property pContacts (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'pContacts' moved to '_1074244381pContacts'
	 */

    [Bindable(event="propertyChange")]
    public function get pContacts():mx.containers.Panel
    {
        return this._1074244381pContacts;
    }

    public function set pContacts(value:mx.containers.Panel):void
    {
    	var oldValue:Object = this._1074244381pContacts;
        if (oldValue !== value)
        {
            this._1074244381pContacts = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "pContacts", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property pEditContact (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'pEditContact' moved to '_1779847206pEditContact'
	 */

    [Bindable(event="propertyChange")]
    public function get pEditContact():mx.containers.Panel
    {
        return this._1779847206pEditContact;
    }

    public function set pEditContact(value:mx.containers.Panel):void
    {
    	var oldValue:Object = this._1779847206pEditContact;
        if (oldValue !== value)
        {
            this._1779847206pEditContact = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "pEditContact", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property pEditPerson (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'pEditPerson' moved to '_143382159pEditPerson'
	 */

    [Bindable(event="propertyChange")]
    public function get pEditPerson():mx.containers.Panel
    {
        return this._143382159pEditPerson;
    }

    public function set pEditPerson(value:mx.containers.Panel):void
    {
    	var oldValue:Object = this._143382159pEditPerson;
        if (oldValue !== value)
        {
            this._143382159pEditPerson = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "pEditPerson", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property pNewContact (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'pNewContact' moved to '_150335760pNewContact'
	 */

    [Bindable(event="propertyChange")]
    public function get pNewContact():mx.containers.Panel
    {
        return this._150335760pNewContact;
    }

    public function set pNewContact(value:mx.containers.Panel):void
    {
    	var oldValue:Object = this._150335760pNewContact;
        if (oldValue !== value)
        {
            this._150335760pNewContact = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "pNewContact", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property pNewPerson (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'pNewPerson' moved to '_773854853pNewPerson'
	 */

    [Bindable(event="propertyChange")]
    public function get pNewPerson():mx.containers.Panel
    {
        return this._773854853pNewPerson;
    }

    public function set pNewPerson(value:mx.containers.Panel):void
    {
    	var oldValue:Object = this._773854853pNewPerson;
        if (oldValue !== value)
        {
            this._773854853pNewPerson = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "pNewPerson", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property pPersons (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'pPersons' moved to '_1387525842pPersons'
	 */

    [Bindable(event="propertyChange")]
    public function get pPersons():mx.containers.Panel
    {
        return this._1387525842pPersons;
    }

    public function set pPersons(value:mx.containers.Panel):void
    {
    	var oldValue:Object = this._1387525842pPersons;
        if (oldValue !== value)
        {
            this._1387525842pPersons = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "pPersons", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property persons (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'persons' moved to '_678441026persons'
	 */

    [Bindable(event="propertyChange")]
    public function get persons():test.granite.components.DataGrid
    {
        return this._678441026persons;
    }

    public function set persons(value:test.granite.components.DataGrid):void
    {
    	var oldValue:Object = this._678441026persons;
        if (oldValue !== value)
        {
            this._678441026persons = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "persons", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property savContact (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'savContact' moved to '_1755728664savContact'
	 */

    [Bindable(event="propertyChange")]
    public function get savContact():mx.controls.Button
    {
        return this._1755728664savContact;
    }

    public function set savContact(value:mx.controls.Button):void
    {
    	var oldValue:Object = this._1755728664savContact;
        if (oldValue !== value)
        {
            this._1755728664savContact = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "savContact", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property saveNewPerson (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'saveNewPerson' moved to '_1603154968saveNewPerson'
	 */

    [Bindable(event="propertyChange")]
    public function get saveNewPerson():mx.controls.Button
    {
        return this._1603154968saveNewPerson;
    }

    public function set saveNewPerson(value:mx.controls.Button):void
    {
    	var oldValue:Object = this._1603154968saveNewPerson;
        if (oldValue !== value)
        {
            this._1603154968saveNewPerson = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "saveNewPerson", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property savePerson (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'savePerson' moved to '_970225458savePerson'
	 */

    [Bindable(event="propertyChange")]
    public function get savePerson():mx.controls.Button
    {
        return this._970225458savePerson;
    }

    public function set savePerson(value:mx.controls.Button):void
    {
    	var oldValue:Object = this._970225458savePerson;
        if (oldValue !== value)
        {
            this._970225458savePerson = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "savePerson", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property vbContacts (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'vbContacts' moved to '_1041524511vbContacts'
	 */

    [Bindable(event="propertyChange")]
    public function get vbContacts():mx.containers.VBox
    {
        return this._1041524511vbContacts;
    }

    public function set vbContacts(value:mx.containers.VBox):void
    {
    	var oldValue:Object = this._1041524511vbContacts;
        if (oldValue !== value)
        {
            this._1041524511vbContacts = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "vbContacts", oldValue, value));
        }
    }

	/**
	 * generated bindable wrapper for property vbPersons (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'vbPersons' moved to '_1451671410vbPersons'
	 */

    [Bindable(event="propertyChange")]
    public function get vbPersons():mx.containers.VBox
    {
        return this._1451671410vbPersons;
    }

    public function set vbPersons(value:mx.containers.VBox):void
    {
    	var oldValue:Object = this._1451671410vbPersons;
        if (oldValue !== value)
        {
            this._1451671410vbPersons = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "vbPersons", oldValue, value));
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
	 * generated bindable wrapper for property peopleService (public)
	 * - generated setter
	 * - generated getter
	 * - original public var 'peopleService' moved to '_870710566peopleService'
	 */

    [Bindable(event="propertyChange")]
    public function get peopleService():ListCollectionView
    {
        return this._870710566peopleService;
    }

    public function set peopleService(value:ListCollectionView):void
    {
    	var oldValue:Object = this._870710566peopleService;
        if (oldValue !== value)
        {
            this._870710566peopleService = value;
            this.dispatchEvent(mx.events.PropertyChangeEvent.createUpdateEvent(this, "peopleService", oldValue, value));
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



}
