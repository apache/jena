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

package org.apache.jena.util.iterator;

import static junit.framework.TestCase.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.apache.jena.test.JenaTestLib;

public class TestAsCollection {

    @Test
    public void testAsList() {
        testReturnsList("");
        testReturnsList("understanding");
        testReturnsList("understanding is");
        testReturnsList("understanding is a three-edged sword");
    }

    @Test
    public void testAsSet() {
        testReturnsSet("");
        testReturnsSet("x");
        testReturnsSet("x x");
        testReturnsSet("x y x");
        testReturnsSet("a b c d e f a c f x");
        testReturnsSet("the avalanch has already started");
    }

    private Set<String> testReturnsSet(String elements) {
        Set<String> result = JenaTestLib.setOfStrings(elements);
        assertEquals(result, WrappedIterator.create(result.iterator()).toSet());
        return result;
    }

    private void testReturnsList(String elements) {
        List<String> L = JenaTestLib.listOfStrings(elements);
        assertEquals(L, WrappedIterator.create(L.iterator()).toList());
    }
}
