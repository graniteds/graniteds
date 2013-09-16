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
/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 3 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.scan;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;

/**
 * @author Franck WOLFF
 */
public class VFS3Scanner implements Scanner {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    private final List<ScannedItemHandler> handlers = new ArrayList<ScannedItemHandler>();
    private final String marker;
    private final ClassLoader loader;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors.

    public VFS3Scanner(ScannedItemHandler handler) {
        this(handler, null, Thread.currentThread().getContextClassLoader());
    }

    public VFS3Scanner(ScannedItemHandler handler, String marker) {
        this(handler, marker, Thread.currentThread().getContextClassLoader());
    }

    public VFS3Scanner(ScannedItemHandler handler, ClassLoader loader) {
        this(handler, null, loader);
    }

    public VFS3Scanner(ScannedItemHandler handler, String marker, ClassLoader loader) {
        this.marker = marker;
        this.handlers.add(handler);
        this.loader = loader;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties.

    public String getMarker() {
        return marker;
    }

    public void addHandler(ScannedItemHandler handler) {
    	if (!handlers.contains(handler))
    		handlers.add(handler);
    }

    public void addHandlers(Collection<ScannedItemHandler> handlers) {
    	for (ScannedItemHandler handler : handlers)
    		addHandler(handler);
    }
    
    public ClassLoader getLoader() {
        return loader;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Scan methods.

    public void scan() throws IOException {
        if (marker == null) {
        	if (!(loader instanceof URLClassLoader))
        		throw new RuntimeException("ClassLoader used with no marker should be a URLClassLoader: " + loader);
        	
            for (URL url : ((URLClassLoader)loader).getURLs()) {
                VirtualFile root = getRoot(url, 1);
                if (root != null)
                	handleRoot(null, root);
            }
        }
        else {
            for (Enumeration<URL> urlEnum = loader.getResources(marker); urlEnum.hasMoreElements(); ) {
            	URL url = urlEnum.nextElement();
                VirtualFile root = getRoot(url, marker.lastIndexOf('/') > 0 ? 2 : 1);
                if (root != null)
                	handleRoot(url, root);
            }
        }
    }

    
    protected void handleRoot(URL markerUrl, VirtualFile root) throws IOException {
    	VFS3FileScannedItem markerItem = null;
    	
    	if (markerUrl != null) {
    		try {
	    		VirtualFile markerFile = VFS.getChild(markerUrl);
	    		markerItem = new VFS3FileScannedItem(this, null, markerFile, markerFile);
	            for (ScannedItemHandler handler : handlers) {
	            	boolean skip = handler.handleMarkerItem(markerItem);
	            	if (skip)
	            		return;
	            }
    		}
        	catch (URISyntaxException e) {
        		IOException ex = new IOException("Invalid URI " + markerUrl);
        		ex.initCause(e);
        		throw ex;
        	}
    	}
    	
    	if (root.isFile()) {
            for (ScannedItemHandler handler : handlers)
            	handler.handleScannedItem(new VFS3FileScannedItem(this, markerItem, root, root));
    	}
    	else {
    		String rootPathName = root.getPathName();
    		int rootPathNameLength = rootPathName.length();
    		List<VirtualFile> children = root.getChildrenRecursively();
    		for (VirtualFile child : children) {
    			if (child.isFile()) {
    				String name = child.getPathName();
    				// move past '/'
    				int length = rootPathNameLength;
    				if (name.charAt(length) == '/')
    					length++;
    	            for (ScannedItemHandler handler : handlers)
    	            	handler.handleScannedItem(new VFS3FileScannedItem(this, markerItem, root, child));
    			}
    		}
    	}
    }
    

    protected static VirtualFile getRoot(URL url, int parentDepth) throws IOException {
    	String urlString = url.toString();
    	// TODO - this should go away once we figure out why -exp.war is part of CL resources
    	if (urlString.startsWith("vfs") == false)
    		return null;

    	int p = urlString.indexOf(":");
    	String file = urlString.substring(p + 1);
    	URL vfsurl = null;
    	String relative;
    	File fp = new File(file);

    	if (fp.exists()) {
    		vfsurl = fp.getParentFile().toURI().toURL();
    		relative = fp.getName();
    	}
    	else {
    		File curr = fp;
    		relative = fp.getName();
    		while ((curr = curr.getParentFile()) != null) {
    			if (curr.exists()) {
    				vfsurl = curr.toURI().toURL();
    				break;
    			}
    			
    			relative = curr.getName() + "/" + relative;
    		}
    	}

    	try {
	    	VirtualFile top = VFS.getChild(vfsurl);
	    	top = top.getChild(relative);
	    	while (parentDepth > 0) {
	    		if (top == null)
	    			throw new IllegalArgumentException("Null parent: " + vfsurl + ", relative: " + relative);
	    		top = top.getParent();
	    		parentDepth--;
	    	}
	
	    	return top;
    	}
    	catch (URISyntaxException e) {
    		IOException ex = new IOException("Invalid URI " + url);
    		ex.initCause(e);
    		throw ex;
    	}
    }
}
