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

package org.apache.jena.sparql.core.journaling;

import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.sparql.core.DatasetGraphFactory.createMem;
import static org.apache.jena.sparql.graph.GraphFactory.createGraphMem;
import static org.apache.jena.sparql.sse.SSE.parseTriple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.AbstractDatasetGraphTests;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphMap;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.TestDatasetGraphWithLock;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.journaling.QuadOperation.QuadDeletion;
import org.apache.jena.sparql.core.journaling.TestDatasetGraphWithRecord.TestDatasetGraphWithRecordAsDSG;
import org.apache.jena.sparql.core.journaling.TestDatasetGraphWithRecord.TestDatasetGraphWithRecordConcurrency;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestDatasetGraphWithRecordAsDSG.class, TestDatasetGraphWithRecordConcurrency.class })
public class TestDatasetGraphWithRecord {

	public static class TestDatasetGraphWithRecordAsDSG extends AbstractDatasetGraphTests {
		private static final Node graphName = createURI("some-graph");
		private static final Quad q1 = new Quad(graphName, parseTriple("(<subject1> <predicate> <object1>)"));
		private static final Quad q2 = new Quad(graphName, parseTriple("(<subject1> <predicate> <object2>)"));
		private static final Quad q3 = new Quad(graphName, parseTriple("(<subject2> <predicate> <object2>)"));

		@Override
		protected DatasetGraph emptyDataset() {
			return new DatasetGraphWithRecord(createMem());
		}

		/**
		 * Adding a graph via {@link DatasetGraphWithRecord#addGraph(Node, org.apache.jena.graph.Graph)} should copy the
		 * tuples from that graph, instead of creating a reference to that graph.
		 */
		@Test
		public void testAddGraphCopiesTuples() {
			final Graph graph = createGraphMem();
			graph.add(q1.asTriple());
			graph.add(q2.asTriple());

			final Dataset dataset = DatasetFactory.wrap(emptyDataset());
			final DatasetGraph dsg = dataset.asDatasetGraph();

			dataset.begin(WRITE);
			try {
				dsg.addGraph(graphName, graph);
				dataset.commit();
			} finally {
				dataset.end();
			}

			assertTrue(dsg.contains(q1));
			assertTrue(dsg.contains(q2));
			// we add a new triple to our original graph
			graph.add(q3.asTriple());
			// which should not show up in the dataset, because of our "copy-on-addGraph" semantics
			assertFalse(dsg.contains(ANY, q3.getSubject(), q3.getPredicate(), q3.getObject()));
			dataset.close();
		}

		/**
		 * {@link Transactional#abort()} is properly supported.
		 */
		@Test
		public void testSimpleAbort() {
			final Dataset dataset = DatasetFactory.wrap(emptyDataset());
			final DatasetGraph dsg = dataset.asDatasetGraph();

			dataset.begin(WRITE);
			try {
				dsg.addGraph(graphName, createGraphMem());
				dsg.add(q1);
				dataset.commit();
			} finally {
				dataset.end();
			}
			try {
				dataset.begin(WRITE);
				dsg.add(q2);
				dsg.add(q3);
				dsg.delete(q3);
				dsg.add(q3);
				dataset.abort();
			} finally {
				dataset.end();
			}
			assertTrue(dsg.contains(q1));
			assertFalse(dsg.contains(q2));
			assertFalse(dsg.contains(q3));
			dataset.close();
		}

		/**
		 * Neither adding an already-present quad nor removing an absent quad should produce any change in the record.
		 */
		@Test
		public void testRecordShouldBeCompact() {
			final List<QuadOperation<?, ?>> record = new ArrayList<>();
			final Dataset dataset = DatasetFactory.wrap(new DatasetGraphWithRecord(
					new DatasetGraphMap(createGraphMem()), new ListBackedOperationRecord<>(record)));
			final DatasetGraph dsg = dataset.asDatasetGraph();

			dataset.begin(WRITE);
			try {
				dsg.addGraph(graphName, createGraphMem());
				// add the same quad twice
				dsg.add(q1);
				dsg.add(q1);
				// now there should be only one operation in the journal
				assertEquals(1, record.size());

				dsg.delete(q1);
				// now there should be two operations in the journal
				assertEquals(2, record.size());
				// delete the quad we've already deleted
				dsg.delete(q1);
				// now there should still be only two operations in the journal
				assertEquals(2, record.size());
			} finally {
				dataset.end();
			}
			dataset.close();
		}

