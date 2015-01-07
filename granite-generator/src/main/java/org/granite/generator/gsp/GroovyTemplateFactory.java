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
package org.granite.generator.gsp;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Franck WOLFF
 */
public class GroovyTemplateFactory {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    private final Map<URI, GroovyTemplate> templatesMap = new HashMap<URI, GroovyTemplate>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    public GroovyTemplateFactory() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Checking.

    public boolean isOutdated() {
    	for (GroovyTemplate template : templatesMap.values()) {
    		if (template.isOutdated())
    			return true;
    	}
        return false;
    }

    public void cleanOutdated() {
    	List<URI> outdated = new ArrayList<URI>();
    	for (GroovyTemplate template : templatesMap.values()) {
    		if (template.isOutdated())
    			outdated.add(template.getUri());
    	}
    	for (URI uri : outdated)
    		templatesMap.remove(uri);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Cleanup.

    public void clear() {
        templatesMap.clear();
    }

    public void clear(URI uri) {
        templatesMap.remove(uri);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Template loading.

    public GroovyTemplate getTemplate(URI uri, boolean baseTemplate) {
        return getTemplate(uri, baseTemplate, Charset.defaultCharset());
    }

    public GroovyTemplate getTemplate(URI uri, boolean baseTemplate, Charset charset) {
        if (uri == null || charset == null)
            throw new IllegalArgumentException("uri and charset cannot be null");

        GroovyTemplate template = templatesMap.get(uri);

        if (template == null) {
            template = new GroovyTemplate(uri, baseTemplate, charset);
            templatesMap.put(uri, template);
        }

        return template;
    }
}
