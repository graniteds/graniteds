/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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

import org.jboss.vfs.VirtualFile;


/**
 * @author Franck WOLFF
 */
public class VFS3FileScannedItem extends AbstractScannedItem {

    private final VirtualFile root;
    private final VirtualFile file;
    
    private String relativePath = null;

    
    public VFS3FileScannedItem(Scanner scanner, VFS3FileScannedItem marker, VirtualFile root, VirtualFile file) {
        super(scanner, marker);

        this.root = root;
        this.file = file;
    }

    public long getSize() {
		return file.getSize();
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
	        StringBuffer sb = new StringBuffer();
	        for (VirtualFile f = file; f != null && !root.equals(f); f = f.getParent()) {
	            if (sb.length() > 0)
	                sb.insert(0, '/');
	            sb.insert(0, f.getName());
	        }
	        relativePath = sb.toString();
		}
		return relativePath;
	}

	
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof VFS3FileScannedItem))
            return false;
        return file.equals(((VFS3FileScannedItem)obj).file) && root.equals(((VFS3FileScannedItem)obj).root);
    }

    @Override
    public int hashCode() {
        return root.hashCode() + (31 * file.hashCode());
    }
}
