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

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import java.nio.ByteBuffer;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class DemoIT {

    AmazonKinesis kinesis;

    @Before
    public void before() {
        kinesis = new AmazonKinesisClient();
    }

    @Test
    public void sendResponse() {
        String accountId = "test";
        String sessionId = UUID.randomUUID().toString();
        byte[] message = newMessage(accountId, sessionId, "demo", "What are the product stats?", "http://requestb.in/");
        System.out.println(new String(message));
        PutRecordResult result = kinesis.putRecord(new PutRecordRequest().withData(ByteBuffer.wrap(message)).withStreamName("salient").withPartitionKey(accountId));
        System.out.println(result);
    }

    private byte[] newMessage(String accountId, String sessionId, String knowledgeBaseId, String text, String replyTo) {
        return String.format("{\n"
                + "    \"accountId\": \"%s\",\n"
                + "    \"sessionId\": \"%s\",\n"
                + "    \"knowledgeBaseId\": \"%s\",\n"
                + "    \"profiles\": [\"demo\"],\n"
                + "    \"command\": \"insert\",\n"
                + "    \"objects\": [{\n"
                + "         \"ws.salient.chat.Message\": {\n"
                + "             \"replyTo\": {\n"
                + "             \"http\": {\n"
                + "                 \"url\": \"%s\"\n"
                + "             }\n"
                + "             },"
                + "             \"text\":\"%s\""
                + "            }\n"
                + "        }]\n"
                + "}", accountId, sessionId, knowledgeBaseId, replyTo, text).getBytes();
    }

}
