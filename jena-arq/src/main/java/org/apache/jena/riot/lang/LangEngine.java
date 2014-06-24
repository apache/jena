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

package org.apache.jena.riot.lang;

import static org.apache.jena.riot.tokens.TokenType.EOF ;
import static org.apache.jena.riot.tokens.TokenType.NODE ;
import org.apache.jena.atlas.AtlasException ;
import org.apache.jena.atlas.iterator.PeekIterator ;
import org.apache.jena.riot.RiotParseException ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.graph.Node ;

/** Common operations for RIOT parsers - not the implementation LangRIOT  */
public class LangEngine
{
    protected ParserProfile profile ;
    protected final Tokenizer tokens ;
    private final PeekIterator<Token> peekIter ;

    protected LangEngine(Tokenizer tokens, ParserProfile profile)
    {
        this.tokens = tokens ;
        this.profile = profile ;
        this.peekIter = new PeekIterator<>(tokens) ;
    }
     
    // ---- Managing tokens.
    
    protected final Token peekToken()
    {
        // Avoid repeating.
        if ( eof() ) return tokenEOF ;
        return peekIter.peek() ;
    }
    
    // Set when we get to EOF to record line/col of the EOF.
    private Token tokenEOF = null ;

    protected final boolean eof()
    {
        if ( tokenEOF != null )
            return true ;
        
        if ( ! moreTokens() )
        {
            tokenEOF = new Token(tokens.getLine(), tokens.getColumn()) ;
            tokenEOF.setType(EOF) ;
            return true ;
        }
        return false ;
    }

    protected final boolean moreTokens() 
    {
        return peekIter.hasNext() ;
    }
    
    protected final boolean lookingAt(TokenType tokenType)
    {
        if ( eof() )
            return tokenType == EOF ;
        if ( tokenType == NODE )
            return peekToken().isNode() ;
        return peekToken().hasType(tokenType) ;
    }
    
    // Remember line/col of last token for messages 
    protected long currLine = -1 ;
    protected long currCol = -1 ;
    
    protected final Token nextToken()
    {
        if ( eof() )
            return tokenEOF ;
        
        // Tokenizer errors appear here!
        try {
            Token t = peekIter.next() ;
            currLine = t.getLine() ;
            currCol = t.getColumn() ;
            return t ;
        } catch (RiotParseException ex)
        {
            // Intercept to log it.
            raiseException(ex) ;
            throw ex ;
        }
        catch (AtlasException ex)
        {
            // Bad I/O
            RiotParseException ex2 = new RiotParseException(ex.getMessage(), -1, -1) ;
            raiseException(ex2) ;
            throw ex2 ;
        }
    }

    protected final Node scopedBNode(Node scopeNode, String label)
    {
        return profile.getLabelToNode().get(scopeNode, label) ;
    }
    
    protected final void expectOrEOF(String msg, TokenType tokenType)
    {
        // DOT or EOF
        if ( eof() )
            return ;
        expect(msg, tokenType) ;
    }
    
    protected final void skipIf(TokenType ttype) {
        if ( lookingAt(ttype) )
            nextToken() ;
    }
    
    protected final void expect(String msg, TokenType ttype)
    {
        if ( ! lookingAt(ttype) )
        {
            Token location = peekToken() ;
            exception(location, msg) ;
        }
        nextToken() ;
    }

    protected final void exception(Token token, String msg, Object... args)
    { 
        if ( token != null )
            exceptionDirect(String.format(msg, args), token.getLine(), token.getColumn()) ;
        else
            exceptionDirect(String.format(msg, args), -1, -1) ;
    }

    protected final void exceptionDirect(String msg, long line, long col)
    { 
        raiseException(new RiotParseException(msg, line, col)) ;
    }
    
    protected final void raiseException(RiotParseException ex)
    { 
        ErrorHandler errorHandler = profile.getHandler() ; 
        if ( errorHandler != null )
            errorHandler.fatal(ex.getOriginalMessage(), ex.getLine(), ex.getCol()) ;
        throw ex ;
    }
}
