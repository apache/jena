/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites;


import org.junit.Test ;

import com.hp.hpl.jena.graph.Factory ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.Binding1 ;
import com.hp.hpl.jena.sparql.modify.op.UpdateDelete ;
import com.hp.hpl.jena.sparql.modify.op.UpdateDeleteData ;
import com.hp.hpl.jena.sparql.modify.op.UpdateInsert ;
import com.hp.hpl.jena.sparql.modify.op.UpdateInsertData ;
import com.hp.hpl.jena.sparql.modify.op.UpdateModify ;
import com.hp.hpl.jena.sparql.syntax.Element ;
import com.hp.hpl.jena.sparql.syntax.Template ;
import com.hp.hpl.jena.sparql.syntax.TemplateGroup ;
import com.hp.hpl.jena.sparql.syntax.TemplateTriple ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateProcessor ;
import com.hp.hpl.jena.update.UpdateRequest ;

public abstract class TestUpdateGraph extends TestUpdateBase
{
    protected static Node s = NodeFactory.parseNode("<http://example/r>") ;
    protected static Node p = NodeFactory.parseNode("<http://example/p>") ;
    protected static Node q = NodeFactory.parseNode("<http://example/q>") ;
    protected static Node v = NodeFactory.parseNode("<http://example/v>") ;
    
    protected static Node o1 = NodeFactory.parseNode("2007") ;
    protected static Triple triple1 =  new Triple(s,p,o1) ;
    protected static Node o2 = NodeFactory.parseNode("1066") ;
    protected static Triple triple2 =  new Triple(s,p,o2) ;
    protected static Graph graph1 = data1() ;
    protected static Node graphIRI = NodeFactory.parseNode("<http://example/graph>") ;
    
