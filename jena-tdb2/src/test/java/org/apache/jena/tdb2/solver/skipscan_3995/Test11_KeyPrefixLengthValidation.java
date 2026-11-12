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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.test.RecordLib;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeFactory;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Finding 11 - {@code BPTreeDistinctKeyPrefixIterator:61}.
 *
 * <p>The 2-argument {@code create} validates the prefix length:</p>
 * <pre>{@code
 * if ( keyPrefixLength < 1 )
 *     throw new IllegalArgumentException("keyPrefixLength must be >= 1");
 * else if ( keyPrefixLength > node.params.recordFactory.keyLength() )
 *     throw new IllegalArgumentException(...);
 * }</pre>
 * <p>The new 4-argument overload added by this PR drops both checks. With a length of 0 every
 * {@code Arrays.compare} over the prefix returns 0, so {@code haveSamePrefix} and
 * {@code comparePrefix} are vacuously true; with a length beyond the key, the comparisons read
 * past the prefix.</p>
 *
 * <p>See {@link #distinctWithNoProjectedVariables()}: the skip scan executor really does pass 0,
 * so this validation and the caller have to be fixed together.</p>
 */
public class Test11_KeyPrefixLengthValidation {

    private static BPlusTree tree() {
        BPlusTree bpt = BPlusTreeFactory.makeMem(2, 2, RecordLib.TestRecordLength, 0);
        bpt.nonTransactional();
        for ( int i = 0; i < 20; i++ )
            bpt.insert(RecordLib.intToRecord(i));
        return bpt;
    }

    @Test
    @Disabled("A range of 0 should gracefully match all records.")
    public void prefixLengthZeroIsRejected() {
        BPlusTree bpt = tree();
        Record min = RecordLib.intToRecord(0);
        Record max = RecordLib.intToRecord(19);
        assertThrows(IllegalArgumentException.class, () -> bpt.distinctByKeyPrefix(0, min, max),
                     "The ranged overload accepts a prefix length of 0");
    }

    @Test
    public void prefixLengthBeyondTheKeyIsRejected() {
        BPlusTree bpt = tree();
        Record min = RecordLib.intToRecord(0);
        Record max = RecordLib.intToRecord(19);
        assertThrows(IllegalArgumentException.class,
                     () -> bpt.distinctByKeyPrefix(RecordLib.TestRecordLength + 1, min, max),
                     "The ranged overload accepts a prefix length beyond the key length");
    }

    /**
     * {@code SELECT DISTINCT ?zz { ?s ?p ?o }} has no needed slots, so the executor calls
     * {@code bpt.distinctByKeyPrefix(0, min, max)}. It currently returns the right answer only
     * because a zero-length prefix makes every comparison vacuously true. This test passes today
     * and guards the degenerate case once the validation above is restored.
     */
    @Test
    public void distinctWithNoProjectedVariables() {
        DatasetGraphTDB dsg = tdb2("""
            :s1 :p :o1 .
            :s1 :p :o2 .
            """);
        assertSkipScanAgrees(dsg, "SELECT DISTINCT ?zz { ?s ?p ?o }");
    }
}
