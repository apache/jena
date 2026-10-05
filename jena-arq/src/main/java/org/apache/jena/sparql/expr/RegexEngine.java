/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.expr;

import java.text.CharacterIterator;
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
 * The two provided are the regular expression implement in Apache Xerces (2.11.0) and the JDK {@code java.util.regex}.
 * <p>
 * By default {@code java.util.regex} is used. It does not support the "x" flag.
 * <p>
 * The default is set by symbol {@code ARQ.regexImpl} (comand line {@code arq:regexImpl}) to either "javaRegex" or
 * "xercesRegex".
 *
 */
public abstract class RegexEngine {

    /**
     * Default maximum permitted regular expression evaluation time
     * @see #MAX_REGEX_EVALUATION_TIME
     */
    public static final long DEFAULT_MAX_REGEX_EVALUATION_TIME = 250;
    /**
     * Maximum permitted regular expression evaluation time, defaults to {@link #DEFAULT_MAX_REGEX_EVALUATION_TIME}
     * <p>
     * For some regular expression patterns the evaluation time can be excessive, especially if a pattern, or the
     * pattern combined with the data leads to catastrophic backtracking.  Therefore, we bound the evaluation time by
     * using a special {@link CharSequence}/{@link CharacterIterator} wrapper that enforces a time limit.  If the limit
     * is exceeded then an {@link ExprEvalException} is thrown.  This does impose a small performance penalty though
     * this generally should be negligible.
     * </p>
     * <p>
     * If an application makes extensive use of complex regular expressions, and you are certain that those are not
     * subject to catastrophic backtracking you can either increase the limit appropriately.  Or you can set to a
     * zero/negative value to disable the limit entirely.
     * </p>
     */
    public static long MAX_REGEX_EVALUATION_TIME = DEFAULT_MAX_REGEX_EVALUATION_TIME;

    public abstract boolean match(String string);

    // ---- ----

    public static enum RegexImpl {Java, Xerces}

    private static RegexImpl regexImpl = chooseRegexImpl(ARQ.getContext());

    private static RegexImpl chooseRegexImpl(Context context) {
        Object v = ARQ.getContext().get(ARQ.regexImpl);
        if (v == null) {
            return RegexImpl.Java;
        }

        if (v instanceof String str) {
            return switch (str) {
                case ARQConstants.strJavaRegex -> RegexImpl.Java;
                case ARQConstants.strXercesRegex -> RegexImpl.Xerces;
                default -> throw new IllegalArgumentException("Unexpected value: " + str);
            };
        }
        if (v instanceof Symbol sym) {
            Log.error(E_Regex.class, "Regex implementation context setting is a symbol : default to Java");
        } else {
            Log.warn(E_Regex.class, "Regex implementation not recognized : default to Java");
        }
        return RegexImpl.Java;
    }

    /**
     * For testing
     */
    public static void setRegexImpl(RegexImpl valRegexImpl) {
        Objects.requireNonNull(valRegexImpl);
        regexImpl = valRegexImpl;
    }

    // These functions are used E_StrReplace, SHACL and ShEx which only use Java regex.
    public static Pattern makePattern(String label, String patternStr, String flags) {
        try {
            int mask = 0;
            if (flags != null) {
                mask = makeMask(flags);
                if (flags.contains("q")) {
                    patternStr = Pattern.quote(patternStr);
                }
            }
            return Pattern.compile(patternStr, mask);
        } catch (PatternSyntaxException pEx) {
            throw new ExprEvalException(label + " pattern exception: " + pEx);
        }
    }

