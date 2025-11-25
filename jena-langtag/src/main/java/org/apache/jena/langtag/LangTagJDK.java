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

import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Objects;
import java.util.Set;

/**
 * LangTag parsing.
 * <p>
 * A layer over the JDK {@link Locale} and {@link Builder} to introduce a class without legacy langtag conversion.
 * {@link LangTag}.
 * <p>
 * This is not RFC 5646 compliant.
 * <ul>
 * <li>Does not handle language subtags (e.g. "zh-cmn-Hans-CN")</li>
 * <li>Does not handle grandfathered language tags e.g. "i-enochian"</li>
 * <li>Multiple variant subtags</li>
 * <li>Legacy "en-GB-oed" - "oed" is a 3 letter script (script is 4 by the grammar/)</li>
 * <ul>
 */
public final class LangTagJDK implements LangTag {
    private final String langTagAsGiven;
    private final String fmtString;
    private final String lang;
    private final String script;
    private final String region;
    private final String variant;
    private final String extension;
    // Not supported by the JDK (part of extensions).
    private final String privateUse;

    private static Locale.Builder locBuild = new Locale.Builder();

    public static LangTag create(String string) {
        try {
            locBuild.clear();
            locBuild.setLanguageTag(string);
            return asLangTag(string, locBuild);
        } catch (IllformedLocaleException ex) {
            return null;
        }
    }

    private LangTagJDK(String langTagAsGiven, String fmtString, String language, String script, String region, String variant, String extension, String privateUse) {
        this.langTagAsGiven = langTagAsGiven;
        this.fmtString  = Objects.requireNonNull(fmtString);
        this.lang       = maybe(language);
        this.script     = maybe(script);
        this.region     = maybe(region);
        this.variant    = maybe(variant);
        this.extension  = maybe(extension);
        this.privateUse = maybe(privateUse);
    }

    private static String maybe(String x) {
        // Choice.
        if ( x == null )
            return null;
        if ( x.isEmpty() )
            return null;
        return x;
    }

    @Override public String str() { return fmtString; }

    @Override public String getLanguage() { return lang; }
    @Override public String getScript() { return script; }
    @Override public String getRegion() { return region; }
    @Override public String getVariant() { return variant; }
    @Override public String getExtension() { return extension; }
    @Override public String getPrivateUse() { return privateUse; }

    public static String canonical(String str) {
        try {
            // Does not do conversion of language for ISO 639 codes that have changed.
            return locBuild.setLanguageTag(str).build().toLanguageTag();
        } catch (IllformedLocaleException ex) {
            return str;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(langTagAsGiven, fmtString,
                            lang, script, region, variant,
                            extension, privateUse);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( !(obj instanceof LangTagJDK) )
            return false;
        LangTagJDK other = (LangTagJDK)obj;
        return Objects.equals(lang, other.lang)
                && Objects.equals(script, other.script)
                && Objects.equals(region, other.region)
                && Objects.equals(variant, other.variant)
                && Objects.equals(extension, other.extension)
                && Objects.equals(privateUse, other.privateUse)
                && Objects.equals(langTagAsGiven, other.langTagAsGiven)
                && Objects.equals(fmtString, other.fmtString);
    }

    private static Character privateUseSingleton = Character.valueOf('x');

    private static LangTag asLangTag(String string, Locale.Builder locBuild) {
        Locale locale = locBuild.build();
        Set<Character> extkeys = locale.getExtensionKeys();
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for ( Character k : extkeys ) {
            String ext = locale.getExtension(k);
            StringBuilder sb = sb1;
            if ( privateUseSingleton.equals(k) )
                sb = sb2;
            if ( sb.length() != 0 )
                sb.append('-');
            sb.append(k);
            sb.append('-');
            sb.append(ext);
        }
        String extension = sb1.toString();
        String privateUse = sb2.toString();
        return new LangTagJDK(string,
                              locale.toLanguageTag(),
                              locale.getLanguage(),
                              locale.getScript(),
                              locale.getCountry(),
                              locale.getVariant(),
                              extension,
                              privateUse);
    }
}
