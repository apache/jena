/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.permissions.graph;

import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.Factory;
import org.apache.jena.permissions.MockSecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluatorParameters;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies that messages are properly filtered when sent to listeners.
 *
 */
@RunWith(value = SecurityEvaluatorParameters.class)
public class GraphEventManagerTest {
    private final GraphEventManager manager;
    private final Graph g;
    private final SecuredGraph sg;
    private final SecurityEvaluator securityEvaluator;
    private Triple[] tripleArray;

    private final RecordingGraphListener listener;

    public GraphEventManagerTest(final MockSecurityEvaluator securityEvaluator) {
        this.securityEvaluator = securityEvaluator;
        g = GraphFactory.createDefaultGraph();

        sg = Factory.getInstance(securityEvaluator, "http://example.com/testGraph", g);
        manager = sg.getEventManager();
        listener = new RecordingGraphListener();
        manager.register(listener);

    }

    @Test
    public void notifyAddTest() {
        Object principal = securityEvaluator.getPrincipal();
        final Set<Action> ADD = SecurityEvaluator.Util.asSet(new Action[] { Action.Create, Action.Read });
        g.add(tripleArray[0]);
        if (securityEvaluator.evaluateAny(principal, ADD, sg.getModelNode())) {
            Assert.assertTrue("Should recorded add", listener.isAdd());
        } else {
            Assert.assertFalse("Should not have recorded add", listener.isAdd());
        }
        g.delete(Triple.ANY);
        listener.reset();
    }

    @Test
    public void notifyDeleteTest() {
        Object principal = securityEvaluator.getPrincipal();
        final Set<Action> DELETE = SecurityEvaluator.Util.asSet(new Action[] { Action.Delete, Action.Read });
        g.delete(tripleArray[0]);
        if (securityEvaluator.evaluateAny(principal, DELETE, sg.getModelNode())) {
            Assert.assertTrue("Should have recorded delete", listener.isDelete());
        } else {
            Assert.assertFalse("Should not have recorded delete", listener.isDelete());
        }

        listener.reset();

    }

    @Test
    public void notifyEventTest() {
        g.getEventManager().notifyEvent(g, "Foo");
        Assert.assertTrue("Should recorded delete", listener.isEvent());
        listener.reset();
        // final RecordingGraphListener listener2 = new
        // RecordingGraphListener();
        // g.getEventManager().register(listener2);
        sg.getEventManager().notifyEvent(sg, "Foo");
        Assert.assertTrue("Should recorded delete", listener.isEvent());
        // Assert.assertTrue("Should recorded delete", listener2.isEvent());
        listener.reset();

    }

    @Before
    public void setup() {
        tripleArray = new Triple[] {
                Triple.create(NodeFactory.createURI("http://example.com/1"), NodeFactory.createURI("http://example.com/v"),
                        NodeFactory.createBlankNode()),
                Triple.create(NodeFactory.createURI("http://example.com/2"), NodeFactory.createURI("http://example.com/v"),
                        NodeFactory.createBlankNode()),
                Triple.create(NodeFactory.createURI("http://example.com/3"), NodeFactory.createURI("http://example.com/v"),
                        NodeFactory.createBlankNode()) };

    }
}
