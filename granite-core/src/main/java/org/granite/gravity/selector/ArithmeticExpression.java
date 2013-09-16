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
package org.granite.gravity.selector;

import javax.jms.JMSException;

/**
 * An expression which performs an operation on two expression values
 *
 * @version $Revision: 1.2 $
 */
public abstract class ArithmeticExpression extends BinaryExpression {

    protected static final int INTEGER = 1;
    protected static final int LONG = 2;
    protected static final int DOUBLE = 3;

    /**
     * @param left
     * @param right
     */
    public ArithmeticExpression(Expression left, Expression right) {
        super(left, right);
    }

    public static Expression createPlus(Expression left, Expression right) {
        return new ArithmeticExpression(left, right) {
            @Override
            protected Object evaluate(Object lvalue, Object rvalue) {
                if (lvalue instanceof String) {
                    String text = (String) lvalue;
                    String answer = text + rvalue;
                    return answer;
                }
                else if (lvalue instanceof Number) {
                    return plus((Number) lvalue, asNumber(rvalue));
                }
                throw new RuntimeException("Cannot call plus operation on: " + lvalue + " and: " + rvalue);
            }

            @Override
            public String getExpressionSymbol() {
                return "+";
            }
        };
    }

    public static Expression createMinus(Expression left, Expression right) {
        return new ArithmeticExpression(left, right) {
            @Override
            protected Object evaluate(Object lvalue, Object rvalue) {
                if (lvalue instanceof Number) {
                    return minus((Number) lvalue, asNumber(rvalue));
                }
                throw new RuntimeException("Cannot call minus operation on: " + lvalue + " and: " + rvalue);
            }

            @Override
            public String getExpressionSymbol() {
                return "-";
            }
        };
    }

    public static Expression createMultiply(Expression left, Expression right) {
        return new ArithmeticExpression(left, right) {

            @Override
            protected Object evaluate(Object lvalue, Object rvalue) {
                if (lvalue instanceof Number) {
                    return multiply((Number) lvalue, asNumber(rvalue));
                }
                throw new RuntimeException("Cannot call multiply operation on: " + lvalue + " and: " + rvalue);
            }

            @Override
            public String getExpressionSymbol() {
                return "*";
            }
        };
    }

    public static Expression createDivide(Expression left, Expression right) {
        return new ArithmeticExpression(left, right) {

            @Override
            protected Object evaluate(Object lvalue, Object rvalue) {
                if (lvalue instanceof Number) {
                    return divide((Number) lvalue, asNumber(rvalue));
                }
                throw new RuntimeException("Cannot call divide operation on: " + lvalue + " and: " + rvalue);
            }

            @Override
            public String getExpressionSymbol() {
                return "/";
            }
        };
    }

    public static Expression createMod(Expression left, Expression right) {
        return new ArithmeticExpression(left, right) {

            @Override
            protected Object evaluate(Object lvalue, Object rvalue) {
                if (lvalue instanceof Number) {
                    return mod((Number) lvalue, asNumber(rvalue));
                }
                throw new RuntimeException("Cannot call mod operation on: " + lvalue + " and: " + rvalue);
            }

            @Override
            public String getExpressionSymbol() {
                return "%";
            }
        };
    }

    protected Number plus(Number left, Number right) {
        switch (numberType(left, right)) {
            case INTEGER:
                return new Integer(left.intValue() + right.intValue());
            case LONG:
                return new Long(left.longValue() + right.longValue());
            default:
                return new Double(left.doubleValue() + right.doubleValue());
        }
    }

    protected Number minus(Number left, Number right) {
        switch (numberType(left, right)) {
            case INTEGER:
                return new Integer(left.intValue() - right.intValue());
            case LONG:
                return new Long(left.longValue() - right.longValue());
            default:
                return new Double(left.doubleValue() - right.doubleValue());
        }
    }

    protected Number multiply(Number left, Number right) {
        switch (numberType(left, right)) {
            case INTEGER:
                return new Integer(left.intValue() * right.intValue());
            case LONG:
                return new Long(left.longValue() * right.longValue());
            default:
                return new Double(left.doubleValue() * right.doubleValue());
        }
    }

    protected Number divide(Number left, Number right) {
        return new Double(left.doubleValue() / right.doubleValue());
    }

    protected Number mod(Number left, Number right) {
        return new Double(left.doubleValue() % right.doubleValue());
    }

    private int numberType(Number left, Number right) {
        if (isDouble(left) || isDouble(right)) {
            return DOUBLE;
        }
        else if (left instanceof Long || right instanceof Long) {
            return LONG;
        }
        else {
            return INTEGER;
        }
    }

    private boolean isDouble(Number n) {
        return n instanceof Float || n instanceof Double;
    }

    protected Number asNumber(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        }
        throw new RuntimeException("Cannot convert value: " + value + " into a number");
    }

    public Object evaluate(MessageEvaluationContext message) throws JMSException {
        Object lvalue = left.evaluate(message);
        if (lvalue == null) {
            return null;
        }
        Object rvalue = right.evaluate(message);
        if (rvalue == null) {
            return null;
        }
        return evaluate(lvalue, rvalue);
    }


    /**
     * @param lvalue
     * @param rvalue
     * @return the evaluated value
     */
    abstract protected Object evaluate(Object lvalue, Object rvalue);

}
