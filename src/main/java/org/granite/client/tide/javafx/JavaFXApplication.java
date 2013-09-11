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
package org.granite.client.tide.javafx;

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
	        TraversableResolver traversableResolver = new JavaFXTraversableResolver(dataManager);
	        ValidatorFactory validatorFactory = NotifyingValidation.byDefaultProvider().configure().traversableResolver(traversableResolver).buildValidatorFactory();
	        
	        initialBeans.put("traversableResolver", traversableResolver);
	        initialBeans.put("validatorFactory", validatorFactory);
	    }
	    catch (Exception e) {
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
}
