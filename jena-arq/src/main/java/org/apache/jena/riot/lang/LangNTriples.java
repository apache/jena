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

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * N-Triples.
 * 
 * @see <a href="http://www.w3.org/TR/n-triples/">http://www.w3.org/TR/n-triples/</a>
 */
public final class LangNTriples extends LangNTuple<Triple>
{
    private static Logger messageLog = LoggerFactory.getLogger("N-Triples") ;
    
    public LangNTriples(Tokenizer tokens, ParserProfile profile, StreamRDF dest) {
        super(tokens, profile, dest) ;
    }
    
    @Override
    public Lang getLang()   { return RDFLanguages.NTRIPLES ; }

    /** Method to parse the whole stream of triples, sending each to the sink */ 
    @Override
    protected final void runParser() {
        while (hasNext()) {
            Triple x = parseOne();
            if ( x != null )
                dest.triple(x);
        }
    }

    @Override
    protected final Triple parseOne() {
        Triple triple = parseTriple();
        Token x = nextToken();
        if ( x.getType() != TokenType.DOT )
            exception(x, "Triple not terminated by DOT: %s", x);
        return triple;
    }

    @Override
    protected final Node tokenAsNode(Token token) {
        return profile.create(null, token);
    }
}
