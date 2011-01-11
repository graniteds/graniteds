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

package org.granite.tide {

    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    

    /**
     * 	TideResponder defines a simple responder for remote calls
     * 	It allows to keep a token object between the call and the result/fault handler
     *  It also allows to add many result/fault handlers that will all be triggered by the origin call
     * 	
     * 	@author William DRAI
     */
    public class TideResponder implements ITideMergeResponder {
        
        private var _resultHandlers:Array = new Array();
        private var _faultHandlers:Array = new Array();
        private var _token:Object;
        private var _mergeWith:Object = null;
        
        
        public function TideResponder(resultHandler:Function, faultHandler:Function, token:Object = null, mergeWith:Object = null):void {
            if (resultHandler != null)
                _resultHandlers.push(resultHandler);
            if (faultHandler != null)
                _faultHandlers.push(faultHandler);   
            _token = token;
            _mergeWith = mergeWith;
        }
        
        /**
         * 	Add result/fault handlers
         * 
         *  @param resultHandler a result handler method
         *  @param faultHandler a fault handler method
         */
        public function addHandlers(resultHandler:Function = null, faultHandler:Function = null):void {
            if (resultHandler != null)
                _resultHandlers.push(resultHandler);
            if (faultHandler != null)
                _faultHandlers.push(faultHandler);
        }
        
        /**
         * 	Result handler method
         * 
         *  @param event result event
         */
        public function result(event:TideResultEvent):void {
            for each (var resultHandler:Function in _resultHandlers) { 
                if (_token != null)
                    resultHandler(event, _token);
                else
                    resultHandler(event);
            }
        }
        
        /**
         * 	Fault handler method
         * 
         *  @param event fault event
         */
        public function fault(event:TideFaultEvent):void {
            for each (var faultHandler:Function in _faultHandlers) {
                if (_token != null)
                    faultHandler(event, _token);
                else
                    faultHandler(event);
            }
        } 
        
        
        public function get mergeResultWith():Object {
            return _mergeWith;
        }
    }
}