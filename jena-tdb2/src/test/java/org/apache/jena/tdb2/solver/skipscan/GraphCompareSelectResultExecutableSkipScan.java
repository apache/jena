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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.LongAdder;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExecBuilder;

/** Extension of the GraphCompare executable with verification that skip scan was actually used. */
class GraphCompareSelectResultExecutableSkipScan
    extends GraphCompareSelectResultExecutable {

    private LongAdder skipScanOkCounter = new LongAdder();
    private LongAdder skipScanFailoverCounter = new LongAdder();
    private boolean expectSkipScan;

    public GraphCompareSelectResultExecutableSkipScan(String testLabel, Query query, DatasetGraph referenceDsg,
            DatasetGraph testDsg, boolean expectSkipScan) {
        super(testLabel, query, referenceDsg, testDsg);
        this.expectSkipScan = expectSkipScan;
    }

    @Override
    protected QueryExecBuilder mutateTestExecBuilder(QueryExecBuilder qExec) {
        return qExec
            .set(OpExecutorTDB2SkipScan.symSkipScanOkCounter, skipScanOkCounter)
            .set(OpExecutorTDB2SkipScan.symSkipScanFailoverCounter, skipScanFailoverCounter);
    }

    @Override
    public void execute() {
        super.execute();
        if (expectSkipScan) {
            assertTrue(skipScanOkCounter.longValue() > 0, () -> "Expected skip scan to be used - " + getTestLabel());
            assertEquals(0, skipScanFailoverCounter.longValue(), () -> getTestLabel());
        } else {
            assertTrue(skipScanOkCounter.longValue() == 0, () -> "Expected NO skip scan to be used - " + getTestLabel());
        }
    }
}
