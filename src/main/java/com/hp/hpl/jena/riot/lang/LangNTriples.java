/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import org.openjena.atlas.lib.Sink ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.riot.Checker ;
import com.hp.hpl.jena.riot.tokens.Token ;
import com.hp.hpl.jena.riot.tokens.TokenType ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;

public class LangNTriples extends LangNTuple<Triple>
{
    private static Logger messageLog = LoggerFactory.getLogger("N-Triples") ;
    
    public LangNTriples(Tokenizer tokens,
                        Checker checker,
                        Sink<Triple> sink)
    {
        super(tokens, checker, sink) ;
    }

    @Override
    protected final Triple parseOne() 
    { 
        Token sToken = nextToken() ;
        Token pToken = nextToken() ;
        Token oToken = nextToken() ;
        
        Node s = parseIRIOrBNode(sToken) ;
        Node p = parseIRI(pToken) ;
        Node o = parseRDFTerm(oToken) ;

        Token x = nextToken() ;
        if ( x.getType() != TokenType.DOT )
            exception("Triple not terminated by DOT: %s", x, x) ;
        Checker checker = getChecker() ;
        if ( checker != null )
        {
            boolean b = checker.check(s, sToken.getLine(), sToken.getColumn()) ;
            b &= checker.check(p, pToken.getLine(), pToken.getColumn()) ;
            b &= checker.check(o, oToken.getLine(), oToken.getColumn()) ;
            if ( !b && skipOnBadTerm )
            {
                skipOne(new Triple(s, p, o)) ;
                return null ;
            }
        }
        return new Triple(s, p, o) ; 
    }
    
    @Override
    protected final Node tokenAsNode(Token token) 
    {
        if ( token.hasType(TokenType.BNODE) )
            return scopedBNode(null, token.getImage()) ;
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