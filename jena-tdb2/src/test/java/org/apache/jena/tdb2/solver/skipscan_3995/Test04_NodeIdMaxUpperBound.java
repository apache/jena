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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Arrays;

import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.test.RecordLib;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeFactory;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.junit.jupiter.api.Test;

/**
 * Finding 4 - {@code NodeIdUtils:35}.
 *
 * <pre>{@code
 * private static final NodeId NodeIdMax = NodeId.createRaw(NodeIdType.SPECIAL, Long.MAX_VALUE);
 * }</pre>
 * <p>encodes to {@code 0xF0FFFFFFFFFFFFFF}, which is not an upper bound over storable NodeIds:
 * inline negative {@code xsd:double} values encode above it. Range upper bounds built with
 * {@code anyToMax} therefore silently exclude real records.</p>
 */
public class Test04_NodeIdMaxUpperBound {

    private static final String DATA = """
        :s1 :p "-1.0E7"^^xsd:double .
        :s1 :p :o1 .
        :s1 :p "abc" .
        """;

    /** A negative double must not disappear from a DISTINCT over the object slot. */
    @Test
    public void distinctObjectIncludesNegativeDouble() {
        DatasetGraphTDB dsg = tdb2(DATA);
        assertSkipScanAgrees(dsg, "SELECT DISTINCT ?o { ?s :p ?o }");
    }

    /**
     * The same through a longer key prefix (SPO rather than POS).
     *
     * <p>Note that {@code SELECT DISTINCT ?p ?o { ?s ?p ?o }} does <em>not</em> show the defect:
     * there the first ranged slot is the predicate, whose NodeId is below {@code NodeIdMax}, so
     * the record sorts inside the range whatever the object is. The bound only cuts records
     * short when the oversized NodeId sits in the first ranged slot.</p>
     */
    @Test
    public void distinctObjectWithConstantSubject() {
        DatasetGraphTDB dsg = tdb2(DATA);
        assertSkipScanAgrees(dsg, "SELECT DISTINCT ?o { :s1 :p ?o }");
    }

    /**
     * An all-0xFF key is the only safe upper bound, so the fix for this finding needs one -
     * but {@link Record#incrementKey} throws on it, and
     * {@code BPTreeDistinctKeyPrefixIterator} computes {@code incrementKey(maxRecord)} eagerly
     * in its constructor. The range iterator must accept a maximal upper bound.
     */
    @Test
    public void rangeIteratorAcceptsMaximalUpperBound() {
        RecordFactory factory = RecordLib.recordFactory;
        BPlusTree bpt = BPlusTreeFactory.makeMem(2, 2, RecordLib.TestRecordLength, 0);
        bpt.nonTransactional();
        for ( int i = 0; i < 20; i++ )
            bpt.insert(RecordLib.intToRecord(i));

        byte[] allFF = new byte[RecordLib.TestRecordLength];
        Arrays.fill(allFF, (byte)0xFF);
        Record min = factory.create(new byte[RecordLib.TestRecordLength]);
        Record max = factory.create(allFF);

        assertDoesNotThrow(() -> bpt.distinctByKeyPrefix(1, min, max),
                           "An all-0xFF upper bound - the only correct maximum - is rejected");
    }
}
