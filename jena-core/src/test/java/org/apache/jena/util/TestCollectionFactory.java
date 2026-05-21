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

package org.apache.jena.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.apache.jena.test.JenaTestLib;
import org.junit.jupiter.api.Test;

public class TestCollectionFactory {

    @Test
    public void testHashMapExists() {
        JenaTestLib.assertInstanceOf(Map.class, CollectionFactory.createHashedMap());
    }

    @Test
    public void testHashMapSized() {
        JenaTestLib.assertInstanceOf(Map.class, CollectionFactory.createHashedMap(42));
    }

    @Test
    public void testHashMapCopy() {
        Map<String, String> map = new HashMap<>();
        map.put("here", "Bristol");
        map.put("there", "Oxford");
        Map<String, String> copy = CollectionFactory.createHashedMap(map);
        assertEquals(map, copy);
    }

    @Test
    public void testHashSetExists() {
        JenaTestLib.assertInstanceOf(Set.class, CollectionFactory.<Object>createHashedSet());
    }

    @Test
    public void testHashSetCopy() {
        Set<String> s = new HashSet<>();
        s.add("jelly");
        s.add("concrete");
        Set<String> copy = CollectionFactory.createHashedSet(s);
        assertEquals(s, copy);
    }

}
