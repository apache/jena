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

package org.apache.jena.sparql.core.mem;

import static org.awaitility.Awaitility.await ;
import static org.apache.jena.graph.NodeFactory.createBlankNode ;
import static org.apache.jena.query.ReadWrite.READ ;
import static org.apache.jena.query.ReadWrite.WRITE ;
import static org.slf4j.LoggerFactory.getLogger ;

import java.util.concurrent.atomic.AtomicBoolean ;

import org.apache.jena.sparql.core.Quad ;
import org.junit.Assert ;
import org.junit.Test ;
import org.slf4j.Logger ;

public class TestDatasetGraphInMemoryThreading extends Assert {

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

		dsg.abort();
		dsg.end(); // release the write lock to competitor
		await().untilTrue(writeLockCaptured);
		assertTrue("Lock was not handed over to waiting thread!", writeLockCaptured.get());
	}
}
