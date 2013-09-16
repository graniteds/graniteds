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
//========================================================================
//$Id: WaitingContinuation.java,v 1.1 2005/11/14 17:45:56 gregwilkins Exp $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.granite.gravity.generic;

import org.granite.logging.Logger;


/* Extract from Jetty 6.1 to support other servlet containers */
public class WaitingContinuation {
	
	private static final Logger log = Logger.getLogger(WaitingContinuation.class);
	
    Object _mutex;
    Object _object;
    boolean _new=true;
    boolean _resumed=false;
    boolean _pending=false;
    boolean _expired=false;

    public WaitingContinuation()
    {
        _mutex=this;
    }
    
    public WaitingContinuation(Object mutex)
    {
        _mutex=mutex==null?this:mutex;
    }
    
    public void resume()
    {
        synchronized (_mutex)
        {
            _resumed=true;
            _mutex.notify();
        }
    }
    
    public void reset()
    {
        synchronized (_mutex)
        {
            _resumed=false;
            _pending=false;
            _expired=false;
            _mutex.notify();
        }
    }

    public boolean isNew()
    {
        return _new;
    }

    public boolean suspend(long timeout)
    {
        synchronized (_mutex)
        {
            _new=false;
            _pending=true;
            boolean result;
            try
            {
                log.debug("Continuation suspend " + timeout);
                if (!_resumed && timeout>=0)
                {
                    if (timeout==0)
                        _mutex.wait();
                    else if (timeout>0)
                        _mutex.wait(timeout);
                        
                }
            }
            catch (InterruptedException e)
            {
                _expired=true;
                log.debug("Continuation timeout");
            }
            finally
            {
                result=_resumed;
                _resumed=false;
                _pending=false;
            }
            
            return result;
        }
    }
    
    public boolean isPending()
    {
        synchronized (_mutex)
        {
            return _pending;
        }
    }
    
    public boolean isResumed()
    {
        synchronized (_mutex)
        {
            return _resumed;
        }
    }
    
    public boolean isExpired()
    {
        synchronized (_mutex)
        {
            return _expired;
        }
    }

    public Object getObject()
    {
        return _object;
    }

    public void setObject(Object object)
    {
        _object = object;
    }

    public Object getMutex()
    {
        return _mutex;
    }

    public void setMutex(Object mutex)
    {
        if (!_new && _mutex!=this && _pending)
            throw new IllegalStateException();
        _mutex = mutex==null ? this : mutex; 
    }

    @Override
    public String toString()
    {
        synchronized (this)
        {
            return "WaitingContinuation@"+hashCode()+
            (_new?",new":"")+
            (_pending?",pending":"")+
            (_resumed?",resumed":"")+
            (_expired?",expired":"");
        }
    }
}