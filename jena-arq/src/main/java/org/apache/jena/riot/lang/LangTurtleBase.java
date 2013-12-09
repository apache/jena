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

import static org.apache.jena.riot.tokens.TokenType.COMMA ;
import static org.apache.jena.riot.tokens.TokenType.DIRECTIVE ;
import static org.apache.jena.riot.tokens.TokenType.DOT ;
import static org.apache.jena.riot.tokens.TokenType.EOF ;
import static org.apache.jena.riot.tokens.TokenType.IRI ;
import static org.apache.jena.riot.tokens.TokenType.KEYWORD ;
import static org.apache.jena.riot.tokens.TokenType.LBRACE ;
import static org.apache.jena.riot.tokens.TokenType.LBRACKET ;
import static org.apache.jena.riot.tokens.TokenType.LPAREN ;
import static org.apache.jena.riot.tokens.TokenType.NODE ;
import static org.apache.jena.riot.tokens.TokenType.PREFIXED_NAME ;
import static org.apache.jena.riot.tokens.TokenType.RBRACKET ;
import static org.apache.jena.riot.tokens.TokenType.RPAREN ;
import static org.apache.jena.riot.tokens.TokenType.SEMICOLON ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.system.ParserProfile ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.vocabulary.OWL ;

/** The main engine for all things Turtle-ish (Turtle, TriG). */
public abstract class LangTurtleBase extends LangBase {
    // See http://www.w3.org/TR/turtle/
    // Some predicates (if accepted)
    protected final static String  KW_A           = "a" ;
    protected final static String  KW_SAME_AS     = "=" ;
    protected final static String  KW_LOG_IMPLIES = "=>" ;
    protected final static String  KW_TRUE        = "true" ;
    protected final static String  KW_FALSE       = "false" ;

    protected final static boolean VERBOSE        = false ;
    // protected final static boolean CHECKING = true ;
    // Current graph - null for default graph
    private Node                   currentGraph   = null ;

    public final Node getCurrentGraph() {
        return currentGraph ;
    }

    public final void setCurrentGraph(Node graph) {
        this.currentGraph = graph ;
    }

    protected LangTurtleBase(Tokenizer tokens, ParserProfile profile, StreamRDF dest) {
        super(tokens, profile, dest) ;
    }

    @Override
    protected final void runParser() {
        while (moreTokens()) {
            Token t = peekToken() ;
            if ( lookingAt(DIRECTIVE) ) {
                directive() ; // @form.
                continue ;
            }

            if ( lookingAt(KEYWORD) ) {
                if ( t.getImage().equalsIgnoreCase("PREFIX") || t.getImage().equalsIgnoreCase("BASE") ) {
                    directiveKeyword() ;
                    continue ;
                }
            }

            oneTopLevelElement() ;

            if ( lookingAt(EOF) )
                break ;
        }
    }

    // Do one top level item for the language.
    protected abstract void oneTopLevelElement() ;

    /**
     * Emit a triple - nodes have been checked as has legality of node type in
     * location
     */
    protected abstract void emit(Node subject, Node predicate, Node object) ;

    protected final void directiveKeyword() {
        Token t = peekToken() ;
        String x = t.getImage() ;
        nextToken() ;

        if ( x.equalsIgnoreCase("BASE") ) {
            directiveBase() ;
            return ;
        }

        if ( x.equalsIgnoreCase("PREFIX") ) {
            directivePrefix() ;
            return ;
        }
        exception(t, "Unrecognized keyword for directive: %s", x) ;
    }

    protected final void directive() {
        // It's a directive ...
        Token t = peekToken() ;
        String x = t.getImage() ;
        nextToken() ;

        if ( x.equals("base") ) {
            directiveBase() ;
            if ( profile.isStrictMode() )
                // The line number is probably one ahead due to the newline
                expect("Base directive not terminated by a dot", DOT) ;
            else
                skipIf(DOT) ;
            return ;
        }

        if ( x.equals("prefix") ) {
            directivePrefix() ;
            if ( profile.isStrictMode() )
                // The line number is probably one ahead due to the newline
                expect("Prefix directive not terminated by a dot", DOT) ;   
            else
                skipIf(DOT) ;
            return ;
        }
        exception(t, "Unrecognized directive: %s", x) ;
    }

    protected final void directivePrefix() {
        // Raw - unresolved prefix name.
        if ( !lookingAt(PREFIXED_NAME) )
            exception(peekToken(), "@prefix or PREFIX requires a prefix (found '" + peekToken() + "')") ;
        if ( peekToken().getImage2().length() != 0 )
            exception(peekToken(), "@prefix or PREFIX requires a prefix with no suffix (found '" + peekToken() + "')") ;
        String prefix = peekToken().getImage() ;
        nextToken() ;
        if ( !lookingAt(IRI) )
            exception(peekToken(), "@prefix requires an IRI (found '" + peekToken() + "')") ;
        String iriStr = peekToken().getImage() ;
        IRI iri = profile.makeIRI(iriStr, currLine, currCol) ;
        profile.getPrologue().getPrefixMap().add(prefix, iri) ;
        emitPrefix(prefix, iri.toString()) ;
        nextToken() ;
    }

