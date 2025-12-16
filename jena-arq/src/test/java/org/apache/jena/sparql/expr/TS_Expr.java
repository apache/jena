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

package org.apache.jena.sparql.expr;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import org.apache.jena.sparql.expr.nodevalue.TestNodeValueSortKey;

@Suite
@SelectClasses({
    TestNVFactory.class
    , TestNodeValue.class
    , TestExpressions.class
    , TestExpressions2.class
    , TestExpressions3.class
    , TestExpressions4.class
    , TestCastXSD.class
    , TestNodeFunctions.class
    , TestExpressionsMath.class
    , TestFunctions.class
    , TestFunctions2.class
    , TestFunctionsByURI.class
    , TestExprTripleTerms.class
    , TestLeviathanFunctions.class
    , TestNodeValueOps.class
    , TestOrdering.class
    , TestComparison.class
    , TestSortOrdering.class
    , TestRegex.class
    , TestXSDFuncOp.class
    , TestExprLib.class
    , TestExprTransform.class
    , TestCustomAggregates.class
    , TestStatisticsAggregates.class
    , TestNodeValueSortKey.class
    , TestExprFunctionOp_NodeTransform.class
    , TestExprFunctionOp_ExprTransform.class
})

public class TS_Expr
{
    // Expected warnings off.
    private static boolean bVerboseWarnings;
    private static boolean bWarnOnUnknownFunction;

    @BeforeAll public static void beforeClass() {
        bVerboseWarnings = NodeValue.VerboseWarnings;
        bWarnOnUnknownFunction = E_Function.WarnOnUnknownFunction;
        NodeValue.VerboseWarnings = false;
        E_Function.WarnOnUnknownFunction = false;
    }

    @AfterAll public static void afterClass() {
        NodeValue.VerboseWarnings = bVerboseWarnings;
        E_Function.WarnOnUnknownFunction = bWarnOnUnknownFunction;
    }
}
