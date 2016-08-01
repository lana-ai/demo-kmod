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

import ai.lana.aws.Resource;
import ai.lana.aws.cloudwatch.Metric;
import ai.lana.aws.cloudwatch.MetricValue;
import ai.lana.aws.dynamodb.Table;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.kie.api.runtime.KieSession;
import org.kie.api.time.SessionClock;
import ws.salient.chat.Chat;
import ws.salient.chat.Intent;
import ws.salient.chat.LineChart;
import ws.salient.chat.Response;

@Singleton
public class Metrics {

    private final ObjectMapper json;
    private final Chat chat;
    private final SessionClock clock;
    private final Map<String, Integer[]> seriesValues;
    private final Map<String, Integer[]> thresholdSeriesValues;
    private final KieSession ksession;

    public void newMetrics(MetricValue value, Table table) {
        long provisionedReadCapacityUnits = table.getProvisionedReadCapacityUnits();
        long consumedReadCapacityUnits = value.getSum();
        long throttledRequests = 0;
        long lambdaErrors = 0;
        if (consumedReadCapacityUnits > provisionedReadCapacityUnits) {
            throttledRequests = consumedReadCapacityUnits - provisionedReadCapacityUnits;
            lambdaErrors = throttledRequests;
            consumedReadCapacityUnits = provisionedReadCapacityUnits;
        }
        ksession.insert(new MetricValue()
                .withMetric(new Metric()
                        .withNamespace("DynamoDB")
                        .withMetricName("ProvisionedReadCapacityUnits")
                        .withDimension("TableName", "product"))
                .withSum(provisionedReadCapacityUnits)
                .withTimestamp(value.getTimestamp()));
        ksession.insert(new MetricValue()
                .withMetric(new Metric()
                        .withNamespace("DynamoDB")
                        .withMetricName("ConsumedReadCapacityUnits")
                        .withDimension("TableName", "product"))
                .withSum(consumedReadCapacityUnits)
                .withTimestamp(value.getTimestamp()));
        ksession.insert(new MetricValue()
                .withMetric(new Metric()
                        .withNamespace("DynamoDB")
                        .withMetricName("ThrottledRequests")
                        .withDimension("TableName", "product"))
                .withSum(throttledRequests)
                .withTimestamp(value.getTimestamp()));
        ksession.insert(new MetricValue()
                .withMetric(new Metric()
                        .withNamespace("Lambda")
                        .withMetricName("Errors")
                        .withDimension("FunctionName", "getproducts"))
                .withSum(lambdaErrors)
                .withTimestamp(value.getTimestamp()));
    }

    public Response getErrors(Intent intent, Resource resource, TreeSet<MetricValue> chartValues) {
        return getErrors(intent, Arrays.asList(resource), chartValues).get(0);
    }

    public Response getStatistics(Intent intent, Resource resource, TreeSet<MetricValue> chartValues) {
        return getStatistics(intent, Arrays.asList(resource), chartValues).get(0);
    }

    public List<Response> getErrors(Intent intent, List<Resource> resources, TreeSet<MetricValue> chartValues) {
        return getResponses(intent, resources, true, chartValues);
    }

    public List<Response> getStatistics(Intent intent, List<Resource> resources, TreeSet<MetricValue> chartValues) {
        return getResponses(intent, resources, false, chartValues);
    }

    public List<Response> getResponses(Intent intent, List<Resource> resources, boolean errors, TreeSet<MetricValue> chartValues) {
        return resources.stream().map((resource) -> {
            return new Response(intent, null, getChart(resource.getMetricNamespace(), Arrays.asList(resource.getName()), chartValues,
                    resource.getMetricNames(errors)));
        }).collect(Collectors.toList());
    }

