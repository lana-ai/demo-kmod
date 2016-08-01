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

package ai.lana.aws.lambda;

import ai.lana.aws.Resource;
import ai.lana.aws.Service;
import ai.lana.aws.cloudwatch.Metric;
import ws.salient.chat.Attachable;
import ws.salient.chat.Context;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Function extends Resource implements Attachable, Serializable {

    private static final long serialVersionUID = 1L;
    
    private String functionName;

    public Function() {
    }

    public Function(Service service, String functionName) {
        super(service, functionName);
        this.functionName = functionName;
        List<Metric> metrics = new LinkedList();
        metrics.add(new Metric()
                .withNamespace("Lambda")
                .withMetricName("Invocations")
                .withDimension("FunctionName", functionName));
        metrics.add(new Metric()
                .withNamespace("Lambda")
                .withMetricName("Errors")
                .withDimension("FunctionName", functionName)
                .withError(Boolean.TRUE));
        setMetrics(metrics);
    }

    public Function withAlias(String alias) {
        getAliases().withAlias(alias);
        return this;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public Function withFunctionName(String functionName) {
        this.functionName = functionName;
        return this;
    }

    @JsonIgnore
    @Override
    public String getName() {
        return functionName;
    }
    
    @JsonIgnore
    @Override
    public List<String> getTypes() {
        return Arrays.asList("Resource");
    }

    @Override
    public String toString() {
        return "Function{" + "functionName=" + functionName + '}';
    }
    
    @Override
    public Object getAttachment(Context context) {
        return super.getAttachment("#f0863d", "FunctionName");
    }

}
