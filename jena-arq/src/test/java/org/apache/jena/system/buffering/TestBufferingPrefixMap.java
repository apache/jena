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

package org.apache.jena.system.buffering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.apache.jena.atlas.lib.StreamOps;
import org.apache.jena.riot.system.PrefixEntry;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestBufferingPrefixMap {

    @Test public void buffering_prefix_01_basic() {
        PrefixMap base = PrefixMapFactory.create();
        PrefixMap pmap = new BufferingPrefixMap(base);
        assertEquals(0, pmap.size());
        assertTrue(pmap.isEmpty());
    }

    @Test public void buffering_prefix_02_base_prefix() {
        // Base has a prefix.
        PrefixMap base = PrefixMapFactory.create();
        base.add("x", "http://example/");
        PrefixMap pmap = new BufferingPrefixMap(base);
        assertEquals(1, pmap.size());
        assertFalse(pmap.isEmpty());
    }

    @Test public void buffering_prefix_03_add() {
        PrefixMap base = PrefixMapFactory.create();

        PrefixMap pmap = new BufferingPrefixMap(base);
        pmap.add("x", "http://example/");
        assertFalse(pmap.isEmpty());
        assertEquals(1, pmap.size());

        assertTrue(base.isEmpty());
        assertEquals(0, base.size());
    }

    @Test public void buffering_prefix_04_base_add() {
        PrefixMap base = PrefixMapFactory.create();
        base.add("x1", "http://example/1#");
        PrefixMap pmap = new BufferingPrefixMap(base);
        pmap.add("x2", "http://example/2#");
        assertEquals(2, pmap.size());
        assertEquals(1, base.size());

    }

    @Test public void buffering_prefix_05_add_remove() {
        PrefixMap base = PrefixMapFactory.create();
        base.add("x", "http://example/");
        PrefixMap pmap = new BufferingPrefixMap(base);
        pmap.delete("x");

        assertTrue(pmap.isEmpty());
        assertFalse(base.isEmpty());

        assertEquals(0, pmap.size());
        assertEquals(1, base.size());
    }

    @Test public void buffering_prefix_06_flush() {
        PrefixMap base = PrefixMapFactory.create();
        base.add("x1", "http://example/1#");
        BufferingPrefixMap pmap = new BufferingPrefixMap(base);
        pmap.add("x2", "http://example/2#");
        assertEquals(2, pmap.size());
        assertEquals(1, base.size());

        pmap.flush();
        assertEquals(2, base.size());
        assertEquals("http://example/2#", base.get("x2"));
    }

    @Test public void buffering_prefix_07_stream() {
        PrefixMap base = PrefixMapFactory.create();
        base.add("x1", "http://example/1#");
        BufferingPrefixMap pmap = new BufferingPrefixMap(base);
        pmap.add("x2", "http://example/2#");
        Set<String> set1 = StreamOps.toSet(pmap.stream().map(PrefixEntry::getPrefix));
        assertEquals(Set.of("x2", "x1"), set1);
    }
}
