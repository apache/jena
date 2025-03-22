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
     * Format language tag.
     * This is the system-wide policy for formatting language tags.
     */
    public static String formatLangTag(String input) {
        return SysLangTag.formatLangTag(input);
    }

    /** Base formatter following
     * <a href="https://datatracker.ietf.org/doc/html/rfc5646#section-2.1.1">RFC 5646 section 2.1.1</a>
     * with the interpretation that "after singleton"
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
            InternalLangTag.error("Bad language string: %s", string);
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
}
