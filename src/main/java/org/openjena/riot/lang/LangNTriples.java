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

package org.openjena.riot.lang;

import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.openjena.riot.Lang ;
import org.openjena.riot.system.ParserProfile ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

public final class LangNTriples extends LangNTuple<Triple>
{
    private static Logger messageLog = LoggerFactory.getLogger("N-Triples") ;
    
    public LangNTriples(Tokenizer tokens,
                        ParserProfile profile,
                        Sink<Triple> sink)
    {
        super(tokens, profile, sink) ;
    }
    
    @Override
    public Lang getLang()   { return Lang.NTRIPLES ; }

//    static final Node X = Node.createURI("http://example") ;
//    static final Triple T = new Triple(X, X, X) ;
    
    @Override
    protected final Triple parseOne() 
    { 
        Token sToken = nextToken() ;
        if ( sToken.isEOF() )
            exception(sToken, "Premature end of file: %s", sToken) ;
        
        Token pToken = nextToken() ;
        if ( pToken.isEOF() )
            exception(pToken, "Premature end of file: %s", pToken) ;
        
        Token oToken = nextToken() ;
        if ( oToken.isEOF() )
            exception(oToken, "Premature end of file: %s", oToken) ;

        // Check in createTriple - but this is cheap so do it anyway.
        checkIRIOrBNode(sToken) ;
        checkIRI(pToken) ;
        checkRDFTerm(oToken) ;
        Token x = nextToken() ;
        
        if ( x.getType() != TokenType.DOT )
            exception(x, "Triple not terminated by DOT: %s", x) ;
//        Node s = X ;
//        Node p = X ;
//        Node o = X ;
//        return T ;
        
        Node s = tokenAsNode(sToken) ;
        Node p = tokenAsNode(pToken) ;
        Node o = tokenAsNode(oToken) ;
        return profile.createTriple(s, p, o, sToken.getLine(), sToken.getColumn()) ;
    }
    
    @Override
    protected final Node tokenAsNode(Token token)
    {
        return profile.create(null, token) ;
    }
}
