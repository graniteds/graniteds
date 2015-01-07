/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.validation {

	import mx.resources.IResourceManager;
	import mx.resources.ResourceManager;
	
	import org.granite.util.InternalBundle;

	/**
	 * The <code>ValidationMessages</code> class is responsible of loading standard
	 * validation resources bundles. Three language are provided by default: english,
	 * french, german (as in the JSR-303 default implementation) and chinese.
	 * 
	 * <p>
	 * The name of this bundle is "ValidationMessages", add it is possible to override
	 * default resources or to add new ones programatically (see Flex documentation).
	 * Here is a sample of such an override and addition (here at runtime):
	 * </p>
	 * 
	 * <listing>
	 * var validationBundle:ResourceBundle = new ResourceBundle("en_US", ValidationMessages.NAME);
	 * 
	 * // override of the default Size message ("size must be between {min} and {max}").
	 * validationBundle.content["javax.validation.constraints.Size.message"] = "Size should be in [{min}, {max}]";
	 * 
	 * // add a new message.
	 * validationBundle.content["my.validation.SpecialConstraint.message"] = "Value must be special";
	 * 
	 * ResourceManager.getInstance().addResourceBundle(validationBundle);
	 * ResourceManager.getInstance().update();
	 * ResourceManager.getInstance().localeChain = ["en_US"];
	 * </listing>
	 * 
	 * @author Franck WOLFF
	 * 
	 * @see http://livedocs.adobe.com/flex/3/html/l10n_2.html
	 */
	public class ValidationMessages extends InternalBundle {
		
		/**
		 * A constant defining the name of the validation resource bundle.
		 */
		public static const NAME:String = "ValidationMessages";

		[Embed(source="resources/ValidationMessages.xml", mimeType="application/octet-stream")]
		private static var _validationMessages:Class;

		[Embed(source="resources/ValidationMessages_fr.xml", mimeType="application/octet-stream")]
		private static var _validationMessages_fr:Class;
		
		[Embed(source="resources/ValidationMessages_de.xml", mimeType="application/octet-stream")]
		private static var _validationMessages_de:Class;
		
		[Embed(source="resources/ValidationMessages_ch.xml", mimeType="application/octet-stream")]
		private static var _validationMessages_ch:Class;
		
		private static var _instance:ValidationMessages;
		
		/**
		 * @private
		 */
		function ValidationMessages(restrictor:Restrictor) {
			super(NAME);
			
			if (restrictor == null)
				throw new Error("Illegal constructor access: use the getInstance method.");
			
			addBundle("en", XML(new _validationMessages), true);
			addBundle("fr", XML(new _validationMessages_fr));
			addBundle("de", XML(new _validationMessages_de));
			addBundle("ch", XML(new _validationMessages_ch));
		}
		
		/**
		 * Returns a unique instance of the <code>ValidationMessages</code> class.
		 * 
		 * @return a unique instance of the <code>ValidationMessages</code> class.
		 */
		public static function getInstance():ValidationMessages {
			if (_instance == null)
				_instance = new ValidationMessages(new Restrictor());
			return _instance;
		}
	}
}
class Restrictor {}