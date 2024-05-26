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

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.sparql.lang.QueryParserBase;
import org.junit.Test;
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

    @Test public void syntax_uri_brackets_1() {
        testParseIRIs("<http://example/#[]>");
    }

    @Test public void syntax_uri_brackets_2() {
        testParseIRIs("<http://example/abc[]>");
    }

    @Test public void syntax_uri_brackets_3() {
        testParseIRIs("<http://[::1]/abc>");
    }

    @Test(expected = QueryParseException.class)
    public void syntax_uri_brace_1() {
        testParseIRIs("<http://example/{}>");
    }

    @Test(expected = QueryParseException.class)
    public void syntax_uri_brace_2() {
        testParseIRIs("<http://example/#{}>");
    }

    @Test(expected = QueryParseException.class)
    public void syntax_uri_space_1() {
        testParseIRIs("<http://example/abc def>");
    }

    @Test(expected = QueryParseException.class)
    public void syntax_uri_space_2() {
        testParseIRIs("<http://example/abc?q= def>");
    }

    @Test(expected = QueryParseException.class)
    public void syntax_uri_space_3() {
        testParseIRIs("< http://example/abc>");
    }

    @Test(expected = QueryParseException.class)
    public void syntax_uri_space_4() {
        testParseIRIs("<http://example/abc >");
    }

    // Test that a URI string can be used in Turtle data
    // and in SPARQL in the same way.
    public static void testParseIRIs(String string) {
        silent(()->QueryFactory.create("SELECT * { "+string+" a <http://example/TYPE> }"));
    }
}