    protected final void directiveBase() {
        Token token = peekToken() ;
        if ( !lookingAt(IRI) )
            exception(token, "@base requires an IRI (found '" + token + "')") ;
        String baseStr = token.getImage() ;
        IRI baseIRI = profile.makeIRI(baseStr, currLine, currCol) ;
        emitBase(baseIRI.toString()) ;
        nextToken() ;
        profile.getPrologue().setBaseURI(baseIRI) ;
    }

    

    // Unlike many operations in this parser suite
    // this does not assume that we have definitely
    // entering this state. It does checks and may
    // signal a parse exception.

    protected final void triplesSameSubject() {
        // Either a IRI/prefixed name or a construct that generates triples

        // TriplesSameSubject -> Term PropertyListNotEmpty
        if ( lookingAt(NODE) ) {
            triples() ;
            return ;
        }

        boolean maybeList = lookingAt(LPAREN) ;
        
        // Turtle: TriplesSameSubject -> TriplesNode PropertyList?
        // TriG:   (blankNodePropertyList | collection) predicateObjectList? '.'
        //         labelOrSubject (wrappedGraph | predicateObjectList '.')
        if ( peekTriplesNodeCompound() ) {
            Node n = triplesNodeCompound() ;

            // May be followed by:
            // A predicateObject list
            // A DOT or EOF.
            // But if a DOT or EOF, then it can't have been () or [].

            // Turtle, as spec'ed does not allow
            // (1 2 3 4) .
            // There must be a predicate and object.

            // -- If strict turtle.
            if ( profile.isStrictMode() && maybeList ) {
                if ( peekPredicate() ) {
                    predicateObjectList(n) ;
                    expectEndOfTriples() ;
                    return ;
                }
                exception(peekToken(), "Predicate/object required after (...) - Unexpected token : %s",
                          peekToken()) ;
            }
            // ---
            // If we allow top-level lists and [...].
            // Should check if () and [].

            if ( lookingAt(EOF) )
                return ;
            if ( lookingAt(DOT) ) {
                nextToken() ;
                return ;
            }

            if ( peekPredicate() )
                predicateObjectList(n) ;
            expectEndOfTriples() ;
            //exception(peekToken(), "Unexpected token : %s", peekToken()) ;
            return ;
        }
        exception(peekToken(), "Out of place: %s", peekToken()) ;
    }

    // Must be at least one triple.
    protected final void triples() {
        // Looking at a node.
        Node subject = node() ;
        if ( subject == null )
            exception(peekToken(), "Not recognized: expected node: %s", peekToken().text()) ;

        nextToken() ;
        predicateObjectList(subject) ;
        expectEndOfTriples() ;
    }

    // Differs between Turtle and TriG.
    // TriG, inside {} does not need the trailing DOT
    protected abstract void expectEndOfTriples() ;
    
    // The DOT is required by Turtle (strictly).
    // It is not in N3 and SPARQL.
    
    protected void expectEndOfTriplesTurtle() {
        if ( profile.isStrictMode() )
            expect("Triples not terminated by DOT", DOT) ;
        else
            expectOrEOF("Triples not terminated by DOT", DOT) ;
    }
        
    protected final void predicateObjectList(Node subject) {
        predicateObjectItem(subject) ;

        for (;;) {
            if ( !lookingAt(SEMICOLON) )
                break ;
            // predicatelist continues - move over all ";"
            while (lookingAt(SEMICOLON))
                nextToken() ;
            if ( !peekPredicate() )
                // Trailing (pointless) SEMICOLONs, no following
                // predicate/object list.
                break ;
            predicateObjectItem(subject) ;
        }
    }

    protected final void predicateObjectItem(Node subject) {
        Node predicate = predicate() ;
        nextToken() ;
        objectList(subject, predicate) ;
    }

    static protected final Node nodeSameAs     = OWL.sameAs.asNode() ;
    static protected final Node nodeLogImplies = NodeFactory.createURI("http://www.w3.org/2000/10/swap/log#implies") ;

    /** Get predicate - maybe null for "illegal" */
    protected final Node predicate() {
        Token t = peekToken() ;

        if ( t.hasType(TokenType.KEYWORD) ) {
            boolean strict = profile.isStrictMode() ;
            Token tErr = peekToken() ;
            String image = peekToken().getImage() ;
            if ( image.equals(KW_A) )
                return NodeConst.nodeRDFType ;
            if ( !strict && image.equals(KW_SAME_AS) )
                return nodeSameAs ;
            if ( !strict && image.equals(KW_LOG_IMPLIES) )
                return NodeConst.nodeRDFType ;
            exception(tErr, "Unrecognized: " + image) ;
        }

        Node n = node() ;
        if ( n == null || !n.isURI() )
            exception(t, "Expected IRI for predicate: got: %s", t) ;
        return n ;
    }

