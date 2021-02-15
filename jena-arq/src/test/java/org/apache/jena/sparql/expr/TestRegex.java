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

package org.apache.jena.sparql.expr;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.util.Symbol;
import org.junit.AfterClass;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestRegex
{
    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "Java Regex",   ARQ.javaRegex },
                                              { "Xerces Regex", ARQ.xercesRegex } });
    }

    public TestRegex(String name, Symbol setting) {
        ARQ.getContext().set(ARQ.regexImpl, setting) ;
    }

    private static Object value;

    @BeforeClass
    public static void beforeClass() {
        value = ARQ.getContext().get(ARQ.regexImpl);
    }

    @AfterClass
    public static void afterClass() {
        ARQ.getContext().set(ARQ.regexImpl, value);
    }

    @Test public void testRegex01() { regexTest( "ABC",  "ABC",  null,   true) ; }
    @Test public void testRegex02() { regexTest( "ABC",  "abc",  null,   false) ; }
    @Test public void testRegex03() { regexTest( "ABC",  "abc",  "",     false) ; }
    @Test public void testRegex04() { regexTest( "ABC",  "abc",  "i",    true) ; }
    @Test public void testRegex05() { regexTest( "abc",  "B",    "i",    true) ; }
    @Test public void testRegex06() { regexTest( "ABC",  "^ABC", null,   true) ; }
    @Test public void testRegex07() { regexTest( "ABC",  "BC",   null,   true) ; }
    @Test public void testRegex08() { regexTest( "ABC",  "^BC",  null,   false) ; }
    @Test public void testRegex09() { regexTest( "[[",   "[",    "q",    true) ; }

    public void regexTest(String value, String pattern, String flags, boolean expected) {
        Expr s = NodeValue.makeString(value) ;
        E_Regex r = new E_Regex(s, pattern, flags) ;
        NodeValue nv = r.eval(BindingFactory.empty(), null) ;
        boolean b = nv.getBoolean() ;
        if ( b != expected )
            fail(fmtTest(value, pattern, flags)+" ==> "+b+" expected "+expected) ;
    }

    private String fmtTest(String value, String pattern, String flags) {
        String tmp = "regex(\""+value+"\", \""+pattern+"\"" ;
        if ( flags != null )
            tmp = tmp + ", \""+flags+"\"" ;
        tmp = tmp + ")" ;
        return tmp ;
    }

    // Bad regex
    @Test(expected=ExprEvalException.class)
    public void testRegexErr1() { regexTest("ABC", "(", null, false) ; }

    // No such flag
    @Test(expected=ExprEvalException.class)
    public void testRegexErr2() { regexTest("ABC", "abc", "g", false) ; }

    // No such flag
    @Test(expected=ExprEvalException.class)
    public void testRegexErr3() { regexTest("ABC", "abc", "u", false) ; }

}
