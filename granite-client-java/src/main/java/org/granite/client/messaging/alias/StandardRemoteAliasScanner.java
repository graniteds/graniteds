/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.messaging.alias;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.messaging.RemoteAliasScanner;
import org.granite.logging.Logger;
import org.granite.scan.ScannedItem;
import org.granite.scan.ScannedItemHandler;
import org.granite.scan.Scanner;
import org.granite.scan.ScannerFactory;

/**
 * @author Franck WOLFF
 */
public class StandardRemoteAliasScanner implements RemoteAliasScanner {
	
	private static final Logger log = Logger.getLogger(StandardRemoteAliasScanner.class);

	@Override
	public Set<Class<?>> scan(Set<String> packageNames) {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		
		Scanner scanner = ScannerFactory.createScanner(new MessagingScannedItemHandler(packageNames, classes), null);
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
		
		MessagingScannedItemHandler(Set<String> packageNames, Set<Class<?>> classes) {
			this.packageNames = new String[packageNames.size()];
			int i = 0;
			for (String packageName : packageNames)
				this.packageNames[i++] = packageName.replace('.', '/') + '/';
			
			this.classes = classes;
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
						RemoteAlias alias = cls.getAnnotation(RemoteAlias.class);
						if (alias != null)
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
