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

package org.apache.jena.sparql;

import org.apache.jena.arq.junit.manifest.Manifests;
import org.apache.jena.arq.junit.runners.Label;
import org.apache.jena.arq.junit.runners.RunnerSPARQL;
import org.apache.jena.sparql.expr.E_Function ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith;

/** The test suite for all DAWG (the first SPARQL working group) approved tests. 
 *  Many are the same as or overlap with ARQ tests (because the ARQ ones were 
 *  contributed to DAWG or developed in response the feature design within DAWG)
 *  but we keep this set here as a reference.  
 */
@RunWith(RunnerSPARQL.class)
@Label("DAWG")
@Manifests({
    // One test, dawg-optional-filter-005-simplified or dawg-optional-filter-005-not-simplified
    // must fail because it's the same query and data with different interpretations of the
    // spec.  ARQ implements dawg-optional-filter-005-not-simplified.

    "testing/DAWG-Final/manifest-syntax.ttl",
    "testing/DAWG-Final/manifest-evaluation.ttl",

    "testing/DAWG/Misc/manifest.n3",
    "testing/DAWG/Syntax/manifest.n3",
    "testing/DAWG/examples/manifest.n3",
})

public class Scripts_DAWG
{
    private static boolean bVerboseWarnings;
    private static boolean bWarnOnUnknownFunction;

    @BeforeClass
    public static void beforeClass() {
        bVerboseWarnings = NodeValue.VerboseWarnings;
        bWarnOnUnknownFunction = E_Function.WarnOnUnknownFunction;
        NodeValue.VerboseWarnings = false;
        E_Function.WarnOnUnknownFunction = false;
    }

    @AfterClass
    public static void afterClass() {
        NodeValue.VerboseWarnings = bVerboseWarnings;
        E_Function.WarnOnUnknownFunction = bWarnOnUnknownFunction;
    }
}
