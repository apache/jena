/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
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
     * Compares the performance of looking up a specific namespaces 1000 times
     */
    private void test_performance(PrefixMap normal, FastPrefixMap fast, String input, String expected, boolean fastShouldWin) {
        long nPerf = this.run(normal, input, expected, 1000);
        long fPerf = this.run(fast, input, expected, 1000);

        //System.out.println("PrefixMap performance: " + nPerf + "ns");
        //System.out.println("Fast Prefix Map performance: " + fPerf + "ns");

        if (fastShouldWin) {
            if (fPerf > nPerf)
                Assert.fail("Expected FastPrefixMap to outperform PrefixMap");
        } else {
            if (nPerf > fPerf)
                Assert.fail("Expected PrefixMap to outperform FastPrefixMap");
        }
    }
    
    /**
     * Compares the performance of looking up every namespace 100 times
     * @param normal PrefixMap
     * @param fast FastPrefixMap
     * @param namespaces Number of namespaces
     * @param fastShouldWin Whether the FastPrefixMap should outperform the PrefixMap
     */
    private void test_amalgamated_performance(PrefixMap normal, FastPrefixMap fast, int namespaces, boolean fastShouldWin) {
        long nPerf = 0, fPerf = 0;
        
        for (int i = 1; i <= namespaces; i++) {
            String input = "http://example/ns" + i + "#x";
            String expected = "ns" + i + ":x";
            nPerf += run(normal, input, expected, 100);
            fPerf += run(fast, input, expected, 100);
        }
        
        //System.out.println("PrefixMap performance: " + nPerf + "ns");
        //System.out.println("Fast Prefix Map performance: " + fPerf + "ns");

        if (fastShouldWin) {
            if (fPerf > nPerf)
                Assert.fail("Expected FastPrefixMap to outperform PrefixMap");
        } else {
            if (nPerf > fPerf)
                Assert.fail("Expected PrefixMap to outperform FastPrefixMap");
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
     * Expect {@link PrefixMap} to outperform {@link FastPrefixMap} when there
     * is a single namespace
     */
    @Test
    public void prefixMap_abbrev_performance_01() {
        PrefixMap pmap = new PrefixMap();
        populate(pmap, 1);
        FastPrefixMap fmap = new FastPrefixMap();
        populate(fmap, 1);

        test_performance(pmap, fmap, "http://example/ns1#x", "ns1:x", false);
    }

    /**
     * Expect {@link FastPrefixMap} to outperform {@link PrefixMap} as soon as
     * there are a few namespaces
     */
    @Test
    public void prefixMap_abbrev_performance_02() {
        PrefixMap pmap = new PrefixMap();
        populate(pmap, 2);
        FastPrefixMap fmap = new FastPrefixMap();
        populate(fmap, 2);

        test_performance(pmap, fmap, "http://example/ns2#x", "ns2:x", true);
    }

    /**
     * Expect {@link FastPrefixMap} to outperform {@link PrefixMap} as soon as
     * there are a few namespaces
     */
    @Test
    public void prefixMap_abbrev_performance_03() {
        PrefixMap pmap = new PrefixMap();
        populate(pmap, 5);
        FastPrefixMap fmap = new FastPrefixMap();
        populate(fmap, 5);

        test_performance(pmap, fmap, "http://example/ns5#x", "ns5:x", true);
    }

    /**
     * Expect {@link FastPrefixMap} to significantly outperform
     * {@link PrefixMap} once there are a good number of namespaces
     */
    @Test
    public void prefixMap_abbrev_performance_04() {
        PrefixMap pmap = new PrefixMap();
        populate(pmap, 20);
        FastPrefixMap fmap = new FastPrefixMap();
        populate(fmap, 20);

        test_performance(pmap, fmap, "http://example/ns20#x", "ns20:x", true);
    }
    
    /**
     * Expect {@link FastPrefixMap} to significantly outperform
     * {@link PrefixMap} once there are a good number of namespaces
     */
    @Test
    public void prefixMap_abbrev_performance_05() {
        PrefixMap pmap = new PrefixMap();
        populate(pmap, 100);
        FastPrefixMap fmap = new FastPrefixMap();
        populate(fmap, 100);

        test_amalgamated_performance(pmap, fmap, 100, true);
    }
}
