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

package org.apache.jena.util;

import static org.apache.jena.util.SplitIRI.*;

import junit.framework.JUnit4TestAdapter;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test splitting IRI strings using Turtle rules. Includes the generally splitting
 * for display - the difference is that for Turtle, some characters need escaping.
 */
public class TestSplitIRI_TTL {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(TestSplitIRI_TTL.class);
    }

    // Basics
    @Test
    public void split_basic_00() {
        testSplit("http://example/foo", "http://example/".length());
    }

    @Test
    public void split_basic_01() {
        testPrefixLocalnameTTL("http://example/foo", "http://example/", "foo");
    }

    @Test
    public void split_basic_02() {
        testPrefixLocalnameTTL("http://example/foo#bar", "http://example/foo#", "bar");
    }

    @Test
    public void split_basic_03() {
        testPrefixLocalnameTTL("http://example/foo#", "http://example/foo#", "");
    }

    @Test
    public void split_basic_04() {
        testPrefixLocalnameTTL("http://example/", "http://example/", "");
    }

    @Test
    public void split_basic_05() {
        testPrefixLocalnameTTL("http://example/1abc", "http://example/", "1abc");
    }

    @Test
    public void split_basic_06() {
        testPrefixLocalnameTTL("http://example/1.2.3.4", "http://example/", "1.2.3.4");
    }

    @Test
    public void split_basic_07() {
        testPrefixLocalnameTTL("http://example/xyz#1.2.3.4", "http://example/xyz#", "1.2.3.4");
    }

    @Test
    public void split_basic_08() {
        testPrefixLocalnameTTL("http://example/xyz#_abc", "http://example/xyz#", "\\_abc");
    }

    @Test
    public void split_basic_09() {
        testPrefixLocalnameTTL("http://example/xyz/_1.2.3.4", "http://example/xyz/", "\\_1.2.3.4");
    }

    // Relative URIs
    @Test
    public void split_rel_1() {
        testPrefixLocalnameTTL("xyz/_1.2.3.4", "xyz/", "\\_1.2.3.4");
    }

    @Test
    public void split_rel_2() {
        testPrefixLocalnameTTL("xyz", "", "xyz");
    }

    @Test
    public void split_rel_3() {
        testPrefixLocalnameTTL("", "", "");
    }

    // Bizarre but legal URIs
    @Test
    public void split_weird_1() {
        testPrefixLocalnameTTL("abc:def", "abc:", "def");
    }

    @Test
    public void split_weird_2() {
        testPrefixLocalnameTTL("", "", "");
    }

    // Trailing '.' - split with escape.
    @Test
    public void split_weird_3() {
        testPrefixLocalnameTTL("http://example/abc#x.", "http://example/abc#", "x\\.");
    }

    @Test
    public void split_weird_4() {
        testPrefixLocalnameTTL("http://example/", "http://example/", "");
    }

    // Turtle details.
    // "." leading dot is not legal.
    @Test
    public void split_ttl_01() {
        testPrefixLocalnameTTL("http://example/foo#bar:baz", "http://example/foo#", "bar:baz");
    }

    @Test
    public void split_ttl_02() {
        testPrefixLocalnameTTL("http://example/a:b:c", "http://example/", "a:b:c");
    }

    @Test
    public void split_ttl_03() {
        testPrefixLocalnameTTL("http://example/.2.3.4", "http://example/.", "2.3.4");
    }

    // "." leading dot is not legal.
    @Test
    public void split_ttl_04() {
        testPrefixLocalnameTTL("abc:xyz/.def", "abc:xyz/.", "def");
    }

    // "-" leading dash is not legal.
    @Test
    public void split_ttl_05() {
        testPrefixLocalnameTTL("abc:xyz/-def", "abc:xyz/-", "def");
    }

    @Test
    public void split_ttl_06() {
        testPrefixLocalnameTTL("abc:xyz/-.-.-def", "abc:xyz/-.-.-", "def");
    }

    // Turtle-escape.
    @Test
    public void split_ttl_07() {
        testPrefixLocalnameTTL("http://example/id=89", "http://example/", "id\\=89");
    }

    // Turtle details, including escaping.
    // Test for PrefixLocalnameEsc
    @Test
    public void split_ttl_esc_01() {
        testPrefixLocalnameTTL("http://example/id=89", "http://example/", "id\\=89");
    }

    @Test
    public void split_ttl_esc_02() {
        testPrefixLocalnameTTL("http://example/a,b", "http://example/", "a\\,b");
    }

    // Trailing '.' Legal if escaped.
    @Test
    public void split_ttl_esc_03() {
        testPrefixLocalnameTTL("http://example/2.3.", "http://example/", "2.3\\.");
    }

    @Test
    public void split_ttl_esc_04() {
        testPrefixLocalnameTTL("http://example/abc#x.", "http://example/abc#", "x\\.");
    }

    // URNs split differently.
    @Test
    public void split_urn_01() {
        testPrefixLocalnameTTL("urn:foo:bar", "urn:foo:", "bar");
    }

    @Test
    public void split_urn_02() {
        testPrefixLocalnameTTL("urn:example:bar/b", "urn:example:", "bar\\/b");
    }

    @Test
    public void split_urn_03() {
        testPrefixLocalnameTTL("urn:example:bar#frag", "urn:example:bar#", "frag");
    }

    // Fragments, including Turtle escapes.
    @Test
    public void split_frag_01() {
        testPrefixLocalnameTTL("http://example/foo#bar:baz", "http://example/foo#", "bar:baz");
    }

    @Test
    public void split_frag_02() {
        testPrefixLocalnameTTL("http://example/abc/def#ghi/jkl", "http://example/abc/def#", "ghi\\/jkl");
    }

    @Test
    public void split_frag_03() {
        testPrefixLocalnameTTL("urn:example:abc#ghi:jkl", "urn:example:abc#", "ghi:jkl");
    }

    @Test
    public void split_frag_04() {
        testPrefixLocalnameTTL("urn:example:abc#ghi/jkl", "urn:example:abc#", "ghi\\/jkl");
    }

    @Test
    public void split_frag_05() {
        testPrefixLocalnameTTL("urn:example:abc#ghi:jkl", "urn:example:abc#", "ghi:jkl");
    }

    private void testSplit(String string, int expected) {
        int i = splitpoint(string);
        Assert.assertEquals(expected, i);
    }

    private void testPrefixLocalnameTTL(String string, String expectedNamespace, String expectedLocalname) {
        checkPrefixLocalname(string, expectedNamespace, namespaceTTL(string), expectedLocalname, localnameTTL(string));
    }

    private void checkPrefixLocalname(String string, String expectedNamespace, String actualNamespace, String expectedLocalname,
                                      String actualLocalName) {
        if ( expectedNamespace != null )
            Assert.assertEquals(expectedNamespace, actualNamespace);
        if ( expectedLocalname != null )
            Assert.assertEquals(expectedLocalname, actualLocalName);
    }
}
