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

package org.apache.jena.sparql.core.mem;

import static com.jayway.awaitility.Awaitility.await;
import static java.lang.System.err;
import static org.apache.jena.atlas.iterator.Iter.iter;
import static org.apache.jena.atlas.iterator.Iter.some;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.graph.NodeFactory.createBlankNode;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.sparql.core.Quad.unionGraph;
import static org.apache.jena.sparql.graph.GraphFactory.createGraphMem;
import static org.apache.jena.sparql.sse.SSE.*;
import static org.junit.Assert.*;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.AbstractDatasetGraphTests;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.TestDatasetGraphViewGraphs;
import org.apache.jena.sparql.core.TestDatasetGraphWithLock;
import org.apache.jena.sparql.core.mem.TestDatasetGraphInMemory.TestDatasetGraphInMemoryBasic;
import org.apache.jena.sparql.core.mem.TestDatasetGraphInMemory.TestDatasetGraphInMemoryLock;
import org.apache.jena.sparql.core.mem.TestDatasetGraphInMemory.TestDatasetGraphInMemoryThreading;
import org.apache.jena.sparql.core.mem.TestDatasetGraphInMemory.TestDatasetGraphInMemoryTransactions;
import org.apache.jena.sparql.core.mem.TestDatasetGraphInMemory.TestDatasetGraphInMemoryViews;
import org.apache.jena.sparql.transaction.AbstractTestTransaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.slf4j.Logger;

@RunWith(Suite.class)
@SuiteClasses({ TestDatasetGraphInMemoryBasic.class, TestDatasetGraphInMemoryViews.class,
		TestDatasetGraphInMemoryLock.class, TestDatasetGraphInMemoryThreading.class,
		TestDatasetGraphInMemoryTransactions.class })
public class TestDatasetGraphInMemory {

	public static class TestDatasetGraphInMemoryThreading extends Assert {

		Logger log = getLogger(TestDatasetGraphInMemoryThreading.class);

		Quad q = Quad.create(createBlankNode(), createBlankNode(), createBlankNode(), createBlankNode());

		@Test
		public void abortedChangesNeverBecomeVisible() {
			final DatasetGraphInMemory dsg = new DatasetGraphInMemory();
			// flags with which to interleave threads
			final AtomicBoolean addedButNotAborted = new AtomicBoolean(false);
			final AtomicBoolean addedCheckedButNotAborted = new AtomicBoolean(false);
			final AtomicBoolean aborted = new AtomicBoolean(false);

			dsg.begin(READ);
			assertTrue(dsg.isEmpty()); // no quads present
			dsg.end();

			// we introduce a Writer thread
			new Thread() {

				@Override
				public void run() {
					dsg.begin(WRITE);
					log.debug("Writer: Added test quad.");
					dsg.add(q);
					assertFalse(dsg.isEmpty()); // quad has appeared in this transaction
					addedButNotAborted.set(true);
					log.debug("Writer: Waiting to abort addition of test quad.");
					await().untilTrue(addedCheckedButNotAborted);
					assertFalse(dsg.isEmpty()); // quad has appeared, but only inside this transaction
					log.debug("Writer: Aborting test quad.");
					dsg.abort();
					log.debug("Writer: Aborted test quad.");
					aborted.set(true);
				}
			}.start();
			// back to Reader code
			log.debug("Reader: Waiting for test quad to be added in Writer thread.");
			await().untilTrue(addedButNotAborted);
			dsg.begin(READ);
			assertTrue(dsg.isEmpty()); // no quads present to Reader
			dsg.end();
			log.debug("Reader: Checked to see test quad is not visible.");
			addedCheckedButNotAborted.set(true);
			log.debug("Reader: Waiting to see Writer transaction aborted.");
			await().untilTrue(aborted);
			dsg.begin(READ);
			assertTrue(dsg.isEmpty()); // no quads have appeared
			dsg.end();
		}

		@Test
		public void snapshotsShouldBeIsolated() {
			final DatasetGraphInMemory dsg = new DatasetGraphInMemory();
			// flags with which to interleave threads
			final AtomicBoolean addedButNotCommitted = new AtomicBoolean(false);
			final AtomicBoolean addedCheckedButNotCommitted = new AtomicBoolean(false);
			final AtomicBoolean committed = new AtomicBoolean(false);

			dsg.begin(READ);
			assertTrue(dsg.isEmpty()); // no quads present
			dsg.end();

			// we introduce a Writer thread
			new Thread() {

				@Override
				public void run() {
					dsg.begin(WRITE);
					log.debug("Writer: Added test quad.");
					dsg.add(q);
					assertFalse(dsg.isEmpty()); // quad has appeared, but only in this transaction
					addedButNotCommitted.set(true);
					log.debug("Writer: Waiting to commit test quad.");
					await().untilTrue(addedCheckedButNotCommitted);
					log.debug("Writer: Committing test quad.");
					dsg.commit();
					log.debug("Writer: Committed test quad.");
					committed.set(true);
				}
			}.start();
			// back to Reader code
			log.debug("Reader: Waiting for test quad to be added in Writer thread.");
			await().untilTrue(addedButNotCommitted);

			dsg.begin(READ);
			assertTrue(dsg.isEmpty()); // still no quads present, because Reader and Writer are isolated
			log.debug("Reader: Checked to see test quad is not yet visible.");
			addedCheckedButNotCommitted.set(true);
			log.debug("Reader: Waiting to see test quad committed.");
			await().untilTrue(committed);
			assertTrue(dsg.isEmpty()); // still no quads present, because Reader and Writer are isolated
			dsg.end();
			// but a new transaction should see the results of Writer's action
			dsg.begin(READ);
			assertFalse(dsg.isEmpty()); // quad has appeared, for new transaction
			dsg.end();
		}

