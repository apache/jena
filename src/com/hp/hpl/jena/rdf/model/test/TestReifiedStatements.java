package com.hp.hpl.jena.rdf.model.test;

/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: TestReifiedStatements.java,v 1.3 2003-03-26 12:39:08 chris-dollin Exp $
*/

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.RDF;
import java.util.*;

import junit.framework.*;

/**
    test the properties required of ReifiedStatement objects.
    @author kers 
*/
public class TestReifiedStatements extends GraphTestBase
    {
    public TestReifiedStatements( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestReifiedStatements.class ); }   
        
    /**
        contexts are just a way to have bunches of test-specific
        information to be prepared during setUp.
    */
    public static class Context
        {
        Model model = ModelFactory.createDefaultModel();
        
        Resource S;
        Property P;
        RDFNode O;
        
        Statement SPO;
        
        Context( String tag )
            {
            String spoo = "spoo:" + tag + "/";
            S = model.createResource( spoo + "subject" );
            P = model.createProperty( spoo + "predicate" );
            O = model.createLiteral( spoo + "object" );       
            SPO = model.createStatement( S, P, O );     
            }
        }
        
    private Context blue;
    private Context red;
    
    static final String aURI = "spoo:theInvisibleMan";
    static final String anotherURI = "spoo:ChevyChase";

    public void setUp()
        {
        red = new Context( "red" );
        blue = new Context( "blue" );    
        }
        
    /**
        the simplist case: if we assert all the components of a reification quad,
        we can get a ReifiedStatement that represents the reified statement.
    */ 
    public void testBasicReification()
        {
        Model m = red.model;
        Resource R = m.createResource( aURI );
        m.add( R, RDF.type, RDF.Statement );
        m.add( R, RDF.subject, red.S );
        m.add( R, RDF.predicate, red.P );
        m.add( R, RDF.object, red.O );
        RDFNode rs = R.as( ReifiedStatement.class );
        assertEquals( "can recover statement", red.SPO, ((ReifiedStatement) rs).getStatement() );
        }
    
    /**
        check that, from a model with any combination of the statements given,
        we can convert R into a ReifiedStatement iff the four components of the
        quad are in the model.
    */
    public void testReificationCombinations()
        {
        Model m = red.model;
        Resource R = m.createResource( aURI ), S = m.createResource( anotherURI );
        Property P = (Property) R.as( Property.class );
        Object [][] statements =
            {
                { m.createStatement( R, RDF.type, RDF.Statement ), new Integer(1) },
                { m.createStatement( R, RDF.subject, red.S ), new Integer(2) },
                { m.createStatement( R, RDF.predicate, red.P ), new Integer(4) },
                { m.createStatement( R, RDF.object, red.O ), new Integer(8) },
                { m.createStatement( S, P, red.O ), new Integer(16) },
                { m.createStatement( R, P, red.O ), new Integer(32) },
                { m.createStatement( S, RDF.subject, red.S ), new Integer(64) },
                { m.createStatement( S, RDF.predicate, red.P ), new Integer(128) },
                { m.createStatement( S, RDF.object, red.O ), new Integer(256) },
                { m.createStatement( S, RDF.type, RDF.Statement ), new Integer(512) }
            };
        testCombinations( m, R, 0, statements, statements.length );
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
                ReifiedStatement rs = (ReifiedStatement) R.as( ReifiedStatement.class );
                assertTrue( "should not reify: not all components present [" + mask + "]: " + rs, (mask & 15) == 15 );
                }
            catch (DoesNotReifyException e)
                { assertFalse( "should reify: all components present", mask == 15 ); }
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

   
    public void testConstructionFromStatements()
        {
        testConstructionFromStatements( red );
        testConstructionFromStatements( blue );
        }
        
    public void testConstructionFromStatements( Context c )
        {
        Model m = c.model;
        Statement st = c.SPO;
        testStatementAndModel( "fromStatement", st.createReifiedStatement(), m, st );
        }
        
    public void testStatementAndModel( String title, ReifiedStatement rs, Model m, Statement st )
        {
        assertEquals( title + ": recover statement", st, rs.getStatement() );
        assertEquals( title + ": recover model", m, rs.getModel() );
        }
        
    public void testConstructionFromModels()
        {
        testConstructionFromModels( red );
        testConstructionFromModels( blue );
        }
        
    public void testConstructionFromModels( Context c )
        {
        Model m = c.model;
        Statement st = c.SPO;
        testStatementAndModel( "fromModel", m.createReifiedStatement( st ) , m, st );    
        }
        
    public void testConstructionByURI()
        {
        testConstructionByURI( red );
        testConstructionByURI( blue );
        }
        
    public void testConstructionByURI( Context c )
        {
        Model m = c.model;
        Statement st = c.SPO;
        ReifiedStatement rs = m.createReifiedStatement( "spoo:handle", st );
        ReifiedStatement rs2 = st.createReifiedStatement( "spoo:gripper");
        assertEquals( "recover statement (URI)", st, rs.getStatement() );
        assertEquals( "recover URI", "spoo:handle", rs.getURI() );
        assertEquals( "recover URI", "spoo:gripper", rs2.getURI() );
        }
        
    public void testConversion()
        {
        final String uri = "spoo:handle";
        Context c = red;
        Model m = c.model;
        Statement st = c.SPO;
        ReifiedStatement rs = m.createReifiedStatement( uri, st );
        ReifiedStatement rs2 = (ReifiedStatement) m.createResource( uri ).as( ReifiedStatement.class );
        assertEquals( "recover statement", st, rs2.getStatement() );
        }
        
    public void testDoesNotReifyUnknown()
        {
        Model m = red.model;
        try
            {
            m.createResource( "spoo:rubbish" ).as( ReifiedStatement.class );
            fail( "that resource should have no reification" );
            }
        catch (DoesNotReifyException e)
            { /* that's what we expect */ }
        }
        
    public void testDoesNotReifyElsewhere()
        {
        final String uri = "spoo:rubbish";
        Model m1 = red.model;
        ReifiedStatement rs1 = m1.createReifiedStatement( uri, red.SPO );
        try
            {
            Model m2 = blue.model;
            m2.createResource( uri ).as( ReifiedStatement.class );
            fail( "that resource should have no reification in the blue model" );            
            }
        catch (DoesNotReifyException e)
            { /* that's what we expect */ }
        }

    /**
        now we test that a reified statement that has been inserted into a 
        model (as an S, P, or O) is then recoverable from that model
        by its URI.
    */
