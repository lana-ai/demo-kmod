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

package ai.lana.aws.cloudwatch;

import com.google.common.collect.ComparisonChain;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Metric implements Comparable<Metric>, Serializable {
   
    private static final long serialVersionUID = 1L;
    
    private String namespace;
    private String metricName;
    private Boolean error = false;
    private Map<String, String> dimensions = new LinkedHashMap();

    public String getNamespace() {
        return namespace;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Map<String, String> getDimensions() {
        return dimensions;
    }

    public void setDimensions(Map<String, String> dimensions) {
        this.dimensions = dimensions;
    }

    public Boolean isError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }
    
    public Metric withError(Boolean error) {
        this.error = error;
        return this;
    }

    public Metric withDimension(String name, String value) {
        if (dimensions == null) {
            dimensions = new LinkedHashMap();
        }
        dimensions.put(name, value);
        return this;
    }

    public Metric withNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public Metric withMetricName(String metricName) {
        this.metricName = metricName;
        return this;
    }

    @Override
    public String toString() {
        return "Metric{" + "namespace=" + namespace + ", metricName=" + metricName + ", dimensions=" + dimensions + '}';
    }

    @Override
    public int compareTo(Metric metric) {
        return ComparisonChain.start().compare(namespace, metric.namespace)
                .compare(metricName, metric.metricName)
                .result();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.namespace);
        hash = 47 * hash + Objects.hashCode(this.metricName);
        hash = 47 * hash + Objects.hashCode(this.dimensions);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Metric other = (Metric) obj;
        if (!Objects.equals(this.namespace, other.namespace)) {
            return false;
        }
        if (!Objects.equals(this.metricName, other.metricName)) {
            return false;
        }
        return Objects.equals(this.dimensions, other.dimensions);
    }
    
    

}
