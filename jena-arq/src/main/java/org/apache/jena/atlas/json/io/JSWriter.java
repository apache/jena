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

import java.io.OutputStream ;
import java.math.BigDecimal ;
import java.util.ArrayDeque ;
import java.util.Deque ;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;

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
    Deque<AtomicBoolean> stack = new ArrayDeque<>() ;

    public JSWriter startObject() {
        startCompound() ;
        out.print(ObjectStart) ;
        out.incIndent() ;
        return this;
    }

    public JSWriter finishObject() {
        out.decIndent() ;
        if ( isFirst() )
            out.print(ObjectFinish) ;
        else {
            out.ensureStartOfLine() ;
            out.println(ObjectFinish) ;
        }
        finishCompound() ;
        return this;
    }

    public JSWriter key(String key) {
        if ( isFirst() ) {
            out.println() ;
            setNotFirst() ;
        } else
            out.println(ObjectSep) ;
        value(key) ;
        out.print(ObjectPairSep) ;
        // Ready to start the pair value.
        return this;
    }

    // "Pair" is the name used in the JSON spec.
    public JSWriter pair(String key, String value) {
        key(key) ;
        value(value) ;
        return this;
    }

    public JSWriter pair(String key, boolean val) {
        key(key) ;
        value(val) ;
        return this;
    }

    public JSWriter pair(String key, long val) {
        key(key) ;
        value(val) ;
        return this;
    }

    public JSWriter pair(String key, Number val) {
        key(key) ;
        value(val) ;
        return this;
    }

    public JSWriter startArray() {
        startCompound() ;
        out.print(ArrayStart) ;
        return this;
    }

    public JSWriter finishArray() {
        out.print(ArrayFinish) ; // Leave on same line.
        finishCompound() ;
        return this;
    }

    public JSWriter arrayElement(String str) {
        arrayElementProcess() ;
        value(str) ;
        return this;
    }

    private JSWriter arrayElementProcess() {
        if ( isFirst() )
            setNotFirst() ;
        else
            out.print(ArraySep) ;
        return this;
    }

    public JSWriter arrayElement(boolean b) {
        arrayElementProcess() ;
        value(b) ;
        return this;
    }

    public JSWriter arrayElement(long integer) {
        arrayElementProcess() ;
        value(integer) ;
        return this;
    }

    /**
     * Useful if you are manually creating arrays and so need to print array
     * separators yourself
     */
    public JSWriter arraySep() {
        out.print(ArraySep) ;
        return this;
    }

    public static String outputQuotedString(String string) {
        IndentedLineBuffer b = new IndentedLineBuffer() ;
        outputQuotedString(b, string) ;
        return b.asString() ;
    }

    /*
     * Output a JSON string with escaping.
     */
    /*package*/ static void outputQuotedString(IndentedWriter out, String string) {
        outputQuotedString(out, string, false) ;
    }

    /*
     * Output a JSON string with escaping. Optionally allow base words (unquoted
     * strings) which, for member names, is legal javascript but not legal JSON.
     * The Jena JSON parser accepts them.
     */
    /*package*/  static void outputQuotedString(IndentedWriter out, String string, boolean allowBareWords) {
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

        // Quoted string.
        out.print(quoteChar) ;
        JsonIO.escape(string, out, quoteChar);
        out.print(quoteChar) ;
    }

    private void startCompound() {
        stack.push(new AtomicBoolean(true)) ;
    }

    private void finishCompound() {
        stack.pop() ;
    }

    private boolean isFirst() {
        return stack.peek().get() ;
    }

    private void setNotFirst() {
        stack.peek().set(false) ;
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
}
