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

package org.apache.jena.rfc3986;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Other tests of IRIs : tests for features
 * @see TestRFC3986Syntax
 * @see TestNormalize
 * @see TestResolve
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestIRI3986 {

    @Test public void absolute1() {
        isTrue("http://example/foo", IRI3986::isAbsolute);
    }

    @Test public void absolute2() {
        // Fragment
        isFalse("http://example/foo#a", IRI3986::isAbsolute);
    }

    @Test public void absolute3() {
        isFalse("example/foo#a", IRI3986::isAbsolute);
    }

    @Test public void relative1() {
        isFalse("http://example/foo", IRI3986::isRelative);
    }

    @Test public void relative2() {
        isTrue("//example/foo#a", IRI3986::isRelative);
    }

    @Test public void relative3() {
        isTrue("example", IRI3986::isRelative);
    }

    @Test public void relative4() {
        isTrue("example/foo#a", IRI3986::isRelative);
    }

    @Test public void rootless1() {
        isTrue("urn:ab:cde", IRI3986::isRootless);
    }

    @Test public void rootless2() {
        isFalse("http://example/path", IRI3986::isRootless);
    }

    @Test public void hierarchical1() {
        isTrue("http://example/path", IRI3986::isHierarchical);
    }

    @Test public void hierarchical2() {
        isFalse("urn:ab:cde", IRI3986::isHierarchical);
    }

    @Test public void query1() {
        isTrue("http://example/foo?query=bar", IRI3986::hasQuery);
    }

    @Test public void query2() {
        isTrue("http://example/foo?", IRI3986::hasQuery);
    }

    @Test public void query3() {
        isTrue("http://example/foo?query", IRI3986::hasQuery);
    }

    @Test public void query4() {
        isFalse("http://example/foo#query", IRI3986::hasQuery);
    }

    @Test public void query5() {
        isFalse("http://example/foo#", IRI3986::hasQuery);
    }

    @Test public void fragment1() {
        isTrue("http://example/foo#frag", IRI3986::hasFragment);
    }

    @Test public void fragment2() {
        isTrue("http://example/foo#", IRI3986::hasFragment);
    }

    @Test public void fragment3() {
        isTrue("foo#fragment", IRI3986::hasFragment);
    }

    @Test public void fragment4() {
        isTrue("#fragment", IRI3986::hasFragment);
    }

    @Test public void fragment5() {
        isFalse("foo", IRI3986::hasFragment);
    }

    @Test public void asRFC3986_1() {
        isTrue("http://host/abc", IRI3986::isRFC3986);
    }

    @Test public void asRFC3986_2() {
        isFalse("http://host/αβγ", IRI3986::isRFC3986);
    }

    // ----

    private static void isTrue(String iriStr, Predicate<IRI3986> testPredicate) {
        assertTrue(testPredicate.test(IRI3986.create(iriStr)));
    }

    private static void isFalse(String iriStr, Predicate<IRI3986> testPredicate) {
        assertFalse(testPredicate.test(IRI3986.create(iriStr)));
    }
}
