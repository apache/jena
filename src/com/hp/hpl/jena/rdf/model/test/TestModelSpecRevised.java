/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestModelSpecRevised.java,v 1.8 2004-07-30 15:22:02 chris-dollin Exp $
*/
package com.hp.hpl.jena.rdf.model.test;

import java.net.URL;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelSpecImpl;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.HashUtils;

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
    
    public void testFactoryWrapper()
        {
        List L = new ArrayList( Rule.parseRules( "[name: (?s owl:foo ?p) -> (?s ?p ?a)]" ) );
        MockFactory mock = new MockFactory();
        WrappedFactory wrap = new WrappedFactory( mock );
        assertEquals( mock.reasoner, wrap.create( null ) );
        assertEquals( mock.model, wrap.getCapabilities() );
        assertEquals( mock.getURI(), wrap.getURI() );
        wrap.setRules( L );
        assertEquals( L, ((FBRuleReasoner) wrap.create( null )).getRules() );
        }
    
    public class WrappedFactory implements RuleReasonerFactory
       {
       protected ReasonerFactory base;
       protected List rules = new ArrayList();
       
       public WrappedFactory( ReasonerFactory base )
           { this.base = base; }
       
       public Reasoner create(Resource configuration)
           { Reasoner result = base.create( configuration ); 
           if (result instanceof FBRuleReasoner) ((FBRuleReasoner) result).setRules( rules );
           return result; }

       public Model getCapabilities()
           { return base.getCapabilities();  }

       public String getURI()
           { return base.getURI();  }
       
       public void setRules( List rules )
           { this.rules = rules; }
       }
   
    protected static class MockFactory implements ReasonerFactory
        {
        public final Model model = ModelFactory.createDefaultModel();
        public final Reasoner reasoner = new GenericRuleReasoner( new ArrayList() );
        
        protected void add( String what )
            {}
        
        public Reasoner create( Resource configuration )
            { add( "create" ); return reasoner; }

        public Model getCapabilities()
            { add( "getCapabilities" ); return model; }

        public String getURI()
            { add( "getURI" ); return "eg:someURI"; }
        
        }
    
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
    
    public void testRulesetURLWorks()
        {
        String uri = GenericRuleReasonerFactory.URI;
        String url = "file:testing/modelspecs/empty.rules";
        Model rs = modelWithStatements( "_a jms:reasoner " + uri + "; _a jms:ruleSetURL " + url );
        Resource A = resource( "_a" );
        ModelSpecImpl.getReasonerFactory( A, rs );
        }
    
    public void testRulesetURLLoads()
        {
        String uri = GenericRuleReasonerFactory.URI;
        String url = "file:testing/modelspecs/empty.rules" );
        Model rs = modelWithStatements( "_a jms:reasoner " + uri + "; _a jms:ruleSetURL " + url );
        Resource A = resource( "_a" );
        ReasonerFactory rf = ModelSpecImpl.getReasonerFactory( A, rs );
        Set rules = HashUtils.createSet( ((FBRuleReasoner) rf.create( null )).getRules() );
        // assertEquals( null, rules );
        }
    
    protected void testGetReasoner( String uri, Class wantClass )
        {
        Model rs = modelWithStatements( "_a jms:reasoner " + uri );
        Resource A = resource( "_a" );
        ReasonerFactory rf = ModelSpecImpl.getReasonerFactory( A, rs );
        Reasoner r = rf.create( null );
        assertEquals( wantClass, r.getClass() );
        }

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
