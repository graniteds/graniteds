






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
public class _LoginWatcherSetupUtil extends Sprite
    implements mx.binding.IWatcherSetupUtil
{
    public function _LoginWatcherSetupUtil()
    {
        super();
    }

    public static function init(fbs:IFlexModuleFactory):void
    {
        import Login;
        (Login).watcherSetupUtil = new _LoginWatcherSetupUtil();
    }

    public function setup(target:Object,
                          propertyGetter:Function,
                          bindings:Array,
                          watchers:Array):void
    {
        import mx.core.UIComponentDescriptor;
        import mx.containers.Panel;
        import mx.core.DeferredInstanceFromClass;
        import mx.utils.ObjectProxy;
        import mx.controls.Text;
        import org.granite.tide.events.TideFaultEvent;
        import mx.binding.IBindingClient;
        import flash.events.MouseEvent;
        import mx.core.ClassFactory;
        import mx.core.IFactory;
        import mx.controls.Button;
        import mx.core.DeferredInstanceFromFunction;
        import mx.utils.UIDUtil;
        import mx.containers.FormItem;
        import flash.events.EventDispatcher;
        import mx.binding.BindingManager;
        import org.granite.tide.ejb.Identity;
        import mx.containers.Form;
        import mx.core.IDeferredInstance;
        import mx.core.IPropertyChangeNotifier;
        import flash.events.IEventDispatcher;
        import mx.events.PropertyChangeEvent;
        import mx.core.mx_internal;
        import mx.events.FlexEvent;
        import mx.controls.TextInput;
        import org.granite.tide.events.TideResultEvent;
        import flash.events.Event;

        // writeWatcher id=0 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher shouldWriteChildren=true
        watchers[0] = new mx.binding.PropertyWatcher("message",
            {
                propertyChange: true
            }
,         // writeWatcherListeners id=0 size=1
        [
        bindings[0]
        ]
,
                                                                 propertyGetter
);


        // writeWatcherBottom id=0 shouldWriteSelf=true class=flex2.compiler.as3.binding.PropertyWatcher
        watchers[0].updateParent(target);

 





    }
}

}
