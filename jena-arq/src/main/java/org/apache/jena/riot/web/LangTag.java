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

package org.apache.jena.riot.web ;

import java.util.Locale ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.riot.system.RiotChars ;

/**
 * Language tags: support for parsing and canonicalization of case.
 * Grandfathered forms ("i-") are left untouched. Unsupported or syntactically
 * illegal forms are handled in canonicalization by doing nothing.
 * <ul>
 * <li>Language tags syntax: <a href="http://www.ietf.org/rfc/rfc4646.txt">RFC
 * 4646</a></li>
 * <li>Matching Language tags: <a href="http://www.ietf.org/rfc/rfc4647.txt">RFC
 * 4647</a></li>
 * <li>Language tags syntax: <a href="http://www.ietf.org/rfc/rfc5646.txt">RFC
 * 5646</a></li>
 * </ul>
 */

public class LangTag {
    // Valid language tag, not ireegular nor grandfathered.
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

    private LangTag() {}

    // Defined by BCP 47 which is currently RFC5646 which obsoletes RFC4646.

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
     * ABNF definition: <a href="http://www.ietf.org/rfc/rfc4234.txt">RFC
     * 4234</a>
     * 
     * Language-Tag = langtag / privateuse ; private use tag / grandfathered ;
     * grandfathered registrations
     * 
     * langtag = (language ["-" script] ["-" region]("-" variant)("-" extension)
     * ["-" privateuse])
     * 
     * language = (2*3ALPHA [ extlang ]) ; shortest ISO 639 code / 4ALPHA ;
     * reserved for future use / 5*8ALPHA ; registered language subtag
     * 
     * extlang = *3("-" 3ALPHA) ; reserved for future use
     * 
     * script = 4ALPHA ; ISO 15924 code
     * 
     * region = 2ALPHA ; ISO 3166 code / 3DIGIT ; UN M.49 code
     * 
     * variant = 5*8alphanum ; registered variants / (DIGIT 3alphanum)
     * 
     * extension = singleton 1*("-" (2*8alphanum))
     * 
     * singleton = %x41-57 / %x59-5A / %x61-77 / %x79-7A / DIGIT ; "a"-"w" /
     * "y"-"z" / "A"-"W" / "Y"-"Z" / "0"-"9" ; Single letters: x/X is reserved
     * for private use
     * 
     * privateuse = ("x"/"X") 1*("-" (1*8alphanum))
     * 
     * grandfathered = 1*3ALPHA 1*2("-" (2*8alphanum)) ; grandfathered
     * registration ; Note: i is the only singleton ; that starts a
     * grandfathered tag
     * 
     * alphanum = (ALPHA / DIGIT) ; letters and numbers
     */

    private static final String languageRE_1         = "(?:[a-zA-Z]{2,3}(?:-[a-zA-Z]{3}){0,3})" ;                   // including
                                                                                                                     // extlang
    private static final String languageRE_2         = "[a-zA-Z]{4}" ;
    private static final String languageRE_3         = "[a-zA-Z]{5,8}" ;
    private static final String language             = languageRE_1 + "|" + languageRE_2 + "|" + languageRE_3 ;

    private static final String script               = "[a-zA-Z]{4}" ;
    private static final String region               = "[a-zA-Z]{2}|[0-9]{3}" ;
    private static final String variant              = "[a-zA-Z0-9]{5,8}" ;
    private static final String extension1           = "(?:[a-zA-Z0-9]-[a-zA-Z0-9]{2,8})" ;
    private static final String extension            = extension1 + "(?:-" + extension1 + ")*" ;

    // private static final String singleton = null ;
    // private static final String privateuse = null ;
    // private static final String grandfathered = null ;

    private static final String langtag              = String.format("^(%s)(?:-(%s))?(?:-(%s))?(?:-(%s))?(?:-(%s))?$",
                                                                     language, script, region, variant, extension) ;

    // Private use forms "x-"
    private static final String privateuseRE         = "^[xX](-[a-zA-Z0-9]{1,8})*$" ;
    // In general, this can look like a langtag but there are no registered
    // forms that do so.
    // This is for the "i-" forms only.
    private static final String grandfatheredRE      = "i(?:-[a-zA-Z0-9]{2,8}){1,2}" ;

    private static Pattern      pattern              = Pattern.compile(langtag) ;
    private static Pattern      patternPrivateuse    = Pattern.compile(privateuseRE) ;
    private static Pattern      patternGrandfathered = Pattern.compile(grandfatheredRE) ;

