/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json.io.parser;

import static com.hp.hpl.jena.riot.tokens.TokenType.EOF;
import org.openjena.atlas.iterator.PeekIterator ;
import org.openjena.atlas.json.JsonParseException ;

import com.hp.hpl.jena.riot.tokens.Token;
import com.hp.hpl.jena.riot.tokens.TokenType;
import com.hp.hpl.jena.riot.tokens.Tokenizer;

public class ParserBase
{
    protected boolean VERBOSE = true ;
    private Tokenizer tokens ;
    private PeekIterator<Token> peekTokens ;

    ParserBase(Tokenizer tokens)
    {
        this.tokens = tokens ;
        peekTokens = new PeekIterator<Token>(tokens) ;
    }
    
    private static Token tokenEOF = null ;

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
        Token t = peekTokens.peekOrNull() ;
        if ( t == null  )
            return tokenType == EOF ;
        return t.hasType(tokenType) ;
    }
    
    final protected boolean lookingAtString()
    {
        Token t = peekTokens.peekOrNull() ;
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
        Token t = peekTokens.peekOrNull() ;
        if ( t == null  )
            return false ;
        if ( t.hasType(TokenType.INTEGER) ) return true ;
        if ( t.hasType(TokenType.HEX) )     return true ;
        if ( t.hasType(TokenType.DECIMAL) ) return true ;
        if ( t.hasType(TokenType.DOUBLE) )  return true ;
        return false ;
    }
    
    final protected Token token()
    {
        // Avoid repeating.
        if ( eof() ) return tokenEOF ;
        return peekTokens.peek() ;
    }
    
    final protected Token move()
    {
        if ( eof() )
        {
//            if ( VERBOSE ) log.info("Move: EOF") ;
            return tokenEOF ;
        }
        
        Token t = peekTokens.next() ;
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
        move() ;
    }

    final protected void exception(String msg, Object... args)
    { throw new JsonParseException(String.format(msg, args), 
                                    (int)tokens.getLine(), 
                                    (int)tokens.getColumn()) ; }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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