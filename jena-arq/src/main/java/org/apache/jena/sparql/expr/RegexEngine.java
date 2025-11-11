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

package org.apache.jena.sparql.expr;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.ext.xerces_regex.REUtil;
import org.apache.jena.ext.xerces_regex.RegexParseException;
import org.apache.jena.ext.xerces_regex.RegularExpression;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * Encapsulate a specific regular expression systems.
 * <p>
 * The two provided are the regular expression implement in Apache Xerces (2.11.0)
 * and the JDK {@code java.util.regex}.
 * <p>
 * By default {@code java.util.regex} is used. It does not support the "x" flag.
 * <p>
 * The default is set by symbol {@code ARQ.regexImpl} (comand line {@code arq:regexImpl})
 * to either "javaRegex" or "xercesRegex".
 *
 */
public abstract class RegexEngine {
    public abstract boolean match(String string);

    // ---- ----

    public static enum RegexImpl { Java, Xerces }

    private static RegexImpl regexImpl = chooseRegexImpl(ARQ.getContext());

    private static RegexImpl chooseRegexImpl(Context context) {
        Object v = ARQ.getContext().get(ARQ.regexImpl);
        if ( v == null )
            return RegexImpl.Java;

        if ( v instanceof String str ) {
            return switch(str) {
                case ARQConstants.strJavaRegex -> RegexImpl.Java;
                case ARQConstants.strXercesRegex -> RegexImpl.Xerces;
                default -> throw new IllegalArgumentException("Unexpected value: " + str);
            };
        }
        if ( v instanceof Symbol sym )
            Log.error(E_Regex.class, "Regex implementation context setting is a symbol : default to Java");
        else
            Log.warn(E_Regex.class, "Regex implementation not recognized : default to Java");
        return RegexImpl.Java;
    }

    /** For testing */
    public static void setRegexImpl(RegexImpl valRegexImpl) {
        Objects.requireNonNull(valRegexImpl);
        regexImpl = valRegexImpl;
    }

    // These functions are used E_StrReplace, SHACL and ShEx which only use Java regex.
    public static Pattern makePattern(String label, String patternStr, String flags) {
        try {
            int mask = 0;
            if ( flags != null ) {
                mask = makeMask(flags);
                if ( flags.contains("q") )
                    patternStr = Pattern.quote(patternStr);
            }
            return Pattern.compile(patternStr, mask);
        } catch (PatternSyntaxException pEx) {
            throw new ExprEvalException(label + " pattern exception: " + pEx);
        }
    }

    public static int makeMask(String modifiers) {
        if ( modifiers == null )
            return 0;
        int newMask = 0;
        for ( int i = 0 ; i < modifiers.length() ; i++ ) {
            switch (modifiers.charAt(i)) {
                case 'i' -> {
                    newMask |= Pattern.UNICODE_CASE;
                    newMask |= Pattern.CASE_INSENSITIVE;
                }
                case 'm' -> newMask |= Pattern.MULTILINE;
                case 's' -> newMask |= Pattern.DOTALL;
                case 'x' -> newMask |= Pattern.COMMENTS;
                // Handled separately.
                case 'q' -> {}
                default ->
                    throw new ExprEvalException("Unsupported flag in regex modifiers: " + modifiers.charAt(i));
            }
        }
        return newMask;
    }

    public static class RegexJava extends RegexEngine {
        private final Pattern regexPattern;

        public RegexJava(String pattern, String flags) {
            regexPattern = makePattern("Regex", pattern, flags);
        }

        @Override
        public boolean match(String s) {
            Matcher m = regexPattern.matcher(s);
            return m.find();
        }
    }

    public static class RegexXerces extends RegexEngine {
        private final RegularExpression regexPattern;

        public RegexXerces(String pattern, String flags) {
            if ( flags != null && flags.contains("q") ) {
                flags = flags.replace("q", "");
                pattern = REUtil.quoteMeta(pattern);
            }
            regexPattern = makePattern(pattern, flags);
        }

        @Override
        public boolean match(String s) {
            return regexPattern.matches(s);
        }

        private RegularExpression makePattern(String patternStr, String flags) {
            // flag q supported above.
            // Input : only m s i x
            // Check/modify flags.
            // Always "u", never patternStr
            // x: Remove whitespace characters (#x9, #xA, #xD and #x20) unless in [] classes
            try {
                return new RegularExpression(patternStr, flags);
            } catch (RegexParseException pEx) {
                throw new ExprEvalException("Regex: Pattern exception: " + pEx);
            }
        }
    }

    public static RegexEngine create(String pattern, String flags) {
        return switch (regexImpl) {
            case Java -> new RegexEngine.RegexJava(pattern, flags);
            case Xerces -> new RegexEngine.RegexXerces(pattern, flags);
            default -> new RegexEngine.RegexJava(pattern, flags);
        };
    }

}
