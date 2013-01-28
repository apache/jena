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

package org.apache.jena.riot.system;

import org.junit.Test;

import org.apache.jena.atlas.junit.BaseTest;
import org.apache.jena.iri.IRIFactory;

/**
 * Abstract tests for {@link LightweightPrefixMap} implementations
 * 
 */
public abstract class TestLightweightPrefixMap extends BaseTest {
    protected IRIFactory factory = IRIFactory.iriImplementation();

    /**
     * Gets the prefix map implementation to test, each call should result in a
     * fresh instance
     * 
     * @return Prefix Map
     */
    protected abstract LightweightPrefixMap getPrefixMap();

    /**
     * Simple expand test
     */
    @Test
    public void prefixMap_expand_01() {
        LightweightPrefixMap pmap = this.getPrefixMap();
        add(pmap, "", "http://example/");
        String x = pmap.expand("", "x");
        assertEquals("http://example/x", x);
    }

    /**
     * Simple expand test
     */
    @Test
    public void prefixMap_expand_02() {
        LightweightPrefixMap pmap = this.getPrefixMap();
        add(pmap, "ex", "http://example/");
        String x = pmap.expand("", "x");
        assertNull(x);
    }

    /**
     * Simple expand test
     */
    @Test
    public void prefixMap_expand_03() {
        LightweightPrefixMap pmap = this.getPrefixMap();
        // Defining twice should not cause an issue
        add(pmap, "ex", "http://example/");
        add(pmap, "ex", "http://example/");
        String x = pmap.expand("", "x");
        assertNull(x);
    }

    /**
     * Simple expand test
     */
    @Test
    public void prefixMap_expand_04() {
        LightweightPrefixMap pmap = this.getPrefixMap();
        // The most recent definition should always be the one that applies
        add(pmap, "ex", "http://example/");
        add(pmap, "ex", "http://elsewhere/ns#");
        String x = pmap.expand("ex", "x");
        assertEquals("http://elsewhere/ns#x", x);
    }

    /**
     * Simple delete test - deleting a non-existent prefix should work
     */
    @Test
    public void prefixMap_delete_01() {
        LightweightPrefixMap pmap = this.getPrefixMap();
        // Deleting a non-existent prefix should not cause an issue
        pmap.delete("ex");
    }

    /**
     * Simple delete test
     */
    @Test
    public void prefixMap_delete_02() {
        LightweightPrefixMap pmap = this.getPrefixMap();
        add(pmap, "ex", "http://example/");
        String x = pmap.expand("ex", "x");
        assertEquals("http://example/x", x);
        pmap.delete("ex");
        x = pmap.expand("ex", "x");
        assertNull(x);
    }

    /**
     * Abbreviation test - no prefixes means no abbreviation
     */
    @Test
    public void prefixMap_abbrev_01() {
        LightweightPrefixMap pmap = this.getPrefixMap();
        String x = pmap.abbreviate("http://example/x");
        assertNull(x);
    }

    /**
     * Abbreviation test - no relevant prefixes means no abbreviation
     */
    @Test
    public void prefixMap_abbrev_02() {
        LightweightPrefixMap pmap = this.getPrefixMap();
        add(pmap, "ex", "http://elsewhere/ns#");
        String x = pmap.abbreviate("http://example/x");
        assertNull(x);
    }

    /**
     * Abbreviation test
     */
    @Test
    public void prefixMap_abbrev_03() {
        LightweightPrefixMap pmap = this.getPrefixMap();
        add(pmap, "ex", "http://example/");
        add(pmap, "eg", "http://elsewhere/ns#");
        String x = pmap.abbreviate("http://example/x");
        assertEquals("ex:x", x);
    }

    /**
     * Abbreviation test - check correct abbreviation is selected when
     * namespaces are similar
     */
    @Test
    public void prefixMap_abbrev_04() {
        LightweightPrefixMap pmap = this.getPrefixMap();
        add(pmap, "ex", "http://example/");
        add(pmap, "eg", "http://example/ns#");
        String x = pmap.abbreviate("http://example/x");
        assertEquals("ex:x", x);
    }

    /**
     * Abbreviation test - check either abbreviation is selected when namespaces
     * are identical with different prefixes
     */
    @Test
    public void prefixMap_abbrev_05() {
        LightweightPrefixMap pmap = this.getPrefixMap();
        add(pmap, "ex", "http://example/");
        add(pmap, "eg", "http://example/");
        String x = pmap.abbreviate("http://example/x");
        assertNotNull(x);
        assertTrue(x.equals("ex:x") || x.equals("eg:x"));
    }

    /**
     * Abbreviation test - check correct abbreviation is selected when lots of
     * namespaces are defined
     */
    @Test
    public void prefixMap_abbrev_06() {
        LightweightPrefixMap pmap = this.getPrefixMap();
        for (int i = 1; i <= 100; i++) {
            add(pmap, "ns" + i, "http://example/ns" + i + "#");
        }
        String x = pmap.abbreviate("http://example/ns100#x");
        assertEquals("ns100:x", x);
    }

    /**
     * Helper method for adding a namespace mapping
     * 
     * @param pmap
     *            Prefix Map
     * @param prefix
     *            Prefix
     * @param uri
     *            URI
     */
    protected void add(LightweightPrefixMap pmap, String prefix, String uri) {
        pmap.add(prefix, factory.create(uri));
    }

}
