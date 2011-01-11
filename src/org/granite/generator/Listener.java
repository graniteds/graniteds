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

package org.granite.generator;

/**
 * @author Franck WOLFF
 */
public interface Listener {

	public static final String MSG_FILE_NOT_EXISTS = "output file does not exist";
	public static final String MSG_FILE_EXISTS_NO_OVER = "output file already exists and must not be overwritten";
	public static final String MSG_FILE_OUTDATED = "output file is outdated";
	public static final String MSG_FILE_UPTODATE = "output file is up-to-date";

	public void generating(Input<?> input, Output<?> output);
	public void generating(String file, String message);
	public void skipping(Input<?> input, Output<?> output);
	public void skipping(String file, String message);
	
    public void debug(String message);
    public void debug(String message, Throwable t);
	
    public void info(String message);
    public void info(String message, Throwable t);

    public void warn(String message);
    public void warn(String message, Throwable t);

    public void error(String message);
    public void error(String message, Throwable t);
}
