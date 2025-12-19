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

import static org.apache.jena.riot.tokens.TokenType.*;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.tokens.StringType;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.TokenType;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.sparql.graph.NodeConst;

/**
 * The main engine for all things Turtle-ish (Turtle, TriG).
 * <p>
 * This parser generates a form of generalized RDF
 * with literals and triple terms as subjects,
 * but not arbitrary predicates.
 * <p>
 * The additional conditions area applied when the triples are created by the {@link ParserProfile}
 */
public abstract class LangTurtleBase extends LangBase {
    // See http://www.w3.org/TR/turtle/
    // Some predicates (if accepted)
    protected final static String  KW_A           = "a";
    protected final static String  KW_SAME_AS     = "=";
    //protected final static String  KW_LOG_IMPLIES = "=>";
    protected final static String  KW_TRUE        = "true";
    protected final static String  KW_FALSE       = "false";

    protected final static boolean VERBOSE        = false;
    // protected final static boolean CHECKING = true;
    // Current graph - null for default graph
    private Node                   currentGraph   = null;

    protected final PrefixMap prefixMap;

    protected final Node getCurrentGraph() {
        return currentGraph;
    }

    protected final void setCurrentGraph(Node graph) {
        this.currentGraph = graph;
    }

    protected LangTurtleBase(Tokenizer tokens, ParserProfile profile, StreamRDF dest) {
        super(tokens, profile, dest);
        prefixMap = profile.getPrefixMap();
    }

    @Override
    protected final void runParser() {
        while (moreTokens()) {
            Token t = peekToken();
            if ( lookingAt(DIRECTIVE) ) {
                directiveAtWord(); // @form.
                continue;
            }

            if ( lookingAt(KEYWORD) ) {
                String text = t.getImage();
                if ( text.equalsIgnoreCase("PREFIX") || text.equalsIgnoreCase("BASE") || text.equalsIgnoreCase("VERSION") ) {
                    directiveKeyword();
                    continue;
                }
            }

            oneTopLevelElement();

            if ( lookingAt(EOF) )
                break;
        }
    }

    // Do one top level item for the language.
    protected abstract void oneTopLevelElement();

    /**
     * Emit a triple - nodes have been checked as has the
     * legality of node type in location.
     */
    protected abstract void emit(Node subject, Node predicate, Node object);

    // Directive, keyword form.
    protected final void directiveKeyword() {
        Token t = peekToken();
        String x = t.getImage();
        nextToken();

        if ( x.equalsIgnoreCase("BASE") ) {
            directiveBase();
            return;
        }

        if ( x.equalsIgnoreCase("PREFIX") ) {
            directivePrefix();
            return;
        }

        if ( x.equalsIgnoreCase("VERSION") ) {
            directiveVersion();
            return;
        }

        exception(t, "Unrecognized keyword for directive: %s", x);
    }

    // Directive, @-form.
    protected final void directiveAtWord() {
        Token t = peekToken();
        String x = t.getImage();
        nextToken();
        processAtDirective(t, x);
    }

    private void processAtDirective(Token t, String x) {
        if ( x.equals("base") ) {
            directiveBase();
            if ( isStrictMode() )
                // The line number is probably one ahead due to the newline
                expect("Base directive not terminated by a dot", DOT);
            else
                skipIf(DOT);
            return;
        }

        if ( x.equals("prefix") ) {
            directivePrefix();
            if ( isStrictMode() )
                // The line number is probably one ahead due to the newline
                expect("Prefix directive not terminated by a dot", DOT);
            else
                skipIf(DOT);
            return;
        }
        if ( x.equals("version") ) {
            directiveVersion();
            if ( isStrictMode() )
                // The line number is probably one ahead due to the newline
                expect("Prefix directive not terminated by a dot", DOT);
            else
                skipIf(DOT);
            return;
        }
        exception(t, "Unrecognized directive: %s", x);
    }

