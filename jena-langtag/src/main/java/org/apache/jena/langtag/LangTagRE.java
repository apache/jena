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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LangTagRE implements LangTag {

    public static LangTag create(String string) {
        LangTag langTagRE = new LangTagRE(string);
        return langTagRE;
    }

    private final String string;
    private final String[] parts;

    private LangTagRE(String string) {
        this.string = string;
        this.parts = LangTagByRE.parse(string);
    }

    @Override
    public String str() {
        return null;
    }

    @Override
    public String getLanguage() {
        return parts[idxLanguage];
    }

    @Override
    public String getScript() {
        return parts[idxScript];
    }

    @Override
    public String getRegion() {
        return parts[idxRegion];
    }

    @Override
    public String getVariant() {
        return parts[idxVariant];
    }

    @Override
    public String getExtension() {
        return parts[idxExtension];
    }

    @Override
    public String getPrivateUse() {
        return parts[idxPrivateUse];
    }

    /*package*/ static final int  idxLanguage  = 0;
    /*package*/ static final int  idxScript    = 1;
    /*package*/ static final int  idxRegion    = 2;
    /*package*/ static final int  idxVariant   = 3;
    /*package*/ static final int  idxExtension = 4;
    /*package*/ static final int  idxPrivateUse = 5;

    /** Language tag handled with regular expressions. */
    static class LangTagByRE {
        /**
         * Language tags: support for parsing and canonicalization of case.
         * Grandfathered forms ("i-") are left untouched. Unsupported or syntactically
         * illegal forms are handled in canonicalization by doing nothing.
         * <ul>
         * <li>Language tags syntax: <a href="http://www.ietf.org/rfc/rfc4646.txt">RFC 4646</a></li>
         * <li>Matching Language tags: <a href="http://www.ietf.org/rfc/rfc4647.txt">RFC 4647</a></li>
         * <li>Language tags syntax (BCP 47): <a href="http://www.ietf.org/rfc/rfc5646.txt">RFC 5646</a></li>
         * </ul>
         */

        // Valid language tag, not irregular, not grand-fathered.

        private static final int partsLength  = 6;

        private LangTagByRE() {}

        // Defined by BCP 47 which is currently RFC 5646 and which obsoletes RFC 4646.

        // Canonical forms:
        /*
         * RFC 4646 In this format, all non-initial two-letter subtags are
         * uppercase, all non-initial four-letter subtags are titlecase, and all
         * other subtags are lowercase.
         */
        /*
         * RFC 5646 An implementation can reproduce this format without accessing
         * the registry as follows. All subtags, including extension and private use
         * subtags, use lowercase letters with two exceptions: two-letter and
         * four-letter subtags that neither appear at the start of the tag nor occur
         * after singletons. Such two-letter subtags are all uppercase (as in the
         * tags "en-CA-x-ca" or "sgn-BE-FR") and four- letter subtags are titlecase
         * (as in the tag "az-Latn-x-latn").
         */

        /*
         * ABNF definition: <a href="http://www.ietf.org/rfc/rfc5646.txt">RFC 5646</a>
         *
Language-Tag  = langtag            ; normal language tags
               / privateuse         ; private use tag
               / grandfathered      ; grandfathered tags

 langtag       = language
                 ["-" script]
                 ["-" region]
                 *("-" variant)
                 *("-" extension)
                 ["-" privateuse]

 language      = 2*3ALPHA           ; shortest ISO 639 code
                 ["-" extlang]      ; sometimes followed by
                                    ; extended language subtags
               / 4ALPHA             ; or reserved for future use
               / 5*8ALPHA           ; or registered language subtag

 extlang       = 3ALPHA             ; selected ISO 639 codes
                 *2("-" 3ALPHA)     ; permanently reserved

 script        = 4ALPHA             ; ISO 15924 code

 region        = 2ALPHA             ; ISO 3166-1 code
               / 3DIGIT             ; UN M.49 code

 variant       = 5*8alphanum        ; registered variants
               / (DIGIT 3alphanum)

 extension     = singleton 1*("-" (2*8alphanum))

                                    ; Single alphanumerics
                                    ; "x" reserved for private use
 singleton     = DIGIT              ; 0 - 9
               / %x41-57            ; A - W
               / %x59-5A            ; Y - Z
               / %x61-77            ; a - w
               / %x79-7A            ; y - z

 privateuse    = "x" 1*("-" (1*8alphanum))

 grandfathered = irregular          ; non-redundant tags registered
               / regular            ; during the RFC 3066 era

 irregular     = "en-GB-oed"        ; irregular tags do not match
               / "i-ami"            ; the 'langtag' production and
               / "i-bnn"            ; would not otherwise be
               / "i-default"        ; considered 'well-formed'
               / "i-enochian"       ; These tags are all valid,
               / "i-hak"            ; but most are deprecated
               / "i-klingon"        ; in favor of more modern
               / "i-lux"            ; subtags or subtag
               / "i-mingo"          ; combination
               / "i-navajo"
               / "i-pwn"
               / "i-tao"
               / "i-tay"
               / "i-tsu"
               / "sgn-BE-FR"
               / "sgn-BE-NL"
               / "sgn-CH-DE"

 regular       = "art-lojban"       ; these tags match the 'langtag'
               / "cel-gaulish"      ; production, but their subtags
               / "no-bok"           ; are not extended language
               / "no-nyn"           ; or variant subtags: their meaning
               / "zh-guoyu"         ; is defined by their registration
               / "zh-hakka"         ; and all of these are deprecated
               / "zh-min"           ; in favor of a more modern
               / "zh-min-nan"       ; subtag or sequence of subtags
               / "zh-xiang"

 alphanum      = (ALPHA / DIGIT)    ; letters and numbers
          */

        private static final String languageRE_1         = "(?:[a-zA-Z]{2,3}(?:-[a-zA-Z]{3}){0,3})";
        private static final String languageRE_2         = "[a-zA-Z]{4}";
        private static final String languageRE_3         = "[a-zA-Z]{5,8}";
        private static final String language             = languageRE_1 + "|" + languageRE_2 + "|" + languageRE_3;

        private static final String script               = "[a-zA-Z]{4}";
        private static final String region               = "[a-zA-Z]{2}|[0-9]{3}";

        private static final String variant1             = "(?:[a-zA-Z0-9]{5,8}|[0-9][a-zA-Z0-9]{3})";
        private static final String variant              = variant1 + "(?:-" + variant1 + ")*";

        private static final String extension1           = "(?:[a-wyzA-WYZ0-9](?:-[a-zA-Z0-9]{2,8})+)"; // Not 'x'
        private static final String extension            = extension1 + "(?:-" + extension1 + ")*";

        private static final String privateuse           = "[xX](?:-[a-zA-Z0-9]{1,8})+";

        private static final String langtag              = String.format("^(%s)(?:-(%s))?(?:-(%s))?(?:-(%s))*(?:-(%s))?(?:-(%s))?$",
                                                                         language, script, region, variant, extension, privateuse);

        // This is for the "i-" forms only.
        private static final String grandfatheredRE      = "^i(?:-[a-zA-Z0-9]{2,8}){1,2}$";
        private static final String privateUseLangRE     = "^"+privateuse+"$";

        private static Pattern      pattern              = Pattern.compile(langtag);
        private static Pattern      patternGrandfathered = Pattern.compile(grandfatheredRE);
        private static Pattern      privateUseLang       = Pattern.compile(privateUseLangRE);
        private static Pattern      enOED                 = Pattern.compile("en-GB-oed", Pattern.CASE_INSENSITIVE);

        /**
         * Validate - basic syntax check for a language tags: [a-zA-Z]+ ('-'[a-zA-Z0-9]+)*
         */
        /*package*/ static boolean check(String languageTag) {
            int len = languageTag.length();
            int idx = 0;
            boolean first = true;
            while (idx < languageTag.length()) {
                int idx2 = checkPart(languageTag, idx, first);
                first = false;
                if ( idx2 == idx )
                    // zero length part.
                    return false;
                idx = idx2;
                if ( idx == len )
                    return true;
                if ( languageTag.charAt(idx) != '-' )
                    return false;
                idx++;
                if ( idx == len )
                    // trailing DASH
                    return false;
            }
            return true;
        }

        private static int checkPart(String languageTag, int idx, boolean leader) {
            for (; idx < languageTag.length(); idx++) {
                int ch = languageTag.charAt(idx);
                if ( leader ) {
                    if ( InternalLangTag.isA2Z(ch) )
                        continue;
                } else {
                    if ( InternalLangTag.isA2ZN(ch) )
                        continue;
                }
                // Not acceptable.
                return idx;
            }
            // Off end.
            return idx;
        }

        /**
         * Parse a langtag string and return it's parts in canonical case. See
         * constants for the array contents. Parts not present cause a null in
         * the return array.
         *
         * @return Langtag parts, or null if the input string does not parse as a lang tag.
         */
        /*package*/ static String[] parse(String languageTag) {
            String[] parts = new String[partsLength];

            Matcher m = pattern.matcher(languageTag);
            if ( !m.find() ) {
                m = patternGrandfathered.matcher(languageTag);
                if ( m.find() ) {
                    parts[idxLanguage] = m.group(0);
                    return parts;
                }
                // Private use language, not extension.
                m = privateUseLang.matcher(languageTag);
                if ( m.find() ) {
                    parts[idxPrivateUse] = m.group(0);
                    return parts;
                }

                // Irregular
                m = enOED.matcher(languageTag);
                if ( m.find() ) {
                    parts[idxLanguage] = "en";
                    parts[idxRegion] = "GB";
                    parts[idxVariant] = "oed";
                    return parts;
                }

                // Give up.
                return null;
            }

            int gc = m.groupCount();
            for (int i = 0; i < gc; i++)
                parts[i] = m.group(i + 1);

            parts[idxLanguage] = lowercase(parts[idxLanguage]);
            parts[idxScript] = titlecase(parts[idxScript]);
            parts[idxRegion] = uppercase(parts[idxRegion]);
            parts[idxVariant] = lowercase(parts[idxVariant]);
            parts[idxExtension] = lowercase(parts[idxExtension]);
            parts[idxPrivateUse] = lowercase(parts[idxPrivateUse]);
            return parts;
        }

        /** Canonicalize with the rules of RFC 4646, or RFC 5646 without replacement of preferred form. */
        /*package*/ static String canonical(String str) {
            if ( str == null )
                return null;
            String[] parts = parse(str);
            String x = canonical(parts);
            if ( x == null ) {
                // Could try to apply the rule case-setting rules
                // even through it's not a conforming langtag.
                return str;
            }
            return x;
        }

        /**
         * Canonicalize with the rules of RFC 4646 "In this format, all non-initial
         * two-letter subtags are uppercase, all non-initial four-letter subtags are
         * titlecase, and all other subtags are lowercase." In addition, leave
         * extensions unchanged.
         * <p>
         * This is the same as RFC5646 without replacement of preferred form
         * or consulting the registry.
         */
        /*package*/ static String canonical(String[] parts) {
            // We canonicalised parts on parsing.
            if ( parts == null )
                return null;

            if ( parts[0] == null ) {
                // Grandfathered
                return parts[idxExtension];
            }

            StringBuilder sb = new StringBuilder();
            sb.append(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                if ( parts[i] != null ) {
                    sb.append("-");
                    sb.append(parts[i]);
                }
            }
            return sb.toString();
        }

        // Teh basic formatting rule.
        private static String strcase_unused(String string) {
            if ( string == null )
                return null;
            if ( string.length() == 2 )
                return uppercase(string);
            if ( string.length() == 4 )
                return titlecase(string);
            return lowercase(string);
        }

        private static String lowercase(String string) {
            if ( string == null )
                return null;
            return string.toLowerCase(Locale.ROOT);
        }

        private static String uppercase(String string) {
            if ( string == null )
                return null;
            return string.toUpperCase(Locale.ROOT);
        }

        private static String titlecase(String string) {
            if ( string == null )
                return null;
            char ch1 = string.charAt(0);
            ch1 = Character.toUpperCase(ch1);
            string = lowercase(string.substring(1));
            return ch1 + string;
        }
    }
}
