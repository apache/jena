/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.test.suites;


import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.modify.op.UpdateDelete;
import com.hp.hpl.jena.sparql.modify.op.UpdateInsert;
import com.hp.hpl.jena.sparql.modify.op.UpdateModify;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.syntax.TemplateGroup;
import com.hp.hpl.jena.sparql.syntax.TemplateTriple;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;

import com.hp.hpl.jena.query.QueryFactory;

public class TestUpdateGraph extends TestUpdateBase
{
    protected static Node s = NodeFactory.create("<http://example/r>") ;
    protected static Node p = NodeFactory.create("<http://example/p>") ;
    protected static Node o1 = NodeFactory.create("2007") ;
    protected static Triple triple1 =  new Triple(s,p,o1) ;
    protected static Node o2 = NodeFactory.create("1066") ;
    protected static Triple triple2 =  new Triple(s,p,o2) ;
    protected static Graph graph1 = data1() ;
    protected static Node graphIRI = NodeFactory.create("<http://example/graph>") ;
    
    public void testInsert1()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        UpdateInsert insert = new UpdateInsert() ;
        insert.setInsertTemplate(new TemplateGroup()) ;
        insert.exec(gStore) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    public void testInsert2()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        UpdateInsert insert = new UpdateInsert() ;
        insert.setInsertTemplate(new TemplateTriple(triple1)) ;
        insert.exec(gStore) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple1)) ;
    }
    
    public void testInsert3()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        UpdateInsert insert = new UpdateInsert(graph1) ;
        insert.exec(gStore) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple1)) ;
    }
    
    public void testInsert4()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        gStore.addGraph(graphIRI, Factory.createDefaultGraph()) ;
        UpdateInsert insert = new UpdateInsert(triple1) ;
        insert.addGraphName(graphIRI) ;
        insert.exec(gStore) ;
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1)) ;
    }

    public void testInsert5()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        gStore.setDefaultGraph(graph1) ;
        Element element = QueryFactory.createElement("{ ?s <http://example/p> 2007 }" ) ;
        Template template = QueryFactory.createTemplate("{ ?s <http://example/p> 1066 }" ) ;
        UpdateInsert insert = new UpdateInsert() ;
        
        insert.setPattern(element) ;
        insert.setInsertTemplate(template) ;
        
        insert.exec(gStore) ;
        assertTrue(graphContains(gStore.getDefaultGraph(), triple2)) ;
    }
    
    public void testDelete1()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        UpdateDelete insert = new UpdateDelete() ;
        insert.setDeleteTemplate(new TemplateGroup()) ;
        insert.exec(gStore) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    public void testDelete2()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        gStore.setDefaultGraph(data1()) ;
        UpdateDelete delete = new UpdateDelete() ;
        delete.setDeleteTemplate(new TemplateGroup()) ;
        delete.exec(gStore) ;
        assertFalse(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    public void testDelete3()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        gStore.setDefaultGraph(data1()) ;
        UpdateDelete delete = new UpdateDelete(triple1) ;
        delete.exec(gStore) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    
    public void testDelete4()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        gStore.addGraph(graphIRI, data1()) ;
        UpdateDelete delete = new UpdateDelete(triple1) ;
        delete.addGraphName(graphIRI) ;
        delete.exec(gStore) ;
        assertTrue(graphEmpty(gStore.getGraph(graphIRI))) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    public void testDelete5()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        gStore.setDefaultGraph(data2()) ;
        gStore.addGraph(graphIRI, data1()) ;
        
        UpdateDelete delete = new UpdateDelete() ;
        delete.setPattern("{ ?s <http://example/p> ?o } ") ;
        delete.setDeleteTemplate("{ ?s <http://example/p> 2007 }") ;
        
        delete.addGraphName(graphIRI) ;
        delete.exec(gStore) ;

        assertTrue(graphEmpty(gStore.getGraph(graphIRI))) ;
        assertFalse(graphEmpty(gStore.getDefaultGraph())) ;
    }
    
    public void testModify1()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        gStore.setDefaultGraph(data2()) ;
        gStore.addGraph(graphIRI, Factory.createDefaultGraph()) ;
        UpdateModify modify = new UpdateModify() ;
        modify.addGraphName(graphIRI) ;
        modify.setPattern("{ ?s <http://example/p> ?o } ") ;
        modify.setDeleteTemplate("{ ?s <http://example/p> ?o}") ;
        modify.setInsertTemplate(new TemplateTriple(triple1)) ;
        modify.exec(gStore) ;
        assertFalse(graphEmpty(gStore.getGraph(graphIRI))) ;
        assertFalse(graphEmpty(gStore.getDefaultGraph())) ;
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1)) ;
    }
    
    public void testUpdateScript1()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        script(gStore, "update-1.rup") ;
        assertTrue(graphContains(gStore.getDefaultGraph(), new Triple(s,p,NodeFactory.create("123")))) ;
    }
    
    public void testUpdateScript2()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        script(gStore, "update-2.rup") ;
        assertTrue(graphContains(gStore.getGraph(Node.createURI("http://example/g1")),
                                 new Triple(s,p,NodeFactory.create("123")))) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
    }

    public void testUpdateScript3()
    {
        GraphStore gStore = GraphStoreFactory.create() ;
        script(gStore, "update-3.rup") ;
        assertTrue(graphEmpty(gStore.getGraph(Node.createURI("http://example/g1")))) ;
        assertTrue(graphEmpty(gStore.getDefaultGraph())) ;
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
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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