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
package org.granite.tide.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.granite.gravity.Gravity;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataUpdatePostprocessor;
import org.granite.util.ThrowableCallable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Spring AOP interceptor to handle publishing of data changes instead of relying on the default behaviour
 * This can be used outside of a HTTP Granite context and inside the security/transaction context
 * 
 * Ensure that the order of the interceptor is correctly setup to use ON_COMMIT publish mode: this interceptor must be executed inside a transaction
 *  
 * @author William DRAI
 */
public class TideDataPublishingInterceptor implements MethodInterceptor, InitializingBean {
	
	//private static final Logger log = Logger.getLogger(TideDataPublishingInterceptor.class);
	
	private Gravity gravity;
	private DataUpdatePostprocessor dataUpdatePostprocessor;
	
	private TideDataPublishingWrapper tideDataPublishingWrapper = null;

	
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
    public void afterPropertiesSet() throws Exception {
        if (tideDataPublishingWrapper == null)
            tideDataPublishingWrapper = new TideDataPublishingWrapper(gravity, dataUpdatePostprocessor);
    }

    public Object invoke(final MethodInvocation invocation) throws Throwable {
    	DataEnabled dataEnabled = invocation.getThis().getClass().getAnnotation(DataEnabled.class);
    	if (dataEnabled == null || dataEnabled.topic().equals("") || !dataEnabled.useInterceptor())
    		return invocation.proceed();

        return tideDataPublishingWrapper.execute(dataEnabled, new ThrowableCallable<Object>() {
            @Override
            public Object call() throws Throwable {
                return invocation.proceed();
            }
        });
    }
}
