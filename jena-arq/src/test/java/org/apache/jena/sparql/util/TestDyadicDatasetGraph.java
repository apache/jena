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

package org.apache.jena.sparql.util;

import static org.apache.jena.sparql.core.DatasetGraphFactory.createTxnMem;

import org.apache.jena.atlas.junit.BaseTest;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphZero;
import org.junit.Test;

public abstract class TestDyadicDatasetGraph extends BaseTest {

    public abstract DatasetGraph testInstance(DatasetGraph left, DatasetGraph right, Context c);

    private DatasetGraph emptyDsg() {
        return testInstance(createTxnMem(), createTxnMem(), Context.emptyContext);
    }

    @Test(expected = NullPointerException.class)
    public void nullDatasetGraphsNotAllowed() {
        testInstance(null, null, Context.emptyContext);
    }

    @Test(expected = NullPointerException.class)
    public void nullContextNotAllowed() {
        testInstance(new DatasetGraphZero(), new DatasetGraphZero(), null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noAddingQuads() {
        emptyDsg().add(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noAddingQuads2() {
        emptyDsg().add(null, null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noDeletingQuads() {
        emptyDsg().delete(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noDeletingQuads2() {
        emptyDsg().delete(null, null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noDeletingAnyQuads() {
        emptyDsg().deleteAny(null, null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noAddingGraphs() {
        emptyDsg().addGraph(null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noDeletingGraphs() {
        emptyDsg().removeGraph(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noClearing() {
        emptyDsg().clear();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void noAddingToDefaultGraph() {
        emptyDsg().getDefaultGraph().add(null);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void noDeletingFromDefaultGraph() {
        emptyDsg().getDefaultGraph().delete(null);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void noAddingToANamedGraph() {
        Node graphName = NodeFactory.createBlankNode();
        emptyDsg().getGraph(graphName).add(null);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void noDeletingFromANamedGraph() {
        Node graphName = NodeFactory.createBlankNode();
        emptyDsg().getGraph(graphName).delete(null);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void noClearingDefaultGraph() {
        emptyDsg().getDefaultGraph().clear();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void noClearingANamedGraph() {
        Node graphName = NodeFactory.createBlankNode();
        emptyDsg().getGraph(graphName).clear();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void noRemovingFromANamedGraph() {
        Node graphName = NodeFactory.createBlankNode();
        emptyDsg().getGraph(graphName).remove(null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noWriting1() {
        emptyDsg().begin(ReadWrite.WRITE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noWriting2() {
        emptyDsg().begin(TxnType.WRITE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noWriting3() {
        emptyDsg().begin(TxnType.READ_PROMOTE);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noWriting4() {
        emptyDsg().begin(TxnType.READ_COMMITTED_PROMOTE);
    }
    
    @Test
    public void noPromoting() {
        assertFalse(emptyDsg().promote());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void noCommitting() {
        final DatasetGraph dsg = emptyDsg();
        assertFalse(dsg.isInTransaction());
        dsg.begin(ReadWrite.READ);
        assertTrue(dsg.isInTransaction());
        dsg.commit();
    }
    
    @Test
    public void testTransactionTypeAndMode() {
        final DatasetGraph dsg = emptyDsg();
        assertFalse(dsg.isInTransaction());
        try {
            dsg.begin(TxnType.READ);
            assertTrue(dsg.isInTransaction());
            assertEquals(TxnType.READ, dsg.transactionType());
            assertEquals(ReadWrite.READ, dsg.transactionMode());
        } finally {
            dsg.end();
        }
        assertFalse(dsg.isInTransaction());
    }

    @Test
    public void canUseEndToFinishTransaction1() {
        DatasetGraph dsg = emptyDsg();
        assertFalse(dsg.isInTransaction());
        try {
            dsg.begin(ReadWrite.READ);
            assertTrue(dsg.isInTransaction());
            dsg.end();
            assertFalse(dsg.isInTransaction());
        } catch (UnsupportedOperationException e) {
            fail();
        }
    }

    @Test
    public void canUseEndToFinishTransaction2() {
        DatasetGraph dsg = emptyDsg();
        assertFalse(dsg.isInTransaction());
        try {
            dsg.begin(TxnType.READ);
            assertTrue(dsg.isInTransaction());
            dsg.end();
            assertFalse(dsg.isInTransaction());
        } catch (UnsupportedOperationException e) {
            fail();
        }
    }

    @Test
    public void canUseAbortToFinishTransaction1() {
        DatasetGraph dsg = emptyDsg();
        assertFalse(dsg.isInTransaction());
        try {
            dsg.begin(ReadWrite.READ);
            assertTrue(dsg.isInTransaction());
            dsg.abort();
            assertFalse(dsg.isInTransaction());
        } catch (UnsupportedOperationException e) {
            fail();
        }
    }

    @Test
    public void canUseAbortToFinishTransaction2() {
        DatasetGraph dsg = emptyDsg();
        assertFalse(dsg.isInTransaction());
        try {
            dsg.begin(TxnType.READ);
            assertTrue(dsg.isInTransaction());
            dsg.abort();
            assertFalse(dsg.isInTransaction());
        } catch (UnsupportedOperationException e) {
            fail();
        }
    }
}
