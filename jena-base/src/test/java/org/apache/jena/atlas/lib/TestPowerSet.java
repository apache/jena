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

package org.apache.jena.atlas.lib;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class TestPowerSet {
    @Test
    public void testPowerSet_0() {
        test(Set.of(), Set.of(Set.of()));
    }

    @Test
    public void testPowerSet_1() {
        test(Set.of("1"),
             Set.of(
                    Set.of(), Set.of("1")
                    ));
    }

    @Test
    public void testPowerSet_2() {
        test(Set.of("1", "2"),
             Set.of(
                    Set.of(), Set.of("1"), Set.of("2"), Set.of("1", "2")
                    ));
    }

    @Test
    public void testPowerSet_3() {
        test(Set.of("1", "2", "3"),
             Set.of(
                    Set.of(),
                    Set.of("1"), Set.of("2"),  Set.of("3"),
                    Set.of("1", "2"), Set.of("1", "3"), Set.of("2", "3"),
                    Set.of("1", "2", "3")
                    ));
    }


    private void test(Set<String> input, Set<Set<String>> expected) {
        Set<Set<String>> result = Lib.powerSet(input);
        Assert.assertEquals(expected, result);
    }
}
