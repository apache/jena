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

import static org.apache.jena.util.SplitIRI.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TestSplitIRI_Display {

    @Test
    public void split_basic_00() {
        testSplit("http://example/foo", "http://example/".length());
    }

    @Test
    public void split_basic_01() {
        testPrefixLocalname("http://example/foo", "http://example/", "foo");
    }

    @Test
    public void split_basic_02() {
        testPrefixLocalname("http://example/foo#bar", "http://example/foo#", "bar");
    }

    @Test
    public void split_basic_03() {
        testPrefixLocalname("http://example/foo#", "http://example/foo#", "");
    }

    @Test
    public void split_basic_04() {
        testPrefixLocalname("http://example/", "http://example/", "");
    }

    @Test
    public void split_basic_05() {
        testPrefixLocalname("http://example/1abc", "http://example/", "1abc");
    }

    @Test
    public void split_basic_06() {
        testPrefixLocalname("http://example/1.2.3.4", "http://example/", "1.2.3.4");
    }

    @Test
    public void split_basic_07() {
        testPrefixLocalname("http://example/xyz#1.2.3.4", "http://example/xyz#", "1.2.3.4");
    }

    @Test
    public void split_basic_08() {
        testPrefixLocalname("http://example/xyz#_abc", "http://example/xyz#", "_abc");
    }

    @Test
    public void split_basic_09() {
        testPrefixLocalname("http://example/xyz/_1.2.3.4", "http://example/xyz/", "_1.2.3.4");
    }

    @Test
    public void split_rel_1() {
        testPrefixLocalname("xyz/_1.2.3.4", "xyz/", "_1.2.3.4");
    }

    @Test
    public void split_rel_2() {
        testPrefixLocalname("xyz", "", "xyz");
    }

    @Test
    public void split_rel_3() {
        testPrefixLocalname("", "", "");
    }

    @Test
    public void split_weird_1() {
        testPrefixLocalname("abc:def", "abc:", "def");
    }

    @Test
    public void split_weird_2() {
        testPrefixLocalname("", "", "");
    }

    @Test
    public void split_ttl_01() {
        testPrefixLocalname("http://example/foo#bar:baz", "http://example/foo#", "bar:baz");
    }

    @Test
    public void split_ttl_02() {
        testPrefixLocalname("http://example/a:b:c", "http://example/", "a:b:c");
    }

    @Test
    public void split_ttl_03() {
        testPrefixLocalname("http://example/.2.3.4", "http://example/.", "2.3.4");
    }

    @Test
    public void split_ttl_04() {
        testPrefixLocalname("abc:xyz/.def", "abc:xyz/.", "def");
    }

    @Test
    public void split_ttl_05() {
        testPrefixLocalname("abc:xyz/-def", "abc:xyz/-", "def");
    }

    @Test
    public void split_ttl_06() {
        testPrefixLocalname("abc:xyz/-.-.-def", "abc:xyz/-.-.-", "def");
    }

    @Test
    public void split_ttl_07() {
        testPrefixLocalname("http://example/id=89", "http://example/", "id=89");
    }

    @Test
    public void split_urn_01() {
        testPrefixLocalname("urn:foo:bar", "urn:foo:", "bar");
    }

    @Test
    public void split_urn_02() {
        testPrefixLocalname("urn:example:bar/b", "urn:example:", "bar/b");
    }

    @Test
    public void split_urn_03() {
        testPrefixLocalname("urn:example:bar#frag", "urn:example:bar#", "frag");
    }

    private void testSplit(String string, int expected) {
        int i = splitpoint(string);
        assertEquals(expected, i);
    }

    private void testPrefixLocalnameNoSplit(String string) {
        int i = splitpoint(string);
        String msg = string;
        if (i != -1) {
            String ns = namespaceTTL(string);
            String ln = localnameTTL(string);
            msg = "Unexpected split of '" + string + "' into (" + ns + ", " + ln + ") [index=" + i + "]";
        }
        assertEquals(-1, i, msg);
    }

    private void testPrefixLocalname(String string, String expectedNamespace, String expectedLocalname) {
        String actualNamespace = namespace(string);
        String actualLocalName = localname(string);
        checkPrefixLocalname(string, expectedNamespace, actualNamespace, expectedLocalname, actualLocalName);
        if (expectedNamespace != null && expectedLocalname != null) {
            String x = actualNamespace + actualLocalName;
            assertEquals(string, x);
        }
    }

    private void checkPrefixLocalname(String string, String expectedNamespace, String actualNamespace, String expectedLocalname,
                                      String actualLocalName) {
        if (expectedNamespace != null)
            assertEquals(expectedNamespace, actualNamespace);
        if (expectedLocalname != null)
            assertEquals(expectedLocalname, actualLocalName);
    }
}
