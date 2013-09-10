/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.granite.gravity.selector;

import flex.messaging.messages.Message;

/**
 * MessageEvaluationContext is used to cache selection results.
 *
 * A message usually has multiple selectors applied against it. Some selector
 * have a high cost of evaluating against the message. Those selectors may whish
 * to cache evaluation results associated with the message in the
 * MessageEvaluationContext.
 *
 * @version $Revision: 1.4 $
 */
public class MessageEvaluationContext {

    private final Message message;

    public MessageEvaluationContext(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
