/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3;
import java.io.*;
//import antlr.collections.AST;


/**
 * @author		Andy Seaborne
 * @version 	$Id: N3ErrorPrinter.java,v 1.4 2003-03-11 18:04:08 andy_seaborne Exp $
 */
public class N3ErrorPrinter extends NullN3EventHandler
{
	static final String NL = System.getProperty("line.separator","\n") ;
	
	Writer out = null;
	
	public N3ErrorPrinter(OutputStream _out)
	{
		try {
			out = new BufferedWriter(new OutputStreamWriter(_out, "UTF-8")) ;
		} catch (java.io.UnsupportedEncodingException ex) {}
	}
	
	/** Best not to use a PrintWriter, but use an OutputStreamWriter (buffered)
	 * 	with charset "UTF-8".
	 */
	
	public N3ErrorPrinter(Writer _out)
	{
		out = _out;
	}

	public void error(Exception ex, String message) 		{ println("Error: "+message) ; flush() ; }
	//public void warning(Exception ex, String message)		{ println("Warning: "+message) ; }
	//public void deprecated(Exception ex, String message)	{ println("Deprecated: "+message) ; }

	private void print(String s) { try { out.write(s) ; } catch (java.io.IOException ex) {} }
	private void println(String s) { try { out.write(s) ; out.write(NL) ;}catch (java.io.IOException ex) {} }
	private void println() { try { out.write(NL) ;} catch (java.io.IOException ex) {} }
	private void flush() { try { out.flush() ; } catch (java.io.IOException ex) {} }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001-2003
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