    @Test public void testInsertData1()
    {
		GraphStore gStore = getEmptyGraphStore() ;
		defaultGraphData(gStore, graph1) ;
        UpdateInsertData insert = new UpdateInsertData() ;
        insert.setData(data2()) ;
        UpdateProcessor uProc = UpdateFactory.create(insert, gStore) ;
        uProc.execute(); 
        
        assertFalse(graphEmpty(gStore.getDefaultGraph())) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple1)) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple2)) ;
    }
    
    @Test public void testDeleteData1()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, graph1) ;
        UpdateDeleteData delete = new UpdateDeleteData() ;
        delete.setData(data2()) ;
        UpdateProcessor uProc = UpdateFactory.create(delete, gStore) ;
        uProc.execute(); 
        
        assertFalse(graphEmpty(gStore.getDefaultGraph())) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple1)) ;
        assertFalse(graphContains(gStore.getDefaultGraph(), triple2)) ;
    }
    
    @Test public void testDeleteData2()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, graph1) ;
        UpdateDeleteData delete = new UpdateDeleteData() ;
        delete.setData(data1()) ;
        UpdateProcessor uProc = UpdateFactory.create(delete, gStore) ;
        uProc.execute(); 
        
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
        assertFalse(graphContains(gStore.getDefaultGraph(), triple1)) ;
    }
    
    @Test public void testInsert1()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        UpdateInsert insert = new UpdateInsert() ;
        insert.setInsertTemplate(new TemplateGroup()) ;
        UpdateAction.execute(insert, gStore) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    @Test public void testInsert2()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        UpdateInsert insert = new UpdateInsert() ;
        insert.setInsertTemplate(new TemplateTriple(triple1)) ;
        UpdateAction.execute(insert, gStore) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple1)) ;
    }
    
    @Test public void testInsert3()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        UpdateInsert insert = new UpdateInsert(graph1) ;
        UpdateAction.execute(insert, gStore) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple1)) ;
    }
    
    @Test public void testInsert4()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        gStore.addGraph(graphIRI, Factory.createDefaultGraph()) ;
        UpdateInsert insert = new UpdateInsert(triple1) ;
        insert.addGraphName(graphIRI) ;
        UpdateAction.execute(insert, gStore) ;
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1)) ;
    }

    @Test public void testInsert5()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, graph1) ;
        Element element = QueryFactory.createElement("{ ?s <http://example/p> 2007 }" ) ;
        Template template = QueryFactory.createTemplate("{ ?s <http://example/p> 1066 }" ) ;
        UpdateInsert insert = new UpdateInsert() ;
        
        insert.setPattern(element) ;
        insert.setInsertTemplate(template) ;
        
        UpdateAction.execute(insert, gStore) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple2)) ;
    }
    
    @Test public void testDelete1()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        UpdateDelete insert = new UpdateDelete() ;
        insert.setDeleteTemplate(new TemplateGroup()) ;
        UpdateAction.execute(insert, gStore) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    @Test public void testDelete2()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, graph1) ;
        UpdateDelete delete = new UpdateDelete() ;
        delete.setDeleteTemplate(new TemplateGroup()) ;
        UpdateAction.execute(delete, gStore) ;
        assertFalse(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    @Test public void testDelete3()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, graph1) ;
        UpdateDelete delete = new UpdateDelete(triple1) ;
        UpdateAction.execute(delete, gStore) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    
    @Test public void testDelete4()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        namedGraphData(gStore, graphIRI, data1()) ;
        UpdateDelete delete = new UpdateDelete(triple1) ;
        delete.addGraphName(graphIRI) ;
        UpdateAction.execute(delete, gStore) ;
        assertTrue(graphEmpty(gStore.getGraph(graphIRI))) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    @Test public void testDelete5()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, data2()) ;
        namedGraphData(gStore, graphIRI, data1()) ;
        
        UpdateDelete delete = new UpdateDelete() ;
        delete.setPattern("{ ?s <http://example/p> ?o } ") ;
        delete.setDeleteTemplate("{ ?s <http://example/p> 2007 }") ;
        
        delete.addGraphName(graphIRI) ;
        UpdateAction.execute(delete, gStore) ;

        assertTrue(graphEmpty(gStore.getGraph(graphIRI))) ;
        assertFalse(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    @Test public void testModify1()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        defaultGraphData(gStore, data2()) ;
        namedGraphData(gStore, graphIRI, Factory.createDefaultGraph()) ;
        UpdateModify modify = new UpdateModify() ;
        modify.addGraphName(graphIRI) ;
        modify.setPattern("{ ?s <http://example/p> ?o } ") ;
        modify.setDeleteTemplate("{ ?s <http://example/p> ?o}") ;
        modify.setInsertTemplate(new TemplateTriple(triple1)) ;
        UpdateAction.execute(modify, gStore) ;
        assertFalse(graphEmpty(gStore.getGraph(graphIRI))) ;
        assertFalse(graphEmpty(gStore.getDefaultGraph())) ;
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1)) ;
    }
    
    @Test public void testUpdateScript1()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "update-1.rup") ;
        assertTrue(graphContains(gStore.getDefaultGraph(), new Triple(s,p,NodeFactory.parseNode("123")))) ;
    }
    
    @Test public void testUpdateScript2()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "update-2.rup") ;
        assertTrue(graphContains(gStore.getGraph(Node.createURI("http://example/g1")),
                                 new Triple(s,p,NodeFactory.parseNode("123")))) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }

    @Test public void testUpdateScript3()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "update-3.rup") ;
        assertTrue(graphEmpty(gStore.getGraph(Node.createURI("http://example/g1")))) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }

    @Test public void testUpdateScript4()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "data-1.rup") ;
        assertTrue(graphContains(gStore.getDefaultGraph(),
                                 new Triple(s,p,NodeFactory.parseNode("123")))) ;
    }
    
    @Test public void testUpdateScript5()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "data-2.rup") ;
        
        
        Graph g = GraphUtils.makePlainGraph() ;
        Node b = Node.createAnon() ;
        
        g.add(new Triple(s, p, b)) ;
        g.add(new Triple(b, q, v)) ;
        assertTrue(g.isIsomorphicWith(gStore.getDefaultGraph())) ;
    }
    
    @Test public void testUpdateScript6()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "data-3.rup") ;
        assertTrue(graphContains(gStore.getGraph(graphIRI),
                                 new Triple(s,p,NodeFactory.parseNode("123")))) ;
    }
    
    @Test public void testUpdateScript7()
    {
        GraphStore gStore = getEmptyGraphStore() ;
        script(gStore, "data-4.rup") ;
        assertTrue(graphContains(gStore.getDefaultGraph(),
                                 new Triple(s,p,NodeFactory.parseNode("123")))) ;
        Graph g = gStore.getGraph(graphIRI) ;
        assertTrue(graphContains(gStore.getGraph(graphIRI),
                                 new Triple(s,p,o2))) ;
    }
    
    
    private Graph testUpdateInitialBindingWorker(Var v, Node n)
    {
        GraphStore gStore = getEmptyGraphStore() ;
        UpdateRequest req = UpdateFactory.create() ;

        UpdateInsert ins = new UpdateInsert() ;
        TemplateGroup template = new TemplateGroup() ;
        template.addTriple(triple1) ;
        template.addTriple(triple2) ;
        ins.setInsertTemplate(template) ;
        req.addUpdate(ins) ;

        UpdateDelete delete = new UpdateDelete() ;
        delete.setPattern("{ ?s <http://example/p> ?o } ") ;
        delete.setDeleteTemplate("{ ?s <http://example/p> ?o}") ;
        req.addUpdate(delete) ;
        
        Binding b = new Binding1(null, v, n) ;
        UpdateAction.execute(req, gStore, b) ;
        
        return gStore.getDefaultGraph() ;
    }
    
    @Test public void testUpdateInitialBinding1()
    {
        Graph graph = testUpdateInitialBindingWorker(Var.alloc("o"), o1) ;
        assertEquals(graph.size(), 1) ;
        assertFalse(graphContains(graph, triple1)) ;
        assertTrue(graphContains(graph, triple2)) ;
    }
    
    @Test public void testUpdateInitialBinding2()
    {
        Graph graph = testUpdateInitialBindingWorker(Var.alloc("o"), o2) ;
        assertEquals(graph.size(), 1) ;
        assertTrue(graphContains(graph, triple1)) ;
        assertFalse(graphContains(graph, triple2)) ;
    }

    @Test public void testUpdateInitialBinding3()
    {
        // Does not affect the delete
        Graph graph = testUpdateInitialBindingWorker(Var.alloc("FF"), o1) ;
        assertTrue(graphEmpty(graph)) ;
    }
    
    private static Graph data1()
    {
        Graph graph = Factory.createDefaultGraph() ;
        graph.add(triple1) ;
        return graph ; 
    }
    
    private static Graph data2()
    {
        Graph graph = Factory.createDefaultGraph() ;
        graph.add(triple2) ;
        return graph ; 
    }

 
  
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
