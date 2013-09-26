/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.generator.gsp;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.granite.generator.Template;
import org.granite.generator.exception.TemplateCompilationException;
import org.granite.generator.exception.TemplateExecutionException;
import org.granite.generator.exception.TemplateParsingException;
import org.granite.generator.gsp.token.Token;
import org.granite.util.URIUtil;

/**
 * @author Franck WOLFF
 */
public class GroovyTemplate implements Template {

    private final URI uri;
    private final boolean base;
    private final Charset charset;

    private String source = null;
    private Script script = null;
    private long lastModified = -1L;

    GroovyTemplate(URI uri, boolean base, Charset charset) {
        if (uri == null || charset == null)
            throw new IllegalArgumentException("uri and charset cannot be null");

        this.uri = uri;
        this.base = base;
        this.charset = charset;
    }
    
    protected void reset(boolean cleanSource) {
    	if (cleanSource)
    		source = null;
        script = null;
        lastModified = -1L;
    }
    protected void reset() {
    	reset(true);
    }

    @Override
	public URI getUri() {
        return uri;
    }

    public long getLastModified() {
		return lastModified;
	}
    
    protected long readLastModified() {
    	long readLastModified = -1L;
    	if ("file".equals(uri.getScheme())) {
    		readLastModified = new File(uri).lastModified();
    		if (readLastModified == 0L)
    			readLastModified = -1L;
    	}
    	else
    		readLastModified = Long.MAX_VALUE;
    	return readLastModified;
    }
    
    public boolean isOutdated() {
    	long newLastModified = readLastModified();
    	return script != null && (newLastModified == -1 || lastModified < newLastModified);
    }

	@Override
	public boolean isBase() {
		return base;
	}

	@Override
	public String getMarkup() {
        try {
            return URIUtil.getContentAsString(uri);
        } catch (Exception e) {
            return "Could not load uri content: " + uri + " (" + e.toString() + ")";
        }
    }

    @Override
	public String getSource() {
        return source;
    }

    @Override
	public void compile() throws IOException, TemplateParsingException, TemplateCompilationException {
    	reset();
    	
        InputStream is = null;
        try {
            is = URIUtil.getInputStream(uri, getClass().getClassLoader());
            
            lastModified = readLastModified();

            Reader reader = new BufferedReader(new InputStreamReader(is, charset));

            Parser parser = new Parser();
            List<Token> tokens = parser.parse(reader);

            GroovyRenderer renderer = new GroovyRenderer();
            source = renderer.renderSource(tokens);

            // Warning: the classloader for javax annotations is defined in BuilderParentClassLoader
            // in the case of the Eclipse builder
            GroovyShell shell = new GroovyShell(Thread.currentThread().getContextClassLoader());
            script = shell.parse(source);
        } catch (ParseException e) {
            throw new TemplateParsingException(this, "Could not parse template: " + uri, e);
        } catch (CompilationFailedException e) {
            throw new TemplateCompilationException(this, "Could not compile template: " + uri, e);
        } finally {
        	if (script == null)
        		reset(false);
            
        	if (is != null)
                is.close();
        }
    }

    @Override
	public void execute(Map<String, Object> bindings, Writer out)
    	throws IOException, TemplateParsingException, TemplateCompilationException, TemplateExecutionException {
    	
    	if (script == null)
    		compile();
        
    	try {
            Binding binding = (bindings == null ? new Binding() : new Binding(bindings));
            Script scriptInstance = InvokerHelper.createScript(script.getClass(), binding);
            scriptInstance.setProperty("out", out);
            scriptInstance.run();
            out.flush();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new TemplateExecutionException(this, "Could not execute template: " + uri, e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof GroovyTemplate))
            return false;
        return uri.equals(((GroovyTemplate)obj).uri);
    }
    
    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
