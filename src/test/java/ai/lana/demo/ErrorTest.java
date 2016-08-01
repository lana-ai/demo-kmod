/*
 * Copyright 2016 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.lana.demo;

import com.amazonaws.services.lambda.invoke.LambdaFunctionException;
import ws.salient.chat.Message;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;


public class ErrorTest extends SessionTest {
    
    @Test
    public void greet() throws Exception {
        insertError("Hi Lana", getString("error"));
    }

    protected void insertError(String text, String expectedReplyText) throws Exception {
        ksession.insert(new Message(text, replyTo));
        ksession.fireAllRules();
        handleException(new LambdaFunctionException("Luis error", false, "Error"));
        assertEquals(1, ksession.getProcessInstances().size());
        assertEquals("chat-reply", workItem.getParameter("functionName"));
        Message reply = (Message) workItem.getParameter("reply");
        assertNotNull(reply);
        assertNotNull(reply.getReplyTo());
        if (expectedReplyText != null) {
            assertEquals(expectedReplyText, reply.getText());
        }
        ksession.getWorkItemManager().completeWorkItem(
                workItem.getId(),
                null);
        assertEquals(0, ksession.getProcessInstances().size());
        ksession.fireAllRules();
    }
    
}
