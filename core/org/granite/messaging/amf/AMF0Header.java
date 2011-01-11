/*
 * www.openamf.org
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.granite.messaging.amf;

import java.io.Serializable;

/**
 * AMF Header
 *
 * @author Jason Calabrese <jasonc@missionvi.com>
 * @author Pat Maddox <pergesu@users.sourceforge.net>
 * @see AMF0Body
 * @see AMF0Message
 * @version $Revision: 1.8 $, $Date: 2003/08/16 13:11:16 $
 */
public class AMF0Header implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String key;
    protected boolean required;
    protected Object value;

    public AMF0Header(String key, boolean required, Object value) {
        this.key = key;
        this.required = required;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(String indent) {
        return (new StringBuilder()
            .append(indent).append(AMF0Header.class.getName()).append(" {")
            .append('\n').append(indent).append("  key = ").append(key)
            .append('\n').append(indent).append("  required = ").append(required)
            .append('\n').append(indent).append("  value = ").append(value)
            .append('\n').append(indent).append("}")
            .toString()
        );
    }

}
