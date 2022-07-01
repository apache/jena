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

package org.apache.jena.riot.system;

public class RiotChars
{
    // ---- Character classes

    public static boolean isAlpha(int codepoint) {
        return Character.isLetter(codepoint);
    }

    public static boolean isAlphaNumeric(int codepoint) {
        return Character.isLetterOrDigit(codepoint);
    }

    /** ASCII A-Z */
    public static boolean isA2Z(int ch) {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z');
    }

    /** ASCII A-Z or 0-9 */
    public static boolean isA2ZN(int ch) {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z') || range(ch, '0', '9');
    }

    /** ASCII 0-9 */
    public static boolean isDigit(int ch) {
        return range(ch, '0', '9');
    }

    public static boolean isWhitespace(int ch) {
        return isHorizontalWhitespace(ch) || isNewlineChar(ch) || ch == '\f';
    }

    public static boolean isHorizontalWhitespace(int ch) {
        return ch == ' ' || ch == '\t';
    }

    public static boolean isNewlineChar(int ch) {
        return ch == '\r' || ch == '\n';
    }

    /*
     * The token rules from SPARQL and Turtle.
     * BLANK_NODE_LABEL  ::= '_:' ( PN_CHARS_U | [0-9] ) ((PN_CHARS|'.')* PN_CHARS)?
     *
     * PNAME_NS          ::=  PN_PREFIX? ':'
     * PNAME_LN          ::=  PNAME_NS PN_LOCAL
     *
     * PN_CHARS_BASE     ::=  [A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF]
     *                   | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF]
     *                   | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD]
     *                   | [#x10000-#xEFFFF]
     *
     * PN_CHARS_U     ::=  PN_CHARS_BASE | '_'
     * PN_CHARS       ::=  PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
     *
     * PN_PREFIX      ::=  PN_CHARS_BASE ((PN_CHARS|'.')* PN_CHARS)?
     * PN_LOCAL       ::=  (PN_CHARS_U | ':' | [0-9] | PLX ) ((PN_CHARS | '.' | ':' | PLX)* (PN_CHARS | ':' | PLX) )?
     *
     * PLX            ::=  PERCENT | PN_LOCAL_ESC
     * PERCENT        ::=  '%' HEX HEX
     * HEX            ::=  [0-9] | [A-F] | [a-f]
     * PN_LOCAL_ESC   ::=  '\' ( '_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')'
     *                   | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%' )
     *
     *   Notes: PN_CHARS_BASE has a hole above #xD800 -- these are the surrogate pairs
     *   "high surrogates" (D800–DBFF) "low surrogates" (DC00–DFFF).
     */

    public static boolean isPNCharsBase(int ch) {
        // PN_CHARS_BASE ::= [A-Z] | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] |
        //                   [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] |
        //                   [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] |
        //                   [#x10000-#xEFFFF]
        return
            range(ch, 'a', 'z') || range(ch, 'A', 'Z') || range(ch, 0x00C0, 0x00D6)
                                || range(ch, 0x00D8, 0x00F6) || range(ch, 0x00F8, 0x02FF) ||
            range(ch, 0x0370, 0x037D) || range(ch, 0x037F, 0x1FFF) || range(ch, 0x200C, 0x200D) || range(ch, 0x2070, 0x218F) ||
            range(ch, 0x2C00, 0x2FEF) || range(ch, 0x3001, 0xD7FF) ||
            // Surrogate pairs
            range(ch, 0xD800, 0xDFFF) ||
            range(ch, 0xF900, 0xFDCF) || range(ch, 0xFDF0, 0xFFFD) ||
            range(ch, 0x10000, 0xEFFFF); // Outside the basic plane.
    }

    public static boolean isPNChars_U(int ch) {
        //PN_CHARS_BASE | '_'
        return isPNCharsBase(ch) || ( ch == '_' );
    }

    // Convenience addition.
    public static boolean isPNChars_U_N(int ch) {
        // PN_CHARS_U | [0-9]
        return isPNChars_U(ch) || isDigit(ch);
    }

    public static boolean isPNChars(int ch) {
        // PN_CHARS ::=  PN_CHARS_U | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
        return isPNChars_U(ch) || isDigit(ch) || ( ch == '-' ) || ch == 0x00B7 || range(ch, 0x300, 0x036F) || range(ch, 0x203F, 0x2040);
    }

    public static boolean isPN_LOCAL_ESC(char ch) {
        //[172s]  PN_LOCAL_ESC    ::=
        // '\' ('_' | '~' | '.' | '-' | '!' | '$' | '&' | "'" | '(' | ')' | '*' | '+' | ',' | ';' | '=' | '/' | '?' | '#' | '@' | '%')
        switch (ch) {
            case '_':  case '~': case '.': case '-': case '!': case '$': case '&':
            case '\'': case '(': case ')': case '*': case '+': case ',': case ';':
            case '=':  case '/': case '?': case '#': case '@': case '%':
                return true;
            default:
                return false;
        }
    }

    /** Hexadecimal character */
    public static boolean isHexChar(int ch) {
        return range(ch, '0', '9') || range(ch, 'a', 'f') || range(ch, 'A', 'F');
    }

    /** Hexadecimal character, only lower case a-f */
    public static boolean isHexCharLC(int ch) {
        return range(ch, '0', '9') || range(ch, 'a', 'f');
    }

    /** Hexadecimal character, only upper case A-F */
    public static boolean isHexCharUC(int ch) {
        return range(ch, '0', '9') || range(ch, 'A', 'F');
    }

    public static int valHexChar(int ch) {
        if ( range(ch, '0', '9') )
            return ch - '0';
        if ( range(ch, 'a', 'f') )
            return ch - 'a' + 10;
        if ( range(ch, 'A', 'F') )
            return ch - 'A' + 10;
        return -1;
    }

    /** Test whether a codepoint is a given range (both ends inclusive)*/
    public static boolean range(int ch, int a, int b) {
        return (ch >= a && ch <= b);
    }
}
