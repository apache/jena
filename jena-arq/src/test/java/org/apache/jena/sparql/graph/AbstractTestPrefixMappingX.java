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

package org.apache.jena.sparql.graph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.Test;

import org.apache.jena.shared.PrefixMapping;

/**
 * Test prefix mappings - subclass this test and override getMapping() to deliver the
 * prefixMapping to be tested.
 */
public abstract class AbstractTestPrefixMappingX {
    // This is a copy of the jena-core class, converted to JUnit5.

    public AbstractTestPrefixMappingX() {}

    /**
     * Subclasses implement to return a new, empty prefixMapping of their preferred
     * kind.
     */
    abstract protected PrefixMapping getMapping();

    static final String crispURI = "http://crisp.nosuch.net/";
    static final String ropeURI = "scheme:rope/string#";
    static final String butterURI = "ftp://ftp.nowhere.at.all/cream#";

    /**
     * The empty prefix is specifically allowed [for the default namespace].
     */
    @Test public void testEmptyPrefix() {
        addGetTest("", crispURI);
    }

    @Test public void testStrPrefix1() {
        addGetTest("abc", "http://example/");
    }

    @Test public void testStrPrefix2() {
        // U+1F607 - smiling face with halo
        String prefix = new String(Character.toChars(0x1F607));
        addGetTest(prefix, "http://example/");
    }

    private void addGetTest(String prefix, String uri) {
        PrefixMapping pmap = getMapping();
        pmap.setNsPrefix(prefix, uri);
        assertEquals(uri, pmap.getNsPrefixURI(prefix));
    }

    static final String[] badNames = {"<hello>", "foo:bar", "with a space", "-argument"};

    /**
     * Test that various illegal names are trapped.
     */
    @Test public void testCheckNames() {
        PrefixMapping ns = getMapping();
        for ( String bad : badNames ) {
            try {
                ns.setNsPrefix(bad, crispURI);
                fail("'" + bad + "' is an illegal prefix and should be trapped");
            } catch (PrefixMapping.IllegalPrefixException e) {
                pass();
            }
        }
    }

    @Test public void testNullURITrapped() {
        try {
            getMapping().setNsPrefix("xy", null);
            fail("should trap null URI in setNsPrefix");
        } catch (NullPointerException e) {
            pass();
        }
    }

    /**
     * test that a PrefixMapping maps names to URIs. The names and URIs are all fully
     * distinct - overlapping names/uris are dealt with in other tests.
     */
    @Test public void testPrefixMappingMapping() {
        String toast = "ftp://ftp.nowhere.not/";
        assertNotEquals(crispURI, toast, ()->"crisp and toast must differ");
        /* */
        PrefixMapping ns = getMapping();
        assertEquals(null, ns.getNsPrefixURI("crisp"), ()->"crisp should be unset");
        assertEquals(null, ns.getNsPrefixURI("toast"), ()->"toast should be unset");
        assertEquals(null, ns.getNsPrefixURI("butter"), ()->"toast should be unset");
        /* */
        ns.setNsPrefix("crisp", crispURI);
        assertEquals(crispURI, ns.getNsPrefixURI("crisp"), ()->"crisp should be set");
        assertEquals(null, ns.getNsPrefixURI("toast"), ()->"toast should still be unset");
        assertEquals(null, ns.getNsPrefixURI("butter"), ()->"butter should still be unset");
        /* */
        ns.setNsPrefix("toast", toast);
        assertEquals(crispURI, ns.getNsPrefixURI("crisp"), ()->"crisp should be set");
        assertEquals(toast, ns.getNsPrefixURI("toast"), ()->"toast should be set");
        assertEquals(null, ns.getNsPrefixURI("butter"), ()->"butter should still be unset");
    }

    /**
     * Test that we can run the prefix mapping in reverse - from URIs to prefixes.
     * uriB is a prefix of uriA to try and ensure that the ordering of the map
     * doesn't matter.
     */
    @Test public void testReversePrefixMapping() {
        PrefixMapping ns = getMapping();
        String uriA = "http://jena.hpl.hp.com/A#", uriB = "http://jena.hpl.hp.com/";
        String uriC = "http://jena.hpl.hp.com/Csharp/";
        String prefixA = "aa", prefixB = "bb";
        ns.setNsPrefix(prefixA, uriA).setNsPrefix(prefixB, uriB);
        assertEquals(null, ns.getNsURIPrefix(uriC));
        assertEquals(prefixA, ns.getNsURIPrefix(uriA));
        assertEquals(prefixB, ns.getNsURIPrefix(uriB));
    }

