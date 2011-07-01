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

package org.granite.gravity.selector;

import java.io.IOException;

import javax.jms.JMSException;

import org.apache.activemq.command.Response;
import org.apache.activemq.filter.BooleanExpression;
import org.apache.activemq.filter.MessageEvaluationContext;
import org.apache.activemq.selector.SelectorParser;
import org.apache.activemq.state.CommandVisitor;

import flex.messaging.messages.Message;

/**
 * @author William DRAI
 */
public class ActiveMQMessageSelector implements MessageSelector {

    private BooleanExpression expression;


    public ActiveMQMessageSelector(String selector) {
        try {
            this.expression = SelectorParser.parse(selector);
        }
        catch (Exception e) {
            throw new RuntimeException("ActiveMQ SelectorParser error " + selector, e);
        }
    }

    public boolean accept(Message message) {
        try {
            MessageEvaluationContext context = new MessageEvaluationContext();
            MessageAdapter ma = new MessageAdapter(message);
            context.setMessageReference(ma);

            return expression.matches(context);
        }
        catch (Exception e) {
            throw new RuntimeException("ActiveMQ selector accept error " + message, e);
        }
    }


    private static class MessageAdapter extends org.apache.activemq.command.Message {
        private Message message = null;

        public MessageAdapter(Message message) {
            this.message = message;
        }

        @Override
        public Object getProperty(String name) throws IOException {
            return message.getHeader(name);
        }

        @Override
        public org.apache.activemq.command.Message copy() {
            return null;
        }

        public Response visit(CommandVisitor visitor) throws Exception {
            return null;
        }

        public byte getDataStructureType() {
            return 0;
        }

		@Override
		public void clearBody() throws JMSException {
		}
    }
}
