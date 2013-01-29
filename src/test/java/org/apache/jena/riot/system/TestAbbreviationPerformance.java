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

package org.apache.jena.riot.system;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for performance of {@link LightweightPrefixMap} implementations
 * 
 */
public class TestAbbreviationPerformance {
    
    /**
     * Compares the performance of looking up every namespace 1000 times
     * @param normal PrefixMap
     * @param fast FastAbbreviatingPrefixMap
     * @param namespaces Number of namespaces
     * @param fastShouldWin Whether the FastAbbreviatingPrefixMap should outperform the PrefixMap
     */
    private void test_amalgamated_performance(PrefixMap normal, FastAbbreviatingPrefixMap fast, int namespaces, boolean fastShouldWin) {
        long nPerf = 0, fPerf = 0;
        
        for (int i = 1; i <= namespaces; i++) {
            String input = "http://example/ns" + i + "#x";
            String expected = "ns" + i + ":x";
            nPerf += run(normal, input, expected, 1000);
            fPerf += run(fast, input, expected, 1000);
        }
        
        System.out.println("PrefixMap performance: " + nPerf + "ns");
        System.out.println("Fast Prefix Map performance: " + fPerf + "ns");

        if (fastShouldWin) {
            if (fPerf > nPerf)
                Assert.fail("Expected FastAbbreviatingPrefixMap to outperform PrefixMap");
        } else {
            if (nPerf > fPerf)
                Assert.fail("Expected PrefixMap to outperform FastAbbreviatingPrefixMap");
        }
    }

    private long run(LightweightPrefixMap pmap, String input, String expected, int runs) {
        long start = System.nanoTime();
        for (int i = 1; i <= runs; i++) {
            String x = pmap.abbreviate(input);
            Assert.assertEquals(expected, x);
        }
        return System.nanoTime() - start;
    }

    private void populate(LightweightPrefixMap pmap, int count) {
        for (int i = 1; i <= count; i++) {
            pmap.add("ns" + i, "http://example/ns" + i + "#");
        }
    }

    /**
     * Expect {@link PrefixMap} to outperform {@link FastAbbreviatingPrefixMap} when there
     * is a single namespace
     */
    @Test
    public void prefixMap_abbrev_performance_01() {
        PrefixMap pmap = new PrefixMap();
        populate(pmap, 1);
        FastAbbreviatingPrefixMap fmap = new FastAbbreviatingPrefixMap();
        populate(fmap, 1);

        test_amalgamated_performance(pmap, fmap, 1, false);
    }

    /**
     * Expect {@link FastAbbreviatingPrefixMap} to outperform {@link PrefixMap} as soon as
     * there are a few namespaces
     */
    @Test
    public void prefixMap_abbrev_performance_02() {
        PrefixMap pmap = new PrefixMap();
        populate(pmap, 5);
        FastAbbreviatingPrefixMap fmap = new FastAbbreviatingPrefixMap();
        populate(fmap, 5);

        test_amalgamated_performance(pmap, fmap, 5, true);
    }

    /**
     * Expect {@link FastAbbreviatingPrefixMap} to significantly outperform
     * {@link PrefixMap} once there are a good number of namespaces
     */
    @Test
    public void prefixMap_abbrev_performance_03() {
        PrefixMap pmap = new PrefixMap();
        populate(pmap, 20);
        FastAbbreviatingPrefixMap fmap = new FastAbbreviatingPrefixMap();
        populate(fmap, 20);

        test_amalgamated_performance(pmap, fmap, 20, true);
    }
    
    /**
     * Expect {@link FastAbbreviatingPrefixMap} to significantly outperform
     * {@link PrefixMap} once there are a good number of namespaces
     */
    @Test
    public void prefixMap_abbrev_performance_04() {
        PrefixMap pmap = new PrefixMap();
        populate(pmap, 100);
        FastAbbreviatingPrefixMap fmap = new FastAbbreviatingPrefixMap();
        populate(fmap, 100);

        test_amalgamated_performance(pmap, fmap, 100, true);
    }
}
