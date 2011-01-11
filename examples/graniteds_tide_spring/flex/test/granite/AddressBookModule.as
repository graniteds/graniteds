package test.granite
{
    import mx.logging.Log;
    import mx.logging.targets.TraceTarget;
    
    import org.granite.tide.ITideModule;
    import org.granite.tide.Tide;
    import org.granite.tide.data.DataObserver;
    import org.granite.tide.data.OptimisticLockExceptionHandler;
    import org.granite.tide.spring.PagedQuery;
    import org.granite.tide.validators.ValidatorExceptionHandler;
    
    import test.granite.ctl.*;
    import test.granite.spring.entity.Person;
    
    
    [Bindable]
    public class AddressBookModule implements ITideModule {
        
        public function init(tide:Tide):void {
            var t:TraceTarget = new TraceTarget();
            t.filters = ["org.granite.*"];
            Log.addTarget(t);
            
            tide.addExceptionHandler(ValidatorExceptionHandler);
            tide.addExceptionHandler(OptimisticLockExceptionHandler);
            tide.addExceptionHandler(AccessDeniedExceptionHandler);
            
            // Initialize Tide client components
            
            // people component is a client PagedQuery component, linked to the server-side EntityQuery component named people
            // it is marked as autoCreate=true to be initialized as soon as it is referenced somewhere
            // it is marked as RESTRICT_YES so the collection content is cleared on user logout
            tide.addComponentWithFactory("people", PagedQuery, 
            	{ remoteComponentName: "personController",
            	  useController: true,
            	  filterClass: Person,
            	  elementClass: Person,
            	  maxResults: 36 }, 
            	false, true, Tide.RESTRICT_YES);
            // addressBookCtl is a simple client component
            // it is marked as autoCreate=true to be initialized as soon as it is referenced somewhere
            // it is marked as RESTRICT_YES so its properties are automatically cleared on user logout
            tide.addComponent("addressBookCtl", AddressBookCtl, false, true, Tide.RESTRICT_YES);
            // searchCtl component is a client component with 2 static injections
            // references to the persons DataGrid and people are directly injected instead of bound with Flex data binding
            tide.addComponentWithFactory("searchCtl", SearchCtl, {
                persons: "#{application.persons}",
                people: "#{people}"
            }, false, true, Tide.RESTRICT_YES);
            // the client Person component is marked RESTRICT_YES so it is cleared on user logout
            tide.addComponent("person", Person, false, true, Tide.RESTRICT_YES);
            
            // bind a list refresh to user login
            tide.addEventObserver("org.granite.tide.login", "people", "fullRefresh");
            
            // simple DataObserver component: the component name must match the JMS topic name
            // We define event observers so the component subscribed the topic on user login
            tide.addComponent("addressBookTopic", DataObserver);
            tide.addEventObserver("org.granite.tide.login", "addressBookTopic", "subscribe");
            tide.addEventObserver("org.granite.tide.logout", "addressBookTopic", "unsubscribe");
        }
    }
}
