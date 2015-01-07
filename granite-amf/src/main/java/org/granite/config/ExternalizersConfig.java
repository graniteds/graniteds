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
package org.granite.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.granite.messaging.amf.io.util.ActionScriptClassDescriptor;
import org.granite.messaging.amf.io.util.JavaClassDescriptor;
import org.granite.messaging.amf.io.util.externalizer.Externalizer;
import org.granite.util.XMap;

/**
 * @author Franck WOLFF
 */
public interface ExternalizersConfig extends Config {
	    
    public Map<String, String> getInstantiators();

    public String getInstantiator(String type);

    public XMap getExternalizersConfiguration();

	public void setExternalizersConfiguration(XMap externalizersConfiguration);

	public Externalizer getExternalizer(String type);
	
    public void registerExternalizer(Externalizer externalizer);

    public Externalizer setExternalizersByType(String type, String externalizerType);

    public String putExternalizersByInstanceOf(String instanceOf, String externalizerType);

    public String putExternalizersByAnnotatedWith(String annotatedWith, String externalizerType);
	
	public Map<String, Externalizer> getExternalizersByType();
	
	public Map<String, String> getExternalizersByInstanceOf();
	
	public Map<String, String> getExternalizersByAnnotatedWith();
	
	public List<Externalizer> getScannedExternalizers();
	
	public Class<? extends ActionScriptClassDescriptor> getActionScriptDescriptor(String type);

    public Map<String, Class<? extends ActionScriptClassDescriptor>> getAs3DescriptorsByType();

    public Map<String, String> getAs3DescriptorsByInstanceOf();
    
    
    public ConcurrentMap<String, JavaClassDescriptor> getJavaDescriptorsCache();
    
    public Class<? extends JavaClassDescriptor> getJavaDescriptor(String type);

    public Map<String, Class<? extends JavaClassDescriptor>> getJavaDescriptorsByType();

    public Map<String, String> getJavaDescriptorsByInstanceOf();
}
