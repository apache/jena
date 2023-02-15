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

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestBufferingPrefixMapping {

    @Test public void buffering_prefix_01_basic() {
        PrefixMapping base = new PrefixMappingImpl();
        PrefixMapping pmap = new BufferingPrefixMapping(base);
        assertEquals(0, pmap.numPrefixes());
        assertTrue(pmap.hasNoMappings());
    }

    @Test public void buffering_prefix_02_base_prefix() {
        // Base has a prefix.
        PrefixMapping base = new PrefixMappingImpl();
        base.setNsPrefix("x", "http://example/");
        PrefixMapping pmap = new BufferingPrefixMapping(base);
        assertEquals(1, pmap.numPrefixes());
        assertFalse(pmap.hasNoMappings());
    }

    @Test public void buffering_prefix_03_add() {
        PrefixMapping base = new PrefixMappingImpl();

        PrefixMapping pmap = new BufferingPrefixMapping(base);
        pmap.setNsPrefix("x", "http://example/");
        assertFalse(pmap.hasNoMappings());
        assertEquals(1, pmap.numPrefixes());

        assertTrue(base.hasNoMappings());
        assertEquals(0, base.numPrefixes());
    }

    @Test public void buffering_prefix_04_base_add() {
        PrefixMapping base = new PrefixMappingImpl();
        base.setNsPrefix("x1", "http://example/1#");
        PrefixMapping pmap = new BufferingPrefixMapping(base);
        pmap.setNsPrefix("x2", "http://example/2#");
        assertEquals(2, pmap.numPrefixes());
        assertEquals(1, base.numPrefixes());

    }

    @Test public void buffering_prefix_05_add_remove() {
        PrefixMapping base = new PrefixMappingImpl();
        base.setNsPrefix("x", "http://example/");
        PrefixMapping pmap = new BufferingPrefixMapping(base);
        pmap.removeNsPrefix("x");

        assertTrue(pmap.hasNoMappings());
        assertFalse(base.hasNoMappings());

        assertEquals(0, pmap.numPrefixes());
        assertEquals(1, base.numPrefixes());
    }

    @Test public void buffering_prefix_06_flush() {
        PrefixMapping base = new PrefixMappingImpl();
        base.setNsPrefix("x1", "http://example/1#");
        BufferingPrefixMapping pmap = new BufferingPrefixMapping(base);
        pmap.setNsPrefix("x2", "http://example/2#");
        assertEquals(2, pmap.numPrefixes());
        assertEquals(1, base.numPrefixes());

        pmap.flush();
        assertEquals(2, base.numPrefixes());
        assertEquals("http://example/2#", base.getNsPrefixURI("x2"));
    }
}
