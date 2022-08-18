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

package org.apache.jena.irix;

/** Characters and character classes */
public class Chars3986 {
    // See Also RiotChars - SPARQL and Turtle parsing.

    /** End of file/string marker - this is not a valid Unicode codepoint. */
    public static final char EOF = 0xFFFF;

    //  pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
    //  pct-encoded   = "%" HEXDIG HEXDIG
    //
    //  unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
    //  iunreserved   = ALPHA / DIGIT / "-" / "." / "_" / "~" / ucschar
    //  reserved      = gen-delims / sub-delims
    //  gen-delims    = ":" / "/" / "?" / "#" / "[" / "]" / "@"
    //  sub-delims    = "!" / "$" / "&" / "'" / "(" / ")"
    //                / "*" / "+" / "," / ";" / "="
    //  ipchar        = iunreserved / pct-encoded / sub-delims / ":" / "@"
    //                = ipchar / ucschar

    /** RFC3986 pchar */
    public static boolean isPChar(char ch, String str, int posn) {
        return unreserved(ch) || isPctEncoded(ch, str, posn) || subDelims(ch) || ch == ':' || ch == '@';
    }

    /** RFC3987 ipchar */
    public static boolean isIPChar(char ch, String str, int posn) {
        return isPChar(ch, str, posn) || isUcsChar(ch);
    }

    /**
     * Test whether the character at location 'x' is percent-encoded. This operation
     * needs to look at next two characters if and only if ch is '%'.
     * <p>
     * This function looks ahead 2 characters which will be parsed but likely they
     * are in the L1 or L2 cache and the alternative is more complex logic (return
     * the new character position in some way).
     */
    public static boolean isPctEncoded(char ch, CharSequence s, int x) {
        if ( ch != '%' )
            return false;
        char ch1 = charAt(s, x+1);
        char ch2 = charAt(s, x+2);
        return percentCheck(x, ch1, ch2);
    }

    public static boolean isAlpha(char ch) {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z');
    }

    /** RFC3987: International alphabetic. */
    public static boolean isIAlpha(char ch) {
        return isAlpha(ch) || isUcsChar(ch);
    }

    // RFC 3987
    //  ucschar        = %xA0-D7FF / %xF900-FDCF / %xFDF0-FFEF
    //                 / %x10000-1FFFD / %x20000-2FFFD / %x30000-3FFFD
    //                 / %x40000-4FFFD / %x50000-5FFFD / %x60000-6FFFD
    //                 / %x70000-7FFFD / %x80000-8FFFD / %x90000-9FFFD
    //                 / %xA0000-AFFFD / %xB0000-BFFFD / %xC0000-CFFFD
    //                 / %xD0000-DFFFD / %xE1000-EFFFD

    // Surrogates are "hi-lo" : DC000-DFFF and D800-DFFF
    // We assume the java string is valid and surrogates are correctly in high-low pairs.

    public static boolean isUcsChar(char ch) {
            return range(ch, 0xA0, 0xD7FF)  || range(ch, 0xF900, 0xFDCF)  || range(ch, 0xFDF0, 0xFFEF)
                    // Allow surrogates.
                    || Character.isSurrogate(ch);
                // Java is 16 bits chars.
    //            || range(ch, 0x10000, 0x1FFFD) || range(ch, 0x20000, 0x2FFFD) || range(ch, 0x30000, 0x3FFFD)
    //            || range(ch, 0x40000, 0x4FFFD) || range(ch, 0x50000, 0x5FFFD) || range(ch, 0x60000, 0x6FFFD)
    //            || range(ch, 0x70000, 0x7FFFD) || range(ch, 0x80000, 0x8FFFD) || range(ch, 0x90000, 0x9FFFD)
    //            || range(ch, 0xA0000, 0xAFFFD) || range(ch, 0xB0000, 0xBFFFD) || range(ch, 0xC0000, 0xCFFFD)
    //            || range(ch, 0xD0000, 0xDFFFD) || range(ch, 0xE1000, 0xEFFFD)
        }

