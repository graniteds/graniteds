/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.jboss.seam.example.booking.control {

	import org.granite.tide.seam.In;
    import org.granite.tide.events.TideResultEvent;

    [Bindable]
    [Name("hotelsCtl", restrict="true")]
    public class HotelSearchCtl {
	
        [In]
        public var hotelSearch:Object;

        [Observer("findHotels")]
        public function findHotels(searchString:String, pageSize:Number):void	{
            hotelSearch.searchString = searchString;
            hotelSearch.pageSize = pageSize;
			
			In(hotelSearch.nextPageAvailable)
            hotelSearch.find();
        }

        [Observer("nextPageHotels")]
        public function nextPageHotels():void	{
			In(hotelSearch.nextPageAvailable)
            hotelSearch.nextPage();
        }
		
        /*
        [Observer("preLogout")]
        public function logoutHandler(event:TideContextEvent):void {
            event.context.mainAppUI.tabNavigator.removeAllChildren();
        }
		*/
    }
}