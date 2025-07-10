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

package org.apache.jena.langtag;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;

@ParameterizedClass
@MethodSource("provideArgs")
public class TestLangTagFormat {


    private static Function<String, String> formatter1 = (s)-> LangTagRFC5646.create(s).str();
    private static Function<String, String> formatter2 = (s)-> LangTags.basicFormat(s);

    private record ArgPair(String name, Function<String, String> formatter) {}
    private static Stream<ArgPair> provideArgs() {
        return List.of
                (new ArgPair("LangTagRFC5646", formatter1),
                 new ArgPair("LangTagOps", formatter2)
                ).stream();
    }

    private final String formatterName;
    private final Function<String, String> formatter;

    public TestLangTagFormat(@SuppressWarnings("exports") ArgPair args) {
      this.formatterName = args.name;
      this.formatter = args.formatter;

    }

    @Test public void testBasicFormat01() { test("de", "de"); }
    @Test public void testBasicFormat02() { test("FR", "fr"); }
    @Test public void testBasicFormat03() { test("jA", "ja"); }
    @Test public void testBasicFormat04() { test("de-DE", "de-DE"); }
    @Test public void testBasicFormat05() { test("en-US", "en-US"); }
    @Test public void testBasicFormat06() { test("en-US-variant", "en-US-variant"); }

    // 419 is a region.
    @Test public void testBasicFormat10() { test("es-419", "es-419"); }
    @Test public void testBasicFormat11() { test("es-latn-419", "es-Latn-419"); }

    @Test public void testBasicFormat90() { test("en-GB-oed", "en-GB-oed"); }
    @Test public void testBasicFormat91() { test("EN-gb-OED", "en-GB-oed"); }

    // Taken from the examples in RFC 5646
    @Test public void testBasicFormat20() { test("zh-hant",         "zh-Hant"); }
    @Test public void testBasicFormat21() { test("sr-cyrl",         "sr-Cyrl"); }
    @Test public void testBasicFormat22() { test("sr-latn",         "sr-Latn"); }
    @Test public void testBasicFormat23() { test("zh-cmn-hans-cn",  "zh-cmn-Hans-CN"); }
    @Test public void testBasicFormat24() { test("cmn-hans-cn",     "cmn-Hans-CN"); }
    @Test public void testBasicFormat25() { test("zh-yue-hk",       "zh-yue-HK"); }
    @Test public void testBasicFormat26() { test("yue-hk",          "yue-HK"); }
    @Test public void testBasicFormat27() { test("zh-hans-cn",      "zh-Hans-CN"); }
    @Test public void testBasicFormat28() { test("sr-latn-rs",      "sr-Latn-RS"); }
    @Test public void testBasicFormat29() { test("sl-rozaj",        "sl-rozaj"); }
    @Test public void testBasicFormat30() { test("sl-rozaj-biske",  "sl-rozaj-biske"); }
    @Test public void testBasicFormat31() { test("de-ch-1901",      "de-CH-1901"); }
    @Test public void testBasicFormat32() { test("sl-it-nedis",     "sl-IT-nedis"); }
    @Test public void testBasicFormat33() { test("hy-latn-it-arevela",      "hy-Latn-IT-arevela"); }
    @Test public void testBasicFormat34() { test("de-ch-x-phonebk",         "de-CH-x-phonebk"); }
    @Test public void testBasicFormat35() { test("az-arab-x-aze-derbend",   "az-Arab-x-aze-derbend"); }
    @Test public void testBasicFormat36() { test("x-whatever",              "x-whatever"); }
    @Test public void testBasicFormat37() { test("qaa-qaaa-qm-x-southern",  "qaa-Qaaa-QM-x-southern"); }
    @Test public void testBasicFormat38() { test("de-qaaa",         "de-Qaaa"); }
    @Test public void testBasicFormat39() { test("en-us-u-islamcal",        "en-US-u-islamcal"); }
    @Test public void testBasicFormat40() { test("zh-cn-a-myext-x-private", "zh-CN-a-myext-x-private"); }
    @Test public void testBasicFormat41() { test("en-a-myext-b-another",    "en-a-myext-b-another"); }
    @Test public void testBasicFormat42() { test("en-123",          "en-123"); }
    @Test public void testBasicFormat43() { test("en-1234",         "en-1234"); }
    @Test public void testBasicFormat44() { test("en-brs-xxx-latn-gb",      "en-brs-xxx-Latn-GB"); }
    @Test public void testBasicFormat45() { test("EN-LATN",         "en-Latn"); }
    @Test public void testBasicFormat46() { test("en-latn-gb",      "en-Latn-GB"); }
    @Test public void testBasicFormat47() { test("de-ch-w-extend",  "de-CH-w-extend"); }
    @Test public void testBasicFormat48() { test("de-ch-x-phonebk-morech",  "de-CH-x-phonebk-morech"); }
    @Test public void testBasicFormat49() { test("x-private",       "x-private"); }
    @Test public void testBasicFormat50() { test("az-latn-x-latn",  "az-Latn-x-latn"); }
    @Test public void testBasicFormat51() { test("en-latn-X-DaTa",  "en-Latn-x-data"); }

