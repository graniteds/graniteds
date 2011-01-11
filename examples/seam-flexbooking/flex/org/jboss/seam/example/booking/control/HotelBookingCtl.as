package org.jboss.seam.example.booking.control {

    import org.granite.tide.events.TideContextEvent;
    import org.granite.tide.events.TideResultEvent;
	import org.granite.tide.seam.In;
	import org.granite.tide.seam.framework.Conversation;

    import org.jboss.seam.example.booking.Booking;
    import org.jboss.seam.example.booking.Hotel;

    [Bindable]
    [Name("hotelBookingCtl", scope="conversation", restrict="true")]
    public class HotelBookingCtl {

		public static const INIT:String = "INIT";
		public static const VIEW:String = "VIEW";
		public static const BOOK:String = "BOOK";
		public static const CONFIRM:String = "CONFIRM";
		public static const REVISE:String = "REVISE";

		public var state:String = INIT;

        [In]
        public var conversation:Conversation;

        [In]
        public var hotelBooking:Object;

        [In]
        public var hotel:Hotel;
	
        [In]
        public var booking:Booking;

        [Observer("selectHotel")]
        public function selectHotel(hotel:Hotel):void {
        	conversation.description = "View Hotel: " + hotel.name;
			hotelBooking.selectHotel(hotel, selectHotelResult);
        }

        private function selectHotelResult(event:TideResultEvent):void {
        	state = VIEW;
			event.context.raiseEvent("showHotelView");
        }

        [Observer("bookHotel")]
        public function bookHotel():void {
        	conversation.description = "Book Hotel: " + hotel.name;
			hotelBooking.bookHotel(bookHotelResult);
        }

        private function bookHotelResult(event:TideResultEvent):void {
        	booking.checkinDate = new Date();
        	booking.checkoutDate = new Date();
        	booking.checkoutDate.date++;
        	state = BOOK;
			event.context.raiseEvent("showBookView");
			event.context.raiseEvent("resetBookView");
        }

        [Observer("cancelHotel")]
        public function cancelHotel(event:TideContextEvent):void {
			hotelBooking.cancel(cancelHotelResult);
        }
        
        private function cancelHotelResult(event:TideResultEvent):void {
        	state = INIT;
			event.context.raiseEvent("showSearchView");
        }

        [Observer("checkBook")]
        public function checkBook():void {
			In(hotelBooking.bookingValid)
			hotelBooking.setBookingDetails(checkBookResult);
        }
        
        private function checkBookResult(event:TideResultEvent):void {
        	if (hotelBooking.bookingValid && event.context.mainBookView.validateForm(event.context.mainBookView.bookForm)) {
        		state = CONFIRM;
				event.context.raiseEvent("showConfirmState");
        	}
        }

        [Observer("reviseBook")]
        public function reviseBook(event:TideContextEvent):void {
        	state = REVISE;
			event.context.raiseEvent("showReviseState");
        }

        [Observer("confirmBook")]
        public function confirmBook():void {
			hotelBooking.confirm(confirmBookResult);
        }

        private function confirmBookResult(event:TideResultEvent):void {
        	state = INIT;
			event.context.raiseEvent("resetBookView");
			event.context.raiseEvent("showSearchView", false);
        }
        
        public function isBooking():Boolean {
        	return state == BOOK || state == CONFIRM || state == REVISE;
        }
        
        public function isBookingConfirm():Boolean {
        	return state == CONFIRM;
        }
    }
}
