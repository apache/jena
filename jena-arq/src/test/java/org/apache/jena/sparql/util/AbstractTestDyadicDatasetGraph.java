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
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphZero;

public abstract class AbstractTestDyadicDatasetGraph {

    public abstract DatasetGraph testInstance(DatasetGraph left, DatasetGraph right, Context c);

    private DatasetGraph emptyDsg() {
        return testInstance(createTxnMem(), createTxnMem(), Context.emptyContext());
    }

    @Test
    public void nullDatasetGraphsNotAllowed() {
		assertThrows(NullPointerException.class,
					 ()-> testInstance(null, null, Context.emptyContext()));
    }

    @Test
    public void nullContextNotAllowed() {
		assertThrows(NullPointerException.class,
					 ()-> testInstance(DatasetGraphZero.create(), DatasetGraphZero.create(), null));
    }

    @Test
    public void noAddingQuads() {
		assertThrows(UnsupportedOperationException.class,
					 ()-> emptyDsg().add(null));
    }

    @Test
    public void noAddingQuads2() {
		assertThrows(UnsupportedOperationException.class,
					 ()-> emptyDsg().add(null, null, null, null));
    }

    @Test
    public void noDeletingQuads() {
		assertThrows(UnsupportedOperationException.class,
					 ()-> emptyDsg().delete(null));
    }

    @Test
    public void noDeletingQuads2() {
		assertThrows(UnsupportedOperationException.class,
					 ()-> emptyDsg().delete(null, null, null, null));
    }

    @Test
    public void noDeletingAnyQuads() {
		assertThrows(UnsupportedOperationException.class,
					 ()-> emptyDsg().deleteAny(null, null, null, null));
    }

    @Test
    public void noAddingGraphs() {
		assertThrows(UnsupportedOperationException.class,
					 ()-> emptyDsg().addGraph(null, null));
    }

    @Test
    public void noDeletingGraphs() {
		assertThrows(UnsupportedOperationException.class,
					 ()-> emptyDsg().removeGraph(null));
    }

    @Test
    public void noClearing() {
		assertThrows(UnsupportedOperationException.class,
					 ()-> emptyDsg().clear());
    }

    @Test
    public void noAddingToDefaultGraph() {
		assertThrows(UnsupportedOperationException.class,
					 ()-> emptyDsg().getDefaultGraph().add(null));
    }

    @Test
    public void noDeletingFromDefaultGraph() {
		assertThrows(UnsupportedOperationException.class,
					 ()-> emptyDsg().getDefaultGraph().delete(null));
    }

    @Test
    public void noAddingToANamedGraph() {
        Node graphName = NodeFactory.createBlankNode();
        assertThrows(UnsupportedOperationException.class,
					 ()-> emptyDsg().getGraph(graphName).add(null));
    }

    @Test
    public void noDeletingFromANamedGraph() {
		Node graphName = NodeFactory.createBlankNode();
		assertThrows(UnsupportedOperationException.class,
					 ()->   emptyDsg().getGraph(graphName).delete(null));
    }

    @Test
    public void noClearingDefaultGraph() {
		assertThrows(UnsupportedOperationException.class,
					 ()-> emptyDsg().getDefaultGraph().clear());
    }

    @Test
    public void noClearingNamedGraph() {
        Node graphName = NodeFactory.createBlankNode();
        assertThrows(UnsupportedOperationException.class, ()->
            emptyDsg().getGraph(graphName).clear());
    }

    @Test
    public void noRemovingFromNamedGraph() {
        Node graphName = NodeFactory.createBlankNode();
        assertThrows(UnsupportedOperationException.class, ()->
            emptyDsg().getGraph(graphName)
            .remove(null, null, null));
    }

    // Read lifecycle.
    @Test
    public void txnRead1() {
        final DatasetGraph dsg = emptyDsg();
        assertFalse(dsg.isInTransaction());
        dsg.begin(ReadWrite.READ);
        assertTrue(dsg.isInTransaction());
        dsg.commit();
        dsg.end();
    }

    @Test
    public void txnRead2() {
        final DatasetGraph dsg = emptyDsg();
        assertFalse(dsg.isInTransaction());
        dsg.begin(ReadWrite.READ);
        assertTrue(dsg.isInTransaction());
        dsg.end();
    }

    @Test
    public void txnRead3() {
        final DatasetGraph dsg = emptyDsg();
        assertFalse(dsg.isInTransaction());
        dsg.begin();
        assertTrue(dsg.isInTransaction());
        assertEquals(ReadWrite.READ, dsg.transactionMode());
        assertEquals(TxnType.READ, dsg.transactionType());
        dsg.end();
    }

    @Test
    public void noWriting1() {
		assertThrows(JenaTransactionException.class,
					 ()-> emptyDsg().begin(ReadWrite.WRITE));
    }

    @Test
    public void noWriting2() {
		assertThrows(JenaTransactionException.class,
					 ()-> emptyDsg().begin(TxnType.WRITE));
    }

    @Test
    public void noWriting3() {
		assertThrows(JenaTransactionException.class,
					 ()-> emptyDsg().begin(TxnType.READ_PROMOTE));
    }

    @Test
    public void noWriting4() {
		assertThrows(JenaTransactionException.class,
					 ()-> emptyDsg().begin(TxnType.READ_COMMITTED_PROMOTE));
    }

    @Test
    public void noPromoting() {
        final DatasetGraph dsg = emptyDsg();
        // Dynadic datasets are read-only.
        dsg.begin(ReadWrite.READ);
        boolean b = dsg.promote();
        assertFalse(b);
    }

    @Test
    public void testTransactionTypeAndMode() {
        final DatasetGraph dsg = emptyDsg();
        assertFalse(dsg.isInTransaction());
        dsg.begin(TxnType.READ);
        assertTrue(dsg.isInTransaction());
        assertEquals(TxnType.READ, dsg.transactionType());
        assertEquals(ReadWrite.READ, dsg.transactionMode());
        dsg.end();
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