    @Test public void irregular_01() { test("SGN-BE-FR",    "sgn-BE-FR"); }
    @Test public void irregular_02() { test("sgn-be-fr",    "sgn-BE-FR"); }
    @Test public void irregular_03() { test("sgn-be-nl",    "sgn-BE-NL"); }
    @Test public void irregular_04() { test("sgn-ch-de",    "sgn-CH-DE"); }
    @Test public void irregular_05() { test("i-klingon",    "i-klingon"); }

    // Mentioned in RFC 4646
    @Test public void parseCanonical_01() { test("en-ca-x-ca",          "en-CA-x-ca"); }
    @Test public void parseCanonical_02() { test("EN-ca-X-Ca",          "en-CA-x-ca"); }
    @Test public void parseCanonical_03() { test("En-Ca-X-Ca",          "en-CA-x-ca"); }
    @Test public void parseCanonical_04() { test("AZ-latn-x-LATN",      "az-Latn-x-latn"); }
    @Test public void parseCanonical_05() { test("Az-latn-X-Latn",      "az-Latn-x-latn"); }

    @Test public void parseCanonical_10() { test("zh-hant",             "zh-Hant"); }
    @Test public void parseCanonical_11() { test("zh-latn-wadegile",    "zh-Latn-wadegile"); }
    @Test public void parseCanonical_12() { test("zh-latn-pinyin",      "zh-Latn-pinyin"); }
    @Test public void parseCanonical_13() { test("en-us",               "en-US"); }
    @Test public void parseCanonical_14() { test("EN-Gb",               "en-GB"); }
    @Test public void parseCanonical_15() { test("qqq-002",             "qqq-002"); }
    @Test public void parseCanonical_16() { test("ja-latn",             "ja-Latn"); }
    @Test public void parseCanonical_17() { test("x-local",             "x-local"); }
    @Test public void parseCanonical_18() { test("he-latn",             "he-Latn"); }
    @Test public void parseCanonical_19() { test("und",                 "und"); }
    @Test public void parseCanonical_20() { test("nn",                  "nn"); }
    @Test public void parseCanonical_21() { test("ko-latn",             "ko-Latn"); }
    @Test public void parseCanonical_22() { test("ar-latn",             "ar-Latn"); }
    @Test public void parseCanonical_23() { test("la-x-liturgic",       "la-x-liturgic"); }
    @Test public void parseCanonical_24() { test("fa-x-middle",         "fa-x-middle"); }
    @Test public void parseCanonical_25() { test("qqq-142",             "qqq-142"); }
    @Test public void parseCanonical_26() { test("bnt",                 "bnt"); }
    @Test public void parseCanonical_27() { test("grc-x-liturgic",      "grc-x-liturgic"); }
    @Test public void parseCanonical_28() { test("egy-Latn",            "egy-Latn"); }
    @Test public void parseCanonical_29() { test("la-x-medieval",       "la-x-medieval"); }

    private void test(String langString, String expected) {
        String result = formatter.apply(langString);
        // JUnit4 argument order.
        assertEquals(expected, result, ()->formatterName+"("+langString+")");
    }
}