    public static int makeMask(String modifiers) {
        if (modifiers == null) {
            return 0;
        }
        int newMask = 0;
        for (int i = 0; i < modifiers.length(); i++) {
            switch (modifiers.charAt(i)) {
                case 'i' -> {
                    newMask |= Pattern.UNICODE_CASE;
                    newMask |= Pattern.CASE_INSENSITIVE;
                }
                case 'm' -> newMask |= Pattern.MULTILINE;
                case 's' -> newMask |= Pattern.DOTALL;
                case 'x' -> newMask |= Pattern.COMMENTS;
                // Handled separately.
                case 'q' -> {
                }
                default -> throw new ExprEvalException("Unsupported flag in regex modifiers: " + modifiers.charAt(i));
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
            Matcher m = MAX_REGEX_EVALUATION_TIME >= 0 ? regexPattern.matcher(new TimeBoundedCharSequence(s, MAX_REGEX_EVALUATION_TIME)) : regexPattern.matcher(s);
            return m.find();
        }
    }

    public static class RegexXerces extends RegexEngine {
        private final RegularExpression regexPattern;

        public RegexXerces(String pattern, String flags) {
            if (flags != null && flags.contains("q")) {
                flags = flags.replace("q", "");
                pattern = REUtil.quoteMeta(pattern);
            }
            regexPattern = makePattern(pattern, flags);
        }

        @Override
        public boolean match(String s) {
            if (MAX_REGEX_EVALUATION_TIME > 0) {
                return regexPattern.matches(new TimeBoundedCharSequence(s, MAX_REGEX_EVALUATION_TIME));
            } else {
                return regexPattern.matches(s);
            }
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

    /**
     * A {@link CharSequence} and {@link CharacterIterator} that time bounds operations so that if too much time is
     * spent accessing it {@link ExprEvalException}'s will be thrown.  This is used to protect the regular expression
     * evaluation against catastrophic backtracking.
     */
    public static final class TimeBoundedCharSequence implements CharSequence, CharacterIterator {

        private final CharSequence data;
        private final long timeLimit;
        private int iterIndex = 0;

        public TimeBoundedCharSequence(CharSequence characters, long timeLimitMillis) {
            this.data = characters;
            this.timeLimit = timeLimitMillis < System.currentTimeMillis() ? System.currentTimeMillis() + timeLimitMillis : timeLimitMillis;
            // Note always make an immediate time limit check as if we've been constructed as a sub-sequence then
            // the time limit may have been exceeded already
            checkTimeLimit();
        }

        private void checkTimeLimit() {
            if (System.currentTimeMillis() >= this.timeLimit) {
                throw new ExprEvalException("Regular expression time limit exceeded");
            }
        }

        @Override
        public int length() {
            checkTimeLimit();
            return this.data.length();
        }

        @Override
        public char charAt(int index) {
            checkTimeLimit();
            return this.data.charAt(index);
        }

        private void checkIndex(String checkType, int index) {
            if (index < 0 || index >= this.data.length()) {
                throw new IndexOutOfBoundsException(
                        String.format("%s %,d out of bounds, must be in range 0-%,d", checkType, index, this.data.length()));
            }
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            checkTimeLimit();
            if (start == end) {
                return new TimeBoundedCharSequence("", this.timeLimit);
            }
            checkIndex("Subsequence Start Index", start);
            checkIndex("Subsequence End Index", end - 1);
            if (start > end) {
                throw new IllegalArgumentException("Subsequence Start Index cannot be greater than Subsequence End Index");
            }
            return new TimeBoundedCharSubsequence(this, start, end);
        }

        @Override
        public char first() {
            this.iterIndex = this.getBeginIndex();
            if (this.length() == 0) {
                return CharacterIterator.DONE;
            }
            return this.charAt(this.iterIndex);
        }

        @Override
        public char last() {
            this.iterIndex = this.length() > 0 ? this.getEndIndex() - 1 : 0;
            if (this.length() == 0) {
                return CharacterIterator.DONE;
            }
            return this.charAt(this.iterIndex);
        }

        @Override
        public char current() {
            if (this.iterIndex >= this.length()) {
                return CharacterIterator.DONE;
            }
            return this.charAt(this.iterIndex);
        }

        @Override
        public char next() {
            this.iterIndex++;
            if (this.iterIndex >= this.getEndIndex()) {
                this.iterIndex = this.getEndIndex();
                return CharacterIterator.DONE;
            }
            return this.charAt(this.iterIndex);
        }

        @Override
        public char previous() {
            if (this.iterIndex == this.getBeginIndex()) {
                return CharacterIterator.DONE;
            }
            this.iterIndex--;
            return this.charAt(this.iterIndex);
        }

        @Override
        public char setIndex(int position) {
            this.iterIndex = position;
            if (this.iterIndex >= 0 && this.iterIndex < this.data.length()) {
                if (this.iterIndex == this.getEndIndex()) {
                    return CharacterIterator.DONE;
                }
                return charAt(this.iterIndex);
            } else {
                throw new IllegalArgumentException("Invalid iterator position " + position);
            }
        }

        @Override
        public int getBeginIndex() {
            checkTimeLimit();
            return 0;
        }

        @Override
        public int getEndIndex() {
            checkTimeLimit();
            return this.data.length();
        }

        @Override
        public int getIndex() {
            return this.iterIndex;
        }

        @Override
        public Object clone() {
            throw new IllegalStateException("Unable to clone");
        }

        @Override
        public String toString() {
            return this.data.toString();
        }

        private static final class TimeBoundedCharSubsequence implements CharSequence {
            private final TimeBoundedCharSequence parent;
            private final int startIndex, endIndex, length;

            public TimeBoundedCharSubsequence(TimeBoundedCharSequence parent, int startIndex, int endIndex) {
                this.parent = parent;
                this.startIndex = startIndex;
                this.endIndex = endIndex;
                this.length = this.endIndex - this.startIndex;
            }

            @Override
            public int length() {
                this.parent.checkTimeLimit();
                return this.length;
            }

            @Override
            public char charAt(int index) {
                return this.parent.charAt(this.startIndex + index);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return this.parent.subSequence(this.startIndex + start, this.startIndex + end);
            }

            @Override
            public String toString() {
                return this.parent.data.subSequence(this.startIndex, this.endIndex).toString();
            }
        }
    }

}
