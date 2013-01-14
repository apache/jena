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

import org.junit.After;
import org.junit.Assert;

/**
 * Test nesting concurrency.
 */

public class TestConcurrencyNesting extends AbstractModelTestBase
{

	public static int MODEL1 = 0;
	public static int MODEL2 = 1;

	// Test suite to exercise the locking
	long SLEEP = 100;
	int threadCount = 0;

	// Note : reuse the model across tests.

	private Model[] workingModels;

	private final int outerModel;
	private final int innerModel;
	boolean outerLock;
	boolean innerLock;
	boolean exceptionExpected;

	/**
	 * Create a TestConcurrencyNesting fixture.
	 * 
	 * @param modelFactory
	 *            an instance of the TestingModelFactory
	 * @param model1Idx
	 *            which model to use for outer model.
	 * @param model2Idx
	 *            which model to use for inner model
	 * @param lock1
	 *            lock type for outer model. Lock.READ or Lock.WRITE
	 * @param lock2
	 *            lock type for inner model.
	 * @param exExpected
	 *            true if an exception is expected.
	 * @param name
	 *            The name of the test to run.
	 */
	public TestConcurrencyNesting( final TestingModelFactory modelFactory,
			final Integer model1Idx, final Integer model2Idx,
			final Boolean lock1, final Boolean lock2, final Boolean exExpected,
			final String name )
	{
		super(modelFactory, name);
		outerModel = model1Idx;
		outerLock = lock1;
		innerModel = model2Idx;
		innerLock = lock2;
		exceptionExpected = exExpected;
	}

	/**
	 * A human readable test name.
	 */
	@Override
	public String getName()
	{
		return String.format("%s - %s outerLock:%s innerLock:%s", super
				.getName(), (outerModel == innerModel) ? "same" : "different",
				(outerLock), (innerLock));
	}

	@Override
	public void setUp()
	{
		workingModels = new Model[2];
		workingModels[TestConcurrencyNesting.MODEL1] = createModel();
		workingModels[TestConcurrencyNesting.MODEL2] = createModel();
	}

	@Override
	@After
	public void tearDown()
	{
		workingModels[TestConcurrencyNesting.MODEL1].close();
		workingModels[TestConcurrencyNesting.MODEL2].close();
	}

	public void testNesting() throws Throwable
	{
		boolean gotException = false;
		try
		{
			workingModels[outerModel].enterCriticalSection(outerLock);

			try
			{
				try
				{
					// Should fail if outerLock is READ and innerLock is WRITE
					// and its on the same model, inner and outer.
					workingModels[innerModel].enterCriticalSection(innerLock);

				}
				finally
				{
					workingModels[innerModel].leaveCriticalSection();
				}
			}
			catch (final Exception ex)
			{
				gotException = true;
			}

		}
		finally
		{
			workingModels[outerModel].leaveCriticalSection();
		}

		if (exceptionExpected)
		{
			Assert.assertTrue(getName()
					+ " Failed to get expected lock promotion error",
					gotException);
		}
		else
		{
			Assert.assertTrue(getName()
					+ " Got unexpected lock promotion error", !gotException);
		}
	}

}
