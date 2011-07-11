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

package test.granite.ctl
{

import flash.utils.flash_proxy;

import mx.messaging.messages.ErrorMessage;
import mx.utils.object_proxy;

import org.granite.tide.BaseContext;
import org.granite.tide.IExceptionHandler;

use namespace flash_proxy;
use namespace object_proxy;


/**
 * @author William DRAI
 */
public class SecurityExceptionHandler implements IExceptionHandler
{

    public static const SECURITY:String = "Server.Security.";


    public function accepts( emsg:ErrorMessage ):Boolean
    {
        return emsg.faultCode.search( SECURITY ) == 0;
    }

    public function handle( context:BaseContext, emsg:ErrorMessage ):void
    {
        // Ignore for now
    }
}
}
