/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3;
import java.io.*;
import antlr.collections.AST;
import com.hp.hpl.jena.n3.*;

/**
 * @author		Andy Seaborne
 * @version 	$Id: N3EventPrinter.java,v 1.1.1.1 2002-12-19 19:14:37 bwm Exp $
 */
public class N3EventPrinter implements N3ParserEventHandler
{
	public boolean printStartFinish = false ;
	static final String NL = System.getProperty("line.separator","\n") ;


	
	Writer out = null;
	
	public N3EventPrinter(OutputStream _out)
	{
		try {
			out = new BufferedWriter(new OutputStreamWriter(_out, "UTF-8")) ;
		} catch (java.io.UnsupportedEncodingException ex) {}
	}
	
	/** Best not to use a PrintWriter, but use an OutputStreamWriter (buffered)
	 * 	with charset "UTF-8".
	 */
	
	public N3EventPrinter(Writer _out)
	{
		out = _out;
	}

	public void error(Exception ex, String message) 		{ println(out, "Error: "+message) ; flush(out); }
	//public void warning(Exception ex, String message)		{ println(out, "Warning: "+message) ; flush(out) ; }
	//public void deprecated(Exception ex, String message)	{ println(out, "Deprecated: "+message) ; flush(out) ; }

	public void startDocument()
	{
		if ( printStartFinish )
		{
			println(out, "Document start");
			flush(out);
		}
	}
	public void endDocument()
	{
		if ( printStartFinish )
		{
			println(out, "Document end");
			flush(out);
		}
	}

	public void startFormula(int line, String context)
	{
		if ( printStartFinish )
		{
			print(out, "Formula start: "+context) ;
			flush(out) ;
		}
	}
	
	public void endFormula(int line, String context)
	{
		if ( printStartFinish )
		{
			print(out, "Formula finish: "+context) ;
			flush(out) ;
		}
	}

	public void directive(int line, AST directive, AST[] args, String context)
	{
		if ( context != null )
			print(out, context+" ") ;

		print(out, directive.getText()) ;

		for (int i = 0; i < args.length; i++)
		{
			print(out, " ");
			printSlot(out, args[i]) ;
		}
		println(out) ;
		flush(out);
	}

	public void quad(int line, AST subj, AST prop, AST obj, String context)
	{
		if ( context != null )
			print(out, context+" ") ;

		print(out, "[ ");
		printSlot(out, subj);
		print(out, " , ");
		printSlot(out, prop);
		print(out, " , ");
		printSlot(out, obj);
		println(out, " ]");
		flush(out);
	}

	static public String formatSlot(AST slot)
	{
		try {
			StringWriter sw = new StringWriter() ;
			printSlot(sw, slot) ;
			sw.close() ;
			return sw.toString() ;
		} catch (IOException ioEx) {}
		return null ;
	}

	private static void printSlot(Writer out, AST ast)
	{
		try {
			if (ast == null)
			{
				out.write("<null>");
				return;
			}
	
			int tokenType = ast.getType();
			String tmp = ast.toString();
	
			if (tokenType == N3Parser.LITERAL)
			{
				out.write('"');
				printString(out, tmp);
				out.write('"');
				AST dt = ast.getFirstChild() ;
				if ( dt != null )
				{
					out.write("^^") ;
					printSlot(out, dt) ;
				}
				return;
			}
	
			if (tmp.equals(""))
				tmp = "<empty string>";
	
			out.write(tmp) ;
			out.write('(');
			out.write(N3Parser.getTokenNames()[tokenType]);
			out.write(')');
		} catch (IOException ioEx) {}
		
	}

	private static void printString(Writer out, String s)
	{
		try {
			for (int i = 0; i < s.length(); i++)
			{
				char c = s.charAt(i);
				if (c == '\\' || c == '"')
				{
					out.write('\\');
					out.write(c);
				}
				else if (c == '\n')
				{
					out.write("\\n");
				}
				else if (c == '\r')
				{
					out.write("\\r");
				}
				else if (c == '\t')
				{
					out.write("\\t");
				}
				else if (c >= 32 && c < 127)
				{
					out.write(c);
				}
				else
				{
					String hexstr = Integer.toHexString(c).toUpperCase();
					int pad = 4 - hexstr.length();
					out.write("\\u");
					for (; pad > 0; pad--)
						out.write("0");
					out.write(hexstr);
				}
			}
		} catch (IOException ioEx) {}
	}

	private static void print(Writer out, String s)
	{
		try
		{
			out.write(s);
		}
		catch (java.io.IOException ex)
		{
		}
	}
	private static void println(Writer out, String s)
	{
		try
		{
			out.write(s);
			out.write(NL);
			out.flush();
		}
		catch (java.io.IOException ex)
		{
		}
	}
	private static void println(Writer out)
	{
		try
		{
			out.write(NL);
		}
		catch (java.io.IOException ex)
		{
		}
	}
	private static void flush(Writer out)
	{
		try{ out.flush() ; } catch (java.io.IOException ex) {} }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2002
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
