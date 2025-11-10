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

package org.apache.jena.langtagx;

import org.apache.jena.langtag.LangTag;
import org.apache.jena.langtag.LangTagException;
import org.apache.jena.langtag.LangTags;
import org.apache.jena.shared.JenaException;

/**
 * This class defines the Jena-side policies for language tags
 * and maps operations to one implementation.
 * <p>
 * Language tags do not include text direction.
 *
 * {@link org.apache.jena.langtag.LangTags}.
 */
public class LangTagX {

    // If needed, convert to singleton.
    // public LangOps get() { ... }

    /**
     * Create a {@link LangTag} object, using the Jena system default
     * implementation of the {@code LangTag} interface.
     * The string must conform to the syntax defined in rules and syntax in
     * <a href="https://datatracker.ietf.org/doc/html/rfc5646">RFC 5646</a>
     */
    public static LangTag createLanguageTag(String langTagStr) {
        try {
            return org.apache.jena.langtag.LangTags.create(langTagStr);
        } catch (LangTagException ex) {
            throw convertException(ex);
        }
    }

    /*package*/ static final boolean legacyLangTag = false;
    /**
     * Prepare the language tag - apply formatting normalization, and always return a string.
     * If the input is invalid as a language tag, return the input as-is.
     * @throws JenaException on an all blank string.
     */
    public static String formatLanguageTag(String langTagStr) {
        if ( langTagStr == null )
            return langTagStr;
        if ( legacyLangTag )
            return langTagStr;
        if ( langTagStr.isEmpty() )
            return langTagStr;
        try {
            return LangTags.format(langTagStr);
        } catch (LangTagException ex) {
            if ( langTagStr.isBlank() )
                throw new JenaException("Language tag string is all white space");
            // Bad language tag. e.g. over long primary language or subtags.
            // Apply a more basic formatting - split into segments and
            // apply the subtag length rules.
            try {
                return LangTags.basicFormat(langTagStr);
            } catch (LangTagException ex2) {
                // Very bad
                return langTagStr;
            }
        }
    }

    /**
     * Check a string is valid as a language tag.
     * This function returns true or false and does not throw an exception.
     */
    public static boolean checkLanguageTag(String langTagStr) {
        return org.apache.jena.langtag.LangTags.check(langTagStr);
    }

    /**
     * Check a language tag string meets the Turtle(etc) and SPARQL grammar rule
     * for a language tag without initial text direction.
     * <p>
     * Passing this test does not guarantee the string is valid language tag. Use
     * {@link LangTagX#checkLanguageTag(String)} for validity checking.
     */
    public static boolean checkLanguageTagBasicSyntax(String langTagStr) {
        return org.apache.jena.langtag.LangTags.basicCheck(langTagStr);
    }

    /**
     * Check a string is valid as a language tag.
     * Throw a {@link JenaException} if it is not valid.
     */
    public static void requireValidLanguageTag(String langTagStr) {
        try {
            org.apache.jena.langtag.LangTags.requireValid(langTagStr);
        } catch (LangTagException ex)  {
            throw convertException(ex);
        }
    }

    /** Is @code{langTagStr1} the same language tag as @code{langTagStr2}? */
    public static boolean sameLanguageTagAs(String langTagStr1, String langTagStr2) {
        requireValidLanguageTag(langTagStr1);
        requireValidLanguageTag(langTagStr2);
        return langTagStr1.equalsIgnoreCase(langTagStr2);
    }

    private static JenaException convertException(LangTagException ex) {
        return new JenaException(ex.getMessage());
    }
}
