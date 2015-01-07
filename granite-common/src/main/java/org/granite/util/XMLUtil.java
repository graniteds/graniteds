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
package org.granite.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * @author Franck WOLFF
 */
public interface XMLUtil {

	public Document newDocument();
	
	public Document newDocument(String root);
	
	public Document getDocument(Node node);

	public Element newElement(Node parent, String name);

	public Element newElement(Node parent, String name, String value);
	
	public String getNormalizedValue(Node node);
	
	public String setValue(Node node, String value);
	
    public Document buildDocument(String xml);
    
	public Document loadDocument(InputStream input) throws IOException, SAXException;
	
	public Document loadDocument(InputStream input, EntityResolver resolver, ErrorHandler errorHandler) throws IOException, SAXException;
    
	public void saveDocument(Document document, OutputStream output);
	
    public String toString(Document doc);
    
	public String toNodeString(Node node);
	
	public Node selectSingleNode(Object context, String expression);
	
	public List<Node> selectNodeSet(Object context, String expression);

}
