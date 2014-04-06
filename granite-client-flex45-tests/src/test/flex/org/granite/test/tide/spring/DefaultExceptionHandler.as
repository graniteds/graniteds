/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.spring {

    import flash.utils.flash_proxy;
    
    import mx.collections.IList;
    import mx.collections.errors.ItemPendingError;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    import mx.utils.object_proxy;
    import mx.controls.Alert;
    
    import org.granite.collections.IPersistentCollection;
    import org.granite.meta;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IExceptionHandler;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.events.TideValidatorEvent;
    import org.granite.tide.service.ServerSession;

    use namespace flash_proxy;
    use namespace object_proxy;
    

    /**
     * @author William DRAI
     */
    public class DefaultExceptionHandler implements IExceptionHandler {
		
        public function accepts(emsg:ErrorMessage):Boolean {
            return true;
        }

        public function handle(serverSession:ServerSession, context:BaseContext, emsg:ErrorMessage):void {
           	context.raiseEvent("handled.default");
        }
    }
}
