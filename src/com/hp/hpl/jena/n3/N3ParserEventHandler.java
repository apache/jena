/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.n3;
import antlr.collections.AST;

/** Interface for handling the output events from the N3 parser
 * 
 * @author		Andy Seaborne
 * @version 	$Id: N3ParserEventHandler.java,v 1.6 2005-02-21 12:04:06 andy_seaborne Exp $
 */
public interface N3ParserEventHandler
{
	
	public void startDocument() ;
	public void endDocument() ;
	
	// The string message will be informative as to position.
	public void error(Exception ex, String message) ;
	//public void warning(Exception ex, String message) ;
	//public void deprecated(Exception ex, String message) ;
	
	// The id identifies the formula found.

	public void startFormula(int line, String context) ;
	public void endFormula(int line, String context) ;
	
	public void quad(int line, AST subj, AST prop, AST obj, String context) ;
	public void directive(int line, AST directive, AST[] args, String context) ;
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
