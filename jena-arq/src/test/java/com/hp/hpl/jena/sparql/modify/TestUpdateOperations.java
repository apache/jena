/**
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
import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.update.* ;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

// Most of the testing of SPARQL Update is scripts and uses the SPARQL-WG test suite.
// Here are a few additional tests
public class TestUpdateOperations extends BaseTest
{
    private static final String DIR = "testing/Update" ;
    private GraphStore graphStore() { return GraphStoreFactory.create() ; }
    private Node gName = SSE.parseNode("<http://example/g>") ;
    
    @Test public void load1() {
        GraphStore gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD <"+DIR+"/D.nt>") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(1, gs.getDefaultGraph().size()) ;
        assertFalse( gs.listGraphNodes().hasNext()) ;
    }

    @Test public void load2() {
        GraphStore gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD <"+DIR+"/D.nt> INTO GRAPH <"+gName.getURI()+">") ;
        UpdateAction.execute(req, gs) ;
    }
     
    // Quad loading

    @Test public void load3() {
        GraphStore gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD <"+DIR+"/D.nq>") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, gs.getDefaultGraph().size()) ;
        gs.containsGraph(NodeFactory.createURI("http://example/")) ;
        assertEquals(1, gs.getGraph(gName).size()) ;
    }

    // Bad: loading quads into a named graph
    @Test(expected=UpdateException.class)
    public void load4() {
        GraphStore gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD <"+DIR+"/D.nq> INTO GRAPH <"+gName.getURI()+">") ;
        UpdateAction.execute(req, gs) ;
    }

    @Test public void load5() {
        GraphStore gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD SILENT <"+DIR+"/D.nq> INTO GRAPH <"+gName.getURI()+">") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, Iter.count(gs.find())) ;
    }
    
    @Test public void insert_where_01() {
        Model m = ModelFactory.createDefaultModel();
        Resource anon = m.createResource();
        anon.addProperty(RDF.type, OWL.Thing);
        assertEquals(1, m.size());
        
        GraphStore gs = GraphStoreFactory.create(m);
        UpdateRequest req = UpdateFactory.create("INSERT { ?s ?p ?o } WHERE { ?o ?p ?s }");
        UpdateAction.execute(req, gs);
        
        assertEquals(2, m.size());
        assertEquals(1, m.listStatements(anon, null, (RDFNode)null).toList().size());
        assertEquals(1, m.listStatements(null, null, anon).toList().size());
    }
}

