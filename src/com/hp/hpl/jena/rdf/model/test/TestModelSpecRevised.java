/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestModelSpecRevised.java,v 1.19 2004-08-07 11:33:32 chris-dollin Exp $
*/
package com.hp.hpl.jena.rdf.model.test;

import java.util.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.FileUtils;
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
    
    public static final Resource A = resource( "_a" );

    public void testTriples()
        {
        check( "a R b", "a R b", new Object[] {} );
        check( "a R b", "a R ?0", new Object[] {"b"} );
        check( "a P b", "a ?0 ?1", new Object[] {"P", "b"} );
        check( "_a P 17; Q P _a", "?0 ?1 ?2; ?3 ?1 ?0", new Object[] {"_a", "P", "17", "Q"} );
        }
    
    public void check( String wanted, String template, Object [] args )
        {
        Model m = modelWithStatements( template, args );
        assertIsoModels( modelWithStatements( wanted ), m );
        }
    
    /**
     * @param string
     * @param objects
     * @return
     */
    private static Model modelWithStatements( String facts, Object[] objects )
        {
        Graph g = new GraphMem();
        StringTokenizer semis = new StringTokenizer( facts, ";" );
        while (semis.hasMoreTokens()) 
            g.add( replace( triple( PrefixMapping.Extended, semis.nextToken() ), objects ) );
        return ModelFactory.createModelForGraph( g );
        }

    /**
     * @param t
     * @param objects
     * @return
     */
    private static Triple replace( Triple t, Object[] objects )
        {
        return Triple.create
            ( replace( t.getSubject(), objects ), replace( t.getPredicate(), objects ), replace( t.getObject(), objects ) );
        }

    /**
     * @param n
     * @return
     */
    private static Node replace( Node n, Object [] objects )
        {
        if (n.isVariable())
            {
            String name = n.getName();
            if (Character.isDigit( name.charAt(0))) 
                return Node.create( (String) objects[Integer.parseInt( name )] );
            }
        return n;
        }

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
        Model rs = modelWithStatements( "_a jms:reasoner ?0; _a jms:ruleSetURL nowhere:man", new Object[] { uri } ); 
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
        Model rs = modelWithStatements( "_a jms:reasoner ?0; _a jms:ruleSetURL ?1; _a jms:ruleSetURL ?2", new Object[] {factoryURI, rulesA, rulesB});
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
    
    public void testSchema()
    	{
    	ReasonerRegistry.theRegistry().register( "fake:factory", new FakeFactory() );
        String d = 
            "_root jms:reasonsWith _reasoner"
            + "; _reasoner jms:reasoner fake:factory"
            + "; _root jms:maker _maker"
            + "; _maker rdf:type jms:MemMakerSpec"
            + "; _maker jms:reificationMode jms:rsMinimal"
            + "; _reasoner jms:schemaURL ?0"
            + "; _reasoner jms:schemaURL ?1"
            ;
        Model desc = modelWithStatements( d, new Object[] {file("schema.n3"), file("schema2.n3")} );
		ModelSpec spec = ModelFactory.createSpec( desc );
		validateHasSchema( loadBoth( "schema.n3", "schema2.n3" ), spec.createModel() );
    	}
    
	/**
     * @return
     */
    private Graph loadBoth( String x, String y )
        {
        Model schema = FileUtils.loadModel( file( x ) );
        Model schema2 = FileUtils.loadModel( file( y ) );
        schema.add( schema2 );
        return schema.getGraph();
        }

    private void validateHasSchema( Graph schema, Model m )
		{
		((FakeReasoner) ((InfGraph) m.getGraph()).getReasoner()).validate( schema );
		}

	protected static class FakeReasoner implements Reasoner
		{
    	public Graph bound = new GraphMem();
    	
    	public void validate( Graph desired )
    		{ assertTrue( "bound graph is not correct", desired.isIsomorphicWith( bound ) ); }
    	
		public Reasoner bindSchema( Graph tbox ) throws ReasonerException
			{ bound.getBulkUpdateHandler().add( tbox ); return this; }

		public Reasoner bindSchema( Model tbox ) throws ReasonerException
			{ return bindSchema( tbox.getGraph() ); }

		public InfGraph bind( Graph data ) throws ReasonerException
			{ return new BasicForwardRuleInfGraph( this, new ArrayList(), new GraphMem(), new GraphMem() ); }

		public void setDerivationLogging( boolean logOn )
			{ throw new JenaException( "fakes don't do this" ); }

		public void setParameter( Property parameterUri, Object value )
			{ throw new JenaException( "fakes don't do this" ); }
		
		public Model getCapabilities()
			{ throw new JenaException( "fakes don't do this" ); }

		public void addDescription( Model configSpec, Resource base )
			{ throw new JenaException( "fakes don't do this" ); }

		public boolean supportsProperty( Property property )
			{ throw new JenaException( "fakes don't do this" ); }

		}
    
    protected static class FakeFactory implements ReasonerFactory
		{
		public Reasoner create( Resource configuration )
			{ return new FakeReasoner();
			}

		public Model getCapabilities()
			{
			// TODO Auto-generated method stub
			return null;
			}

		public String getURI()
			{
			// TODO Auto-generated method stub
			return null;
			}
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
   
    public void testDescription()
    	{
        String ruleString = "[rdfs3a: (?x rdfs:range  ?y), (?y rdfs:subClassOf ?z) -> (?x rdfs:range  ?z)]";
    	Model desc = createInfModelDesc( A, GenericRuleReasonerFactory.URI, ruleString );
    	ModelSpec s = ModelSpecImpl.create( desc );
    	// assertIsoModels( desc, s.getDescription() );
    	}
    
    private ModelSpec createInfModelSpec( String ruleString )
        {
        Model desc = createInfModelDesc( A, GenericRuleReasonerFactory.URI, ruleString );
        return ModelSpecImpl.create( desc );
        }
    
    public static Model createInfModelDesc( Resource root, String URI, String ruleString )
        {
        String d = 
            "_root jms:reasonsWith _reasoner"
            + "; _reasoner jms:reasoner ?0"
            + "; _root jms:maker _maker"
            + "; _maker rdf:type jms:MemMakerSpec"
            + "; _maker jms:reificationMode jms:rsMinimal"
            + "; _reasoner jms:ruleSet _rules"
            + "; _rules jms:hasRule ?1"
            ;
        return modelWithStatements( d, new Object[] {URI, "'" + ruleString + "'"} ); 
        }
   
    /**
     * @param factoryURI
     * @param rulesURL
    */
    private void testRuleSetURL( String factoryURI, String rulesURL )
        {
        List rules = Rule.rulesFromURL( rulesURL );
        Model rs = modelWithStatements( "_a jms:reasoner ?0; _a jms:ruleSetURL ?1", new Object[] {factoryURI, rulesURL} );
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
