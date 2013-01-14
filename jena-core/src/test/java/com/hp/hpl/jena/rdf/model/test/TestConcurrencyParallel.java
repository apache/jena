/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.shared.Lock;

import org.junit.Assert;

/**
 * Parallel concurrency tests.
 */

public class TestConcurrencyParallel extends AbstractModelTestBase
{

	class Operation extends Thread
	{
		Model lockModel;
		boolean readLock;

		Operation( final Model m, final boolean withReadLock )
		{
			lockModel = m;
			readLock = withReadLock;
		}

		@Override
		public void run()
		{
			for (int i = 0; i < 2; i++)
			{
				try
				{
					lockModel.enterCriticalSection(readLock);
					if (readLock)
						readOperation(false);
					else
						writeOperation(false);
				}
				finally
				{
					lockModel.leaveCriticalSection();
				}
			}
		}
	}

	// Operations ----------------------------------------------
	long SLEEP = 100;
	int threadTotal = 10;

	int threadCount = 0;

	volatile int writers = 0;

	public TestConcurrencyParallel( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	// The example model operations
	void doStuff( final String label, final boolean doThrow )
	{
		Thread.currentThread().getName();
		// Puase a while to cause other threads to (try to) enter the region.
		try
		{
			Thread.sleep(SLEEP);
		}
		catch (final InterruptedException intEx)
		{
		}
		if (doThrow)
		{
			throw new RuntimeException(label);
		}
	}

	public void readOperation( final boolean doThrow )
	{
		if (writers > 0)
		{
			System.err.println("Concurrency error: writers around!");
		}
		doStuff("read operation", false);
		if (writers > 0)
		{
			System.err.println("Concurrency error: writers around!");
		}
	}

	// Example operations

	public void testParallel() throws Throwable
	{

		final Thread threads[] = new Thread[threadTotal];

		boolean getReadLock = Lock.READ;
		for (int i = 0; i < threadTotal; i++)
		{
			final String nextId = "T" + Integer.toString(++threadCount);
			threads[i] = new Operation(model, getReadLock);
			threads[i].setName(nextId);
			threads[i].start();

			getReadLock = !getReadLock;
		}

		boolean problems = false;
		for (int i = 0; i < threadTotal; i++)
		{
			try
			{
				threads[i].join(200 * SLEEP);
			}
			catch (final InterruptedException intEx)
			{
			}
		}

		// Try again for any we missed.
		for (int i = 0; i < threadTotal; i++)
		{
			if (threads[i].isAlive())
			{
				try
				{
					threads[i].join(200 * SLEEP);
				}
				catch (final InterruptedException intEx)
				{
				}
			}
			if (threads[i].isAlive())
			{
				System.out.println("Thread " + threads[i].getName()
						+ " failed to finish");
				problems = true;
			}
		}

		Assert.assertTrue("Some thread failed to finish", !problems);
	}

	public void writeOperation( final boolean doThrow )
	{
		writers++;
		doStuff("write operation", false);
		writers--;

	}

}
