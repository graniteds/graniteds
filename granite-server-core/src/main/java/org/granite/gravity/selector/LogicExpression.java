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
package org.granite.gravity.selector;

import javax.jms.JMSException;

/**
 * A filter performing a comparison of two objects
 *
 * @version $Revision: 1.2 $
 */
public abstract class LogicExpression extends BinaryExpression implements BooleanExpression {

    public static BooleanExpression createOR(BooleanExpression lvalue, BooleanExpression rvalue) {
        return new LogicExpression(lvalue, rvalue) {
            @Override
            public Object evaluate(MessageEvaluationContext message) throws JMSException {

                Boolean lv = (Boolean) left.evaluate(message);
                // Can we do an OR shortcut??
                if (lv !=null && lv.booleanValue()) {
                    return Boolean.TRUE;
                }

                Boolean rv = (Boolean) right.evaluate(message);
                return rv==null ? null : rv;
            }

            @Override
            public String getExpressionSymbol() {
                return "OR";
            }
        };
    }

    public static BooleanExpression createAND(BooleanExpression lvalue, BooleanExpression rvalue) {
        return new LogicExpression(lvalue, rvalue) {
            @Override
            public Object evaluate(MessageEvaluationContext message) throws JMSException {

                Boolean lv = (Boolean) left.evaluate(message);

                // Can we do an AND shortcut??
                if (lv == null)
                    return null;
                if (!lv.booleanValue()) {
                    return Boolean.FALSE;
                }

                Boolean rv = (Boolean) right.evaluate(message);
                return rv == null ? null : rv;
            }

            @Override
            public String getExpressionSymbol() {
                return "AND";
            }
        };
    }

    /**
     * @param left
     * @param right
     */
    public LogicExpression(BooleanExpression left, BooleanExpression right) {
        super(left, right);
    }

    abstract public Object evaluate(MessageEvaluationContext message) throws JMSException;

    public boolean matches(MessageEvaluationContext message) throws JMSException {
        Object object = evaluate(message);
        return object!=null && object==Boolean.TRUE;
    }

}
