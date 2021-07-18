/**
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

package org.apache.jena.http.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays ;
import java.util.List ;
import java.util.Map ;

import org.junit.Test ;

public class TestAuthStringTokenizer {

    static void tokenize(String input, String ... expectedOutput) {
        List<String> output = AuthStringTokenizer.tokenize(input) ;
        List<String> x = Arrays.asList(expectedOutput) ;
        assertEquals(x, output) ;
    }

    static Map<String, String> parse(String input) {
        return AuthStringTokenizer.parse(input) ;
    }

    @Test public void tokenize_01() {
        tokenize("abc",
                 "abc") ;
    }

    @Test public void tokenize_02() {
        tokenize("") ;
    }

    @Test public void tokenize_03() {
        tokenize("\"\"",
                 "\"\"") ;
    }

    @Test public void tokenize_04() {
        tokenize("\"abc\"",
                 "\"abc\"") ;
    }

    @Test public void tokenize_05() {
        tokenize("abc def",
                 "abc", "def") ;
    }

    @Test public void tokenize_06() {
        tokenize("abc=  def",
                 "abc", "=", "def") ;
    }

    @Test public void tokenize_07() {
        tokenize("xyz abc=def",
                 "xyz", "abc", "=", "def") ;
    }


    @Test public void tokenize_08() {
        tokenize("xyz , abc=def",
                "xyz", ",", "abc", "=", "def") ;
    }

    @Test public void tokenize_09() {
        tokenize("xyz , abc=def ,",
                 "xyz", ",", "abc", "=", "def", "," ) ;
    }

    @Test public void tokenize_10() {
        tokenize("xyz , = abc=def ,",
                 "xyz", ",", "=", "abc", "=", "def", "," ) ;
    }

    @Test public void tokenize_11() {
        tokenize("abc=\"\"",
                 "abc", "=", "\"\"") ;
    }

    private static String nullString = "" ;

    @Test public void parse_01() {
        Map<String, String> map = parse("") ;
        assertTrue(map.isEmpty()) ;
    }

    @Test public void parse_02() {
        Map<String, String> map = parse("a") ;
        assertEquals(1, map.size()) ;
        assertTrue(map.containsKey("a")) ;
        assertEquals(nullString, map.get("a")) ;
    }

    @Test public void parse_03() {
        Map<String, String> map = parse("a=b") ;
        assertEquals(1, map.size()) ;
        assertTrue(map.containsKey("a")) ;
        assertEquals("b", map.get("a")) ;
    }

    @Test public void parse_04() {
        Map<String, String> map = parse("Digest a=b c=\"def\", xyz=\"rst uvw\"") ;
        assertEquals(5, map.size()) ;
        assertEquals("Digest", map.get("SCHEME"));
        assertTrue(map.containsKey("digest"));
        assertEquals(nullString, map.get("digest")) ;
        assertEquals("b", map.get("a")) ;
        assertEquals("def", map.get("c")) ;
        assertEquals("rst uvw", map.get("xyz")) ;
    }
}