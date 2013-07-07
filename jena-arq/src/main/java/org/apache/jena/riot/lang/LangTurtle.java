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

package org.apache.jena.riot.lang ;

import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

/** Turtle language */
public class LangTurtle extends LangTurtleBase {
    public LangTurtle(Tokenizer tokens, ParserProfile profile, StreamRDF dest) {
        super(tokens, profile, dest) ;
        setCurrentGraph(null) ;
    }

    @Override
    public Lang getLang() {
        return RDFLanguages.TURTLE ;
    }

    @Override
    protected final void oneTopLevelElement() {
        triplesSameSubject() ;
    }

    @Override
    protected void expectEndOfTriples() {
        expectEndOfTriplesTurtle() ;
    }

    @Override
    protected void emit(Node subject, Node predicate, Node object) {
        Triple t = profile.createTriple(subject, predicate, object, currLine, currCol) ;
        dest.triple(t) ;
    }
}