    // int version - includes support for beyond 16 bit chars.
    public static boolean int_isUcsChar(int ch) {
        // RFC 3987
        // ucschar    = %xA0-D7FF / %xF900-FDCF / %xFDF0-FFEF
        //            / %x10000-1FFFD / %x20000-2FFFD / %x30000-3FFFD
        //            / %x40000-4FFFD / %x50000-5FFFD / %x60000-6FFFD
        //            / %x70000-7FFFD / %x80000-8FFFD / %x90000-9FFFD
        //            / %xA0000-AFFFD / %xB0000-BFFFD / %xC0000-CFFFD
        //            / %xD0000-DFFFD / %xE1000-EFFFD
        boolean b = range(ch, 0xA0, 0xD7FF)  || range(ch, 0xF900, 0xFDCF)  || range(ch, 0xFDF0, 0xFFEF);
        if ( b )
            return true;
        if ( ch < 0x10000 )
            return false;
        // 32 bit checks.
        return
            range(ch, 0x10000, 0x1FFFD) || range(ch, 0x20000, 0x2FFFD) || range(ch, 0x30000, 0x3FFFD) ||
            range(ch, 0x40000, 0x4FFFD) || range(ch, 0x50000, 0x5FFFD) || range(ch, 0x60000, 0x6FFFD) ||
            range(ch, 0x70000, 0x7FFFD) || range(ch, 0x80000, 0x8FFFD) || range(ch, 0x90000, 0x9FFFD) ||
            range(ch, 0xA0000, 0xAFFFD) || range(ch, 0xB0000, 0xBFFFD) || range(ch, 0xC0000, 0xCFFFD) ||
            range(ch, 0xD0000, 0xDFFFD) || range(ch, 0xE1000, 0xEFFFD);
    }

    //iprivate       = %xE000-F8FF / %xF0000-FFFFD / %x100000-10FFFD
    public static boolean isIPrivate(char ch) {
        // Java is 16 bits chars.
        return range(ch, 0xE000, 0xF8FF) ;
    }

    public static boolean int_isIPrivate(int ch) {
        return range(ch, 0xE000, 0xF8FF) || range(ch, 0xF0000, 0xFFFFD) || range(ch, 0x100000, 0X10FFFD);
    }

    /** RFC 3986 : unreserved */
    public static boolean unreserved(char ch) {
        if ( isAlpha(ch) || isDigit(ch) )
            return true;
        switch(ch) {
            // unreserved
            case '-': case '.': case '_': case '~': return true;
        }
        return false;
    }

    /** RFC 3987 : iunreserved */
    public static boolean iunreserved(char ch) {
        if ( isIAlpha(ch) || isDigit(ch) )
            return true;
        switch(ch) {
            // unreserved
            case '-': case '.': case '_': case '~': return true;
        }
        return false;
    }

    /** RFC 3986 : sub-delims */
    public static boolean subDelims(char ch) {
        switch(ch) {
            case '!': case '$': case '&': case '\'': case '(': case ')':
            case '*': case '+': case ',': case ';': case '=': return true;
        }
        return false;
    }

    /** RFC 3986 : gen-delims / sub-delims */
    public static boolean genDelims(char ch) {
        switch(ch) {
            case ':': case '/': case '?': case '#': case '[': case ']': case '@': return true;
        }
        return false;
    }

    /** Return a display string for a character suitable for error messages. */
    public static String displayChar(char ch) {
        return String.format("%c (0x%04X)", ch, (int)ch);
    }

    private static boolean percentCheck(int idx, char ch1, char ch2) {
        if ( ch1 == EOF || ch2 == EOF ) {
            parseError(idx+1, "Incomplete %-encoded character");
            return false;
        }
        if ( isHexDigit(ch1) && isHexDigit(ch2) )
            return true;
        parseError(idx+1, "Bad %-encoded character ["+displayChar(ch1)+" "+displayChar(ch2)+"]");
        return false;
    }

    /** String.charAt except with an EOF character, not an exception. */
    public static char charAt(CharSequence str, int x) {
        if ( x >= str.length() )
            return EOF;
        return str.charAt(x);
    }

    /** Test whether a character is in a character range (both ends inclusive) */
    public static boolean range(int ch, int start, int finish) {
        return ch >= start && ch <= finish;
    }

    public static boolean isDigit(char ch) {
        return range(ch, '0', '9');
    }

    /**
     * {@code HEXDIG =  DIGIT / "A" / "B" / "C" / "D" / "E" / "F"}
     * but also lower case (non-normalized form). See RFC 3986 sec 6.2.2.1
     */
    public static boolean isHexDigit(char ch) {
        return range(ch, '0', '9' ) || range(ch, 'A', 'F' ) || range(ch, 'a', 'f' )  ;
    }

    public static int hexValue(char ch) {
        if ( range(ch, '0', '9' ) ) return ch-'0';
        if ( range(ch, 'A', 'F' ) ) return ch-'A'+10;
        if ( range(ch, 'a', 'f' ) ) return ch-'a'+10;
        return -1;
    }

    private static void parseError(int posn, String s) {
        // Choice of error handling.
    }
}
