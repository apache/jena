/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.graph;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;

/** Basic add and delete tests for a graph */

public abstract class AbstractTestGraph2 extends BaseTest
{
    // This will become the basis for a general graph test in Jena
    protected static final Node s1 = NodeFactory.parseNode("<ex:s1>") ;
    protected static final Node p1 = NodeFactory.parseNode("<ex:p1>") ;
    protected static final Node o1 = NodeFactory.parseNode("<ex:o1>") ;

    protected static final Node s2 = NodeFactory.parseNode("<ex:s2>") ;
    protected static final Node p2 = NodeFactory.parseNode("<ex:p2>") ;
    protected static final Node o2 = NodeFactory.parseNode("<ex:o2>") ;
    
    protected static final Node lit1 = NodeFactory.parseNode("'lex'") ;
    protected static final Node lit2 = NodeFactory.parseNode("'lex'@en") ;
    protected static final Node lit3 = NodeFactory.parseNode("123") ;
    
    static Triple triple(Node s, Node p, Node o)
    { return new Triple(s, p, o) ; }
    
    protected abstract Graph emptyGraph() ;
    protected abstract void returnGraph(Graph g) ;
    
    @Test public void graph_01()
    {
        Graph g = emptyGraph() ;
        assertEquals(0, g.size()) ;
        returnGraph(g) ;
    }
    
    @Test public void graph_add_01()
    {
        Graph g = emptyGraph() ;
        Triple t = triple(s1, p1, o1) ;
        g.add(t) ;
        assertEquals(1, g.size()) ;
        assertTrue(g.contains(t)) ;
        assertTrue(g.contains(s1,p1,o1)) ;
        returnGraph(g) ;
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
        returnGraph(g) ;

    }
    
    @Test public void graph_add_03()
    {
        Graph g = emptyGraph() ;
        // SPO twice -- as different nodes.
        Node ns1 = NodeFactory.parseNode("<ex:s>") ;
        Node np1 = NodeFactory.parseNode("<ex:p>") ;
        Node no1 = NodeFactory.parseNode("<ex:o>") ;
        
        Node ns2 = NodeFactory.parseNode("<ex:s>") ;
        Node np2 = NodeFactory.parseNode("<ex:p>") ;
        Node no2 = NodeFactory.parseNode("<ex:o>") ;
        
        Triple t1 = triple(ns1, np1, no1) ;
        Triple t2 = triple(ns2, np2, no2) ;
        g.add(t1) ;
        g.add(t2) ;
        assertEquals(1, g.size()) ;
        assertTrue(g.contains(t1)) ;
        assertTrue(g.contains(t2)) ;
        assertTrue(g.contains(ns1,np1,no1)) ;
        returnGraph(g) ;

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
        Node o = NodeFactory.parseNode("<ex:lex>") ;
        assertFalse(g.contains(s1,p1,o)) ;
        returnGraph(g) ;

    }        
        
    @Test public void graph_add_delete_01()
    {
        Graph g = emptyGraph() ;
        Triple t = triple(s1, p1, o1) ;
        g.add(t) ;
        g.delete(t) ;
        assertEquals(0, g.size()) ;
        assertFalse("g contains t", g.contains(t)) ;
        returnGraph(g) ;
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
        returnGraph(g) ;
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
        returnGraph(g) ;
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
        returnGraph(g) ;
    }

    @Test public void graph_add_find_01()
    {
        // Tests the "unknown node" handling
        Graph g = emptyGraph() ;
        Triple t1 = triple(s1, p1, o1) ;
        assertEquals(0, g.size()) ;
        assertFalse(g.contains(t1)) ;
        g.add(t1) ;
        assertTrue(g.contains(t1)) ;
        returnGraph(g) ;
    }
    
    @Test public void graph_add_find_02()
    {
        // Tests the "unknown node" handling
        Graph g = emptyGraph() ;
        Triple t1 = triple(s1, p1, o1) ;
        assertEquals(0, g.size()) ;
        assertFalse(g.contains(t1)) ;
        g.add(t1) ;
        assertTrue(g.contains(t1)) ;
        returnGraph(g) ;
    }

    private static Node any = Node.ANY ;
    
    @Test public void remove_01()
    {
        Graph g = emptyGraph() ;
        Triple t1 = triple(s1, p1, o1) ;
        g.add(t1) ;
        g.getBulkUpdateHandler().remove(any, any, any) ;
        assertEquals(0, g.size()) ;
        returnGraph(g) ;
    }
    
    @Test public void remove_02()
    {
        Graph g = emptyGraph() ;
        Triple t1 = triple(s1, p1, o1) ;
        g.add(t1) ;
        g.getBulkUpdateHandler().remove(s2, any, any) ;
        assertEquals(1, g.size()) ;
        assertTrue(g.contains(t1)) ;
        returnGraph(g) ;
    }

    @Test public void remove_03()
    {
        Graph g = emptyGraph() ;
        Triple t1 = triple(s1, p1, o1) ;
        g.add(t1) ;
        g.getBulkUpdateHandler().remove(s1, any, any) ;
        assertEquals(0, g.size()) ;
        returnGraph(g) ;
    }

    @Test public void removeAll_01()
    {
        Graph g = emptyGraph() ;
        Triple t1 = triple(s1, p1, o1) ;
        Triple t2 = triple(s1, p1, o2) ;
        Triple t3 = triple(s2, p1, o1) ;
        Triple t4 = triple(s2, p1, o2) ;
        g.add(t1) ;
        g.add(t2) ;
        g.add(t3) ;
        g.add(t4) ;
        g.getBulkUpdateHandler().removeAll() ;
        assertEquals(0, g.size()) ;
        returnGraph(g) ;
    }
       
    @Test public void count_01()
    {
        Graph g = emptyGraph() ;
        assertEquals(0, g.size()) ;
        Triple t1 = triple(s1, p1, o1) ;
        g.add(t1) ;
        assertEquals(1, g.size()) ;
        returnGraph(g) ;
    }
    
    // Tests : triples and values.
    
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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