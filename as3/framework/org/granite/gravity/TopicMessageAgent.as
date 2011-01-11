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

package org.granite.gravity {

    import mx.messaging.MessageAgent;

    /**
     *	Gravity-specific base class for message agents
     *  
     * 	@author Franck WOLFF
     */
    public class TopicMessageAgent extends MessageAgent {

        private var _topic:String = null;
        private var _username:String = null;
        private var _password:String = null;

        public function get topic():String {
            return _topic;
        }
        public function set topic(value:String):void {
            _topic = value;
        }
        
        public function setIdentity(username:String, password:String):void {
            _username = username;
            _password = password;
            
            if (connected)
                doAuthenticate();
        }
        protected function doAuthenticate():void {
            if (_username != null || _password != null) {
                setCredentials(_username, _password);
                _username = _password = null;
            }
        }
        override public function logout():void {
            super.logout();
            _username = _password = null;
        }
    }
}
