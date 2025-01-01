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

package org.apache.jena.riot.out.quoted;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.OutputUtils;
import org.apache.jena.atlas.lib.CharSpace;
import org.apache.jena.atlas.lib.Chars;
import org.apache.jena.atlas.lib.EscapeStr;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.SysRIOT;

/**
 * Writing URI strings for Turtle etc.
 * <p>If the URI string contains bad characters (control characters
 * x00 to x20 and characters {@literal <>}"{}|^`\) various ways to handle this are provided.
 * They cause either a different URI to written or an illegal one.
 * <p>
 * There is no way to write these illegal characters.
 * Percent-encoding is an encoding, not an escape mechanism.
 * It put actual 3 characters %-X-X.into the URI.
 * Even if the character is put in with a Unicode \-u escape, it is not a legal URI
 * and will fail URI parsing.
 */
public class QuotedURI {

    private final CharSpace charSpace;

    public QuotedURI() {
        this(CharSpace.UTF8);
    }

    /** Write a replacement for a bad character. */
    @FunctionalInterface
    private interface BadCharWriter { void writeChar(AWriter out, char ch); }

    /** Signal a bad character. */
    @FunctionalInterface
    private interface BadCharHandler { void badChar(int idx, String str, char ch); }

    public QuotedURI(CharSpace charSpace) {
        this.charSpace = charSpace;
    }

    /** Write a string for a URI on one line. */
    public void writeURI(AWriter w, String s) {
        // URIs do not have an escape mechanism. %-is an encoding (it is a change to the URI characters).
        //
        // The grammar rule: '<' ([^#x00-#x20<>"{}|^`\] | UCHAR)* '>'

        // The Turtle and SPARQL grammar rules are lax by design. They don't include the
        // whole grammar of RFC 3986. Instead, they allow a range of characters to have
        // a one line URI rule then require implementations check for legal URIs.

        w.print(Chars.CH_LT);
        if ( CharSpace.isAscii(charSpace) )
            EscapeStr.writeASCII(w, s);
        else
            systemWriteUnicodeURI(w, s);
        w.print(Chars.CH_GT);
    }

    /** Write a URI string in UTF-8.
     * This function is the policy for all Turtle/TriG/N-Tiples/N-Quads writing of URIs.
     * <p>
     * URIs strings sometimes contain bad characters.
     * Control characters x00 to x20 and characters <>"{}|^`\ are mentioned
     * in the grammars - Turtle et al., and SPARQL but there is also the requirement to be a legal URI
     * according to <a href="https://www.w3.org/TR/rdf-concepts/#iri-abnf">ABNF for URIs</a>
     * on top of the basic parsing rule.
     * <p>
     * Some control characters mess up the output: e.g. \n, \r, \f, BEL (U+0007) and others.
     * <p>
     * There are three choices:
     * <ol>
     * <li> Write whatever : Raw characters are emitted. {@link #writeRaw}.</li>
     * <li> \-u control chars and bad characters. This meets the basic grammar requirements of Turtle/SPARQL
     *      but not the requirement to be a legal IRI : {@link #writeUnicodeEscapeBadChars}.</li>
     * <li>Only worry about control characters, including ones that mess with the layout : {@link #writeUnicodeEscapeCtlChars}
     * </li>
     * <li>Warn about bad characters. {@link #writeWarnBadChars}</li>
     * </ol>
     *
     * {@link #writeUnicodeEscapeCtlChars} is like {@link #writeUnicodeEscapeBadChars}
     * except it only unicode-scapes control characters and lets characters, including space, through.
     */
    private static void systemWriteUnicodeURI(AWriter w, String s) {
        // Write as-is
        //writeDirect(w, s);

        // Write with unicode UCHAR escapes. Legal by the grammar rule but still not a legal URI.
        // All bad characters: Escape with \-u
        writeUnicodeEscapeBadChars(w, s);
        // Write with unicode UCHAR escapes just the control characters as UCHAR.
        // writeUnicodeEscapeCtlChars(w, s);

        // Percent encode. Makes a change of URI.
        //writePercentEncodedeBadChars(w, s);

        // warn-and-print and exception-or-write.
        //writeWarnBadChars(w, s);
        // writeExceptionOnBadChar(w,s);
    }

    // ---- Write choices.

    /*package*/ static void writeDirect(AWriter out, String uriStr) {
        out.print(uriStr);
    }

    /**
     * Write, but check for bad characters. If there are any, warn but still output the string.
     * Bad characters are those in the IRIREF productions:
     * controls chars, and illegal chars - space and {@code <>"{}|^`\}
     */
    /*package*/ static void writeWarnBadChars(AWriter out, String uriStr) {
        driverOnBadChars(out, uriStr, QuotedURI::warn);
    }

    /**
     * Check for bad characters. If there are any, throw an exception with no output.
     * Bad characters are those in the IRIREF productions:
     * controls chars, and illegal chars - space and {@code <>"{}|^`\}
     */
    /*package*/ static void writeExceptionOnBadChar(AWriter out, String uriStr) {
        driverOnBadChars(out, uriStr, QuotedURI::error);
    }

    /**
     * Write, using Unicode escapes for controls chars, and illegal chars -
     * space and <>"{}|^`\
     * This function can write strings that have a different meaning.
     * %-encoding is not an escape mechanism - there a really are three characters in the URI for %20
     */
    /*package*/ static void writeUnicodeEscapeBadChars(AWriter out, String uriStr) {
        driverWriteBadChars(out, uriStr, QuotedURI::escapeUnicode);
    }

