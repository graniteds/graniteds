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
package org.granite.tide.data;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.persistence.TransactionRequiredException;

import org.granite.messaging.service.ExceptionConverter;
import org.granite.messaging.service.ServiceException;


public class PersistenceExceptionConverter implements ExceptionConverter {
    
    public static final String ENTITY_EXISTS = "Persistence.EntityExists";
    public static final String ENTITY_NOT_FOUND = "Persistence.EntityNotFound";
    public static final String NON_UNIQUE_RESULT = "Persistence.NonUnique";
    public static final String NO_RESULT = "Persistence.NoResult";
    public static final String OPTIMISTIC_LOCK = "Persistence.OptimisticLock";
    public static final String TRANSACTION_REQUIRED = "Persistence.TransactionRequired";
    public static final String ROLLBACK = "Persistence.Rollback";
    public static final String OTHER = "Persistence.Error";
    

    public boolean accepts(Throwable t, Throwable finalException) {
        return t.getClass().equals(EntityExistsException.class) 
            || t.getClass().equals(EntityNotFoundException.class)
            || t.getClass().equals(NonUniqueResultException.class)
            || t.getClass().equals(NoResultException.class)
            || t.getClass().equals(OptimisticLockException.class)
            || t.getClass().equals(TransactionRequiredException.class)
            || t.getClass().equals(RollbackException.class)
            || PersistenceException.class.isAssignableFrom(t.getClass());
    }

    public ServiceException convert(Throwable t, String detail, Map<String, Object> extendedData) {
        String error = null;
        Map<String, Object> ex = null;
        if (t.getClass().equals(EntityExistsException.class))
            error = ENTITY_EXISTS;
        else if (t.getClass().equals(EntityNotFoundException.class))
            error = ENTITY_NOT_FOUND;
        else if (t.getClass().equals(NonUniqueResultException.class))
            error = NON_UNIQUE_RESULT;
        else if (t.getClass().equals(NoResultException.class))
            error = NO_RESULT;
        else if (t.getClass().equals(OptimisticLockException.class)) {
            error = OPTIMISTIC_LOCK;
            ex = new HashMap<String, Object>();
            ex.put("entity", ((OptimisticLockException)t).getEntity());
        }
        else if (t.getClass().equals(TransactionRequiredException.class))
            error = TRANSACTION_REQUIRED;
        else if (t.getClass().equals(RollbackException.class))
            error = ROLLBACK;
        else
            error = OTHER; 
        
        ServiceException se = new ServiceException(error, t.getMessage(), detail, t);
        if (ex != null && !ex.isEmpty())
            se.getExtendedData().putAll(ex);
        return se;
    }

}
