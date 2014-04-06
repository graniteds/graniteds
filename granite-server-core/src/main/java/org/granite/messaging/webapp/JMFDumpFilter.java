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
package org.granite.messaging.webapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.granite.config.GraniteConfigListener;
import org.granite.logging.Logger;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.messaging.jmf.SharedContext;
import org.granite.util.ContentType;

/**
 * @author Franck WOLFF
 */
public class JMFDumpFilter implements Filter {

    private static final Logger log = Logger.getLogger(JMFDumpFilter.class);
    
    private SharedContext jmfSharedContext = null;

    public void init(FilterConfig config) throws ServletException {
    	jmfSharedContext = GraniteConfigListener.getDumpSharedContext(config.getServletContext());
    	if (jmfSharedContext == null)
    		throw GraniteConfigListener.newSharedContextNotInitializedException();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

    	if (!ContentType.JMF_AMF.mimeType().equals(request.getContentType())) {
    		log.info("Ignoring request with content-type: " + request.getContentType());
    		chain.doFilter(request, response);
    	}
    	else {
	        DumpRequestWrapper requestWrapper = new DumpRequestWrapper((HttpServletRequest)request);
	        DumpResponseWrapper responseWrapper= new DumpResponseWrapper((HttpServletResponse)response);
	
	        chain.doFilter(requestWrapper, responseWrapper);
    	}
    }

    public void destroy() {
    	jmfSharedContext = null;
    }

    private void dumpBytes(String label, byte[] bytes) throws IOException {
    	final String encoding = "UTF-8";
    	
    	ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
    	PrintStream ps = new PrintStream(baos, true, encoding);
    	
    	JMFDumper dumper = new JMFDumper(new ByteArrayInputStream(bytes), jmfSharedContext, ps);
    	dumper.dump();
    	dumper.close();
    	
        log.info("[JMF %s (%d bytes)]\n%s", label.toUpperCase(), bytes.length, new String(baos.toByteArray(), encoding));
    }

    class DumpRequestWrapper extends HttpServletRequestWrapper {

        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        public DumpRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {

            final InputStream is = getRequest().getInputStream();

            return new ServletInputStream() {
                
            	@Override
                public int read() throws IOException {
            		int b = is.read();
            		if (b != -1)
            			baos.write(b);
                    return b;
                }

				@Override
				public int available() throws IOException {
					return is.available();
				}

				@Override
				public void close() throws IOException {
					is.close();
					
					dumpBytes("request", baos.toByteArray());
				}
            };
        }
    }

    class DumpResponseWrapper extends HttpServletResponseWrapper {

        private final ByteArrayOutputStream baos = new ByteArrayOutputStream(256);

        public DumpResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {

        	final OutputStream os = getResponse().getOutputStream();
        	
            return new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    baos.write(b);
                    os.write(b);
                }

				@Override
				public void close() throws IOException {
					os.close();
					
					dumpBytes("response", baos.toByteArray());
				}
            };
        }
    }
}
