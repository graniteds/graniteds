
package 
{
import Login;
import flash.accessibility.*;
import flash.debugger.*;
import flash.display.*;
import flash.errors.*;
import flash.events.*;
import flash.external.*;
import flash.filters.*;
import flash.geom.*;
import flash.media.*;
import flash.net.*;
import flash.printing.*;
import flash.profiler.*;
import flash.system.*;
import flash.text.*;
import flash.ui.*;
import flash.utils.*;
import flash.xml.*;
import mx.binding.*;
import mx.containers.ApplicationControlBar;
import mx.containers.Form;
import mx.containers.Panel;
import mx.containers.VBox;
import mx.containers.ViewStack;
import mx.controls.Button;
import mx.controls.TextInput;
import mx.core.Application;
import mx.core.ClassFactory;
import mx.core.DeferredInstanceFromClass;
import mx.core.DeferredInstanceFromFunction;
import mx.core.IDeferredInstance;
import mx.core.IFactory;
import mx.core.IPropertyChangeNotifier;
import mx.core.mx_internal;
import mx.styles.*;
import test.granite.components.DataGrid;
import test.granite.components.EntityForm;
import test.granite.components.DataGridColumn;
import mx.containers.HBox;
import mx.controls.Label;
import mx.states.AddChild;
import test.granite.components.FormInput;
import mx.controls.Spacer;
import mx.containers.ControlBar;
import mx.states.SetProperty;
import mx.containers.VBox;
import mx.controls.Button;
import test.granite.components.columns;
import mx.containers.FormItem;
import mx.core.Application;
import mx.states.State;

public class Persons extends mx.core.Application
{
	public function Persons() {}

	[Bindable]
	public var appView : mx.containers.ViewStack;
	[Bindable]
	public var loginView : Login;
	[Bindable]
	public var loggedInView : mx.containers.VBox;
	[Bindable]
	public var acb : mx.containers.ApplicationControlBar;
	[Bindable]
	public var fSearch : mx.controls.TextInput;
	[Bindable]
	public var vbPersons : mx.containers.VBox;
	[Bindable]
	public var pPersons : mx.containers.Panel;
	[Bindable]
	public var persons : test.granite.components.DataGrid;
	[Bindable]
	public var vbContacts : mx.containers.VBox;
	[Bindable]
	public var pContacts : mx.containers.Panel;
	[Bindable]
	public var contacts : test.granite.components.DataGrid;
	[Bindable]
	public var pNewPerson : mx.containers.Panel;
	[Bindable]
	public var fCreatePerson : test.granite.components.EntityForm;
	[Bindable]
	public var saveNewPerson : mx.controls.Button;
	[Bindable]
	public var cancelPerson : mx.controls.Button;
	[Bindable]
	public var pEditPerson : mx.containers.Panel;
	[Bindable]
	public var fEditPerson : test.granite.components.EntityForm;
	[Bindable]
	public var savePerson : mx.controls.Button;
	[Bindable]
	public var pNewContact : mx.containers.Panel;
	[Bindable]
	public var fNewContact : test.granite.components.EntityForm;
	[Bindable]
	public var fNewContact1 : mx.containers.Form;
	[Bindable]
	public var fNewContact2 : mx.containers.Form;
	[Bindable]
	public var savContact : mx.controls.Button;
	[Bindable]
	public var pEditContact : mx.containers.Panel;
	[Bindable]
	public var fEditContact : test.granite.components.EntityForm;
	[Bindable]
	public var fEditContact1 : mx.containers.Form;
	[Bindable]
	public var fEditContact2 : mx.containers.Form;
	[Bindable]
	public var modContact : mx.controls.Button;

	mx_internal var _bindings : Array;
	mx_internal var _watchers : Array;
	mx_internal var _bindingsByDestination : Object;
	mx_internal var _bindingsBeginWithWord : Object;

include "C:/workspace/graniteds/examples/graniteds_tide_ejb3/build/swf/Persons.mxml:33,89";

}}
