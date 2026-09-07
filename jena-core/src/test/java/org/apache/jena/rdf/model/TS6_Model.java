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

import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import org.apache.jena.test.JenaTestLib;

@Suite
@SelectClasses({
    TestDefaultModel.class,

    // Not model-parameterized.
    TestAnonID.class,
    TestListStatements.class,
    TestResourceFactory.class,
    TestConcurrency.class,
    TestModelFactory.class,
    TestProperties.class,

    // Parameterized over ModelCreators.creators().
    TestContains.class,
    TestStatements.class,
    TestAddAndContains.class,
    TestAddModel.class,
    TestAltMethods.class,
    TestBagMethods.class,
    TestContainerConstructors.class,
    TestContainers.class,
    TestCopyInOutOfModel.class,
    TestGetFromModel.class,
    TestHiddenStatements.class,
    TestIterators.class,
    TestList.class,
    TestListSubjects.class,
    TestListSubjectsEtc.class,
    TestLiteralImpl.class,
    TestLiterals.class,
    TestLiteralsInModel.class,
    TestModel.class,
    TestModelBulkUpdate.class,
    TestModelEvents.class,
    TestModelPolymorphism.class,
    TestModelRead.class,
    TestModelSetOperations.class,
    TestNamespace.class,
    TestObjectOfProperties.class,
    TestObjects.class,
    TestRDFNodes.class,
    TestReaderEvents.class,
    TestReaders.class,
    TestRemoveSPO.class,
    TestResourceImpl.class,
    TestResourceMethods.class,
    TestResources.class,
    TestSeqMethods.class,
    TestSimpleListStatements.class,
    TestStatementCreation.class,
    TestStatementMethods.class,
    TestStatementTerms.class
})

public class TS6_Model {
    @BeforeSuite
    public static void beforeSuite() {
        JenaTestLib.setup();
    }
}
