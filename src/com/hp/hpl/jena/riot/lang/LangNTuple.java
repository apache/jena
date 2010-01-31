/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import java.util.Iterator ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import atlas.event.Event ;
import atlas.event.EventManager ;
import atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.riot.ErrorHandlerLogger ;
import com.hp.hpl.jena.riot.RIOT ;
import com.hp.hpl.jena.riot.tokens.Token ;
import com.hp.hpl.jena.riot.tokens.TokenType ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;

/** N-Quads, N-triples parser framework, with both push and pull interfaces.
 * 
 * <ul>
 * <li>The {@link #parse} method processes the whole stream of tokens, 
 *   sending each to a {@link atlas.lib.Sink} object.</li>
 * <li>The <tt>Iterator&lt;X&gt;</tt> interface yields triples one-by-one.</li>
 *  </ul>  
 */
public abstract class LangNTuple<X> extends LangBase implements Iterator<X>
{
    private static Logger log = LoggerFactory.getLogger(LangNTuple.class) ;
    //private static Logger messageLog = LoggerFactory.getLogger("N-Triples") ;
    
    public static final boolean STRICT = false ;
    protected final Sink<X> sink ;
    
    protected LangNTuple(Tokenizer tokens, Sink<X> sink, Logger messageLog)
    { 
        super(null, new ErrorHandlerLogger(messageLog), tokens) ;
        this.sink = sink ; 
    }
    
    /** Method to parse the whole stream of triples, sending each to the sink */ 
    public final void parse()
    {
        EventManager.send(sink, new Event(RIOT.startRead, null)) ;
        parseAll(sink) ;
        EventManager.send(sink, new Event(RIOT.finishRead, null)) ;
    }

    protected void parseAll(Sink<X> sink)
    {
        while(hasNext())
        {
            X x = parseOne() ; 
            sink.send(x) ;
        }
    }

    // Assumes no syntax errors.
    //@Override
    public final boolean hasNext()
    {
        return super.moreTokens() ;
    }
    
    //@Override
    public final X next()
    {
        return parseOne() ;
    }
    
    //@Override
    public final void remove()
    { throw new UnsupportedOperationException(); }

    protected abstract X parseOne() ;
    

    
    @Override
    protected final Node tokenAsNode(Token token) 
    {
        if ( token.hasType(TokenType.BNODE) )
            return scopedBNode(token.getImage()) ;
        // Leave IRIs alone (don't resolve)
        return token.asNode() ;
    }
    
    protected final Node parseIRIOrBNode(Token s)
    {
        if ( ! ( s.hasType(TokenType.BNODE) ||
                 s.hasType(TokenType.IRI) ) )
            exception("Expected BNode or IRI", s) ;
        return tokenAsNode(s) ;
    }

    protected final Node parseIRI(Token p)
    {
        if ( ! p.hasType(TokenType.IRI) )
            exception("Expected IRI", p) ;
        return tokenAsNode(p) ;
    }

    protected final Node parseRDFTerm(Token o)
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
            
        return tokenAsNode(o) ;
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