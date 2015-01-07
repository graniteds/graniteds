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
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Franck WOLFF
 */
public class StdXMLUtil implements XMLUtil {

	protected static final String TO_STRING_XSL = 
		"<?xml version='1.0' encoding='UTF-8'?>" +
		"<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>" +
		"    <xsl:strip-space elements='*'/>" +
		"    <xsl:template match='/'>" +
		"        <xsl:copy-of select='*'/>" +
		"    </xsl:template>" +
		"</xsl:stylesheet>";

    private DocumentBuilderFactory documentBuilderFactory = null;
	private DocumentBuilderFactory validatingDocumentBuilderFactory = null;
    private TransformerFactory transformerFactory = null;
	private Templates toStringTemplates = null;
	private XPathFactory xPathFactory = null;

	
	public Document newDocument() {
        return newDocument(null);
	}
	
	public Document newDocument(String root) {
		try {
			Document document = getDocumentBuilderFactory().newDocumentBuilder().newDocument();
			document.setXmlVersion("1.0");
	        document.setXmlStandalone(true);
	        if (root != null)
				newElement(document, root);
	        return document;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Document getDocument(Node node) {
		return (node instanceof Document ? (Document)node : node.getOwnerDocument());
	}

	public Element newElement(Node parent, String name) {
		return newElement(parent, name, null);
	}

	public Element newElement(Node parent, String name, String value) {
		Element element = getDocument(parent).createElement(name);
		parent.appendChild(element);
		if (value != null)
			element.setTextContent(value);
		return element;
	}
	
	public String getNormalizedValue(Node node) {
		if (node == null)
			return null;
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			StringBuilder sb = new StringBuilder();
			for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getNodeType() == Node.TEXT_NODE && child.getNodeValue() != null) {
					String value = child.getNodeValue().trim();
					if (value.length() > 0) {
						if (sb.length() > 0)
							sb.append(' ');
						sb.append(value);
					}
				}
			}
			return sb.toString();
		}
		return (node.getNodeValue() != null ? node.getNodeValue().trim() : null);
	}

	public String setValue(Node node, String value) {
		if (node != null) {
			String previousValue = getNormalizedValue(node);
			switch (node.getNodeType()) {
			case Node.ELEMENT_NODE:
				((Element)node).setTextContent(value);
				break;
			case Node.ATTRIBUTE_NODE:
			case Node.TEXT_NODE:
				node.setNodeValue(value);
				break;
			default:
				throw new RuntimeException("Illegal node for write operations: " + node);
			}
			return previousValue;
		}
		return null;
	}
	
    public Document buildDocument(String xml) {
        try {
            DocumentBuilder builder = getDocumentBuilderFactory().newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new RuntimeException("Could not parse XML string", e);
        }
    }
    
	public Document loadDocument(InputStream input) throws IOException, SAXException {
		try {
			return getDocumentBuilderFactory().newDocumentBuilder().parse(input);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Document loadDocument(InputStream input, EntityResolver resolver, ErrorHandler errorHandler) throws IOException, SAXException {
		try {
			DocumentBuilder builder = getValidatingDocumentBuilderFactory().newDocumentBuilder();
			builder.setEntityResolver(resolver);
			if (errorHandler != null)
				builder.setErrorHandler(errorHandler);
			return builder.parse(input);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}	
    
	public void saveDocument(Document document, OutputStream output) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			
			transformer.transform(new DOMSource(document), new StreamResult(output));
		}
		catch (TransformerException e) {
			throw new RuntimeException("Could not save document", e);
		}
	}

    public String toString(Document doc) {
        try {
            Transformer transformer = getTransformerFactory().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize document", e);
        }
    }
    
	protected Templates getToStringTemplates() {
		if (toStringTemplates == null) {
			try {
				toStringTemplates = TransformerFactory.newInstance().newTemplates(new StreamSource(new StringReader(TO_STRING_XSL)));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return toStringTemplates;
	}

	public String toNodeString(Node node) {
		try {
			Transformer transformer = getToStringTemplates().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, Charset.defaultCharset().name());
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			
			StringWriter sw = new StringWriter();
			transformer.transform(new DOMSource(node), new StreamResult(sw));
			return sw.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
				
	}
	
	public Node selectSingleNode(Object context, String expression) {
		try {
			return (Node)getXPathFactory().newXPath().evaluate(expression, context, XPathConstants.NODE);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<Node> selectNodeSet(Object context, String expression) {
		try {
			NodeList nodeList = (NodeList)getXPathFactory().newXPath().evaluate(expression, context, XPathConstants.NODESET);
			List<Node> nodes = new ArrayList<Node>(nodeList.getLength());
			for (int i = 0; i < nodeList.getLength(); i++)
				nodes.add(nodeList.item(i));
			return nodes;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private DocumentBuilderFactory getDocumentBuilderFactory() {
		if (documentBuilderFactory == null) {
			try {
				documentBuilderFactory = DocumentBuilderFactory.newInstance();
	
				documentBuilderFactory.setCoalescing(true);
				documentBuilderFactory.setIgnoringComments(true);
			} 
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return documentBuilderFactory;
	}

	private DocumentBuilderFactory getValidatingDocumentBuilderFactory() {
		if (validatingDocumentBuilderFactory == null) {
			try {
				validatingDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
				validatingDocumentBuilderFactory.setCoalescing(true);
				validatingDocumentBuilderFactory.setIgnoringComments(true);
				validatingDocumentBuilderFactory.setValidating(true);
				validatingDocumentBuilderFactory.setIgnoringElementContentWhitespace(true);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return validatingDocumentBuilderFactory;
	}

    private TransformerFactory getTransformerFactory() {
        if (transformerFactory == null)
            transformerFactory = TransformerFactory.newInstance();
        return transformerFactory;
    }
    
	private XPathFactory getXPathFactory() {
		if (xPathFactory == null) {
			try {
				xPathFactory = XPathFactory.newInstance();
			}
			catch (Exception e) {
				try {
					// Fallback to xalan for Google App Engine
					Class<?> factoryClass = Thread.currentThread().getContextClassLoader().loadClass("org.apache.xpath.jaxp.XPathFactoryImpl");
					Method m = factoryClass.getMethod("newInstance", String.class, String.class, ClassLoader.class);
					xPathFactory = (XPathFactory)m.invoke(null, XPathFactory.DEFAULT_OBJECT_MODEL_URI, "org.apache.xpath.jaxp.XPathFactoryImpl", null);
				}
				catch (Exception f) {
					throw new RuntimeException("XPathFactory could not be found", f);
				}
			}
		}
		return xPathFactory;
	}

}
