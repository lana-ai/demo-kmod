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

import ws.salient.chat.Intent;
import java.text.ParseException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Test;


public class UpdateTableTest extends SessionTest {
    
    
    @Test
    public void updateProductProvisionedReads() throws ParseException {
        clock.advanceTime(30, TimeUnit.SECONDS);
        Intent updateTable =  new Intent("UpdateTable", 0.99).withScore("Greet",2.01160045E-4).put("Resource", "product").put("builtin.number", "150");
        insertMessage("Set product reads to 150", updateTable);
        clock.advanceTime(30, TimeUnit.SECONDS);
    }
    
    
    @Test
    public void updateProvisionedReads() throws ParseException {
        clock.advanceTime(30, TimeUnit.SECONDS);
        Intent updateTable =  new Intent("UpdateTable", 0.99).withScore("Greet",2.01160045E-4).put("builtin.number", "130");
        insertMessage("Set product reads to 130", updateTable);
        clock.advanceTime(30, TimeUnit.SECONDS);
    }
    
    @Test
    public void updateProvisionedNoValue() throws ParseException {
        clock.advanceTime(30, TimeUnit.SECONDS);
        assertMessage("Set reads", new Intent("UpdateTable", 0.99), getString("dynamodb.update.value", Collections.singletonMap("Resource", "product")));
        clock.advanceTime(30, TimeUnit.SECONDS);
        insertMessage("130", new Intent("None", 0.99).put("builtin.number", "130"));
    }
    
    
    @Test
    public void updateProvisionedReadError() throws ParseException {
        clock.advanceTime(30, TimeUnit.SECONDS);
        Intent updateTable =  new Intent("UpdateTable", 0.99).withScore("Greet",2.01160045E-4).put("builtin.number", "78");
        insertMessage("Set product reads to 50", updateTable);
        clock.advanceTime(30, TimeUnit.SECONDS);
    }
    
    @Test
    public void updateProvisionedReadsMin() throws ParseException {
        clock.advanceTime(30, TimeUnit.SECONDS);
        Intent updateTable =  new Intent("UpdateTable", 0.99).put("Resource", "product").put("builtin.number", "0");
        assertMessage("Decrease product reads to 140", updateTable, getString("dynamodb.update.min"));
        clock.advanceTime(30, TimeUnit.SECONDS);
    }
    
    @Test
    public void updateProvisionedReadsMax() throws ParseException {
        clock.advanceTime(30, TimeUnit.SECONDS);
        Intent updateTable =  new Intent("UpdateTable", 0.99).put("Resource", "product").put("builtin.number", "10001");
        assertMessage("Decrease product reads to 140", updateTable, getString("dynamodb.update.max"));
        clock.advanceTime(30, TimeUnit.SECONDS);
    }
}
