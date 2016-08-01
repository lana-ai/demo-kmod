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

import ai.lana.aws.cloudwatch.Metric;
import ai.lana.aws.cloudwatch.MetricValue;
import ws.salient.chat.Intent;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class AlarmTest extends SessionTest {
    
    private Metric metric;
    
    @Before
    public void init() {
        metric = new Metric().withNamespace("Lambda").
                        withMetricName("Invocations")
                        .withDimension("FunctionName", "getproducts");
    } 
    
    
    @Test
    public void updateProvisionedReads() throws Exception {
        assertMessage("Hi Lana", new Intent("Greet", 0.99), getString("greet"));
        int processCount = ksession.getProcessInstances().size();
        assertEquals(0, processCount);
        clock.advanceTime(10L, TimeUnit.SECONDS);
        insertMessage("set provisioned reads to 50", new Intent("UpdateTable", 0.99).putSingle("builtin.number", "50"));
    }
    
    @Test
    public void getProductTableStatistics() {
        insertValue(84, false);
        assertMessage("What are the stats for the product table?", new Intent("GetMetricStatistics", 0.99).put("Resource", "product").put("Service", "dynamodb"), true);
    }
    
    @Test
    public void getProductTableAlarm() {
        insertValue(106, true);
        insertValue(84, true);
        insertValue(30, true);
        insertValue(84, true);
    }
    
    protected void insertValue(Integer sum, boolean expectAlarm) {
        ksession.fireAllRules();
        long factCount = ksession.getFactCount();
        clock.advanceTime(1, TimeUnit.MINUTES);
        ksession.insert(new MetricValue().withMetric(metric).withSum(sum.longValue()).withTimestamp(clock.getCurrentTime()));
        ksession.fireAllRules();
        // Expect 5 new metric values
        assertEquals(factCount + 5, ksession.getFactCount());
    }
    
    
}
