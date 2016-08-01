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
import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;

@Role(Role.Type.EVENT)
@Expires("1h")
public class MetricValue implements Comparable<MetricValue>, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Metric metric;
    private Long sum;
    private Long timestamp;

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }
    
    public MetricValue withMetric(Metric metric) {
        this.metric = metric;
        return this;
    }

    public Long getSum() {
        return sum;
    }

    public void setSum(Long sum) {
        this.sum = sum;
    }
    
    public MetricValue withSum(Long sum) {
        this.sum = sum;
        return this;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public MetricValue withTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {
        return "MetricValue{" + "metric=" + metric + ", sum=" + sum + ", timestamp=" + timestamp + '}';
    }
    
    @Override
    public int compareTo(MetricValue value) {
        return ComparisonChain.start().compare(metric, value.metric)
                .compare(timestamp, value.timestamp)
                .result();
    }
    
    
}
