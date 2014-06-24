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

package org.apache.jena.atlas.json.io.parser;

import static org.apache.jena.riot.tokens.TokenType.EOF ;
import org.apache.jena.atlas.iterator.PeekIterator ;
import org.apache.jena.atlas.json.JsonParseException ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;


class JSONParserBase
{
    protected boolean VERBOSE = true ;
    private Tokenizer tokens ;
    private PeekIterator<Token> peekTokens ;
    protected long currLine = -1 ;
    protected long currCol = -1 ;

    protected JSONParserBase(Tokenizer tokens)
    {
        this.tokens = tokens ;
        peekTokens = new PeekIterator<>(tokens) ;
    }
    
    private Token tokenEOF = null ;

    final protected boolean eof()
    {
        if ( tokenEOF != null )
            return true ;
        
        if ( ! moreTokens() )
        {
            tokenEOF = new Token(tokens.getLine(), tokens.getColumn()) ;
            return true ;
        }
        return false ;
    }

    final protected boolean moreTokens() 
    {
        return peekTokens.hasNext() ;
    }
    
    final protected boolean lookingAt(TokenType tokenType)
    {
        Token t = peekTokens.peek() ;
        if ( t == null  )
            return tokenType == EOF ;
        return t.hasType(tokenType) ;
    }
    
    final protected boolean lookingAtString()
    {
        Token t = peekTokens.peek() ;
        if ( t == null  )
            return false ;
        if ( t.hasType(TokenType.STRING1) ) return true ;
        if ( t.hasType(TokenType.STRING2) ) return true ;
        if ( t.hasType(TokenType.LONG_STRING1) ) return true ;
        if ( t.hasType(TokenType.LONG_STRING2) ) return true ;
        return false ;
    }
    
    final protected boolean lookingAtNumber()
    {
        Token t = peekTokens.peek() ;
        if ( t == null  )
            return false ;
        if ( t.hasType(TokenType.INTEGER) ) return true ;
        if ( t.hasType(TokenType.HEX) )     return true ;
        if ( t.hasType(TokenType.DECIMAL) ) return true ;
        if ( t.hasType(TokenType.DOUBLE) )  return true ;
        return false ;
    }
    
    final protected Token peekToken()
    {
        // Avoid repeating.
        if ( eof() ) return tokenEOF ;
        return peekTokens.element() ;
    }
    
    final protected Token nextToken()
    {
        if ( eof() )
            return tokenEOF ;
        
        Token t = peekTokens.next() ;
        currLine = t.getLine() ;
        currCol = t.getColumn() ;
//        if ( VERBOSE ) log.info("Move: " + t) ;
        return t ;
    }
    
    final protected void expectOrEOF(String msg, TokenType tokenType)
    {
        // DOT or EOF
        if ( eof() )
            return ;
        expect(msg, tokenType) ;
    }
    
    final protected void expect(String msg, TokenType ttype)
    {
        if ( ! lookingAt(ttype) )
            exception(msg) ;
        nextToken() ;
    }

    final protected void exception(String msg, Object... args)
    { throw new JsonParseException(String.format(msg, args), 
                                    (int)tokens.getLine(), 
                                    (int)tokens.getColumn()) ; }
}
