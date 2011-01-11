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

package org.granite.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Franck WOLFF
 */
public class StreamGobbler extends Thread {

    private final InputStream is;
    private final OutputStream os;

    public StreamGobbler(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    @Override
    public void run() {
        try {
            for (int b = is.read(); b != -1; b = is.read())
                os.write(b & 0xFF);
        } catch (IOException e)  {
            throw new RuntimeException(e);
        } finally {
            try {
                os.flush();
                os.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
