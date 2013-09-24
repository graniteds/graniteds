/*
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
package org.granite.tide.data.model {
	
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	
	
	[RemoteClass(alias="org.granite.tide.data.model.PageInfo")]
	public class PageInfo implements IExternalizable {
		
		public var firstResult:int;
		public var maxResults:int;
		public var sortInfo:SortInfo;
		
		
		public function PageInfo(firstResult:int = 0, maxResults:int = 0, order:Array = null, desc:Array = null):void {
			this.firstResult = firstResult;
			this.maxResults = maxResults;
			if (order == null || desc == null)
				this.sortInfo = null;
			else
				this.sortInfo = new SortInfo(order, desc);
		}
		
	
		public function writeExternal(out:IDataOutput):void {
			out.writeObject(firstResult);
			out.writeObject(maxResults);
			out.writeObject(sortInfo != null ? sortInfo.order : null);
			out.writeObject(sortInfo != null ? sortInfo.desc : null);
		}
		
		public function readExternal(input:IDataInput):void {
			this.firstResult = input.readObject() as int;
			this.maxResults = input.readObject() as int;
			var order:Array = input.readObject() as Array;
			var desc:Array = input.readObject() as Array;
			if (order != null && desc != null)
				this.sortInfo = new SortInfo(order, desc);
			else
				this.sortInfo = null;
		}
	
	}
}