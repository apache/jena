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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.apache.jena.tdb2.solver.skipscan_3995.SkipScanTestLib.assertSkipScanAgrees;
import static org.apache.jena.tdb2.solver.skipscan_3995.SkipScanTestLib.tdb2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeFactory;
import org.junit.jupiter.api.Test;

/** Half-open prefix ranges must agree across shortcuts, traversal and leaf scans. */
public class Test12_PrefixIteratorRange {
    private static final RecordFactory RF = new RecordFactory(4, 0);

    private static Record record(int key) {
        return RF.create(Bytes.packInt(key));
    }

    private static BPlusTree tree(int order, List<Integer> keys) {
        BPlusTree tree = BPlusTreeFactory.makeMem(order, 2, 4, 0);
        tree.nonTransactional();
        keys.forEach(k -> tree.insert(record(k)));
        return tree;
    }

    private static List<Integer> prefixes(BPlusTree tree, int length, Record min, Record max) {
        List<Integer> result = new ArrayList<>();
        tree.distinctByKeyPrefix(length, min, max).forEachRemaining(r -> {
            byte[] prefix = new byte[4];
            System.arraycopy(r.getKey(), 0, prefix, 0, length);
            result.add(Bytes.getInt(prefix));
        });
        return result;
    }

    @Test
    public void exclusiveShortUpperBound() {
        BPlusTree tree = tree(4, List.of(0, 256));
        assertEquals(List.of(0), prefixes(tree, 3,
                RF.create(new byte[] {0, 0, 0}), RF.create(new byte[] {0, 0, 1})));
    }

    @Test
    public void suffixesDoNotAffectBounds() {
        assertEquals(List.of(0), prefixes(tree(4, List.of(0)), 3, record(1), null));
        BPlusTree tree = tree(4, List.of(0, 256));
        assertEquals(List.of(0, 256), prefixes(tree, 3, record(1), null));
        assertEquals(List.of(0), prefixes(tree, 3, null, record(257)));
        assertEquals(List.of(), prefixes(tree, 3, record(0), record(255)));
        assertEquals(List.of(), prefixes(tree, 3, record(512), record(0)));
    }

    @Test
    public void rangesAcrossTreeShapes() {
        List<Integer> keys = new ArrayList<>();
        for (int group = 0; group < 40; group++) {
            for (int member = 0; member < 24; member++)
                keys.add((group << 8) | member);
        }
        // Exercise unsigned ordering as well as the highest possible key.
        keys.add(0x80000000);
        keys.add(0xffffff00);
        keys.add(0xffffffff);
        for (int order : new int[] {2, 3, 4}) {
            BPlusTree tree = tree(order, keys);
            for (int length : new int[] {0, 1, 2, 3, 4}) {
                List<Record> bounds = new ArrayList<>();
                bounds.add(null);
                for (int key : new int[] {0, 1, 255, 256, 257, 2048, 4095, 9999,
                        0x80000000, 0xffffff00, 0xffffffff}) {
                    bounds.add(record(key));
                    bounds.add(RF.create(Arrays.copyOf(record(key).getKey(), length)));
                }
                for (Record min : bounds) {
                    for (Record max : bounds) {
                        List<Integer> expected = new ArrayList<>();
                        for (int key : keys) {
                            byte[] bytes = record(key).getKey();
                            if ((min == null || Arrays.compareUnsigned(bytes, 0, length, min.getKey(), 0, length) >= 0)
                                    && (max == null || Arrays.compareUnsigned(bytes, 0, length, max.getKey(), 0, length) < 0)) {
                                byte[] prefix = new byte[4];
                                System.arraycopy(bytes, 0, prefix, 0, length);
                                int value = Bytes.getInt(prefix);
                                if (expected.isEmpty() || expected.get(expected.size() - 1) != value)
                                    expected.add(value);
                            }
                        }
                        assertEquals(expected, prefixes(tree, length, min, max),
                                "order=" + order + " length=" + length + " min=" + min + " max=" + max);
                    }
                }
            }
        }
    }

    @Test
    public void unboundedMaximumAndZeroLengthPrefix() {
        BPlusTree tree = tree(2, List.of(0, -1));
        assertNull(Record.incrementKeyOrNull(record(-1)));
        assertEquals(List.of(-1), prefixes(tree, 4, record(-1), null));
        assertEquals(List.of(0), prefixes(tree, 0, null, null));
        assertEquals(List.of(0), prefixes(tree, 0, RF.create(new byte[0]), null));
        assertEquals(List.of(), prefixes(tree, 0, null, RF.create(new byte[0])));
        assertEquals(List.of(), prefixes(tree(2, List.of()), 0, null, null));
    }

    @Test
    public void invalidBoundsAndLengths() {
        for (BPlusTree tree : List.of(tree(2, List.of()), tree(2, List.of(0)), tree(2, List.of(0, 256)))) {
            assertThrows(IllegalArgumentException.class, () -> tree.distinctByKeyPrefix(-1));
            assertThrows(IllegalArgumentException.class, () -> tree.distinctByKeyPrefix(5));
            assertThrows(IllegalArgumentException.class,
                    () -> tree.distinctByKeyPrefix(3, RF.create(new byte[2]), null));
            assertThrows(IllegalArgumentException.class,
                    () -> tree.distinctByKeyPrefix(3, null, RF.create(new byte[2])));
        }
    }

    @Test
    public void adjacentInlineLiteralsDoNotLeakAcrossExclusiveBound() {
        var dsg = tdb2(":s1 :p 1 . :s2 :q 2 .");
        try {
            assertSkipScanAgrees(dsg, "SELECT DISTINCT ?zz { ?s ?p 0 }");
            assertSkipScanAgrees(dsg, "SELECT DISTINCT ?zz { ?s ?p 1 }");
        } finally {
            dsg.close();
        }
    }
}
