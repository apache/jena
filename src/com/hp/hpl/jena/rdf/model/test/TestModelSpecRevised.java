/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestModelSpecRevised.java,v 1.16 2004-08-05 15:04:03 chris-dollin Exp $
*/
package com.hp.hpl.jena.rdf.model.test;

import java.util.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.TestSuite;

/**
     TestModelSpecRevised
     @author kers
*/
public class TestModelSpecRevised extends ModelTestBase
    {
    public TestModelSpecRevised( String name )
        { super( name ); }
    
    public static TestSuite suite()
         { return new TestSuite( TestModelSpecRevised.class ); }
//        {
//        TestSuite result = new TestSuite();
//        result.addTest( new TestModelSpecRevised( "testCreateReasoningModel" ) );
//        return result;
//        }
    
    public static final Resource A = resource( "_a" );

    protected static void assertSameRules( List wanted, List got )
        {
        assertEquals( new HashSet( wanted ), new HashSet( got ) );
        }
    
    public void testNoReasonerSuppliedException()
        { 
        Model rs = modelWithStatements( "_a rdf:type jms:ReasonerSpec" );
        Resource A = resource( "_a" );
        try { InfModelSpec.getReasonerFactory( A, rs ); fail( "should catch missing reasoner" ); }
        catch (NoReasonerSuppliedException e) { pass(); }
        }

    public void testNoSuchReasonerException()
        {
        Model rs = modelWithStatements( "_a rdf:type jms:ReasonerSpec; _a jms:reasoner nosuch:reasoner" );
        Resource A = resource( "_a" );
        try 
            { InfModelSpec.getReasonerFactory( A, rs ); 
            fail( "should catch unknown reasoner" ); }
        catch (NoSuchReasonerException e) 
            { assertEquals( "nosuch:reasoner", e.getURI() ); 
            assertContains( "nosuch:reasoner", e.toString() ); }
        }
    
    public void testGetOWLFBReasoner()
        {
        testGetReasoner( OWLFBRuleReasonerFactory.URI, OWLFBRuleReasoner.class );
        }
        
    public void testGetRDFSRuleReasoner()
        {
        testGetReasoner( RDFSRuleReasonerFactory.URI, RDFSRuleReasoner.class );
        }
    
    public void testRulesetURLFails()
        {
        String uri = GenericRuleReasonerFactory.URI;
        Model rs = modelWithStatements( "_a jms:reasoner " + uri + "; _a jms:ruleSetURL nowhere:man" );
        try { InfModelSpec.getReasonerFactory( A, rs ); fail( "should report ruleset failure" ); }
        catch (RulesetNotFoundException e) { assertEquals( "nowhere:man", e.getURI() ); }
        }
    
    public void testEmptyRulesetURLWorks()
        {
        testRuleSetURL( GenericRuleReasonerFactory.URI, file( "empty.rules" ) );
        }

    public void testNonEmptyRulesetURLWorks()
        {
        testRuleSetURL( GenericRuleReasonerFactory.URI, file( "example.rules" ) );
        }
    
    public void testMultipleRulesetURLsWork()
        {
        String factoryURI = GenericRuleReasonerFactory.URI;
        String rulesA = file( "example.rules" ), rulesB = file( "extra.rules" );
        List rules = append( Rule.rulesFromURL( rulesA ), Rule.rulesFromURL( rulesB ) );
        Model rs = modelWithStatements( "_a jms:reasoner " + factoryURI + "; _a jms:ruleSetURL " + rulesA + "; _a jms:ruleSetURL " + rulesB );
        ReasonerFactory rf = InfModelSpec.getReasonerFactory( A, rs );
        RuleReasoner gr = (RuleReasoner) rf.create( null );
        assertSameRules( rules, gr.getRules() );
        }
    
    public void testInlineRulesets()
        {
        String factoryURI = GenericRuleReasonerFactory.URI;
        String ruleStringA = "[rdfs2:  (?x ?p ?y), (?p rdfs:domain ?c) -> (?x rdf:type ?c)]";
        String ruleStringB = "[rdfs9:  (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]";
        List rules = append( Rule.parseRules( ruleStringA ), Rule.parseRules( ruleStringB ) );
        Model rs = rSpec( factoryURI )
            .add( A, JMS.ruleSet, resource( "onward:rules" ) )
            .add( resource( "onward:rules" ), JMS.hasRule, ruleStringA )
            .add( resource( "onward:rules" ), JMS.hasRule, ruleStringB );
        ReasonerFactory rf = InfModelSpec.getReasonerFactory( A, rs );
        RuleReasoner gr = (RuleReasoner) rf.create( null );
        assertSameRules( rules, gr.getRules() );
        }    
    
    public void testURLRulesets()
        {
        String factoryURI = GenericRuleReasonerFactory.URI;
        String ruleFileA = file( "example.rules" );
        String ruleFileB = file( "extra.rules" );
        List rules = append( Rule.rulesFromURL( ruleFileA ), Rule.rulesFromURL( ruleFileB ) );
        Model rs = rSpec( factoryURI )
            .add( A, JMS.ruleSet, resource( "onward:rules" ) )
            .add( resource( "onward:rules" ), JMS.ruleSetURL, resource( ruleFileA ) )
            .add( resource( "onward:rules" ), JMS.ruleSetURL, resource( ruleFileB ) );
        ReasonerFactory rf = InfModelSpec.getReasonerFactory( A, rs );
        RuleReasoner gr = (RuleReasoner) rf.create( null );
        assertSameRules( rules, gr.getRules() );
        }
    
    public void testCreateReasoningModel()
        {
        String ruleString = "[rdfs3a: (?x rdfs:range  ?y), (?y rdfs:subClassOf ?z) -> (?x rdfs:range  ?z)]";
        List wanted = Rule.parseRules( ruleString );
        ModelSpec spec = createInfModelSpec( ruleString );
        Model m = spec.createModel();
        Graph g = m.getGraph();
        assertTrue( g instanceof InfGraph );
        Reasoner r = ((InfGraph) g).getReasoner();
        assertTrue( r instanceof RuleReasoner );
        RuleReasoner rr = (RuleReasoner) r;
        List rules = rr.getRules();
        assertSameRules( wanted, rules );
        }
    
    private ModelSpec createInfModelSpec( String ruleString )
        {
        Model desc = createInfModelDesc( A, GenericRuleReasonerFactory.URI, ruleString );
        return ModelSpecImpl.create( desc );
        }
    
    public static Model createInfModelDesc( Resource root, String URI, String ruleString )
        {
        Resource maker = resource();
        Resource reasoner = resource();
        Resource rules = resource();
        Resource res = resource( URI );
        return ModelFactory.createDefaultModel()
            .add( root, JMS.reasonsWith, reasoner )
            .add( reasoner, JMS.reasoner, res )
            .add( root, JMS.maker, maker )
            .add( maker, RDF.type, JMS.MemMakerSpec )
            .add( maker, JMS.reificationMode, JMS.rsMinimal )
            .add( reasoner, JMS.ruleSet, rules )
            .add( rules, JMS.hasRule, ruleString )
            ;
        }
    
    /**
     * @param factoryURI
     * @param rulesURL
    */
    private void testRuleSetURL( String factoryURI, String rulesURL )
        {
        List rules = Rule.rulesFromURL( rulesURL );
        Model rs = modelWithStatements( "_a jms:reasoner " + factoryURI + "; _a jms:ruleSetURL " + rulesURL );
        ReasonerFactory rf = InfModelSpec.getReasonerFactory( A, rs );
        GenericRuleReasoner gr = (GenericRuleReasoner) rf.create( null );
        assertSameRules( rules, gr.getRules() );
        }
    
    protected void testGetReasoner( String uri, Class wantClass )
        {
        ReasonerFactory rf = InfModelSpec.getReasonerFactory( A, rSpec( uri ) );
        assertEquals( wantClass, rf.create( null ).getClass() );
        }

    protected Model rSpec( String factoryURI )
        { return modelWithStatements( "_a jms:reasoner " + factoryURI ); }
    
    protected static String file( String name )
        { return "file:testing/modelspecs/" + name; }
    
    protected void assertContains( String x, String y )
        {
        if (y == null) fail( "<null> does not contain anything, especially '" + x + "'" );
        if (y.indexOf( x ) < 0) fail( "'" + y + "' does not contain '" + x + "'" );
        }
    }


/*
    (c) Copyright 2004, Hewlett-Packard Development Company, LP
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
