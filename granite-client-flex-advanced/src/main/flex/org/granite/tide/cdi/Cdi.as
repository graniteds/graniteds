/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.tide.cdi {
	
    import mx.logging.ILogger;
    import mx.logging.Log;

    import org.granite.tide.Tide;
    import org.granite.tide.Component;
    import org.granite.tide.service.ServerSession;

    [Bindable]
	/**
	 * 	Implementation of the Tide singleton for EJB3 services
	 * 
     * 	@author William DRAI
     */
	public class Cdi extends Tide {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.cdi.Cdi");
		
		public static const IDENTITY_NAME:String = "org.granite.tide.cdi.identity";
		
		
		public function Cdi(destination:String) {
		    super(destination);
		}
		
		public static function getInstance(destination:String = null):Cdi {
			return Tide.getInstance(destination != null ? destination : "server", Cdi) as Cdi;
		}
		
		/**
		 *	Clear Tide singleton (should be used only for testing)
		 */
		public static function resetInstance():void {
			Tide.resetInstance();
		}
		
		protected override function init(contextClass:Class, componentClass:Class):void {
		    super.init(Context, Component);
			addComponent(IDENTITY_NAME, Identity);
			setComponentRemoteSync(IDENTITY_NAME, Tide.SYNC_NONE);
			getDescriptor(IDENTITY_NAME).scope = Tide.SCOPE_SESSION;
		}

        protected override function initServerSession(destination:String):ServerSession {
            return new CdiServerSession(destination);
        }

		public function getCdiContext(contextId:String = null):Context {
		    return super.getContext(contextId) as Context;
		}
		
	}
}
