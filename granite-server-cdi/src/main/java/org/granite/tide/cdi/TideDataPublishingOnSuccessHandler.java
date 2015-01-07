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
package org.granite.tide.cdi;

import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Instance;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.granite.logging.Logger;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataEnabled.PublishMode;


/**
 * CDI event listener to handle publishing of data changes instead of relying on the default behaviour
 * This can be used outside of a HTTP Granite context and inside the security/transaction context
 * @author William DRAI
 *
 */
public class TideDataPublishingOnSuccessHandler {
	
	private static final Logger log = Logger.getLogger(TideDataPublishingOnSuccessHandler.class);
	
    public void doPublish(@Observes(during=TransactionPhase.BEFORE_COMPLETION) TideDataPublishingEvent event, Instance<UserTransaction> uts) {
        if (uts.isUnsatisfied() || uts.isAmbiguous()) {
            // No JTA UserTransaction, probably a servlet container
            // Anyway we should not be here
            return;
        }

        UserTransaction ut = uts.get();        
    	try {
			if (!(ut.getStatus() == Status.STATUS_MARKED_ROLLBACK || ut.getStatus() == Status.STATUS_ROLLING_BACK || ut.getStatus() == Status.STATUS_ROLLING_BACK))
				DataContext.publish(PublishMode.ON_COMMIT);
		} 
    	catch (SystemException e) {
    		log.warn(e, "Could not get the current status of the transaction ???");
		}
    	if (event.getInitContext())
    		DataContext.remove();
    }
}
