/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author William DRAI
 */
public class TideMessage implements Externalizable {
    
	private static final long serialVersionUID = 1L;
	
    public static final String INFO = "INFO";
    public static final String WARNING = "WARNING";
    public static final String ERROR = "ERROR";
    public static final String FATAL = "FATAL";

    private final String severity;
    private final String summary;
    private final String detail;

    
    public TideMessage(String severity, String summary, String detail) {
        this.severity = severity;
        this.summary = summary;
        this.detail = detail;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // write only bean...
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(detail);
        out.writeObject(severity);
        out.writeObject(summary);
    }
}
