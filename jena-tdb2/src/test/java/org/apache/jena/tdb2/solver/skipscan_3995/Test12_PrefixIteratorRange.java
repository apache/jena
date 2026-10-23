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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.test.RecordLib;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Finding 12 - {@code BPTreeDistinctKeyPrefixIterator}: the ranged iterator yields records
 * outside the requested range.
 *
 * <p>Two short-circuits return a page's or subtree's lowest record without checking it against
 * the range. {@code moveOnCurrent:158} has no check at all:</p>
 * <pre>{@code
 * if ( haveSamePrefix(subtreeMin, subtreeMax) )
 *     return Iter.singletonIterator(subtreeMin);
 * }</pre>
 * <p>and the check this PR added at {@code getRecordsIterator:190} compares <em>prefixes</em>,
 * not records:</p>
 * <pre>{@code
 * if ( comparePrefix(lowRecord, minRecord) >= 0 && comparePrefix(highRecord, maxRecord) <= 0 )
 *     iter = Iter.singletonIterator(lowRecord);
 * }</pre>
 * <p>so when {@code lowRecord} and {@code minRecord} share a prefix - exactly the case this
 * shortcut is for - a record below the lower bound passes the test and is emitted. See
 * {@link #singletonPageShortcutRespectsLowerBound()} for the three-line case.</p>
 *
 * <p>{@link #rangedDistinctByPrefix()} is a whole-contract sweep rather than one tree shape:
 * over many sub-ranges it requires exactly one record per distinct key prefix, each inside the
 * requested range. The keys are laid out so that many records share a 3-byte prefix, which is
 * what produces the uniform-prefix leaf pages and subtrees both shortcuts trigger on, and the
 * range boundaries fall inside such a group.</p>
 */
public class Test12_PrefixIteratorRange {

    /** Keys {@code [0, 0, group, member]}: every group of members shares a 3-byte prefix. */
    private static final int GROUPS = 40;
    private static final int MEMBERS = 24;

    private static List<Integer> keys() {
        List<Integer> keys = new ArrayList<>();
        for ( int group = 0; group < GROUPS; group++ ) {
            for ( int member = 0; member < MEMBERS; member++ )
                keys.add((group << 8) | member);
        }
        return keys;
    }

    private static byte[] prefixOf(int key, int prefixLength) {
        return Arrays.copyOf(Bytes.packInt(key), prefixLength);
    }

    /**
     * The smallest case: every record shares one 3-byte prefix, and the range starts one past
     * the lowest. The single record returned must be inside the range.
     */
    @Test
    @Disabled("Test seems invalid: Records are only compared by the first n bytes, where n is the prefix length.")
    public void singletonPageShortcutRespectsLowerBound() {
        BPlusTree bpt = BPlusTreeFactory.makeMem(2, 2, RecordLib.TestRecordLength, 0);
        bpt.nonTransactional();
        for ( int key = 0; key <= 5; key++ )
            bpt.insert(RecordLib.intToRecord(key));

        Iterator<Record> iter = bpt.distinctByKeyPrefix(3, RecordLib.intToRecord(1), RecordLib.intToRecord(5));
        List<Integer> actual = new ArrayList<>();
        while ( iter.hasNext() )
            actual.add(RecordLib.recordToInt(iter.next()));

        assertEquals(List.of(1), actual,
                     "The ranged iterator returned a record below the lower bound");
    }

    @Test
    @Disabled("Test seems invalid; only the first prefixLen bytes are used to compare the records of the bpt against the first prefixLen bytes of the query range [x, y[")
    public void rangedDistinctByPrefix() {
        List<Integer> keys = keys();

        for ( int order : new int[] {2, 3, 4} ) {
            BPlusTree bpt = BPlusTreeFactory.makeMem(order, 2, RecordLib.TestRecordLength, 0);
            bpt.nonTransactional();
            for ( int key : keys )
                bpt.insert(RecordLib.intToRecord(key));

            for ( int prefixLength : new int[] {3, 4} ) {
                // Range boundaries on group boundaries and inside groups.
                for ( int fromGroup = 0; fromGroup < GROUPS; fromGroup += 7 ) {
                    for ( int offset : new int[] {0, 1, MEMBERS / 2, MEMBERS - 1} ) {
                        int from = (fromGroup << 8) | offset;
                        int to = ((fromGroup + 5) << 8) | (MEMBERS - 1 - offset);
                        if ( to < from )
                            continue;
                        check(bpt, order, prefixLength, keys, from, to);
                    }
                }
            }
        }
    }

    private static void check(BPlusTree bpt, int order, int prefixLength, List<Integer> keys, int from, int to) {
        List<String> expected = new ArrayList<>();
        for ( int key : keys.stream().sorted().toList() ) {
            if ( key < from || key > to )
                continue;
            String prefix = Bytes.asHex(prefixOf(key, prefixLength));
            if ( expected.isEmpty() || !expected.get(expected.size() - 1).equals(prefix) )
                expected.add(prefix);
        }

        List<String> actual = new ArrayList<>();
        Record minInc = RecordLib.intToRecord(from);
        Record maxExc = RecordLib.intToRecord(to);
        Iterator<Record> iter = bpt.distinctByKeyPrefix(prefixLength,
                                                        minInc,
                                                        maxExc);
        while ( iter.hasNext() ) {
            Record record = iter.next();
            int key = RecordLib.recordToInt(record);
            assertTrue(key >= from && key <= to,
                       () -> "Record outside the requested range"
                           + " [order=" + order + ", prefixLength=" + prefixLength + "]"
                           + "\n  Range:  [" + from + ", " + to + "]"
                           + "\n  Record: " + key);
            actual.add(Bytes.asHex(prefixOf(key, prefixLength)));
        }

        assertEquals(expected, actual,
                     () -> "Wrong set of distinct prefixes"
                         + " [order=" + order + ", prefixLength=" + prefixLength + "]"
                         + "\n  Range: [" + from + ", " + to + "]");
    }
}
