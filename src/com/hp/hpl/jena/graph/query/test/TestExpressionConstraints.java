/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestExpressionConstraints.java,v 1.12 2004-03-16 16:20:09 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.*;

import com.hp.hpl.jena.util.iterator.Map1;

import junit.framework.*;
import java.util.*;

/**
	TestExpressionConstraints - test the "new" (as of September 2003) Constraint system
    for Query.

	@author kers
*/
public class TestExpressionConstraints extends GraphTestBase 
    {
    public TestExpressionConstraints( String name )    
        { super( name ); }
        
    public static TestSuite suite()
        { return new TestSuite( TestExpressionConstraints.class ); }

    protected static final Node X = Query.X;
    protected static final Node Y = Query.Y;
    protected static final Node Z = Query.Z;
    protected static final Node ANY = Query.ANY;
    
    protected static final Expression eTRUE = Expression.TRUE;
    protected static final Expression eFALSE = Expression.FALSE;

    protected VariableIndexes emptyVI = new VariableIndexes()
        { public int indexOf(String name) { return -1; } };
        
    protected IndexValues emptyIV = new IndexValues()
        { public Object get( int index ) { return null; } };
    
    protected static final Map1 getFirst = new Map1()
        { public Object map1(Object x) { return ((List) x).get(0); }};        
        
    public void testConstraintFALSE()
        {
        Graph g = graphWith( "x R y; a P b" );
        Query q = new Query().addMatch( X, ANY, ANY ).addConstraint( eFALSE )
            ;
        assertFalse( q.executeBindings( g, new Node[] {X} ).hasNext() ); 
        }    
        
    public void testConstraintTRUE()
        {
        Graph g = graphWith( "x R y; a P b" );
        Query q = new Query().addMatch( X, ANY, ANY ).addConstraint( eTRUE )
            ;
        assertTrue( q.executeBindings( g, new Node[] {X} ).hasNext() ); 
        }
        
    private BaseExampleExpression notEqual( Node x, Node y )
        {
        return ExampleCreate.NE( x, y );
        }

    private BaseExampleExpression areEqual( Node x, Node y )
        {
        return ExampleCreate.EQ( x, y );
        }
        
    protected BaseExampleExpression matches( Node x, Node y )
        { return ExampleCreate.MATCHES( x, y ); }

    public void testConstraintNE1()
        {        
        Graph g = graphWith( "x R y; a P a" );
        Query q = new Query()
            .addMatch( X, ANY, Y )
            .addConstraint( notEqual( X, Y ) )
            ;
        Set expected = new HashSet();
        expected.add( node( "x" ) );
        assertEquals( expected, iteratorToSet( q.executeBindings( g, new Node[] {X} ).mapWith( getFirst ) ) );     
        }        
        
    public void testConstraintNE2()
        {        
        Graph g = graphWith( "x R y; a P a" );
        Query q = new Query()
            // .addMatch( Z, ANY, ANY )
            .addMatch( X, ANY, Y )
            .addConstraint( notEqual( X, Y ) )
            ;
        Set expected = new HashSet();
        expected.add( node( "x" ) );
        assertEquals( expected, iteratorToSet( q.executeBindings( g, new Node[] {X} ).mapWith( getFirst ) ) );     
        }
                
    public void testConstraintNE3()
        {        
        Graph g = graphWith( "x R a; y P b; z Q c" );
        Query q = new Query()
            .addMatch( X, ANY, ANY )
            .addConstraint( notEqual( X, node( "y" ) ) )
            ;
        Set expected = new HashSet();
        expected.add( node( "x" ) );
        expected.add( node( "z" ) );
        assertEquals( expected, iteratorToSet( q.executeBindings( g, new Node[] {X} ).mapWith( getFirst ) ) );     
        }    
        
    public void testConstraintNE4()
        {        
        Graph g = graphWith( "x R a; y P b; z Q c" );
        Query q = new Query()
            .addMatch( X, ANY, ANY )
            .addConstraint( notEqual( X, node( "y" ) ) )
            .addConstraint( notEqual( X, node( "x" ) ) )
            ;
        Set expected = new HashSet();
        expected.add( node( "z" ) );
        assertEquals( expected, iteratorToSet( q.executeBindings( g, new Node[] {X} ).mapWith( getFirst ) ) );     
        }
        
    public static class VI implements VariableIndexes
        {
        private Map values = new HashMap();
        
        public VI set( String x, int i ) { values.put( x, new Integer( i ) ); return this; }    
        
        public int indexOf( String name ) { return ((Integer) values.get( name )).intValue(); }
        }
    
    public static class IV implements IndexValues
        {
        private Map values = new HashMap();
        
        public IV set( int i, Object x ) { values.put( new Integer( i ), x ); return this; }    
        
        public Object get( int i ) { return values.get( new Integer( i ) ); }
        }
        
    public void testVI()
        {
        VI varValues = new VI() .set( "X", 1 ).set( "Y", 2 ).set( "Z", 3 );
        assertEquals( 1, varValues.indexOf( "X" ) );
        assertEquals( 2, varValues.indexOf( "Y" ) );
        assertEquals( 3, varValues.indexOf( "Z" ) );
        }    
        
    public void testNE()
        {
        Expression e = areEqual( X, Y );
        VariableIndexes vi = new VI().set( "X", 1 ).set( "Y", 2 );
        IndexValues iv = new IV().set( 1, "something" ).set( 2, "else" );
        assertEquals( false, e.prepare( vi ).evalBool( iv ) );    
        }
        
	public void testVVTrue()
        { assertEquals( true, Expression.TRUE.prepare( emptyVI ).evalBool( emptyIV ) ); }
        
    public void testVVFalse()
        { assertEquals( false, Expression.FALSE.prepare( emptyVI ).evalBool( emptyIV ) ); }

    public void testVVMatches()
        { VariableIndexes vi = new VI().set( "X", 0 ).set( "Y", 1 );
        IndexValues iv = new IV().set( 0, "hello" ).set( 1, "ell" );
        assertEquals( true, matches( X, Y ).prepare( vi ).evalBool( iv ) );  }
        
    public void testPrepareTRUE()
        {
        IndexValues none = new IndexValues() 
            { public Object get( int i ) { return null; } };
        Valuator t = Expression.TRUE.prepare( new Mapping( new Node[0] ) );  
        assertEquals( true, t.evalBool( none ) );    
        }
        
    public void testPrepareFALSE()
        {
        IndexValues none = new IndexValues() 
            { public Object get( int i ) { return null; } };
        Valuator t = Expression.FALSE.prepare( new Mapping( new Node[0] ) );  
        assertEquals( false, t.evalBool( none ) );    
        }
        
    public void testPrepareNE()
        {
        Expression e = notEqual( X, Y );
        VariableIndexes map = new Mapping( new Node[2] );
        // Valuator ep = e.prepare( map );        
        }
    
    public void testURIs()
        {
        assertEquals( "http://jena.hpl.hp.com/constraints/NE", notEqual( X, Y ).getFun() );
        assertEquals( "http://jena.hpl.hp.com/constraints/EQ", areEqual( X, Y ).getFun() );
        assertEquals( "http://jena.hpl.hp.com/constraints/MATCHES", matches( X, Y ).getFun() );
        }
    
    public void testLiterals()
        {
        assertTrue( Expression.TRUE.isConstant() );
        assertTrue( Expression.FALSE.isConstant() );
        assertFalse( notEqual( X, Y ).isConstant() );
        assertEquals( Boolean.TRUE, Expression.TRUE.getValue() );
        assertEquals( Boolean.FALSE, Expression.FALSE.getValue() );
        }
    
    public void testDetectAnd()
        {
        BaseExampleExpression e1 = notEqual( X, Y ), e2 = notEqual( X, Z );
        Query q = new Query().addConstraint( e1.and( e2 ) );
        Set eBoth = new HashSet(); eBoth.add( e1 ); eBoth.add( e2 );
        Set s = iteratorToSet( q.getConstraints().iterator() );
        assertEquals( eBoth, s );
        }
    }

/*
    (c) Copyright 2003, Hewlett-Packard Development Company, LP
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