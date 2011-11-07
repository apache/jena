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

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.CollectionFactory;
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
    
    @Override
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
        if (model.getReificationStyle() != ModelFactory.Minimal) 
            { Resource R = model.createResource( aURI );
            model.add( R, RDF.type, RDF.Statement );
            model.add( R, RDF.subject, S );
            model.add( R, RDF.predicate, P );
            model.add( R, RDF.object, O );
            RDFNode rs = R.as( ReifiedStatement.class );
            assertEquals( "can recover statement", SPO, ((ReifiedStatement) rs).getStatement() ); }
        }    
        
    /**
        check that, from a model with any combination of the statements given,
        we can convert R into a ReifiedStatement iff the four components of the
        quad are in the model.
    */
    public void testReificationCombinations()
        {
        Resource RR = model.createResource( aURI ), SS = model.createResource( anotherURI );
        Property PP = RR.as( Property.class );
        Object [][] statements =
            {
                { model.createStatement( RR, RDF.type, RDF.Statement ), new Integer(1) },
                { model.createStatement( RR, RDF.subject, SS ), new Integer(2) },
                { model.createStatement( RR, RDF.predicate, PP ), new Integer(4) },
                { model.createStatement( RR, RDF.object, O ), new Integer(8) },
                { model.createStatement( SS, PP, O ), new Integer(16) },
                { model.createStatement( RR, PP, O ), new Integer(32) },
                { model.createStatement( SS, RDF.subject, SS ), new Integer(64) },
                { model.createStatement( SS, RDF.predicate, PP ), new Integer(128) },
                { model.createStatement( SS, RDF.object, O ), new Integer(256) },
                { model.createStatement( SS, RDF.type, RDF.Statement ), new Integer(512) }
            };
        if (model.getReificationStyle() != ModelFactory.Minimal)
            testCombinations( model, RR, 0, statements, statements.length );
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
                ReifiedStatement rs = R.as( ReifiedStatement.class );
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
        ReifiedStatement rs2 = model.createResource( uri ).as( ReifiedStatement.class );
        assertEquals( "recover statement", SPO, rs2.getStatement() );
        }
    
    public void testDoesNotReifyUnknown()
        {
        testDoesNotReify( "model should not reify rubbish", model.createResource( "spoo:rubbish" ) );
        }

    public void testQuintetOfQuadlets() 
        {
        Resource rs = model.createResource();
        rs.addProperty( RDF.type, RDF.Statement );
        model.createResource().addProperty( RDF.value, rs );
        rs.addProperty( RDF.subject, model.createResource() );
        rs.addProperty( RDF.predicate, model.createProperty( "http://example.org/foo" ) );
        rs.addProperty( RDF.object, model.createResource() );
        rs.addProperty( RDF.object, model.createResource() );
        StmtIterator it = model.listStatements();
        while (it.hasNext()) 
            {
            Statement s = it.nextStatement();
            assertFalse(s.getObject().equals(s.getSubject()));
            }
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
        utility method: get a set of all the elements delivered by
        _m.listReifiedStatements_.
    */  
    public Set<ReifiedStatement> getSetRS( Model m )
        { return m.listReifiedStatements().toSet(); }
   
    protected static Set<ReifiedStatement> noStatements = CollectionFactory.createHashedSet();
    
    /**
        test that listReifiedStatements produces an iterator that contains
        the right reified statements. We *don't* test that they're not
        duplicated, because they might be; disallowing duplicates
        could be expensive.
    */
    public void testListReifiedStatements()
        {
        assertEquals( "initially: no reified statements", noStatements, getSetRS( model ) );
        ReifiedStatement rs = model.createReifiedStatement( aURI, SPO );
        // assertEquals( "still: no reified statements", empty, getSetRS( m ) );
    /* */
        model.add( rs, P, O );   
        Set<ReifiedStatement> justRS = arrayToSet( new ReifiedStatement [] {rs} );
        assertEquals( "post-add: one reified statement", justRS, getSetRS( model ) );
        model.add( S, P, rs );
        assertEquals( "post-add: still one reified statement", justRS, getSetRS( model ) );
    /* */
        ReifiedStatement rs2 = model.createReifiedStatement( anotherURI, SPO2 );
        Set<ReifiedStatement> bothRS = arrayToSet( new ReifiedStatement[] {rs, rs2} );
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
        
    public Set<ReifiedStatement> getSetRS( Model m, Statement st )
        { return m.listReifiedStatements( st ).toSet(); }
        
    public void testListReifiedSpecificStatements()
        {
        assertEquals( "no statements should match st", noStatements, getSetRS( model, SPO ) );
    /* */
        ReifiedStatement rs = model.createReifiedStatement( aURI, SPO );
        ReifiedStatement rs2 = model.createReifiedStatement( anotherURI, SPO2 );
        model.add( rs, P, O );
        // assertEquals( "still no matching statement", empty, getSetRS( m, stOther ) );
    /* */
        Set<ReifiedStatement> justRS2 = arrayToSet( new ReifiedStatement [] {rs2} );
        model.add( rs2, P, O );
        assertEquals( "now one matching statement", justRS2, getSetRS( model, SPO2 ) );        
        }
    
    public void testStatementListReifiedStatements()
        {
        Statement st = SPO;
        Model m = model;
        assertEquals( "it's not there yet", noStatements, GraphTestBase.iteratorToSet( st.listReifiedStatements() ) );
        ReifiedStatement rs = m.createReifiedStatement( aURI, st );
        Set<ReifiedStatement> justRS = arrayToSet( new ReifiedStatement [] {rs} );
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
        assertInstanceOf( ReifiedStatement.class, r );
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
