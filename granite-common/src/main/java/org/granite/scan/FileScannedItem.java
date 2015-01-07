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
package org.granite.scan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Franck WOLFF
 */
public class FileScannedItem extends AbstractScannedItem {

    private final File root;
    private final File file;

    private String relativePath = null;

    public FileScannedItem(Scanner scanner, FileScannedItem marker, File root, File file) {
        super(scanner, marker);

        this.root = root;
        this.file = file;
    }

    public File getRoot() {
        return root;
    }

    public File getFile() {
        return file;
    }

    public long getSize() {
        return file.length();
    }

    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    public String getName() {
        return file.getName();
    }

    public String getRelativePath() {
        if (relativePath == null) {
            StringBuffer sb = new StringBuffer();
            for (File f = file; f != null && !root.equals(f); f = f.getParentFile()) {
                if (sb.length() > 0)
                    sb.insert(0, '/');
                sb.insert(0, f.getName());
            }
            relativePath = sb.toString();
        }
        return relativePath;
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof FileScannedItem))
            return false;
        return file.equals(((FileScannedItem)obj).file) && root.equals(((FileScannedItem)obj).root);
    }

    @Override
    public int hashCode() {
        return root.hashCode() + (31 * file.hashCode());
    }
}
