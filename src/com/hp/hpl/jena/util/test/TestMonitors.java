/******************************************************************
 * File:        TestMonitorGraph.java
 * Created by:  Dave Reynolds
 * Created on:  12-May-2005
 * 
 * (c) Copyright 2005, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestMonitors.java,v 1.4 2007-01-02 11:53:25 andy_seaborne Exp $
 *****************************************************************/

package com.hp.hpl.jena.util.test;

import java.util.*;

import com.hp.hpl.jena.util.MonitorGraph;
import com.hp.hpl.jena.util.MonitorModel;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.RecordingListener;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.RecordingModelListener;
import com.hp.hpl.jena.reasoner.test.TestUtil;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for MonitorGraph implementation.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $
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
    Node a = Node.create(NS + "a");
    Node p = Node.create(NS + "p");
    Triple t1 = new Triple(a, p, Node.create(NS + "v1"));
    Triple t2 = new Triple(a, p, Node.create(NS + "v2"));
    Triple t3 = new Triple(a, p, Node.create(NS + "v3"));
    Triple t4 = new Triple(a, p, Node.create(NS + "v4"));
    Triple t5 = new Triple(a, p, Node.create(NS + "v5"));
    Triple t6 = new Triple(a, p, Node.create(NS + "v6"));
    
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
        List additions = new ArrayList();
        List deletions = new ArrayList();
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
        List additions = new ArrayList();
        List deletions = new ArrayList();
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
        List additions = new ArrayList();
        List deletions = new ArrayList();
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


/*
    (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
