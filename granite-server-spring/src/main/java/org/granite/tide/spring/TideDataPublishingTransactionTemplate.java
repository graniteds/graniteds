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

package org.granite.tide.spring;

import org.granite.gravity.Gravity;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataUpdatePostprocessor;
import org.granite.util.ThrowableCallable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * Extended Spring Transaction template which handles publishing of data changes
 * This can be used outside of any Granite context and can replace the default Spring TransactionTemplate
 *
 * @author William DRAI
 */
public class TideDataPublishingTransactionTemplate extends TransactionTemplate implements InitializingBean {

    private static final long serialVersionUID = 1L;

    //private static final Logger log = Logger.getLogger(TideDataPublishingTransactionTemplate.class);

    private Gravity gravity;
    private DataUpdatePostprocessor dataUpdatePostprocessor;

    private TideDataPublishingWrapper tideDataPublishingWrapper = null;

    @Autowired
    public void setGravity(Gravity gravity) {
        this.gravity = gravity;
    }

    public void setTideDataPublishingWrapper(TideDataPublishingWrapper tideDataPublishingWrapper) {
        this.tideDataPublishingWrapper = tideDataPublishingWrapper;
    }

    @Autowired(required=false)
    public void setDataUpdatePostprocessor(DataUpdatePostprocessor dataUpdatePostprocessor) {
        this.dataUpdatePostprocessor = dataUpdatePostprocessor;
    }

    @Override
    public void afterPropertiesSet() {
        if (tideDataPublishingWrapper == null)
            tideDataPublishingWrapper = new TideDataPublishingWrapper(gravity, dataUpdatePostprocessor);
    }

    @Override
    public <T> T execute(TransactionCallback<T> action) throws TransactionException {
        DataEnabled dataEnabled = null;
        if (action.getClass().isAnnotationPresent(DataEnabled.class))
            dataEnabled = action.getClass().getAnnotation(DataEnabled.class);
        else if ((action.getClass().isMemberClass() || action.getClass().isAnonymousClass()) && action.getClass().getEnclosingClass().isAnnotationPresent(DataEnabled.class))
            dataEnabled = action.getClass().getEnclosingClass().getAnnotation(DataEnabled.class);

        if (dataEnabled == null || !dataEnabled.useInterceptor())
            return super.execute(action);

        return super.execute(new DataEnabledTransactionCallback<T>(action, dataEnabled));
    }

    private class DataEnabledTransactionCallback<T> implements TransactionCallback<T> {

        private final TransactionCallback<T> action;
        private final DataEnabled dataEnabled;

        public DataEnabledTransactionCallback(TransactionCallback<T> action, DataEnabled dataEnabled) {
            this.action = action;
            this.dataEnabled = dataEnabled;
        }

        @Override
        public T doInTransaction(final TransactionStatus status) {
            try {
                return tideDataPublishingWrapper.execute(dataEnabled, new ThrowableCallable<T>() {
                    public T call() throws Throwable {
                        return action.doInTransaction(status);
                    }
                });
            }
            catch (Error e) {
                throw e;
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Throwable t) {
                throw new RuntimeException("Error in transaction", t);
            }
        }
    }
}
