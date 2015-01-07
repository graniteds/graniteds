/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.spring;

import org.granite.gravity.Gravity;
import org.granite.logging.Logger;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.granite.tide.data.DataUpdatePostprocessor;
import org.granite.tide.data.TideSynchronizationManager;
import org.granite.util.ThrowableCallable;
import org.granite.util.TypeUtil;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Common class to implement data enabled interceptors
 */
public class TideDataPublishingWrapper {

    private static final Logger log = Logger.getLogger(TideDataPublishingWrapper.class);

    private Map<String, TideSynchronizationManager> syncsMap = new HashMap<String, TideSynchronizationManager>();

    private Gravity gravity;
    private DataUpdatePostprocessor dataUpdatePostprocessor;

    public void setGravity(Gravity gravity) {
        this.gravity = gravity;
    }

    public void setDataUpdatePostprocessor(DataUpdatePostprocessor dataUpdatePostprocessor) {
        this.dataUpdatePostprocessor = dataUpdatePostprocessor;
    }

    public TideDataPublishingWrapper() {
        try {
            syncsMap.put("org.springframework.orm.hibernate3.SessionHolder", TypeUtil.newInstance("org.granite.tide.spring.Hibernate3SynchronizationManager", TideSynchronizationManager.class));
        }
        catch (Throwable e) {
            // Hibernate 3 not present
        }
        try {
            syncsMap.put("org.springframework.orm.hibernate4.SessionHolder", TypeUtil.newInstance("org.granite.tide.spring.Hibernate4SynchronizationManager", TideSynchronizationManager.class));
        }
        catch (Throwable e) {
            // Hibernate 4 not present
        }
        try {
            syncsMap.put("org.springframework.orm.jpa.EntityManagerHolder", TypeUtil.newInstance("org.granite.tide.spring.JPASynchronizationManager", TideSynchronizationManager.class));
        }
        catch (Throwable e) {
            // JPA not present
        }
    }

    public TideDataPublishingWrapper(Gravity gravity, DataUpdatePostprocessor dataUpdatePostprocessor) {
        this();
        this.gravity = gravity;
        this.dataUpdatePostprocessor = dataUpdatePostprocessor;
    }

    public <T> T execute(DataEnabled dataEnabled, ThrowableCallable<T> action) throws Throwable {
        boolean shouldRemoveContextAtEnd = DataContext.get() == null;
        boolean shouldInitContext = shouldRemoveContextAtEnd || DataContext.isNull();
        boolean onCommit = false;

        if (shouldInitContext) {
            DataContext.init(gravity, dataEnabled.topic(), dataEnabled.params(), dataEnabled.publish());
            if (dataUpdatePostprocessor != null)
                DataContext.get().setDataUpdatePostprocessor(dataUpdatePostprocessor);
        }

        DataContext.observe();
        try {
            if (dataEnabled.publish().equals(PublishMode.ON_COMMIT) && !TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                if (TransactionSynchronizationManager.isSynchronizationActive()) {
                    boolean registered = false;
                    for (Object resource : TransactionSynchronizationManager.getResourceMap().values()) {
                        if (syncsMap.containsKey(resource.getClass().getName())) {
                            registered = syncsMap.get(resource.getClass().getName()).registerSynchronization(resource, shouldRemoveContextAtEnd);
                            break;
                        }
                    }
                    if (!registered)
                        TransactionSynchronizationManager.registerSynchronization(new DataPublishingTransactionSynchronization(shouldRemoveContextAtEnd));
                    else if (shouldRemoveContextAtEnd)
                        TransactionSynchronizationManager.registerSynchronization(new DataContextCleanupTransactionSynchronization());
                    onCommit = true;
                }
                else if (TransactionSynchronizationManager.isActualTransactionActive()) {
                    log.error("Could not register synchronization for ON_COMMIT publish mode, check that the Spring PlatformTransactionManager supports it "
                            + "and that the order of the TransactionInterceptor is lower than the order of TideDataPublishingInterceptor");
                    if (shouldRemoveContextAtEnd)
                        DataContext.remove();
                }
                else {
                    // No transaction, clear data context and wait for a possible manual transactions
                    if (shouldRemoveContextAtEnd)
                        DataContext.remove();
                }
            }

            T ret = action.call();

            DataContext.publish(PublishMode.ON_SUCCESS);
            return ret;
        }
        finally {
            if (shouldRemoveContextAtEnd && !onCommit)
                DataContext.remove();
        }
    }

    private static class DataPublishingTransactionSynchronization extends TransactionSynchronizationAdapter {

        private boolean removeContext = false;

        public DataPublishingTransactionSynchronization(boolean removeContext) {
            this.removeContext = removeContext;
        }

        @Override
        public void beforeCommit(boolean readOnly) {
            if (!readOnly)
                DataContext.publish(PublishMode.ON_COMMIT);
        }

        @Override
        public void beforeCompletion() {
            if (removeContext)
                DataContext.remove();
        }

        @Override
        public void afterCompletion(int status) {
            if (removeContext)
                DataContext.remove();
        }
    }

    private static class DataContextCleanupTransactionSynchronization extends TransactionSynchronizationAdapter {

        @Override
        public void afterCompletion(int status) {
            DataContext.remove();
        }
    }

}
