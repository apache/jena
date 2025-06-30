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

package org.apache.jena.sparql.syntax;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import static org.apache.jena.query.Syntax.*;
import org.apache.jena.sparql.lang.QueryParserBase;
import org.slf4j.Logger;

/**
 * Query parsing is primarily tested by the scripts in the test suite.
 * This class adds a few tests that are more conveniently done
 * in code rather than scripts.
 */
public class TestQueryParser {
    private static final Logger loggerSPARQL = QueryParserBase.parserLog;

    private static void silent(Runnable action) {
        LogCtl.withLevel(loggerSPARQL, "fatal", action);
    }

    // Single backslash so a Java string escape, raw surrogate in the string.
    @Test
    public void syntax_unicode_raw_surrogate_uri() {
        QueryParseException ex = assertThrows(QueryParseException.class,  ()->testParse("SELECT * { <http://example/\uD800> ?p ?o}"));
        assertTrue(ex.getMessage().contains("surrogate"));
    }

    @Test
    public void syntax_unicode_raw_surrogate_string() {
        QueryParseException ex = assertThrows(QueryParseException.class,  ()->testParse("SELECT * { ?s ?p '\uD800' }"));
        assertTrue(ex.getMessage().contains("surrogate"));
    }

    // Double backslash so the query string has an escape in it.
    @Test
    public void syntax_unicode_escaped_surrogate_uri() {
        QueryParseException ex = assertThrows(QueryParseException.class,  ()->testParse("SELECT * { <http://example/\\uD800> ?p ?o}"));
        assertTrue(ex.getMessage().contains("surrogate"));
    }

    @Test
    public void syntax_unicode_escaped_surrogate_strings() {
        QueryParseException ex = assertThrows(QueryParseException.class,  ()->testParse("SELECT * { ?s ?p '\\uD800'}"));
        assertTrue(ex.getMessage().contains("surrogate"));
    }

    @Test
    public void syntax_unicode_surrogate_pair_by_unicode_escape() {
        // Allow - because Java strings may have surrogate pairs so we allow them in unicode escapes if paired.
        testParse("SELECT * { ?s ?p '\\uD801\\uDC37'}");

//        QueryParseException ex = assertThrows(QueryParseException.class,  ()->testParse("SELECT * { ?s ?p '\\uD801\\uDC37'}"));
//        assertTrue(ex.getMessage().contains("surrogate"));
    }

    private static void testParse(String string) {
        QueryFactory.create(string, syntaxSPARQL_12);
        QueryFactory.create(string, syntaxARQ);
    }

    @Test public void syntax_uri_brackets_1() {
        testParseIRIs("<http://example/#[]>");
    }

    @Test public void syntax_uri_brackets_2() {
        testParseIRIs("<http://example/abc[]>");
    }

    @Test public void syntax_uri_brackets_3() {
        testParseIRIs("<http://[::1]/abc>");
    }

    @Test
    public void syntax_uri_brace_1() {
        assertThrows(QueryParseException.class, ()->testParseIRIs("<http://example/{}>"));
    }

    @Test public void syntax_uri_brace_2() {
        assertThrows(QueryParseException.class, ()->testParseIRIs("<http://example/#{}>"));
    }

    @Test
    public void syntax_uri_space_1() {
        assertThrows(QueryParseException.class, ()->testParseIRIs("<http://example/abc def>"));
    }

    @Test
    public void syntax_uri_space_2() {
        assertThrows(QueryParseException.class, ()->testParseIRIs("<http://example/abc?q= def>"));
    }

    @Test
    public void syntax_uri_space_3() {
        assertThrows(QueryParseException.class, ()->testParseIRIs("< http://example/abc>"));
    }

    @Test
    public void syntax_uri_space_4() {
        assertThrows(QueryParseException.class, ()->testParseIRIs("<http://example/abc >"));
    }

    // Test that a URI string can be used in Turtle data
    // and in SPARQL in the same way.
    private static void testParseIRIs(String string) {
        silent(()->QueryFactory.create("SELECT * { "+string+" a <http://example/TYPE> }"));
    }
}
