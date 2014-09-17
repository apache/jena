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

package com.hp.hpl.jena.sparql.modify;


import org.apache.jena.atlas.iterator.Iter ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.graph.GraphFactory ;
import com.hp.hpl.jena.sparql.modify.request.* ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.syntax.Element ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateExecutionFactory ;
import com.hp.hpl.jena.update.UpdateProcessor ;

public abstract class AbstractTestUpdateGraph extends AbstractTestUpdateBase
{
    protected static Node s = NodeFactoryExtra.parseNode("<http://example/r>") ;
    protected static Node p = NodeFactoryExtra.parseNode("<http://example/p>") ;
    protected static Node q = NodeFactoryExtra.parseNode("<http://example/q>") ;
    protected static Node v = NodeFactoryExtra.parseNode("<http://example/v>") ;
    
    protected static Node o1 = NodeFactoryExtra.parseNode("2007") ;
    protected static Triple triple1 =  new Triple(s,p,o1) ;
    protected static Node o2 = NodeFactoryExtra.parseNode("1066") ;
    protected static Triple triple2 =  new Triple(s,p,o2) ;
    protected static Graph graph1 = data1() ;
    protected static Node graphIRI = NodeFactoryExtra.parseNode("<http://example/graph>") ;
    
    @Test public void testInsertData1()
    {
		GraphStore gStore = getEmptyGraphStore() ;
		defaultGraphData(gStore, graph1) ;
		QuadDataAcc acc = new QuadDataAcc() ;
		acc.addTriple(triple2) ;
        UpdateDataInsert insert = new UpdateDataInsert(acc) ;
        UpdateProcessor uProc = UpdateExecutionFactory.create(insert, gStore) ;
        uProc.execute(); 
        
        assertFalse(graphEmpty(gStore.getDefaultGraph())) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple1)) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple2)) ;
    }

    @Test public void testDeleteData1()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, graph1) ;
        QuadDataAcc acc = new QuadDataAcc() ;
        acc.addTriple(triple2) ;
        UpdateDataDelete delete = new UpdateDataDelete(acc) ;
        UpdateProcessor uProc = UpdateExecutionFactory.create(delete, gStore) ;
        uProc.execute(); 

        assertFalse(graphEmpty(gStore.getDefaultGraph())) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple1)) ;
        assertFalse(graphContains(gStore.getDefaultGraph(), triple2)) ;
    }

    @Test public void testDeleteData2()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, graph1) ;
        QuadDataAcc acc = new QuadDataAcc() ;
        acc.addTriple(triple1) ;
        UpdateDataDelete delete = new UpdateDataDelete(acc) ;
        UpdateProcessor uProc = UpdateExecutionFactory.create(delete, gStore) ;
        uProc.execute(); 

        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
        assertFalse(graphContains(gStore.getDefaultGraph(), triple1)) ;
    }
    
    @Test public void testInsert1()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        UpdateModify insert = new UpdateModify() ;
        UpdateAction.execute(insert, gStore) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    @Test public void testInsert2()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        UpdateModify insert = new UpdateModify() ;
        insert.getInsertAcc().addTriple(triple1) ;
        UpdateAction.execute(insert, gStore) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple1)) ;
    }
    
    @Test public void testInsert3()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        gStore.addGraph(graphIRI, Factory.createDefaultGraph()) ;
        UpdateModify insert = new UpdateModify() ;
        insert.getInsertAcc().addQuad(new Quad(graphIRI, triple1)) ;
        UpdateAction.execute(insert, gStore) ;
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1)) ;
    }

    @Test public void testInsert4()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, graph1) ;
        UpdateModify insert = new UpdateModify() ;
        insert.getInsertAcc().addTriple(SSE.parseTriple("(?s <http://example/p> 1066)")) ;
        Element element = QueryFactory.createElement("{ ?s <http://example/p> 2007 }" ) ;
        insert.setElement(element) ;
        UpdateAction.execute(insert, gStore) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple2)) ;
    }
    
    @Test public void testDelete1()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        QuadAcc acc = new QuadAcc() ;
        UpdateDeleteWhere delete = new UpdateDeleteWhere(acc) ;
        UpdateAction.execute(delete, gStore) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    @Test public void testDelete2()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, graph1) ;
        QuadAcc acc = new QuadAcc() ;
        UpdateDeleteWhere delete = new UpdateDeleteWhere(acc) ;
        acc.addTriple(SSE.parseTriple("(?s ?p ?o)")) ;
        UpdateAction.execute(delete, gStore) ;
        assertTrue("Not empty", graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    @Test public void testDelete3()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, graph1) ;
        QuadDataAcc acc = new QuadDataAcc() ;
        UpdateDataDelete delete = new UpdateDataDelete(acc) ;
        acc.addTriple(triple1) ;
        UpdateAction.execute(delete, gStore) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    
    @Test public void testDelete4()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        namedGraphData(gStore, graphIRI, data1()) ;
        
        QuadDataAcc acc = new QuadDataAcc() ;
        UpdateDataDelete delete = new UpdateDataDelete(acc) ;
        acc.setGraph(graphIRI) ;
        acc.addTriple(triple1) ;
        UpdateAction.execute(delete, gStore) ;
        assertTrue(graphEmpty(gStore.getGraph(graphIRI))) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    @Test public void testDelete5()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, data2()) ;
        namedGraphData(gStore, graphIRI, data1()) ;
        
        UpdateModify modify = new UpdateModify() ;
        Element element = QueryFactory.createElement("{ ?s <http://example/p> ?o }" ) ;
        modify.setElement(element) ;
        modify.getDeleteAcc().addQuad(SSE.parseQuad("(<http://example/graph> ?s <http://example/p> 2007 )")) ;
        UpdateAction.execute(modify, gStore) ;

        assertTrue("Not empty", graphEmpty(gStore.getGraph(graphIRI))) ;
        assertFalse(graphEmpty(gStore.getDefaultGraph())) ;
    }

    @Test public void testModify1()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, data2()) ;
        namedGraphData(gStore, graphIRI, Factory.createDefaultGraph()) ;
        
        UpdateModify modify = new UpdateModify() ;
        Element element = QueryFactory.createElement("{ ?s <http://example/p> ?o }" ) ;
        modify.setElement(element) ;
        modify.getInsertAcc().addQuad(new Quad(graphIRI, triple1)) ;
        modify.getDeleteAcc().addTriple(SSE.parseTriple("(?s <http://example/p> ?o)")) ;
        modify.getDeleteAcc().addQuad(SSE.parseQuad("(<http://example/graph> ?s <http://example/p> ?o)")) ; 
        UpdateAction.execute(modify, gStore) ;
        
        assertFalse(graphEmpty(gStore.getGraph(graphIRI))) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1)) ;
    }
    
    @Test public void testModify2()
    {
        // Use blank nodes (will expose any problems in serialization when spill occurs)
        Triple t =  new Triple(com.hp.hpl.jena.graph.NodeFactory.createAnon(),p,o2);
        
        GraphStore gStore = getEmptyGraphStore() ;
        // Set the threshold to in order to force spill to disk
        gStore.getContext().set(ARQ.spillToDiskThreshold, 0L) ;
        
        defaultGraphData(gStore, data(t)) ;
        namedGraphData(gStore, graphIRI, data(t));
        
        UpdateModify modify = new UpdateModify() ;
        Element element = QueryFactory.createElement("{ ?s <http://example/p> ?o }" ) ;
        modify.setElement(element) ;
        modify.getInsertAcc().addQuad(new Quad(graphIRI, triple1)) ;
        modify.getDeleteAcc().addTriple(SSE.parseTriple("(?s <http://example/p> ?o)")) ;
        modify.getDeleteAcc().addQuad(SSE.parseQuad("(<http://example/graph> ?s <http://example/p> ?o)")) ; 
        UpdateAction.execute(modify, gStore) ;
        
        assertFalse(graphEmpty(gStore.getGraph(graphIRI))) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1)) ;
        assertFalse(graphContains(gStore.getGraph(graphIRI), t));
    }
    
    @Test public void testModifyInitialBindings()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, data12()) ;
        namedGraphData(gStore, graphIRI, Factory.createDefaultGraph()) ;
        
        Binding initialBinding = BindingFactory.binding(Var.alloc("o"), o1) ;
        
        UpdateModify modify = new UpdateModify() ;
        Element element = QueryFactory.createElement("{ ?s <http://example/p> ?o }" ) ;
        modify.setElement(element) ;
        modify.getInsertAcc().addQuad(new Quad(graphIRI, triple1)) ;
        modify.getDeleteAcc().addTriple(SSE.parseTriple("(?s <http://example/p> ?o)")) ;
        modify.getDeleteAcc().addQuad(SSE.parseQuad("(<http://example/graph> ?s <http://example/p> ?o)")) ; 
        UpdateAction.execute(modify, gStore, initialBinding) ;
        
        assertFalse(graphEmpty(gStore.getGraph(graphIRI))) ;
        assertFalse(graphEmpty(gStore.getDefaultGraph())) ;
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1)) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple2)) ;
        assertFalse(graphContains(gStore.getDefaultGraph(), triple1)) ;
    }
    
    @Test public void testCopy()
    {
        // Use blank nodes (will expose any problems in serialization when spill occurs)
        Triple t =  new Triple(com.hp.hpl.jena.graph.NodeFactory.createAnon(),p,o2);
        Triple t2 = new Triple(com.hp.hpl.jena.graph.NodeFactory.createAnon(),p,o1);
        
        GraphStore gStore = getEmptyGraphStore() ;
        // Set the threshold to in order to force spill to disk
        gStore.getContext().set(ARQ.spillToDiskThreshold, 0L) ;
        
        defaultGraphData(gStore, data(triple1, triple2, t)) ;
        namedGraphData(gStore, graphIRI, data(t2));
        
        UpdateCopy copy = new UpdateCopy(Target.DEFAULT, Target.create(graphIRI));
        UpdateAction.execute(copy, gStore);
        
        assertFalse(graphEmpty(gStore.getDefaultGraph())) ;
        assertFalse(graphEmpty(gStore.getGraph(graphIRI))) ;
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1)) ;
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple2)) ;
        assertTrue(graphContains(gStore.getGraph(graphIRI), t)) ;
        assertFalse(graphContains(gStore.getGraph(graphIRI), t2)) ;
        assertTrue(gStore.getDefaultGraph().isIsomorphicWith(gStore.getGraph(graphIRI)));
    }
    
    @Test public void testUpdateScript1()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "update-1.ru") ;
        assertTrue(graphContains(gStore.getDefaultGraph(), new Triple(s,p,NodeFactoryExtra.parseNode("123")))) ;
    }
    
    @Test public void testUpdateScript2()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "update-2.ru") ;
        assertTrue(graphContains(gStore.getGraph(com.hp.hpl.jena.graph.NodeFactory.createURI("http://example/g1")),
                                 new Triple(s,p,NodeFactoryExtra.parseNode("123")))) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }

    @Test public void testUpdateScript3()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "update-3.ru") ;
        assertTrue(graphEmpty(gStore.getGraph(com.hp.hpl.jena.graph.NodeFactory.createURI("http://example/g1")))) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }

    @Test public void testUpdateScript4()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "data-1.ru") ;
        assertTrue(graphContains(gStore.getDefaultGraph(),
                                 new Triple(s,p,NodeFactoryExtra.parseNode("123")))) ;
    }
    
    @Test public void testUpdateScript5()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "data-2.ru") ;
        
        
        Graph g = GraphFactory.createPlainGraph() ;
        Node b = com.hp.hpl.jena.graph.NodeFactory.createAnon() ;
        
        g.add(new Triple(s, p, b)) ;
        g.add(new Triple(b, q, v)) ;
        assertTrue(g.isIsomorphicWith(gStore.getDefaultGraph())) ;
    }
    
    @Test public void testUpdateScript6()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "data-3.ru") ;
        assertTrue(graphContains(gStore.getGraph(graphIRI),
                                 new Triple(s,p,NodeFactoryExtra.parseNode("123")))) ;
    }
    
    @Test public void testUpdateScript7()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "data-4.ru") ;
        assertTrue(graphContains(gStore.getDefaultGraph(),
                                 new Triple(s,p,NodeFactoryExtra.parseNode("123")))) ;
        Graph g = gStore.getGraph(graphIRI) ;
        assertTrue(graphContains(gStore.getGraph(graphIRI),
                                 new Triple(s,p,o2))) ;
    }
    
    @Test(expected = QueryException.class) public void testUpdateBad1()      { testBad("bad-1.ru", 1) ; }
    @Test public void testUpdateBad2()      { testBad("bad-2.ru", 1) ; }
    @Test public void testUpdateBad3()      { testBad("bad-3.ru", 0) ; }

    private void testBad(String file, int expectedSize)
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, file) ;
        assertEquals(expectedSize, countQuads(gStore)) ;
    }
    
    private static long countQuads(DatasetGraph dsg) { return Iter.count(dsg.find()); }

    private static Graph data1()
    {
        return data(triple1) ;
    }
    
    private static Graph data2()
    {
        return data(triple2) ;
    }
    
    private static Graph data12()
    {
        return data(triple1, triple2) ;
    }
    
    private static Graph data(Triple... triples)
    {
        Graph graph = Factory.createDefaultGraph();
        for ( Triple t : triples )
            graph.add(t);
        return graph;
    }
}
