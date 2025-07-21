/*
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

package org.apache.jena.dboe.trans.bplustree;


import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


/**
 * Run the B+Tree algorithm tests but for each combination of explicit
 * write-in-place / always copy modes.
 */
@ParameterizedClass(name="Node dup={0}, Record dup={1}")
@MethodSource("provideArgs")
public class TestBPTreeModes extends TestBPlusTreeNonTxn
{
    private static Stream<Arguments> provideArgs() {
        List<Arguments> x = List.of
                (Arguments.of(true, true),
                 Arguments.of(true, false),
                 Arguments.of(false, true),
                 Arguments.of(false, false));
        return x.stream();
    }

    public TestBPTreeModes(boolean nodeMode, boolean recordsMode) {
        BPT.promoteDuplicateNodes = nodeMode;
        BPT.promoteDuplicateRecords = recordsMode;
    }

    boolean modeAtStartNodes;
    boolean modeAtStartRecords;

    @BeforeAll public static void setupSuite() {
        BPT.forcePromoteModes = true;
    }

    @AfterAll public static void resetSuite() {
        BPT.forcePromoteModes = false;
    }

    @BeforeEach public void setModes() {
        BPT.forcePromoteModes = true;
        modeAtStartNodes = BPT.promoteDuplicateNodes;
        modeAtStartRecords = BPT.promoteDuplicateRecords;
    }

    @AfterEach public void resetModes() {
        BPT.promoteDuplicateNodes = modeAtStartNodes;
        BPT.promoteDuplicateRecords = modeAtStartRecords;
    }
}
