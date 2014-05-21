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
package org.granite.client.scan;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.granite.client.configuration.ClassScanner;
import org.granite.logging.Logger;
import org.granite.scan.ScannedItem;
import org.granite.scan.ScannedItemHandler;
import org.granite.scan.Scanner;
import org.granite.scan.ScannerFactory;

/**
 * @author Franck WOLFF
 */
public class StandardClassScanner implements ClassScanner {
	
	private static final Logger log = Logger.getLogger(StandardClassScanner.class);

	@Override
	public Set<Class<?>> scan(Set<String> packageNames, Class<? extends Annotation> annotationClass) {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		
		Scanner scanner = ScannerFactory.createScanner(new MessagingScannedItemHandler(packageNames, classes, annotationClass), null);
        try {
            scanner.scan();
        }
        catch (Exception e) {
            log.error(e, "Could not scan classpath for @RemoteAlias");
        }
		
        return classes;
	}
	
	class MessagingScannedItemHandler implements ScannedItemHandler {

		final String[] packageNames;
		final Set<Class<?>> classes;
		final Class<? extends Annotation> annotationClass;
		
		MessagingScannedItemHandler(Set<String> packageNames, Set<Class<?>> classes, Class<? extends Annotation> annotationClass) {
			this.packageNames = new String[packageNames.size()];
			int i = 0;
			for (String packageName : packageNames)
				this.packageNames[i++] = packageName.replace('.', '/') + '/';
			
			this.classes = classes;
			this.annotationClass = annotationClass;
		}
		
		@Override
		public boolean handleMarkerItem(ScannedItem item) {
			return false;
		}

		@Override
		public void handleScannedItem(ScannedItem item) {
			if ("class".equals(item.getExtension())) {
				boolean scan = false;
				
				String path = item.getRelativePath();
				for (String packageName : packageNames) {
					if (path.startsWith(packageName)) {
						scan = true;
						break;
					}
				}
				
				if (scan) {
					try {
						Class<?> cls = item.loadAsClass();
						if (cls.isAnnotationPresent(annotationClass))
							classes.add(cls);
					}
					catch (ClassFormatError e) {
					}
					catch (ClassNotFoundException e) {
					}
					catch (IOException e) {
						log.error(e, "Could not load class: %s", item);
					}
				}
			}
		}
	}
}