		@Test
		public void locksAreCorrectlyDistributed() {
			final DatasetGraphInMemory dsg = new DatasetGraphInMemory();
			final AtomicBoolean readLockCaptured = new AtomicBoolean(false);
			final AtomicBoolean writeLockCaptured = new AtomicBoolean(false);

			dsg.begin(WRITE); // acquire the write lock: no other Thread can now acquire it until it is released

			new Thread() {

				@Override
				public void run() {
					dsg.begin(READ); // a read lock should always be available except during a commit
					readLockCaptured.set(true);
					dsg.end();

					dsg.begin(WRITE); // this should block until the write lock is released
					writeLockCaptured.set(true);
				}
			}.start();
			await().untilTrue(readLockCaptured);
			if (writeLockCaptured.get()) fail("Write lock captured by two threads at once!");

			dsg.end(); // release the write lock to competitor
			await().untilTrue(writeLockCaptured);
			assertTrue("Lock was not handed over to waiting thread!", writeLockCaptured.get());
		}
	}

	public static class TestDatasetGraphInMemoryBasic extends AbstractDatasetGraphTests {

		@Test
		public void orderingOfNodesFromFindIsCorrect() {
			final DatasetGraph dsg = DatasetGraphFactory.createTxnMem() ;

	        final Node p = parseNode(":p") ;
	        final Triple triple = parseTriple("(:s :p :o)");
			dsg.getDefaultGraph().add(triple);
	        final Iterator<Triple> iter = dsg.getDefaultGraph().find(null, p, null) ;
	        assertTrue(some(iter, triple::equals));


	        final Node p1 = parseNode(":p1") ;
	        final Quad quad = parseQuad("(:g1 :s1 :p1 :o1)");
			dsg.add(quad) ;

	        final Iterator<Quad> iter2 = dsg.find(null, null, p1, null) ;

	        assertTrue(some(iter2, quad::equals));
	        Iter.print(err,iter2);
		}

		@Test
		public void prefixesAreManaged() {
			final Node graphName = createURI("http://example/g");
			final DatasetGraph dsg = emptyDataset();
			dsg.addGraph(graphName, createGraphMem());
			final Dataset dataset = DatasetFactory.create(dsg);
			Model model = dataset.getNamedModel(graphName.getURI());
			final String testPrefix = "example";
			final String testURI = "http://example/";
			model.setNsPrefix(testPrefix, testURI);
			assertEquals(testURI, model.getNsPrefixURI(testPrefix));
			model.close();
			model = dataset.getNamedModel(graphName.getURI());
			final String nsURI = dataset.getNamedModel(graphName.getURI()).getNsPrefixURI(testPrefix);
			assertNotNull(nsURI);
			assertEquals(testURI, nsURI);
		}

		@Test
		public void unionGraphWorksProperly() {
			final DatasetGraph dsg = emptyDataset();
			// quads from named graphs should appear in union
			final Quad q = Quad.create(createBlankNode(), createBlankNode(), createBlankNode(), createBlankNode());
			dsg.add(q);
			assertTrue(iter(dsg.find(unionGraph, ANY, ANY, ANY)).some(q::equals));
			// no triples from default graph should appear in union
			final Triple t = Triple.create(createBlankNode(), createBlankNode(), createBlankNode());
			dsg.getDefaultGraph().add(t);
			assertFalse(iter(dsg.find(unionGraph, ANY, ANY, ANY)).some(t::equals));
		}

		@Override
		protected DatasetGraph emptyDataset() {
			return DatasetGraphFactory.createTxnMem();
		}
	}

	public static class TestDatasetGraphInMemoryLock extends TestDatasetGraphWithLock {
		@Override
		protected Dataset createFixed() {
			return DatasetFactory.createTxnMem();
		}
	}

	public static class TestDatasetGraphInMemoryViews extends TestDatasetGraphViewGraphs {

		@Override
		protected DatasetGraph createBaseDSG() {
			return DatasetGraphFactory.createTxnMem();
		}
	}

	public static class TestDatasetGraphInMemoryTransactions extends AbstractTestTransaction {

		@Override
		protected Dataset create() {
			return DatasetFactory.createTxnMem();
		}
	}
}
