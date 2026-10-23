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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.Bytes;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.tdb2.store.DatasetGraphTDB;

/**
 * Finding 6 - {@code Record:211}.
 *
 * <pre>{@code
 * private static byte[] padRight(byte[] arr, int length) {
 *     if ( length == arr.length )      return arr;
 *     else if ( length < arr.length )  return Arrays.copyOf(arr, arr.length);
 *     return Arrays.copyOf(arr, length);
 * }
 * }</pre>
 * <p>Zero-padding is right for a lower bound and wrong for an upper bound, and
 * {@code BPTreeDistinctKeyPrefixIterator.create} uses the one method for both:</p>
 * <pre>{@code
 * Record paddedMinRecord = Record.padRight(minRecord, nodeMinRecord);
 * Record paddedMaxRecord = Record.padRight(maxRecord, nodeMaxRecord);
 * }</pre>
 */
public class Test06_RecordPadRightUpperBound {

    private static final String DATA = """
        :s1 :p  :o1 .
        :s1 :p  :o2 .
        :s2 :p2 :o3 .
        """;

    /**
     * A padded upper bound must still bound every record that shares its prefix.
     * Zero-padding produces the smallest such record instead of the largest.
     */
    @Test
    public void paddedUpperBoundStillBounds() {
        RecordFactory factory = new RecordFactory(3, 0);
        Record prototype = factory.create(new byte[] {0, 0, 0});

        Record partialMax = new Record(new byte[] {0x05}, null);
        Record paddedMax = Record.padRight(partialMax, prototype, (byte)0xff);

        // A stored record with the same leading byte: it must not sort above the upper bound.
        Record stored = factory.create(new byte[] {0x05, 0x00, 0x7F});

        assertTrue(Arrays.compareUnsigned(paddedMax.getKey(), stored.getKey()) >= 0,
                   () -> "Padding an upper bound with zeros excludes records inside the range."
                       + "\n  Upper bound: " + Bytes.asHex(paddedMax.getKey())
                       + "\n  Stored:      " + Bytes.asHex(stored.getKey()));
    }

    /** "If no padding is needed then record is returned" - but a shorter prototype forces a copy. */
    @Test
    public void noPaddingNeededReturnsTheSameRecord() {
        Record record = new RecordFactory(3, 0).create(new byte[] {1, 2, 3});
        Record shorterPrototype = new RecordFactory(1, 0).create(new byte[] {0});
        assertSame(record, Record.padRight(record, shorterPrototype, (byte)0xff),
                   "padRight created a new Record although no padding was needed");
    }

    /**
     * End to end: every needed slot is a constant, so the skip scan builds a 1-slot range over a
     * 3-slot key. The zero-padded upper bound makes the range empty.
     */
    @Test
    public void distinctWithConstantSubjectOnly() {
        DatasetGraphTDB dsg = tdb2(DATA);
        assertSkipScanAgrees(dsg, "SELECT DISTINCT ?zz { :s1 ?p ?o }");
    }
}