    /** Check raw token to see if it might be a predciate */
    protected final boolean peekPredicate() {
        if ( lookingAt(TokenType.KEYWORD) ) {
            String image = peekToken().getImage() ;
            boolean strict = profile.isStrictMode() ;
            if ( image.equals(KW_A) )
                return true ;
            if ( !strict && image.equals(KW_SAME_AS) )
                return true ;
            if ( !strict && image.equals(KW_LOG_IMPLIES) )
                return true ;
            return false ;
        }
        // if ( lookingAt(NODE) )
        // return true ;
        if ( lookingAt(TokenType.IRI) )
            return true ;
        if ( lookingAt(TokenType.PREFIXED_NAME) )
            return true ;
        return false ;
    }

    /** Maybe "null" for not-a-node. */
    protected final Node node() {
        // Token to Node
        Node n = tokenAsNode(peekToken()) ;
        if ( n == null )
            return null ;
        return n ;
    }

    protected final void objectList(Node subject, Node predicate) {
        for (;;) {
            Node object = triplesNode() ;
            emitTriple(subject, predicate, object) ;

            if ( !moreTokens() )
                break ;
            if ( !lookingAt(COMMA) )
                break ;
            // list continues - move over the ","
            nextToken() ;
        }
    }

    // A structure of triples that itself generates a node.
    // Special checks for [] and ().

    protected final Node triplesNode() {
        if ( lookingAt(NODE) ) {
            Node n = node() ;
            nextToken() ;
            return n ;
        }

        // Special words.
        if ( lookingAt(TokenType.KEYWORD) ) {
            Token tErr = peekToken() ;
            // Location independent node words
            String image = peekToken().getImage() ;
            nextToken() ;
            if ( image.equals(KW_TRUE) )
                return NodeConst.nodeTrue ;
            if ( image.equals(KW_FALSE) )
                return NodeConst.nodeFalse ;
            if ( image.equals(KW_A) )
                exception(tErr, "Keyword 'a' not legal at this point") ;

            exception(tErr, "Unrecognized keyword: " + image) ;
        }

        return triplesNodeCompound() ;
    }

    protected final boolean peekTriplesNodeCompound() {
        if ( lookingAt(LBRACKET) )
            return true ;
        if ( lookingAt(LBRACE) )
            return true ;
        if ( lookingAt(LPAREN) )
            return true ;
        return false ;
    }

    protected final Node triplesNodeCompound() {
        if ( lookingAt(LBRACKET) )
            return triplesBlankNode() ;
        if ( lookingAt(LBRACE) )
            return triplesFormula() ;
        if ( lookingAt(LPAREN) )
            return triplesList() ;
        exception(peekToken(), "Unrecognized: " + peekToken()) ;
        return null ;
    }

    protected final Node triplesBlankNode() {
        Token t = nextToken() ; // Skip [
        Node subject = profile.createBlankNode(currentGraph, t.getLine(), t.getColumn()) ;
        triplesBlankNode(subject) ;
        return subject ;
    }

    protected final void triplesBlankNode(Node subject) {
        if ( peekPredicate() )
            predicateObjectList(subject) ;
        expect("Triples not terminated properly in []-list", RBRACKET) ;
        // Exit: after the ]
    }

    protected final Node triplesFormula() {
        exception(peekToken(), "Not implemented (formulae, graph literals)") ;
        return null ;
    }

    protected final Node triplesList() {
        nextToken() ;
        Node lastCell = null ;
        Node listHead = null ;

        startList() ;

        for (;;) {
            Token errorToken = peekToken() ;
            if ( eof() )
                exception(peekToken(), "Unterminated list") ;

            if ( lookingAt(RPAREN) ) {
                nextToken() ;
                break ;
            }

            // The value.
            Node n = triplesNode() ;

            if ( n == null )
                exception(errorToken, "Malformed list") ;

            // Node for the list structre.
            Node nextCell = NodeFactory.createAnon() ;
            if ( listHead == null )
                listHead = nextCell ;
            if ( lastCell != null )
                emitTriple(lastCell, NodeConst.nodeRest, nextCell) ;
            lastCell = nextCell ;

            emitTriple(nextCell, NodeConst.nodeFirst, n) ;

            if ( !moreTokens() ) // Error.
                break ;
        }
        // On exit, just after the RPARENS

        if ( lastCell == null )
            // Simple ()
            return NodeConst.nodeNil ;

        // Finish list.
        emitTriple(lastCell, NodeConst.nodeRest, NodeConst.nodeNil) ;

        finishList() ;

        return listHead ;
    }

    // Signal start of a list
    protected void finishList() {}

    // Signal end of a list
    protected void startList() {}

    protected final void emitTriple(Node subject, Node predicate, Node object) {
        emit(subject, predicate, object) ;
    }

    private final void emitPrefix(String prefix, String iriStr) {
        dest.prefix(prefix, iriStr) ; 
    }

    private final void emitBase(String baseStr) { 
        dest.base(baseStr);
    }

    protected final Node tokenAsNode(Token token) {
        return profile.create(currentGraph, token) ;
    }
}
