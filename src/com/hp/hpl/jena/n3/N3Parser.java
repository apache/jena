/*
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3;

import java.io.* ;

import antlr.TokenStreamException;

/** The formal interface to the N3 parser.  Wraps up the antlr parser and lexer.
 * @author		Andy Seaborne
 * @version 	$Id: N3Parser.java,v 1.2 2003-02-20 16:48:30 andy_seaborne Exp $
 */
public class N3Parser /*extends N3AntlrParser*/ implements N3AntlrParserTokenTypes
{
	N3AntlrLexer lexer = null ;
	N3AntlrParser parser = null ;
	
	public N3Parser(BufferedReader r, N3ParserEventHandler h)
	{
		lexer = new N3AntlrLexer(r) ;
		parser = new N3AntlrParser(lexer) ;
		parser.setEventHandler(h) ;
		parser.setLexer(lexer) ;
    }

	public N3Parser(Reader r, N3ParserEventHandler h)
	{
		lexer = new N3AntlrLexer(r) ;
		parser = new N3AntlrParser(lexer) ;
		parser.setEventHandler(h) ;
		parser.setLexer(lexer) ;
    }

	public N3Parser(InputStream in, N3ParserEventHandler h)
	{
		lexer = new N3AntlrLexer(in) ;
		parser = new N3AntlrParser(lexer) ;
		parser.setEventHandler(h) ;
		parser.setLexer(lexer) ;
    }

    static public String[] getTokenNames() { return N3AntlrParser._tokenNames ; }
    
    public int line() { return lexer.getLine() ; }
    public int col() { return lexer.getColumn() ; }
    
    public N3AntlrParser getParser()  { return parser ; }
    public N3AntlrLexer getLexer()    { return lexer ; }

	/** Call the top level parser rule */
	public void parse() throws antlr.RecognitionException, TokenStreamException
	{
		parser.document() ;
	}
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
