package org.jboss.seam.example.booking.control {

    import mx.logging.Log;
    import mx.logging.targets.TraceTarget;
    
    import org.granite.tide.ITideModule;
    import org.granite.tide.Tide;
    import org.granite.tide.validators.ValidatorExceptionHandler;
    
    
    [Bindable]
    public class HotelBookingModule implements ITideModule {
        
        public function init(tide:Tide):void {
            var t:TraceTarget = new TraceTarget();
            t.filters = ["org.granite.*"];
            Log.addTarget(t);
            
            tide.addExceptionHandler(ValidatorExceptionHandler);
            tide.addExceptionHandler(NotLoggedInExceptionHandler);
            
            // Initialize Tide client components
            tide.addComponents([UserCtl, HotelSearchCtl, HotelBookingCtl, BookingListCtl]); 
        }
    }
}
