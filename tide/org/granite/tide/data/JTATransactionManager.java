/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.tide.data;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.granite.tide.TideTransactionManager;

/**
 * Responsible for attaching a entity with the entity mangager
 * @author cingram
 *
 */
public class JTATransactionManager implements TideTransactionManager {
	
	public static final String USER_TRANSACTION_JNDI = "java:comp/UserTransaction";

	
	public Object begin(TideTransactionPersistenceManager pm) {
        try {
            InitialContext ic = new InitialContext();
            UserTransaction ut = (UserTransaction)ic.lookup(USER_TRANSACTION_JNDI);
            ut.begin();
            return ut;
        }
        catch (NamingException f) {
            throw new RuntimeException("Could not initiate JTA transaction for lazy initialization", f);
        }
        catch (SystemException f) {
            throw new RuntimeException("Could not initiate JTA transaction for lazy initialization", f);
        }
        catch (NotSupportedException f) {
            throw new RuntimeException("Could not initiate JTA transaction for lazy initialization", f);
        }
	}
	
	public void commit(Object tx) throws Exception {
        if (tx instanceof UserTransaction)
            ((UserTransaction)tx).commit();
	}
	
	public void rollback(Object tx) {
	    try {
            if (tx instanceof UserTransaction)
                ((UserTransaction)tx).rollback();
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to rollback attach entity and init collection", e);
        }
	}
}
