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

package ai.lana.aws.dynamodb;

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

public class Table extends Resource implements Serializable, Attachable {

    private static final long serialVersionUID = 1L;
    
    private String tableName;
    private Long provisionedReadCapacityUnits;
    private Long provisionedWriteCapacityUnits;

    public Table() {
    }

    public Table(Service service, String tableName) {
        super(service, tableName);
        this.tableName = tableName;
        List<Metric> metrics = new LinkedList();
        metrics.add(new Metric()
                .withNamespace("DynamoDB")
                .withMetricName("ProvisionedReadCapacityUnits")
                .withDimension("TableName", tableName));
        metrics.add(new Metric()
                .withNamespace("DynamoDB")
                .withMetricName("ConsumedReadCapacityUnits")
                .withDimension("TableName", tableName));
        metrics.add(new Metric()
                .withNamespace("DynamoDB")
                .withMetricName("ThrottledRequests")
                .withDimension("TableName", tableName)
                .withError(Boolean.TRUE));
        setMetrics(metrics);
    }

    public String getTableName() {
        return tableName;
    }

    public Long getProvisionedReadCapacityUnits() {
        return provisionedReadCapacityUnits;
    }

    public Long getProvisionedWriteCapacityUnits() {
        return provisionedWriteCapacityUnits;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setProvisionedReadCapacityUnits(Long provisionedReadCapacityUnits) {
        this.provisionedReadCapacityUnits = provisionedReadCapacityUnits;
    }

    public void setProvisionedWriteCapacityUnits(Long provisionedWriteCapacityUnits) {
        this.provisionedWriteCapacityUnits = provisionedWriteCapacityUnits;
    }

    public Table withTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public Table withProvisionedReadCapacityUnits(Long provisionedReadCapacityUnits) {
        this.provisionedReadCapacityUnits = provisionedReadCapacityUnits;
        return this;
    }

    public Table withProvisionedWriteCapacityUnits(Long provisionedWriteCapacityUnits) {
        this.provisionedWriteCapacityUnits = provisionedWriteCapacityUnits;
        return this;
    }

    public Table withAlias(String alias) {
        getAliases().withAlias(alias);
        return this;
    }

    @Override
    public String toString() {
        return "Table{" + "tableName=" + tableName + ", provisionedReadCapacityUnits=" + provisionedReadCapacityUnits + ", provisionedWriteCapacityUnits=" + provisionedWriteCapacityUnits + '}';
    }

    @JsonIgnore
    @Override
    public String getName() {
        return tableName;
    }

    @JsonIgnore
    @Override
    public List<String> getTypes() {
        return Arrays.asList("Resource");
    }

    @Override
    public Object getAttachment(Context context) {
        return super.getAttachment("#2a75b5", "TableName");
    }

}
