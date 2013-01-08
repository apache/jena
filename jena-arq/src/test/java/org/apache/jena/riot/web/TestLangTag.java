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

package org.apache.jena.riot.web;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.web.LangTag ;
import org.junit.Test ;

public class TestLangTag extends BaseTest
{
    @Test public void parse_01() 
    { parseGood("en",                  "en",               "en", null, null, null, null) ; }

    @Test public void parse_02()
    { parseGood("en-uk",               "en-UK",            "en", null, "UK", null, null) ; }
    
    @Test public void parse_03()
    { parseGood("es-419",              "es-419",           "es", null, "419", null, null) ; }
    
    @Test public void parse_04()
    { parseGood("zh-Hant",             "zh-Hant",          "zh", "Hant", null, null, null) ; }
    
    @Test public void parse_05()
    { parseGood("sr-Latn-CS",          "sr-Latn-CS",       "sr", "Latn", "CS", null, null) ; }
    
    @Test public void parse_06()
    { parseGood("sl-nedis",            "sl-nedis",         "sl", null, null, "nedis", null) ; }
    
    @Test public void parse_07()
    { parseGood("sl-IT-nedis",         "sl-IT-nedis",      "sl", null, "IT", "nedis", null) ; }
    
    @Test public void parse_08()
    { parseGood("sl-Latn-IT-nedis",    "sl-Latn-IT-nedis", "sl", "Latn", "IT", "nedis", null) ; }
    
    @Test public void parse_09()
    { parseGood("de-CH-x-Phonebk",     "de-CH-x-Phonebk",  "de", null, "CH", null, "x-Phonebk") ; }
    
    @Test public void parse_10()
    { parseGood("zh-cn-a-myExt-x-private", "zh-CN-a-myExt-x-private", 
                                      "zh", null, "CN", null, "a-myExt-x-private") ; }
    
    @Test public void parse_bad_01() { parseBad("i18n") ; }
    @Test public void parse_bad_02() { parseBad("i@n") ; }
    @Test public void parse_bad_03() { parseBad("123-abc") ; }
    @Test public void parse_bad_04() { parseBad("en-") ; }
    
    private static void parseGood(String input, String ex_output, String... ex_parts )
    {
      String[] parts = LangTag.parse(input) ;
      assertArrayEquals(ex_parts, parts) ;
      
      String output = LangTag.canonical(input) ;
      assertEquals(ex_output, output) ;
      
      assertTrue(LangTag.check(input)) ;
    }

    
    private static void parseBad(String input)
    {
        String[] parts = LangTag.parse(input) ;
        assertNull(parts) ;
        String output = LangTag.canonical(input) ;
        assertEquals(input, output) ;
        assertFalse(LangTag.check(input)) ;
    }

}
