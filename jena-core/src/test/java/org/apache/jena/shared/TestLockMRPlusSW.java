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

package org.apache.jena.shared;

import static org.awaitility.Awaitility.await;
import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.defaultThreadFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.test.JenaTestBase;
import org.junit.Test;

import junit.framework.TestSuite;

public class TestLockMRPlusSW extends JenaTestBase {

	public TestLockMRPlusSW(final String name) {
		super(name);
	}

	public static TestSuite suite() {
		return new TestSuite(TestLockMRPlusSW.class);
	}

	@Test
	public void testMultipleReadersAtATime() {
		final Lock testLock = new LockMRPlusSW();
		testLock.enterCriticalSection(true);
		final AtomicBoolean secondReaderHasLock = new AtomicBoolean();
		// new reader
		defaultThreadFactory().newThread(() -> {
			testLock.enterCriticalSection(true);
			secondReaderHasLock.set(true);
		}).start();
		// the only way to fail is to timeout
		await().untilTrue(secondReaderHasLock);
	}

	@Test
	public void testOneWriterAtATime() throws InterruptedException {
		final Lock testLock = new LockMRPlusSW();
		testLock.enterCriticalSection(false);
		final AtomicBoolean secondWriterHasLock = new AtomicBoolean();
		// new writer
		defaultThreadFactory().newThread(() -> {
			testLock.enterCriticalSection(false);
			secondWriterHasLock.set(true);
		}).start();
		sleep(5000);
		assertFalse("Multiple writers were allowed!", secondWriterHasLock.get());
	}

	@Test
	public void testAWriterDoesNotBlockReaders() {
		final Lock testLock = new LockMRPlusSW();
		testLock.enterCriticalSection(false);
		final AtomicBoolean readerHasLock = new AtomicBoolean();
		// new reader
		defaultThreadFactory().newThread(() -> {
			testLock.enterCriticalSection(true);
			readerHasLock.set(true);
		}).start();
		await().untilTrue(readerHasLock);
	}
}
