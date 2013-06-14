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
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Quad ;

/**
 * N-Quads.
 * http://sw.deri.org/2008/07/n-quads/
 */
public class LangNQuads extends LangNTuple<Quad>
{
    // Null for no graph.
    private Node currentGraph = null ;
    
    public LangNQuads(Tokenizer tokens, ParserProfile profile, StreamRDF dest)
    {
        super(tokens, profile, dest) ;
    }

    @Override
    public Lang getLang()   { return RDFLanguages.NQUADS ; }
    
    /** Method to parse the whole stream of triples, sending each to the sink */ 
    @Override
    protected final void runParser()
    {
        while(hasNext())
        {
            Quad x = parseOne() ;
            if ( x != null )
                dest.quad(x) ;
        }
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
            // Allow bNodes for graph names.
            checkIRIOrBNode(xToken) ;
            // Allow only IRIs
            //checkIRI(xToken) ;
            c = tokenAsNode(xToken) ;
            xToken = nextToken() ;
            currentGraph = c ;
        }
        else
        {
            c = Quad.defaultGraphNodeGenerated ;
            currentGraph = null ;
        }
        
        // createQuad may also check but these checks are cheap and do form syntax errors.
        checkIRIOrBNode(sToken) ;
        checkIRI(pToken) ;
        checkRDFTerm(oToken) ;
        // xToken already checked.

        Node s = tokenAsNode(sToken) ;
        Node p = tokenAsNode(pToken) ;
        Node o = tokenAsNode(oToken) ;
        
        // Check end of tuple.
        
        if ( xToken.getType() != TokenType.DOT )
            exception(xToken, "Quad not terminated by DOT: %s", xToken) ;
        
        return profile.createQuad(c, s, p, o, sToken.getLine(), sToken.getColumn()) ;
    }
    
    @Override
    protected final Node tokenAsNode(Token token) 
    {
        return profile.create(currentGraph, token) ;
    }
}
