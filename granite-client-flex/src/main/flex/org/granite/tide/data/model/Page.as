/*
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
package org.granite.tide.data.model {
		
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	
	import mx.collections.IList;
	
	
	[RemoteClass(alias="org.granite.tide.data.model.Page")]
	public class Page implements IExternalizable {
		
		public var firstResult:int;
		public var maxResults:int;
		public var resultCount:int;
		public var resultList:IList;
		
		
		public function Page():void {
		}
	
		public function writeExternal(out:IDataOutput):void {
			out.writeObject(firstResult);
			out.writeObject(maxResults);
			out.writeObject(resultCount);
			out.writeObject(resultList);
		}
	
		public function readExternal(input:IDataInput):void {
			firstResult = input.readObject() as int;
			maxResults = input.readObject() as int;
			resultCount = input.readObject() as int;
			resultList = input.readObject() as IList;
		}
	
	}
}