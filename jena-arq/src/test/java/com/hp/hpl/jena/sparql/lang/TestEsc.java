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

package com.hp.hpl.jena.sparql.lang;

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.Test ;

import com.hp.hpl.jena.query.QueryParseException ;

public class TestEsc extends BaseTest
{
    @Test public void testEsc01() { execTest("x\\uabcd", "x\uabcd") ; }
    @Test public void testEsc02() { execTest("\\uabcdx", "\uabcdx") ; }
    @Test public void testEsc03() { execTest("1234\\uabcd1234", "1234\uabcd1234") ; }
    @Test public void testEsc04() { execTestFail("\\X") ; }
    @Test public void testEsc05() { execTestFail("\\Xz") ; }
    @Test public void testEsc06() { execTestFail("a\\X") ; }
    
    
    @Test public void testEscUni01() { execTestFail("\\uabck") ; }
    @Test public void testEscUni02() { execTestFail("\\uab") ; }
    @Test public void testEscUni03() { execTestFail("\\uabc") ; }
    @Test public void testEscUni04() { execTestFail("\\ua") ; }
    @Test public void testEscUni05() { execTestFail("\\u") ; }
    @Test public void testEscUni06() { execTestFail("\\") ; }
    @Test public void testEscUni07() { execTest("\\u0020", " ") ; }
    @Test public void testEscUni08() { execTest("\\uFFFF", "\uFFFF") ; }
    @Test public void testEscUni09() { execTest("\\u0000", "\u0000") ; }
    @Test public void testEscUni10() { execTestFail("\\U0000") ; }
    @Test public void testEscUni11() { execTestFail("\\U0000A") ; }
    @Test public void testEscUni12() { execTestFail("\\U0000AB") ; }
    @Test public void testEscUni13() { execTestFail("\\U0000ABC") ; }
    @Test public void testEscUni14() { execTest("\\U0000ABCD", "\uABCD") ; }
    @Test public void testEscUni15() { execTestFail("\\U0000") ; }
    @Test public void testEscUni16() { execTest("\\U00000000", "\u0000") ; }
    @Test public void testEscUni17() { execTest("x\\tx\\nx\\r", "x\tx\nx\r") ; }
    @Test public void testEscUni18() { execTest("x\\t\\n\\r", "x\t\n\r") ; }
    
    private void execTestFail(String input)
    {
        try {
            String s = ParserBase.unescapeStr(input) ;
            fail("Unescaping succeeded on "+input) ;
        } catch (QueryParseException ex)
        {
            return ;
        }
        
    }
    
    private void execTest(String input, String outcome)
    {
        String result = ParserBase.unescapeStr(input) ;
        assertEquals("Unescaped string does not match ("+input+")", outcome, result) ;
    }

    
}
