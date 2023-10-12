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

package org.apache.jena.atlas.io;
import static java.lang.String.format;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;

import org.apache.jena.atlas.lib.Closeable;

/** A writer that records what the current indentation level is, and
 *  uses that to insert a prefix at each line.
 *  It can also insert line numbers at the beginning of lines. */

public class IndentedWriter extends AWriterBase implements AWriter, Closeable
{
    /** Stdout wrapped in an IndentedWriter - no line numbers */
    public static final IndentedWriter stdout = new IndentedWriter(System.out);
    /** Stderr wrapped in an IndentedWriter - no line numbers */
    public static final IndentedWriter stderr = new IndentedWriter(System.err);

    static {
        stdout.setFlushOnNewline(true);
        stderr.setFlushOnNewline(true);
    }

    // Note cases:if (!flatMode)
    // 1/ incIndent - decIndent with no output should not cause any padding
    // 2/ newline() then no text, then finish should not cause a line number.

    protected Writer out = null;

    protected static final int INDENT = 2;

    // Configuration.
    protected int unitIndent = INDENT;
    private char padChar = ' ';
    private String padString = null;
    private String linePrefix = null;
    protected boolean lineNumbers = false;
    protected boolean flatMode = false;
    private boolean flushOnNewline = false;

    // Internal state.
    protected boolean startingNewLine = true;
    private String endOfLineMarker = null;
    protected int currentIndent = 0;
    protected int column = 0;
    protected int row = 1;

    /** Construct a UTF8 IndentedWriter around an OutputStream */
    public IndentedWriter(OutputStream outStream) { this(outStream, false); }

    /** Construct a UTF8 IndentedWriter around an OutputStream */
    public IndentedWriter(OutputStream outStream, boolean withLineNumbers) {
        this(makeWriter(outStream), withLineNumbers);
    }

    /** Create an independent copy of the {@code IndentedWriter}.
     *  Changes to the configuration of the copy will not affect the original {@code IndentedWriter}.
     *  This include indentation level.
     *  <br/>Row and column counters are reset.
     *  <br/>Indent is initially. zero.
     *  <br/>They do share the underlying output {@link Writer}.
     *  @param other
     *  @return IndentedWriter
     */
    public static IndentedWriter clone(IndentedWriter other) {
        IndentedWriter dup = new IndentedWriter(other.out);
        dup.unitIndent      = other.unitIndent;
        dup.padChar         = other.padChar;
        dup.padString       = other.padString;
        dup.linePrefix      = other.linePrefix;
        dup.lineNumbers     = other.lineNumbers;
        dup.flatMode        = other.flatMode;
        dup.flushOnNewline  = other.flushOnNewline;
        return dup;
    }

    @Override
    public IndentedWriter clone() {
        return clone(this);
    }
    private static Writer makeWriter(OutputStream out) {
        return IO.asBufferedUTF8(out);
    }

    /** Using Writers directly is discouraged */
    protected IndentedWriter(Writer writer) {
        this(writer, false);
    }

    /** Using Writers directly is discouraged */
    protected IndentedWriter(Writer writer, boolean withLineNumbers) {
        out = writer;
        lineNumbers = withLineNumbers;
        startingNewLine = true;
    }

    protected IndentedWriter rtnObject() { return this; }

    @Override
    public IndentedWriter print(String str) {
        if ( str == null )
            str = "null";
        if ( false ) {
            // Don't check for embedded newlines.
            write$(str);
            return this;
        }
        for ( int i = 0; i < str.length(); i++ )
            printOneChar(str.charAt(i));
        return this;
    }

    @Override
    public IndentedWriter printf(String formatStr, Object... args) {
        print(format(formatStr, args));
        return this;
    }

    @Override
    public IndentedWriter print(char ch)      { printOneChar(ch); return this; }
    public IndentedWriter print(Object obj)   { print(String.valueOf(obj)); return this; }

