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
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

public final class LangNTriples extends LangNTuple<Triple>
{
    private static Logger messageLog = LoggerFactory.getLogger("N-Triples") ;
    
    public LangNTriples(Tokenizer tokens,
                        ParserProfile profile,
                        StreamRDF dest)
    {
        super(tokens, profile, dest) ;
    }
    
    @Override
    public Lang getLang()   { return RDFLanguages.NTRIPLES ; }

    /** Method to parse the whole stream of triples, sending each to the sink */ 
    @Override
    protected final void runParser()
    {
        while(hasNext())
        {
            Triple x = parseOne() ;
            if ( x != null )
                dest.triple(x) ;
        }
    }
    
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
