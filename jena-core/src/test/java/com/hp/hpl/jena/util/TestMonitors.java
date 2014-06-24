/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.util;

import java.util.*;

import com.hp.hpl.jena.util.MonitorGraph;
import com.hp.hpl.jena.util.MonitorModel;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.RecordingModelListener;
import com.hp.hpl.jena.reasoner.test.TestUtil;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for MonitorGraph implementation.
 */

public class TestMonitors extends TestCase {

    /**
     * Boilerplate for junit
     */ 
    public TestMonitors( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestMonitors.class ); 
    }  

    // constants used in the tests 
    String NS = "http://jena.hpl.hp.com/test#";
    Node a = NodeCreateUtils.create(NS + "a");
    Node p = NodeCreateUtils.create(NS + "p");
    Triple t1 = new Triple(a, p, NodeCreateUtils.create(NS + "v1"));
    Triple t2 = new Triple(a, p, NodeCreateUtils.create(NS + "v2"));
    Triple t3 = new Triple(a, p, NodeCreateUtils.create(NS + "v3"));
    Triple t4 = new Triple(a, p, NodeCreateUtils.create(NS + "v4"));
    Triple t5 = new Triple(a, p, NodeCreateUtils.create(NS + "v5"));
    Triple t6 = new Triple(a, p, NodeCreateUtils.create(NS + "v6"));
    
    /**
     * Basic graph level test, no monitoring
     */
    public void testBasics() {
        Graph base = Factory.createGraphMem();
        MonitorGraph monitor = new MonitorGraph(base);
        
        // base data
        base.add(t1);
        base.add(t2);
        base.add(t3);
        
        // Test changes from empty
        List<Triple> additions = new ArrayList<>();
        List<Triple> deletions = new ArrayList<>();
        monitor.snapshot(additions, deletions);
        TestUtil.assertIteratorValues(this, additions.iterator(), new Object[] {t1, t2, t3});
        TestUtil.assertIteratorValues(this, deletions.iterator(), new Object[] {});
        
        // Make some new changes
        base.add(t4);
        base.add(t5);
        base.delete(t1);
        base.delete(t2);
        
        additions.clear();
        deletions.clear();
        monitor.snapshot(additions, deletions);
        TestUtil.assertIteratorValues(this, additions.iterator(), new Object[] {t4, t5});
        TestUtil.assertIteratorValues(this, deletions.iterator(), new Object[] {t1, t2});
        TestUtil.assertIteratorValues(this, monitor.find(Node.ANY, Node.ANY, Node.ANY), new Object[] {t3, t4, t5});
    }
    
    /**
     * Monitoring test.
     */
    public void testListener() {
        Graph base = Factory.createGraphMem();
        MonitorGraph monitor = new MonitorGraph(base);
        RecordingListener listener = new RecordingListener();
        monitor.getEventManager().register(listener);
        // base data
        base.add(t1);
        base.add(t2);
        base.add(t3);
        
        listener.has(new Object[]{});
        
        // Test changes from empty
        List<Triple> additions = new ArrayList<>();
        List<Triple> deletions = new ArrayList<>();
        monitor.snapshot(additions, deletions);
        TestUtil.assertIteratorValues(this, additions.iterator(), new Object[] {t1, t2, t3});
        TestUtil.assertIteratorValues(this, deletions.iterator(), new Object[] {});
        
        listener.assertHas(new Object[] {"addList", monitor, additions, "deleteList", monitor, deletions});
        listener.clear();
        
        // Make some new changes
        base.add(t4);
        base.add(t5);
        base.delete(t1);
        base.delete(t2);
        
        additions.clear();
        deletions.clear();
        monitor.snapshot(additions, deletions);
        TestUtil.assertIteratorValues(this, additions.iterator(), new Object[] {t4, t5});
        TestUtil.assertIteratorValues(this, deletions.iterator(), new Object[] {t1, t2});
        TestUtil.assertIteratorValues(this, monitor.find(Node.ANY, Node.ANY, Node.ANY), new Object[] {t3, t4, t5});
        
        listener.assertHas(new Object[] {"addList", monitor, additions, "deleteList", monitor, deletions});
        listener.clear();
    }
    
    /**
     * Test model level access
     */
    public void testModelMonitor() {
        Model base = ModelFactory.createDefaultModel();
        // Constants for model level test
        Resource ar = base.createResource(NS + "a");
        Property pr = base.createProperty(NS + "p");
        Statement s1 = base.createStatement(ar, pr, "1");
        Statement s2 = base.createStatement(ar, pr, "2");
        Statement s3 = base.createStatement(ar, pr, "3");
        Statement s4 = base.createStatement(ar, pr, "4");
        Statement s5 = base.createStatement(ar, pr, "5");
        
        MonitorModel monitor = new MonitorModel(base);
        RecordingModelListener listener = new RecordingModelListener();
        monitor.register(listener);
        
        // base data
        base.add(s1);
        base.add(s2);
        base.add(s3);
        
        // Test changes from empty
        List<Statement> additions = new ArrayList<>();
        List<Statement> deletions = new ArrayList<>();
        monitor.snapshot(additions, deletions);
        TestUtil.assertIteratorValues(this, additions.iterator(), new Object[] {s1, s2, s3});
        TestUtil.assertIteratorValues(this, deletions.iterator(), new Object[] {});
        listener.assertHas(new Object[] {"addList", additions, "removeList", deletions});
        listener.clear();
        
        // Make some new changes
        base.add(s4);
        base.add(s5);
        base.remove(s1);
        base.remove(s2);
        
        additions.clear();
        deletions.clear();
        monitor.snapshot(additions, deletions);
        TestUtil.assertIteratorValues(this, additions.iterator(), new Object[] {s4, s5});
        TestUtil.assertIteratorValues(this, deletions.iterator(), new Object[] {s1, s2});
        TestUtil.assertIteratorValues(this, monitor.listStatements(), new Object[] {s3, s4, s5});
        
        listener.assertHas(new Object[] {"addList", additions, "removeList", deletions});
        listener.clear();
    }
   
}
