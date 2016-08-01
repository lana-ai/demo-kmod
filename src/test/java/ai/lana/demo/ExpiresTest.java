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

import java.util.concurrent.TimeUnit;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import ws.salient.chat.Intent;

public class ExpiresTest extends SessionTest {
    
    public void messagesExpire() {
        
        assertMessage("Hi Lana", new Intent("Greet", 0.99));
        long factCount =  ksession.getFactCount();
        
        clock.advanceTime(1, TimeUnit.HOURS);
        assertMessage("What resources do I have?", new Intent("ListResources", 0.99));
        assertEquals(factCount + 2, ksession.getFactCount());
        
        clock.advanceTime(24, TimeUnit.HOURS);
        ksession.fireAllRules();
        assertEquals(factCount, ksession.getFactCount());
        
        clock.advanceTime(1, TimeUnit.HOURS);
        ksession.fireAllRules();
        assertEquals(factCount - 2, ksession.getFactCount());
    }   
    
    
}
