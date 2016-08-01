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

import ai.lana.aws.cloudwatch.MetricAlarm;
import ai.lana.aws.cloudwatch.MetricValue;
import ws.salient.chat.Intent;
import ws.salient.chat.Response;
import com.ibm.icu.text.RuleBasedNumberFormat;
import static com.ibm.icu.text.RuleBasedNumberFormat.SPELLOUT;
import java.text.ParseException;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.kie.api.runtime.KieSession;
import org.kie.api.time.SessionClock;

@Singleton
public class DynamoDB {

    private KieSession session;
    private final RuleBasedNumberFormat numberFormat;
    private SessionClock clock;

    @Inject
    public DynamoDB(Locale locale, KieSession session, SessionClock clock) {
        this.session = session;
        this.clock = clock;
        numberFormat = new RuleBasedNumberFormat(locale,
                SPELLOUT);
    }

    private Number parse(String number) {
        try {
            Number capacity = numberFormat.parse(number);
            return capacity;
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Response updateProvisionedReads(Intent intent, MetricValue value, MetricAlarm alarm, Table table, String number) {
        Response response;
        Number capacity = parse(number);
        if (capacity.intValue() < 1) {
            response = new Response(intent, "dynamodb.update.min");
        } else if (capacity.intValue() > 10000) {
            response = new Response(intent, "dynamodb.update.max");
        } else {
            table.setProvisionedReadCapacityUnits(capacity.longValue());
            session.update(session.getFactHandle(table), table);
            session.insert(value);
            alarm.setThreshold((table.getProvisionedReadCapacityUnits() * 75L) / 100L);
            session.update(session.getFactHandle(alarm), alarm);;
            response = (new Response(intent, "dynamodb.update").putSingle("Resource", table.getName()));
        }
        return response;
    }

    //        RuleBasedNumberFormat format = new RuleBasedNumberFormat(locale,
//                     SPELLOUT);
//        
//        System.out.println(format.format(100L));
//        
//        Number result = format.parse("one hundred and five");
//        System.out.println(result.intValue() + "");
}
