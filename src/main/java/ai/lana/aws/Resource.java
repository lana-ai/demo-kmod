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

package ai.lana.aws;

import ai.lana.aws.cloudwatch.Metric;
import ws.salient.chat.Aliases;
import ws.salient.chat.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Resource implements Entity, Serializable {
    
    private Aliases aliases = new Aliases();
    private List<Metric> metrics = new LinkedList();
    private Service service;
    
    public Resource() {
    }

    public Resource(Service service, String name) {
        this.service = service;
        aliases.withAlias(name);
    }
    
    @Override
    public Aliases getAliases() {
        return aliases;
    }
    
    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setAliases(Aliases aliases) {
        this.aliases = aliases;
    }

    @JsonIgnore
    public String getMetricNamespace() {
        return metrics.get(0).getNamespace();
    }

    @JsonIgnore
    public List<String> getMetricNames() {
        return metrics.stream().map((metric) -> {
            return metric.getMetricName();
        }).collect(Collectors.toList());
    }
    
    public List<String> getMetricNames(Boolean error) {
        return metrics.stream().filter((metric) -> {
            return metric.isError().equals(error);
        }).map((metric) -> {
            return metric.getMetricName();
        }).collect(Collectors.toList());
    }

    public List<Metric> getMetrics(Boolean error) {
        return metrics.stream().filter((metric) -> {
            return metric.isError().equals(error);
        }).collect(Collectors.toList());
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }
    
    public Object getAttachment(String color, String fieldTitle) {
        Map attachment = new LinkedHashMap();
        attachment.put("color", color);
        Map nameField = new LinkedHashMap();
        nameField.put("title", fieldTitle);
        nameField.put("value", getName());
        nameField.put("short", true);
        List fields =  new LinkedList();
        fields.add(nameField);
        attachment.put("fields",fields);
        return attachment;
    }

}
