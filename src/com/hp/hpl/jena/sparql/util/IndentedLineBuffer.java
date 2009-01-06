/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.io.* ;

/** IndentLineBuffer is a buffer that records an indent level 
 *  and uses that to insert a prefix at each line.
 *  It can also insert line numbers at the beginning of lines.
 * 
 *  <Insert rant about StringBuffer being final here>
 *  Also, Java 1.5, StringBuilding is fatser than StringBuffer (not sync'ed)
 *  so could switch to that (exxept we are really using a StringWriter)  
 * 
 * @author Andy Seaborne
 */

public class IndentedLineBuffer
{
    IndentedWriter iWriter ;
    StringWriter sw = new StringWriter() ;
    
    public IndentedLineBuffer() { this(false) ; }
    
    public IndentedLineBuffer(boolean withLineNumbers)
    {
        // UTF-8 safe.
        iWriter = new IndentedWriter(sw, withLineNumbers) ;
    }
    
    public IndentedWriter getIndentedWriter() { return iWriter ; }
    public StringBuffer getBuffer() { iWriter.flush() ; return sw.getBuffer(); }
    
    public String asString() { iWriter.flush() ; return sw.toString() ; }
    @Override
    public String toString() { return asString() ; }
    
    public void append(String s) { iWriter.print(s) ; }
    public void append(char ch)  { iWriter.print(ch) ;}
    
    public void newline() { iWriter.newline() ; }
    
    public void incIndent(int x) { iWriter.incIndent(x) ; }
    public void decIndent(int x) { iWriter.decIndent(x) ; }
    
    public void pad() { iWriter.pad() ; }
    public int getRow() { return iWriter.getRow() ; }
    public int getCol() { return iWriter.getCol() ; }
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