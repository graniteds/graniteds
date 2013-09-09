/**
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