    @Override
    public IndentedWriter println(String str) { print(str); newline(); return this; }
    public IndentedWriter println(char ch)    { print(ch); newline(); return this; }
    public IndentedWriter println(Object obj) { print(String.valueOf(obj)); newline(); return this; }

    @Override
    public IndentedWriter println() { newline(); return this; }

    @Override
    public IndentedWriter print(char[] cbuf) {
        for ( char aCbuf : cbuf ) {
            printOneChar(aCbuf);
        }
        return this;
    }

    /** Print a string N times */
    public IndentedWriter print(String s, int n) {
        for ( int i = 0; i < n; i++ )
            print(s);
        return this;
    }

    /** Print a char N times */
    public IndentedWriter print(char ch, int n) {
        lineStart();
        for ( int i = 0; i < n; i++ )
            printOneChar(ch);
        return this;
    }

    private char lastChar = '\0';

    // Worker
    private void printOneChar(char ch) {
        // Turn \r\n into a single newline call.
        // Assumes we don't get \r\r\n etc
        if ( ch == '\n' && lastChar == '\r' ) {
            lastChar = ch;
            return;
        }

        lineStart();
        lastChar = ch;

        // newline
        if ( ch == '\n' || ch == '\r' ) {
            newline();
            return;
        }
        write$(ch);
        column += 1;
    }

    private void write$(char ch)
    { try { out.write(ch); } catch (IOException ex) { IO.exception(ex); } }

    private void write$(String s)
    { try { out.write(s); } catch (IOException ex) { IO.exception(ex); } }

    public IndentedWriter newline() {
        lineStart();

        if ( endOfLineMarker != null )
            print(endOfLineMarker);
        if ( !flatMode )
            write$('\n');
        startingNewLine = true;
        row++;
        column = 0;
        // Note that PrintWriters do not autoflush by default
        // so if layered over a PrintWriter, need to flush that as well.
        if ( flushOnNewline )
            flush();
        return this;
    }

    private boolean atStartOfLine() { return column <= currentIndent; }

    public IndentedWriter ensureStartOfLine() {
        if ( !atStartOfLine() )
            newline();
        return this;
    }

    public boolean atLineStart()        { return startingNewLine; }

    // A line is prefix?number?content.
    private void lineStart() {
        if ( flatMode ) {
            if ( startingNewLine && row > 1 )
                // Space between each line.
                write$(' ');
            startingNewLine = false;
            return;
        }

        // Need to do this at line start, not at the previous line end
        // otherwise a final blank line will have a prefix and line number.
        if ( startingNewLine ) {
            if ( linePrefix != null )
                write$(linePrefix);
            insertLineNumber();
        }
        padInternal();
        startingNewLine = false;
    }

    @Override
    public void close() { IO.close(out); }

    @Override
    public IndentedWriter flush() { IO.flush(out); return this; }

    /** Pad to the indent (if we are before it) */
    public IndentedWriter pad() {
        if ( startingNewLine && currentIndent > 0 )
            lineStart();
        padInternal();
        return this;
    }

    /** Pad to a given number of columns EXCLUDING the indent.
     *
     * @param col Column number (first column is 1).
     */
    public IndentedWriter pad(int col) { return pad(col, false); }

    /** Pad to a given number of columns maybe including the indent.
     *
     * @param col Column number (first column is 1).
     * @param absoluteColumn Whether to include the indent
     */
    public IndentedWriter pad(int col, boolean absoluteColumn) {
        // Make absolute
        if ( !absoluteColumn )
            col = col + currentIndent;
        int spaces = col - column;
        for ( int i = 0; i < spaces; i++ ) {
            write$(' ');        // Always a space.
            column++;
        }
        return this;
    }

    private void padInternal() {
        if ( padString == null ) {
            for ( int i = column; i < currentIndent; i++ ) {
                write$(padChar);
                column++;
            }
        } else {
            for ( int i = column; i < currentIndent; i += padString.length() ) {
                write$(padString);
                column += padString.length();
            }
        }
    }

