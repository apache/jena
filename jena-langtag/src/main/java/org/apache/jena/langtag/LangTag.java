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

/**
 * A language tag as a tuple of 5 strings (lang, script, region,
 * variant, extension) and
 * <p>
 * See {@link LangTagRFC5646} for generating {@code LangTag}s. Note this returns the old ISO code.
 * See the javadoc of {@link Locale#getLanguage()}.
 * <p>
 * {@link LangTagJDK} is an alternative version which uses the Java locale
 * built-in functionality and does not canonical language names (replace one name by another).
 * JDK Locale It is not fully RFC 5646 compliance
 * <p>
 * Language tags are BCP 47.
 * <p>
 * RFCs:
 * <ul>
 * <li><a href="https://tools.ietf.org/html/5646">RFC 5646</a> "Tags for Identifying Languages"
 * <li><a href="https://tools.ietf.org/html/4646">RFC 4646</a> "Tags for Identifying Languages"
 * <li><a href="https://tools.ietf.org/html/3066">RFC 3066</a> "Tags for the Identification of Languages"
 * </ul>
 * Related:
 * <ul>
 * <li><a href="https://tools.ietf.org/html/4647">RFC 4647</a> "Matching of Language Tags"
 * <li><a href="https://tools.ietf.org/html/4234">RFC 4232</a> "Augmented BNF for Syntax Specifications: ABNF"
 * </ul>
 */
public sealed interface LangTag permits LangTagJDK, LangTagRFC5646, LangTagRE {

    /**
     * Create a {@link LangTag} from a string
     * that meets the
     * <a href="https://datatracker.ietf.org/doc/html/rfc5646#section-2.1">syntax of RFC 5646</a>.
     *
     * @throws LangTagException if the string is syntacticly invalid.
     */
    public static LangTag of(String string) {
        LangTag langTag =  SysLangTag.create(string);
        // Implementations should not return null but just in case ...
        if ( langTag == null )
            throw new LangTagException("Bad syntax");
        return langTag;
    }

    /**
     * Formatted according to the RFC 5646 rules.
     * <p>
     * {@code toString()} should return the language tag with the same case as it was originally.
     */
    public String str();

    public String getLanguage();
    public String getScript();
    public String getRegion();
    public String getVariant();
    public String getExtension();
    public String getPrivateUse();

    @Override public int hashCode();
    @Override public boolean equals(Object other);
    @Override public String toString();
}
