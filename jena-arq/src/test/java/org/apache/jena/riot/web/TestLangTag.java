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
    
    private void testCanonical(String input, String ex_output) {
        String output = LangTag.canonical(input) ;
        assertEquals(ex_output, output) ;
    }

    // "x" extensions and irregular forms are left alone, including "sgn-be-fr" 

    // Mentioned in BCP 47 tests
//    @Test public void parseCanonical_01() { testCanonical("en-ca-x-ca","en-CA-x-ca"); }         // "x"
//    @Test public void parseCanonical_02() { testCanonical("EN-ca-X-Ca","en-CA-x-ca"); }
//    @Test public void parseCanonical_03() { testCanonical("En-Ca-X-Ca","en-CA-x-ca"); }
//    @Test public void parseCanonical_04() { testCanonical("SGN-BE-FR","sgn-BE-FR"); }   // Irregular
//    @Test public void parseCanonical_05() { testCanonical("sgn-be-fr","sgn-BE-FR"); }   // Irregular
//    @Test public void parseCanonical_06() { testCanonical("AZ-latn-x-LATN","az-Latn-x-latn"); }
//    @Test public void parseCanonical_07() { testCanonical("Az-latn-X-Latn","az-Latn-x-latn"); }
    
    @Test public void parseCanonical_10() { testCanonical("zh-hant",            "zh-Hant"); }
    @Test public void parseCanonical_11() { testCanonical("zh-latn-wadegile",   "zh-Latn-wadegile"); }
    @Test public void parseCanonical_12() { testCanonical("zh-latn-pinyin",     "zh-Latn-pinyin"); }
    @Test public void parseCanonical_13() { testCanonical("en-us",              "en-US"); }
    @Test public void parseCanonical_14() { testCanonical("EN-Gb",              "en-GB"); }
    @Test public void parseCanonical_15() { testCanonical("qqq-002",            "qqq-002"); }
    @Test public void parseCanonical_16() { testCanonical("ja-latn",            "ja-Latn"); }
    @Test public void parseCanonical_17() { testCanonical("x-local",            "x-local"); }
    @Test public void parseCanonical_18() { testCanonical("he-latn",            "he-Latn"); }
    @Test public void parseCanonical_19() { testCanonical("und",                "und"); }
    @Test public void parseCanonical_20() { testCanonical("nn",                 "nn"); }
    @Test public void parseCanonical_21() { testCanonical("ko-latn",            "ko-Latn"); }
    @Test public void parseCanonical_22() { testCanonical("ar-latn",            "ar-Latn"); }
    @Test public void parseCanonical_23() { testCanonical("la-x-liturgic",      "la-x-liturgic"); }
    @Test public void parseCanonical_24() { testCanonical("fa-x-middle",        "fa-x-middle"); }
    @Test public void parseCanonical_25() { testCanonical("qqq-142",            "qqq-142"); }
    @Test public void parseCanonical_26() { testCanonical("bnt",                "bnt"); }
    @Test public void parseCanonical_27() { testCanonical("grc-x-liturgic",     "grc-x-liturgic"); }
    @Test public void parseCanonical_28() { testCanonical("egy-Latn",           "egy-Latn"); }
    @Test public void parseCanonical_29() { testCanonical("la-x-medieval",      "la-x-medieval"); }
}
