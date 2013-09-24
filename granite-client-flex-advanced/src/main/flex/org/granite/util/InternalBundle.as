/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.util {

	import flash.utils.Dictionary;
	
	import mx.resources.ResourceManager;
	import mx.utils.StringUtil;

	/**
	 * Utility (abstract) class for embedded resources bundles loading and resource
	 * lookup.
	 * 
	 * <p>
	 * Bundles loaded by this class must be in the Java XML properties format, ie:
	 * </p>
	 * 
	 * <listing>
	 * &lt;properties&gt;
	 *     &lt;entry key="hello.world"&gt;Salut tout le monde!&lt;/entry&gt;
	 * &lt;/properties&gt;
	 * </listing>
	 * 
	 * @author Franck WOLFF
	 */
	public class InternalBundle {

		private var _bundleName:String;
		private var _defaultBundle:Dictionary;
		private var _bundles:Dictionary = new Dictionary();
		
		/**
		 * Constructs a new <code>InternalBundle</code> instance.
		 * 
		 * @param bundleName the name of this resources bundle, that will be
		 * 		used for standard Flex <code>ResourceManager</code> looking.
		 * 
		 * @see mx.resources.ResourceManager
		 * @see #getString
		 */
		function InternalBundle(bundleName:String) {
			if (bundleName == null)
				throw new Error("Bundle name cannot be null");
			_bundleName = bundleName;
		}
		
		/**
		 * Add XML resources properties for the given locale.
		 * 
		 * @param locale the locale in which the properties are given (eg: "fr" or "fr_FR").
		 * @param properties a XML object that contains resources properties for the locale.
		 * @param defaultBundle <code>true</code> if this bundle should be used as a fallback
		 * 		bundle when a resource for a given locale cannot be found. Default is
		 * 		<code>false</code>.
		 */
		protected function addBundle(locale:String, properties:XML, defaultBundle:Boolean = false):void {
			if (locale == null)
				throw new Error("Locale cannot be null");
			_bundles[locale] = parseXMLBundle(properties);
			if (defaultBundle)
				_defaultBundle = _bundles[locale];
		}
		
		/**
		 * Look for a resource named <code>resourceName</code> in the loaded Flex application
		 * bundles (with the bundle name provided at creation time) and, if it cannot be found,
		 * look for it in this internal bundle properties.
		 * 
		 * <p>
		 * Locale resolution with internal bundles is performed by using the usual Java algorithm:
		 * if "fr_FR" is passed as the locale (or if it appears in the current locale chain), this
		 * class will first try to find a bundle for this exact locale (as passed to the
		 * <code>addBundle</code> method); if no such bundle can be found, it will try to find a
		 * bundle for the less restrictive "fr" locale; if it fails again, it will try to use the
		 * default bundle.
		 * </p>
		 * 
		 * <p>
		 * If a <code>parameters</code> array is passed to this method, the parameters in it
		 * are converted to Strings and then substituted, in order, for the placeholders
		 * "{0}", "{1}", and so on, in the string before it is returned.
		 * </p>
		 * 
		 * <p>
		 * If the specified resource is not found, this method returns null.
		 * </p>
		 * 
		 * @param resourceName the name of a resource in the resource bundle.
		 * @param parameters an Array of parameters that are substituted for the placeholders.
		 * 		Each parameter is converted to a String with the toString() method before
		 * 		being substituted.
		 * @param locale a specific locale to be used for the lookup, or null to search all
		 * 		locales in the localeChain. This parameter is optional and defaults to null; you
		 * 		should seldom need to specify it. 
		 * 
		 * @return the resource value, or null if it is not found. 
		 */
		public function getString(resourceName:String, parameters:Array  = null, locale:String = null):String {
			var value:String = ResourceManager.getInstance().getString(_bundleName, resourceName, parameters, locale);
			
			if (value != null)
				return value;
			
			var localeChain:Array = (locale == null ? ResourceManager.getInstance().localeChain : [locale]),
				localeInChain:String,
				bundle:Dictionary,
				tokens:Array;
			
			for each (localeInChain in localeChain) {
				bundle = _bundles[localeInChain];
				if (bundle != null)
					return substitute(bundle[resourceName], parameters);
			}
			
			for each (localeInChain in localeChain) {
				if (localeInChain.indexOf('_') != -1) {
					tokens = localeInChain.split('_');
					tokens.pop();
					while (tokens.length > 0) {
						bundle = _bundles[tokens.join('_')];
						if (bundle != null) {
							_bundles[localeInChain] = bundle;
							return substitute(bundle[resourceName], parameters);
						}
						tokens.pop();
					}
				}
			}
			
			if (_defaultBundle != null)
				return substitute(_defaultBundle[resourceName], parameters);
			
			return null;
		}
		
		/**
		 * @private
		 */
		private static function substitute(message:String, parameters:Array):String {
			if (message == null)
				return null;
			if (parameters == null || parameters.length == 0)
				return message;
			return StringUtil.substitute(message, parameters);
		}
		
		/**
		 * @private
		 */
		private static function parseXMLBundle(properties:XML):Dictionary {
			var bundle:Dictionary = new Dictionary(),
				entry:XML;
			for each (entry in properties.entry)
				bundle[entry.@key.toString()] = entry.toString();
			return bundle;
		}
	}
}