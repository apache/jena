/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.io;
import static java.lang.String.format ;

import java.io.IOException ;
import java.io.OutputStream ;
import java.io.Writer ;

import com.hp.hpl.jena.util.FileUtils ;

/** A writer that records what the current indentation level is, and
 *  uses that to insert a prefix at each line. 
 *  It can also insert line numbers at the beginning of lines. */

public class IndentedWriter
{
    /** Stdout wrapped in an IndentedWriter - no line numbers */
    public static final IndentedWriter stdout = new IndentedWriter(System.out) ; 
    /** Stderr wrapped in an IndentedWriter - no line numbers */
    public static final IndentedWriter stderr = new IndentedWriter(System.err) ;
    
    // Note cases:if (!flatMode) 
    // 1/ incIndent - decIndent with no output should not cause any padding
    // 2/ newline() then no text, then finish should not cause a line number.
    
    protected Writer out = null ;
    
    protected static final int INDENT = 2 ;
    protected int unitIndent = INDENT ;
    protected int currentIndent = 0 ;
    protected int column = 0 ;
    protected int row = 1 ;
    protected boolean lineNumbers = false ;
    protected boolean startingNewLine = true ;
    private char padChar = ' ' ;
    private String endOfLineMarker = null ;     // Null mean none.
    private String padString = null ;
    
    protected boolean flatMode = false ;
    
    private IndentedWriter() { this(System.out, false) ; }
    
    /** Construct a UTF8 IndentedWriter around an OutputStream */
    public IndentedWriter(OutputStream outStream) { this(outStream, false) ; }
    
    /** Construct a UTF8 IndentedWriter around an OutputStream */
    public IndentedWriter(OutputStream outStream, boolean withLineNumbers)
    {
        this(FileUtils.asPrintWriterUTF8(outStream), withLineNumbers) ;
    }
    
    /** Using Writers is discouraged */
    protected IndentedWriter(Writer writer) { this(writer, false) ; }
    
    /** Using Writers is discouraged */
    protected IndentedWriter(Writer writer, boolean withLineNumbers)
    {
        out = writer ;
        lineNumbers = withLineNumbers ;
        startingNewLine = true ;
    }
    
    // Internally, use \n for newline.
    // On output, we use the platform PrintWriter.println()
    // public void print(String s) { lineStart() ; out.print(s) ; column += s.length() ; }
    
    public void print(Object obj) 
    {
        String s = "null" ;
        if ( obj != null )
            s = obj.toString() ;
        for ( int i = 0 ; i < s.length() ; i++ )
            printOneChar(s.charAt(i)) ;
    }
    
    public void printf(String formatStr, Object... args)
    {
        print(format(formatStr, args)) ;
    }
    
    public void print(char ch) { printOneChar(ch) ; }
    
    public void println(Object obj) { print(obj) ; newline() ; }
    public void println(char ch)  { print(ch) ; newline() ; }

    public void println() { newline() ; }
    
    private char lastChar = '\0' ;
    // Worker
    private void printOneChar(char ch) 
    {
        // Turn \r\n into a single newline call.
        // Assumes we don't get \r\r\n etc 
        if ( ch == '\n' && lastChar == '\r' )
        {
            lastChar = ch ;
            return ;
        }
        
        lineStart() ; 
        lastChar = ch ;
        
        // newline
        if ( ch == '\n' || ch == '\r' )
        { 
            newline() ;
            return ;
        }
        write(ch) ;
        column += 1 ;
    }

    private void write(char ch) 
    { try { out.write(ch) ; } catch (IOException ex) {} }
    
    private void write(String s) 
    { try { out.write(s) ; } catch (IOException ex) {} }
    
    /** Print a string N times */
    public void print(String s, int n)
    {
        for ( int i = 0 ; i < n ; i++ ) print(s) ;
    }

    /** Print a char N times */
    public void print(char ch, int n)
    {
        lineStart() ;
        for ( int i = 0 ; i < n ; i++ ) printOneChar(ch) ;
    }
    
    public void newline()
    {
        lineStart() ; 

        if ( endOfLineMarker != null )
            print(endOfLineMarker) ;
        if ( ! flatMode )
            write('\n') ;
        startingNewLine = true ;
        row++ ;
        column = 0 ;
        // Note that PrintWriters do not autoflush by default
        // so if layered over a PrintWirter, need to flush that as well.  
        flush() ;
    }
    
