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

package org.apache.jena.query.text.changes;

import static org.apache.jena.query.text.changes.TextQuadAction.ADD;
import static org.apache.jena.query.text.changes.TextQuadAction.DELETE;
import static org.apache.jena.query.text.changes.TextQuadAction.NO_ADD;
import static org.apache.jena.query.text.changes.TextQuadAction.NO_DELETE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Test ;

public class TestDatasetMonitor
{
    static Quad quad1 = SSE.parseQuad("(_ <s> <p> 1)") ;
    static Quad quad2 = SSE.parseQuad("(<g> <s> <p> 2)") ;
    static Quad quad3 = SSE.parseQuad("(<g> <s> <p> 3)") ;
    static Quad quad4 = SSE.parseQuad("(<g> <s> <p> 4)") ;

    @Test public void countChanges_01() {
        DatasetGraph dsgBase = DatasetGraphFactory.create() ;
        ChangesCounter dsgChanges = new ChangesCounter() ;
        DatasetGraph dsg = new DatasetGraphTextMonitor(dsgBase, dsgChanges) ;

        check(dsgChanges, 0, 0, 0, 0) ;
        dsg.add(quad1) ;
        check(dsgChanges, 1, 0, 0, 0) ;
    }

    @Test public void countChanges_02() {
        DatasetGraph dsgBase = DatasetGraphFactory.create() ;
        ChangesCounter dsgChanges = new ChangesCounter() ;
        DatasetGraph dsg = new DatasetGraphTextMonitor(dsgBase, dsgChanges) ;

        check(dsgChanges, 0, 0, 0, 0) ;
        dsg.add(quad1) ;
        dsg.delete(quad1) ;
        check(dsgChanges, 1, 1, 0, 0) ;
    }

    @Test public void countChanges_03() {
        DatasetGraph dsgBase = DatasetGraphFactory.create() ;
        ChangesCounter dsgChanges = new ChangesCounter() ;
        DatasetGraph dsg = new DatasetGraphTextMonitor(dsgBase, dsgChanges) ;

        check(dsgChanges, 0, 0, 0, 0) ;
        dsg.add(quad1) ;
        dsg.add(quad1) ;
        dsg.delete(quad1) ;
        dsg.delete(quad1) ;
        check(dsgChanges, 1, 1, 1, 1) ;
    }

    @Test public void countChanges_04() {
        DatasetGraph dsgBase = DatasetGraphFactory.create() ;
        ChangesCounter dsgChanges = new ChangesCounter() ;
        DatasetGraph dsg = new DatasetGraphTextMonitor(dsgBase, dsgChanges) ;

        check(dsgChanges, 0, 0, 0, 0) ;
        dsg.add(quad1) ;
        dsg.delete(quad1) ;
        dsg.add(quad1) ;
        dsg.delete(quad1) ;
        check(dsgChanges, 2, 2, 0, 0) ;
    }

    @Test public void countChanges_05() {
        DatasetGraph dsgBase = DatasetGraphFactory.create() ;
        ChangesCounter dsgChanges = new ChangesCounter() ;
        DatasetGraph dsg = new DatasetGraphTextMonitor(dsgBase, dsgChanges) ;

        check(dsgChanges, 0, 0, 0, 0) ;
        Graph g = dsg.getDefaultGraph();
        g.add(quad1.asTriple()) ;
        g.delete(quad1.asTriple()) ;
        g.add(quad1.asTriple()) ;
        g.delete(quad1.asTriple()) ;
        check(dsgChanges, 2, 2, 0, 0) ;
    }

    @Test public void countChanges_06() {
        DatasetGraph dsgBase = DatasetGraphFactory.create() ;
        ChangesCounter dsgChanges = new ChangesCounter() ;
        DatasetGraph dsg = new DatasetGraphTextMonitor(dsgBase, dsgChanges) ;

        check(dsgChanges, 0, 0, 0, 0) ;
        Graph g = dsg.getGraph(NodeFactory.createURI("http://example.com/g1"));
        g.add(quad1.asTriple()) ;
        g.delete(quad1.asTriple()) ;
        g.add(quad1.asTriple()) ;
        g.delete(quad1.asTriple()) ;
        check(dsgChanges, 2, 2, 0, 0) ;
    }

