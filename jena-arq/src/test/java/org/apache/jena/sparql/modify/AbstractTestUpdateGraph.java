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

package org.apache.jena.sparql.modify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.graph.*;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.modify.request.*;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.update.UpdateAction;

public abstract class AbstractTestUpdateGraph extends AbstractTestUpdateBase
{
    protected static Node s = NodeFactoryExtra.parseNode("<http://example/r>");
    protected static Node p = NodeFactoryExtra.parseNode("<http://example/p>");
    protected static Node q = NodeFactoryExtra.parseNode("<http://example/q>");
    protected static Node v = NodeFactoryExtra.parseNode("<http://example/v>");

    protected static Node o1 = NodeFactoryExtra.parseNode("2007");
    protected static Triple triple1 = Triple.create(s, p, o1);
    protected static Node o2 = NodeFactoryExtra.parseNode("1066");
    protected static Triple triple2 = Triple.create(s, p, o2);
    protected static Graph graph1 = data1();
    protected static Node graphIRI = NodeFactoryExtra.parseNode("<http://example/graph>");

    @Test
    public void testInsertData1() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        defaultGraphData(gStore, graph1);
        QuadDataAcc acc = new QuadDataAcc();
        acc.addTriple(triple2);
        UpdateDataInsert insert = new UpdateDataInsert(acc);
        UpdateExec uProc = UpdateExec.dataset(gStore).update(insert).build();
        uProc.execute();

