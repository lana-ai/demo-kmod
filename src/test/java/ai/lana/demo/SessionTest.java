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

import ai.lana.aws.dynamodb.DynamoDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static junit.framework.Assert.fail;
import org.drools.core.process.instance.impl.DefaultWorkItemManager;
import org.drools.core.process.instance.impl.WorkItemImpl;
import org.drools.core.time.SessionPseudoClock;
import org.jbpm.bpmn2.handler.WorkItemHandlerRuntimeException;
import org.jbpm.process.core.context.exception.ExceptionScope;
import org.jbpm.process.instance.context.exception.ExceptionScopeInstance;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.process.NodeInstanceContainer;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import ws.salient.chat.Chat;
import ws.salient.chat.Intent;
import ws.salient.chat.Message;
import ws.salient.chat.Render;
import ws.salient.luis.Luis;

public class SessionTest {

    KieContainer kcontainer;
    KieBase kbase;
    KieSession ksession;
    ObjectMapper json;
    Map<String, Object> replyTo;
    RuleFlowProcessInstance process;
    Long workItemId;
    Chat chat;
    ResourceBundle bundle;
    Metrics metrics;
    WorkItem workItem;
    SessionPseudoClock clock;
    Properties properties;
    ExecutorService service;
    Locale locale = Locale.US;
    DynamoDB dynamodb;

    @Before
    public void before() {
        kcontainer = KieServices.Factory.get().getKieClasspathContainer();
        kbase = kcontainer.getKieBase("ai.lana.demo");
        KieSessionConfiguration config = KieServices.Factory.get().newKieSessionConfiguration();
        config.setOption(ClockTypeOption.get("pseudo"));
        ksession = kbase.newKieSession(config, null);
        clock = ksession.getSessionClock();
        clock.advanceTime(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        ksession.addEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterNodeTriggered(ProcessNodeTriggeredEvent pnte) {
                ksession.fireAllRules();
            }
        });
        json = new ObjectMapper();
        properties = new Properties();
        properties.put("ws.salient.luis.appId", "luis1234");
        properties.put("ws.salient.subscriptionKey", "examplekey");
        properties.put("ws.salient.chat.pusher.appId", "pusher1234");
        properties.put("ws.salient.chat.pusher.key", "examplekey");

        chat = new Chat(ksession, kbase, properties, kcontainer.getClassLoader(), locale, clock, ZoneOffset.UTC);
        metrics = new Metrics(json, chat, ksession, clock);
        
        dynamodb = new DynamoDB(locale, ksession, clock);
        ksession.setGlobal("clock", clock);
        ksession.setGlobal("chat", chat);
        ksession.setGlobal("metrics", metrics);
        ksession.setGlobal("luis", new Luis(properties, ksession));
        ksession.setGlobal("zoneId", ZoneOffset.UTC);
        ksession.setGlobal("dynamodb", dynamodb);

        bundle = ResourceBundle.getBundle("ai.lana.demo.chat", locale, kcontainer.getClassLoader());
         