    @Test public void countChanges_07() {
        DatasetGraph dsgBase = DatasetGraphFactory.create() ;
        ChangesCounter dsgChanges = new ChangesCounter() ;
        DatasetGraph dsg = new DatasetGraphTextMonitor(dsgBase, dsgChanges) ;

        check(dsgChanges, 0, 0, 0, 0) ;
        Graph g = dsg.getDefaultGraph();
        g.add(quad1.asTriple()) ;
        dsg.removeGraph(Quad.defaultGraphIRI);
        check(dsgChanges, 1, 1, 0, 0) ;
    }

    @Test public void countChanges_08() {
        DatasetGraph dsgBase = DatasetGraphFactory.create() ;
        ChangesCounter dsgChanges = new ChangesCounter() ;
        DatasetGraph dsg = new DatasetGraphTextMonitor(dsgBase, dsgChanges) ;

        check(dsgChanges, 0, 0, 0, 0) ;
        Graph g = dsg.getDefaultGraph();
        g.add(quad1.asTriple()) ;
        dsg.removeGraph(Quad.defaultGraphNodeGenerated);
        check(dsgChanges, 1, 1, 0, 0) ;
    }

    @Test public void captureChanges_01() {
        DatasetGraph dsgBase = DatasetGraphFactory.create() ;
        ChangesCapture dsgCapture = new ChangesCapture() ;
        DatasetGraph dsg = new DatasetGraphTextMonitor(dsgBase, dsgCapture) ;

        dsg.add(quad1) ;
        dsg.delete(quad1) ;
        dsg.add(quad2) ;
        dsg.add(quad2) ;

        List<Pair<TextQuadAction, Quad>> record = dsgCapture.getActions()  ;
        // Records only real actions.
        assertEquals(3, record.size()) ;
        check(record, 0, ADD, quad1) ;
        check(record, 1, DELETE, quad1) ;
        check(record, 2, ADD, quad2) ;
    }

    @Test public void captureChanges_02() {
        DatasetGraph dsgBase = DatasetGraphFactory.create() ;
        ChangesCapture dsgCapture = new ChangesCapture(true) ;
        DatasetGraph dsg = new DatasetGraphTextMonitor(dsgBase, dsgCapture) ;

        dsg.add(quad1) ;
        dsg.delete(quad1) ;
        dsg.delete(quad1) ;
        dsg.add(quad2) ;
        dsg.add(quad2) ;

        List<Pair<TextQuadAction, Quad>> record = dsgCapture.getActions()  ;
        assertEquals(5, record.size()) ;
        check(record, 0, ADD, quad1) ;
        check(record, 1, DELETE, quad1) ;
        check(record, 2, NO_DELETE, quad1) ;
        check(record, 3, ADD, quad2) ;
        check(record, 4, NO_ADD, quad2) ;
    }

    private static void check(ChangesCounter changes, long adds, long deletes, long noAdds, long noDeletes)
    {
        assertEquals("Adds",        adds, changes.countAdd) ;
        assertEquals("Deletes",     deletes, changes.countDelete) ;
        assertEquals("NoAdds",      noAdds, changes.countNoAdd) ;
        assertEquals("NoDeletes",   noDeletes, changes.countNoDelete) ;
    }

    private static void check(List<Pair<TextQuadAction, Quad>> record, int indx, TextQuadAction quadAction, Quad quad)
    {
        assertTrue("Index "+indx+" out of range [0,"+record.size()+")", 0 <= indx && indx < record.size() ) ;
        Pair<TextQuadAction, Quad> pair = record.get(indx) ;
        assertEquals(quadAction, pair.getLeft()) ;
        assertEquals(quad, pair.getRight()) ;
    }
}

