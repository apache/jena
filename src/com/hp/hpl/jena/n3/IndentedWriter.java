/*
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.n3;

import java.io.* ;
import com.hp.hpl.jena.JenaRuntime ;

/** Simple class that provides output with moving left margin.
 *  Does not cope with tabs or newlines in output strings.
 * 
 * @author		Andy Seaborne
 * @version 	$Id: IndentedWriter.java,v 1.6 2004-12-06 13:50:12 andy_seaborne Exp $
 */

// Not robust/complete enough for public use
/*public*/ class IndentedWriter //extends Writer
{
	String lineSeparator = JenaRuntime.getLineSeparator() ;
	
	Writer writer ;
	int column ;
	int row ;
	int currentIndent ;
	
	public IndentedWriter(Writer w)
	{
		writer = w ;
		column = 0 ; 
		row = 0 ;
		currentIndent = 0 ;
	}

    public Writer getWriter() { return writer ; }

	public int getRow() { return row ; }
	public int getCol() { return column ; }
	public int getIndent() { return currentIndent ; }
	
	public void incIndent(int x) { currentIndent += x ; }
	public void decIndent(int x) { currentIndent -= x ; }
	public void setIndent(int x) { currentIndent = x ; }
	
	public void print(String s)
	{
		try { writer.write(s); column += s.length() ; }
		catch (java.io.IOException ex) {}
	}

	public void println(String s)
	{
		try { writer.write(s);	println() ; }
		catch (java.io.IOException ex) { }
	}
	
	public void println()
	{
		try {
			writer.write(lineSeparator); 
			writer.flush() ;
			column = 0 ;
			row++ ; 
			padTo() ;
		}
		catch (java.io.IOException ex) { }
	}
	
	public void padTo() throws IOException
	{
		StringBuffer sBuff = new StringBuffer() ;
		for ( int i = 0 ; i < currentIndent ; i++ )
			writer.write(' ') ;
		column = column + currentIndent ;
	}
	
	public void flush() { try { writer.flush() ; } catch (IOException ioEx) {} }
	public void close() { try { writer.close() ; } catch (IOException ioEx) {} }

}


/*
 *  (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