//    public void testCrossModelReification()
//        {
//        final String uri = "spoo:anchor";
//        ReifiedStatement rs = red.model.createReifiedStatement( uri, red.SPO );
//        assertTrue( "is too!", rs instanceof ReifiedStatement );
//        blue.model.add( rs, red.P, red.O );
//        ReifiedStatement rs2 = (ReifiedStatement) 
//            blue.model.createResource( uri )
//            .as( ReifiedStatement.class )
//            ;
//        assertEquals( "recovered", red.SPO, rs2.getStatement() );
//        }
        
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
        { return iteratorToSet( m.listReifiedStatements() ); }
       
    protected static Set empty = arrayToSet( new Object [] {} );
 
    /**
        test that listReifiedStatements produces an iterator that contains
        the right reified statements. We *don't* test that they're not
        duplicated, because they might be; disallowing duplicates
        could be expensive.
    */
    public void testListReifiedStatements()
        {
        Model m = red.model;
        assertEquals( "initially: no reified statements", empty, getSetRS( m ) );
        ReifiedStatement rs = m.createReifiedStatement( aURI, red.SPO );
        // assertEquals( "still: no reified statements", empty, getSetRS( m ) );
    /* */
        m.add( rs, red.P, red.O );   
        Set justRS = arrayToSet( new Object [] {rs} );
        assertEquals( "post-add: one reified statement", justRS, getSetRS( m ) );
        red.model.add( red.S, red.P, rs );
        assertEquals( "post-add: still one reified statement", justRS, getSetRS( m ) );
    /* */
        ReifiedStatement rs2 = m.createReifiedStatement( anotherURI, blue.SPO );
        Set bothRS = arrayToSet( new Object[] {rs, rs2} );
        m.add( rs2, red.P, red.O );
        assertEquals( "post-add: still one reified statement", bothRS, getSetRS( m ) );
        }

    /**
        this test appeared when TestStatementResources crashed using reified
        statements as a step-0 implementation for asSubject()/asObject(). Looks
        like there was a problem in modelReifier().getRS(), which we're fixing ...
    */
    public void testListDoesntCrash()
        {
        ReifiedStatement rs1 = red.model.createReifiedStatement( red.SPO );
        ReifiedStatement rs2 = red.model.createReifiedStatement( blue.SPO );
        assertTrue( "should be non-empty", red.model.listReifiedStatements().hasNext() );
        }
    
    
    public Set getSetRS( Model m, Statement st )
        { return iteratorToSet( m.listReifiedStatements( st ) ); }
        
    public void testListReifiedSpecificStatements()
        {
        Statement st = red.SPO, stOther = blue.SPO;
        Model m = red.model;
        assertEquals( "no statements should match st", empty, getSetRS( m, st ) );
    /* */
        ReifiedStatement rs = m.createReifiedStatement( aURI, red.SPO );
        ReifiedStatement rs2 = m.createReifiedStatement( anotherURI, blue.SPO );
        m.add( rs, red.P, red.O );
        // assertEquals( "still no matching statement", empty, getSetRS( m, stOther ) );
    /* */
        Set justRS2 = arrayToSet( new Object [] {rs2} );
        m.add( rs2, red.P, red.O );
        assertEquals( "now one matching statement", justRS2, getSetRS( m, stOther ) );        
        }
        
    public void testStatementListReifiedStatements()
        {
        Statement st = red.SPO;
        Model m = red.model;
        assertEquals( "it's not there yet", empty, iteratorToSet( st.listReifiedStatements() ) );
        ReifiedStatement rs = m.createReifiedStatement( aURI, st );
        Set justRS = arrayToSet( new Object [] {rs} );
        m.add( rs, red.P, red.O );
        assertEquals( "it's here now", justRS, iteratorToSet( st.listReifiedStatements() ) );    
        }
        
    public void testIsReified()
        {
        Statement st = red.SPO;
        Model m = red.model;
        ReifiedStatement rs = m.createReifiedStatement( aURI, st );
        // assertFalse( "st should not be reified", st.isReified() );
        // assertFalse( "m should not have st reified", m.isReified( st ) );
        m.add( rs, red.P, red.O );
        assertTrue( "st should be reified now", st.isReified() );
        assertTrue( "m should have st reified now", m.isReified( st ) );
        assertFalse( "this new statement should not be reified", m.createStatement( blue.S, blue.P, blue.O ).isReified() );
        }
        
    public void testGetAny()
        {
        Resource r = red.model.getAnyReifiedStatement( red.SPO );
        assertTrue( "should get reified statement back", r instanceof ReifiedStatement );
        assertEquals( "should get me the statement", red.SPO, ((ReifiedStatement) r).getStatement() );
        }
        
    public void testRemoveReificationWorks()
        {
        Statement st = red.SPO;
        Model m = red.model;
        ReifiedStatement rs = m.createReifiedStatement( aURI, st );
        assertTrue( "st is now reified", st.isReified() );
        m.removeAllReifications( st );
        assertFalse( "st is no longer reified", st.isReified() );
        }
        
    public void testRR()
        {
        Statement st = red.SPO;
        Model m = red.model;
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
        
    }
    
/*
    (c) Copyright Hewlett-Packard Company 2003
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