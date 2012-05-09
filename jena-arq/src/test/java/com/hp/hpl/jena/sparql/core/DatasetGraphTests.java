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

package com.hp.hpl.jena.sparql.core;
import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertNotNull ;
import static org.junit.Assert.assertTrue ;

import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import org.junit.Test ;
import org.openjena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public abstract class DatasetGraphTests
{
    protected abstract DatasetGraph emptyDataset() ;
    
    @Test public void create_1()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        assertNotNull(dsg.getDefaultGraph()) ;
        assertTrue(dsg.getDefaultGraph().isEmpty()) ;
    }
    
    // Quad operations 
    /*
     * void add(Quad quad)
     * void delete(Quad quad)
     * void deleteAny(Node g, Node s, Node p, Node o)
     * Iterator<Quad> find(Quad quad)
     * Iterator<Quad> find(Node g, Node s, Node p , Node o)
     * boolean contains(Node g, Node s, Node p , Node o)
     * boolean contains(Quad quad)
     */
    @Test public void quad_01()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Quad quad = SSE.parseQuad("(quad <g> <s> <p> <o>)") ;
        dsg.add(quad) ;
        assertTrue(dsg.contains(quad)) ;
        
        Iterator<Quad> iter = dsg.find(quad) ;
        assertTrue(iter.hasNext()) ;
        Quad quad2 = iter.next();
        assertFalse(iter.hasNext()) ;
        assertEquals(quad, quad2) ;
        
        // and the graph view.
        assertTrue(dsg.getDefaultGraph().isEmpty()) ;
        assertFalse(dsg.getGraph(Node.createURI("g")).isEmpty()) ;
    }
    
    @Test public void quad_02()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Quad quad = SSE.parseQuad("(quad <g> <s> <p> <o>)") ;
        dsg.add(quad) ;
        dsg.containsGraph(Node.createURI("g")) ;
        
        dsg.delete(quad) ;
        assertTrue(dsg.isEmpty()) ;
        assertTrue(dsg.getDefaultGraph().isEmpty()) ;
        assertTrue(dsg.getGraph(Node.createURI("g")).isEmpty()) ;
    }
    
    @Test public void quad_03()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Quad quad1 = SSE.parseQuad("(quad <g> <s> <p> <o1>)") ;
        Quad quad2 = SSE.parseQuad("(quad <g> <s> <p> <o2>)") ;
        dsg.add(quad1) ; 
        dsg.add(quad2) ; 
        
        dsg.deleteAny(Node.createURI("g"), Node.createURI("s"), null, null) ;
        assertFalse(dsg.contains(quad1)) ; 
        assertFalse(dsg.contains(quad2)) ; 
    }
    
    @Test public void quad_04()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Quad quad1 = SSE.parseQuad("(quad <g> <s> <p> <o1>)") ;
        Quad quad2 = SSE.parseQuad("(quad <g> <s> <p> <o2>)") ;
        dsg.add(quad1) ; 
        dsg.add(quad2) ; 
        Iterator<Quad> iter = dsg.find(Node.createURI("g"), Node.createURI("s"), null, null) ;

        assertTrue(iter.hasNext()) ;
        Set<Quad> x = Iter.iter(iter).toSet() ;
        assertEquals(2, x.size()) ;
        assertTrue(x.contains(quad1)) ;
        assertTrue(x.contains(quad2)) ;
    }

    @Test public void quad_05()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Quad quad1 = SSE.parseQuad("(quad <g> <s> <p> <o1>)") ;
        Quad quad2 = SSE.parseQuad("(quad <g> <s> <p> <o2>)") ;
        Node g  = SSE.parseNode("<g>") ;
        Node s  = SSE.parseNode("<s>") ;
        Node p  = SSE.parseNode("<p>") ;
        Node o1 = SSE.parseNode("<o1>") ;
        Node o2 = SSE.parseNode("<o2>") ;
        
        dsg.add(g,s,p,o1) ;
        assertTrue(dsg.contains(quad1)) ;
        assertTrue(dsg.contains(g,s,p,o1)) ;
        assertFalse(dsg.contains(g,s,p,o2)) ;
    }


    /*
     * getDefaultGraph()
     * getGraph(Node) 
     * containsGraph(Node)
     * ???? setDefaultGraph(Graph) 
     * addGraph(Node, Graph)
     * removeGraph(Node)
     * listGraphNodes()
     */
    
    // Graph centric operations
    @Test public void graph_00()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Node gn = Node.createURI("g") ;
        Graph g = GraphFactory.createDefaultGraph() ;
        g.add(SSE.parseTriple("(<s> <p> <o>)")) ;   // So the graph is not empty.
        dsg.addGraph(gn, g);
        assertTrue(dsg.containsGraph(gn)) ;
    }
    
    // Graph centric operations
    @Test public void graph_01()
    {
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Node g = Node.createURI("g") ;
        
        Triple t = SSE.parseTriple("(<s> <p> <o>)") ;
        
        dsg.getGraph(g).add(t) ;
        assertTrue(dsg.getGraph(g).contains(t)) ;
        
        Quad quad = SSE.parseQuad("(quad <g> <s> <p> <o>)") ;
        Iterator<Quad> iter = dsg.find(null, null, null, null) ;
        
        assertTrue(iter.hasNext()) ;
        Quad quad2 = iter.next();
        assertFalse(iter.hasNext()) ;
        assertEquals(quad, quad2) ;
        
        assertTrue(dsg.getDefaultGraph().isEmpty()) ;
        assertFalse(dsg.getGraph(Node.createURI("g")).isEmpty()) ;
    }

    @Test public void graph_02()
    {
        Node g = Node.createURI("g") ;
        DatasetGraph dsg = emptyDataset() ;
        assertNotNull(dsg) ;
        Quad quad = SSE.parseQuad("(quad <g> <s> <p> <o>)") ;
        dsg.add(quad) ;
        
        Triple t = SSE.parseTriple("(<s> <p> <o>)") ;
        dsg.getGraph(g).delete(t) ;
        assertTrue(dsg.getDefaultGraph().isEmpty()) ;
        assertTrue(dsg.getGraph(Node.createURI("g")).isEmpty()) ;
        assertFalse(dsg.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY).hasNext()) ; 
    }
    
    @Test public void graph_03()
    {
        Node g = Node.createURI("g") ;
        DatasetGraph dsg = emptyDataset() ;
        Graph data = SSE.parseGraph("(graph (<s> <p> <o>))") ;
        dsg.addGraph(g, data) ;
        Quad quad = SSE.parseQuad("(quad <g> <s> <p> <o>)") ;
        assertTrue(dsg.contains(quad)) ;
    }
    
    @Test public void find_01()
    {
        Node g1 = Node.createURI("g1") ;
        DatasetGraph dsg = emptyDataset() ;
        Quad quad1 = SSE.parseQuad("(quad <g1> <s1> <p1> <o1>)") ;
        Quad quad2 = SSE.parseQuad("(quad <g2> <s2> <p2> <o2>)") ;
        dsg.add(quad1) ;
        dsg.add(quad2) ;
        
        List<Quad> quads = Iter.toList(dsg.find(g1, null, null, null)) ;
        assertEquals(1, quads.size()) ;
        Quad q = quads.get(0) ; 
        assertEquals(quad1, q) ;
    }
    
    @Test public void deleteAny_01()
    {
        DatasetGraph dsg = emptyDataset() ;
        Node subject = Node.createURI("http://example/s");
        Node predicate = Node.createURI("http://example/p");
        Node object = Node.createAnon();
        dsg.add(new Quad(Quad.defaultGraphIRI, subject, predicate, object));
        dsg.deleteAny(Node.ANY, subject, null, null);
    }
  
}
