package org.jboss.seam.example.booking.control {

    import mx.collections.ArrayCollection;
    import mx.controls.Alert;

    import org.granite.tide.events.TideContextEvent;
    import org.granite.tide.events.TideResultEvent;

    import org.jboss.seam.example.booking.Booking;

    [Bindable]
    [Name("bookingsCtl", restrict="true")]
    public class BookingListCtl {
        
        [In]
        public var bookingList:Object;
        
        [In]
        public var bookings:ArrayCollection = new ArrayCollection();


        [Observer("org.granite.tide.login")]
        public function loggedInHandler():void {
            bookingList.getBookings();
        }


        [Observer("cancelBooking")]
        public function cancelBooking(booking:Booking):void {
            bookingList.booking = booking;
            bookingList.cancel();
        }
    }
}
