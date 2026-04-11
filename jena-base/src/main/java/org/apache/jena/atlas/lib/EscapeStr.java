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

package org.apache.jena.atlas.lib;

import org.apache.jena.atlas.AtlasException;
import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.OutputUtils;
import org.apache.jena.atlas.io.StringWriterI;

/** String escape utilities */
public class EscapeStr
{
    /*
     * Escape characters in a string according to Turtle rules
     * for a single line string, using double quotes as the string delimiters
     * Delimiters are not included in the result.
     */
    public static String stringEsc(String s) {
        return stringEsc(s, Chars.CH_QUOTE2);
    }

    /*
     * Escape characters in a string according to Turtle rules,
     * where {@code quoteChar} is the delimiter.
     * Delimiters are not included in the result.
     */
    public static String stringEsc(String s, char quoteChar) {
        AWriter w = new StringWriterI();
        stringEsc(w, s, quoteChar, true, CharSpace.UTF8);
        return w.toString();
    }

    /** Write a string - basic escaping, no quote escaping. */
    public static void stringEsc(AWriter out, String s, boolean asciiOnly) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            // \\ Escape always possible.
            if (c == '\\') {
                out.print('\\');
                out.print(c);
                continue;
            }
            switch(c) {
                case '\n':  out.print("\\n"); continue;
                case '\t':  out.print("\\t"); continue;
                case '\r':  out.print("\\r"); continue;
                case '\f':  out.print("\\f"); continue;
                default:    // Drop through
            }
            if ( !asciiOnly )
                out.print(c);
            else
                writeCharAsASCII(out, c);
        }
    }

    /** String escape, with quote escaping, including option for multi-line 3 quote form. */
    public static void stringEsc(AWriter out, String s, char quoteChar, boolean singleLineString) {
        stringEsc(out, s, quoteChar, singleLineString, CharSpace.UTF8);
    }

    public static void stringEsc(AWriter out, String s, char quoteChar, boolean singleLineString, CharSpace charSpace) {
        boolean ascii = ( CharSpace.ASCII == charSpace );
        int len = s.length();
        int quotesInARow = 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            // \\ Escape always possible.
            if (c == '\\') {
                out.print('\\');
                out.print(c);
                continue;
            }
            if ( ! singleLineString ) {
                // Multiline string.
                if ( c == quoteChar ) {
                    quotesInARow++;
                    if ( (quotesInARow == 3) || (!singleLineString && (i == len - 1)) ) {
                        // Always quote the final character for multiline use
                        // otherwise it will run into the wrapping 3 quotes.
                        out.print("\\");
                        out.print(quoteChar);
                        quotesInARow = 0;
                        continue;
                    }
                } else {
                    quotesInARow = 0;
                }
            } else {
                // Single line.
                if ( c == quoteChar ) {
                    out.print("\\"); out.print(c); continue;
                }
                switch(c) {
                    case '\n':  out.print("\\n"); continue;
                    case '\t':  out.print("\\t"); continue;
                    case '\r':  out.print("\\r"); continue;
                    case '\f':  out.print("\\f"); continue;
                    default:    // Drop through
                }
            }

            if ( ascii ) {
                writeCharAsASCII(out, c);
                continue;
            }

            if ( c == '\uFFFD' ) {
                // Unicode replacement character: write as \-u escape
                // The text tokenizer raises warnings on raw U+FFFD. A replacement character is generated
                // if a decoding error occurs (e.g. ISO-8859-1 passed into UTF-8); there is no literal U+FFFD
                // in the original input. Written as a unicode escape is not treated as a warning.
                out.print("\\uFFFD");
                continue;
            }

            // Normal case!
            out.print(c);
        }
    }

    /** Write a string with Unicode to ASCII conversion using \-u escapes */
    public static void writeASCII(AWriter out, String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            writeCharAsASCII(out, c);
        }
    }

    /** Write a character with Unicode to ASCII conversion using \-u escapes */
    public static void writeCharAsASCII(AWriter out, char c) {
        if ( c >= 32 && c < 127 )
            out.print(c);
        else {
            // Outside the charset range.
            // Does not cover beyond 16 bits codepoints directly
            // (i.e. \U escapes) but Java keeps these as surrogate
            // pairs and will print as characters
            out.print("\\u");
            OutputUtils.printHex(out, c, 4);
        }
    }

    // Utilities to remove escapes

    /** Replace \ escapes (\\u, \t, \n etc) in a string */
    public static String unescapeStr(String s)
    { return unescapeStr(s, '\\'); }

    /** Replace \ escapes (\\u, \t, \n etc) in a string */
    public static String unescapeStr(String s, char escapeChar)
    { return unescape(s, escapeChar, false); }


    /** Unicode escapes  \-u and \-U only */
    public static String unescapeUnicode(String s) {
        return unescape(s, '\\', true);
    }

    // Main worker function for unescaping strings.
    public static String unescape(String s, char escape, boolean pointCodeOnly) {
        int i = s.indexOf(escape);

        if ( i == -1 )
            return s;

        // Dump the initial part straight into the string buffer
        StringBuilder sb = new StringBuilder(s.substring(0,i));

        for ( ; i < s.length() ; i++ ) {
            char ch = s.charAt(i);

            if ( ch != escape ) {
                sb.append(ch);
                continue;
            }

            if ( i >= s.length()-1 )
                throw new AtlasException("Illegal escape at end of string");
            // Move over the \
            i = i + 1;
            char ch2 = s.charAt(i);
            // \\u and \\U
            if ( ch2 == 'u' ) {
                // Maybe "\-u { ... }"
                int x = processDelimitedHex(sb, s, i);
                if ( x >= 0 ) {
                    i = x;
                    // Yes - done.
                    continue;
                }
                // \-u-xxxx
                if ( i+4 >= s.length() )
                    throw new AtlasException("\\u escape too short");
                int x4 = Hex.hexStringToInt(s, i+1, 4);
                sb.append((char)x4);
                // Jump 1 2 3 4 -- already skipped \ and u
                i = i+4;
                continue;
            }
            if ( ch2 == 'U' ) {
                if ( i+8 >= s.length() )
                    throw new AtlasException("\\U escape too short");
                int ch8 = Hex.hexStringToInt(s, i+1, 8);
                if ( Character.charCount(ch8) == 1 )
                    sb.append((char)ch8);
                else {
                    // See also TokenerText.insertCodepoint and TokenerText.readUnicodeEscape
                    // Convert to UTF-16. Note that the rest of any system this is used
                    // in must also respect codepoints and surrogate pairs.
                    if ( !Character.isDefined(ch8) && !Character.isSupplementaryCodePoint(ch8) )
                        throw new AtlasException(String.format("Illegal codepoint: 0x%04X", ch8));
                    if ( ch8 > Character.MAX_CODE_POINT )
                        throw new AtlasException(String.format("Illegal code point in \\U sequence value: 0x%08X", ch8));
                    char[] chars = Character.toChars(ch8);
                    sb.append(chars);
                }
                // Jump 1 2 3 4 5 6 7 8 -- already skipped \ and U
                i = i+8;
                continue;
            }

            // Are we doing just point code escapes?
            // If so, \X-anything else is legal as a literal "\" and "X"

            if ( pointCodeOnly ) {
                sb.append('\\');
                sb.append(ch2);
                continue;
            }

            char actualCh = 0;
            switch(ch2) {
                case 'n' -> actualCh = '\n';
                case 't' -> actualCh = '\t';
                case 'r' -> actualCh = '\r';
                case 'b' -> actualCh = '\b';
                case 'f' -> actualCh = '\f';
                case '\'' -> actualCh = '\'';
                case '\"' -> actualCh = '\"';
                case '\\' -> actualCh = '\\';
                default -> { throw new AtlasException("Unknown escape: \\"+ch2); }
            }

            if ( actualCh != 0 ) {
                sb.append(actualCh);
                continue;
            }

            // Failed to classify the escape sequence
            throw new AtlasException("Unknown escape: \\"+ch2);
        }
        return sb.toString();
    }

    // Parse "hex...}" - i.e. after "\ u {"
    // Return the new value of the loop index.
    // Return -1 if not delimited hex escape sequence
    private static int processDelimitedHex(StringBuilder sb, String s, int i) {
        // On entry, i is the index after 'u'
        // \-u-{hex...}
        if ( i+2 >= s.length()-1 )
            // +2 is for the {}
            throw new AtlasException("\\u escape too short");
        char ch3 = s.charAt(i+1);
        if ( ch3 != Chars.CH_LBRACE )
            return -1;
        // \-u-{hex...}
        i = i+2;    // Looking after the `{`
        int j = 0;
        int value = 0;
        while( i+j < s.length() ) {
            char ch4 = s.charAt(i+j);
            if ( ch4 == Chars.CH_RBRACE )
                break;
            int v = Hex.hexDigitToInt(ch4, -1);
            if ( v == -1 )
                throw new AtlasException(String.format("Bad character in delimitred hex sequence: %s, 0x%04X", Character.toString(ch4), ch4));
            value = (value<<4)+v ;
            j++;
            // Check length while waiting for }
            // 6 (max unicode range) or 8 (max 32 bit).
            if ( j > 6 )
                throw new AtlasException("\\u{} sequence too long");
        }
        if ( j == 0 )
            throw new AtlasException("Empty \\u{} sequence");

        int ch8 = value;
        if ( Character.charCount(ch8) == 1 )
            sb.append((char)ch8);
        else {
            if ( !Character.isDefined(ch8) && !Character.isSupplementaryCodePoint(ch8) )
                throw new AtlasException(String.format("Illegal codepoint: 0x%04X", ch8));
            if ( ch8 > Character.MAX_CODE_POINT )
                throw new AtlasException(String.format("Illegal code point in \\u{..} sequence value: 0x%08X", ch8));
            char[] chars = Character.toChars(ch8);
            sb.append(chars);
        }
        // Looking at the closing '}'
        i = i+j;
        return i;
    }
}