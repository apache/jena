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

public class LangExamples {

    // Examples from RFC 5646
    static String[] examples5646 = {
       "de",
       "fr",
       "ja",
       "i-enochian",        // (example of a grandfathered tag)
       "zh-Hant",           // (Chinese written using the Traditional Chinese script)
       "zh-Hans",           // (Chinese written using the Simplified Chinese script)
       "sr-Cyrl",           // (Serbian written using the Cyrillic script)
       "sr-Latn",           // (Serbian written using the Latin script)

    //Extended language subtags and their primary language subtag counterparts:
       "zh-cmn-Hans-CN",    // (Chinese, Mandarin, Simplified script, as used in China)
       "cmn-Hans-CN",       // (Mandarin Chinese, Simplified script, as used in China)
       "zh-yue-HK",         // (Chinese, Cantonese, as used in Hong Kong SAR)
       "yue-HK",            // (Cantonese, Chinese, as used in Hong Kong SAR)
    //Language-Script-Region:
       "zh-Hans-CN",        // (Chinese written using the Simplified script as used in mainland China)
       "sr-Latn-RS",        // (Serbian written using the Latin script as used in Serbia)
    //Language-Variant:
       "sl-rozaj",          // (Resian dialect of Slovenian)
       "sl-rozaj-biske",    // (San Giorgio dialect of Resian dialect of Slovenian)
       "sl-nedis",          // (Nadiza dialect of Slovenian)
    //Language-Region-Variant:
       "de-CH-1901",        // (German as used in Switzerland using the 1901 variant [orthography])
       "sl-IT-nedis",       // (Slovenian as used in Italy, Nadiza dialect)
    //Language-Script-Region-Variant:
       "hy-Latn-IT-arevela",    // (Eastern Armenian written in Latin script, as used in Italy)
    //Language-Region:
       "de-DE",             // (German for Germany)
       "en-US",             // (English as used in the United States)
       "es-419",            // (Spanish appropriate for the Latin America and Caribbean region using the UN region code)
    //Private use subtags:
       "de-CH-x-phonebk",
       "az-Arab-x-AZE-derbend",
    //Private use registry values:
       "x-whatever",        // (private use using the singleton 'x')
       "qaa-Qaaa-QM-x-southern", // (all private tags)
       "de-Qaaa",           // (German, with a private script)
       "sr-Latn-QM",        // (Serbian, Latin script, private region)
       "sr-Qaaa-RS",        // (Serbian, private script, for Serbia)
    //Tags that use extensions
    // (examples ONLY -- extensions MUST be defined by revision or update to this document, or by RFC):
       "en-US-u-islamcal",
       "zh-CN-a-myext-x-private",
       "en-a-myext-b-another"
    };

    static String[] examples5646_bad = {
    //Some Invalid Tags:
       "de-419-DE", // (two region tags)
       "a-DE" // (use of a single-character subtag in primary position; note
              // that there are a few grandfathered tags that start with "i-" that
              // are valid)
       //"ar-a-aaa-b-bbb-a-ccc" // (two extensions with same single-letterprefix)
    };
}
