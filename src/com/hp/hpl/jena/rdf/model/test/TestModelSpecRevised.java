/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestModelSpecRevised.java,v 1.13 2004-08-04 11:31:06 chris-dollin Exp $
*/
package com.hp.hpl.jena.rdf.model.test;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelSpecImpl;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.shared.*;

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
    
    /*
     * 
        Statement factStatement = description.getProperty( root, JMS.reasonsWith );
        if (factStatement == null) return null;
        Statement reStatement = description.getProperty( factStatement.getResource(), JMS.reasoner );
        String factoryURI = reStatement.getResource().getURI();
        return ReasonerRegistry.theRegistry().getFactory( factoryURI );
     */
    public void testNoReasonerSuppliedException()
        { 
        Model rs = modelWithStatements( "_a rdf:type jms:ReasonerSpec" );
        Resource A = resource( "_a" );
        try { ModelSpecImpl.getReasonerFactory( A, rs ); fail( "should catch missing reasoner" ); }
        catch (NoReasonerSuppliedException e) { pass(); }
        }

    public void testNoSuchReasonerException()
        {
        Model rs = modelWithStatements( "_a rdf:type jms:ReasonerSpec; _a jms:reasoner nosuch:reasoner" );
        Resource A = resource( "_a" );
        try 
            { ModelSpecImpl.getReasonerFactory( A, rs ); 
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
        Resource A = resource( "_a" );
        try { ModelSpecImpl.getReasonerFactory( A, rs ); fail( "should report ruleset failure" ); }
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
        Resource A = resource( "_a" );
        ReasonerFactory rf = ModelSpecImpl.getReasonerFactory( A, rs );
        GenericRuleReasoner gr = (GenericRuleReasoner) rf.create( null );
        assertEquals( new HashSet( rules ), new HashSet( gr.getRules() ) );
        }
    
    /**
     * @param factoryURI
     * @param rulesURL
    */
    private void testRuleSetURL( String factoryURI, String rulesURL )
        {
        List rules = Rule.rulesFromURL( rulesURL );
        Model rs = modelWithStatements( "_a jms:reasoner " + factoryURI + "; _a jms:ruleSetURL " + rulesURL );
        Resource A = resource( "_a" );
        ReasonerFactory rf = ModelSpecImpl.getReasonerFactory( A, rs );
        GenericRuleReasoner gr = (GenericRuleReasoner) rf.create( null );
        assertEquals( rules, gr.getRules() );
        }
    
    protected void testGetReasoner( String uri, Class wantClass )
        {
        Model rs = modelWithStatements( "_a jms:reasoner " + uri );
        Resource A = resource( "_a" );
        ReasonerFactory rf = ModelSpecImpl.getReasonerFactory( A, rs );
        Reasoner r = rf.create( null );
        assertEquals( wantClass, r.getClass() );
        }

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