    /**
     * test that we can extract a proper Map from a PrefixMapping
     */
    @Test public void testPrefixMappingMap() {
        PrefixMapping ns = getCrispyRope();
        Map<String, String> map = ns.getNsPrefixMap();
        assertEquals(2, map.size(), ()->"map should have two elements");
        assertEquals(crispURI, map.get("crisp"));
        assertEquals("scheme:rope/string#", map.get("rope"));
    }

    /**
     * test that the Map returned by getNsPrefixMap does not alias (parts of) the
     * secret internal map of the PrefixMapping
     */
    @Test public void testPrefixMappingSecret() {
        PrefixMapping ns = getCrispyRope();
        Map<String, String> map = ns.getNsPrefixMap();
        // The map may be unmodifiable in which case put throws
        // UnsupportedOperationException
        try {
            map.put("crisp", "with/onions");
            map.put("sandwich", "with/cheese");
        } catch (UnsupportedOperationException ex) {}

        assertEquals(crispURI, ns.getNsPrefixURI("crisp"));
        assertEquals(ropeURI, ns.getNsPrefixURI("rope"));
        assertEquals(null, ns.getNsPrefixURI("sandwich"));
    }

    private PrefixMapping getCrispyRope() {
        PrefixMapping ns = getMapping();
        ns.setNsPrefix("crisp", crispURI);
        ns.setNsPrefix("rope", ropeURI);
        return ns;
    }

    /**
     * these are strings that should not change when they are prefix-expanded with
     * crisp and rope as legal prefixes.
     */
    static final String[] dontChange = {"", "http://www.somedomain.something/whatever#", "crispy:cabbage", "cris:isOnInfiniteEarths",
        "rop:tangled/web", "roped:abseiling"};

    /**
     * these are the required mappings which the test cases below should satisfy: an
     * array of 2-arrays, where element 0 is the string to expand and element 1 is
     * the string it should expand to.
     */
    static final String[][] expansions = {{"crisp:pathPart", crispURI + "pathPart"}, {"rope:partPath", ropeURI + "partPath"},
        {"crisp:path:part", crispURI + "path:part"},};

    @Test public void testExpandPrefix() {
        PrefixMapping ns = getMapping();
        ns.setNsPrefix("crisp", crispURI);
        ns.setNsPrefix("rope", ropeURI);
        /* */
        for ( String aDontChange : dontChange ) {
            assertEquals(aDontChange, ns.expandPrefix(aDontChange), ()->"should be unchanged");
        }
        /* */
        for ( String[] expansion : expansions ) {
            assertEquals(expansion[1], ns.expandPrefix(expansion[0]), ()->"should expand correctly");
        }
    }

    @Test public void testUseEasyPrefix() {
        testUseEasyPrefix("prefix mapping impl", getMapping());
        testShortForm("prefix mapping impl", getMapping());
    }

    public static void testUseEasyPrefix(String title, PrefixMapping ns) {
        testShortForm(title, ns);
    }

    public static void testShortForm(String title, PrefixMapping ns) {
        ns.setNsPrefix("crisp", crispURI);
        ns.setNsPrefix("butter", butterURI);
        assertEquals("", ns.shortForm(""), ()->title);
        assertEquals(ropeURI, ns.shortForm(ropeURI), ()->title);
        assertEquals("crisp:tail", ns.shortForm(crispURI + "tail"), ()->title);
        assertEquals("butter:here:we:are", ns.shortForm(butterURI + "here:we:are"), ()->title);
    }

    @Test public void testEasyQName() {
        PrefixMapping ns = getMapping();
        String alphaURI = "http://seasonal.song/preamble/";
        ns.setNsPrefix("alpha", alphaURI);
        assertEquals("alpha:rowboat", ns.qnameFor(alphaURI + "rowboat"));
    }

