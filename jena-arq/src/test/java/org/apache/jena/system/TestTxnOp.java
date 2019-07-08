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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.TxnType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)   
public class TestTxnOp {
    
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        List<Object[]> tests = new ArrayList<>();
        
        tests.add(test(true,   TxnType.READ,   TxnType.WRITE));
        tests.add(test(true,   TxnType.READ,   TxnType.READ_COMMITTED_PROMOTE));
        tests.add(test(true,   TxnType.READ,   TxnType.READ_PROMOTE));
        tests.add(test(true,   TxnType.READ,   TxnType.READ));
    
        tests.add(test(true,   TxnType.READ_PROMOTE,   TxnType.WRITE));
        tests.add(test(false,  TxnType.READ_PROMOTE,   TxnType.READ_COMMITTED_PROMOTE));
        tests.add(test(true,   TxnType.READ_PROMOTE,   TxnType.READ_PROMOTE));
        tests.add(test(false,  TxnType.READ_PROMOTE,   TxnType.READ));
    
        tests.add(test(true,   TxnType.READ_COMMITTED_PROMOTE,   TxnType.WRITE));
        tests.add(test(true,   TxnType.READ_COMMITTED_PROMOTE,   TxnType.READ_COMMITTED_PROMOTE));
        tests.add(test(false,  TxnType.READ_COMMITTED_PROMOTE,   TxnType.READ_PROMOTE));
        tests.add(test(false,  TxnType.READ_COMMITTED_PROMOTE,   TxnType.READ));
    
        tests.add(test(true,   TxnType.WRITE,   TxnType.WRITE));
        tests.add(test(false,  TxnType.WRITE,   TxnType.READ_COMMITTED_PROMOTE));
        tests.add(test(false,  TxnType.WRITE,   TxnType.READ_PROMOTE));
        tests.add(test(false,  TxnType.WRITE,   TxnType.READ));
        return tests;
    }

    private static Object[] test(boolean expected, TxnType innerTxnType, TxnType outerTxnType) {
        String name = innerTxnType.name()+" < "+outerTxnType.name();
        return new Object[] {name, expected, innerTxnType, outerTxnType}; 
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
        assertEquals(name, expected, TxnOp.isTxnTypeCompatible(innerTxnType, outerTxnType));
    }
}
