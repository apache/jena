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

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.ibm.icu.util.StringTokenizer;

import junit.framework.TestSuite;

public class TestQueryTriple extends QueryTestBase
    {
    public TestQueryTriple(String name)
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestQueryTriple.class ); }

    public static final QueryNodeFactory F = QueryNode.factory;
    
    public void testQueryTripleSPO()
        { 
        QueryNode S = new QueryNode.Fixed( NodeCreateUtils.create( "_subject" ) );
        QueryNode P = new QueryNode.Fixed( NodeCreateUtils.create( "predicate" ) );
        QueryNode O = new QueryNode.Fixed( NodeCreateUtils.create( "99" ) );
        QueryTriple t = new QueryTriple( S, P, O );
        assertSame( S, t.S );
        assertSame( P, t.P );
        assertSame( O, t.O );
        }
    
    public void testQueryTripleClassifySimple()
        {
        testQueryTripleClassifySimple( triple( "_x y ?z" ) );
        testQueryTripleClassifySimple( triple( "a b 17" ) );
        testQueryTripleClassifySimple( triple( "?? y _" ) );
        testQueryTripleClassifySimple( triple( "?x y z" ) );
        }

    protected void testQueryTripleClassifySimple(Triple t)
        {
        Mapping m = new Mapping( new Node[0] );
        Mapping m2 = new Mapping( new Node[0] );
        Set<Node> s = new HashSet<Node>();
        QueryTriple q = QueryTriple.classify( F, m, t );
        testClassifiedOK( t.getSubject(), m2, s, q.S );
        testClassifiedOK( t.getPredicate(), m2, s, q.P );
        testClassifiedOK( t.getObject(), m2, s, q.O );
        }

    protected void testClassifiedOK( Node node, Mapping m2, Set<Node> s, QueryNode q )
        {
        assertSame( node, q.node );
        assertSame( QueryNode.classify( F, m2, s, node ).getClass(), q.getClass() );
        }
    
    public void testJustBoundSO()
        {
        Mapping m = new Mapping( new Node[0] );
        QueryTriple q = QueryTriple.classify( F, m, triple( "?x ?y ?x" ) );
        assertEquals( QueryNode.JustBound.class, q.O.getClass() );
        assertEquals( q.S.index, q.O.index );
        }        
    
    public void testJustBoundSP()
        {
        Mapping m = new Mapping( new Node[0] );
        QueryTriple q = QueryTriple.classify( F, m, triple( "?x ?x ?y" ) );
        assertEquals( QueryNode.JustBound.class, q.P.getClass() );
        assertEquals( q.S.index, q.P.index );
        }    
    
    public void testJustBoundPO()
        {
        Mapping m = new Mapping( new Node[0] );
        QueryTriple q = QueryTriple.classify( F, m, triple( "?x ?y ?y" ) );
        assertEquals( QueryNode.JustBound.class, q.O.getClass() );
        assertEquals( q.P.index, q.O.index );
        }    
    
    public void testJustBoundSPO()
        {
        Mapping m = new Mapping( new Node[0] );
        QueryTriple q = QueryTriple.classify( F, m, triple( "?x ?x ?x" ) );
        assertEquals( QueryNode.JustBound.class, q.P.getClass() );
        assertEquals( QueryNode.JustBound.class, q.O.getClass() );
        assertEquals( q.S.index, q.P.index );
        assertEquals( q.S.index, q.O.index );
        }    
    
    public void testSimpleClassifyArray()
        {
        testSimpleClassifyArray( tripleArray( "" ) );
        testSimpleClassifyArray( tripleArray( "a P b" ) );
        testSimpleClassifyArray( tripleArray( "a P b; c Q d" ) );
        testSimpleClassifyArray( tripleArray( "?a P ?b; ?b Q ?c" ) );
        testSimpleClassifyArray( tripleArray( "?a P ?b; ?b Q ?c; ?a Z ?c" ) );
        }

    protected void testSimpleClassifyArray( Triple[] triples )
        {
        Mapping m = new Mapping( new Node[0] );
        QueryTriple [] q = QueryTriple.classify( F, m, triples );
        assertEquals( triples.length, q.length );
        for (int i = 0; i < q.length; i += 1) 
            {
            assertEquals( triples[i].getSubject(), q[i].S.node );
            assertEquals( triples[i].getPredicate(), q[i].P.node );
            assertEquals( triples[i].getObject(), q[i].O.node );
            }
        }
   
    public void testJustBoundConfinement()
        {
        Mapping m = new Mapping( new Node[0] );
        QueryTriple [] q = QueryTriple.classify( F, m, tripleArray( "?x P ?x; ?x Q ?x" ) );
        assertInstanceOf( QueryNode.Bind.class, q[0].S );
        assertInstanceOf( QueryNode.JustBound.class, q[0].O );
        assertInstanceOf( QueryNode.Bound.class, q[1].S );
        }
    
    protected static final String [][] matchings =
        {
            { "s P o", "s P o", "y" },
            { "s P o", "a Q b", "y" },
            { "?x P y", "a P y", "y0=a" },
            { "?x P ?y", "go P og", "y0=go;1=og" },
            { "?x P ?x", "a P a", "y0=a" },
            { "?x P ?x", "a P b", "n" }
        };
    
    public void testMatch()
        {
        for (int i = 0; i < matchings.length; i += 1)
            {
            String [] m = matchings[i];
            testMatch( triple( m[0] ), triple( m[1] ), m[2].charAt(0) == 'y', m[2].substring(1) );
            }
        }
    
    protected void testMatch( Triple toClassify, Triple toMatch, boolean result, String bindings )
        {
        Mapping map = new Mapping( new Node[0] );
        QueryTriple t = QueryTriple.classify( F, map, toClassify );
        Matcher m = t.createMatcher();
        Domain d = new Domain( 3 );
        assertEquals( result, m.match( d, toMatch ) );
        StringTokenizer st = new StringTokenizer( bindings, ";" );
        while (st.hasMoreTokens()) testBinding( d, st.nextToken() );
        }

    protected void testBinding( Domain d, String binding )
        {
        int eq = binding.indexOf( '=' );
        int index = Integer.parseInt( binding.substring( 0, eq ) );
        Node value = NodeCreateUtils.create( binding.substring( eq + 1 ) );
        assertEquals( value, d.getElement( index ) );
        }
    }