    /** Get row/line (counts from 1) */
    public int getRow() { return row; }
    /** Get the absolute column.
     *  This is the location where the next character on the line will be printed.
     *  The IndentedWriter may not yet have padded to this place.
     */
    public int getCol() {
        if ( currentIndent > column )
            return currentIndent;
        return column;
    }

    public IndentedWriter incIndent() { incIndent(unitIndent); return this; }

    public IndentedWriter incIndent(int x) {
        currentIndent += x;
        return this;
    }

    public IndentedWriter decIndent() { decIndent(unitIndent); return this; }

    public IndentedWriter decIndent(int x) {
        currentIndent -= x;
        return this;
    }

    /** Position past current indent */
    public int getCurrentOffset() {
        int x = getCol() - getAbsoluteIndent();
        if ( x >= 0 )
            return x;
        // At start of line somehow.
        return 0;
    }

    /** Get indent from the left hand edge */
    public int getAbsoluteIndent()       { return currentIndent; }

    /** Set indent from the left hand edge. Returns {@code this}. */
    public IndentedWriter setAbsoluteIndent(int x) { currentIndent = x; return rtnObject(); }

    public boolean hasLineNumbers() {
        return lineNumbers;
    }

    public IndentedWriter setLineNumbers(boolean lineNumbers) {
        this.lineNumbers = lineNumbers;
        return rtnObject();
    }

    public String getEndOfLineMarker()              { return endOfLineMarker; }

    /** Set the marker included at end of line - set to null for "none".  Usually used for debugging. */
    public IndentedWriter setEndOfLineMarker(String marker) {
        endOfLineMarker = marker;
        return rtnObject();
    }

    /** Flat mode - print without NL, for a more compact representation*/
    public boolean inFlatMode()                     { return flatMode; }
    /** Flat mode - print without NL, for a more compact representation*/
    public IndentedWriter setFlatMode(boolean flatMode) {
        this.flatMode = flatMode;
        return rtnObject();
    }

    /** Flush on newline **/
    public boolean getFlushOnNewline()              { return flushOnNewline; }

    /** Flush on newline in this code.
     * This is set for {@link IndentedWriter#stdout} and {@link IndentedWriter#stderr}
     * but not by default otherwise. The underlying output, if it is a {@link PrintStream}
     * may also have a flush on newline as well (e.g {@link System#out}).
     */
    public IndentedWriter setFlushOnNewline(boolean flushOnNewline) {
        this.flushOnNewline = flushOnNewline;
        return rtnObject();
    }

    public char getPadChar()                        { return padChar; }

    public IndentedWriter setPadChar(char ch) {
        this.padChar = ch;
        return rtnObject();
    }

    public String getPadString()                    { return padString; }

    public IndentedWriter setPadString(String str) {
        this.padString = str;
        this.unitIndent = str.length();
        return rtnObject();
    }

    /** Initial string printed at the start of each line : defaults to no string. */
    public String getLinePrefix() {
        return linePrefix;
    }

    /** Set the initial string printed at the start of each line. */
    public IndentedWriter setLinePrefix(String str) {
        this.linePrefix = str;
        return rtnObject();
    }

    public int getUnitIndent()         { return unitIndent; }

    public IndentedWriter setUnitIndent(int x) {
        unitIndent = x;
        return rtnObject();
    }

    private int widthLineNumber = 3;

    /** Width of the number field */
    public int getNumberWidth() { return widthLineNumber; }

    /** Set the width of the number field.
     * There is also a single space after the number not included in this setting.
     */
    public IndentedWriter setNumberWidth(int widthOfNumbers) {
        widthLineNumber = widthOfNumbers;
        return rtnObject();
    }

    private void insertLineNumber() {
        if ( !lineNumbers )
            return;
        String s = Integer.toString(row);
        for ( int i = 0; i < widthLineNumber - s.length(); i++ )
            write$(' ');
        write$(s);
        write$(' ');
    }

    @Override
    public String toString() {
        return String.format("Indent = %d : Row = %d : Col = %d", currentIndent, row, column);
    }
}
