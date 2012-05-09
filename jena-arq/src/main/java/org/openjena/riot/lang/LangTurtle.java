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

import static org.openjena.riot.tokens.TokenType.DOT ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.Lang ;
import org.openjena.riot.system.ParserProfile ;
import org.openjena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
/** Turtle language */
public class LangTurtle extends LangTurtleBase<Triple>
{
    public LangTurtle(Tokenizer tokens, 
                      ParserProfile profile, 
                      Sink<Triple> sink) 
    {
        super(tokens, profile, sink) ;
        setCurrentGraph(null) ;
    }

    @Override
    public Lang getLang()   { return Lang.TURTLE ; }
    
    @Override
    protected final void oneTopLevelElement()
    {
        triplesSameSubject() ;
    }
    
    @Override
    protected void expectEndOfTriples()
    {
        // The DOT is required by Turtle (strictly).
        // It is not in N3 and SPARQL.
        if ( strict )
            expect("Triples not terminated by DOT", DOT) ;
        else
            expectOrEOF("Triples not terminated by DOT", DOT) ;
    }
    
    @Override
    protected void emit(Node subject, Node predicate, Node object)
    {
        Triple t = profile.createTriple(subject, predicate, object, currLine, currCol) ;
        sink.send(t) ;
    }
}
