/*
  (c) Copyright 2002, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: AbstractTestReifiedStatements.java,v 1.7 2003-09-08 10:25:11 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.graph.test.*;

import java.util.*;

/**
 	@author kers
*/
public abstract class AbstractTestReifiedStatements extends ModelTestBase
    {
    public AbstractTestReifiedStatements( String name )
        { 
        super( name ); 
        }
        
    public abstract Model getModel();
       
    private Model model;
    private Resource S;
    private Property P;
    private RDFNode O;
    private Statement SPO;
    private Statement SPO2;
    
    private static final String aURI = "jena:test/reifying#someURI";
    private static final String anotherURI = "jena:test/reifying#anotherURI";
    private static final String anchor = "jena:test/Reifying#";    
    
    public void setUp()
        {
        model = getModel();
        Resource S2 = model.createResource( anchor + "subject2" );
        S = model.createResource( anchor + "subject" );
        P = model.createProperty( anchor + "predicate" );
        O = model.createLiteral( anchor + "object" );       
        SPO = model.createStatement( S, P, O );             
        SPO2 = model.createStatement( S2, P, O );
        }
        
    /**
        the simplest case: if we assert all the components of a reification quad,
        we can get a ReifiedStatement that represents the reified statement.
    */ 
    public void testBasicReification()
        {
        Model m = model;
        Resource R = m.createResource( aURI );
        m.add( R, RDF.type, RDF.Statement );
        m.add( R, RDF.subject, S );
        m.add( R, RDF.predicate, P );
        m.add( R, RDF.object, O );
        RDFNode rs = R.as( ReifiedStatement.class );
        assertEquals( "can recover statement", SPO, ((ReifiedStatement) rs).getStatement() );
        }    
        
    /**
        check that, from a model with any combination of the statements given,
        we can convert R into a ReifiedStatement iff the four components of the
        quad are in the model.
    */
    public void testReificationCombinations()
        {
        Model m = model;
        Resource RR = m.createResource( aURI ), SS = m.createResource( anotherURI );
        Property PP = (Property) RR.as( Property.class );
        Object [][] statements =
            {
                { m.createStatement( RR, RDF.type, RDF.Statement ), new Integer(1) },
                { m.createStatement( RR, RDF.subject, SS ), new Integer(2) },
                { m.createStatement( RR, RDF.predicate, PP ), new Integer(4) },
                { m.createStatement( RR, RDF.object, O ), new Integer(8) },
                { m.createStatement( SS, PP, O ), new Integer(16) },
                { m.createStatement( RR, PP, O ), new Integer(32) },
                { m.createStatement( SS, RDF.subject, SS ), new Integer(64) },
                { m.createStatement( SS, RDF.predicate, PP ), new Integer(128) },
                { m.createStatement( SS, RDF.object, O ), new Integer(256) },
                { m.createStatement( SS, RDF.type, RDF.Statement ), new Integer(512) }
            };
        testCombinations( m, RR, 0, statements, statements.length );
        }

    /**
        walk down the set of statements (represented as an array), recursing with and
        without each statement being present. The mask bits record those statements
        that are in the model. At the bottom of the recursion (n == 0), check that R
        can be reified exactly when all four quad components are present; the other
        statements don't matter.
    */
    private void testCombinations( Model m, Resource R, int mask, Object [][] statements, int n )
        {
        if (n == 0)
            {
            try
                {
                // System.err.println( "| hello. mask = " + mask );
                ReifiedStatement rs = (ReifiedStatement) R.as( ReifiedStatement.class );
                // System.err.println( "+  we constructed " + rs );
                assertTrue( "should not reify: not all components present [" + mask + "]: " + rs, (mask & 15) == 15 );
                // System.err.println( "+  and we passed the assertion." );
                }
            catch (DoesNotReifyException e)
                { // System.err.println( "+  we exploded" );
                    assertFalse( "should reify: all components present", mask == 15 ); }
            }
        else
            {
            int i = n - 1;
            Statement s = (Statement) statements[i][0];
            int bits = ((Integer) statements[i][1]).intValue();
            testCombinations( m, R, mask, statements, i );
            m.add( s );
            testCombinations( m, R, mask + bits, statements, i );
            m.remove( s );
            }
        }
        
    public void testThisWillBreak()
        {
        Resource R = model.createResource( aURI );
        SPO.createReifiedStatement( aURI );
        model.add(  R, RDF.subject, R );
        }

    /**
        "dirty" reifications - those with conflicting quadlets - should fail.
    */
    public void testDirtyReification()
        {
        Resource R = model.createResource( aURI );
        model.add( R, RDF.type, RDF.Statement );
        model.add( R, RDF.subject, S );
        model.add( R, RDF.subject, P );
        testDoesNotReify( "boo", R );
        }
   
    public void testDoesNotReify( String title, Resource r )
        {
        try { r.as( ReifiedStatement.class ); fail( title + " (" + r + ")" ); }
        catch (DoesNotReifyException e) { /* that's what we expect */ }
        }
    
    public void testConversion()
        {
        final String uri = "spoo:handle";
        model.createReifiedStatement( uri, SPO );
        ReifiedStatement rs2 = (ReifiedStatement) model.createResource( uri ).as( ReifiedStatement.class );
        assertEquals( "recover statement", SPO, rs2.getStatement() );
        }
    
    public void testDoesNotReifyUnknown()
        {
        testDoesNotReify( "model should not reify rubbish", model.createResource( "spoo:rubbish" ) );
        }

    public void testConstructionByURI()
        {
        ReifiedStatement rs = model.createReifiedStatement( "spoo:handle", SPO );
        ReifiedStatement rs2 = SPO.createReifiedStatement( "spoo:gripper");
        assertEquals( "recover statement (URI)", SPO, rs.getStatement() );
        assertEquals( "recover URI", "spoo:handle", rs.getURI() );
        assertEquals( "recover URI", "spoo:gripper", rs2.getURI() );
        }

    public void testStatementAndModel( String title, ReifiedStatement rs, Model m, Statement st )
        {
        assertEquals( title + ": recover statement", st, rs.getStatement() );
        assertEquals( title + ": recover model", m, rs.getModel() );
        }
        
    public void testConstructionFromStatements()
        {
        testStatementAndModel( "fromStatement", SPO.createReifiedStatement(), model, SPO );
        }

    public void testConstructionFromModels()
        {
        testStatementAndModel( "fromModel", model.createReifiedStatement( SPO ) , model, SPO );    
        }
    
    /**
        utility method: make a Set from all the elements of an array.
    */
    public static Set arrayToSet( Object [] array )
        {
        HashSet s = new HashSet();
        for (int i = 0; i < array.length; i += 1) s.add( array[i] );
        return s;
        }
  
    /**
        utility method: get a set of all the elements delivered by
        _m.listReifiedStatements_.
    */  
    public Set getSetRS( Model m )
        { return GraphTestBase.iteratorToSet( m.listReifiedStatements() ); }
   
    protected static Set empty = arrayToSet( new Object [] {} );
    
    /**
        test that listReifiedStatements produces an iterator that contains
        the right reified statements. We *don't* test that they're not
        duplicated, because they might be; disallowing duplicates
        could be expensive.
    */
    public void testListReifiedStatements()
        {
        assertEquals( "initially: no reified statements", empty, getSetRS( model ) );
        ReifiedStatement rs = model.createReifiedStatement( aURI, SPO );
        // assertEquals( "still: no reified statements", empty, getSetRS( m ) );
    /* */
        model.add( rs, P, O );   
        Set justRS = arrayToSet( new Object [] {rs} );
        assertEquals( "post-add: one reified statement", justRS, getSetRS( model ) );
        model.add( S, P, rs );
        assertEquals( "post-add: still one reified statement", justRS, getSetRS( model ) );
    /* */
        ReifiedStatement rs2 = model.createReifiedStatement( anotherURI, SPO2 );
        Set bothRS = arrayToSet( new Object[] {rs, rs2} );
        model.add( rs2, P, O );
        assertEquals( "post-add: still one reified statement", bothRS, getSetRS( model ) );
        }

    /**
        this test appeared when TestStatementResources crashed using reified
        statements as a step-0 implementation for asSubject()/asObject(). Looks
        like there was a problem in modelReifier().getRS(), which we're fixing ...
    */
    public void testListDoesntCrash()
        {
        model.createReifiedStatement( SPO );
        model.createReifiedStatement( SPO2 );
        assertTrue( "should be non-empty", model.listReifiedStatements().hasNext() );
        }
        
    public Set getSetRS( Model m, Statement st )
        { return GraphTestBase.iteratorToSet( m.listReifiedStatements( st ) ); }
        
    public void testListReifiedSpecificStatements()
        {
        assertEquals( "no statements should match st", empty, getSetRS( model, SPO ) );
    /* */
        ReifiedStatement rs = model.createReifiedStatement( aURI, SPO );
        ReifiedStatement rs2 = model.createReifiedStatement( anotherURI, SPO2 );
        model.add( rs, P, O );
        // assertEquals( "still no matching statement", empty, getSetRS( m, stOther ) );
    /* */
        Set justRS2 = arrayToSet( new Object [] {rs2} );
        model.add( rs2, P, O );
        assertEquals( "now one matching statement", justRS2, getSetRS( model, SPO2 ) );        
        }
    
    public void testStatementListReifiedStatements()
        {
        Statement st = SPO;
        Model m = model;
        assertEquals( "it's not there yet", empty, GraphTestBase.iteratorToSet( st.listReifiedStatements() ) );
        ReifiedStatement rs = m.createReifiedStatement( aURI, st );
        Set justRS = arrayToSet( new Object [] {rs} );
        m.add( rs, P, O );
        assertEquals( "it's here now", justRS, GraphTestBase.iteratorToSet( st.listReifiedStatements() ) );    
        }
    
    public void testIsReified()
        {
        ReifiedStatement rs = model.createReifiedStatement( aURI, SPO );
        Resource BS = model.createResource( anchor + "BS" );
        Property BP = model.createProperty( anchor + "BP" );
        RDFNode BO = model.createProperty( anchor + "BO" );
        model.add( rs, P, O );
        assertTrue( "st should be reified now", SPO.isReified() );
        assertTrue( "m should have st reified now", model.isReified( SPO ) );
        assertFalse( "this new statement should not be reified", model.createStatement( BS, BP, BO ).isReified() );
        }
    
    public void testGetAny()
        {
        Resource r = model.getAnyReifiedStatement( SPO );
        assertTrue( "should get reified statement back", r instanceof ReifiedStatement );
        assertEquals( "should get me the statement", SPO, ((ReifiedStatement) r).getStatement() );
        }
    
    public void testRemoveReificationWorks()
        {
        Statement st = SPO;
        Model m = model;
        m.createReifiedStatement( aURI, st );
        assertTrue( "st is now reified", st.isReified() );
        m.removeAllReifications( st );
        assertFalse( "st is no longer reified", st.isReified() );
        }
    
    /**
        Leo Bard spotted a problem whereby removing a reified statement from a model
        with style Standard didn't leave the model empty. Here's a test for it. 
    */
    public void testLeosBug()
        {
        Model A = getModel();
        Statement st = statement( A,  "pigs fly south" );
        ReifiedStatement rst = st.createReifiedStatement( "eh:pointer" );
        A.removeReification( rst );
        assertIsoModels( ModelFactory.createDefaultModel(), A );
        }
    
    public void testRR()
        {
        Statement st = SPO;
        Model m = model;
        ReifiedStatement rs1 = m.createReifiedStatement( aURI, st );
        ReifiedStatement rs2 = m.createReifiedStatement( anotherURI, st );
        m.removeReification( rs1 );
        testNotReifying( m, aURI );
        assertTrue( "st is still reified", st.isReified() );
        m.removeReification( rs2 );
        assertFalse( "st should no longer be reified", st.isReified() );
        }
    
    private void testNotReifying( Model m, String uri )
        {
        try 
            {
            m.createResource( uri ).as( ReifiedStatement.class );
            fail( "there should be no reifiedStatement for " + uri );
            }
        catch (DoesNotReifyException e)
            { /* that's what we require */ }
        }

    public void testDoesNotReifyElsewhere()
        {
        final String uri = "spoo:rubbish";
        Model m2 = getModel();
        model.createReifiedStatement( uri, SPO );
        testDoesNotReify( "blue model should not reify rubbish", m2.createResource( uri ) );
        }                                             
//    public void testXXX()
//        {
//        String root = "http://root/root#";
//        Model m = ModelFactory.createDefaultModel();
//        Model r = ModelFactory.createRDFSModel( m );
//        Resource S = r.createResource( root + "S" );
//        Property P = r.createProperty( root + "P" );
//        RDFNode O = r.createResource( root + "O" );
//        Statement st = r.createStatement( S, P, O );
//        ReifiedStatement rs = st.createReifiedStatement( root + "RS" );
//        }
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