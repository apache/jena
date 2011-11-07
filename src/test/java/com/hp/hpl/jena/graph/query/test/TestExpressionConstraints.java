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

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.query.Expression.Util;

import com.hp.hpl.jena.util.CollectionFactory;

import junit.framework.*;
import java.util.*;

/**
	TestExpressionConstraints - test the "new" (as of September 2003) Constraint system
    for Query.

	@author kers
*/
public class TestExpressionConstraints extends QueryTestBase 
    {
    public TestExpressionConstraints( String name )    
        { super( name ); }
        
    public static TestSuite suite()
        { return new TestSuite( TestExpressionConstraints.class ); }

    protected static final Expression eTRUE = Expression.TRUE;
    protected static final Expression eFALSE = Expression.FALSE;
    
    public void testConstraintFALSE()
        {
        Graph g = graphWith( "x R y; a P b" );
        Query q = new Query().addMatch( X, ANY, ANY ).addConstraint( eFALSE );
        assertFalse( q.executeBindings( g, justX ).hasNext() ); 
        }    
        
    public void testConstraintTRUE()
        {
        Graph g = graphWith( "x R y; a P b" );
        Query q = new Query().addMatch( X, ANY, ANY ).addConstraint( eTRUE );
        assertTrue( q.executeBindings( g, justX ).hasNext() ); 
        }

    public void testConstraintNE1()
        {        
        Graph g = graphWith( "x R y; a P a" );
        Query q = new Query()
            .addMatch( X, ANY, Y )
            .addConstraint( notEqual( X, Y ) )
            ;
        Set<Node> expected = CollectionFactory.createHashedSet();
        expected.add( node( "x" ) );
        assertEquals( expected, iteratorToSet( q.executeBindings( g, justX ).mapWith( getFirst ) ) );     
        }        
        
    public void testConstraintNE2()
        {        
        Graph g = graphWith( "x R y; a P a" );
        Query q = new Query()
            // .addMatch( Z, ANY, ANY )
            .addMatch( X, ANY, Y )
            .addConstraint( notEqual( X, Y ) )
            ;
        Set<Node> expected = CollectionFactory.createHashedSet();
        expected.add( node( "x" ) );
        assertEquals( expected, iteratorToSet( q.executeBindings( g, justX ).mapWith( getFirst ) ) );     
        }
                
    public void testConstraintNE3()
        {        
        Graph g = graphWith( "x R a; y P b; z Q c" );
        Query q = new Query()
            .addMatch( X, ANY, ANY )
            .addConstraint( notEqual( X, node( "y" ) ) )
            ;
        Set<Node> expected = CollectionFactory.createHashedSet();
        expected.add( node( "x" ) );
        expected.add( node( "z" ) );
        assertEquals( expected, iteratorToSet( q.executeBindings( g, justX ).mapWith( getFirst ) ) );     
        }    
        
    public void testConstraintNE4()
        {        
        Graph g = graphWith( "x R a; y P b; z Q c" );
        Query q = new Query()
            .addMatch( X, ANY, ANY )
            .addConstraint( notEqual( X, node( "y" ) ) )
            .addConstraint( notEqual( X, node( "x" ) ) )
            ;
        Set<Node> expected = CollectionFactory.createHashedSet();
        expected.add( node( "z" ) );
        assertEquals( expected, iteratorToSet( q.executeBindings( g, justX ).mapWith( getFirst ) ) );     
        }
        
    public static class VI implements VariableIndexes
        {
        private Map<String, Integer> values = CollectionFactory.createHashedMap();
        
        public VI set( String x, int i ) { values.put( x, new Integer( i ) ); return this; }    
        
        @Override
        public int indexOf( String name ) { return values.get( name ).intValue(); }
        }
    
    public static class IV implements IndexValues
        {
        private Map<Integer, Node> values = CollectionFactory.createHashedMap();
        
        public IV set( int i, String x ) { values.put( new Integer( i ), Node.createLiteral( x ) ); return this; }    
        
        @Override
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
        { assertEquals( true, Expression.TRUE.prepare( noVariables ).evalBool( noIVs ) ); }
        
    public void testVVFalse()
        { assertEquals( false, Expression.FALSE.prepare( noVariables ).evalBool( noIVs ) ); }

    public void testVVMatches()
        { VariableIndexes vi = new VI().set( "X", 0 ).set( "Y", 1 );
        IndexValues iv = new IV().set( 0, "hello" ).set( 1, "ell" );
        assertEquals( true, matches( X, Y ).prepare( vi ).evalBool( iv ) );  }

        
    public void testPrepareNE()
        {
        Expression e = notEqual( X, Y );
        VariableIndexes map = new Mapping( new Node[0] );
        // Valuator ep = e.prepare( map );        
        }
    
    public void testURIs()
        {
        assertEquals( "http://jena.hpl.hp.com/constraints/NE", notEqual( X, Y ).getFun() );
        assertEquals( "http://jena.hpl.hp.com/constraints/EQ", areEqual( X, Y ).getFun() );
        assertEquals( "http://jena.hpl.hp.com/constraints/MATCHES", matches( X, Y ).getFun() );
        }

    public void testNotConstant()
        {
        assertFalse( notEqual( X, Y ).isConstant() );
        }
    
    public void testDetectAnd()
        {
        Expression e1 = notEqual( X, Y ), e2 = notEqual( X, Z );
        Query q = new Query().addConstraint( Dyadic.and( e1, e2 ) );
        Set<Expression> eBoth = CollectionFactory.createHashedSet(); eBoth.add( e1 ); eBoth.add( e2 );
        Set<Expression> s = iteratorToSet( q.getConstraints().iterator() );
        assertEquals( eBoth, s );
        }
    
    /**
        check that an expression which does not admit to being a constant,
        variable, or application, fails the "containsAllVariableOf" test [and hence will
        not be evaluated early by the evaluator]. 
    */
    public void testUnknownExpression()
        {
        Expression eOpaque = new Expression.Base()
            { @Override
            public Valuator prepare(VariableIndexes vi) 
                { return null; }
            };
        assertFalse( Util.containsAllVariablesOf( new HashSet<String>(), eOpaque ) );
        }
    }
