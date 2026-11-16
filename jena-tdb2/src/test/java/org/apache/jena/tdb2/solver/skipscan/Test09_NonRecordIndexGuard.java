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

package org.apache.jena.tdb2.solver.skipscan;

import static org.apache.jena.tdb2.solver.skipscan_3995.SkipScanTestLib.tdb2;
import static org.apache.jena.tdb2.solver.skipscan_3995.SkipScanTestLib.tripleIndex;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb2.store.tupletable.TupleIndexWrapper;
import org.junit.jupiter.api.Test;

/**
 * Finding 9 - {@code OpExecutorTDB2SkipScan:306}.
 *
 * <pre>{@code
 * if ( tupleIndex instanceof TupleIndexRecord recordIdx ) {
 *     RangeIndex rangeIndex = recordIdx.getRangeIndex();
 *     if ( !(rangeIndex instanceof BPlusTree) )
 *         return null;
 * }
 * }</pre>
 * <p>An index that is not a {@code TupleIndexRecord} at all does not match the pattern, so it
 * passes the guard - and then {@code exec} does an unchecked cast:</p>
 * <pre>{@code
 * TupleIndexRecord tupleIndexRecord = (TupleIndexRecord)tupleIndex;
 * }</pre>
 */
public class Test09_NonRecordIndexGuard {

    private static final String DATA = """
        :s1 :p :o1 .
        :s1 :p :o2 .
        """;

    /**
     * Either the guard rejects the index, or the candidate must carry something
     * {@code exec} can cast. {@link TupleIndexWrapper} is a {@code TupleIndex} that is not a
     * {@code TupleIndexRecord}.
     */
    @Test
    public void candidateIndexIsCastable() {
        DatasetGraphTDB dsg = tdb2(DATA);
        Var s = Var.alloc("s");
        Var o = Var.alloc("o");
        Var pv = Var.alloc("p");
        Node[] pattern = new Node[] {s, pv, o};
        TuplePatternSpec spec = TuplePatternSpec.create(pattern, List.of(s));

        TupleIndex wrapped = new TupleIndexWrapper(tripleIndex(dsg, "SPO"));
        SkipScanCandidate candidate = OpExecutorTDB2SkipScan.planCandidate(spec, wrapped);
        if ( candidate == null )
            // The guard rejected it, which is one correct outcome.
            return;

        TupleIndex index = candidate.index();
        assertInstanceOf(TupleIndexRecord.class, index,
                         "planCandidate accepted a TupleIndex that is not a TupleIndexRecord; "
                       + "OpExecutorTDB2SkipScan.exec casts to TupleIndexRecord and throws ClassCastException");
    }
}
