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

import static ai.lana.aws.cloudwatch.MetricAlarm.Comparison.GREATER_THAN;
import static ai.lana.aws.cloudwatch.MetricAlarm.Comparison.LESS_THAN;
import static ai.lana.aws.cloudwatch.MetricAlarm.State.ERROR;
import static ai.lana.aws.cloudwatch.MetricAlarm.State.OK;
import static ai.lana.aws.cloudwatch.MetricAlarm.Statistic.SUM;
import java.io.Serializable;
import org.kie.api.definition.type.Role;

@Role(Role.Type.FACT)
public class MetricAlarm implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static enum Comparison {
        GREATER_THAN,
        LESS_THAN
    }
    public static enum State {
        OK, ERROR
    }
    
    public static enum Statistic {
        SUM
    }
    
    private Metric metric;
    private State stateValue = OK;
    private Long threshold;
    private Statistic statistic = SUM;
    private Comparison comparison = GREATER_THAN;

    public Metric getMetric() {
        return metric;
    }

    public State getStateValue() {
        return stateValue;
    }

    public Long getThreshold() {
        return threshold;
    }

    public Statistic getStatistic() {
        return statistic;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public void setStateValue(State stateValue) {
        this.stateValue = stateValue;
    }

    public void setThreshold(Long threshold) {
        this.threshold = threshold;
    }

    public void setStatistic(Statistic statistic) {
        this.statistic = statistic;
    }

    public MetricAlarm withMetric(Metric metric) {
        this.metric = metric;
        return this;
    }

    public MetricAlarm withStateValue(State stateValue) {
        this.stateValue = stateValue;
        return this;
    }

    public MetricAlarm withThreshold(Long threshold) {
        this.threshold = threshold;
        return this;
    }

    public MetricAlarm withStatistic(Statistic statistic) {
        this.statistic = statistic;
        return this;
    }

    public Comparison getComparison() {
        return comparison;
    }

    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }
    
    public MetricAlarm withComparison(Comparison comparison) {
        this.comparison = comparison;
        return this;
    }
    
    public State getState(Long value) {
        State state = OK;
        if ( (comparison.equals(GREATER_THAN) && (value > threshold)) ||
             (comparison.equals(LESS_THAN) && (value < threshold))   ) {
             state = ERROR;
        }
        return state;
    }

    
    
    
    

}
