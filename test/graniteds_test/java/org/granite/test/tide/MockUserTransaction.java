package org.granite.test.tide;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

public class MockUserTransaction implements UserTransaction {

	@Override
	public void begin() throws NotSupportedException, SystemException {
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException,
			HeuristicRollbackException, SecurityException,
			IllegalStateException, SystemException {
	}

	@Override
	public int getStatus() throws SystemException {
		return 0;
	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException, SystemException {
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
	}

	@Override
	public void setTransactionTimeout(int arg0) throws SystemException {
	}

}