        ksession.getWorkItemManager().registerWorkItemHandler("ws.salient.aws.lambda.FunctionHandler",
                new WorkItemHandler() {
                    @Override
                    public void abortWorkItem(WorkItem workItem,
                            WorkItemManager manager) {

                    }
                    @Override
                    public void executeWorkItem(WorkItem item,
                            WorkItemManager manager) {
                        workItem = item;
                        System.out.println("execute: " + workItem);
                    }
                });
        ksession.fireAllRules();
        replyTo = new LinkedHashMap();
        replyTo.put("pusher", Collections.singletonMap("channelId", "1234567"));

    }

    protected void handleException(Exception ex) {
        WorkItemNodeInstance node = getNodeInstance(workItem.getId());
        ExceptionScopeInstance exceptionScopeInstance = (ExceptionScopeInstance) node.resolveContextInstance(ExceptionScope.EXCEPTION_SCOPE, WorkItemHandlerRuntimeException.class.getName());
        exceptionScopeInstance.handleException(WorkItemHandlerRuntimeException.class.getName(), new WorkItemHandlerRuntimeException(ex));
    }
    
    public WorkItemNodeInstance getNodeInstance(Long workItemId) {
        DefaultWorkItemManager manager = (DefaultWorkItemManager) ksession.getWorkItemManager();
        WorkItemImpl workItem = (WorkItemImpl) manager.getWorkItem(workItemId);
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());
        return getNodeInstance(workItem.getId(), processInstance.getNodeInstances());
    }

    protected WorkItemNodeInstance getNodeInstance(Long workItemId, Collection<org.kie.api.runtime.process.NodeInstance> nodeInstances) {
        for (org.kie.api.runtime.process.NodeInstance nodeInstance : nodeInstances) {
            if (nodeInstance instanceof WorkItemNodeInstance) {
                if (((WorkItemNodeInstance) nodeInstance).getWorkItemId() == workItemId) {
                    return (WorkItemNodeInstance) nodeInstance;
                }
            } else if (nodeInstance instanceof NodeInstanceContainer) {
                WorkItemNodeInstance found = getNodeInstance(workItemId, ((NodeInstanceContainer) nodeInstance).getNodeInstances());
                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    protected void assertMessage(String text, Intent intent) {
        assertMessage(text, intent, false);
    }

    protected void assertMessage(String text, Intent intent, boolean renderPage) {
        assertMessage(text, intent, null, null, null, renderPage);
    }

    protected void assertMessage(String text, Intent intent, String expectedReplyText) {
        this.assertMessage(text, intent, expectedReplyText, null, null);
    }

    protected void assertMessage(String text, Intent intent, String expectedReplyText, String expectedChartTitle, String expectedChartSubtitle) {
        this.assertMessage(text, intent, expectedReplyText, expectedChartTitle, expectedChartSubtitle, (expectedChartTitle != null));
    }

    protected void insertMessage(String text, Intent intent) {
        ksession.insert(new Message(text, replyTo));
        ksession.fireAllRules();
        int processCount = ksession.getProcessInstances().size();
        assertEquals(1, processCount);
        assertEquals("luis-intent", workItem.getParameter("functionName"));
        ksession.getWorkItemManager().completeWorkItem(
                workItem.getId(),
                Collections.singletonMap("intent", intent));
    }
    
    protected void assertMessage(String text, Intent intent, String expectedReplyText, String expectedChartTitle, String expectedChartSubtitle, boolean renderPage) {
        insertMessage(text, intent);
        if (renderPage) {
            assertEquals("chat-render", workItem.getParameter("functionName"));
            
            Render render = (Render) workItem.getParameter("render");
            assertNotNull(render);
            String content = render.getContents().get(0);
            assertNotNull(content);
            if (expectedChartTitle != null) {
                assertTrue(content.contains("title: '" + expectedChartTitle + "'"));
                assertTrue(content.contains("subtitle: '" + expectedChartSubtitle + "'"));
            }
            ksession.getWorkItemManager().completeWorkItem(
                    workItem.getId(),
                    Collections.singletonMap("render", new Render().withImageUrl("http://s3/image.png")));
        }
        assertEquals("chat-reply", workItem.getParameter("functionName"));
        Message reply = (Message) workItem.getParameter("reply");
        assertNotNull(reply);
        assertNotNull(reply.getReplyTo());
        if (expectedReplyText != null) {
            assertEquals(expectedReplyText, reply.getText());
        }
        ksession.getWorkItemManager().completeWorkItem(
                workItem.getId(),
                null);
        assertEquals(0, ksession.getProcessInstances().size());
        ksession.fireAllRules();
        
        // Test all objects are serializable
        try (ObjectOutputStream objectOut = new ObjectOutputStream(new ByteArrayOutputStream())) {
            List values = ksession.getFactHandles().stream().map((handle) -> {return ksession.getObject(handle);}).collect(Collectors.toList());
            objectOut.writeObject(values);
        } catch (IOException ex) {
            fail(ex.toString());
        }
    }
    
    public String getString(String key) {
        return chat.getText(key, null);
    }
    
    public String getString(String key, Map properties) {
        return chat.getText(key, properties);
    }

}
