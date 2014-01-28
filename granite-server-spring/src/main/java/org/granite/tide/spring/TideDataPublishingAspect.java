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

import java.util.concurrent.Callable;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.granite.gravity.Gravity;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataUpdatePostprocessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

/**
 * Spring AOP AspectJ aspect to handle publishing of data changes instead of relying on the default behaviour
 * This can be used outside of a HTTP Granite context and inside the security/transaction context
 *  
 * @author William DRAI
 */
@Aspect
public class TideDataPublishingAspect implements Ordered, InitializingBean {
	
	//private static final Logger log = Logger.getLogger(TideDataPublishingAspect.class);

	private int order = 0;
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

    public int getOrder() {
        return order;
    }
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (tideDataPublishingWrapper == null)
            tideDataPublishingWrapper = new TideDataPublishingWrapper(gravity, dataUpdatePostprocessor);
    }

	@Around("@within(dataEnabled)")
    public Object invoke(final ProceedingJoinPoint pjp, DataEnabled dataEnabled) throws Throwable {
    	if (dataEnabled == null || !dataEnabled.useInterceptor())
    		return pjp.proceed();

        return tideDataPublishingWrapper.execute(dataEnabled, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                try {
                    return pjp.proceed();
                }
                catch (Exception e) {
                    throw e;
                }
                catch (Throwable t) {
                    // Not sure what to do in case of a throwable
                    throw new RuntimeException("Data publishing error", t);
                }
            }
        });
    }
}