    protected final void directivePrefix() {
        // Raw - unresolved prefix name.
        if ( !lookingAt(PREFIXED_NAME) )
            exception(peekToken(), "PREFIX or @prefix requires a prefix (found '" + peekToken() + "')");
        if ( peekToken().getImage2().length() != 0 )
            exception(peekToken(), "PREFIX or @prefix requires a prefix with no suffix (found '" + peekToken() + "')");
        String prefix = peekToken().getImage();
        nextToken();
        if ( !lookingAt(IRI) )
            exception(peekToken(), "@prefix requires an IRI (found '" + peekToken() + "')");
        String str = peekToken().getImage();
        String iri = profile.resolveIRI(str, currLine, currCol);
        prefixMap.add(prefix, iri);
        emitPrefix(prefix, iri);
        nextToken();
    }

    protected final void directiveBase() {
        Token token = peekToken();
        if ( !lookingAt(IRI) )
            exception(token, "BASE or @base requires an IRI (found '" + token + "')");
        String str = token.getImage();
        String baseIRI = profile.resolveIRI(str, currLine, currCol);
        profile.setBaseIRI(baseIRI);
        emitBase(baseIRI);
        nextToken();
    }

    protected final void directiveVersion() {
        Token token = peekToken();
        String directive = null;

        if ( token.hasType(TokenType.LITERAL_LANG) ) {
            // The case of
            //    VERSION "1.2"\n@prefix <uri>
            // Plain string followed by old style directive tokenized as a LITERAL_LANG
            Token subToken = token.getSubToken1();
            directive = token.getImage2(); // This is the "language" without '@'
            token = subToken;
        }

        if ( ! token.isString() )
            exception(token, "Version must be a string (found '" + token + "')");

        // Single quoted string only. '1.2' and "1.2", not '''- or """- strings.
        StringType stringType = token.getStringType();
        switch(stringType) {
            case STRING1, STRING2 ->{}
            case LONG_STRING1, LONG_STRING2 ->
                exception(token, "Triple-quoted strings not allowed for the version string (found '" + token + "')");
            default ->
                exception(token, "Expected a quoted string for the version setting (found '" + token + "')");
        }
        String versionStr = token.getImage();
        emitVersion(versionStr);
        nextToken();

        // If we broke up the LITERAL_LANG token, now process
        // the language tag part as a directive.
        if ( directive != null )
            processAtDirective(token, directive);
    }

    // [8] triples ::= subject predicateObjectList
    //               | blankNodePropertyList predicateObjectList?
    //               | reifiedTriple predicateObjectList?
    //
    // Unlike many operations in this parser suite
    // this does not assume that we are definitely
    // entering this state. It does checks and may
    // signal a parse exception.

    protected final void triples() {
        // Either a IRI/prefixed name or a construct that generates triples
        //     subject predicateObjectList
        if ( lookingAt(NODE) ) {
            triplesSameSubject();
            return;
        }

        boolean maybeList = lookingAt(LPAREN);

        //     blankNodePropertyList predicateObjectList?
        if ( peekTriplesNodeCompound() ) { // LBRACKET, LBRACE, LPAREN
            Node n = triplesNodeCompound();

            // May be followed by:
            //   A predicateObject list
            //   A DOT or EOF.
            //   But if a DOT or EOF, then it can't have been () or [].

            // Turtle, as spec'ed does not allow
            // (1 2 3 4) .
            // There must be a predicate and object.

            // -- If strict turtle.
            if ( isStrictMode() && maybeList ) {
                if ( peekPredicate() ) {
                    predicateObjectList(n);
                    expectEndOfTriples();
                    return;
                }
                exception(peekToken(), "Predicate/object required after (...) - Unexpected token : %s",
                          peekToken());
            }
            // ---
            // If we allow top-level lists and [...].
            // Should check if () and [].

            if ( lookingAt(EOF) )
                return;
            if ( lookingAt(DOT) ) {
                nextToken();
                return;
            }

            if ( peekPredicate() )
                predicateObjectList(n);
            expectEndOfTriples();
            //exception(peekToken(), "Unexpected token : %s", peekToken());
            return;
        }

        // RDF 1.2
        // reified triple, possibly a declaration (empty predicateObjectList).
        //     reifiedTriple predicateObjectList?
        // <<>> subject position. Rule [10]
        if ( lookingAt(LT2) ) {
            Node subject = parseReifiedTriple();
            // predicateObjectList may be empty - peek for DOT.
            if ( lookingAt(DOT) ) {
                // ReifiedTriple declaration.
                nextToken();
                return;
            }
            predicateObjectList(subject);
            expectEndOfTriples();
            return;
        }

        // Triple Term in the subject position.
        // This generates an error as the triple is created.
        // The parser is more general.
        if ( lookingAt(L_TRIPLE) ) {
            Node subject = parseTripleTerm();
            return;
        }
        exception(peekToken(), "Out of place: %s", peekToken());
    }

