/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import static com.hp.hpl.jena.riot.tokens.TokenType.EOF ;
import static com.hp.hpl.jena.riot.tokens.TokenType.NODE ;
import org.openjena.atlas.event.Event ;
import org.openjena.atlas.event.EventManager ;
import org.openjena.atlas.iterator.PeekIterator ;
import org.openjena.atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.riot.Checker ;
import com.hp.hpl.jena.riot.ParseException ;
import com.hp.hpl.jena.riot.RIOT ;
import com.hp.hpl.jena.riot.tokens.Token ;
import com.hp.hpl.jena.riot.tokens.TokenType ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;

/** Common operations for RIOT parsers */
public abstract class LangBase<X> implements LangRIOT
{
    protected Checker checker = null ;

    protected final Tokenizer tokens ;
    private final PeekIterator<Token> peekIter ;

    protected final Sink<X> sink ; 
    
    protected LabelToNode labelmap ;

//    protected LangBase(Tokenizer tokens,
//                    Sink<X> sink,
//                    Checker checker)
//    {
//        this(tokens, sink, checker, LabelToNode.createOneScope()) ;
//    }
    
    protected LangBase(Tokenizer tokens,
                       Sink<X> sink,
                       Checker checker, 
                       LabelToNode map)
    {
        setChecker(checker) ;
        this.sink = sink ;
        this.tokens = tokens ;
        this.peekIter = new PeekIterator<Token>(tokens) ;
        this.labelmap = map ;
    }
        
    //@Override
    public Checker getChecker()                 { return checker ; }
    //@Override
    public void    setChecker(Checker checker)  { this.checker = checker ; }
    
    public void parse()
    {
        EventManager.send(sink, new Event(RIOT.startRead, null)) ;
        runParser() ;
        EventManager.send(sink, new Event(RIOT.finishRead, null)) ;
    }
    
    // ---- Managing tokens.
    
    /** Run the parser - events have been handled. */
    protected abstract void runParser() ;

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
//        if ( tokenType == KEYWORD )
//        {
//            String image = tokenRaw().getImage() ;
//            if ( image.equals(KW_TRUE) )
//                return true ;
//            if ( image.equals(KW_FALSE) )
//                return true ;
//            return false ; 
//        }
        // NB IRIs and PREFIXED_NAMEs
        return peekToken().hasType(tokenType) ;
    }
    
    // Remembver line/col for messages.
    protected long currLine = -1 ;
    protected long currCol = -1 ;
    
    protected final Token nextToken()
    {
        if ( eof() )
            return tokenEOF ;
        
        Token t = peekIter.next() ;
        currLine = t.getLine() ;
        currCol = t.getColumn() ;
        return t ;
    }

    protected abstract Node tokenAsNode(Token token) ;

    protected final Node scopedBNode(Node scopeNode, String label)
    {
        return labelmap.get(scopeNode, label) ;
    }
    
    protected final void expectOrEOF(String msg, TokenType tokenType)
    {
        // DOT or EOF
        if ( eof() )
            return ;
        expect(msg, tokenType) ;
    }
    
    protected final void expect(String msg, TokenType ttype)
    {
        if ( ! lookingAt(ttype) )
            exception(msg) ;
        nextToken() ;
    }

    protected final void exception(String msg, Object... args)
    { 
        exceptionDirect(String.format(msg, args), peekToken().getLine(), peekToken().getColumn()) ;
    }

    protected final void exceptionDirect(String msg, long line, long col)
    { 
        throw new ParseException(msg, line, col) ;
    }

 
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