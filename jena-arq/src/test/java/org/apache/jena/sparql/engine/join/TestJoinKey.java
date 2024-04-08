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
import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.junit.Assert;
import org.junit.Test;

public class TestJoinKey {

    public void testEmptyKey() {
        Assert.assertEquals(JoinKey.create(), JoinKey.empty());
        Assert.assertTrue(JoinKey.create() == JoinKey.empty());
    }

    /** Join key building is expected to order elements by their first occurrence and drop duplicates. */
    @Test
    public void testDuplicates01() {
        JoinKey expected = JoinKey.create("a");
        JoinKey actual = JoinKey.create("a", "a", "a");
        Assert.assertEquals(expected, actual);
    }

    /** Join key building is expected to order elements by their first occurrence and drop duplicates. */
    @Test
    public void testDuplicates02() {
        JoinKey expected = JoinKey.create("c", "a", "b");
        JoinKey actual = JoinKey.create("c", "a", "a", "b", "a", "c", "b");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testIndexOf_small() {
        testIndexOf("a", "b", "c");
    }

    /** The {@link JoinKey} implementation creates an extra var-to-index mapping
     *  when the number of variables exceeds a threshold;
     *  by default {@link ImmutableUniqueList#INDEX_THRESHOLD}.
     *  Here we check whether indexOf still works as expected. */
    @Test
    public void testIndexOf_large() {
        testIndexOf("a", "b", "c", "d", "e", "f", "g");
    }

    /** Tests the join key's indexOf method.
     *  The index of each var in the join key must match the position in the argument. */
    private static void testIndexOf(String... varNames) {
        List<Var> vars = Var.varList(Arrays.asList(varNames));
        JoinKey joinKey = JoinKey.create(vars);
        Assert.assertEquals(vars.size(), joinKey.length());
        for (int i = 0; i < joinKey.length(); ++i) {
            Var v = vars.get(i);
            int index = joinKey.indexOf(v);
            Assert.assertEquals(i, index);
        }
    }
}
