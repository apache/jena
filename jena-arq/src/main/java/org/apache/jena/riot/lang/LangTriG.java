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

import static org.apache.jena.riot.tokens.TokenType.DOT ;
import static org.apache.jena.riot.tokens.TokenType.RBRACE ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** TriG language: http://www.w3.org/TR/trig/ */
public class LangTriG extends LangTurtleBase<Quad> {

    public LangTriG(Tokenizer tokens, ParserProfile profile, StreamRDF dest) {
        super(tokens, profile, dest) ;
    }

    @Override
    public Lang getLang() {
        return RDFLanguages.TRIG ;
    }

    @Override
    protected final void oneTopLevelElement() {
        oneNamedGraphBlock() ;
    }

    protected final void oneNamedGraphBlock() {
        // Directives are only between graph blocks.
        Node graphNode = null ;
        Token token = peekToken() ;
        Token t = token ; // Keep for error message.

        // [ ] { ... }
        if ( token.getType() == TokenType.LBRACKET ) {
            nextToken() ;
            token = peekToken() ;
            if ( token.getType() != TokenType.RBRACKET )
                exception(t, "Broken term: [ not followed by ]") ;

            graphNode = profile.createBlankNode(graphNode, t.getLine(), t.getColumn()) ;
            nextToken() ;
        } else {
            // <uri> { ... }
            // { ... }
            if ( token.isNode() ) {
                graphNode = node() ;
                nextToken() ;
            }
        }

        if ( graphNode != null ) {
            if ( graphNode.isURI() || graphNode.isBlank() )
                setCurrentGraph(graphNode) ;
            else
                exception(t, "Not a legal graph name: " + graphNode) ;
        } else
            setCurrentGraph(Quad.tripleInQuad) ;

        token = peekToken() ;

        // = is optional and old style.
        if ( token.getType() == TokenType.EQUALS ) {
            if ( profile.isStrictMode() )
                exception(token, "Use of = {} is not part of standard TriG: " + graphNode) ;
            // Skip.
            nextToken() ;
            token = peekToken() ;
        }

        if ( token.getType() != TokenType.LBRACE )
            exception(token, "Expected start of graph: got %s", peekToken()) ;
        nextToken() ;

        // **** Turtle but no directives.

        while (true) {
            token = peekToken() ;
            if ( token.hasType(TokenType.RBRACE) )
                break ;
            // Unlike many operations in this parser suite
            // this does not assume that we are definitely entering
            // this state throws an error if the first token
            triplesSameSubject() ;
        }

        // **** Turtle.
        token = nextToken() ;
        if ( token.getType() != TokenType.RBRACE )
            exception(token, "Expected end of graph: got %s", token) ;

        // Skip DOT
        token = peekToken() ;
        if ( token.hasType(TokenType.DOT) )
            nextToken() ;

        // End graph block.
        setCurrentGraph(Quad.tripleInQuad) ;
    }

    @Override
    protected void expectEndOfTriples() {
        // The DOT is required by Turtle (strictly).
        // It is not in N3 and SPARQL or TriG

        // // To make trailing DOT illegal in strict TriG.
        // if ( profile.isStrictMode() ) {
        // expect("Triples not terminated by DOT", DOT) ;
        // return ;
        // }

        if ( lookingAt(DOT) ) {
            nextToken() ;
            return ;
        }

        // Loose - DOT optional.
        if ( lookingAt(RBRACE) )
            // Don't consume the RBRACE - used to break the loop of triples
            // blocks.
            return ;
        exception(peekToken(), "Triples not terminated properly: expected '.', '}' or EOF: got %s", peekToken()) ;
    }

    @Override
    protected void emit(Node subject, Node predicate, Node object) {
        Node graph = getCurrentGraph() ;

        if ( graph == Quad.defaultGraphNodeGenerated )
            graph = Quad.tripleInQuad ;

        Quad quad = profile.createQuad(graph, subject, predicate, object, currLine, currCol) ;
        dest.quad(quad) ;
    }
}
