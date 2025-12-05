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

package org.apache.jena.graph.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import org.apache.jena.memvalue.TestNodeToTriplesMapMem;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TestFindLiterals.class,
    TestLiteralLabels.class,
    TestLiteralLabelSameValueAs.class,
    TestNode.class,
    TestNodeCreateStrings.class,
    TestTriple.class,
    TestTripleField.class,
    TestNodeToTriplesMapMem.class,
    TestReifier.class,
    TestTypedLiterals.class,
    TestDateTime.class,
    TestFactory.class,
    TestGraph.class,
    TestGraphPlain.class,
    TestCoreGraphUtil.class,
    TestGraphPrefixMapping.class,
    TestGraphMatchWithInference.class,
    TestGraphEvents.class,
    TestGraphBaseToString.class,
    TestNodeExtras.class,
    TestRDFStringLiterals.class,
    TestNodeEdgeCases.class,

    // Has to be in a specific package.
    org.apache.jena.graph.TestGraphUtil.class

})

public class TS3_graph { }