    public LineChart getChart(String namespace, Collection dimensions, TreeSet<MetricValue> chartValues, List<String> series) {
        Map<String, LinkedList<MetricValue>> metricNameValues = new LinkedHashMap();
        if (chartValues != null) {
            chartValues.forEach((value) -> {
                String valueName = String.join(".", value.getMetric().getNamespace(), value.getMetric().getMetricName());
                metricNameValues.computeIfAbsent(valueName, (name) -> {
                    return new LinkedList();
                }).add(value);
            });
        }
        LineChart chart;

        List<List<Integer>> columnValues = new LinkedList();
        int maxValues = 0;
        for (String name : series) {
            List<MetricValue> metricValues = null;
            String seriesName = String.join(".", namespace, name);
            if (metricNameValues.containsKey(seriesName)) {
                metricValues = metricNameValues.get(seriesName);
            }
            List values = new LinkedList();
            values.addAll(Arrays.asList(seriesValues.get(String.join(".", namespace, name))));
            if (metricValues != null) {
                metricValues.forEach((metricValue) -> {
                    values.add(metricValue.getSum());
                });
            }
            maxValues = Integer.max(maxValues, values.size());
            columnValues.add(values);
        }

        List<List> rows = new LinkedList();

        Instant instant = Instant.ofEpochMilli(clock.getCurrentTime());
        instant = instant.minusSeconds(maxValues * 15 * 60);

        for (int index  = 0; index < maxValues; index++) {
            List row = new LinkedList();
            row.add(Date.from(instant));
            for (List<Integer> values : columnValues) {
                if (index > values.size() - 1) {
                    row.add(values.get(values.size() - 1));
                } else {
                    row.add(values.get(index));
                }
            }  
            rows.add(row);           
            instant = instant.plusSeconds(60 * 15);
        }
        
        chart = new LineChart(title(namespace, series), subtitle(namespace, dimensions), rows,"datetime", "Time");
        series.stream().forEach((name) -> {
            chart.withColumn(
                    "number",
                    chat.getText(String.join(".", "charts.series.names", namespace, name)),
                    chat.getText(String.join(".", "charts.series.colors", namespace, name))
            );
        });

        return chart;
    }

    protected String title(String namespace, List<String> series) {
        String title = "";
        for (int index = series.size() - 1; index >= 0; index--) {
            title = chat.getText(String.join(".", "charts.titles", namespace, series.get(index))) + title;
            if (series.size() > 1 && index == (series.size() - 1)) {
                title = " and " + title;
            } else if (index > 0) {
                title = ", " + title;
            }
        }
        return title;
    }

    protected String subtitle(String namespace, Collection<String> dimensions) {
        String key = String.join(".", "charts.subtitles", namespace);
        String values = String.join(", ", dimensions);
        String subtitle = chat.getText(key, Collections.singletonMap("dimensions", values));
        return subtitle;
    }

    private String data = "{\"cols\":[{\"id\":\"\",\"label\":\"Time\",\"pattern\":\"\",\"type\":\"timeofday\"},{\"id\":\"\",\"label\":\"Consumed\",\"pattern\":\"\",\"type\":\"number\"},{\"id\":\"\",\"label\":\"Provisioned\",\"pattern\":\"\",\"type\":\"number\"}],\"rows\":[{\"c\":[{\"v\":[8,0,0]},{\"v\":15},{\"v\":80}]},{\"c\":[{\"v\":[8,15,0]},{\"v\":16},{\"v\":80}]},{\"c\":[{\"v\":[8,30,0]},{\"v\":18},{\"v\":80}]},{\"c\":[{\"v\":[8,45,0]},{\"v\":20},{\"v\":80}]},{\"c\":[{\"v\":[9,0,0]},{\"v\":19},{\"v\":80}]},{\"c\":[{\"v\":[9,15,0]},{\"v\":32},{\"v\":140}]},{\"c\":[{\"v\":[9,30,0]},{\"v\":36},{\"v\":140}]},{\"c\":[{\"v\":[9,45,0]},{\"v\":48},{\"v\":140}]},{\"c\":[{\"v\":[10,0,0]},{\"v\":47},{\"v\":140}]},{\"c\":[{\"v\":[10,15,0]},{\"v\":52},{\"v\":140}]},{\"c\":[{\"v\":[10,30,0]},{\"v\":58},{\"v\":140}]},{\"c\":[{\"v\":[10,45,0]},{\"v\":64},{\"v\":140}]},{\"c\":[{\"v\":[11,0,0]},{\"v\":62},{\"v\":140}]},{\"c\":[{\"v\":[11,15,0]},{\"v\":63},{\"v\":140}]},{\"c\":[{\"v\":[11,30,0]},{\"v\":67},{\"v\":140}]},{\"c\":[{\"v\":[11,45,0]},{\"v\":63},{\"v\":140}]},{\"c\":[{\"v\":[12,0,0]},{\"v\":65},{\"v\":140}]},{\"c\":[{\"v\":[12,15,0]},{\"v\":52},{\"v\":140}]},{\"c\":[{\"v\":[12,30,0]},{\"v\":43},{\"v\":140}]},{\"c\":[{\"v\":[12,45,0]},{\"v\":35},{\"v\":140}]},{\"c\":[{\"v\":[13,0,0]},{\"v\":21},{\"v\":140}]},{\"c\":[{\"v\":[13,15,0]},{\"v\":22},{\"v\":80}]},{\"c\":[{\"v\":[13,30,0]},{\"v\":26},{\"v\":80}]},{\"c\":[{\"v\":[13,45,0]},{\"v\":19},{\"v\":80}]},{\"c\":[{\"v\":[14,0,0]},{\"v\":23},{\"v\":80}]},{\"c\":[{\"v\":[14,15,0]},{\"v\":25},{\"v\":80}]},{\"c\":[{\"v\":[14,30,0]},{\"v\":18},{\"v\":80}]},{\"c\":[{\"v\":[14,45,0]},{\"v\":15},{\"v\":80}]},{\"c\":[{\"v\":[15,0,0]},{\"v\":25},{\"v\":80}]},{\"c\":[{\"v\":[15,15,0]},{\"v\":27},{\"v\":80}]},{\"c\":[{\"v\":[15,30,0]},{\"v\":26},{\"v\":80}]},{\"c\":[{\"v\":[15,45,0]},{\"v\":29},{\"v\":80}]},{\"c\":[{\"v\":[16,0,0]},{\"v\":34},{\"v\":80}]},{\"c\":[{\"v\":[16,15,0]},{\"v\":42},{\"v\":140}]},{\"c\":[{\"v\":[16,30,0]},{\"v\":67},{\"v\":140}]},{\"c\":[{\"v\":[16,45,0]},{\"v\":72},{\"v\":140}]}]}";

