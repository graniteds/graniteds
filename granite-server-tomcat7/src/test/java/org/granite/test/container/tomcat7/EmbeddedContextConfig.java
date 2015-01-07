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
package org.granite.test.container.tomcat7;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by william on 30/09/13.
 */
public class EmbeddedContextConfig extends ContextConfig {

    /**
     * Initialize the context config so to disable processing of the default
     * global web.xml.  As an embedded container we lack the stock config file
     * compliment.
     */
    public EmbeddedContextConfig() {
        super();

        setDefaultWebXml(Constants.NoDefaultWebXml);
    }

    @Override
    protected synchronized void beforeStart() {
        super.beforeStart();

        ((StandardContext)context).setJ2EEServer("Test-" + UUID.randomUUID().toString());
        Tomcat.initWebappDefaults(context);
    };

    /**
     * Override to assign an internal field that will trigger the removal
     * of the unpacked WAR when the context is closed.
     */
    @Override
    protected void fixDocBase() throws IOException {
        super.fixDocBase();
        // If this field is not null, the unpacked WAR is removed when
        // the context is closed. This is normally used by the antiLocking
        // feature, though it should have been the normal behavior, at
        // least for an embedded container.
        originalDocBase = context.getDocBase();
    }
}