    /**
     * Validate - basic syntax check for a language tags: [a-zA-Z]+ ('-'
     * [a-zA-Z0-9]+)*
     */
    public static boolean check(String languageTag) {
        int len = languageTag.length() ;
        int idx = 0 ;
        boolean first = true ;
        while (idx < languageTag.length()) {
            int idx2 = checkPart(languageTag, idx, first) ;
            first = false ;
            if ( idx2 == idx )
                // zero length part.
                return false ;
            idx = idx2 ;
            if ( idx == len )
                return true ;
            if ( languageTag.charAt(idx) != Chars.CH_DASH )
                return false ;
            idx++ ;
            if ( idx == len )
                // trailing DASH
                return false ;
        }
        return true ;
    }

    private static int checkPart(String languageTag, int idx, boolean leader) {
        for (; idx < languageTag.length(); idx++) {
            int ch = languageTag.charAt(idx) ;
            if ( leader ) {
                if ( RiotChars.isA2Z(ch) )
                    continue ;
            } else {
                if ( RiotChars.isA2ZN(ch) )
                    continue ;
            }
            // Not acceptable.
            return idx ;
        }
        // Off end.
        return idx ;
    }

    /**
     * Parse a langtag string and return it's parts in canonical case. See
     * constants for the array contents. Parts not present cause a null in the
     * return array.
     * 
     * @return Langtag parts, or null if the input string does not poarse as a
     *         lang tag.
     */
    public static String[] parse(String languageTag) {
        String[] parts = new String[partsLength] ;

        String x = pattern.toString() ;

        Pattern.compile(languageRE_1) ;

        Matcher m = pattern.matcher(languageTag) ;
        if ( !m.find() ) {
            m = patternPrivateuse.matcher(languageTag) ;
            if ( m.find() ) {
                // Place in the "extension" part
                parts[idxExtension] = m.group(0) ;
                return parts ;
            }

            m = patternGrandfathered.matcher(languageTag) ;

            if ( m.find() ) {
                // Place in the "extension" part
                parts[idxExtension] = m.group(0) ;
                return parts ;
            }

            // Give up.
            return null ;
        }

        int gc = m.groupCount() ;
        for (int i = 0; i < gc; i++)
            parts[i] = m.group(i + 1) ;

        parts[idxLanguage] = lowercase(parts[idxLanguage]) ;
        parts[idxScript] = strcase(parts[idxScript]) ;
        parts[idxRegion] = strcase(parts[idxRegion]) ;
        parts[idxVariant] = strcase(parts[idxVariant]) ;
        // parts[idxExtension] = strcase(parts[idxExtension]) ; // Leave
        // extensions alone.
        return parts ;
    }

    /** Canonicalize with the rules of RFC 4646 */
    public static String canonical(String str) {
        if ( str == null )
            return null ;
        String[] parts = parse(str) ;
        String x = canonical(parts) ;
        if ( x == null ) {
            // Could try to apply the rule case-seeting rules
            // even through it's not a conforming langtag.
            return str ;
        }
        return x ;
    }

    /**
     * Canonicalize with the rules of RFC 4646 "In this format, all non-initial
     * two-letter subtags are uppercase, all non-initial four-letter subtags are
     * titlecase, and all other subtags are lowercase." In addition, leave
     * extensions unchanged.
     */
    public static String canonical(String[] parts) {
        // We canonicalised parts on parsing.
        // RFC 5646 is slightly different.
        if ( parts == null )
            return null ;

        if ( parts[0] == null ) {
            // Grandfathered
            return parts[idxExtension] ;
        }

        StringBuilder sb = new StringBuilder() ;
        sb.append(parts[0]) ;
        for (int i = 1; i < parts.length; i++) {
            if ( parts[i] != null ) {
                sb.append("-") ;
                sb.append(parts[i]) ;
            }
        }
        return sb.toString() ;
    }

    private static String strcase(String string) {
        if ( string == null )
            return null ;
        if ( string.length() == 2 )
            return uppercase(string) ;
        if ( string.length() == 4 )
            return titlecase(string) ;
        return lowercase(string) ;
    }

    private static String lowercase(String string) {
        if ( string == null )
            return null ;
        return string.toLowerCase(Locale.ROOT) ;
    }

    private static String uppercase(String string) {
        if ( string == null )
            return null ;
        return string.toUpperCase(Locale.ROOT) ;
    }

    private static String titlecase(String string) {
        if ( string == null )
            return null ;
        char ch1 = string.charAt(0) ;
        ch1 = Character.toUpperCase(ch1) ;
        string = lowercase(string.substring(1)) ;
        return ch1 + string ;
    }
}
