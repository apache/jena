/*
    (c) Copyright 2005 Hewlett-Packard Development Company, LP
    All rights reserved - see end of file.
    $Id: TestQueryTriple.java,v 1.1 2005-07-24 18:58:10 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.query.test;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.Mapping;
import com.hp.hpl.jena.graph.query.QueryNode;
import com.hp.hpl.jena.graph.query.QueryTriple;

import junit.framework.TestSuite;

public class TestQueryTriple extends QueryTestBase
    {
    public TestQueryTriple(String name)
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestQueryTriple.class ); }
    
    public void testQueryTripleSPO()
        { 
        QueryNode S = new QueryNode.Fixed( Node.create( "_subject" ) );
        QueryNode P = new QueryNode.Fixed( Node.create( "predicate" ) );
        QueryNode O = new QueryNode.Fixed( Node.create( "99" ) );
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
        Set s = new HashSet();
        QueryTriple q = QueryTriple.classify( m, t );
        testClassifiedOK( t.getSubject(), m2, s, q.S );
        testClassifiedOK( t.getPredicate(), m2, s, q.P );
        testClassifiedOK( t.getObject(), m2, s, q.O );
        }

    protected void testClassifiedOK( Node node, Mapping m2, Set s, QueryNode q )
        {
        assertSame( node, q.node );
        assertSame( QueryNode.classify( m2, s, node ).getClass(), q.getClass() );
        }
    
    public void testJustBoundSO()
        {
        Mapping m = new Mapping( new Node[0] );
        QueryTriple q = QueryTriple.classify( m, triple( "?x ?y ?x" ) );
        assertEquals( QueryNode.JustBound.class, q.O.getClass() );
        assertEquals( q.S.index, q.O.index );
        }        
    
    public void testJustBoundSP()
        {
        Mapping m = new Mapping( new Node[0] );
        QueryTriple q = QueryTriple.classify( m, triple( "?x ?x ?y" ) );
        assertEquals( QueryNode.JustBound.class, q.P.getClass() );
        assertEquals( q.S.index, q.P.index );
        }    
    
    public void testJustBoundPO()
        {
        Mapping m = new Mapping( new Node[0] );
        QueryTriple q = QueryTriple.classify( m, triple( "?x ?y ?y" ) );
        assertEquals( QueryNode.JustBound.class, q.O.getClass() );
        assertEquals( q.P.index, q.O.index );
        }    
    
    public void testJustBoundSPO()
        {
        Mapping m = new Mapping( new Node[0] );
        QueryTriple q = QueryTriple.classify( m, triple( "?x ?x ?x" ) );
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
        QueryTriple [] q = QueryTriple.classify( m, triples );
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
        QueryTriple [] q = QueryTriple.classify( m, tripleArray( "?x P ?x; ?x Q ?x" ) );
        assertTrue( q[0].S instanceof QueryNode.Bind );
        assertTrue( q[0].O instanceof QueryNode.JustBound );
        assertTrue( q[1].S instanceof QueryNode.Bound );
        }
    }

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/