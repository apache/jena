/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.lang;
import static com.hp.hpl.jena.sparql.util.Utils.equal ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.Checker ;
import org.openjena.riot.ParserProfile ;
import org.openjena.riot.tokens.Token ;
import org.openjena.riot.tokens.TokenType ;
import org.openjena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.FmtUtils ;

/**
 * N-Quads.
 * http://sw.deri.org/2008/07/n-quads/
 */
public class LangNQuads extends LangNTuple<Quad>
{
    // Null for no graph.
    private Node currentGraph = null ;
    
    public LangNQuads(Tokenizer tokens, ParserProfile profile, Sink<Quad> sink)
    {
        super(tokens, profile, sink) ;
    }

    @Override
    protected final Quad parseOne()
    {
        Token sToken = nextToken() ;
        if ( sToken.getType() == TokenType.EOF )
            exception(sToken, "Premature end of file: %s", sToken) ;
        
        Token pToken = nextToken() ;
        if ( pToken.getType() == TokenType.EOF )
            exception(pToken, "Premature end of file: %s", pToken) ;
        
        Token oToken = nextToken() ;
        if ( oToken.getType() == TokenType.EOF )
            exception(oToken, "Premature end of file: %s", oToken) ;
        
        Token xToken = nextToken() ;    // Maybe DOT
        if ( xToken.getType() == TokenType.EOF )
            exception(xToken, "Premature end of file: Quad not terminated by DOT: %s", xToken) ;
        
        // Process graph node first, before S,P,O
        // to set bnode label scope (if not global)
        Node c = null ;

        if ( xToken.getType() != TokenType.DOT )
        {
            checkIRI(xToken) ;
            c = tokenAsNode(xToken) ;
            xToken = nextToken() ;
            currentGraph = c ;
        }
        else
        {
            c = Quad.tripleInQuad ;
            currentGraph = null ;
        }
        
        checkIRIOrBNode(sToken) ;
        checkIRI(pToken) ;
        checkRDFTerm(oToken) ;
        // Already done. checkIRI(xToken) ;

        Node s = tokenAsNode(sToken) ;
        Node p = tokenAsNode(pToken) ;
        Node o = tokenAsNode(oToken) ;
        
        // Check end of tuple.
        
        if ( xToken.getType() != TokenType.DOT )
            exception(xToken, "Quad not terminated by DOT: %s", xToken) ;
        
        return profile.createQuad(c, s, p, o, currLine, currCol) ;
        
//        Checker checker = getChecker() ;
//        
//        if ( checker != null )
//        {
//            boolean b = checker.check(s, sToken.getLine(), sToken.getColumn()) ;
//            b &= checker.check(p, pToken.getLine(), pToken.getColumn()) ;
//            b &= checker.check(o, oToken.getLine(), oToken.getColumn()) ;
//            if ( ! equal(c, Quad.tripleInQuad) ) 
//                b &= checker.check(c, xToken.getLine(), xToken.getColumn()) ;
//            if ( !b && skipOnBadTerm )
//            {
//                Quad q = new Quad(c, s, p, o) ;
//                skipOne(q, FmtUtils.stringForQuad(q), sToken.getLine(), sToken.getColumn()) ;
//                return null ;
//            }
//        }
//        return new Quad(c, s, p, o) ;
    }
    
    @Override
    protected final Node tokenAsNode(Token token) 
    {
        return profile.create(null, labelmap, currentGraph, token) ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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