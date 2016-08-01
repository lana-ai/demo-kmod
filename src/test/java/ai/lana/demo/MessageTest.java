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
import java.util.Arrays;
import org.junit.Test;

public class MessageTest  extends SessionTest {
    
    
    @Test
    public void greet() {
        assertMessage("Hi Lana", new Intent("Greet", 0.99), getString("greet"));
    }
    
    @Test
    public void help() {
        assertMessage("What can you do?", new Intent("Help", 0.99), getString("help.0"));
        assertMessage("What can you do?", new Intent("Help", 0.99), getString("help.1"));
        assertMessage("What can you do?", new Intent("Help", 0.99), getString("help.2"));
        assertMessage("What can you do?", new Intent("Help", 0.99), getString("help.3"));
        assertMessage("What can you do?", new Intent("Help", 0.99), getString("help.0"));
    }
    
    @Test
    public void error() {
        assertMessage("Hi Lana", new Intent("Error", 0.99), getString("error"));
    }
    
    @Test
    public void sayThanks() {
        assertMessage("Thanks Lana!", new Intent("Thank", 0.99), getString("np"));
    }
    
    @Test
    public void listDynamoDBTables() {
        assertMessage("List dynamodb tables please", new Intent("ListResources", 0.99).put("Service", "dynamodb"), getString("list.resources.dynamodb"));
    }
    
    @Test
    public void listLambdaFunctions() {
        assertMessage("And what lambdas are deployed?", new Intent("ListResources", 0.99).put("Service", "lambda"), getString("list.resources.lambda"));
    }
    
    @Test
    public void listTables() {
        assertMessage("List tables please", new Intent("ListResources", 0.99).put("Service", "tables"), getString("list.resources.dynamodb"));
    }
    
    @Test
    public void listResources() {
        insertMessage("List resources", new Intent("ListResources", 0.99));
    }
    
    @Test
    public void getProductTableStatistics() {
        assertMessage("What's the product table looking like?", new Intent("GetMetricStatistics", 0.99).put("Resource", "product").put("Service", "dynamodb"), null, metrics.title("DynamoDB", Arrays.asList("ProvisionedReadCapacityUnits", "ConsumedReadCapacityUnits")), metrics.subtitle("DynamoDB", Arrays.asList("product")));
    }
    
    @Test
    public void getProductResourceStatistics() {
        insertMessage("What's product looking like?", new Intent("GetMetricStatistics", 0.99).put("Resource", "product"));
    }
    
    @Test
    public void getProductsFunctionStatistics() {
        assertMessage("What are the getproducts function's statistics?", new Intent("GetMetricStatistics", 0.99).put("Resource", "getproducts").put("Service", "lambda"), null, metrics.title("Lambda", Arrays.asList("Invocations")), metrics.subtitle("Lambda", Arrays.asList("getproducts")));
    }
    
//    @Test
//    public void getUnknownTableStatistics() {
//        assertMessage("What stats do you have for the apple table?", new Intent("GetMetricStatistics", 0.99).put("Resource", "apple").put("Service", "dynamodb"), bundle.getString("dynamodb.table.unknown").replaceAll("\\$\\{Resource}", "apple"));
//    }
    
    @Test
    public void getProductAliasFunctionStatistics() {
        assertMessage("What are the getproduct function's statistics?", new Intent("GetMetricStatistics", 0.99).put("Resource", "getproduct").put("Service", "lambda"), null, metrics.title("Lambda", Arrays.asList("Invocations")), metrics.subtitle("Lambda", Arrays.asList("getproducts")));
    }
    
//    @Test
//    public void getUnknownFunctionStatistics() {
//        assertMessage("What stats do you have for the apple function?", new Intent("GetMetricStatistics", 0.99).put("Resource", "apple").put("Service", "lambda"), bundle.getString("lambda.unknown").replaceAll("\\$\\{Resource}", "apple"));
//    }
    
    @Test
    public void getProductTableErrors() {
        assertMessage("What are the product table errors looking like?", new Intent("GetMetricErrors", 0.99).put("Resource", "product").put("Service", "dynamodb"), null, metrics.title("DynamoDB", Arrays.asList("ThrottledRequests")), metrics.subtitle("DynamoDB", Arrays.asList("product")));
    }
    
    @Test
    public void getTableAndFunctionErrors() {
        insertMessage("errors for tables and functions", new Intent("GetMetricErrors", 0.99).put("Service", "tables").put("Service", "functions"));
    }
    
    @Test
    public void getProductAliasFunctionErrors() {
        assertMessage("What are the getproduct function's errors?", new Intent("GetMetricErrors", 0.99).put("Resource", "getproduct").put("Service", "lambda"), null, metrics.title("Lambda", Arrays.asList("Errors")), metrics.subtitle("Lambda", Arrays.asList("getproducts")));
    }

}
