/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.sse.lang;

import java.io.Reader ;

import com.hp.hpl.jena.sparql.sse.SSEParseException ;
import com.hp.hpl.jena.sparql.sse.lang.parser.ParseException ;
import com.hp.hpl.jena.sparql.sse.lang.parser.SSE_ParserCore ;
import com.hp.hpl.jena.sparql.sse.lang.parser.TokenMgrError ;

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
