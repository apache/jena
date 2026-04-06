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

package org.apache.jena.riot.writer.c14n;

import static org.apache.jena.riot.system.RiotChars.range;

import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.io.AWriter ;
import org.apache.jena.atlas.io.OutputUtils ;
import org.apache.jena.atlas.lib.CharSpace;
import org.apache.jena.atlas.lib.Chars;
import org.apache.jena.util.XML11Char;

/** String escape utilities */
public class EscapeStr_C14N
{
    /** String escape, with quote escaping, including option for multi-line 3 quote form. */
    public static void stringEsc(AWriter out, String s, char quoteChar) {
        stringEsc(out, s, quoteChar, true, CharSpace.UTF8);
    }

//    /** String escape, with quote escaping, including option for multi-line 3 quote form. */
//    public static void stringEsc(AWriter out, String s, char quoteChar, boolean singleLineString) {
//        stringEsc(out, s, quoteChar, singleLineString, CharSpace.UTF8);
//    }

    public static void stringEsc(AWriter out, String s, char quoteChar, boolean singleLineString, CharSpace charSpace) {
        boolean ascii = ( CharSpace.ASCII == charSpace ) ;
        int len = s.length() ;
        int quotesInARow = 0 ;

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            // \\ Escape always possible.
            if (c == '\\') {
                out.print('\\') ;
                out.print(c) ;
                continue ;
            }

            // Surrogates - print raw so that UTF-8 is generated.
            // XML 1.1 includes [#x10000-#x10FFFF]
            if ( false ) {
                // With checking which should not be necessary.
                if ( Character.isHighSurrogate(c) ) {
                    // Surrogates : value = 0x10000 + (H − 0xD800) × 0x400 + (L − 0xDC00)
                    // i.e. 0x10000 + (H − 0xD800) << 10 + (L − 0xDC00)
                    // i.e. 1024 "high" surrogates (D800–DBFF) and 1024 "low" surrogates (DC00–DFFF)
                    // making 1,048,576 values (2^20) from 0x10000 (0x400 is 1024 = 2^10)

                    // But AWriter does not write ints
                    // s.codePointAt(i);

                    // If we want our own checking messages.
                    i++;
                    if (i >= len )
                        throw new AtlasException("High surrogate at end of string");
                    char c2 = s.charAt(i);
                    if ( !Character.isLowSurrogate(c2) )
                        throw new AtlasException("High surrogate not followed by low surrogate");
                    out.write(c);
                    out.write(c2);
                    continue;
                }
                if ( Character.isLowSurrogate(c) )
                    throw new AtlasException("Low surrogate not following a high surrogate");
            }

            if ( ! singleLineString ) {
                // Multiline string.
                if ( c == quoteChar ) {
                    quotesInARow++ ;
                    if ( (quotesInARow == 3) || (!singleLineString && (i == len - 1)) ) {
                        // Always quote the final character for multiline use
                        // otherwise it will run into the wrapping 3 quotes.
                        out.print("\\");
                        out.print(quoteChar);
                        quotesInARow = 0;
                        continue;
                    }
                } else {
                    quotesInARow = 0 ;
                }
            } else {
                // Single line.
                if ( c == quoteChar ) {
                    out.print("\\"); out.print(c) ; continue ;
                }
                switch(c) {
                    // x08 - BS - Backspace
                    // x09 - HT - Horizontal tab
                    // x0A - LF - Line feed = NL - newline
                    // NOT x0B - VT - Vertical tab
                    // x0C - FF - Form Feed
                    // x0D - CR
                    case '\b':  out.print("\\b"); continue;
                    case '\t':  out.print("\\t"); continue;
                    case '\n':  out.print("\\n"); continue;
                    case '\f':  out.print("\\f"); continue;
                    case '\r':  out.print("\\r"); continue;
                    default:    // Drop through
                }
            }

            if ( ascii ) {
                writeCharAsASCII(out, c) ;
                continue;
            }

//            if ( c == '\uFFFD' ) {
//                // Unicode replacement character: write as \-u escape
//                // The text tokenizer raises warnings on raw U+FFFD. A replacement character is generated
//                // if a decoding error occurs (e.g. ISO-8859-1 passed into UTF-8); there is no literal U+FFFD
//                // in the original input. Written as a unicode escape is not treated as a warning.
//                out.print("\\uFFFD");
//                continue;
//            }

            // XML11Char.isXML11Valid(c);
            //  [2]     Char       ::=      [#x1-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]

            // N-Triples/ N-quads canonical form.
            // """
            // Characters in the range from U+0000 to U+0007, VT, characters in the range from U+000E to U+001F, DEL,
            // and characters not matching the Char production from [XML11] MUST be represented by UCHAR using a lowercase \ u with 4 HEXes.
            // """

            // Surrogates would be XML11Char.isXML11Invalid, they get printed as \-u
            // We want to pass then raw to the java->UTF-8 processor.
            if ( c <= 0x07 || c == Chars.VT || range(c, 0x0E , 0x1F ) || c == Chars.DEL || XML11Char.isXML11Invalid(c)) {

                if ( ! Character.isSurrogate(c) ) {
                    // Only 4 hex here (the Java string would have had surrogates pairs).
                    writeUnicodeEscape(out, c);
                    continue;
                }
            }

            // Normal case!
            out.print(c);
        }
    }

    // Choose between \-U 8 hex and \-u 4 hex.
    private static void writeUnicodeEscape(AWriter out, int c) {
        if ( c <= 0xFFFF )
            writeUnicodeEscape4(out, c);
        else
            writeUnicodeEscape8(out, c);
    }

    private static void writeUnicodeEscape4(AWriter out, int c) {
        out.print("\\u") ;
        OutputUtils.printHex(out, c, 4) ;
    }

    private static void writeUnicodeEscape8(AWriter out, int c) {
        out.print("\\U");
        OutputUtils.printHex(out, c, 8);
    }

    /** Write a string with Unicode to ASCII conversion using \-u escapes */
    public static void writeASCII(AWriter out, String s) {
        int len = s.length() ;
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
            out.print("\\u") ;
            OutputUtils.printHex(out, c, 4) ;
        }
    }
}