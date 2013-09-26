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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.granite.generator.gsp.token.Comment;
import org.granite.generator.gsp.token.Declaration;
import org.granite.generator.gsp.token.Directive;
import org.granite.generator.gsp.token.Expression;
import org.granite.generator.gsp.token.Scriplet;
import org.granite.generator.gsp.token.TemplateText;
import org.granite.generator.gsp.token.Token;

/**
 * @author Franck WOLFF
 */
public class Parser {

    private final List<Token> elements = new ArrayList<Token>();

    public Parser() {
    }

    public List<Token> parse(Reader reader) throws IOException, ParseException {
        if (reader == null)
            throw new NullPointerException("reader cannot be null");
        if (!(reader instanceof StringReader || reader instanceof BufferedReader))
            reader = new BufferedReader(reader);
        return parse(read(reader));
    }

    public List<Token> getElements() {
        return elements;
    }

    public void reset() {
        elements.clear();
    }

    private List<Token> parse(String template) throws ParseException {
        if (template == null)
            throw new NullPointerException("Argument template cannot be null");

        StringBuilder sb = new StringBuilder(64);

        final int length = template.length();
        boolean inText = true;
        for (int i = 0; i < length; i++) {
            char first = inText ? '<' : '%';
            char last = inText ? '%' : '>';
            char c = template.charAt(i);
            boolean appendC = true;

            if (c == first) { // '<' (template text) or '%' (gsp tag content)
                if (i+1 < length) {
                    c = template.charAt(i+1);
                    if (c == last) { // "<%" (template text) or "%>" (gsp tag content)
                        if (inText)
                            addTemplateText(i - sb.length(), sb.toString());
                        else
                            addScriptingElement(i - sb.length() - 2, sb.toString());
                        sb.setLength(0);
                        inText = !inText;
                        appendC = false;
                        i++;
                    } else if (c == '\\') { // "<\" (template text) or "%\" (gsp tag content)
                        sb.append(first);
                        for (i += 2; i < length && (c = template.charAt(i)) == '\\'; i++)
                            sb.append(c);
                        if (c != last) // add skiped first '/'
                            sb.append('\\');
                    } else
                        c = first;
                }
            }

            if (appendC)
                sb.append(c);
        }

        if (!inText)
            throw new ParseException("Missing script section end (\"%>\")");

        addTemplateText(length - sb.length(), sb.toString());

        return elements;
    }

    private String read(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder(1024);

        int pc = -1, c = -1;
        while((c = reader.read()) != -1) {
            switch (c) {
            case '\r': // "\r[\n]" (Windows or Mac)
                break;
            case '\n': // "[\r]\n" (Windows or Unix)
                sb.append('\n');
                break;
            default:
                if (pc == '\r')  // "\r" (Mac)
                    sb.append('\n');
                sb.append((char)c);
                break;
            }
            pc = c;
        }

        return sb.toString();
    }

    private void addTemplateText(int index, String text) {
        if (text.length() > 0)
            elements.add(new TemplateText(index, text));
    }

    private void addScriptingElement(int index, String text) throws ParseException {
        if (text.length() == 0)
            return;
        char first = text.charAt(0);
        switch (first) {
        case '=': // expression
            elements.add(new Expression(index, text.substring(1).trim()));
            break;
        case '!': // variable or method declaration
            elements.add(new Declaration(index, text.substring(1).trim()));
            break;
        case '@': // directive
            elements.add(new Directive(index, text.substring(1).trim()));
            break;
        case '-': // comment ?
            if (text.startsWith("--") && text.endsWith("--")) {
                elements.add(new Comment(index, text.substring(2, text.length() - 4)));
                break;
            }
            // fall down (scriplet starting with '-')...
        default: // scriplet
            elements.add(new Scriplet(index, text));
            break;
        }
    }
}
