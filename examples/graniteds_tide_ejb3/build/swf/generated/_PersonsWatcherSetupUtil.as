






package
{
import flash.display.Sprite;
import mx.core.IFlexModuleFactory;
import mx.binding.ArrayElementWatcher;
import mx.binding.FunctionReturnWatcher;
import mx.binding.IWatcherSetupUtil;
import mx.binding.PropertyWatcher;
import mx.binding.RepeaterComponentWatcher;
import mx.binding.RepeaterItemWatcher;
import mx.binding.StaticPropertyWatcher;
import mx.binding.XMLWatcher;
import mx.binding.Watcher;

[ExcludeClass]
[Mixin]
public class _PersonsWatcherSetupUtil extends Sprite
    implements mx.binding.IWatcherSetupUtil
{
    public function _PersonsWatcherSetupUtil()
    {
        super();
    }

    public static function init(fbs:IFlexModuleFactory):void
    {
        import Persons;
        (Persons).watcherSetupUtil = new _PersonsWatcherSetupUtil();
    }

    public function setup(target:Object,
                          propertyGetter:Function,
                          bindings:Array,
                          watchers:Array):void
    {
        import mx.containers.ApplicationControlBar;
        import mx.containers.Panel;
        import test.granite.components.EntityForm;
        import org.granite.tide.events.TideUIEvent;
        import mx.collections.ArrayCollection;
        import mx.utils.ObjectProxy;
        import mx.events.CloseEvent;
        import flash.events.MouseEvent;
        import test.granite.components.FormInput;
        import mx.controls.Spacer;
        import mx.containers.ControlBar;
        import test.granite.components.DataGrid;
        import mx.core.ClassFactory;
        import mx.core.IFactory;
        import mx.controls.Button;
        import mx.utils.UIDUtil;
        import flash.events.EventDispatcher;
        import mx.core.Application;
        import mx.states.State;
        import mx.containers.HBox;
        import mx.containers.Form;
        import mx.states.AddChild;
        import mx.core.IDeferredInstance;
        import mx.core.IPropertyChangeNotifier;
        import flash.events.IEventDispatcher;
        import org.granite.tide.data.events.TideDataConflictsEvent;
        import mx.events.PropertyChangeEvent;
        import mx.events.FlexEvent;
        import mx.collections.ListCollectionView;
        import mx.events.ListEvent;
        import flash.events.Event;
        import mx.core.UIComponentDescriptor;
        import test.granite.components.DataGridColumn;
        import Login;
        import mx.core.DeferredInstanceFromClass;
        import org.granite.tide.data.Conflicts;
        import mx.binding.IBindingClient;
        import org.granite.tide.service.DefaultServiceInitializer;
        import mx.containers.VBox;
        import mx.core.DeferredInstanceFromFunction;
        import test.granite.ejb3.entity.Contact;
        import mx.containers.FormItem;
        import mx.controls.Alert;
        import org.granite.tide.ejb.Context;
        import mx.binding.BindingManager;
        import org.granite.tide.ejb.Identity;
        import mx.events.ValidationResultEvent;
        import mx.controls.Label;
        import test.granite.AddressBookModule;
        import mx.states.SetProperty;
        import mx.core.mx_internal;
        import test.granite.ejb3.entity.Person;
        import mx.controls.TextInput;
        import mx.containers.ViewStack;
        import mx.core.UIComponent;
        import org.granite.tide.ejb.Ejb;

        // writeWatcher id=14 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[14] = new mx.binding.PropertyWatcher("pContacts",
            {
                propertyChange: true
            }
,         // writeWatcherListeners id=14 size=4
        [
        bindings[18],
        bindings[22],
        bindings[26],
        bindings[14]
        ]
,
                                                                 propertyGetter
);

        // writeWatcher id=0 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[0] = new mx.binding.PropertyWatcher("identity",
            {
                propertyChange: true
            }
,         // writeWatcherListeners id=0 size=4
        [
        bindings[0],
        bindings[3],
        bindings[5],
        bindings[7]
        ]
,
                                                                 propertyGetter
);

        // writeWatcher id=5 shouldWriteSelf=true class=flex2.compiler.as3.binding.FunctionReturnWatcher shouldWriteChildren=true
        watchers[5] = new mx.binding.FunctionReturnWatcher("hasRole",
                                                                     target,
                                                                     function():Array { return [ "admin" ]; },
            {
                roleChanged: true,
                propertyChange: true
            }
,
                                                                     [bindings[3]],
                                                                     null
);

        // writeWatcher id=6 shouldWriteSelf=true class=flex2.compiler.as3.binding.FunctionReturnWatcher shouldWriteChildren=true
        watchers[6] = new mx.binding.FunctionReturnWatcher("hasRole",
                                                                     target,
                                                                     function():Array { return [ "admin" ]; },
            {
                roleChanged: true,
                propertyChange: true
            }
,
                                                                     [bindings[5]],
                                                                     null
);

        // writeWatcher id=9 shouldWriteSelf=true class=flex2.compiler.as3.binding.FunctionReturnWatcher shouldWriteChildren=true
        watchers[9] = new mx.binding.FunctionReturnWatcher("hasRole",
                                                                     target,
                                                                     function():Array { return [ "admin" ]; },
            {
                roleChanged: true,
                propertyChange: true
            }
,
                                                                     [bindings[7]],
                                                                     null
);

        // writeWatcher id=1 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[1] = new mx.binding.PropertyWatcher("loggedIn",
            {
                propertyChange: true
            }
,         // writeWatcherListeners id=1 size=1
        [
        bindings[0]
        ]
,
                                                                 null
);

        // writeWatcher id=12 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[12] = new mx.binding.PropertyWatcher("vbPersons",
            {
                propertyChange: true
            }
