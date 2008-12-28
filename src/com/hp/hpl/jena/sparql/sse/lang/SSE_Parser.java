/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.lang;

import java.io.Reader;

import com.hp.hpl.jena.sparql.sse.SSEParseException;
import com.hp.hpl.jena.sparql.sse.lang.parser.ParseException;
import com.hp.hpl.jena.sparql.sse.lang.parser.SSE_ParserCore;
import com.hp.hpl.jena.sparql.sse.lang.parser.TokenMgrError;

/** Public interface to the SSE parser */

public class SSE_Parser
{
    public static void term(Reader reader, ParseHandler handler)
    {
        SSE_ParserCore p = new SSE_ParserCore(reader) ;
        p.setHandler(handler) ;
        try
        {
            p.term() ;
            // Checks for EOF 
//            //<EOF> test : EOF is always token 0.
//            if ( p.token_source.getNextToken().kind != 0 )
//                throw new SSEParseException("Trailing characters after "+item, item.getLine(), item.getColumn()) ;
       } 
       catch (ParseException ex)
       { throw new SSEParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginColumn) ; }
       catch (TokenMgrError tErr)
       { 
           // Last valid token : not the same as token error message - but this should not happen
           int col = p.token.endColumn ;
           int line = p.token.endLine ;
           throw new SSEParseException(tErr.getMessage(), line, col) ;
       }
       //catch (JenaException ex)  { throw new TurtleParseException(ex.getMessage(), ex) ; }
    }

    public static void parse(Reader reader, ParseHandler handler)
    {
        SSE_ParserCore p = new SSE_ParserCore(reader) ;
        p.setHandler(handler) ;
        try
        {
            p.parse() ;
       } 
       catch (ParseException ex)
       { throw new SSEParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginColumn) ; }
       catch (TokenMgrError tErr)
       { 
           // Last valid token : not the same as token error message - but this should not happen
           int col = p.token.endColumn ;
           int line = p.token.endLine ;
           throw new SSEParseException(tErr.getMessage(), line, col) ;
       }
       //catch (JenaException ex)  { throw new TurtleParseException(ex.getMessage(), ex) ; }
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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