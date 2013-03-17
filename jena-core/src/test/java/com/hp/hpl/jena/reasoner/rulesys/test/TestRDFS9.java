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

package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.Union;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test harness used in debugging some issues with execution
 * of modified versions of rule rdfs9.
 */
public class TestRDFS9 extends TestCase {
    
    /**
     * Boilerplate for junit
     */ 
    public TestRDFS9( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite(TestRDFS9.class);
    }  

    /**
     * Test a type inheritance example.
     */
    public void testRDFSInheritance() {
        Node C1 = NodeFactory.createURI("C1");
        Node C2 = NodeFactory.createURI("C2");
        Node C3 = NodeFactory.createURI("C3");
        Node C4 = NodeFactory.createURI("C4");
        Node D = NodeFactory.createURI("D");
        Node a = NodeFactory.createURI("a");
        Node b = NodeFactory.createURI("b");
        Node p = NodeFactory.createURI("p");
        Node q = NodeFactory.createURI("q");
        Node r = NodeFactory.createURI("r");
        Node sC = RDFS.subClassOf.asNode();
        Node ty = RDF.type.asNode();
        
        Graph tdata = Factory.createGraphMem();
        tdata.add(new Triple(C1, sC, C2));
        tdata.add(new Triple(C2, sC, C3));
        tdata.add(new Triple(p, RDFS.subPropertyOf.asNode(), q));
        tdata.add(new Triple(q, RDFS.subPropertyOf.asNode(), r));
        tdata.add(new Triple(r, RDFS.domain.asNode(), D));
        Graph data = Factory.createGraphMem();
        data.add(new Triple(a, p, b));
        InfGraph igraph = ReasonerRegistry.getRDFSReasoner().bind(new Union(tdata, data));
        TestUtil.assertIteratorValues(this, igraph.find(a, ty, null),
        new Object[] {
            new Triple(a, ty, D),
            new Triple(a, ty, RDFS.Resource.asNode()),
        });
        // Check if first of these is in the wildcard listing
        boolean ok = false;
        Triple target = new Triple(a,ty,D);
        for (Iterator<Triple> i = igraph.find(null,ty,null); i.hasNext(); ) {
            Triple t = i.next();
            if (t.equals(target)) {
                ok = true;
                break;
            }
        }
        assertTrue(ok);
        igraph = ReasonerRegistry.getRDFSReasoner().bindSchema(tdata).bind(data);
        TestUtil.assertIteratorValues(this, igraph.find(a, ty, null),
        new Object[] {
            new Triple(a, ty, D),
            new Triple(a, ty, RDFS.Resource.asNode()),
        });
        // Check if first of these is in the wildcard listing
        ok = false;
        target = new Triple(a,ty,D);
        for (Iterator<Triple> i = igraph.find(null,ty,null); i.hasNext(); ) {
            Triple t = i.next();
            if (t.equals(target)) {
                ok = true;
                break;
            }
        }
        assertTrue(ok);
    }
}
