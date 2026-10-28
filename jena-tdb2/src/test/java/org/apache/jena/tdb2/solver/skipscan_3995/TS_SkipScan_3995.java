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

package org.apache.jena.tdb2.solver.skipscan_3995;

import org.apache.jena.tdb2.solver.skipscan.Test05_EqualityLinkArrayOverflow;
import org.apache.jena.tdb2.solver.skipscan.Test07_ResidualConditionsUnreachable;
import org.apache.jena.tdb2.solver.skipscan.Test09_NonRecordIndexGuard;
import org.apache.jena.tdb2.solver.skipscan.Test10_RemapValueConditions;
import org.apache.jena.tdb2.solver.skipscan.Test13_OpExtSkipScanContract;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Review tests for <a href="https://github.com/apache/jena/pull/3995">PR #3995</a>
 * (GH-3994: Improved TDB2 index access for single pattern queries).
 *
 * <p>One class per review finding, numbered to match. Each test states the behaviour the code
 * should have, so <strong>these tests fail against the PR as it stands</strong> - each failure
 * is the finding. Two exceptions, noted in their own javadoc, pass today and are guards:
 * {@link Test11_KeyPrefixLengthValidation#distinctWithNoProjectedVariables()} and
 * {@link Test15_UnusedCodeAndWeakAssertion#distinctOverRandIsNotCollapsed()}.</p>
 *
 * <p>Most of the query-level tests run the same query twice over the same TDB2 database, with
 * {@code SystemTDB.symSkipScan} off and on, and require the answers to agree - see
 * {@link SkipScanTestLib#assertSkipScanAgrees}. They also require that the skip scan was
 * actually engaged, so none of them can pass vacuously.</p>
 */
@Suite
@SelectClasses({
    Test01_DistinctPlacementProjection.class
    , Test02_InputBindingDropped.class
    , Test03_CacheKeySubstitution.class
    , Test04_NodeIdMaxUpperBound.class
    , Test05_EqualityLinkArrayOverflow.class
    , Test06_RecordPadRightUpperBound.class
    , Test07_ResidualConditionsUnreachable.class
    , Test08_PatternQueryCacheKey.class
    , Test09_NonRecordIndexGuard.class
    , Test10_RemapValueConditions.class
    , Test11_KeyPrefixLengthValidation.class
    , Test12_PrefixIteratorRange.class
    , Test13_OpExtSkipScanContract.class
    , Test14_AddResourceObjectsAsSubjects.class
})
public class TS_SkipScan_3995 {
}
