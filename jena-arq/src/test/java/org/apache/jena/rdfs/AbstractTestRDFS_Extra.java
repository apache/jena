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

package org.apache.jena.rdfs;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public abstract class AbstractTestRDFS_Extra
    extends AbstractTestRDFS_Find
{
    public AbstractTestRDFS_Extra(String testLabel) {
        super(testLabel);
    }

    /** Some RDFS reasoners so far produce duplicates which fail cardinality tests. */
    @Override
    protected boolean defaultCompareAsSet() {
        return true;
    }

    @TestFactory
    @Disabled("Fails on certain patterns - Needs investigation.")
    public List<DynamicTest> testSubPropertyOfRdfType01() {
        List<DynamicTest> tests = prepareRdfsFindTestsSSE(
            "(graph (:directType rdfs:subPropertyOf rdf:type) )",
            "(graph (:fido :directType :Dog) )"
        ).build();
        return tests;
    }

    @TestFactory
    public List<DynamicTest> testSubClassOf01() {
        List<DynamicTest> tests = prepareRdfsFindTestsSSE(
            "(graph (:Dog rdfs:subClassOf rdf:Mammal) )",
            "(graph (:fido rdf:type :Dog) )"
        ).build();
        return tests;
    }

    @TestFactory
    public List<DynamicTest> testRange01() {
        List<DynamicTest> tests = prepareRdfsFindTestsSSE(
            "(graph (:owner rdfs:range :Person) )",
            "(graph (:fido :owner :alice) )"
        ).build();
        return tests;
    }

    @TestFactory
    public List<DynamicTest> testRangeWithLiteral01() {
        List<DynamicTest> tests = prepareRdfsFindTestsSSE(
            "(graph (:name rdfs:range :Literal) )",
            "(graph (:fido :name 'Fido') )"
        ).build();
        return tests;
    }

    @TestFactory
    public List<DynamicTest> testDomain01() {
        List<DynamicTest> tests = prepareRdfsFindTestsSSE(
            "(graph (:owner rdfs:domain :Pet) )",
            "(graph (:fido :owner :alice) )"
        ).build();
        return tests;
    }
}
