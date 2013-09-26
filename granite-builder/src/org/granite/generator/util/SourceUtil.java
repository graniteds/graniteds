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

package org.granite.generator.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * @author Franck WOLFF
 */
public abstract class SourceUtil {

    public static String numberize(String source) {
    	
    	if (source == null)
    		return "(source code not available)";

    	StringBuilder sb = new StringBuilder(source.length());

        try {
            BufferedReader reader = new BufferedReader(new StringReader(source));
            int i = 1;
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(String.format("%4d | ", Integer.valueOf(i++)))
                  .append(line)
                  .append('\n');
            }
        } catch (IOException e) {
            // never...
        }

        return sb.toString();
    }
}
