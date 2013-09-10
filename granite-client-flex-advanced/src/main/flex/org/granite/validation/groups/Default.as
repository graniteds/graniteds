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

package org.granite.validation.groups {

	/**
	 * The default validation group (equivalent of
	 * <code>javax.validation.groups.Default</code>).<p />
	 * 
	 * Note: if you refer to <code>javax.validation.groups.Default</code>
	 * in your constraint annotation declaration, this interface will be
	 * used instead. Hence, the two following declarations are stricktly
	 * equivalent:<p />
	 * <listing>
	 * [NotNull(groups="javax.validation.groups.Default")]
	 * public function get property():String {...}
	 * 
	 * [NotNull(groups="org.granite.validation.groups.Default")]
	 * public function get property():String {...}</listing>
	 * 
	 * @author Franck WOLFF
	 */
	public interface Default {}
}