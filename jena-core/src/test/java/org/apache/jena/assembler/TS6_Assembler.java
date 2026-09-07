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

package org.apache.jena.assembler;

import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import org.apache.jena.test.JenaTestLib;

@Suite
@SelectClasses({
    TestMode.class,
    TestModelExpansion.class,
    TestImportManager.class,
    TestOntModelAcceptance.class,
    TestRuleSet.class,
    TestAssemblerHelp.class,
    TestDefaultModelAssembler.class,
    TestMemoryModelAssembler.class,
    TestAssemblerVocabulary.class,
    TestRuleSetAssembler.class,
    TestInfModelAssembler.class,
    TestAssemblerGroup.class,
    TestAssemblerGroupTracing.class,
    TestReasonerFactoryAssembler.class,
    TestContentAssembler.class,
    TestModelContent.class,
    TestUnionModelAssembler.class,
    TestPrefixMappingAssembler.class,
    TestBuiltinAssemblerGroup.class,
    TestModelAssembler.class,
    TestDocumentManagerAssembler.class,
    TestOntModelSpecAssembler.class,
    TestOntModelAssembler.class
})

public class TS6_Assembler {
    @BeforeSuite
    public static void beforeSuite() {
        JenaTestLib.setup();
    }
}
