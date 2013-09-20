/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
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
		private var _charset:String = null;

        public function get topic():String {
            return _topic;
        }
        public function set topic(value:String):void {
            _topic = value;
        }
        
        public function setIdentity(username:String, password:String, charset:String = null):void {
            _username = username;
			_password = password;
			_charset = charset;
            
            if (connected)
                doAuthenticate();
        }
        protected function doAuthenticate():void {
            if (_username != null || _password != null) {
				setCredentials(_username, _password, _charset);
                _username = _password = _charset = null;
            }
        }
        override public function logout():void {
            super.logout();
            _username = _password = _charset = null;
        }
    }
}
