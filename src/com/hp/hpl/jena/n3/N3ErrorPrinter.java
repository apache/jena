/*
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.n3;
import java.io.*;
import com.hp.hpl.jena.util.FileUtils ;
//import antlr.collections.AST;


/**
 * @author		Andy Seaborne
 * @version 	$Id: N3ErrorPrinter.java,v 1.7 2004-12-06 13:50:12 andy_seaborne Exp $
 */
public class N3ErrorPrinter extends NullN3EventHandler
{
	PrintWriter out = null;
	
	public N3ErrorPrinter(OutputStream _out)
	{
	    out = FileUtils.asPrintWriterUTF8(_out) ;
	}
	
	/** Best not to use a PrintWriter, but use an OutputStreamWriter (buffered)
	 * 	with charset "UTF-8".
	 */
	
	public N3ErrorPrinter(PrintWriter _out)
	{
		out = _out;
	}

	public void error(Exception ex, String message) 		{ println("Error: "+message) ; flush() ; }
	//public void warning(Exception ex, String message)		{ println("Warning: "+message) ; }
	//public void deprecated(Exception ex, String message)	{ println("Deprecated: "+message) ; }

	private void print(String s) { out.print(s) ; }
	private void println(String s) { out.println() ; }
	private void println() { out.println() ; }
	private void flush() { out.flush() ; }
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