,         // writeWatcherListeners id=12 size=2
        [
        bindings[11],
        bindings[15]
        ]
,
                                                                 propertyGetter
);

        // writeWatcher id=7 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[7] = new mx.binding.PropertyWatcher("person",
            {
                propertyChange: true
            }
,         // writeWatcherListeners id=7 size=3
        [
        bindings[16],
        bindings[6],
        bindings[12]
        ]
,
                                                                 propertyGetter
);

        // writeWatcher id=8 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[8] = new mx.binding.PropertyWatcher("contacts",
            {
                propertyChange: true
            }
,         // writeWatcherListeners id=8 size=1
        [
        bindings[6]
        ]
,
                                                                 null
);

        // writeWatcher id=2 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[2] = new mx.binding.PropertyWatcher("peopleService",
            {
                propertyChange: true
            }
,         // writeWatcherListeners id=2 size=1
        [
        bindings[1]
        ]
,
                                                                 propertyGetter
);

        // writeWatcher id=13 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[13] = new mx.binding.PropertyWatcher("pPersons",
            {
                propertyChange: true
            }
,         // writeWatcherListeners id=13 size=4
        [
        bindings[17],
        bindings[21],
        bindings[25],
        bindings[13]
        ]
,
                                                                 propertyGetter
);

        // writeWatcher id=15 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[15] = new mx.binding.PropertyWatcher("vbContacts",
            {
                propertyChange: true
            }
,         // writeWatcherListeners id=15 size=2
        [
        bindings[19],
        bindings[23]
        ]
,
                                                                 propertyGetter
);

        // writeWatcher id=16 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[16] = new mx.binding.PropertyWatcher("contact",
            {
                propertyChange: true
            }
,         // writeWatcherListeners id=16 size=2
        [
        bindings[20],
        bindings[24]
        ]
,
                                                                 propertyGetter
);

        // writeWatcher id=3 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[3] = new mx.binding.PropertyWatcher("persons",
            {
                propertyChange: true
            }
,         // writeWatcherListeners id=3 size=3
        [
        bindings[2],
        bindings[4],
        bindings[8]
        ]
,
                                                                 propertyGetter
);

        // writeWatcher id=4 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[4] = new mx.binding.PropertyWatcher("selectedItem",
            {
                valueCommit: true,
                change: true
            }
,         // writeWatcherListeners id=4 size=3
        [
        bindings[2],
        bindings[4],
        bindings[8]
        ]
,
                                                                 null
);

        // writeWatcher id=10 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[10] = new mx.binding.PropertyWatcher("contacts",
            {
                propertyChange: true
            }
,         // writeWatcherListeners id=10 size=2
        [
        bindings[9],
        bindings[10]
        ]
,
                                                                 propertyGetter
);

        // writeWatcher id=11 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[11] = new mx.binding.PropertyWatcher("selectedItem",
            {
                valueCommit: true,
                change: true
            }
,         // writeWatcherListeners id=11 size=2
        [
        bindings[9],
        bindings[10]
        ]
,
                                                                 null
);


        // writeWatcherBottom id=14 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[14].updateParent(target);

 





        // writeWatcherBottom id=0 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[0].updateParent(target);

 





        // writeWatcherBottom id=5 shouldWriteSelf=true class=flex2.compiler.as3.binding.FunctionReturnWatcher
        // writeEvaluationWatcherPart 5 0 parentWatcher
        watchers[5].parentWatcher = watchers[0];
        watchers[0].addChild(watchers[5]);

 





        // writeWatcherBottom id=6 shouldWriteSelf=true class=flex2.compiler.as3.binding.FunctionReturnWatcher
        // writeEvaluationWatcherPart 6 0 parentWatcher
        watchers[6].parentWatcher = watchers[0];
        watchers[0].addChild(watchers[6]);

 





        // writeWatcherBottom id=9 shouldWriteSelf=true class=flex2.compiler.as3.binding.FunctionReturnWatcher
        // writeEvaluationWatcherPart 9 0 parentWatcher
        watchers[9].parentWatcher = watchers[0];
        watchers[0].addChild(watchers[9]);

 





        // writeWatcherBottom id=1 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[0].addChild(watchers[1]);

 





        // writeWatcherBottom id=12 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[12].updateParent(target);

 





        // writeWatcherBottom id=7 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[7].updateParent(target);

 





        // writeWatcherBottom id=8 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[7].addChild(watchers[8]);

 





        // writeWatcherBottom id=2 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[2].updateParent(target);

 





        // writeWatcherBottom id=13 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[13].updateParent(target);

 





        // writeWatcherBottom id=15 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[15].updateParent(target);

 





        // writeWatcherBottom id=16 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[16].updateParent(target);

 





        // writeWatcherBottom id=3 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[3].updateParent(target);

 





        // writeWatcherBottom id=4 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[3].addChild(watchers[4]);

 





        // writeWatcherBottom id=10 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[10].updateParent(target);

 





        // writeWatcherBottom id=11 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[10].addChild(watchers[11]);

 





    }
}

}
