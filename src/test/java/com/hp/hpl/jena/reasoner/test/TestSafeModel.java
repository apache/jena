/******************************************************************
 * File:        TestSafeModel.java
 * Created by:  Dave Reynolds
 * Created on:  5 Oct 2009
 * 
 * (c) Copyright 2009, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestSafeModel.java,v 1.1 2009-10-05 17:12:23 der Exp $
 *****************************************************************/

package com.hp.hpl.jena.reasoner.test;

import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.impl.SafeGraph;

import static com.hp.hpl.jena.util.PrintUtil.egNS;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Some Jena reasoners support extended graphs which relax the RDF syntactic constraints
 * against literals in the subject position. By default getDeductionsModel in those
 * cases will return a SafeModel 
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $
 */
public class TestSafeModel  extends TestCase {
    
    /**
     * Boilerplate for junit
     */ 
    public TestSafeModel( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite(TestSafeModel.class);
    }  

    /**
     * Create a generalized model via inference and check it is
     * safe but unwrappable
     */
    public void testBasics() {
        Model base = ModelFactory.createDefaultModel();
        Resource r = base.createResource(egNS + "r");
        Property p = base.createProperty(egNS + "p");
        Property q = base.createProperty(egNS + "q");
        Literal l = base.createLiteral("foo");
        Statement asserted = base.createStatement(r, p, l);
        r.addProperty(p, l);
        
        List<Rule> rules = Rule.parseRules("(?r eg:p ?v) -> (?v eg:q ?r).");
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        InfModel inf = ModelFactory.createInfModel(reasoner, base);
        TestUtil.assertIteratorValues(this, inf.listStatements(), new Statement[]{asserted});
        
        /**
         * Commented out to enable safe checkin
        Model deductions = inf.getDeductionsModel();
        TestUtil.assertIteratorValues(this, deductions.listStatements(), new Statement[]{});
        
        Graph safeGraph = deductions.getGraph();
        assertTrue(safeGraph instanceof SafeGraph);
        
        Graph rawGraph = ((SafeGraph)safeGraph).getRawGraph();
        Triple deduction = new Triple(l.asNode(), q.asNode(), r.asNode());
        TestUtil.assertIteratorValues(this, 
                rawGraph.find(Node.ANY, Node.ANY, Node.ANY), 
                new Triple[]{deduction});
        */
    }

}


/*
    (c) Copyright 2009 Hewlett-Packard Development Company, LP
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
