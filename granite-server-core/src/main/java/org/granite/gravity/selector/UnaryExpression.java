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
package org.granite.gravity.selector;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.jms.JMSException;

/**
 * An expression which performs an operation on two expression values
 *
 * @version $Revision: 1.3 $
 */
public abstract class UnaryExpression implements Expression {

    private static final BigDecimal BD_LONG_MIN_VALUE = BigDecimal.valueOf(Long.MIN_VALUE);
    protected Expression right;

    public static Expression createNegate(Expression left) {
        return new UnaryExpression(left) {
            public Object evaluate(MessageEvaluationContext message) throws JMSException {
                Object rvalue = right.evaluate(message);
                if (rvalue == null) {
                    return null;
                }
                if (rvalue instanceof Number) {
                    return negate((Number) rvalue);
                }
                return null;
            }

            @Override
            public String getExpressionSymbol() {
                return "-";
            }
        };
    }

    public static BooleanExpression createInExpression(PropertyExpression right, List<?> elements, final boolean not) {

        // Use a HashSet if there are many elements.
        Collection<?> t;
        if( elements.size()==0 )
            t=null;
        else if( elements.size() < 5 )
            t = elements;
        else {
            t = new HashSet<Object>(elements);
        }
        final Collection<?> inList = t;

        return new BooleanUnaryExpression(right) {
            public Object evaluate(MessageEvaluationContext message) throws JMSException {

                Object rvalue = right.evaluate(message);
                if (rvalue == null) {
                    return null;
                }
                if( rvalue.getClass()!=String.class )
                    return null;

                if( (inList!=null && inList.contains(rvalue)) ^ not ) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;

            }

            @Override
            public String toString() {
                StringBuffer answer = new StringBuffer();
                answer.append(right);
                answer.append(" ");
                answer.append(getExpressionSymbol());
                answer.append(" ( ");

                if (inList != null) {
                	int count=0;
	                for (Iterator<?> i = inList.iterator(); i.hasNext();) {
	                    Object o = i.next();
	                    if( count!=0 ) {
	                        answer.append(", ");
	                    }
	                    answer.append(o);
	                    count++;
	                }
                }

                answer.append(" )");
                return answer.toString();
            }

            @Override
            public String getExpressionSymbol() {
                if( not )
                    return "NOT IN";
                return "IN";
            }
        };
    }

    abstract static class BooleanUnaryExpression extends UnaryExpression implements BooleanExpression {
        public BooleanUnaryExpression(Expression left) {
            super(left);
        }

        public boolean matches(MessageEvaluationContext message) throws JMSException {
            Object object = evaluate(message);
            return object!=null && object == Boolean.TRUE;
        }
    }


    public static BooleanExpression createNOT(BooleanExpression left) {
        return new BooleanUnaryExpression(left) {
            public Object evaluate(MessageEvaluationContext message) throws JMSException {
                Boolean lvalue = (Boolean) right.evaluate(message);
                if (lvalue == null) {
                    return null;
                }
                return lvalue.booleanValue() ? Boolean.FALSE : Boolean.TRUE;
            }

            @Override
            public String getExpressionSymbol() {
                return "NOT";
            }
        };
    }

    public static BooleanExpression createBooleanCast(Expression left) {
        return new BooleanUnaryExpression(left) {
            public Object evaluate(MessageEvaluationContext message) throws JMSException {
                Object rvalue = right.evaluate(message);
                if (rvalue == null)
                    return null;
                if (!rvalue.getClass().equals(Boolean.class))
                    return Boolean.FALSE;
                return ((Boolean)rvalue).booleanValue() ? Boolean.TRUE : Boolean.FALSE;
            }

            @Override
            public String toString() {
                return right.toString();
            }

            @Override
            public String getExpressionSymbol() {
                return "";
            }
        };
    }

    private static Number negate(Number left) {
        Class<?> clazz = left.getClass();
        if (clazz == Integer.class) {
            return new Integer(-left.intValue());
        }
        else if (clazz == Long.class) {
            return new Long(-left.longValue());
        }
        else if (clazz ==  Float.class) {
            return new Float(-left.floatValue());
        }
        else if (clazz == Double.class) {
            return new Double(-left.doubleValue());
        }
        else if (clazz == BigDecimal.class) {
            // We ussually get a big deciamal when we have Long.MIN_VALUE constant in the
            // Selector.  Long.MIN_VALUE is too big to store in a Long as a positive so we store it
            // as a Big decimal.  But it gets Negated right away.. to here we try to covert it back
            // to a Long.
            BigDecimal bd = (BigDecimal)left;
            bd = bd.negate();

            if( BD_LONG_MIN_VALUE.compareTo(bd)==0  ) {
                return new Long(Long.MIN_VALUE);
            }
            return bd;
        }
        else {
            throw new RuntimeException("Don't know how to negate: "+left);
        }
    }

    public UnaryExpression(Expression left) {
        this.right = left;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression expression) {
        right = expression;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "(" + getExpressionSymbol() + " " + right.toString() + ")";
    }

    /**
     * TODO: more efficient hashCode()
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * TODO: more efficient hashCode()
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {

        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        return toString().equals(o.toString());

    }

    /**
     * Returns the symbol that represents this binary expression.  For example, addition is
     * represented by "+"
     *
     * @return teh symbol
     */
    abstract public String getExpressionSymbol();

}