    /**
     * Write, using Unicode escapes for controls chars, and illegal chars -
     * space and <>"{}|^`\
     * This function can write strings that have a different meaning.
     * %-encoding is not an escape mechanism - there a really are three characters in the URI for %20
     */
    /*package*/ static void writePercentEncodedeBadChars(AWriter out, String uriStr) {
        driverWriteBadChars(out, uriStr, QuotedURI::encodePercent);
    }

    /**
     * Write, using Unicode escapes for controls chars - protects against layout characters.
     * This function can write unparseable strings.
     */
    /*package*/ static void writeUnicodeEscapeCtlChars(AWriter out, String uriStr) {
        driverWriteControlChars(out, uriStr, QuotedURI::escapeUnicode);
    }

    /**
     * Check the string first character-by-character then directly print the whole
     * string. The {@code BadCharHandler} can throw an exception, which terminates
     * the function without any output; the exception is propagated.
     */
    private static void driverOnBadChars(AWriter out, String uriStr, BadCharHandler handler) {
        int len = uriStr.length();
        for (int i = 0; i < len; i++) {
            char c = uriStr.charAt(i);
            if ( isControlChar(c) ) {
                handler.badChar(i, uriStr,  c);
                continue;
            } else {
                // And also <>"{}|^`\
                switch(c) {
                    case ' ', '<', '>', '"', '{', '}', '|', '^', '`', '\\':
                    case '\u007F' : // DEL
                        handler.badChar(i, uriStr,  c);
                        continue;
                    default:
                }
            }
        }
        // OK - print
        out.print(uriStr);
    }

    /**
     * Process the string character-by-character.
     * Call a bad character handler on control or bad characters.
     */
    private static void driverWriteBadChars(AWriter out, String uriStr, BadCharWriter badCharWriter) {
        int len = uriStr.length();
        for (int i = 0; i < len; i++) {
            char c = uriStr.charAt(i);
            if ( isControlChar(c) ) {
                badCharWriter.writeChar(out, c);
                continue;
            }
            switch(c) {
                case ' ', '<', '>', '"', '{', '}', '|', '^', '`', '\\':
                case '\u007F' : // DEL
                    badCharWriter.writeChar(out, c);
                    break;
                default:
                    out.print(c);
            }
        }
    }

    /**
     * Process the string character-by-character.
     * Call a bad character handler only on control characters.
     */
    private static void driverWriteControlChars(AWriter out, String uriStr, BadCharWriter escaper) {
        int len = uriStr.length();
        for (int i = 0; i < len; i++) {
            char c = uriStr.charAt(i);
            if ( isControlChar(c) ) {
                escaper.writeChar(out, c);
                continue;
            }
        }
    }

    private static void escapeUnicode(AWriter out, char c) {
        out.print("\\u");
        OutputUtils.printHex(out, c, 4);
    }

    private static void encodePercent(AWriter out, char c) {
        if ( c <= 0xFF ) {
            // One byte
            out.print("%");
            OutputUtils.printHex(out, c, 2);
            return;
        }
        if ( c > 0xFF && c <= 0xFFFF) {
            // 2 byte, Hi-Lo
            int x = c;
            out.print("%");
            OutputUtils.printHex(out, x>>8, 2);
            out.print("%");
            OutputUtils.printHex(out, x|0xFF, 2);
        }
        throw new RiotException("Very bad character! "+Long.toHexString(c));
    }

    private static void warn(int idx, String str, char ch) {
        String msg = formattedMessage(idx, str, ch);
        FmtLog.warn(SysRIOT.getLogger(), msg);
    }

    private static void error(int idx, String str, char ch) {
        String msg = formattedMessage(idx, str, ch);
        throw new RiotException(msg);
    }

    private static String formattedMessage(int idx, String str, char ch) {
        String chStr = Character.toString(ch);
        if ( ch == ' ' )
            chStr = " ";
        if ( ch == '\t' )
            chStr = "\\t";
        if ( ch == '\n' )
            chStr = "\\n";
        str = displayUnicodeEscapeControlChars(str);
        return String.format("Bad character in URI <%s> at position %d: '%s' codepoint U+%04X", str, idx, chStr, (int)ch);
    }

    // ---- Display helpers. Not for output.

    /** Display form, indicating escape characters */
    private static String displayUnicodeEscapeControlChars(String uriStr) {
        IndentedLineBuffer out = new IndentedLineBuffer();
        writeDisplayUnicodeControlChars(out, uriStr);
        return out.asString();
    }

    /**
     * Write, using Unicode codepoints for controls chars, and illegal chars -
     * space and <>"{}|^`\
     * This function creates displayable strings.
     */
    private static void writeDisplayUnicodeControlChars(AWriter out, String uriStr) {
        int len = uriStr.length();
        for (int i = 0; i < len; i++) {
            char c = uriStr.charAt(i);
            // Well-known characters
            String s = displayUnicode(c);
            if ( s != null ) {
                out.print(s);
                continue;
            }
            // Other control chars
            if ( isControlChar(c) ) {
                displayUnicodeEscape(out, c);
                continue;
            }
            out.print(c);
        }
    }

    // ---- Helper functions.

    /**
     * Control chars mentioned in the specs.
     */
    private static boolean isControlChar(char c) {
        return c < 20;
        // Unicode has a another control char block at 007F to 09FF
        // In the range [U+0000, U+001F], or range [U+007F, U+009F]
        //return Character.isISOControl(c);
    }

    private static String displayUnicode(char c) {
        return switch (c) {
            case '\n' -> "[NL]";
            case '\r' -> "[CR]";
            case '\f' -> "[FF]";
            case ' '  -> "[space]";
            case '\t' -> "[tab]";
            // Other bad chars? <>"{}|^`\
            default -> null;
        };
    }

    private static void displayUnicodeEscape(AWriter out, char c) {
        out.print("[U+");
        OutputUtils.printHex(out, c, 4);
        out.print("]");
    }
}
