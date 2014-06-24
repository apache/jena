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

import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.shared.Lock;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Collected test suite for the .model package.
 * 
 * This is the base class for TestPackage implementations.
 * 
 * Model developers should extend this class to implement the package test
 * suite.
 * See TestPackage for example of usage.
 */

public abstract class AbstractTestPackage extends TestSuite
{

	/**
	 * Constructor.
	 * 
	 * @param suiteName
	 *            The name for this TestPackage
	 * @param modelFactory
	 *            The TestingModelFactory that will be used to create models.
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected AbstractTestPackage( final String suiteName,
			final TestingModelFactory modelFactory )
	{
		super(suiteName);

		addTest(TestModelFactory.class);
		addTest(TestSimpleListStatements.class, modelFactory);
		addTest(TestModelPolymorphism.class, modelFactory);
		addTest(TestSimpleSelector.class, modelFactory);
		addTest(TestStatements.class, modelFactory);
		addTest(TestRDFNodes.class, modelFactory);

		addTest(TestReifiedStatements.class, modelFactory);

		addTest(TestIterators.class, modelFactory);

		addTest(TestContains.class, modelFactory);
		addTest(TestLiteralImpl.class, modelFactory);
		addTest(TestResourceImpl.class, modelFactory);
		addTest(TestHiddenStatements.class, modelFactory);
		addTest(TestNamespace.class, modelFactory);
		addTest(TestModelBulkUpdate.class, modelFactory);
		addTest(TestConcurrencyNesting.class, modelFactory,
				TestConcurrencyNesting.MODEL1, TestConcurrencyNesting.MODEL1,
				Lock.READ, Lock.READ, false);
		addTest(TestConcurrencyNesting.class, modelFactory,
				TestConcurrencyNesting.MODEL1, TestConcurrencyNesting.MODEL1,
				Lock.WRITE, Lock.WRITE, false);
		addTest(TestConcurrencyNesting.class, modelFactory,
				TestConcurrencyNesting.MODEL1, TestConcurrencyNesting.MODEL1,
				Lock.READ, Lock.WRITE, true);
		addTest(TestConcurrencyNesting.class, modelFactory,
				TestConcurrencyNesting.MODEL1, TestConcurrencyNesting.MODEL1,
				Lock.WRITE, Lock.READ, false);
		addTest(TestConcurrencyNesting.class, modelFactory,
				TestConcurrencyNesting.MODEL1, TestConcurrencyNesting.MODEL2,
				Lock.READ, Lock.READ, false);
		addTest(TestConcurrencyNesting.class, modelFactory,
				TestConcurrencyNesting.MODEL1, TestConcurrencyNesting.MODEL2,
				Lock.WRITE, Lock.WRITE, false);
		addTest(TestConcurrencyNesting.class, modelFactory,
				TestConcurrencyNesting.MODEL1, TestConcurrencyNesting.MODEL2,
				Lock.READ, Lock.WRITE, false);
		addTest(TestConcurrencyNesting.class, modelFactory,
				TestConcurrencyNesting.MODEL1, TestConcurrencyNesting.MODEL2,
				Lock.WRITE, Lock.READ, false);

		addTest(TestConcurrencyParallel.class, modelFactory);
		addTest(TestModelMakerImpl.class);
		addTest(TestModelPrefixMapping.class, modelFactory);
		addTest(TestContainers.class, modelFactory);
		addTest(TestModel.class, modelFactory);
		addTest(TestModelSetOperations.class, modelFactory);
		addTest(TestSelectors.class, modelFactory);
		addTest(TestModelEvents.class, modelFactory);
		addTest(TestReaderEvents.class, modelFactory);
		addTest(TestList.class, modelFactory);
		addTest(TestAnonID.class);
		addTest(TestLiteralsInModel.class, modelFactory);
		addTest(TestRemoveSPO.class, modelFactory);
		addTest(TestListSubjectsEtc.class, modelFactory);
		addTest(TestModelExtract.class, modelFactory);
		addTest(TestModelRead.class, modelFactory);
		addTestSuite(TestPropertyImpl.class);
		addTest(TestRemoveBug.class, modelFactory);
		addTest(TestContainerConstructors.class, modelFactory);
		addTest(TestAltMethods.class, modelFactory);
		addTest(TestBagMethods.class, modelFactory);
		addTest(TestSeqMethods.class, modelFactory);
		addTest(TestAddAndContains.class, modelFactory);
		addTest(TestAddModel.class, modelFactory);
		addTest(TestGet.class, modelFactory);
		addTest(TestListSubjects.class, modelFactory);
		addTest(TestLiterals.class, modelFactory);
		addTest(TestObjects.class, modelFactory);
		addTest(TestResourceMethods.class, modelFactory);
		addTest(TestResources.class, modelFactory);
		addTest(TestStatementMethods.class, modelFactory);
		addTest(TestStatementCreation.class, modelFactory);
		addTest(TestReaders.class, modelFactory);
		addTest(TestObjectOfProperties.class, modelFactory);
		addTest(TestCopyInOutOfModel.class, modelFactory);
		addTest(TestSelectorUse.class, modelFactory);
		// These tests are probabilistic testing.
		// See notes in the class.
		//addTest(IsomorphicTests.class, modelFactory);
	}

	/**
	 * Adds a test to the test suite by looking for the standard test methods.
	 * These are
	 * methods that start with "test" and have no arguments.
	 * 
	 * @param testClass
	 * @param constructorArgs
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void addTest( final Class<? extends TestCase> testClass,
			final Object... constructorArgs )
	{
		final Object[] args = new Object[constructorArgs.length + 1];
		System.arraycopy(constructorArgs, 0, args, 0, constructorArgs.length);
		final List<Class<?>> parameterTypes = new ArrayList<>();
		for (final Object o : constructorArgs)
		{
			if (o instanceof TestingModelFactory)
			{
				parameterTypes.add(TestingModelFactory.class);
			}
			else
			{
				parameterTypes.add(o.getClass());
			}
		}
		parameterTypes.add(String.class);
		Constructor<TestCase> c;
		try
		{
		    @SuppressWarnings( "unchecked" )
		    Constructor<TestCase> cc = (Constructor<TestCase>) testClass.getConstructor(parameterTypes
					.toArray(new Class[parameterTypes.size()]));
		    c = cc ;
		}
		catch (final SecurityException | NoSuchMethodException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e);
		}

        for (final Method m : testClass.getMethods())
		{
			if (m.getParameterTypes().length == 0)
			{
				if (m.getName().startsWith("test"))
				{
					args[constructorArgs.length] = m.getName();
					try
					{
						addTest(c.newInstance(args));
					}
					catch (final IllegalArgumentException | InvocationTargetException | IllegalAccessException | InstantiationException e)
					{
						e.printStackTrace();
						throw new RuntimeException(e.getMessage(), e);
					}
                }
			}
		}
	}

}
