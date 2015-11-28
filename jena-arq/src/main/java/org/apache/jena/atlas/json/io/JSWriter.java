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

package org.apache.jena.atlas.json.io ;

import static org.apache.jena.atlas.lib.Chars.CH_QUOTE1 ;
import static org.apache.jena.atlas.lib.Chars.CH_QUOTE2 ;
import static org.apache.jena.atlas.lib.Chars.CH_ZERO ;

import java.io.OutputStream ;
import java.math.BigDecimal ;
import java.util.ArrayDeque ;
import java.util.Deque ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.BitsInt ;
import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.atlas.lib.Ref ;

/**
 * A low level streaming JSON writer - assumes correct sequence of calls (e.g.
 * keys in objects). Useful when writing JSON directly from some other structure
 */

public class JSWriter {
    // Isn't this just a weird builder?
    // This is broken for arrays for key then startObject
    protected IndentedWriter out = IndentedWriter.stdout ;

    public JSWriter() {
        this(IndentedWriter.stdout) ;
    }

    public JSWriter(OutputStream ps) { this(new IndentedWriter(ps)) ; }

    public JSWriter(IndentedWriter ps) { out = ps ; }

    public void startOutput() {}

    public void finishOutput() { out.flush() ; }

    // These apply in nested and flat modes (the difference is controlled by the
    // IndentedWriter

    public static final String ArrayStart    = "[ " ;
    public static final String ArrayFinish   = " ]" ;
    public static final String ArraySep      = ", " ;

    public static final String ObjectStart   = "{ " ;
    public static final String ObjectFinish  = "}" ;
    public static final String ObjectSep     = " ," ;
    public static final String ObjectPairSep = " : " ;

    // Remember whether we are in the first element of a compound 
    // (object or array).
    Deque<Ref<Boolean>> stack = new ArrayDeque<>() ;

    public void startObject() {
        startCompound() ;
        out.print(ObjectStart) ;
        out.incIndent() ;
    }

    public void finishObject() {
        out.decIndent() ;
        if ( isFirst() )
            out.print(ObjectFinish) ;
        else {
            out.ensureStartOfLine() ;
            out.println(ObjectFinish) ;
        }
        finishCompound() ;
    }

    public void key(String key) {
        if ( isFirst() ) {
            out.println() ;
            setNotFirst() ;
        } else
            out.println(ObjectSep) ;
        value(key) ;
        out.print(ObjectPairSep) ;
        // Ready to start the pair value.
    }

    // "Pair" is the name used in the JSON spec.
    public void pair(String key, String value) {
        key(key) ;
        value(value) ;
    }

    public void pair(String key, boolean val) {
        key(key) ;
        value(val) ;
    }

    public void pair(String key, long val) {
        key(key) ;
        value(val) ;
    }
    
    public void pair(String key, Number val) {
        key(key) ;
        value(val) ;
    }

    public void startArray() {
        startCompound() ;
        out.print(ArrayStart) ;
        // Messy with objects out.incIndent() ;
    }

    public void finishArray() {
        // out.decIndent() ;
        out.print(ArrayFinish) ; // Leave on same line.
        finishCompound() ;
    }

    public void arrayElement(String str) {
        arrayElementProcess() ;
        value(str) ;
    }

    private void arrayElementProcess() {
        if ( isFirst() )
            setNotFirst() ;
        else
            out.print(ArraySep) ;
    }

    public void arrayElement(boolean b) {
        arrayElementProcess() ;
        value(b) ;
    }

    public void arrayElement(long integer) {
        arrayElementProcess() ;
        value(integer) ;
    }

    /**
     * Useful if you are manually creating arrays and so need to print array
     * separators yourself
     */
    public void arraySep() {
        out.print(ArraySep) ;
    }

    public static String outputQuotedString(String string) {
        IndentedLineBuffer b = new IndentedLineBuffer() ;
        outputQuotedString(b, string) ;
        return b.asString() ;
    }

    /*
     * Output a JSON string with escaping. 
     * \" \\ \/ \b \f \n \r \t control
     * characters (def?) \ u four-hex-digits 
     */
    public static void outputQuotedString(IndentedWriter out, String string) {
        outputQuotedString(out, string, false) ;
    }

