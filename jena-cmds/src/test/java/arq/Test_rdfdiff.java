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

package arq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Test_rdfdiff {

    private static final String DIR = "testing/diff/";

    @Test
    public void testQuadsEqualRelabelledBnodes() {
        int code = rdfdiff.exec("--quads", DIR + "dataset1.nq", DIR + "dataset1copy.nq");
        assertEquals(0, code, "Identical datasets with relabelled blank nodes should be equal (exit 0)");
    }

    @Test
    public void testQuadsUnequalDifferentQuads() {
        int code = rdfdiff.exec("--quads", DIR + "dataset1.nq", DIR + "dataset2.nq");
        assertEquals(1, code, "Different datasets should be unequal (exit 1)");
    }

    @Test
    public void testQuadsTrig() {
        int code = rdfdiff.exec("--quads", DIR + "dataset1.trig", DIR + "dataset2.trig");
        assertEquals(1, code, "Different TriG datasets should be unequal (exit 1)");
    }

    @Test
    public void testTriplesEqual() {
        int code = rdfdiff.exec(DIR + "dataset1.nq", DIR + "dataset1copy.nq", "N-QUADS", "N-QUADS");
        assertEquals(0, code, "Triple mode on identical default graph should be equal (exit 0)");
    }

    @Test
    public void testUsageOnMissingArgs() {
        int code = rdfdiff.exec("--quads");
        assertEquals(-1, code, "Missing arguments should result in error (exit -1)");
    }
}
