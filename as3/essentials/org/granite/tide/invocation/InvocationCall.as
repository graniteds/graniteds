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
    
    import org.granite.tide.IInvocationCall;
    

    [ExcludeClass]
    [RemoteClass(alias="org.granite.tide.invocation.InvocationCall")]
    /**
     * @author William DRAI
     */
    public class InvocationCall implements IInvocationCall {
        
        public function InvocationCall(listeners:ArrayCollection = null, updates:ArrayCollection = null, results:Array = null) {
            super();
            // Copy collection content because original collections are cleared later by the context
            var c:Object;
            this.listeners = new ArrayCollection();
            for each (c in listeners)
                this.listeners.addItem(c);
            this.updates = new ArrayCollection();
            for each (c in updates)
                this.updates.addItem(c);
            this.results = new Array();
            for each (c in results)
                this.results.push(c);
        }
        
        public var listeners:ArrayCollection;
        public var updates:ArrayCollection;
        public var results:Array;
		
		
		public function toString():String {
			var sb:String = "InvocationCall {\n";
			if (listeners != null) {
				sb += "\tlisteners: [";
				for (var listener:String in listeners)
					sb += listener + " ";
				sb += "]\n";
			}
			if (updates != null) {
				sb += "\tupdates: [";
				for (var update:Object in updates)
					sb += update += " ";
				sb += "]\n";
			}
			if (results != null) {
				sb += "\tresults: [";
				for (var result:Object in results)
					sb += (result != null ? result.toString() : "(null)") + " ";
				sb += "]\n";
			}
			sb += "}";
			return sb;
		}
    }
}