    /*
     * Output a JSON string with escaping. Optionally allow base words (unquoted
     * strings) which, for member names, is legal javascript but not legal JSON.
     * The Jena JSON parser accepts them.
     */
    public static void outputQuotedString(IndentedWriter out, String string, boolean allowBareWords) {
        char quoteChar = CH_QUOTE2 ;
        int len = string.length() ;

        if ( allowBareWords ) {
            boolean safeBareWord = true ;
            if ( len != 0 )
                safeBareWord = isA2Z(string.charAt(0)) ;

            if ( safeBareWord ) {
                for (int i = 1; i < len; i++) {
                    char ch = string.charAt(i) ;
                    if ( isA2ZN(ch) )
                        continue ;
                    safeBareWord = false ;
                    break ;
                }
            }
            if ( safeBareWord ) {
                // It's safe as a bare word in JavaScript.
                out.print(string) ;
                return ;
            }
        }

        if ( allowBareWords )
            quoteChar = CH_QUOTE1 ;

        out.print(quoteChar) ;
        for (int i = 0; i < len; i++) {
            char ch = string.charAt(i) ;
            if ( ch == quoteChar ) {
                esc(out, quoteChar) ;
                continue ;
            }

            switch (ch) {
            // Done in default. Only \" is legal JSON.
            // case '"': esc(out, '"') ; break ;
            // case '\'': esc(out, '\'') ; break ;
                case '\\' :
                    esc(out, '\\') ;
                    break ;
                case '/' :
                    // Avoid </ which confuses if it's in HTML (this is from
                    // json.org)
                    if ( i > 0 && string.charAt(i - 1) == '<' )
                        esc(out, '/') ;
                    else
                        out.print(ch) ;
                    break ;
                case '\b' :
                    esc(out, 'b') ;
                    break ;
                case '\f' :
                    esc(out, 'f') ;
                    break ;
                case '\n' :
                    esc(out, 'n') ;
                    break ;
                case '\r' :
                    esc(out, 'r') ;
                    break ;
                case '\t' :
                    esc(out, 't') ;
                    break ;
                default :

                    if ( ch == quoteChar ) {
                        esc(out, '"') ;
                        break ;
                    }

                    // Character.isISOControl(ch) ; //00-1F, 7F-9F
                    // This is more than Character.isISOControl

                    if ( ch < ' ' || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch < '\u2100') ) {
                        out.print("\\u") ;
                        int x = ch ;
                        x = oneHex(out, x, 3) ;
                        x = oneHex(out, x, 2) ;
                        x = oneHex(out, x, 1) ;
                        x = oneHex(out, x, 0) ;
                        break ;
                    }

                    out.print(ch) ;
                    break ;
            }
        }
        if ( quoteChar != CH_ZERO )
            out.print(quoteChar) ;
    }

    private void startCompound() {
        stack.push(new Ref<>(true)) ;
    }

    private void finishCompound() {
        stack.pop() ;
    }

    private boolean isFirst() {
        return stack.peek().getValue() ;
    }

    private void setNotFirst() {
        stack.peek().setValue(false) ;
    }

    private void value(String x) {
        out.print(outputQuotedString(x)) ;
    }

    private void value(boolean b) {
        out.print(Boolean.toString(b)) ;
    }

    private void value(long integer) {
        out.print(Long.toString(integer)) ;
    }

    private void value(BigDecimal number) {
        out.print(number.toString()) ;
    }

    // Caution - assumes "Number" outputs legal JSON format 
    private void value(Number number) {
        out.print(number.toString()) ;
    }
    
    // void valueString(String image) {}
    // void valueInteger(String image) {}
    // void valueDouble(String image) {}
    // void valueBoolean(boolean b) {}
    // void valueNull() {}
    // void valueDecimal(String image) {}

    // Library-ize.

    private static boolean isA2Z(int ch) {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z') ;
    }

    private static boolean isA2ZN(int ch) {
        return range(ch, 'a', 'z') || range(ch, 'A', 'Z') || range(ch, '0', '9') ;
    }

    private static boolean isNumeric(int ch) {
        return range(ch, '0', '9') ;
    }

    private static boolean isWhitespace(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' || ch == '\f' ;
    }

    private static boolean isNewlineChar(int ch) {
        return ch == '\r' || ch == '\n' ;
    }

    private static boolean range(int ch, char a, char b) {
        return (ch >= a && ch <= b) ;
    }

    private static void esc(IndentedWriter out, char ch) {
        out.print('\\') ;
        out.print(ch) ;
    }

    private static int oneHex(IndentedWriter out, int x, int i) {
        int y = BitsInt.unpack(x, 4 * i, 4 * i + 4) ;
        char charHex = Chars.hexDigitsUC[y] ;
        out.print(charHex) ;
        return BitsInt.clear(x, 4 * i, 4 * i + 4) ;
    }
}