        assertFalse(graphEmpty(gStore.getDefaultGraph()));
        assertTrue(graphContains(gStore.getDefaultGraph(), triple1));
        assertTrue(graphContains(gStore.getDefaultGraph(), triple2));
    }

    @Test
    public void testDeleteData1() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        defaultGraphData(gStore, graph1);
        QuadDataAcc acc = new QuadDataAcc();
        acc.addTriple(triple2);
        UpdateDataDelete delete = new UpdateDataDelete(acc);
        UpdateExec uProc = UpdateExec.dataset(gStore).update(delete).build();
        uProc.execute();

        assertFalse(graphEmpty(gStore.getDefaultGraph()));
        assertTrue(graphContains(gStore.getDefaultGraph(), triple1));
        assertFalse(graphContains(gStore.getDefaultGraph(), triple2));
    }

    @Test
    public void testDeleteData2() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        defaultGraphData(gStore, graph1);
        QuadDataAcc acc = new QuadDataAcc();
        acc.addTriple(triple1);
        UpdateDataDelete delete = new UpdateDataDelete(acc);
        UpdateExec uProc = UpdateExec.dataset(gStore).update(delete).build();
        uProc.execute();

        assertTrue(graphEmpty(gStore.getDefaultGraph()));
        assertFalse(graphContains(gStore.getDefaultGraph(), triple1));
    }

    @Test
    public void testInsert1() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        UpdateModify insert = new UpdateModify();
        UpdateAction.execute(insert, gStore);
        assertTrue(graphEmpty(gStore.getDefaultGraph()));
    }

    @Test
    public void testInsert2() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        UpdateModify insert = new UpdateModify();
        insert.getInsertAcc().addTriple(triple1);
        UpdateAction.execute(insert, gStore);
        assertTrue(graphContains(gStore.getDefaultGraph(), triple1));
    }

    @Test
    public void testInsert3() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        gStore.addGraph(graphIRI, GraphMemFactory.createDefaultGraph());
        UpdateModify insert = new UpdateModify();
        insert.getInsertAcc().addQuad(new Quad(graphIRI, triple1));
        UpdateAction.execute(insert, gStore);
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1));
    }

    @Test
    public void testInsert4() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        defaultGraphData(gStore, graph1);
        UpdateModify insert = new UpdateModify();
        insert.getInsertAcc().addTriple(SSE.parseTriple("(?s <http://example/p> 1066)"));
        Element element = QueryFactory.createElement("{ ?s <http://example/p> 2007 }");
        insert.setElement(element);
        UpdateAction.execute(insert, gStore);
        assertTrue(graphContains(gStore.getDefaultGraph(), triple2));
    }

    @Test
    public void testDelete1() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        QuadAcc acc = new QuadAcc();
        UpdateDeleteWhere delete = new UpdateDeleteWhere(acc);
        UpdateAction.execute(delete, gStore);
        assertTrue(graphEmpty(gStore.getDefaultGraph()));
    }

    @Test
    public void testDelete2() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        defaultGraphData(gStore, graph1);
        QuadAcc acc = new QuadAcc();
        UpdateDeleteWhere delete = new UpdateDeleteWhere(acc);
        acc.addTriple(SSE.parseTriple("(?s ?p ?o)"));
        UpdateAction.execute(delete, gStore);
        assertTrue(graphEmpty(gStore.getDefaultGraph()), ()->"Not empty");
    }

    @Test
    public void testDelete3() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        defaultGraphData(gStore, graph1);
        QuadDataAcc acc = new QuadDataAcc();
        UpdateDataDelete delete = new UpdateDataDelete(acc);
        acc.addTriple(triple1);
        UpdateAction.execute(delete, gStore);
        assertTrue(graphEmpty(gStore.getDefaultGraph()));
    }

    @Test
    public void testDelete4() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        namedGraphData(gStore, graphIRI, data1());

        QuadDataAcc acc = new QuadDataAcc();
        UpdateDataDelete delete = new UpdateDataDelete(acc);
        acc.setGraph(graphIRI);
        acc.addTriple(triple1);
        UpdateAction.execute(delete, gStore);
        assertTrue(graphEmpty(gStore.getGraph(graphIRI)));
        assertTrue(graphEmpty(gStore.getDefaultGraph()));
    }

    @Test
    public void testDelete5() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        defaultGraphData(gStore, data2());
        namedGraphData(gStore, graphIRI, data1());

        UpdateModify modify = new UpdateModify();
        Element element = QueryFactory.createElement("{ ?s <http://example/p> ?o }");
        modify.setElement(element);
        modify.getDeleteAcc().addQuad(SSE.parseQuad("(<http://example/graph> ?s <http://example/p> 2007 )"));
        UpdateAction.execute(modify, gStore);

        assertTrue(graphEmpty(gStore.getGraph(graphIRI)), ()->"Not empty");
        assertFalse(graphEmpty(gStore.getDefaultGraph()));
    }

    @Test
    public void testModify1() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        defaultGraphData(gStore, data2());
        namedGraphData(gStore, graphIRI, GraphMemFactory.createDefaultGraph());

        UpdateModify modify = new UpdateModify();
        Element element = QueryFactory.createElement("{ ?s <http://example/p> ?o }");
        modify.setElement(element);
        modify.getInsertAcc().addQuad(new Quad(graphIRI, triple1));
        modify.getDeleteAcc().addTriple(SSE.parseTriple("(?s <http://example/p> ?o)"));
        modify.getDeleteAcc().addQuad(SSE.parseQuad("(<http://example/graph> ?s <http://example/p> ?o)"));
        UpdateAction.execute(modify, gStore);

        assertFalse(graphEmpty(gStore.getGraph(graphIRI)));
        assertTrue(graphEmpty(gStore.getDefaultGraph()));
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1));
    }

    @Test
    public void testModify2() {
        // Use blank nodes (will expose any problems in serialization when spill
        // occurs)
        Triple t = Triple.create(org.apache.jena.graph.NodeFactory.createBlankNode(), p, o2);

        DatasetGraph gStore = getEmptyDatasetGraph();
        // Set the threshold to in order to force spill to disk
        gStore.getContext().set(ARQ.spillToDiskThreshold, 0L);

        defaultGraphData(gStore, data(t));
        namedGraphData(gStore, graphIRI, data(t));

        UpdateModify modify = new UpdateModify();
        Element element = QueryFactory.createElement("{ ?s <http://example/p> ?o }");
        modify.setElement(element);
        modify.getInsertAcc().addQuad(new Quad(graphIRI, triple1));
        modify.getDeleteAcc().addTriple(SSE.parseTriple("(?s <http://example/p> ?o)"));
        modify.getDeleteAcc().addQuad(SSE.parseQuad("(<http://example/graph> ?s <http://example/p> ?o)"));
        UpdateAction.execute(modify, gStore);

        assertFalse(graphEmpty(gStore.getGraph(graphIRI)));
        assertTrue(graphEmpty(gStore.getDefaultGraph()));
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1));
        assertFalse(graphContains(gStore.getGraph(graphIRI), t));
    }

    @Test
    public void testModifyInitialBindings() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        defaultGraphData(gStore, data12());
        namedGraphData(gStore, graphIRI, GraphMemFactory.createDefaultGraph());

        Binding initialBinding = BindingFactory.binding(Var.alloc("o"), o1);

        UpdateModify modify = new UpdateModify();
        Element element = QueryFactory.createElement("{ ?s <http://example/p> ?o }");
        modify.setElement(element);

        modify.getInsertAcc().addQuad(new Quad(graphIRI, triple1));
        modify.getDeleteAcc().addTriple(SSE.parseTriple("(?s <http://example/p> ?o)"));
        modify.getDeleteAcc().addQuad(SSE.parseQuad("(<http://example/graph> ?s <http://example/p> ?o)"));
        UpdateAction.execute(modify, gStore, initialBinding);

        assertFalse(graphEmpty(gStore.getGraph(graphIRI)));
        assertFalse(graphEmpty(gStore.getDefaultGraph()));
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1));
        assertTrue(graphContains(gStore.getDefaultGraph(), triple2));
        assertFalse(graphContains(gStore.getDefaultGraph(), triple1));
    }

    @Test
    public void testCopy() {
        // Use blank nodes (will expose any problems in serialization when spill
        // occurs)
        Triple t = Triple.create(org.apache.jena.graph.NodeFactory.createBlankNode(), p, o2);
        Triple t2 = Triple.create(org.apache.jena.graph.NodeFactory.createBlankNode(), p, o1);

        DatasetGraph gStore = getEmptyDatasetGraph();
        // Set the threshold to in order to force spill to disk
        gStore.getContext().set(ARQ.spillToDiskThreshold, 0L);

        defaultGraphData(gStore, data(triple1, triple2, t));
        namedGraphData(gStore, graphIRI, data(t2));

        UpdateCopy copy = new UpdateCopy(Target.DEFAULT, Target.create(graphIRI));
        UpdateAction.execute(copy, gStore);

        assertFalse(graphEmpty(gStore.getDefaultGraph()));
        assertFalse(graphEmpty(gStore.getGraph(graphIRI)));
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple1));
        assertTrue(graphContains(gStore.getGraph(graphIRI), triple2));
        assertTrue(graphContains(gStore.getGraph(graphIRI), t));
        assertFalse(graphContains(gStore.getGraph(graphIRI), t2));
        assertTrue(gStore.getDefaultGraph().isIsomorphicWith(gStore.getGraph(graphIRI)));
    }

    @Test
    public void testUpdateInsertData() {
        DatasetGraph dsg = getEmptyDatasetGraph();
        String updateStr = "INSERT DATA { <x:s> <x:p> 'dft' }";
        byte[] bytes = Bytes.string2bytes(updateStr);
        UsingList usingList = new UsingList();
        InputStream in = new ByteArrayInputStream(bytes);
        UpdateAction.parseExecute(usingList, dsg, in);
        assertEquals(1, dsg.getDefaultGraph().size());
    }

    @Test
    public void testUpdateDeleteData() {
        DatasetGraph dsg = getEmptyDatasetGraph();
        dsg.add(SSE.parseQuad("(_ <x:s> <x:p> <x:o>)"));
        assertEquals(1, dsg.getDefaultGraph().size());

        String updateStr = "DELETE DATA { <x:s> <x:p> <x:o> }";
        byte[] bytes = Bytes.string2bytes(updateStr);
        UsingList usingList = new UsingList();
        InputStream in = new ByteArrayInputStream(bytes);
        UpdateAction.parseExecute(usingList, dsg, in);
        assertEquals(0, dsg.getDefaultGraph().size());
    }

    @Test
    public void testUpdateScript1() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        script(gStore, "update-1.ru");
        assertTrue(graphContains(gStore.getDefaultGraph(), Triple.create(s, p, NodeFactoryExtra.parseNode("123"))));
    }

    @Test
    public void testUpdateScript2() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        script(gStore, "update-2.ru");
        assertTrue(graphContains(gStore.getGraph(org.apache.jena.graph.NodeFactory.createURI("http://example/g1")),
                                 Triple.create(s, p, NodeFactoryExtra.parseNode("123"))));
        assertTrue(graphEmpty(gStore.getDefaultGraph()));
    }

    @Test
    public void testUpdateScript3() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        script(gStore, "update-3.ru");
        assertTrue(graphEmpty(gStore.getGraph(org.apache.jena.graph.NodeFactory.createURI("http://example/g1"))));
        assertTrue(graphEmpty(gStore.getDefaultGraph()));
    }

    @Test
    public void testUpdateScript4() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        script(gStore, "data-1.ru");
        assertTrue(graphContains(gStore.getDefaultGraph(), Triple.create(s, p, NodeFactoryExtra.parseNode("123"))));
    }

    @Test
    public void testUpdateScript5() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        script(gStore, "data-2.ru");

        Graph g = GraphFactory.createDefaultGraph();
        Node b = org.apache.jena.graph.NodeFactory.createBlankNode();

        g.add(Triple.create(s, p, b));
        g.add(Triple.create(b, q, v));
        assertTrue(g.isIsomorphicWith(gStore.getDefaultGraph()));
    }

    @Test
    public void testUpdateScript6() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        script(gStore, "data-3.ru");
        assertTrue(graphContains(gStore.getGraph(graphIRI), Triple.create(s, p, NodeFactoryExtra.parseNode("123"))));
    }

    @Test
    public void testUpdateScript7() {
        DatasetGraph gStore = getEmptyDatasetGraph();
        script(gStore, "data-4.ru");
        assertTrue(graphContains(gStore.getDefaultGraph(), Triple.create(s, p, NodeFactoryExtra.parseNode("123"))));
        Graph g = gStore.getGraph(graphIRI);
        assertTrue(graphContains(gStore.getGraph(graphIRI), Triple.create(s, p, o2)));
    }

    @Test
    public void testUpdateScript8() {
        Node gn = NodeFactory.createURI("http://example/g");
        Node testNode = NodeFactory.createURI("http://example/test");
        Node result = NodeFactory.createURI("http://example/result");

        DatasetGraph gStore = getEmptyDatasetGraph();
        script(gStore, "data-5-with.ru");

        assertTrue(gStore.containsGraph(gn));
        Graph g = gStore.getGraph(gn);
        assertEquals(2, g.size());
        assertEquals(0, gStore.getDefaultGraph().size());
    }

    @Test
    public void testUpdateBad1() {
        assertThrows(QueryException.class, ()-> testBad("bad-1.ru", 1));
    }

    @Test
    public void testUpdateBad2() {
        testBad("bad-2.ru", 1);
    }

    @Test
    public void testUpdateBad3() {
        testBad("bad-3.ru", 0);
    }

    private void testBad(String file, int expectedSize) {
        // Bad queries may throw an exception or do a
        // partial update (e.g. a template generates literals as subjects).
        DatasetGraph gStore = getEmptyDatasetGraph();
        script(gStore, file);
        assertEquals(expectedSize, countQuads(gStore));
    }

    private static long countQuads(DatasetGraph dsg) {
        return Iter.count(dsg.find());
    }

    private static Graph data1() {
        return data(triple1);
    }

    private static Graph data2() {
        return data(triple2);
    }

    private static Graph data12() {
        return data(triple1, triple2);
    }

    private static Graph data(Triple...triples) {
        Graph graph = GraphMemFactory.createDefaultGraph();
        for ( Triple t : triples )
            graph.add(t);
        return graph;
    }
}
