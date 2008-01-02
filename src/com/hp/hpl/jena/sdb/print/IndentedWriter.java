/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.print;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import com.hp.hpl.jena.util.FileUtils;

/** A writer that records what the current indentation level is, and
 *  uses that to insert a prefix at each line. 
 *  It can also insert line numbers at the beginning of lines.
 * 
 * @author Andy Seaborne
 */

public class IndentedWriter
{
    // Note cases:
    // 1/ incIndent - decIndent with no output should not cause any padding
    // 2/ newline() then no text, then finish should not cause a line number.
    
    protected PrintWriter out = null ;
    
    protected static final int INDENT = 2 ;
    protected int unitIndent = INDENT ;
    protected int currentIndent = 0 ;
    protected int column = 0 ;
    protected int row = 1 ;
    protected boolean lineNumbers = false ;
    protected boolean startingNewLine = true ;
    
    public IndentedWriter(OutputStream outStream) { this(outStream, false) ; }
    
    public IndentedWriter(OutputStream outStream, boolean withLineNumbers)
    {
        this(FileUtils.asPrintWriterUTF8(outStream), withLineNumbers) ;
    }
    
    /*public*/ IndentedWriter(Writer writer) { this(new PrintWriter(writer), false) ; }
    
    /*public*/ IndentedWriter(Writer writer, boolean withLineNumbers)
    { this(new PrintWriter(writer), withLineNumbers) ; }

    /*public*/ IndentedWriter(PrintWriter printWriter) { this(printWriter, false) ; }
    
    /*public*/ IndentedWriter(PrintWriter printWriter, boolean withLineNumbers)
    {
        out = printWriter ;
        lineNumbers = withLineNumbers ;
        startingNewLine = true ;
    }
    
    // Internally, use \n for newline.
    // On output, we use the platform PrintWriter.println()
    // public void print(String s) { lineStart() ; out.print(s) ; column += s.length() ; }
    
    public void print(String s) 
    {
        for ( int i = 0 ; i < s.length() ; i++ )
            printChWorker(s.charAt(i)) ;
    }
    
    public void print(char ch) { printChWorker(ch) ; }
    
    public void println(String s) { print(s) ; newline() ; }
    public void println(char ch)  { print(ch) ; newline() ; }

    public void println() { newline() ; }
    
    char lastChar = '\0' ;

    // Worker
    private void printChWorker(char ch) 
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
        out.print(ch) ;
        column += 1 ;
    }

    /** Print a string N times */
    public void print(String s, int n)
    {
        for ( int i = 0 ; i < n ; i++ ) print(s) ;
    }

    /** Print a char N times */
    public void print(char ch, int n)
    {
        lineStart() ;
        for ( int i = 0 ; i < n ; i++ ) printChWorker(ch) ;
    }
    
    public void newline()
    {
        lineStart() ; 
        out.println() ;
        startingNewLine = true ;
        row++ ;
        column = 0 ;
        //flush() ;
    }
    
    private boolean atStartOfLine() { return column <= currentIndent ; }

    public void ensureStartOfLine()
    {
        if ( !atStartOfLine() )
            newline() ;
    }
    
    public void close() { out.flush(); }
    public void flush() { out.flush(); }
    
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
    
    /** Pad to a given number of columns maybe including the the indent.
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
            out.print(' ') ;
            column++ ;
        }
    }
    
    
    private void padInt() 
    {
        for ( int i = column ; i < currentIndent ; i++ )
        {
            // Avoids infinite recursion using this.print()
            out.print(' ') ;
            column++ ;
        }
    }
    
    public int getRow() { return row ; }
    public int getCol() { return column ; }
    public int getIndent() { return currentIndent ; }
    
    public void incIndent(int x) { currentIndent += x ; }
    public void incIndent()      { incIndent(unitIndent) ; }
    public void decIndent(int x) { currentIndent -= x ; }
    public void decIndent() { decIndent(unitIndent) ; }
    
    public void setUnitIndent(int x) { unitIndent = x ; }
    public int  getUnitIndent() { return unitIndent ; }
    public void setAbsoluteIndent(int x) { currentIndent = x ; }
    
    public boolean atLineStart() { return startingNewLine ; }
    
    private void lineStart()
    {
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
            out.print(' ') ;
        out.print(s) ;
        out.print(' ') ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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