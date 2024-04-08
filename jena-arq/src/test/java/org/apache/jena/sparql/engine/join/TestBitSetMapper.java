/**
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

package org.apache.jena.sparql.engine.join;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Assert;
import org.junit.Test;

public class TestBitSetMapper {

    /** Degenerate case: Binding with no variables. */
    @Test
    public void test01() {
        testRoundtrip(Arrays.asList("x", "y", "z"), "(row )", Arrays.asList());
    }

    /** Degenerate case: Join key with no variables. */
    @Test
    public void test02() {
        testRoundtrip(Arrays.asList(), "(row (?x 0) (?y 1) (?z 2))", Arrays.asList());
    }

    /** Degenerate case: Empty intersection between variables of the join key and the binding. */
    @Test
    public void test03() {
        testRoundtrip(Arrays.asList("x", "y", "z"), "(row (?a 0) (?b 1) (?c 2))", Arrays.asList());
    }

    @Test
    public void test04() {
        testRoundtrip(Arrays.asList("x", "y", "z"), "(row (?x 0) (?y 1) (?z 2))", Arrays.asList("x", "y", "z"));
    }

    @Test
    public void test05() {
        testRoundtrip(Arrays.asList("x", "y", "z"), "(row (?y 1) (?z 2))", Arrays.asList("y", "z"));
    }

    /** Variables that do not occur in the join key must be omitted. */
    @Test
    public void test06() {
        testRoundtrip(Arrays.asList("x", "y", "z"), "(row (?y 1) (?w 2))", Arrays.asList("y"));
    }

    /** Create a bit representation of the variables common to the join key and the binding.
     *  Converting the bit set back to the variable list is expected to yield the list of common variables. */
    private static void testRoundtrip(List<String> joinKeyVarNames, String bindingSse, List<String> expectedVarNames) {
        JoinKey joinKey = JoinKey.create(Var.varList(joinKeyVarNames));
        Binding binding = SSE.parseBinding(bindingSse);

        BitSet bitSet = BitSetMapper.toBitSet(joinKey, binding);

        List<Var> expectedVars = Var.varList(expectedVarNames);
        List<Var> actualVars = BitSetMapper.toList(joinKey, bitSet);

        Assert.assertEquals(expectedVars, actualVars);
    }
}
