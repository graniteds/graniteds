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

package org.granite.tide.invocation {

    import mx.collections.ArrayCollection;
    
    import org.granite.tide.IInvocationResult;
    

    [ExcludeClass]
    [RemoteClass(alias="org.granite.tide.invocation.InvocationResult")]
    /**
     * @author William DRAI
     */
    public class InvocationResult implements IInvocationResult {
        
        private var _result:Object = null;
        
        
        public function InvocationResult() {
            super();
        }
        
        public function get result():Object {
            return _result;
        }
        public function set result(result:Object):void {
            _result = result;
        }
        
        public var scope:int;
        public var restrict:Boolean;
        public var merge:Boolean = true;
		public var updates:Array;
        public var results:ArrayCollection;
        public var events:ArrayCollection;
        public var messages:ArrayCollection;
        public var keyedMessages:Object;
		
		
		public function toString():String {
			var sb:String = "InvocationResult ";
			if (scope == 1)
				sb += "(SESSION) ";
			else if (scope == 2)
				sb += "(CONVERSATION) ";
			if (restrict)
				sb += "(restricted) ";
			sb += "{\n";
			sb += "\tresult: " + (result != null ? result : "(null)");
			if (results != null) {
				sb += "\tresults: [";
				for (var result:Object in results)
					sb += (result != null ? result.toString() : "(null)") + " ";
				sb += "]\n";
			}
			if (updates != null) {
				sb += "\tupdates: [";
				for (var update:Object in updates)
					sb += update[0] + ":" + update[1] + " ";
				sb += "\n";
			}
			if (events != null) {
				sb += "\tevents: [";
				for (var event:Object in events)
					sb += event + " ";
				sb += "]\n";
			}
			sb += "}";
			return sb;
		}
    }
}