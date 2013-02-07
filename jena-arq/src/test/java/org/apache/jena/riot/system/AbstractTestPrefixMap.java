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

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.iri.IRIFactory ;
import org.junit.Test ;

/**
 * Abstract tests for {@link PrefixMap} implementations
 * 
 */
public abstract class AbstractTestPrefixMap extends BaseTest {
    protected IRIFactory factory = IRIFactory.iriImplementation();

    /**
     * Gets the prefix map implementation to test, each call should result in a
     * fresh instance
     * 
     * @return Prefix Map
     */
    protected abstract PrefixMap getPrefixMap();

    @Test
    public void prefixMap_basic_01()
    {
        PrefixMap pmap = getPrefixMap();
        assertTrue(pmap.isEmpty()) ;
        assertEquals(0, pmap.size()) ;
        assertTrue(pmap.getMapping().isEmpty()) ;
        assertTrue(pmap.getMappingCopy().isEmpty()) ;
        assertTrue(pmap.getMappingCopyStr().isEmpty()) ;
    }
    
    @Test
    public void prefixMap_basic_02()
    {
        PrefixMap pmap = getPrefixMap();
        pmap.add("", "http://example/") ;
        assertFalse(pmap.isEmpty()) ;
        assertEquals(1, pmap.size()) ;
        assertTrue(pmap.contains("")) ;
        assertTrue(pmap.getMapping().containsKey("")) ;
        assertTrue(pmap.getMappingCopy().containsKey("")) ;
        assertTrue(pmap.getMappingCopyStr().containsKey("")) ;
    }

    @Test
    public void prefixMap_basic_03()
    {
        PrefixMap pmap = getPrefixMap();
        pmap.add("", "http://example/") ;
        pmap.add("org", "http://example.org/") ;
        assertTrue(pmap.contains("")) ;
        assertTrue(pmap.contains("org")) ;
        assertFalse(pmap.isEmpty()) ;
        assertEquals(2, pmap.size()) ;
    }
    
    @Test
    public void prefixMap_basic_04()
    {
        PrefixMap pmap1 = getPrefixMap();
        pmap1.add("", "http://example/") ;
        pmap1.add("org", "http://example.org/") ;
        PrefixMap pmap2 = getPrefixMap();
        pmap2.putAll(pmap1) ;

        assertTrue(pmap2.contains("")) ;
        assertTrue(pmap2.contains("org")) ;
        assertFalse(pmap2.isEmpty()) ;
        assertEquals(2, pmap2.size()) ;
    }
    
    

    /**
     * Simple expand test
     */
    @Test
    public void prefixMap_expand_01() {
        PrefixMap pmap = this.getPrefixMap();
        add(pmap, "", "http://example/");
        String x = pmap.expand("", "x");
        assertEquals("http://example/x", x);
    }

    /**
     * Simple expand test
     */
    @Test
    public void prefixMap_expand_02() {
        PrefixMap pmap = this.getPrefixMap();
        add(pmap, "ex", "http://example/");
        String x = pmap.expand("", "x");
        assertNull(x);
    }

    /**
     * Simple expand test
     */
    @Test
    public void prefixMap_expand_03() {
        PrefixMap pmap = this.getPrefixMap();
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
        PrefixMap pmap = this.getPrefixMap();
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
        PrefixMap pmap = this.getPrefixMap();
        // Deleting a non-existent prefix should not cause an issue
        pmap.delete("ex");
    }

    /**
     * Simple delete test
     */
    @Test
    public void prefixMap_delete_02() {
        PrefixMap pmap = this.getPrefixMap();
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
        PrefixMap pmap = this.getPrefixMap();
        String x = pmap.abbreviate("http://example/x");
        assertNull(x);
    }

    /**
     * Abbreviation test - no relevant prefixes means no abbreviation
     */
    @Test
    public void prefixMap_abbrev_02() {
        PrefixMap pmap = this.getPrefixMap();
        add(pmap, "ex", "http://elsewhere/ns#");
        String x = pmap.abbreviate("http://example/x");
        assertNull(x);
    }

    /**
     * Abbreviation test
     */
    @Test
    public void prefixMap_abbrev_03() {
        PrefixMap pmap = this.getPrefixMap();
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
        PrefixMap pmap = this.getPrefixMap();
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
        PrefixMap pmap = this.getPrefixMap();
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
        PrefixMap pmap = this.getPrefixMap();
        for (int i = 1; i <= 100; i++) {
            add(pmap, "ns" + i, "http://example/ns" + i + "#");
        }
        String x = pmap.abbreviate("http://example/ns100#x");
        assertEquals("ns100:x", x);
    }

    protected PrefixMap create() {
        PrefixMap pm = getPrefixMap();
        pm.add("p0", "http://example/a/");
        pm.add("p1", "http://example/a/b");
        pm.add("p2", "http://example/a/b/");
        pm.add("p3", "http://example/a/b#");
        pm.add("q1", "http://example/a");
        pm.add("q2", "http://example/a#");
        return pm;
    }

    @Test
    public void prefixMap_abbrev_10() {
        pmTest("http://example/a/b", "p1:", "p0:b" );
    }

    @Test
    public void prefixMap_abbrev_11() {
        pmTest("http://example/a/bcd", "p1:cd", "p0:bcd" );
    }

    @Test
    public void prefixMap_abbrev_12() {
        pmTest("http://example/a/b/c", "p2:c");
    }

    @Test
    public void prefixMap_abbrev_13() {
        pmTest("http://example/a/b/c/");
    }

    @Test
    public void prefixMap_abbrev_14() {
        pmTest("http://example/a/b/c/d");
    }

    @Test
    public void prefixMap_abbrev_15() {
        pmTest("http://example/a/b#x", "p3:x");
    }

    @Test
    public void prefixMap_abbrev_16() {
        pmTest("http://example/a#z", "q2:z");
    }

    @Test
    public void prefixMap_abbrev_17() {
        pmTest("http://example/a/", "p0:");
    }

    @Test
    public void prefixMap_abbrev_18() {
        pmTest("http://example/a", "q1:");
    }

    public void pmTest(String iriStr, String... expected) {
        PrefixMap pm = create();
        String x = pm.abbreviate(iriStr);
        if ( expected.length == 0 )
        {
            assertNull("expected no abbreviation for "+iriStr, x) ;
            return ;
        }
        
        for (String possible : expected) {
            if (possible.equals(x))
                return;
        }
        fail("Expected one of " + StrUtils.strjoin(" , ", expected) + " but got " + x);
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
    protected void add(PrefixMap pmap, String prefix, String uri) {
        pmap.add(prefix, factory.create(uri));
    }

}
