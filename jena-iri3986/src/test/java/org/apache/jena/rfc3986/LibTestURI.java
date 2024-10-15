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

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.function.Supplier;

/**
 * Functions in support of IRI3986 testing.
 */
class LibTestURI {

    // Syntax and violations
    static void good(String iriStr) {
        IRI3986 iri = RFC3986.create(iriStr);
        if ( iri.hasViolations() )
            showViolations(iri);
        assertFalse(iri.hasViolations(), "Has violations");
    }

    // Syntax and violations
    static void goodSyntax(String iriStr) {
        IRI3986 iri = RFC3986.create(iriStr);
    }


    // Bad - syntax OK, but one or more violations
    static void bad(String iriStr) {
        throw new RuntimeException("Not implemented");
    }

    // Bad RFC 3986 syntax - scheme specific rules not run
    static void badSyntax(String iriStr) {
        IRIParseException ex = assertThrowsExactly(IRIParseException.class, ()->RFC3986.checkSyntax(iriStr));
    }

    /**
     * Parse the IRI string (expected to be valid) and execute
     * the scheme specific rules, then test the outcome.
     * If {@code expectedUriScheme}is null, don't check the scheme in the violation.
     */
    static void schemeViolation(String iriStr, URIScheme expectedUriScheme, Issue...issues) {
        int expectedCount = issues.length;

        IRI3986 iri = RFC3986.create(iriStr);
        if ( false || expectedCount == 0 ) {
            // Development helper.
            showViolations(iri);
        }

        if ( expectedUriScheme != null ) {
            assertNotNull(iri.scheme());
            String expectedName = expectedUriScheme.getName();
            String actualName = iri.str().substring(0, expectedName.length());
            boolean matches = expectedName.equalsIgnoreCase(actualName);
            assertTrue(matches, ()->String.format("Expected=%s, actual=%s", expectedName, actualName));
        }

        List<Issue> expected = List.of(issues);
        List<Issue> actual = new ArrayList<>();
        iri.forEachViolation(a->{
            if ( expectedUriScheme != null )
                assertEquals( expectedUriScheme, a.scheme(), "scheme --");
            actual.add(a.issue());
        });

        Supplier<String> messageSupplier = ()->"Issues expected="+expected+", actual="+actual;
        assertTrue(equalsUnordered(expected, actual), messageSupplier);

//        int count = countViolations(iri);
//        Set<Issue> expectedSet = new HashSet<>();
//        expectedSet.addAll(expected);
//
//        Set<Issue> actualSet = new HashSet<>();
//        actualSet.addAll(actual);
//
//
//        assertEquals(actualSet.size(), actual.size(), "Duplicates -- "+actual);
//        assertEquals(expectedSet, actualSet, "Issues -- ");
    }

    /**
     * Parse the IRI string (expected to be valid) and execute
     * the scheme specific rules, then print then print violations.
     * This is only for development.
     */
    static void schemeViolation(String iriStr) {
        IRI3986 iri = RFC3986.create(iriStr);
        showViolations(iri);
    }

    private static final boolean printViolations = false;

    /** Parse a syntactically correct string, test whether violations are raised. */
    static IRI3986 test3986(String iristr, int expectedViolationCount ) {
        IRI3986 iri = IRI3986.create(iristr);
        int count = countViolations(iri);
        if ( printViolations )
            showViolations(iri);
        assertEquals(expectedViolationCount, count, "Violation count");
        return iri;
    }

    static void showViolations(IRI3986 iri) {
        StringJoiner sj = new StringJoiner("\n  ");
        sj.add("<"+iri.str()+">");
        iri.forEachViolation(v->sj.add(v.toString()));
        String all = sj.toString();
        System.err.println(all);
    }

    static int countViolations(IRI3986 iri) {
        //class Ref { int vCount = 0;  }
        var x = new Object() { int vCount = 0;  };
        iri.forEachViolation(a->x.vCount++);
        return x.vCount;
    }

    /**
     * Compare two lists for unordered equality; same elements, same cardinality, any
     * order.
     * <p>
     * This is not intended for use with very large lists.
     */
    static <T> boolean equalsUnordered(List<T> list1, List<T> list2) {
        if ( list1.size() != list2.size() )
            return false;
        // containsAll both ways round isn't enough.
        // Copy, remove elements one by one, expecting to remove them all.
        // The lists are the same length.
        List<T> list2a = new ArrayList<>(list2);
        for ( T elt : list1 ) {
            list2a.remove(elt);
        }
        if ( list2a.size() != 0 )
            return false;
        return true;
    }


}
