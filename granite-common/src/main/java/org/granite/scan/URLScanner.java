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
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class URLScanner implements Scanner {
	
	private static final Logger log = Logger.getLogger(URLScanner.class);

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    private final List<ScannedItemHandler> handlers = new ArrayList<ScannedItemHandler>();
    private final String marker;
    private final ClassLoader loader;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors.

    public URLScanner(ScannedItemHandler handler) {
        this(handler, null, Thread.currentThread().getContextClassLoader());
    }

    public URLScanner(ScannedItemHandler handler, String marker) {
        this(handler, marker, Thread.currentThread().getContextClassLoader());
    }

    public URLScanner(ScannedItemHandler handler, ClassLoader loader) {
        this(handler, null, loader);
    }

    public URLScanner(ScannedItemHandler handler, String marker, ClassLoader loader) {
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
        Set<String> paths = new HashSet<String>();

        if (marker == null) {
        	if (!(loader instanceof URLClassLoader))
        		throw new RuntimeException("ClassLoader used with no marker should be a URLClassLoader: " + loader);
        	
            for (URL url : ((URLClassLoader)loader).getURLs()) {
                String urlPath = url.getFile();
                if (urlPath.endsWith("/"))
                    urlPath = urlPath.substring(0, urlPath.length() - 1);
                paths.add(urlPath);
            }
        }
        else {
            for (Enumeration<URL> urlEnum = loader.getResources(marker); urlEnum.hasMoreElements(); ) {
                String urlPath = URLDecoder.decode(urlEnum.nextElement().getFile(), "UTF-8");

                if (urlPath.startsWith("file:"))
                    urlPath = urlPath.substring(5);

                // Jars.
                if (urlPath.indexOf('!') > 0)
                    urlPath = urlPath.substring(0, urlPath.indexOf('!'));
                // Regular directories.
                else {
                    File dirOrArchive = new File(urlPath);

                    String[] tokens = marker.split("\\Q/\\E", -1);
                    for (int i = 0; i < tokens.length; i++)
                        dirOrArchive = dirOrArchive.getParentFile();

                    urlPath = dirOrArchive.getPath();
                }

                paths.add(urlPath);
            }
        }

        for (String urlPath : paths) {
        	 urlPath = urlPath.replace("%20", " "); // Fix for GDS-1288
        	 File file = new File(urlPath);
            if (file.isDirectory())
                handleDirectory(file, file);
            else
                handleArchive(file);
        }
    }


    public void handleArchive(File file) throws ZipException, IOException {
    	log.debug("Scanning archive %s", file != null ? file.getName() : "");
    	ZipFile zip = null;
    	try {
        	zip = new ZipFile(file);
    	}
    	catch (IOException e) {
    		log.debug(e, "Ignored non archive or corrupt file");
    		return;
    	}
        
        ZipScannedItem markerItem = null;
        if (marker != null) {
            ZipEntry markerEntry = zip.getEntry(marker);
            markerItem = new ZipScannedItem(this, null, zip, markerEntry);
            for (ScannedItemHandler handler : handlers) {
            	boolean skip = handler.handleMarkerItem(markerItem);
            	if (skip)
            		return;
            }
        }

        for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory() && (markerItem == null || !markerItem.getEntry().getName().equals(entry.getName()))) {
                for (ScannedItemHandler handler : handlers)
                	handler.handleScannedItem(new ZipScannedItem(this, markerItem, zip, entry));
            }
        }
    }

    public void handleDirectory(File root, File path) {
    	log.debug("Scanning directory %s, %s", root != null ? root.getName() : "", path != null ? path.getName() : "");
        FileScannedItem markerItem = null;
        if (marker != null) {
            File markerFile = new File(root, marker);
            markerItem = new FileScannedItem(this, null, root, markerFile);
            for (ScannedItemHandler handler : handlers) {
            	boolean skip = handler.handleMarkerItem(markerItem);
            	if (skip)
            		return;
            }
        }
        handleDirectory(markerItem, root, path);
    }

    public void handleDirectory(FileScannedItem markerItem, File root, File path) {
        for (File child : path.listFiles()) {
            if (child.isDirectory())
                handleDirectory(markerItem, root, child);
            else if (markerItem == null || !markerItem.getFile().equals(child)) {
                for (ScannedItemHandler handler : handlers)
                	handler.handleScannedItem(new FileScannedItem(this, markerItem, root, child));
            }
        }
    }
}