    // RDF 1.2
    // [26]  reifiedTriple ::= '<<' (subject | reifiedTriple) verb object reifier? '>>'
    // [26]  reifiedTriple ::= '<<' rtSubject verb rtObject reifier? '>>'
    // Assumes looking at << (LT2) on entry
    private Node parseReifiedTriple() {
        Token startToken = nextToken(); // LT2
        long startLine = startToken.getLine();
        long startColumn = startToken.getColumn();

        Node s = rtSubject(startToken);
        Node p = predicate();
        // rtObject - no blankPredicateObjectList or collection.
        Node o = rtObject(startToken);

        Node reif = possibleReifier(s, p, o, startLine, startColumn);

        if ( ! lookingAt(GT2) )
            exception(peekToken(), "Expected >>, found %s", peekToken().text());
        nextToken();

        Node tripleTerm = profile.createTripleTerm(s, p, o, startLine, startColumn);
        emitTriple(reif, NodeConst.nodeReifies, tripleTerm);
        return reif;
    }

    // -- rtSubject rules
    private Node rtSubject(Token startToken) {
        if ( lookingAt(LT2) )
            return parseReifiedTriple();
        Node s = possibleAnon() ;
        if ( s != null )
            return s;
        s = node();
        if ( ! (s.isURI() || s.isBlank() ) )
            // ReifiedTriple covered by branch.
            exception(peekToken(), "Subject in a reified triple is not a URI, blank node or a nested reified triple: %s", s);
        return s;
    }

    // -- rtObject rules
    private Node rtObject(Token startToken) {
        if (lookingAt(LT2) )
            return parseReifiedTriple();
        Node o = possibleAnon() ;
        if ( o != null )
            return o;
        o = possibleBooleanLiteral();
        if (o != null )
            return o;

        // Not compound triples (blankPredicateObjectList, collections).
        o = object();

        if ( ! (o.isURI() || o.isBlank() || o.isLiteral() || o.isTripleTerm() ) )
            exception(startToken, "Illgeal object in a reified triple: %s", o);
        return o;
    }

    // Possible [], not [ :p 123 ]
    private Node possibleAnon() {
        if ( ! lookingAt(LBRACKET) )
            return null;
        // Consume LBRACKET
        nextToken();
        if ( ! lookingAt(RBRACKET) )
            exception(peekToken(), "Found '[' in reified triple. It must be followed by ']' but got: "+peekToken());
        // Consume RBRACKET
        Token token = nextToken();
        Node x = profile.createBlankNode(currentGraph, token.getLine(), token.getColumn());
        return x;
    }

    private Node parseTripleTerm() {
        Token entryToken = nextToken();
        Node s = ttSubject();
        if ( s.isTripleTerm() )
            exception(entryToken, "Subject of a triple term is a triple term");
        Node p = predicate();
        Node o = ttObject();
        if ( ! lookingAt(R_TRIPLE) )
            exception(peekToken(), "Expected )>>, found %s", peekToken().text());
        nextToken();
        return profile.createTripleTerm(s, p, o, entryToken.getLine(), entryToken.getColumn());
    }

    protected Node possibleReifier(Node s, Node p, Node o, long line, long column) {
        if ( lookingAt(TokenType.VBAR) )
            exception(peekToken(), "Bad syntax: reifiers are '~', not '|'");

        if ( ! lookingAt(TokenType.TILDE) )
            return profile.createBlankNode(currentGraph, line, column);
        return Reifier(s, p, o, line, column);
    }