    @Test public void testNoQNameNoPrefix() {
        PrefixMapping ns = getMapping();
        String alphaURI = "http://seasonal.song/preamble/";
        ns.setNsPrefix("alpha", alphaURI);
        assertEquals(null, ns.qnameFor("eg:rowboat"));
    }

    @Test public void testNoQNameBadLocal() {
        PrefixMapping ns = getMapping();
        String alphaURI = "http://seasonal.song/preamble/";
        ns.setNsPrefix("alpha", alphaURI);
        assertEquals(null, ns.qnameFor(alphaURI + "12345"));
    }

    /**
     * The tests implied by the email where Chris suggested adding qnameFor;
     * shortForm generates illegal qnames but qnameFor does not.
     */
    @Test public void testQnameFromEmail() {
        String uri = "http://some.long.uri/for/a/namespace#";
        PrefixMapping ns = getMapping();
        ns.setNsPrefix("x", uri);
        assertEquals(null, ns.qnameFor(uri));
        assertEquals(null, ns.qnameFor(uri + "non/fiction"));
    }

    /**
     * test that we can add the maplets from another PrefixMapping without losing our
     * own.
     */
    @Test public void testAddOtherPrefixMapping() {
        PrefixMapping a = getMapping();
        PrefixMapping b = getMapping();
        assertFalse(a == b, ()->"must have two diffferent maps");
        a.setNsPrefix("crisp", crispURI);
        a.setNsPrefix("rope", ropeURI);
        b.setNsPrefix("butter", butterURI);
        assertEquals(null, b.getNsPrefixURI("crisp"));
        assertEquals(null, b.getNsPrefixURI("rope"));
        b.setNsPrefixes(a);
        checkContainsMapping(b);
    }

    private void checkContainsMapping(PrefixMapping b) {
        assertEquals(crispURI, b.getNsPrefixURI("crisp"));
        assertEquals(ropeURI, b.getNsPrefixURI("rope"));
        assertEquals(butterURI, b.getNsPrefixURI("butter"));
    }

    /**
     * as for testAddOtherPrefixMapping, except that it's a plain Map we're adding.
     */
    @Test public void testAddMap() {
        PrefixMapping b = getMapping();
        Map<String, String> map = new HashMap<>();
        map.put("crisp", crispURI);
        map.put("rope", ropeURI);
        b.setNsPrefix("butter", butterURI);
        b.setNsPrefixes(map);
        checkContainsMapping(b);
    }

    @Test public void testAddDefaultMap() {
        PrefixMapping pm = getMapping();
        PrefixMapping root = PrefixMapping.Factory.create();
        pm.setNsPrefix("a", "aPrefix:");
        pm.setNsPrefix("b", "bPrefix:");
        root.setNsPrefix("a", "pootle:");
        root.setNsPrefix("z", "bPrefix:");
        root.setNsPrefix("c", "cootle:");
        assertSame(pm, pm.withDefaultMappings(root));
        assertEquals("aPrefix:", pm.getNsPrefixURI("a"));
        assertEquals(null, pm.getNsPrefixURI("z"));
        assertEquals("bPrefix:", pm.getNsPrefixURI("b"));
        assertEquals("cootle:", pm.getNsPrefixURI("c"));
    }

    @Test public void testSecondPrefixRetainsExistingMap() {
        PrefixMapping A = getMapping();
        A.setNsPrefix("a", crispURI);
        A.setNsPrefix("b", crispURI);
        assertEquals(crispURI, A.getNsPrefixURI("a"));
        assertEquals(crispURI, A.getNsPrefixURI("b"));
    }

    @Test public void testSecondPrefixReplacesReverseMap() {
        PrefixMapping A = getMapping();
        A.setNsPrefix("a", crispURI);
        A.setNsPrefix("b", crispURI);
        assertEquals("b", A.getNsURIPrefix(crispURI));
    }

    @Test public void testSecondPrefixDeletedUncoversPreviousMap() {
        PrefixMapping A = getMapping();
        A.setNsPrefix("x", crispURI);
        A.setNsPrefix("y", crispURI);
        A.removeNsPrefix("y");
        assertEquals("x", A.getNsURIPrefix(crispURI));
    }

