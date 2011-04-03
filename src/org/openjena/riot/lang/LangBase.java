/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.lang;

import static org.openjena.riot.tokens.TokenType.EOF ;
import static org.openjena.riot.tokens.TokenType.NODE ;
import org.openjena.atlas.AtlasException ;
import org.openjena.atlas.event.Event ;
import org.openjena.atlas.event.EventManager ;
import org.openjena.atlas.iterator.PeekIterator ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.RiotParseException ;
import org.openjena.riot.SysRIOT ;
import org.openjena.riot.system.ParserProfile ;
import org.openjena.riot.tokens.Token ;
import org.openjena.riot.tokens.TokenType ;
import org.openjena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.graph.Node ;

/** Common operations for RIOT parsers */
public abstract class LangBase<X> implements LangRIOT
{
    protected ParserProfile profile ;
    protected final Tokenizer tokens ;
    private final PeekIterator<Token> peekIter ;

    protected final Sink<X> sink ; 
    
    protected LangBase(Tokenizer tokens,
                       Sink<X> sink,
                       ParserProfile profile)

    {
        //setChecker(checker) ;
        setProfile(profile) ;
        this.sink = sink ;
        this.tokens = tokens ;
        this.peekIter = new PeekIterator<Token>(tokens) ;
    }
     
    //@Override
    public ParserProfile getProfile()                     { return profile ; }
    //@Override
    public void setProfile(ParserProfile profile)
    {
        this.profile = profile ;
    }
    
//    //@Override
//    public Checker getChecker()                 { return checker ; }
//    //@Override
//    // Bad separation of responsibilitied :-(
//    public void    setChecker(Checker checker)
//    { 
//        this.checker = checker ;
//        if ( checker != null)
//            this.errorHandler = checker.getHandler() ;
//        else
//            this.errorHandler = ErrorHandlerLib.errorHandlerNoLogging ;
//    }
    
    public void parse()
    {
        EventManager.send(sink, new Event(SysRIOT.startRead, null)) ;
        runParser() ;
        sink.flush() ;
        EventManager.send(sink, new Event(SysRIOT.finishRead, null)) ;
        tokens.close();
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