    protected Node Reifier(Node s, Node p, Node o, long line, long column) {
        // Tilde
        Token tildeToken = nextToken();
        Token tokenReif = peekToken();

        if ( lookingAt(TokenType.LITERAL_DT) || lookingAt(TokenType.LITERAL_LANG) ) {
            nextToken();
            exception(tildeToken, "Reifiers are URIs or blank nodes: found %s", tokenReif);
        }

        Node reif;
        // URI or bNode
        if ( lookingAtIRIorBNode() ) {
            nextToken();
            reif = tokenAsNode(tokenReif);
        } else if ( lookingAt(LBRACKET) ) {
            // ANON
            nextToken();
            Token t = peekToken();
            if ( ! lookingAt(RBRACKET) )
                exception(peekToken(), "Bad %s in RDF triple. Expected ] after [", "reifier", peekToken().text());
            nextToken();
            reif = profile.createBlankNode(currentGraph, t.getLine(), t.getColumn());
        } else {
            // Just "~" No reifier id
            // or some syntax error we will detect later.
            reif = profile.createBlankNode(currentGraph, tildeToken.getLine(), tildeToken.getColumn());
        }
        return reif;
    }

    private Node ttSubject() {
        Node node = tripleTermSubjectObject(Posn.SUBJECT);
        return node;
    }

    private Node ttObject() {
        Node node = tripleTermSubjectObject(Posn.OBJECT);
        return node;
    }

    // Generalized.
    // Triples with, for example, literals in the subject position, are rejected when the triple is created.
    private Node subject() {
        return nodeTerm();
    }

    private Node object() {
        return nodeTerm();
    }


    // Single token terms, triple terms <<( ... )>> and reified triples. << ... >>
    private Node nodeTerm() {
        if ( lookingAt(LT2) )
            return parseReifiedTriple();
        if ( lookingAt(L_TRIPLE) )
            return parseTripleTerm();
        Node node = node();
        return node;
    }

    // Keywords 'true' and 'false'
    private Node possibleBooleanLiteral() {
        if ( ! lookingAt(TokenType.KEYWORD) )
            return null;
        Token tErr = peekToken();
        // Location independent node words
        String image = peekToken().getImage();
        nextToken();
        if ( image.equals(KW_TRUE) )
            return NodeConst.nodeTrue;
        if ( image.equals(KW_FALSE) )
            return NodeConst.nodeFalse;
        if ( image.equals(KW_A) )
            exception(tErr, "Keyword 'a' not legal at this point");

        exception(tErr, "Unrecognized keyword: " + image);
        return null;
    }

    enum Posn {
        SUBJECT("subject"), OBJECT("object");
        private String label;
        Posn(String label) { this.label = label; }
    }

    /** Any RDFTerm that can appear in a triple term subject or object position. */
    private Node tripleTermSubjectObject(Posn posn) {
        if ( lookingAt(L_TRIPLE) )
            return parseTripleTerm();

        // ANON
        // [14]     blankNodePropertyList   ::=     '[' predicateObjectList ']'
        //    is at least one predicate /object.
        // Method triplesNodeCompound ()-> triplesBlankNode(subject)
        //    can cope with zero length, covering grammar token ANON and rule [7] predicateObjectList cases

        // XXX Generalize and reuse in Reifier.
        // []
        if ( lookingAt(LBRACKET) ) {
            nextToken();
            Token t = peekToken();
            if ( ! lookingAt(RBRACKET) )
                exception(peekToken(), "Bad %s in RDF triple. Expected ] after [", posn.label, peekToken().text());
            nextToken();
            return profile.createBlankNode(currentGraph, t.getLine(), t.getColumn());
        }

        Node n = possibleBooleanLiteral();
        if ( n != null )
            return n;

        // Single token terms
        if ( ! lookingAt(NODE) ) {
            exception(peekToken(), "Bad %s in triple term: %s", posn.label, peekToken().text());
        }
        Node node = node();

        // Further restrictions due to position.
        switch (posn) {
            case OBJECT->{} // None
            case SUBJECT->{
                if ( node.isLiteral() )
                    exception(peekToken(), "Literals are not legal in the %s position.", posn.label);
                if ( node.isTripleTerm() ) {
                    exception(peekToken(), "Triple terms are not legal in the %s position.", posn.label);
                }
            }
        }
        return node;
    }

    // Must be at least one triple.
    protected final void triplesSameSubject() {
        // Looking at a node.
        Node subject = subject();
        if ( subject == null )
            exception(peekToken(), "Not recognized: expected node: %s", peekToken().text());

        predicateObjectList(subject);
        expectEndOfTriples();
    }

