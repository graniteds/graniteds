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
package org.granite.scan;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.virtual.VirtualFile;


/**
 * @author Franck WOLFF
 */
public class VFSFileScannedItem extends AbstractScannedItem {

    private final VirtualFile root;
    private final VirtualFile file;
    
    private String relativePath = null;

    
    public VFSFileScannedItem(Scanner scanner, VFSFileScannedItem marker, VirtualFile root, VirtualFile file) {
        super(scanner, marker);

        this.root = root;
        this.file = file;
    }

    public long getSize() {
    	try {
    		return file.getSize();
    	}
    	catch (IOException e) {
    		throw new RuntimeException("Could not get size fo file " + file, e);
    	}
    }

    public InputStream getInputStream() throws IOException {
        return file.openStream();
    }

    public String getName() {
        return file.getName();
    }
    
    public String getAbsolutePath() {
    	return file.getPathName();
    }

	public String getRelativePath() {
		if (relativePath == null) {
			try {
		        StringBuffer sb = new StringBuffer();
		        for (VirtualFile f = file; f != null && !root.equals(f); f = f.getParent()) {
		            if (sb.length() > 0)
		                sb.insert(0, '/');
		            sb.insert(0, f.getName());
		        }
		        relativePath = sb.toString();
			}
			catch (IOException e) {
				throw new RuntimeException("Could not get path for file " + file, e);
			}
		}
		return relativePath;
	}

	
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof VFSFileScannedItem))
            return false;
        return file.equals(((VFSFileScannedItem)obj).file) && root.equals(((VFSFileScannedItem)obj).root);
    }

    @Override
    public int hashCode() {
        return root.hashCode() + (31 * file.hashCode());
    }
}