		/**
		 * {@link DatasetGraphWithRecord} can only be mutated within a WRITE transaction, not any other kind of
		 * transaction.
		 */
		@Test(expected = JenaTransactionException.class)
		public void testDatasetGraphWithRecordIsWriteTransactionalOnlyForGraphWrites() {
			final Dataset dataset = DatasetFactory.wrap(emptyDataset());
			final DatasetGraph dsg = dataset.asDatasetGraph();

			dataset.begin(READ);
			try {
				dsg.addGraph(graphName, createGraphMem());
			} finally {
				dataset.end();
			}
		}

		/**
		 * {@link DatasetGraphWithRecord} can only be mutated within a WRITE transaction, not any other kind of
		 * transaction.
		 */
		@Test(expected = JenaTransactionException.class)
		public void testDatasetGraphWithRecordIsWriteTransactionalOnlyForTupleWrites() {
			final Dataset dataset = DatasetFactory.wrap(emptyDataset());
			final DatasetGraph dsg = dataset.asDatasetGraph();

			dataset.begin(READ);
			try {
				dsg.add(q1);
			} finally {
				dataset.end();
			}
		}

		/**
		 * {@link DatasetGraphWithRecord#removeGraph(Node)} should leave a record of quads removed.
		 */
		@Test
		public void testRemoveGraph() {
			final List<QuadOperation<?, ?>> record = new ArrayList<>();
			final Dataset dataset = DatasetFactory.wrap(new DatasetGraphWithRecord(
					new DatasetGraphMap(createGraphMem()), new ListBackedOperationRecord<>(record)));
			final DatasetGraph dsg = dataset.asDatasetGraph();

			dataset.begin(WRITE);
			try {
				dsg.addGraph(graphName, createGraphMem());
				dsg.add(q1);
				dataset.commit();
			} finally {
				dataset.end();
			}
			assertTrue(dsg.containsGraph(graphName));
			assertTrue(dsg.contains(q1));
			dataset.begin(WRITE);
			try {
				dsg.removeGraph(graphName);
				// we should see a single deletion resulting from the removeGraph
				assertEquals(1, record.size());
				assertEquals(new QuadDeletion(q1), record.get(0));
				dataset.commit();
			} finally {
				dataset.end();
			}
			assertFalse(dsg.containsGraph(graphName));
			assertFalse(dsg.contains(ANY, q1.getSubject(), q1.getPredicate(), q1.getObject()));
			assertTrue(dsg.isEmpty());
		}

		/**
		 * {@link DatasetGraphWithRecord#clear()} should leave a record of quads removed.
		 */
		@Test
		public void testClear() {
			final List<QuadOperation<?, ?>> record = new ArrayList<>();
			final Dataset dataset = DatasetFactory.wrap(new DatasetGraphWithRecord(
					new DatasetGraphMap(createGraphMem()), new ListBackedOperationRecord<>(record)));
			final DatasetGraph dsg = dataset.asDatasetGraph();

			dataset.begin(WRITE);
			try {
				dsg.addGraph(graphName, createGraphMem());
				dsg.add(q1);
				dataset.commit();
			} finally {
				dataset.end();
			}
			assertTrue(dsg.contains(q1));
			dataset.begin(WRITE);
			try {
				dsg.clear();
				// we should see a single deletion resulting from the clear
				assertEquals(1, record.size());
				assertEquals(new QuadDeletion(q1), record.get(0));
				dataset.commit();
			} finally {
				dataset.end();
			}
			assertTrue(dsg.isEmpty());
		}
	}

	public static class TestDatasetGraphWithRecordConcurrency extends TestDatasetGraphWithLock {
		@Override
		protected Dataset createDataset() {
			return DatasetFactory.wrap(new DatasetGraphWithRecord(createMem()));
		}
	}
}