    /**
     * Test that the empty prefix does not wipe an existing prefix for the same URI.
     */
    @Test public void testEmptyDoesNotWipeURI() {
        PrefixMapping pm = getMapping();
        pm.setNsPrefix("frodo", ropeURI);
        pm.setNsPrefix("", ropeURI);
        assertEquals(ropeURI, pm.getNsPrefixURI("frodo"));
    }

    /**
     * Test that adding a new prefix mapping for U does not throw away a default
     * mapping for U.
     */
    @Test public void testSameURIKeepsDefault() {
        PrefixMapping A = getMapping();
        A.setNsPrefix("", crispURI);
        A.setNsPrefix("crisp", crispURI);
        assertEquals(crispURI, A.getNsPrefixURI(""));
    }

    @Test public void testReturnsSelf() {
        PrefixMapping A = getMapping();
        assertSame(A, A.setNsPrefix("crisp", crispURI));
        assertSame(A, A.setNsPrefixes(A));
        assertSame(A, A.setNsPrefixes(new HashMap<String, String>()));
        assertSame(A, A.removeNsPrefix("rhubarb"));
    }

    @Test public void testRemovePrefix() {
        String hURI = "http://test.remove.prefixes/prefix#";
        String bURI = "http://other.test.remove.prefixes/prefix#";
        PrefixMapping A = getMapping();
        A.setNsPrefix("hr", hURI);
        A.setNsPrefix("br", bURI);
        A.removeNsPrefix("hr");
        assertEquals(null, A.getNsPrefixURI("hr"));
        assertEquals(bURI, A.getNsPrefixURI("br"));
    }

    @Test public void testClear() {
        String hURI = "http://test.remove.prefixes/prefix#";
        String bURI = "http://other.test.remove.prefixes/prefix#";
        PrefixMapping A = getMapping();
        A.setNsPrefix("hr", hURI);
        A.setNsPrefix("br", bURI);
        A.clearNsPrefixMap();

        assertEquals(null, A.getNsPrefixURI("hr"));
        assertEquals(null, A.getNsPrefixURI("br"));

        assertEquals(null, A.getNsURIPrefix(hURI));
        assertEquals(null, A.getNsURIPrefix(bURI));
    }

    @Test public void testNoMapping() {
        String hURI = "http://test.prefixes/prefix#";
        PrefixMapping A = getMapping();
        assertTrue(A.hasNoMappings());
        A.setNsPrefix("hr", hURI);
        assertFalse(A.hasNoMappings());
    }

    @Test public void testNumPrefixes() {
        String hURI = "http://test.prefixes/prefix#";
        PrefixMapping A = getMapping();
        assertEquals(0, A.numPrefixes());
        A.setNsPrefix("hr", hURI);
        assertEquals(1, A.numPrefixes());
    }

    protected void fill(PrefixMapping pm, String settings) {
        List<String> L = listOfStrings(settings);
        for ( String setting : L ) {
            int eq = setting.indexOf('=');
            pm.setNsPrefix(setting.substring(0, eq), setting.substring(eq + 1));
        }
    }

    // we now allow namespaces to end with non-punctuational characters
    @Test public void testAllowNastyNamespace() {
        getMapping().setNsPrefix("abc", "def");
    }

    @Test public void testLock() {
        PrefixMapping A = getMapping();
        assertSame(A, A.lock());
        /* */
        try {
            A.setNsPrefix("crisp", crispURI);
            fail("mapping should be frozen");
        } catch (PrefixMapping.JenaLockedException e) {
            pass();
        }
        /* */
        try {
            A.setNsPrefixes(A);
            fail("mapping should be frozen");
        } catch (PrefixMapping.JenaLockedException e) {
            pass();
        }
        /* */
        try {
            A.setNsPrefixes(new HashMap<String, String>());
            fail("mapping should be frozen");
        } catch (PrefixMapping.JenaLockedException e) {
            pass();
        }
        /* */
        try {
            A.removeNsPrefix("toast");
            fail("mapping should be frozen");
        } catch (PrefixMapping.JenaLockedException e) {
            pass();
        }
    }

    protected void pass() {}

    /**
     * Answer a List of the substrings of <code>s</code> that are separated by
     * spaces.
     */
    static List<String> listOfStrings(String s) {
        List<String> result = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(s);
        while (st.hasMoreTokens())
            result.add(st.nextToken());
        return result;
    }
}
