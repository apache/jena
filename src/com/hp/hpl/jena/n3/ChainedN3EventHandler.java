/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.n3;

import antlr.collections.AST ;

/** Support for chaining handlers.  Sends the operations to a chained handler.
 * @author		Andy Seaborne
 * @version 	$Id: ChainedN3EventHandler.java,v 1.2 2003-01-27 14:29:26 andy_seaborne Exp $
 */

public class ChainedN3EventHandler implements N3ParserEventHandler
{
	N3ParserEventHandler here = null ;
	N3ParserEventHandler next = null ;
	
	public ChainedN3EventHandler(N3ParserEventHandler car, N3ParserEventHandler cdr)
	{
		here = car ;
		next = cdr ;
	}
	
	public void startDocument() { here.startDocument() ; next.startDocument() ; }
	public void endDocument()   { here.endDocument() ; next.startDocument() ; }
	
	public void error(Exception ex, String message)			{ System.err.println(message) ; }
	//public void warning(Exception ex, String message)		{ System.err.println(message) ; }
	//public void deprecated(Exception ex, String message)	{ System.err.println(message) ; }
	
	public void startFormula(int line, String context)
	{
		here.startFormula(line, context) ;
		next.startFormula(line, context) ;
	}
	
	public void endFormula(int line, String context)
	{
		here.endFormula(line, context) ;
		next.endFormula(line, context) ;
	}
	
	public void quad(int line, AST subj, AST prop, AST obj, String context)
	{
		here.quad(line, subj, prop, obj, context) ;
		next.quad(line, subj, prop, obj, context) ;
	}
		
	public void directive(int line, AST directive, AST[] args, String context)
	{
		here.directive(line, directive, args, context) ;
		next.directive(line, directive, args, context) ;
	}
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
