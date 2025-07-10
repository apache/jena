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

package org.apache.jena.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.query.TxnType;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestTxnOp {

    private static Stream<Arguments> provideArgs() {
        List<Arguments> tests = List.of
                (test(true,   TxnType.READ,   TxnType.WRITE),
                 test(true,   TxnType.READ,   TxnType.READ_COMMITTED_PROMOTE),
                 test(true,   TxnType.READ,   TxnType.READ_PROMOTE),
                 test(true,   TxnType.READ,   TxnType.READ),

                 test(true,   TxnType.READ_PROMOTE,   TxnType.WRITE),
                 test(false,  TxnType.READ_PROMOTE,   TxnType.READ_COMMITTED_PROMOTE),
                 test(true,   TxnType.READ_PROMOTE,   TxnType.READ_PROMOTE),
                 test(false,  TxnType.READ_PROMOTE,   TxnType.READ),

                 test(true,   TxnType.READ_COMMITTED_PROMOTE,   TxnType.WRITE),
                 test(true,   TxnType.READ_COMMITTED_PROMOTE,   TxnType.READ_COMMITTED_PROMOTE),
                 test(false,  TxnType.READ_COMMITTED_PROMOTE,   TxnType.READ_PROMOTE),
                 test(false,  TxnType.READ_COMMITTED_PROMOTE,   TxnType.READ),

                 test(true,   TxnType.WRITE,   TxnType.WRITE),
                 test(false,  TxnType.WRITE,   TxnType.READ_COMMITTED_PROMOTE),
                 test(false,  TxnType.WRITE,   TxnType.READ_PROMOTE),
                 test(false,  TxnType.WRITE,   TxnType.READ)
                        );
        return tests.stream();
    }

    private static Arguments test(boolean expected, TxnType innerTxnType, TxnType outerTxnType) {
        String name = innerTxnType.name()+" < "+outerTxnType.name();
        return Arguments.of(name, expected, innerTxnType, outerTxnType);
    }

    private String  name;
    private Boolean expected;
    private TxnType innerTxnType;
    private TxnType outerTxnType;

    public TestTxnOp(String name, Boolean expected, TxnType innerTxnType,TxnType outerTxnType) {
        this.name = name;
        this.expected = expected;
        this.innerTxnType = innerTxnType;
        this.outerTxnType = outerTxnType;
    }

    @Test
    public void test() {
        assertEquals(expected, TxnOp.isTxnTypeCompatible(innerTxnType, outerTxnType), name);
    }
}
