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

package org.apache.jena.rdfpatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Check one dataset tracks another. */
public class TestRDFChangesDataset  {
    DatasetGraph dsgBase = DatasetGraphFactory.createTxnMem();
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    DatasetGraph dsg = RDFPatchOps.textWriter(dsgBase, bout);

    @Before public void beforeTest() {}
    @After public void afterTest() {}

    private static Quad quad1 = SSE.parseQuad("(:g _:s <p> 1)");
    private static Quad quad2 = SSE.parseQuad("(:g _:s <p> 2)");

    private static Triple triple1 = SSE.parseTriple("(_:sx <p1> 11)");
    private static Triple triple2 = SSE.parseTriple("(_:sx <p2> 22)");

    private DatasetGraph replay() {
        IO.close(bout);
        try(ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray())) {
            DatasetGraph dsg2 = DatasetGraphFactory.createTxnMem();
            RDFPatchOps.applyChange(dsg2, bin);
            return dsg2;
        } catch (IOException ex) { IO.exception(ex); return null; }
    }

    private static void check(DatasetGraph dsg, Quad...quads) {
        if ( quads.length == 0 ) {
            assertTrue(dsg.isEmpty());
            return;
        }

        List<Quad> listExpected = Arrays.asList(quads);
        List<Quad> listActual = Iter.toList(dsg.find());
        assertEquals(listActual.size(), listExpected.size());
        assertTrue(ListUtils.equalsUnordered(listExpected, listActual));
    }

    @Test public void record_00() {
        DatasetGraph dsg2 = replay();
        check(dsg2);
    }

    @Test public void record_add() {
        Txn.executeWrite(dsg, ()->dsg.add(quad1));
        DatasetGraph dsg2 = replay();
        check(dsg2, quad1);
    }

    @Test public void record_add_add_1() {
        Txn.executeWrite(dsg, ()-> {
            dsg.add(quad1);
            dsg.add(quad2);
        });
        DatasetGraph dsg2 = replay();
        check(dsg2, quad1, quad2);
    }

    @Test public void record_add_add_2() {
        Txn.executeWrite(dsg, ()-> {
            dsg.add(quad1);
            dsg.add(quad1);
        });
        DatasetGraph dsg2 = replay();
        check(dsg2, quad1);
    }

    @Test public void record_add_delete_1() {
        Txn.executeWrite(dsg, ()-> {
            dsg.add(quad1);
            dsg.delete(quad1);
        });
        DatasetGraph dsg2 = replay();
        check(dsg2);
    }

    @Test public void record_add_delete_2() {
        Txn.executeWrite(dsg, ()-> {
            dsg.add(quad1);
            dsg.delete(quad2);
        });
        DatasetGraph dsg2 = replay();
        check(dsg2, quad1);
    }

    @Test public void record_add_delete_3() {
        Txn.executeWrite(dsg, ()-> {
            dsg.delete(quad2);
            dsg.add(quad1);
        });
        DatasetGraph dsg2 = replay();
        check(dsg2, quad1);
    }

    @Test public void record_add_delete_4() {
        Txn.executeWrite(dsg, ()-> {
            dsg.delete(quad1);
            dsg.add(quad1);
        });
        DatasetGraph dsg2 = replay();
        check(dsg2, quad1);
    }

    @Test public void record_add_abort_1() {
        Txn.executeWrite(dsg, ()-> {
            dsg.delete(quad1);
            dsg.abort();
        });
        DatasetGraph dsg2 = replay();
        check(dsg2);
    }

    @Test public void record_graph_1() {
        Txn.executeWrite(dsg, ()-> {
            Graph g = dsg.getDefaultGraph();
            g.add(triple1);
        });
        DatasetGraph dsg2 = replay();
        check(dsg2, Quad.create(Quad.defaultGraphIRI, triple1));
    }

    @Test public void record_graph_2() {
        Txn.executeWrite(dsg, ()-> {
            Graph g = dsg.getDefaultGraph();
            g.add(triple1);
            g.delete(triple1);
            g.add(triple2);
        });
        DatasetGraph dsg2 = replay();
        check(dsg2, Quad.create(Quad.defaultGraphIRI, triple2));
    }

    @Test public void record_graph_3() {
        Txn.executeWrite(dsg, ()-> {
            Graph g = dsg.getDefaultGraph();
            g.add(triple1);
            dsg.delete(Quad.create(Quad.defaultGraphIRI, triple1));
        });
        DatasetGraph dsg2 = replay();
        check(dsg2);
    }

    @Test public void record_remove_graph_1() {
        Txn.executeWrite(dsg, ()->dsg.add(quad1));
        Node gn = quad1.getGraph();

        Txn.executeWrite(dsg, ()-> {
            dsg.removeGraph(gn);
        });

        DatasetGraph dsg2 = replay();
        check(dsg2);
    }

    @Test public void record_add_graph_1() {
        Txn.executeWrite(dsg, ()->dsg.add(quad1));

        Graph data = GraphFactory.createDefaultGraph();
        Node gn = quad1.getGraph();
        data.add(triple1);
        Txn.executeWrite(dsg, ()-> {
            dsg.addGraph(gn, data);
        });
        DatasetGraph dsg2 = replay();
        check(dsg2, Quad.create(gn, triple1));
    }
}
