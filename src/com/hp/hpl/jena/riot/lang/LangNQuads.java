/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import atlas.lib.Sink ;

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
    public LangNQuads(Tokenizer tokens,
                      Sink<Quad> sink,
                      Checker checker,
                      boolean skipOnError,
                      boolean stopOnError)
    {
        super(tokens, sink, checker, skipOnError, stopOnError) ;
    }

    @Override
    protected Quad parseOne()
    {
        Token sToken = nextToken() ;
        Node s = parseIRIOrBNode(sToken) ;
        
        Token pToken = nextToken() ;
        Node p = parseIRI(pToken) ;
        
        Token oToken = nextToken() ;
        Node o = parseRDFTerm(oToken) ;
        
        Node c = null ;
        Token cToken = nextToken() ;
        
        if ( cToken.getType() != TokenType.DOT )
        {
            c = parseRDFTerm(cToken) ;
            cToken = nextToken() ;
        }
        
        if ( cToken.getType() != TokenType.DOT )
            exception("Quad not terminated by DOT: %s", cToken, cToken) ;
        
        Checker checker = getChecker() ;
        
        if ( checker != null )
        {
            checker.check(s, sToken.getLine(), sToken.getColumn()) ;
            checker.check(p, pToken.getLine(), pToken.getColumn()) ;
            checker.check(o, oToken.getLine(), oToken.getColumn()) ;
            if ( c != null )
                checker.check(c, cToken.getLine(), cToken.getColumn()) ;
        }
        // c may be null, meaning default graph in SPARQL.
        return new Quad(c, s, p, o) ;
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