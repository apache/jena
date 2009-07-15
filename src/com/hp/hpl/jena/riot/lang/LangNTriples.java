/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import atlas.event.Event;
import atlas.event.EventManager;
import atlas.iterator.PeekIterator;
import atlas.lib.Sink;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.riot.*;
import com.hp.hpl.jena.riot.tokens.Token;
import com.hp.hpl.jena.riot.tokens.TokenType;
import com.hp.hpl.jena.riot.tokens.Tokenizer;

/** N-Triples parser, with both push and pull interfaces.
 * <ul>
 * <li>The {@link #parse} method processes the whole stream of tokens, 
 *  sending each to a {@link atlas.lib.Sink} object.</li>
 *  <li>The <tt>Iterator&lt;Triple&gt;</tt> interface yields triples one-by-one.</li>
 *  </ul>  
 */
public class LangNTriples extends LangBase implements Iterator<Triple>
{
    private static Logger log = LoggerFactory.getLogger(LangNTriples.class) ;
    private static Logger messageLog = LoggerFactory.getLogger("N-Triples") ;
    
    public static final boolean STRICT = false ;
    
    private final Tokenizer tokens ;
    private final PeekIterator<Token> peekIter ;

    public LangNTriples(Tokenizer tokens)
    { 
        super(null, new ErrorHandlerLogger(messageLog)) ;
        this.tokens = tokens ;
        this.peekIter = new PeekIterator<Token>(tokens) ;
    }
    
    /** Method to parse the whole stream of triples, sending each to the sink */ 
    public final void parse(Sink<Triple> sink)
    {
        EventManager.send(sink, new Event(RIOT.startRead, null)) ;
        while(hasNext())
        {
           Triple t = parseOne() ;
           sink.send(t) ;
        }
        EventManager.send(sink, new Event(RIOT.finishRead, null)) ;
    }

    public boolean hasNext()
    {
        return peekIter.hasNext() ;
    }
    
    public Triple next()
    {
        return parseOne() ;
    }
    
    //@Override
    public void remove()
    { throw new UnsupportedOperationException(); }

    private Triple parseOne()
    {
        Token sToken = nextToken() ;
        Node s = checkSubject(sToken) ;
        Token pToken = nextToken() ;
        Node p = checkPredicate(pToken) ;
        Token oToken = nextToken() ;
        Node o = checkObject(oToken) ;
        Token x = nextToken() ;
        if ( x.getType() != TokenType.DOT )
            exception("Triple not terminated by DOT: %s", x, x) ;
        Checker checker = getChecker() ;
        if ( checker != null )
        {
            checker.check(s) ;
            checker.check(p) ;
            checker.check(o) ;
        }
        return new Triple(s, p, o) ;
    }

    private Node checkSubject(Token s)
    {
        if ( ! ( s.hasType(TokenType.BNODE) ||
                 s.hasType(TokenType.IRI) ) )
            exception("Illegal subject", s) ;
        return s.asNode() ;
    }

    private Node checkPredicate(Token p)
    {
        if ( ! p.hasType(TokenType.IRI) )
            exception("Illegal predciate", p) ;
        return p.asNode() ;
    }

    private Node checkObject(Token o)
    {
        switch(o.getType())
        {
            case IRI:
            case BNODE:
            case STRING2:
            case LITERAL_DT:
            case LITERAL_LANG:
                break ;
            case STRING1:
                if ( STRICT )
                    exception("Illegal single quoted string", o) ;
                break ;
            default:
                exception("Illegal object", o) ;
        }
            
        return o.asNode() ;
    }

    private Token nextToken()
    {
        if ( ! peekIter.hasNext() )
            exception("Unexpected end of file", tokens.getLine(), tokens.getColumn()) ;
        return peekIter.next() ;
    }
    
    private void exception(String msg, long lineNum, long colNum, Object... args)
    { throw new ParseException(String.format(msg, args), lineNum, colNum) ; }
    
    private void exception(String msg, Token token, Object... args)
    { throw new ParseException(String.format(msg, args), token.getLine(), token.getColumn()) ; }
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