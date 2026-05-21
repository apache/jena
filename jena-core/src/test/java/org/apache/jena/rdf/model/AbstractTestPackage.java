/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.rdf.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.jena.rdf.model.helpers.ModelCreator;


/**
 * Collected test suite for the .model package.
 */
public class AbstractTestPackage extends TestSuite
{
	protected AbstractTestPackage( String suiteName, ModelCreator modelFactory ) {
	    super(suiteName);

	    // Rewrite a Junit4/6 parameterized tests?
	    // Replace by creating the instance here. Test.

	    addTestSuite(TestModelFactory.class);

	    addTest(TestSimpleListStatements.class, modelFactory);
	    addTest(TestModelPolymorphism.class, modelFactory);
	    addTest(TestStatements.class, modelFactory);
	    addTest(TestRDFNodes.class, modelFactory);
	    addTest(TestIterators.class, modelFactory);

	    addTest(TestContains.class, modelFactory);
	    addTest(TestLiteralImpl.class, modelFactory);
	    addTest(TestResourceImpl.class, modelFactory);
	    addTest(TestStatementTerms.class, modelFactory);

	    addTest(TestHiddenStatements.class, modelFactory);
	    addTest(TestNamespace.class, modelFactory);
	    addTest(TestModelBulkUpdate.class, modelFactory);

	    addTest(new TestConcurrency());

	    addTest(TestContainers.class, modelFactory);
	    addTest(TestModel.class, modelFactory);
	    addTest(TestModelSetOperations.class, modelFactory);
	    addTest(TestModelEvents.class, modelFactory);
	    addTest(TestReaderEvents.class, modelFactory);
	    addTest(TestList.class, modelFactory);

	    //addTest(TestAnonID.class);
	    addTestSuite(TestAnonID.class);

	    addTest(TestLiteralsInModel.class, modelFactory);
	    addTest(TestRemoveSPO.class, modelFactory);
	    addTest(TestListSubjectsEtc.class, modelFactory);
	    addTest(TestModelRead.class, modelFactory);
	    addTestSuite(TestProperties.class);
	    addTest(TestContainerConstructors.class, modelFactory);
	    addTest(TestAltMethods.class, modelFactory);
	    addTest(TestBagMethods.class, modelFactory);
	    addTest(TestSeqMethods.class, modelFactory);
	    addTest(TestAddAndContains.class, modelFactory);
	    addTest(TestAddModel.class, modelFactory);
	    addTest(TestGetFromModel.class, modelFactory);
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
	    // These tests are probabilistic testing.
	    // See notes in the class.
	    //addTest(IsomorphicTests.class, modelFactory);
	}

	private void addTest(final Class<? extends TestCase> testClass, ModelCreator modelFactory) {
        final Object[] args = new Object[2];
        args[0] = modelFactory;

        final List<Class<? >> parameterTypes = List.of(ModelCreator.class, String.class);
        Constructor<TestCase> c;
        try {
            @SuppressWarnings("unchecked")
            Constructor<TestCase> cc = (Constructor<TestCase>)testClass.getConstructor(parameterTypes.toArray(new Class[parameterTypes.size()]));
            c = cc;
        } catch (final SecurityException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }

        for ( final Method m : testClass.getMethods() ) {
            if ( m.getParameterTypes().length == 0 ) {
                if ( m.getName().startsWith("test") ) {
                    args[1] = m.getName();
                    try {
                        addTest(c.newInstance(args));
                    } catch (final IllegalArgumentException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            }
        }
    }
}