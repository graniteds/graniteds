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

package org.granite.generator.gsp;

import java.io.FileNotFoundException;
import java.net.URI;

import org.granite.generator.Input;
import org.granite.generator.Listener;
import org.granite.generator.Output;
import org.granite.generator.TemplateUri;
import org.granite.generator.Transformer;
import org.granite.generator.exception.TemplateUriException;
import org.granite.util.URIUtil;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractGroovyTransformer<I extends Input<?>, O extends Output<?>, C extends GroovyConfiguration>
	extends Transformer<I, O, C> {

	public AbstractGroovyTransformer() {
		super();
	}

	public AbstractGroovyTransformer(GroovyConfiguration config, Listener listener) {
		super(config, listener);
	}

	protected GroovyTemplateFactory getTemplateFactory() {
		return getConfig().getGroovyTemplateFactory();
	}

    protected GroovyTemplate getTemplate(TemplateUri templateUri) throws TemplateUriException {
    	return getTemplate(templateUri.getUri(), templateUri.isBase());
    }

    protected GroovyTemplate getTemplate(String path, boolean base) throws TemplateUriException {
    	GroovyTemplateFactory factory = getTemplateFactory();
        try {
        	path = URIUtil.normalize(path);
        	URI uri = new URI(path);
        	String schemeSpecificPart = uri.getSchemeSpecificPart();
        	if (schemeSpecificPart == null || schemeSpecificPart.length() == 0)
        		throw new FileNotFoundException("Template path cannot be empty: " + uri);
        	
        	if (URIUtil.isFileURI(uri) && !URIUtil.isAbsolute(uri)) {
        		URI parent = getConfig().getWorkingDirectory().toURI();
        		if (parent != null)
        			uri = parent.resolve(uri.getRawSchemeSpecificPart());
        	}
            return factory.getTemplate(uri, base);
        } catch (Exception e) {
            throw new TemplateUriException(path, e);
        }
    }
}
