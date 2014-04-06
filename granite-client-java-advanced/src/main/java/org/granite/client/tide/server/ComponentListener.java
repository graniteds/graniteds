/**
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
package org.granite.client.tide.server;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.tide.Context;

/**
 * @author William DRAI
 */
public interface ComponentListener<T> extends ResponseListener {
    
    public String getOperation();
    
    public Object[] getArgs();
    public void setArgs(Object[] args);
    
    public Context getSourceContext();
    
    public Component getComponent();
    
    public void setResult(T result);
    
    public T getResult() throws InterruptedException, ExecutionException;
    
    public Future<T> invoke(ServerSession serverSession);
    
    
    public static interface Handler<T> {
        
        public Runnable result(Context context, ResultEvent event, Object info, String componentName, String operation, TideResponder<T> tideResponder, ComponentListener<T> componentResponder);
        
        public Runnable fault(Context context, FaultEvent event, Object info, String componentName, String operation, TideResponder<T> tideResponder, ComponentListener<T> componentResponder);
        
        public Runnable issue(Context context, IssueEvent event, Object info, String componentName, String operation, TideResponder<T> tideResponder, ComponentListener<T> componentResponder);
    }
}
