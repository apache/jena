/******************************************************************
 * File:        TestRDFS9.java
 * Created by:  Dave Reynolds
 * Created on:  24-Jun-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestRDFS9.java,v 1.2 2003-08-27 13:11:16 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.Union;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test harness used in debugging some issues with execution
 * of modified versions of rule rdfs9.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-08-27 13:11:16 $
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
        Node C1 = Node.createURI("C1");
        Node C2 = Node.createURI("C2");
        Node C3 = Node.createURI("C3");
        Node C4 = Node.createURI("C4");
        Node D = Node.createURI("D");
        Node a = Node.createURI("a");
        Node b = Node.createURI("b");
        Node p = Node.createURI("p");
        Node q = Node.createURI("q");
        Node r = Node.createURI("r");
        Node sC = RDFS.subClassOf.asNode();
        Node ty = RDF.type.asNode();
        
        Graph tdata = new GraphMem();
        tdata.add(new Triple(C1, sC, C2));
        tdata.add(new Triple(C2, sC, C3));
        tdata.add(new Triple(p, RDFS.subPropertyOf.asNode(), q));
        tdata.add(new Triple(q, RDFS.subPropertyOf.asNode(), r));
        tdata.add(new Triple(r, RDFS.domain.asNode(), D));
        Graph data = new GraphMem();
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
        for (Iterator i = igraph.find(null,ty,null); i.hasNext(); ) {
            Triple t = (Triple)i.next();
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
        for (Iterator i = igraph.find(null,ty,null); i.hasNext(); ) {
            Triple t = (Triple)i.next();
            if (t.equals(target)) {
                ok = true;
                break;
            }
        }
        assertTrue(ok);
    }
}


/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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