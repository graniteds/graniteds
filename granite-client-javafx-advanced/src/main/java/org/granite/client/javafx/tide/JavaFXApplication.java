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
package org.granite.client.javafx.tide;

import java.util.Map;

import javax.validation.TraversableResolver;
import javax.validation.ValidatorFactory;

import org.granite.client.tide.Context;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.validation.NotifyingValidation;
import org.granite.logging.Logger;

/**
 * @author William DRAI
 */
public class JavaFXApplication implements org.granite.client.tide.Application {
    
    private static final Logger log = Logger.getLogger(JavaFXApplication.class);
	
	public void initContext(Context context, Map<String, Object> initialBeans) {
	    DataManager dataManager = new JavaFXDataManager();
	    context.setDataManager(dataManager);
	    
	    try {
            new BeanValidationConfiguration().configure(context, initialBeans);
	    }
	    catch (Throwable e) {
	        // Assume Bean Validation not available
	        log.info("Bean validation not available, support not configured");
	    }
	}
	
	public void configure(Object instance) {
		if (instance instanceof ServerSession)
			((ServerSession)instance).setStatus(new JavaFXServerSessionStatus());
	}
	
	@Override
    public void execute(Runnable runnable) {
        javafx.application.Platform.runLater(runnable);
    }


    private static final class BeanValidationConfiguration {
        public void configure(Context context, Map<String, Object> initialBeans) {
            TraversableResolver traversableResolver = new JavaFXTraversableResolver(context.getDataManager());
            ValidatorFactory validatorFactory = NotifyingValidation.byDefaultProvider().configure().traversableResolver(traversableResolver).buildValidatorFactory();

            initialBeans.put("traversableResolver", traversableResolver);
            initialBeans.put("validatorFactory", validatorFactory);
        }
    }
}
