/**
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

package org.apache.jena.sparql.engine.join;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Assert;
import org.junit.Test;

public class TestMultiHashProbeTable {
    // Binding examples taken from TestVarFinder2
    private static Binding row_xy = SSE.parseBinding("(row (?x 1) (?y 2))");
    private static Binding row_x  = SSE.parseBinding("(row (?x 1))");
    private static Binding row_y  = SSE.parseBinding("(row (?y 2))");
    private static Binding row0   = SSE.parseBinding("(row)");

    private static Binding match_x3    = SSE.parseBinding("(row (?x 3))");
    private static Binding match_x3y2  = SSE.parseBinding("(row (?x 3) (?y 2))");

    // TODO Parameterize with different initial join keys including null

    /** Ensure that further updates are rejected after the first lookup. */
    @Test(expected = IllegalStateException.class)
    public void test01() {
        MultiHashProbeTable table = new MultiHashProbeTable(null, null);
        table.getCandidates(row0);
        table.put(row_xy);
    }

    /** Runs the lookup process repeatedly to check for any non-deterministic behavior,
     *  such as scrambled variable orders in JoinKeys due to use of HashSet where LinkedHashSet
     *  needed to be used. */
    @Test
    public void testLookupProcessRepeated() {
        for (int i = 0; i < 1000; ++i) {
            testLookupProcess();
        }
    }

    /**
     * This test adds 4 rows to a MultiHashProbeTable and performs 2 lookups.
     * The state of the created indexes, main and skew tables is checked.
     */
    @Test
    public void testLookupProcess() {
        List<Binding> givenRowList = List.of(row_xy, row_x, row_y, row0);

        // Set.of() methods may not retain order which breaks tests because the join keys
        // depend on which variables are seen first and JoinKey(?x ?y) and JoinKey(?y ?x)
        // are not equal
        Set<Binding> givenRowSet = new LinkedHashSet<>(givenRowList);

        // Sanity check of the input data - this test case does not deal with duplicates
        Assert.assertEquals(givenRowList.size(), givenRowSet.size());

        MultiHashProbeTable table = new MultiHashProbeTable(null, null);
        givenRowSet.forEach(table::put);
        table.doFinalize();

        // We expect only a table for the initial empty join key
        Assert.assertEquals(Set.of(JoinKey.empty()), table.getIndexesByJoinKeys().keySet());

        // Lookup with empty row should match all 4 rows
        // (We don't test whether implementations preserve order)
        Set<Binding> matchedRows = Iter.toSet(table.getCandidates(row0));
        Assert.assertEquals(givenRowSet, matchedRows);

        JoinKey emptyKey = JoinKey.empty();
        JoinKey xKey = JoinKey.create("x");
        JoinKey yKey = JoinKey.create("y");
        JoinKey xyKey = JoinKey.create("x", "y"); // ISSUE join key order may get scrambled

        {
            // Lookup should match all rows without y
            Set<Binding> actual = Iter.toSet(table.getCandidates(match_x3));
            Assert.assertEquals(Set.of(row_y, row0), actual);

            // The indexes map is not a live-view
            Map<JoinKey, JoinIndex> indexes = table.getIndexesByJoinKeys();

            // There should now be an additional index on x
            Assert.assertEquals(Set.of(emptyKey, xKey),
                    table.getIndexesByJoinKeys().keySet());

            JoinIndex xIndex = indexes.get(xKey);

            // Main table should contain all bindings
            Assert.assertEquals(givenRowSet,
                    Iter.toSet(xIndex.getMainTable().getCandidates(BindingFactory.empty())));

            // There should not be a skew table
            Assert.assertTrue(xIndex.getSkewTables().isEmpty());
        }

        {
            // Lookup with join key (x, y) which does not match rows with x
            Set<Binding> actual = Iter.toSet(table.getCandidates(match_x3y2));
            Assert.assertEquals(Set.of(row_y, row0), actual);

            // There should now be an additional index on y
            Assert.assertEquals(Set.of(emptyKey, xKey, xyKey),
                    table.getIndexesByJoinKeys().keySet());

            // The indexes map is not a live-view
            Map<JoinKey, JoinIndex> indexes = table.getIndexesByJoinKeys();

            // There should now be an additional index on [?x, ?y]
            Assert.assertEquals(Set.of(emptyKey, xKey, xyKey),
                    table.getIndexesByJoinKeys().keySet());

            JoinIndex xyIndex = indexes.get(xyKey);

            // Main table should contain all bindings that either bind xy or neither
            Assert.assertEquals(Set.of(row_xy, row0),
                    Iter.toSet(xyIndex.getMainTable().getCandidates(BindingFactory.empty())));

            // There should be skew tables for the bindings with only ?x and only ?y.
            // Beware: Skew tables are created for all cached bindings that bind fewer variables than
            // the lookup binding - they don't depend on the lookup binding's values.
            Map<JoinKey, HashProbeTable> skewTables = xyIndex.getSkewTablesByJoinKey();
            Assert.assertEquals(Set.of(xKey, yKey), skewTables.keySet());

            Assert.assertEquals(Set.of(row_x), Iter.toSet(skewTables.get(xKey).values()));
            Assert.assertEquals(Set.of(row_y), Iter.toSet(skewTables.get(yKey).values()));
        }
    }
}