    private boolean atStartOfLine() { return column <= currentIndent ; }

    public void ensureStartOfLine()
    {
        if ( !atStartOfLine() )
            newline() ;
    }
    
    public void close() { try { out.close(); } catch (IOException ex) {} }
    public void flush() { try { out.flush(); } catch (IOException ex) {} }
    
    public void pad()
    {
        if ( startingNewLine && currentIndent > 0 )
            lineStart() ;
        padInt() ;
    }
    
    /** Pad to a given number of columns EXCLUDING the indent.
     * 
     * @param col Column number (first column is 1).
     */
    public void pad(int col) { pad(col, false) ; }
    
    /** Pad to a given number of columns maybe including the indent.
     * 
     * @param col Column number (first column is 1).
     * @param absoluteColumn Whether to include the indent
     */
    public void pad(int col, boolean absoluteColumn )
    {
        // Make absolute
        if ( !absoluteColumn )
            col = col+currentIndent ;
        int spaces = col - column  ;
        for ( int i = 0 ; i < spaces ; i++ )
        {
            write(' ') ;        // Always a space.
            column++ ;
        }
    }
    
    
    private void padInt() 
    {
        if ( padString == null )
        {
            for ( int i = column ; i < currentIndent ; i++ )
            {
                write(padChar) ;
                column++ ;
            }
        }
        else
        {
            for ( int i = column ; i < currentIndent ; i += padString.length() )
            {
                write(padString) ;
                column += padString.length() ;
            }
        }
    }
    
    public int getRow() { return row ; }
    public int getCol() { return column ; }
    public int getIndent() { return currentIndent ; }
    
    /** Position past current indent */ 
    public int getCurrentOffset()
    { 
        int x = getCol() - getIndent() ;
        if ( x >= 0 )
            return x ;
        // At start of line somehow.
        return 0 ;
    }
    
    
    public boolean hasLineNumbers()
    {
        return lineNumbers ;
    }

    public void setLineNumbers(boolean lineNumbers)
    {
        this.lineNumbers = lineNumbers ;
    }
    
    public String getEndOfLineMarker()              { return endOfLineMarker ; }
    
    /** Set the marker included at end of line - set to null for "none".  Usually used for debugging. */ 
    public void setEndOfLineMarker(String marker)   { endOfLineMarker = marker ; }
    
    /** Flat mode - print without NL, for a more compact representation - depends on caller */  
    public boolean inFlatMode()                 { return flatMode ; }
    public void setFlatMode(boolean flatMode)   { this.flatMode = flatMode ; }
    
    public char getPadChar()                { return padChar ; }
    public void setPadChar(char ch)         { this.padChar  = ch ; }
    public String getPadString()            { return padString ; }
    public void setPadString(String str)    { this.padString = str ; unitIndent = str.length(); }

    public void incIndent()      { incIndent(unitIndent) ; }
    public void incIndent(int x)
    {
        currentIndent += x ;
    }

    public void decIndent() { decIndent(unitIndent) ; }
    public void decIndent(int x) 
    {
        currentIndent -= x ;
    }
    
    public void setUnitIndent(int x) { unitIndent = x ; }
    public int  getUnitIndent() { return unitIndent ; }
    public void setAbsoluteIndent(int x) { currentIndent = x ; }
    
    public boolean atLineStart() { return startingNewLine ; }
    
    private void lineStart()
    {
        if ( flatMode )
        {
            if ( startingNewLine && row > 1 )
                // Space between each line.
                write(' ') ;
            startingNewLine = false ;
            return ;
        }
        
        // Need to do its just before we append anything, not after a NL,
        // so that a final blank does not cause a line number  
        if ( startingNewLine )
            insertLineNumber() ;
        padInt() ;
        startingNewLine = false ;
    }
    
    private static int WidthLineNumber = 3 ;
    
    private void insertLineNumber()
    {
        if ( ! lineNumbers )
            return ;
        String s = Integer.toString(row) ;
        for ( int i = 0 ; i < WidthLineNumber-s.length() ; i++ )
            write(' ') ;
        write(s) ;
        write(' ') ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */