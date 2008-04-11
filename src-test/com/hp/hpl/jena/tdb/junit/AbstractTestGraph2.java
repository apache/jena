/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.junit;

import junit.TestBase;
import org.junit.Test;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.sse.SSE;

/** Basic add and delete tests for a graph */

public abstract class AbstractTestGraph2 extends TestBase
{
    // This will become the basis for a general graph test in Jena(3)
    protected static Node s1 = makeNode("<s1>") ;
    protected static Node p1 = makeNode("<p1>") ;
    protected static Node o1 = makeNode("<o1>") ;

    protected static Node s2 = makeNode("<s2>") ;
    protected static Node p2 = makeNode("<p2>") ;
    protected static Node o2 = makeNode("<o2>") ;
    
    protected static Node lit1 = makeNode("'lex'") ;
    protected static Node lit2 = makeNode("'lex'@en") ;
    protected static Node lit3 = makeNode("123") ;
    
    static Triple triple(Node s, Node p, Node o)
    { return new Triple(s, p, o) ; }
    
    protected abstract Graph emptyGraph() ;
    protected static Node makeNode(String str) { return  SSE.parseNode(str) ; }
    
    @Test public void graph_01()
    {
        Graph g = emptyGraph() ;
        assertEquals(0, g.size()) ;
    }
    
    @Test public void graph_add_01()
    {
        Graph g = emptyGraph() ;
        Triple t = triple(s1, p1, o1) ;
        g.add(t) ;
        assertEquals(1, g.size()) ;
        assertTrue(g.contains(t)) ;
        assertTrue(g.contains(s1,p1,o1)) ;
    }
    
    @Test public void graph_add_02()
    {
        Graph g = emptyGraph() ;

        Triple t = triple(s1, p1, o1) ;
        g.add(t) ;
        g.add(t) ;
        assertEquals(1, g.size()) ;
        assertTrue(g.contains(t)) ;
        assertTrue(g.contains(s1,p1,o1)) ;
    }
    
    @Test public void graph_add_03()
    {
        Graph g = emptyGraph() ;
        // SPO twice -- as different nodes.
        Node ns1 = makeNode("<s>") ;
        Node np1 = makeNode("<p>") ;
        Node no1 = makeNode("<o>") ;
        Node ns2 = makeNode("<s>") ;
        Node np2 = makeNode("<p>") ;
        Node no2 = makeNode("<o>") ;
        
        Triple t1 = triple(ns1, np1, no1) ;
        Triple t2 = triple(ns2, np2, no2) ;
        g.add(t1) ;
        g.add(t2) ;
        assertEquals(1, g.size()) ;
        assertTrue(g.contains(t1)) ;
        assertTrue(g.contains(t2)) ;
        assertTrue(g.contains(ns1,np1,no1)) ;
    }

    @Test public void graph_add_04()
    {
        Graph g = emptyGraph() ;
        // Literals
        Triple t1 = triple(s1, p1, lit1) ;
        Triple t2 = triple(s1, p1, lit2) ;
        g.add(t1) ;
        g.add(t2) ;
        assertEquals(2, g.size()) ;
        assertTrue(g.contains(t1)) ;
        assertTrue(g.contains(t2)) ;
        assertTrue(g.contains(s1,p1,lit1)) ;
        assertTrue(g.contains(s1,p1,lit2)) ;
        Node o = makeNode("<lex>") ;
        assertFalse(g.contains(s1,p1,o)) ;
    }        
        
    @Test public void graph_add_delete_01()
    {
        Graph g = emptyGraph() ;
        Triple t = triple(s1, p1, o1) ;
        g.add(t) ;
        g.delete(t) ;
        assertEquals(0, g.size()) ;
        assertFalse("g contains t", g.contains(t)) ;
    }
    
    @Test public void graph_add_delete_02()
    {
        Graph g = emptyGraph() ;
        Triple t = triple(s1, p1, o1) ;
        // reversed from above
        g.delete(t) ;
        g.add(t) ;
        assertEquals(1, g.size()) ;
        assertTrue("g does not contain t", g.contains(t)) ;
    }

    @Test public void graph_add_delete_03()
    {
        Graph g = emptyGraph() ;
        Triple t = triple(s1, p1, o1) ;
        // Add twice, delete once => empty
        g.add(t) ;
        g.add(t) ;
        g.delete(t) ;
        assertEquals(0, g.size()) ;
        assertFalse("g contains t", g.contains(t)) ;
    }
    
    @Test public void graph_add_delete_04()
    {
        Graph g = emptyGraph() ;
        Triple t1 = triple(s1, p1, o1) ;
        Triple t2 = triple(s2, p2, o2) ;
        g.add(t1) ;
        g.add(t2) ;
        g.delete(t1) ;
        assertEquals(1, g.size()) ;
        assertTrue("g does not contain t2", g.contains(t2)) ;
    }

    // Tests : triples and values.
    
    
    
    // XXX More graph tests
    
    //    @Test public void graph_01()
//    {
//        Graph g = emptyGraph() ;
//    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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