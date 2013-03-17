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

package com.hp.hpl.jena.sparql.graph;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;

/** Basic add and delete tests for a graph */

public abstract class AbstractTestGraph2 extends BaseTest
{
    // This will become the basis for a general graph test in Jena
    protected static final Node s1 = NodeFactoryExtra.parseNode("<ex:s1>") ;
    protected static final Node p1 = NodeFactoryExtra.parseNode("<ex:p1>") ;
    protected static final Node o1 = NodeFactoryExtra.parseNode("<ex:o1>") ;

    protected static final Node s2 = NodeFactoryExtra.parseNode("<ex:s2>") ;
    protected static final Node p2 = NodeFactoryExtra.parseNode("<ex:p2>") ;
    protected static final Node o2 = NodeFactoryExtra.parseNode("<ex:o2>") ;
    
    protected static final Node lit1 = NodeFactoryExtra.parseNode("'lex'") ;
    protected static final Node lit2 = NodeFactoryExtra.parseNode("'lex'@en") ;
    protected static final Node lit3 = NodeFactoryExtra.parseNode("123") ;
    
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
        Node ns1 = NodeFactoryExtra.parseNode("<ex:s>") ;
        Node np1 = NodeFactoryExtra.parseNode("<ex:p>") ;
        Node no1 = NodeFactoryExtra.parseNode("<ex:o>") ;
        
        Node ns2 = NodeFactoryExtra.parseNode("<ex:s>") ;
        Node np2 = NodeFactoryExtra.parseNode("<ex:p>") ;
        Node no2 = NodeFactoryExtra.parseNode("<ex:o>") ;
        
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
        Node o = NodeFactoryExtra.parseNode("<ex:lex>") ;
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
        g.remove(any, any, any) ;
        assertEquals(0, g.size()) ;
        returnGraph(g) ;
    }
    
    @Test public void remove_02()
    {
        Graph g = emptyGraph() ;
        Triple t1 = triple(s1, p1, o1) ;
        g.add(t1) ;
        g.remove(s2, any, any) ;
        assertEquals(1, g.size()) ;
        assertTrue(g.contains(t1)) ;
        returnGraph(g) ;
    }

    @Test public void remove_03()
    {
        Graph g = emptyGraph() ;
        Triple t1 = triple(s1, p1, o1) ;
        g.add(t1) ;
        g.remove(s1, any, any) ;
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
        g.clear() ;
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
