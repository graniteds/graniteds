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
package org.granite.client.android.platform;

import org.granite.client.android.messaging.transport.LoopjTransport;
import org.granite.client.android.scan.DexClassScanner;
import org.granite.client.configuration.ClassScanner;
import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.platform.Platform;
import org.granite.messaging.reflect.Reflection;

import android.content.Context;

/**
 * @author Franck WOLFF
 */
public class AndroidPlatform extends Platform {

	public AndroidPlatform() {
		super(new AndroidReflection(null));
	}

	public AndroidPlatform(ClassLoader reflectionClassLoader) {
		super(new AndroidReflection(reflectionClassLoader));
	}

	public AndroidPlatform(Reflection reflection) {
		super(reflection);
	}

	@Override
	public ClassScanner newClassScanner() {
		if (!(context instanceof Context))
			throw new IllegalArgumentException("context must be an Android application context: " + context);
		
		return new DexClassScanner((Context)context);
	}
	
	@Override
	public Configuration newConfiguration() {
		return null;
	}
	
    @Override
    public Transport newRemotingTransport() {
        if (!(context instanceof Context))
            throw new IllegalArgumentException("context must be an Android application context: " + context);

        return new LoopjTransport((Context)context);
    }
}
