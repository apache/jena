/******************************************************************
 * File:        TestReification.java
 * Created by:  Dave Reynolds
 * Created on:  11 Jan 2007
 * 
 * (c) Copyright 2007, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestReification.java,v 1.2 2007-01-11 15:31:36 chris-dollin Exp $
 *****************************************************************/

package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.List;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.test.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.PrintUtil;

import junit.framework.TestSuite;

public class TestReification extends AbstractTestReifier {
    
    /**
     * Boilerplate for junit
     */ 
    public TestReification( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestReification.class ); 
    }  
    
    public Graph getGraph()
        { return getGraph( ReificationStyle.Minimal ); }

    public Graph getGraph( ReificationStyle style )
        { return makeInfGraph( "", "", style ); }

    /**
     * Case 1: Rules construct a reified statement, is that
     * visible as reified statement in the InfGraph?
     */
    public void testSimpleReification() {
        String rules =  
            "[r1: (?x eh:p ?o) -> (?o rdf:type rdf:Statement) (?o rdf:subject ?x)" +
            "                         (?o rdf:predicate eh:q) (?o rdf:object 42)]";
        Model m = makeInfModel(rules, "r1 p r" );
        TestUtil.assertIteratorLength(m.listReifiedStatements(), 1);
    }

    /**
     * Case 1: Rules complete an exisiting partially reified statement.
     */
    public void SUPPRESStestReificationCompletion() {
        String rules =  
            "[r1: (?x rdf:subject ?s) (?x rdf:predicate ?p) -> (?x rdf:object eh:bar)]";
        Model m = makeInfModel(rules, "r1 rdf:type rdf:Statement; r1 rdf:subject foo; r1 rdf:predicate p" );
        RSIterator i = m.listReifiedStatements();
        assertTrue(i.hasNext());
        assertEquals( triple("foo p bar"), i.nextRS().getStatement().asTriple());
        assertFalse(i.hasNext());
    }
    
    /**
     * Internal helper: create an InfGraph with given rule set and base data.
     * The base data is encoded in kers-special RDF syntax.
     */    
    private InfGraph makeInfGraph(String rules, String data, ReificationStyle style ) {
        PrintUtil.registerPrefix("eh", "eh:/");
        Graph base = graphWith(data);
        List ruleList = Rule.parseRules(rules);
        return new FBRuleReasoner(ruleList).bind(base);
    }
    
    /**
     * Internal helper: create a Model which wraps an InfGraph with given rule set and base data.
     * The base data is encoded in kers-special RDF syntax.
     * @param style TODO
     */
    private Model makeInfModel( String rules, String data, ReificationStyle style ) {
        return ModelFactory.createModelForGraph( makeInfGraph(rules, data, style ) );
    }
        
    private Model makeInfModel( String rules, String data ) {
        return makeInfModel( rules, data, ReificationStyle.Minimal );
    }
    
}


/*
    (c) Copyright 2007 Hewlett-Packard Development Company, LP
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
