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
package org.granite.messaging.webapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

import org.granite.logging.Logger;
import org.granite.util.ServletParams;

/**
 * @author Franck WOLFF
 */
public class DumpFilter implements Filter {

    private static final Logger log = Logger.getLogger(DumpFilter.class);
    private static final String HEXS = "0123456789ABCDEF";
    private static final String DUMP_DIR = "dumpDir";

    File dumpDir = null;

    public void init(FilterConfig config) throws ServletException {
        String dumpDirString = ServletParams.get(config, DUMP_DIR, String.class, null);
        if (dumpDirString != null) {
            File dumpDir = new File(dumpDirString);
            if (!dumpDir.exists() || !dumpDir.isDirectory() || !dumpDir.canWrite())
                log.warn("Ignoring dump directory (is it a writable directory?): %s", dumpDir);
            else
                this.dumpDir = dumpDir;
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        DumpRequestWrapper requestWrapper = new DumpRequestWrapper((HttpServletRequest)request);
        DumpResponseWrapper responseWrapper= new DumpResponseWrapper((HttpServletResponse)response);

        dumpBytes("request", requestWrapper.getBytes());
        chain.doFilter(requestWrapper, responseWrapper);
        dumpBytes("response", responseWrapper.getBytes());
    }

    public void destroy() {
        dumpDir = null;
    }

    private void dumpBytes(String label, byte[] bytes) {
        StringBuilder hexSb = new StringBuilder();
        StringBuilder charSb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            if (hexSb.length() > 0) {
                hexSb.append(' ');
                charSb.append(' ');
            }
            hexSb.append(HEXS.charAt(b >> 4)).append(HEXS.charAt(b & 0x0F));
            if (b >= 0x20 && b <= 0x7e)
                charSb.append(' ').append((char)b);
            else
                charSb.append("##");
        }

        log.info("[RAW %s] {\n%s\n%s\n}", label.toUpperCase(), hexSb.toString(), charSb.toString());

        if (dumpDir != null) {
            File file = new File(dumpDir.getPath() + File.separator + label + "_" + System.currentTimeMillis() + ".amf");
            for (int i = 1; i < 100 && file.exists(); i++)
                file = new File(file.getAbsolutePath() + "." + i);

            OutputStream os = null;
            try {
                os = new FileOutputStream(file);
                os.write(bytes);
            } catch (Exception e) {
                log.error(e, "Could not write dump file: %s", file);
            } finally {
                if (os != null) try {
                    os.close();
                } catch (Exception e) {
                }
            }
        }
    }

    class DumpRequestWrapper extends HttpServletRequestWrapper {

        private byte[] bytes = null;

        public DumpRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);

            setCharacterEncoding("UTF-8");

            InputStream is = null;
            try {
                is = request.getInputStream();

                ByteArrayOutputStream out = new ByteArrayOutputStream(128);
                for (int b = is.read(); b != -1; b = is.read())
                    out.write(b);

                this.bytes = out.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {

            final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

            return new ServletInputStream() {
                @Override
                public int read() throws IOException {
                    return bais.read();
                }
            };
        }

        public byte[] getBytes() {
            return bytes;
        }
    }

    class DumpResponseWrapper extends HttpServletResponseWrapper {

        private ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
        private ServletOutputStream out = null;

        public DumpResponseWrapper(HttpServletResponse response) throws IOException {
            super(response);
            this.out = response.getOutputStream();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {

            return new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    baos.write(b);
                    out.write(b);
                }
            };
        }

        public byte[] getBytes() {
            return baos.toByteArray();
        }
    }
}
