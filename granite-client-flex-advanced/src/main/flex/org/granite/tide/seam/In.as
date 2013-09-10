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

package org.granite.tide.seam { 

	/**
	 * 	Syntax helper to clarify component property evaluations.<br/>
	 *  <br/>
	 *  Example:<br/>
	 *  <pre>
	 *  import org.granite.tide.seam.In;
	 *  
	 *  ...
	 * 
	 * 	In(myComponent.myProperty)
	 *  myComponent.myMethod(param);
	 *  </pre>
	 *  <br/>
	 *  This actually stricly equivalent to:<br/>
	 *  <pre>
	 * 	myComponent.myProperty;
	 *  myComponent.myMethod(param);
	 *  </pre>
	 * 
	 * 	@param args dummy arguments used to evalutate properties
	 */   
	public function In(... args):void {
	}
}
