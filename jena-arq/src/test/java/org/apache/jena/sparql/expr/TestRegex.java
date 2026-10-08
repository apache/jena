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

package org.apache.jena.sparql.expr;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.engine.binding.BindingFactory;

@ParameterizedClass(name="{index}: {0}")
@MethodSource("provideArgs")
public class TestRegex
{

    protected static final String ALPHABET_CSV = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,z,";

    private static Stream<Arguments> provideArgs() {
         List<Arguments> x = List.of(
                Arguments.of( "Java Regex", RegexEngine.RegexImpl.Java),
                Arguments.of( "Xerces Regex", RegexEngine.RegexImpl.Xerces)
                    );
        return x.stream();
    }

    public TestRegex(String name, RegexEngine.RegexImpl regexImpl) {
        RegexEngine.setRegexImpl(regexImpl);
    }

    private static Object value;

    @BeforeAll
    public static void beforeClass() {
        value = ARQ.getContext().get(ARQ.regexImpl);
    }

    @AfterAll
    public static void afterClass() {
        ARQ.getContext().set(ARQ.regexImpl, value);
    }

    @Test public void testRegex01() { regexTest( "ABC",  "ABC",  null,   true); }
    @Test public void testRegex02() { regexTest( "ABC",  "abc",  null,   false); }
    @Test public void testRegex03() { regexTest( "ABC",  "abc",  "",     false); }
    @Test public void testRegex04() { regexTest( "ABC",  "abc",  "i",    true); }
    @Test public void testRegex05() { regexTest( "abc",  "B",    "i",    true); }
    @Test public void testRegex06() { regexTest( "ABC",  "^ABC", null,   true); }
    @Test public void testRegex07() { regexTest( "ABC",  "BC",   null,   true); }
    @Test public void testRegex08() { regexTest( "ABC",  "^BC",  null,   false); }
    @Test public void testRegex09() { regexTest( "[[",   "[",    "q",    true); }

    public void regexTest(String value, String pattern, String flags, boolean expected) {
        Expr s = NodeValue.makeString(value);
        E_Regex r = new E_Regex(s, pattern, flags);
        NodeValue nv = r.eval(BindingFactory.empty(), null);
        boolean b = nv.getBoolean();
        if ( b != expected )
            fail(fmtTest(value, pattern, flags)+" ==> "+b+" expected "+expected);
    }

    private String fmtTest(String value, String pattern, String flags) {
        String tmp = "regex(\""+value+"\", \""+pattern+"\"";
        if ( flags != null )
            tmp = tmp + ", \""+flags+"\"";
        tmp = tmp + ")";
        return tmp;
    }

    // Bad regex
    @Test
    public void testRegexErr1() {
        assertThrows(ExprEvalException.class, ()->
            regexTest("ABC", "(", null, false)
                );
    }

    // No such flag
    @Test
    public void testRegexErr2() {
        assertThrows(ExprEvalException.class, ()->
            regexTest("ABC", "abc", "g", false)
                );
    }

    // No such flag
    @Test
    public void testRegexErr3() {
        assertThrows(ExprEvalException.class, ()->
            regexTest("ABC", "abc", "u", false)
                );
    }


    public static Stream<Arguments> backtrackingRegex() {
        return Stream.of(
                        // While this is a nice example it behaves very different using Java vs Xerces regex so isn't
                        // a stable test
                        /*Arguments.of(ALPHABET_CSV, "^(.*?,){11}P", "m", false),*/
                        Arguments.of(Strings.repeat("a", 4096) + "!", "(a+)+$", "m", false),
                        Arguments.of(Strings.repeat("a", 32 * 1024) + "!", "(a+)+$", "m", false)
                );
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("backtrackingRegex")
    public void givenBacktrackingRegex_whenEvaluatingRegexWithoutTimeLimit_thenEventuallyCompletes(String input, String pattern, String flags, boolean shouldMatch) {
        // Given, When and Then
        long existingMax = RegexEngine.MAX_REGEX_EVALUATION_TIME;
        try {
            RegexEngine.MAX_REGEX_EVALUATION_TIME = -1;
            regexTest(input, pattern, flags, shouldMatch);
        } finally {
            RegexEngine.MAX_REGEX_EVALUATION_TIME = existingMax;
        }
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("backtrackingRegex")
    public void givenBacktrackingRegex_whenEvaluatingRegexWithTimeLimit_thenFails(String input, String pattern, String flags, boolean shouldMatch) {
        // Given, When and Then
        long existingMax = RegexEngine.MAX_REGEX_EVALUATION_TIME;
        try {
            // Intentionally minimal evaluation time so should fail near immediately, while we could set a more
            // realistic limit for the test the reality is that the different regex engines evaluate at different speed
            // so it's hard to pick a limit that reliably fails other than a trivially small one
            RegexEngine.MAX_REGEX_EVALUATION_TIME = 1;
            assertThrows(ExprEvalException.class, () -> regexTest(input, pattern, flags, shouldMatch));
        } finally {
            RegexEngine.MAX_REGEX_EVALUATION_TIME = existingMax;
        }
    }
}
