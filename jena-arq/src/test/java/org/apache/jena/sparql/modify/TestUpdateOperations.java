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

package org.apache.jena.sparql.modify;

import static org.apache.jena.atlas.lib.StrUtils.strjoinNL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.DatasetGraphWrapper ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateException;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.OWL ;
import org.apache.jena.vocabulary.RDF ;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test ;

// Most of the testing of SPARQL Update is scripts and uses the SPARQL-WG test suite.
// Here are a few additional tests
public class TestUpdateOperations
{
    private static final String DIR = "testing/Update" ;
    private DatasetGraph graphStore() { return DatasetGraphFactory.create() ; }
    private Node gName = SSE.parseNode("<http://example/g>") ;

    private static ErrorHandler eh;

    // Silence parser output.
    @BeforeClass public static void beforeClass() {
        eh = ErrorHandlerFactory.getDefaultErrorHandler();
        ErrorHandler silent = ErrorHandlerFactory.errorHandlerStrictSilent();
        ErrorHandlerFactory.setDefaultErrorHandler(silent);
    }

    @AfterClass public static void afterClass() {
        ErrorHandlerFactory.setDefaultErrorHandler(eh);
    }

    @Test public void load1() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD <"+DIR+"/D.nt>") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(1, gs.getDefaultGraph().size()) ;
        assertFalse( gs.listGraphNodes().hasNext()) ;
    }

    @Test public void load2() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD <"+DIR+"/D.nt> INTO GRAPH <"+gName.getURI()+">") ;
        UpdateAction.execute(req, gs) ;
    }

    // Quad loading

    @Test public void load3() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD <"+DIR+"/D.nq>") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, gs.getDefaultGraph().size()) ;
        gs.containsGraph(NodeFactory.createURI("http://example/")) ;
        assertEquals(1, gs.getGraph(gName).size()) ;
    }

    // Bad: loading quads into a named graph
    @Test(expected=UpdateException.class)
    public void load4() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD <"+DIR+"/D.nq> INTO GRAPH <"+gName.getURI()+">") ;
        UpdateAction.execute(req, gs) ;
    }

    @Test public void load5() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD SILENT <"+DIR+"/D.nq> INTO GRAPH <"+gName.getURI()+">") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, Iter.count(gs.find())) ;
    }

    @Test(expected=UpdateException.class)
    public void load6() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD <"+DIR+"/D-bad.nq>") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, Iter.count(gs.find())) ;
    }

    @Test public void load7() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD SILENT <"+DIR+"/D-bad.nq>") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, Iter.count(gs.find())) ;
    }

    @Test(expected=UpdateException.class)
    public void load8() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD <"+DIR+"/D-bad.nt> INTO GRAPH <"+gName.getURI()+">") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, Iter.count(gs.find())) ;
    }

    @Test public void load9() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD SILENT <"+DIR+"/D-bad.nt> INTO GRAPH <"+gName.getURI()+">") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, Iter.count(gs.find())) ;
    }

    @Test(expected=UpdateException.class)
    public void load10() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD <"+DIR+"/D-quads.nt> INTO GRAPH <"+gName.getURI()+">") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, Iter.count(gs.find())) ;
    }

    @Test public void load11() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD SILENT <"+DIR+"/D-quads.nt> INTO GRAPH <"+gName.getURI()+">") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, Iter.count(gs.find())) ;
    }

    @Test(expected=UpdateException.class)
    public void load12() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD <"+DIR+"/D-not-found.nt>") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, Iter.count(gs.find())) ;
    }

    @Test public void load13() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD SILENT <"+DIR+"/D-not-found.nt>") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, Iter.count(gs.find())) ;
    }

    @Test(expected=UpdateException.class)
    public void load14() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD <"+DIR+"/D-not-found.nt> INTO GRAPH <"+gName.getURI()+">") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, Iter.count(gs.find())) ;
    }

    @Test public void load15() {
        DatasetGraph gs = graphStore() ;
        UpdateRequest req = UpdateFactory.create("LOAD SILENT <"+DIR+"/D-not-found.nt> INTO GRAPH <"+gName.getURI()+">") ;
        UpdateAction.execute(req, gs) ;
        assertEquals(0, Iter.count(gs.find())) ;
    }

    @Test public void insert_where_01() {
        Model m = ModelFactory.createDefaultModel();
        Resource anon = m.createResource();
        anon.addProperty(RDF.type, OWL.Thing);
        assertEquals(1, m.size());

        UpdateRequest req = UpdateFactory.create("INSERT { ?s ?p ?o } WHERE { ?o ?p ?s }");
        UpdateAction.execute(req, m);

        assertEquals(2, m.size());
        assertEquals(1, m.listStatements(anon, null, (RDFNode)null).toList().size());
        assertEquals(1, m.listStatements(null, null, anon).toList().size());
    }

    // Check constant and template quads
    @Test public void delete_insert_where_01() {
        DatasetGraph dsg0 = DatasetGraphFactory.create() ;
        UpdateRequest req = UpdateFactory.create("INSERT DATA { <x> <p> 2 . <z> <q> 2 . <z> <q> 3 . }") ;
        UpdateAction.execute(req, dsg0);
        assertEquals(3, dsg0.getDefaultGraph().size()) ;

        AtomicLong counterIns = new AtomicLong(0) ;
        AtomicLong counterDel = new AtomicLong(0) ;
        DatasetGraph dsg = new DatasetGraphWrapper(dsg0) {
            @Override
            public void add(Quad quad) {
                counterIns.incrementAndGet() ;
                super.add(quad) ;
            }

            @Override
            public void delete(Quad quad) {
                counterDel.incrementAndGet() ;
                super.delete(quad) ;
            }
        } ;

        // WHERE clause doubles the effect.
        String s = "DELETE { ?x <p> 2 . <z> <q> 2 } INSERT { ?x <p> 1 . <x> <q> 1  } WHERE { ?x <p> ?o {} UNION {} }" ;
        req = UpdateFactory.create(s) ;
        UpdateAction.execute(req, dsg);
        assertEquals(3, counterIns.get()) ;   // 3 : 1 constant, 2 from template.
        assertEquals(3, counterIns.get()) ;
        assertEquals(3, dsg.getDefaultGraph().size()) ;
    }

    // Ensure that the IRI function in UpdateRequests considers the BASE IRI - https://github.com/apache/jena/issues/1272
    @Test public void insert_with_iri_function_resolution_against_base_01() {
        Model m = ModelFactory.createDefaultModel();
        UpdateRequest req = UpdateFactory.create("BASE <http://www.example.org/> INSERT { ?s ?s ?s } WHERE { BIND(iri('s') AS ?s) }");
        UpdateAction.execute(req, m);
        List<Triple> triples = m.getGraph().find(null,null,null).toList();

        Node s = NodeFactory.createURI("http://www.example.org/s");
        Triple expected = Triple.create(s, s, s);
        assertTrue(triples.contains(expected));
        assertEquals(1, triples.size());
    }

    // Ensure that the IRI function in UpdateRequests considers the BASE IRI in syntactic scope.
    @Test public void insert_with_iri_function_resolution_against_base_02() {
        Model m = ModelFactory.createDefaultModel();
        String updateStr = strjoinNL
                ("BASE <http://www.example.org/base1/> INSERT { ?s ?s ?s } WHERE { BIND(iri('s') AS ?s) }"
                , ";"
                ,"BASE <http://www.example.org/base2/> INSERT { ?s ?s ?s } WHERE { BIND(iri('s') AS ?s) }"
                );
        UpdateRequest req = UpdateFactory.create(updateStr);
        UpdateAction.execute(req, m);

        List<Triple> triples = m.getGraph().find(null,null,null).toList();

        Node s1 = NodeFactory.createURI("http://www.example.org/base1/s");
        Node s2 = NodeFactory.createURI("http://www.example.org/base2/s");
        assertNotEquals("Bad test: different triples are equals", s1, s2);
        Triple expected1 = Triple.create(s1, s1, s1);
        Triple expected2 = Triple.create(s2, s2, s2);
        assertTrue(triples.contains(expected1));
        assertTrue(triples.contains(expected2));
        assertEquals(2, triples.size());
    }

    // ARQ extension. IRI(base, relative)
    private static void test2Arg(String updateStr, String expectedURI) {
        Model m = ModelFactory.createDefaultModel();
        // ARQ extension form.
        UpdateRequest req = UpdateFactory.create(updateStr);
        UpdateAction.execute(req, m);
        List<Triple> triples = m.getGraph().find(null,null,null).toList();
        Node x = NodeFactory.createURI(expectedURI);
        Triple expected = Triple.create(x, x, x);
        assertTrue(triples.contains(expected));
        assertEquals(1, triples.size());
    }

    @Test public void insert_with_iri_function_resolution_against_base_03() {
        test2Arg("BASE <http://www.example.org/> INSERT { ?s ?s ?s } WHERE { BIND(iri('http://example/', 's') AS ?s) }",
                 "http://example/s");
    }

    @Test public void insert_with_iri_function_resolution_against_base_04() {
        test2Arg("BASE <http://www.example.org/> INSERT { ?s ?s ?s } WHERE { BIND(iri(<http://example/>, 's') AS ?s) }",
                 "http://example/s");
    }

    @Test public void insert_with_iri_function_resolution_against_base_05() {
        test2Arg("BASE <http://www.example.org/> INSERT { ?s ?s ?s } WHERE { BIND(iri(<x1/x2/x3>, 's') AS ?s) }",
                  "http://www.example.org/x1/x2/s");
    }
}
