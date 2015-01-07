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
package org.granite.config.api.internal;

import org.granite.config.api.Configuration;
/**
 * @author <a href="mailto:gembin@gmail.com">gembin@gmail.com</a>
 * @since 1.1.0
 */
public class ConfigurationImpl implements Configuration {
	
	private String flexCfgFile;
	private String flexCfgPropFile;
	private String graniteCfgFile;
	private String graniteCfgPropFile;

	public void setFlexServicesConfig(String flexCfgFile) {
		this.flexCfgFile = flexCfgFile;
	}

	public void setFlexServicesConfigProperties(String flexCfgPropFile) {
		this.flexCfgPropFile = flexCfgPropFile;
	}

	public void setGraniteConfig(String grainteCfgFile) {
		this.graniteCfgFile = grainteCfgFile;
	}

	public void setGraniteConfigProperties(String graniteCfgPropFile) {
		this.graniteCfgPropFile = graniteCfgPropFile;
	}

	public String getFlexServicesConfig() {
		return flexCfgFile;
	}

	public String getFlexServicesConfigProperties() {
		return flexCfgPropFile;
	}

	public String getGraniteConfigProperties() {
		return graniteCfgPropFile;
	}

	public String getGraniteConfig() {
		return graniteCfgFile;
	}

}
