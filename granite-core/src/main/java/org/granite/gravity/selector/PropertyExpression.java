/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.gravity.selector;

import javax.jms.JMSException;

import flex.messaging.messages.Message;

/**
 * Represents a property  expression
 *
 * @version $Revision: 1.5 $
 */
public class PropertyExpression implements Expression {

    interface SubExpression {
        public Object evaluate(Message message);
    }

    private final String name;

    public PropertyExpression(String name) {
        this.name = name;
    }

    public Object evaluate(MessageEvaluationContext message) throws JMSException {
        return message.getMessage().getHeader(name);
    }

    public Object evaluate(Message message) {
        return message.getHeader(name);
    }

    public String getName() {
        return name;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {

        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        return name.equals(((PropertyExpression) o).name);

    }

}
