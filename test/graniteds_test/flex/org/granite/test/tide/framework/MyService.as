package org.granite.test.tide.framework {

    import flash.utils.flash_proxy;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Component;
    import org.granite.tide.ITideResponder;
    
    use namespace flash_proxy;

    public class MyService extends Component {

        public function test(resultHandler:Object = null, faultHandler:Function = null):void {
            if (faultHandler != null)
                callProperty("test", resultHandler, faultHandler);
            else if (resultHandler is Function || resultHandler is ITideResponder)
                callProperty("test", resultHandler);
            else if (resultHandler == null)
                callProperty("test");
            else
                throw new Error("Illegal argument to remote call (last argument should be Function or ITideResponder): " + resultHandler);
        }
    }
}