    // Differs between Turtle and TriG.
    // TriG, inside {} does not need the trailing DOT
    protected abstract void expectEndOfTriples();

    // The DOT is required by Turtle (strictly).
    // It is not in N3 and SPARQL.

    protected void expectEndOfTriplesTurtle() {
        if ( isStrictMode() )
            expect("Triples not terminated by DOT", DOT);
        else
            expectOrEOF("Triples not terminated by DOT", DOT);
    }

    protected final boolean lookingAtIRIorBNode() {
        if ( eof() )
            return false;
        Token t = peekToken();
        // Does not cover "[ ]"
        return t.isIRI() || t.isBNode();
    }

    protected final void predicateObjectList(Node subject) {
        predicateObjectItem(subject);

        for (;;) {
            if ( !lookingAt(SEMICOLON) )
                break;
            // predicateList continues - move over all ";"
            while (lookingAt(SEMICOLON))
                nextToken();
            if ( !peekPredicate() )
                // Trailing (pointless) SEMICOLONs, no following
                // predicate/object list.
                break;
            predicateObjectItem(subject);
        }
    }

    protected final void predicateObjectItem(Node subject) {
        Node predicate = predicate();
        objectList(subject, predicate);
    }

    static protected final Node nodeSameAs     = NodeConst.nodeOwlSameAs;
    static protected final Node nodeLogImplies = NodeFactory.createURI("http://www.w3.org/2000/10/swap/log#implies");

    // [11]  verb  ::= predicate | 'a'
    // [13]  predicate ::= iri
    // and '=' (owl:sameAs),
    /** Get predicate - return null for "illegal" */
    protected final Node predicate() {
        if ( lookingAt(TokenType.KEYWORD) ) {
            Token kwToken = nextToken();
            String image = kwToken.getImage();
            if ( image.equals(KW_A) )
                return NodeConst.nodeRDFType;
            // N3-isms
            if ( !isStrictMode() && image.equals(KW_SAME_AS) )
                return nodeSameAs;
            // Relationship between two formulae in N3.
//            if ( !isStrictMode() && image.equals(KW_LOG_IMPLIES) )
//                return log:implies;
            exception(kwToken, "Unrecognized keyword: " + image);
        }

        Token token = peekToken();
        Node n = node();
        if ( n == null || !n.isURI() )
            exception(token, "Expected IRI for predicate: got: %s", token);
        return n;
    }

    /** Check raw token to see if it might be a predicate */
    protected final boolean peekPredicate() {
        if ( lookingAt(TokenType.KEYWORD) ) {
            String image = peekToken().getImage();
            if ( image.equals(KW_A) )
                return true;
            if ( !isStrictMode() && image.equals(KW_SAME_AS) )
                return true;
//            if ( !isStrictMode && image.equals(KW_LOG_IMPLIES) )
//                return true;
            return false;
        }
        // if ( lookingAt(NODE) )
        // return true;
        if ( lookingAt(TokenType.IRI) )
            return true;
        if ( lookingAt(TokenType.PREFIXED_NAME) )
            return true;
        return false;
    }

    /**
     *  Create a Node for the current single token.
     *  <p>
     *  It does not cover tripleTerms, which involves multiple tokens,
     *  nodes/triples for compound structures, {@code ()} nor {@code []}.
     *  Returns "null" for not-a-node.
     */
    protected final Node node() {
        Node n = tokenAsNode(peekToken());
        if ( n == null )
            return null;
        nextToken();
        return n;
    }

    protected final void objectList(Node subject, Node predicate) {
        for (;;) {
            // object ::=
            Node object = triplesNode();
            emitTriple(subject, predicate, object);

            // Maybe annotation.
            possibleAnnotations(subject, predicate, object);
            if ( !moreTokens() )
                break;
            if ( !lookingAt(COMMA) )
                break;
            // list continues - move over the ","
            nextToken();
        }
    }