    @Inject
    public Metrics(ObjectMapper json, Chat chat, KieSession ksession, SessionClock clock) {
        this.ksession = ksession;
        this.json = json;
        this.chat = chat;
        this.clock = clock;
        seriesValues = new LinkedHashMap();
        seriesValues.put("DynamoDB.ProvisionedReadCapacityUnits", new Integer[]{
            80, 80, 80, 80, 80, 140, 140, 140, 140, 140, 140, 140, 140,
            140, 140, 140, 140, 140, 140, 140, 140, 80, 80, 80, 80,
            80, 80, 80, 80, 80, 80, 80, 80, 140, 140, 140});
        seriesValues.put("DynamoDB.ConsumedReadCapacityUnits", new Integer[]{
            15, 16, 18, 20, 19, 32, 36, 48, 47, 52, 58, 98, 75, 63, 67,
            63, 65, 52, 43, 35, 21, 22, 26, 19, 23, 25, 18, 15, 25, 27,
            26, 29, 34, 42, 67, 72});
        seriesValues.put("DynamoDB.ThrottledRequests", new Integer[]{
            0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 1, 0, 0, 0});
        seriesValues.put("DynamoDB.UserErrors", new Integer[]{
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0});
        seriesValues.put("Lambda.Invocations", new Integer[]{
            15, 16, 18, 20, 19, 32, 36, 48, 47, 52, 58, 98, 75, 63, 67,
            63, 65, 52, 43, 35, 21, 22, 26, 19, 23, 25, 18, 15, 25, 27,
            26, 29, 34, 42, 67, 72});
        seriesValues.put("Lambda.Throttles", new Integer[]{
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0});
        seriesValues.put("Lambda.Errors", new Integer[]{
            0, 0, 0, 0, 0, 1, 0, 2, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 1, 0, 0, 0});

        thresholdSeriesValues = new LinkedHashMap();
        thresholdSeriesValues.put("DynamoDB.ConsumedReadCapacityUnits", new Integer[]{
            60, 60, 60, 60, 60, 105, 105, 105, 105, 105, 105, 105, 105,
            105, 105, 105, 105, 105, 105, 105, 105, 60, 60, 60, 60,
            60, 60, 60, 60, 60, 60, 60, 60, 105, 105});
        thresholdSeriesValues.put("DynamoDB.ThrottledRequests", new Integer[]{
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2});
        thresholdSeriesValues.put("Lambda.Errors", new Integer[]{
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2});
    }

}
