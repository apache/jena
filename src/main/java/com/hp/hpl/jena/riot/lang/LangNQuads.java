/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;
import static com.hp.hpl.jena.sparql.util.Utils.equal ;
import org.openjena.atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.riot.Checker ;
import com.hp.hpl.jena.riot.tokens.Token ;
import com.hp.hpl.jena.riot.tokens.TokenType ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;
import com.hp.hpl.jena.sparql.core.Quad ;

/**
 * N-Quads.
 * http://sw.deri.org/2008/07/n-quads/
 */
public class LangNQuads extends LangNTuple<Quad>
{
    // Null for no graph.
    private Node currentGraph = null ;
    
    public LangNQuads(Tokenizer tokens, Checker checker, Sink<Quad> sink)
    {
        super(tokens, checker, sink) ;
    }

    @Override
    protected final Quad parseOne()
    {
        Token sToken = nextToken() ;
        Token pToken = nextToken() ;
        Token oToken = nextToken() ;
        Token xToken = nextToken() ;    // Maybe DOT
        
        // Process graph node first to set bnode label scope (if not global)
        Node c = null ;
        if ( xToken.getType() != TokenType.DOT )
        {
            //c = parseRDFTerm(cToken) ;
            c = parseIRI(xToken) ;
            xToken = nextToken() ;
            currentGraph = c ;
        }
        else
        {
            c = Quad.tripleInQuad ;
            currentGraph = null ;
        }
        
        Node s = parseIRIOrBNode(sToken) ;
        Node p = parseIRI(pToken) ;
        Node o = parseRDFTerm(oToken) ;
        
        // Check end of tuple.
        
        if ( xToken.getType() != TokenType.DOT )
            exception("Quad not terminated by DOT: %s", xToken) ;
        
        Checker checker = getChecker() ;
        
        if ( checker != null )
        {
            boolean b = checker.check(s, sToken.getLine(), sToken.getColumn()) ;
            b &= checker.check(p, pToken.getLine(), pToken.getColumn()) ;
            b &= checker.check(o, oToken.getLine(), oToken.getColumn()) ;
            if ( ! equal(c, Quad.tripleInQuad) ) 
                b &= checker.check(c, xToken.getLine(), xToken.getColumn()) ;
            if ( !b && skipOnBadTerm )
            {
                skipOne(new Quad(c, s, p, o)) ;
                return null ;
            }
        }
        return new Quad(c, s, p, o) ;
    }
    
    @Override
    protected final Node tokenAsNode(Token token) 
    {
        if ( token.hasType(TokenType.BNODE) )
            return scopedBNode(currentGraph, token.getImage()) ;
        // Leave IRIs alone (don't resolve)
        return token.asNode() ;
    }
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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