    private void possibleAnnotations(Node subject, Node predicate, Node object) {
        for(;;) {
            if ( !lookingAt(TokenType.TILDE) && !lookingAt(L_ANN) && /*old style, not adopted*/ !lookingAt(TokenType.VBAR) )
                return;

            Token tokenReifer = peekToken();
            // Always allocate. TILDE or L_ANN.
            Node reif = possibleReifier(subject, predicate, object, tokenReifer.getLine(), tokenReifer.getColumn());
            Node tripleTerm = profile.createTripleTerm(subject, predicate, object, tokenReifer.getLine(), tokenReifer.getColumn());
            emit(reif, NodeConst.nodeReifies, tripleTerm);

            // Annotation syntax
            if ( lookingAt(L_ANN) ) {
                Token tNext = nextToken();
                if ( lookingAt(R_ANN) )
                    exception(tNext, "Empty annotation");
                predicateObjectList(reif);
                expect("Missing end annotation", R_ANN);
            }
        }
    }

    // A structure of triples that itself generates a node.
    // Special checks for [] and ().

    // XXX Use object()
    protected final Node triplesNode() { // == [12] object in the grammar.
        if ( lookingAt(NODE) ) {
            Node n = node();
            return n;
        }
        if ( lookingAt(LT2) )
            return parseReifiedTriple();
        if ( lookingAt(L_TRIPLE) )
            return parseTripleTerm();
        Node n = possibleBooleanLiteral();
        if ( n != null )
            return n;
        return triplesNodeCompound();
    }

    protected final boolean peekTriplesNodeCompound() {
        if ( lookingAt(LBRACKET) )
            return true;
        if ( lookingAt(LBRACE) )
            return true;
        if ( lookingAt(LPAREN) )
            return true;
        return false;
    }

    protected final Node triplesNodeCompound() {
        if ( lookingAt(LBRACKET) )
            return triplesBlankNode();
        if ( lookingAt(LBRACE) )
            return triplesFormula();
        if ( lookingAt(LPAREN) )
            return triplesList();
        exception(peekToken(), "Unrecognized (expected an RDF Term): " + peekToken());
        return null;
    }

    protected final Node triplesBlankNode() {
        Token t = nextToken(); // Skip [
        Node subject = profile.createBlankNode(currentGraph, t.getLine(), t.getColumn());
        triplesBlankNode(subject);
        return subject;
    }

    protected final void triplesBlankNode(Node subject) {
        if ( peekPredicate() )
            predicateObjectList(subject);
        expect("Triples not terminated properly in []-list", RBRACKET);
        // Exit: after the ]
    }

    protected final Node triplesFormula() {
        exception(peekToken(), "Not implemented (formulae, graph literals)");
        return null;
    }

    protected final Node triplesList() {
        nextToken();
        Node lastCell = null;
        Node listHead = null;

        startList();

        for (;;) {
            if ( eof() )
                exception(peekToken(), "Unterminated list");

            if ( lookingAt(RPAREN) ) {
                nextToken();
                break;
            }

            Token elementToken = peekToken();
            // The value.
            Node n = triplesNode();
            if ( n == null )
                exception(elementToken, "Malformed list");

            // Node for the list structure.
            Node nextCell = profile.createBlankNode(currentGraph, elementToken.getLine(), elementToken.getColumn());
            if ( listHead == null )
                listHead = nextCell;
            if ( lastCell != null )
                emitTriple(lastCell, NodeConst.nodeRest, nextCell);
            lastCell = nextCell;

            emitTriple(nextCell, NodeConst.nodeFirst, n);
        }
        // On exit, just after the RPARENS

        if ( lastCell == null ) {
            // Simple ()
            finishList();
            return NodeConst.nodeNil;
        }

        // Finish list.
        emitTriple(lastCell, NodeConst.nodeRest, NodeConst.nodeNil);
        finishList();
        return listHead;
    }

    // Signal start of a list
    protected void finishList() {}

    // Signal end of a list
    protected void startList() {}

    protected final void emitTriple(Node subject, Node predicate, Node object) {
        emit(subject, predicate, object);
    }

    private final void emitPrefix(String prefix, String iriStr) {
        dest.prefix(prefix, iriStr);
    }

    private final void emitBase(String baseStr) {
        dest.base(baseStr);
    }

    private final void emitVersion(String versionStr) {
        dest.version(versionStr)  ;
    }

    protected final Node tokenAsNode(Token token) {
        return profile.create(currentGraph, token);
    }
}
