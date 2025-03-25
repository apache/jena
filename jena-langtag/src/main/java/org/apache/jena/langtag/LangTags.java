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

import static org.apache.jena.langtag.InternalLangTag.error;
import static org.apache.jena.langtag.InternalLangTag.str;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LangTags {

    /** Index of the language part */
    public static final int  idxLanguage  = 0 ;
    /** Index of the script part */
    public static final int  idxScript    = 1 ;
    /** Index of the region part */
    public static final int  idxRegion    = 2 ;
    /** Index of the variant part */
    public static final int  idxVariant   = 3 ;
    /** Index of all extensions */
    public static final int  idxExtension = 4 ;

    private static final int partsLength  = 5 ;

    /** @deprecated Compatibility operation (the behaviour of Jena 5.3.0 and earlier). To be removed. */
    @Deprecated(forRemoval = true)
    public static String[] parse(String languageTag) {
        try {
            LangTag langTag = SysLangTag.create(languageTag);
            if (langTag == null )
                return null;
            String result[] = new String[partsLength];

            result[idxLanguage] = langTag.getLanguage();
            result[idxScript] = langTag.getScript();
            result[idxRegion] = langTag.getRegion();
            result[idxVariant] = langTag.getVariant();
            // Legacy compatible.
            if ( langTag.getPrivateUse() == null )
                result[idxExtension] = langTag.getExtension();
            else if ( langTag.getExtension() == null )
                result[idxExtension] = langTag.getPrivateUse();
            else
                result[idxExtension] = langTag.getExtension()+"-"+langTag.getPrivateUse();
            return result;
        } catch (LangTagException ex) {
            return null;
        }
    }

    /**
     * Create a {@link LangTag} from a string
     * that meets the
     * <a href="https://datatracker.ietf.org/doc/html/rfc5646#section-2.1">syntax of RFC 5646</a>.
     * <p>
     * Throws {@link LangTagException} on bad syntax.
     */
    public static LangTag of(String string) {
        LangTag langTag =  SysLangTag.create(string);
        // Implements should not return null but just in case ...
        if ( langTag == null )
            throw new LangTagException("Bad syntax");
        return langTag;
    }

    /** Same as {@link #of(String)} */
    public static LangTag create(String string) {
        return of(string);
    }

    public static String canonical(String string) {
        LangTag langTag =  of(string);
        return langTag.str();
    }

    /** Check a string is valid as a language tag. */
    public static boolean check(String languageTag) {
        try {
            LangTag langTag = SysLangTag.create(languageTag);
            return (langTag != null );
        } catch (LangTagException ex) {
            return false;
        }
    }

    /**
     * Basic formatter following
     * <a href="https://datatracker.ietf.org/doc/html/rfc5646#section-2.1.1">RFC 5646 section 2.1.1</a>
     */
    public static String basicFormat(String string) {
        // with the interpretation that "after singleton" means anywhere after the singleton.
        if ( string == null )
            return null;
        if ( string.isEmpty() )
            return string;
        List<String> strings = InternalLangTag.splitOnDash(string);
        if ( strings == null ) {
            //return lowercase(string);
            error("Bad language string: %s", string);
        }
        StringBuilder sb = new StringBuilder(string.length());
        boolean singleton = false;
        boolean first = true;

        for ( String s : strings ) {
            if ( first ) {
                // language
                sb.append(InternalLangTag.lowercase(s));
                first = false;
                continue;
            }
            first = false;
            // All subtags after language
            sb.append('-');
            if ( singleton )
                // Always lowercase
                sb.append(InternalLangTag.lowercase(s));
            else {
                // case depends on ;length
                sb.append(InternalLangTag.strcase(s));
                if ( s.length() == 1 )
                    singleton = true;
            }
        }
        return sb.toString();
    }

    /** Is @code{langTag1} the same as @code{langTag2}? */
    public static boolean sameLangTagAs(LangTag langTag1, LangTag langTag2) {
        Objects.requireNonNull(langTag1);
        Objects.requireNonNull(langTag2);
        if ( langTag1 == langTag2 )
            return true;
        if ( ! Objects.equals(langTag1.getLanguage(),langTag2.getLanguage()) )
            return false;
        if ( ! Objects.equals(langTag1.getScript(),langTag2.getScript()) )
            return false;
        if ( ! Objects.equals(langTag1.getRegion(),langTag2.getRegion()) )
            return false;
        if ( ! Objects.equals(langTag1.getVariant(), langTag2.getVariant()) )
            return false;
        if ( ! Objects.equals(langTag1.getExtension(), langTag2.getExtension()) )
            return false;
        if ( ! Objects.equals(langTag1.getPrivateUse(), langTag2.getPrivateUse()) )
            return false;
        return true;
    }

    /**
     * Check a language tag string meets the Turtle(etc) and SPARQL grammar rule
     * for a language tag without initial text direction.
     * <p>
     * Passing this test does not guarantee the string is valid language tag. Use
     * {@link LangTags#check(String)} for validity checking.
     *
     * @returns true or false
     */
    public static boolean basicCheck(String string) {
        try {
            return basicCheckEx(string);
        } catch (LangTagException ex) {
            return false;
        }
    }

    /**
     * Check a language tag string meets the Turtle(etc) and SPARQL grammar rule
     * for a language tag without initial text direction.
     * <p>
     * Passing this test does not guarantee the string is valid language tag. Use
     * {@link LangTags#check(String)} for validity checking.
     *
     * @throws LangTagException
     */
    public static boolean basicCheckEx(String string) {
        boolean start = true;
        int lastSegmentStart = 0;

        for ( int idx = 0; idx < string.length(); idx++ ) {
            char ch = string.charAt(idx);
            if ( InternalLangTag.isA2ZN(ch) )
                continue;
            if ( ch == '-' ) {
                if ( idx == 0 ) {
                    error("'%s': starts with a '-' character", string);
                    return false;
                }
                if ( idx == lastSegmentStart ) {
                    error("'%s': two dashes", string);
                    return false;
                }
                lastSegmentStart = idx+1;
                continue;
            }
            // Not A2ZN, not '-'.
            error("Bad character: (0x%02X) '%s' index %d", (int)ch, str(ch), idx);
            return false;
        }
        // End of string.
        if ( lastSegmentStart == string.length() ) {
            error("'%s': Ends in a '-'", string);
            return false;
        }
        return true;
    }

    /**
     * Split a language tag based on dash separators
     * <p>
     * The string should be a legal language tag, at least by the general SPARQL/Turtle(etc) grammar rule.
     * @returns null on bad input syntax
     *
     * @see LangTags#check
     * @see LangTags#create
     */
    public static List<String> splitOnDash(String string) {
        try {
            return splitOnDashEx(string);
        } catch (LangTagException ex) {
            return null;
        }
    }

    /**
     * Split a language tag into subtags.
     * <p>
     * The string should be a legal language tag, at least by the general SPARQL/Turtle(etc) grammar rule.
     * @throw {@link LangTagException}
     *
     * @see LangTags#check
     * @see LangTags#create
     */
    public static List<String> splitOnDashEx(String string) {
        List<String> parts = new ArrayList<>();
        // Split efficiently based on [a-z][A-Z][0-9] units separated by "-", with meaning error messages.
        StringBuilder sb = new StringBuilder();

        boolean start = true;
        for ( int idx = 0; idx < string.length(); idx++ ) {
            char ch = string.charAt(idx);
            if ( InternalLangTag.isA2ZN(ch) ) {
                sb.append(ch);
                continue;
            }
            if ( ch == '-' ) {
                if ( idx == 0 ) {
                    error("'%s': starts with a '-' character", string);
                    return null;
                }
                String str = sb.toString();
                if ( str.isEmpty() ) {
                    error("'%s': two dashes", string);
                    return null;
                }
                parts.add(str);
                sb.setLength(0);
                continue;
            }
            error("Bad character: (0x%02X) '%s' index %d", (int)ch, str(ch), idx);
            return null;
        }
        String strLast = sb.toString();
        if ( strLast.isEmpty() ) {
            error("'%s': Ends in a '-'", string);
            return null;
        }
        parts.add(strLast);
        return parts;
    }
}

