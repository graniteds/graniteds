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
    
    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;


    [Bindable]
    [RemoteClass(alias="org.granite.tide.TideMessage")]
    /**
     * 	TideMessage is a simple status message object with a severity and text
     *  It is similar to the JBoss Seam StatusMessage class or the JSF FacesMessages
     * 	In general it is used to get messages from the server but messages can also be added locally 	
     * 
     * 	@author William DRAI
     */
    public class TideMessage implements IExternalizable {
        
        public static const INFO:String = "INFO";
        public static const WARNING:String = "WARNING";
        public static const ERROR:String = "ERROR";
        public static const FATAL:String = "FATAL";
        
        
        private var _severity:String;
        private var _summary:String;
        private var _detail:String;
        
        
        public function TideMessage(severity:String = "INFO", summary:String = "", detail:String = "") {
            super();
            _severity = severity;
            _summary = summary;
            _detail = detail;
        }
        
        public function get severity():String {
            return _severity;
        }
        public function set severity(value:String):void {
            _severity = value;
        }
        
        public function get summary():String {
            return _summary;
        }
        public function set summary(value:String):void {
            _summary = value;
        }
        
        public function get detail():String {
            return _detail;
        }
        public function set detail(value:String):void {
            _detail = value;
        }
        
        /**
         *	@private
         */ 
        public function readExternal(input:IDataInput):void {
            _detail = input.readObject() as String;
            _severity = input.readObject() as String;
            _summary = input.readObject() as String;
        }
        
        /**
         *	@private
         */ 
        public function writeExternal(out:IDataOutput):void {
            // read only bean...
        }
    }
}