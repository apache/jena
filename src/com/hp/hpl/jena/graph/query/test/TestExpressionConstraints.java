/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestExpressionConstraints.java,v 1.1 2003-09-26 11:53:08 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.*;

import com.hp.hpl.jena.util.iterator.Map1;

import junit.framework.*;
import java.util.*;

/**
	TestExpressionConstraints - test the "new" (as of Seprember 2003) Constraint system
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
        
    private Expression notEqual( Node x, Node y )
        {
        return Expression.Create.NE( x, y );
        }
        
    public void testConstraintNE1()
        {        
        Map1 getFirst = new Map1(){ public Object map1(Object x) { return ((List) x).get(0); }};
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
        Map1 getFirst = new Map1(){ public Object map1(Object x) { return ((List) x).get(0); }};
        Graph g = graphWith( "x R y; a P a" );
        Query q = new Query()
            .addMatch( Z, ANY, ANY )
            .addMatch( X, ANY, Y )
            .addConstraint( notEqual( X, Y ) )
            ;
        Set expected = new HashSet();
        expected.add( node( "x" ) );
        assertEquals( expected, iteratorToSet( q.executeBindings( g, new Node[] {X} ).mapWith( getFirst ) ) );     
        }
                
    public void testConstraintNE3()
        {        
        Map1 getFirst = new Map1(){ public Object map1(Object x) { return ((List) x).get(0); }};
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
        Map1 getFirst = new Map1(){ public Object map1(Object x) { return ((List) x).get(0); }};
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