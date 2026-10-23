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

import static org.apache.jena.tdb2.solver.skipscan_3995.SkipScanTestLib.assertSkipScanAgrees;
import static org.apache.jena.tdb2.solver.skipscan_3995.SkipScanTestLib.tdb2;

import org.junit.jupiter.api.Test;

import org.apache.jena.tdb2.store.DatasetGraphTDB;

/**
 * Finding 3 - {@code OpExecutorTDB2SkipScan:138}.
 *
 * <p>The index cache is keyed on the <em>substituted</em> pattern but the candidate is computed
 * from the <em>unsubstituted</em> one:</p>
 * <pre>{@code
 * PatternQuery subst = PatternQuery.substitute(patternQuery, binding);
 * PatternQuery cacheKey = PatternQuery.toCacheKey(subst);
 * SkipScanCandidate bestCandidate = indexCache.computeIfAbsent(cacheKey,
 *     cq -> findBestCandidate(patternQuery, execCxt));   // not subst
 * }</pre>
 * <p>so the IndexMatch (key length, value length, residual conditions, canDoDirectDistinct)
 * describes a different pattern from the one executed. The constant substituted in from the
 * binding ends up neither in the key prefix nor in the residual conditions, so it is never
 * applied, and a stale {@code canDoDirectDistinct} suppresses the de-duplication.</p>
 */
public class Test03_CacheKeySubstitution {

    private static final String DATA = """
        :s1 :p  :o1 .
        :s1 :p  :o2 .
        :s2 :p2 :o1 .
        :s3 :p3 :o9 .
        :a  :q  :o1 .
        """;

    /**
     * {@code ?o} is bound to {@code :o1} by the outer pattern before the sub-select runs, so the
     * executed pattern is {@code (?s ?p :o1)} while the plan was made for {@code (?s ?p ?o)}.
     */
    @Test
    public void substitutedConstantIsApplied() {
        DatasetGraphTDB dsg = tdb2(DATA);
        assertSkipScanAgrees(dsg, """
            SELECT * { ?x :q ?o { SELECT DISTINCT ?p ?o { ?s ?p ?o } } }
            """);
    }

    /** The same shape with the subject substituted instead. */
    @Test
    public void substitutedSubject() {
        DatasetGraphTDB dsg = tdb2(DATA);
        assertSkipScanAgrees(dsg, """
            SELECT * { VALUES ?s { :s1 :s2 } { SELECT DISTINCT ?s ?p { ?s ?p ?o } } }
            """);
    }
}
