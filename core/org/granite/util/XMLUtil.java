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

package org.granite.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @author Franck WOLFF
 */
public class XMLUtil {

    private DocumentBuilderFactory documentBuilderFactory = null;
    private TransformerFactory transformerFactory = null;

    public Document buildDocument(String xml) {
        try {
            DocumentBuilder builder = getDocumentBuilderFactory().newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new RuntimeException("Could not parse XML string", e);
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

    private TransformerFactory getTransformerFactory() {
        if (transformerFactory == null)
            transformerFactory = TransformerFactory.newInstance();
        return transformerFactory;
    }

    private DocumentBuilderFactory getDocumentBuilderFactory() {
        if (documentBuilderFactory == null)
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
        return documentBuilderFactory;
    }
}
