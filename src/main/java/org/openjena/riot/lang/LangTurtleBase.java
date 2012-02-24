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

import static org.openjena.riot.tokens.TokenType.* ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.riot.system.ParserProfile ;
import org.openjena.riot.tokens.Token ;
import org.openjena.riot.tokens.TokenType ;
import org.openjena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.vocabulary.OWL ;

/** The main engine for all things Turtle-ish (Turtle, TriG). */
public abstract class LangTurtleBase<X> extends LangBase<X>
{
    /* See http://www.w3.org/TeamSubmission/turtle/
     * http://www.w3.org/TR/turtle/
     */
    /*
[1]     turtleDoc       ::=     statement*
[2]     statement       ::=     directive '.' | triples '.' | ws+
[3]     directive       ::=     prefixID | base
[4]     prefixID        ::=     '@prefix' ws+ prefixName? ':' uriref
[5]     base            ::=     '@base'   ws+ uriref
[6]     triples         ::=     subject predicateObjectList
[7]     predicateObjectList     ::=     verb objectList ( ';' verb objectList )* ( ';')?
[8]     objectList      ::=     object ( ',' object)*
[9]     verb            ::=     predicate | 'a'
[10]    comment         ::=     '#' ( [^#xA#xD] )*
[11]    subject         ::=     resource | blank
[12]    predicate       ::=     resource
[13]    object          ::=     resource | blank | literal
[14]    literal         ::=     quotedString ( '@' language )? | datatypeString | integer | double | decimal | boolean
[15]    datatypeString  ::=     quotedString '^^' resource
[16]    integer         ::=     ('-' | '+')? [0-9]+
[17]    double          ::=     ('-' | '+')? ( [0-9]+ '.' [0-9]* exponent | '.' ([0-9])+ exponent | ([0-9])+ exponent )
[18]    decimal         ::=     ('-' | '+')? ( [0-9]+ '.' [0-9]* | '.' ([0-9])+ | ([0-9])+ )
[19]    exponent        ::=     [eE] ('-' | '+')? [0-9]+
[20]    boolean         ::=     'true' | 'false'
[21]    blank           ::=     nodeID | '[]' | '[' predicateObjectList ']' | collection
[22]    itemList        ::=     object+
[23]    collection      ::=     '(' itemList? ')'
[24]    ws              ::=     #x9 | #xA | #xD | #x20 | comment
[25]    resource        ::=     uriref | qname
[26]    nodeID          ::=     '_:' name
[27]    qname           ::=     prefixName? ':' name?
[28]    uriref          ::=     '<' relativeURI '>'
[29]    language        ::=     [a-z]+ ('-' [a-z0-9]+ )*
[30]    nameStartChar   ::=     [A-Z] | "_" | [a-z] | [#x00C0-#x00D6] | [#x00D8-#x00F6] | [#x00F8-#x02FF] | [#x0370-#x037D] | [#x037F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] | [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] | [#x10000-#xEFFFF]
[31]    nameChar        ::=     nameStartChar | '-' | [0-9] | #x00B7 | [#x0300-#x036F] | [#x203F-#x2040]
[32]    name            ::=     nameStartChar nameChar*
[33]    prefixName      ::=     ( nameStartChar - '_' ) nameChar*
[34]    relativeURI     ::=     ucharacter*
[35]    quotedString    ::=     string | longString
[36]    string          ::=     #x22 scharacter* #x22
[37]    longString      ::=     #x22 #x22 #x22 lcharacter* #x22 #x22 #x22
[38]    character       ::=     '\' 'u' hex hex hex hex | '\' 'U' hex hex hex hex hex hex hex hex |
                                '\\' | [#x20-#x5B] | [#x5D-#x10FFFF]
[39]    echaracter      ::=     character | '\t' | '\n' | '\r'
[40]    hex             ::=     [#x30-#x39] | [#x41-#x46]
[41]    ucharacter      ::=     ( character - #x3E ) | '\>'
[42]    scharacter      ::=     ( echaracter - #x22 ) | '\"'
[43]    lcharacter      ::=     echaracter | '\"' | #x9 | #xA | #xD  
     */
    
    // Predicates
    protected final static String KW_A              = "a" ;
    protected final static String KW_SAME_AS        = "=" ;
    protected final static String KW_LOG_IMPLIES    = "=>" ;
    protected final static String KW_TRUE           = "true" ;
    protected final static String KW_FALSE          = "false" ;
    
    protected final static boolean VERBOSE          = false ;
    //protected final static boolean CHECKING         = true ;
    public static boolean strict                    = false ;
    
//    protected final Prologue prologue ;
    
    // Current graph - null for default graph
    private Node currentGraph = null ;
    
    
    public final Node getCurrentGraph()
    {
        return currentGraph ;
    }

    public final void setCurrentGraph(Node graph)
    {
        // The syntax of the language determines this. 
//        if ( graph != null )
//            checker.check(graph, -1, -1) ;
        this.currentGraph = graph ;
    }

//    /** Provide access to the prologue.  
//     * Use with care.
//     */
//    public Prologue getPrologue()        { return profile ; }
//
//    /** Provide access to the prefix map.  
//     * Note this parser uses a custom, lightweight prefix mapping implementation.
//     * Use with care.
//     */
//    public PrefixMap getPrefixMap()        { return prologue.getPrefixMap() ; }
    
    protected LangTurtleBase(Tokenizer tokens, ParserProfile profile, Sink<X> sink)
    { 
        super(tokens, profile, sink) ;
    }
    
    @Override
    protected final void runParser()
    {
        while(moreTokens())
        {
            Token t = peekToken() ;
            if ( lookingAt(DIRECTIVE) )
            {
                directive() ;
                continue ;
            }
            
            oneTopLevelElement() ;
            
            if ( lookingAt(EOF) )
                break ;
        }
    }
    
    // Do one top level item for the language.
    protected abstract void oneTopLevelElement() ;

    /** Emit a triple - nodes have been checked as has legality of node type in location */
    protected abstract void emit(Node subject, Node predicate, Node object) ;

    protected final void directive()
    {
        // It's a directive ...
        Token t = peekToken() ; 
        String x = t.getImage() ;
        nextToken() ;
        
        if ( x.equals("base") )
        {
            directiveBase() ;
            return ;
        }
        
        if ( x.equals("prefix") )
        {
            directivePrefix() ;
            return ;
        }
        exception(t, "Unrecognized directive: %s", x) ;
    }
    
    protected final void directivePrefix()
    {
        // Raw - unresolved prefix name.
        if ( ! lookingAt(PREFIXED_NAME) )
            exception(peekToken(), "@prefix requires a prefix (found '"+peekToken()+"')") ;
        if ( peekToken().getImage2().length() != 0 )
            exception(peekToken(), "@prefix requires a prefix and no suffix (found '"+peekToken()+"')") ;
        String prefix = peekToken().getImage() ;
        nextToken() ;
        if ( ! lookingAt(IRI) )
            exception(peekToken(), "@prefix requires an IRI (found '"+peekToken()+"')") ;
        String iriStr = peekToken().getImage() ;
        IRI iri = profile.makeIRI(iriStr, currLine, currCol) ;
        profile.getPrologue().getPrefixMap().add(prefix, iri) ;

        nextToken() ;
        expect("Prefix directive not terminated by a dot", DOT) ;
    }

    protected final void directiveBase()
    {
        String baseStr = peekToken().getImage() ;
        IRI baseIRI = profile.makeIRI(baseStr, currLine, currCol) ;
        nextToken() ;
        
        expect("Base directive not terminated by a dot", DOT) ;
        profile.getPrologue().setBaseURI(baseIRI) ;
    }
    
    // Unlike many operations in this parser suite 
    // this does not assume that we have definitely
    // entering this state.  It does checks and may 
    // signal a parse exception.

    protected final void triplesSameSubject()
    {
        // Either a IRI/prefixed name or a construct that generates triples  
        
        // TriplesSameSubject -> Term PropertyListNotEmpty 
        if ( lookingAt(NODE) )
        {
            triples() ;
            return ;
        }
        
        // TriplesSameSubject -> TriplesNode PropertyList?
        if ( peekTriplesNodeCompound() )
        {
            Node n = triplesNodeCompound() ;

            // May be followed by: 
            //   A predicateObject list
            //   A DOT or EOF.
            // But if a DOT or EOF, then it can't have been () or [].
            
            // Turtle, as spec'ed does nto allow 
            // (1 2 3 4) .
            // There must be a predicate and object.
            
            // -- If strict turtle.
            if ( false )
            {
                if ( peekPredicate() )
                {
                    predicateObjectList(n) ;
                    expectEndOfTriples() ;
                    return ;
                }
                exception(peekToken(), "Prediate/object required after (...) and [...] - Unexpected token : %s", peekToken()) ;
            }
            // ---
            // If we allow top-level lists and [...].
            // Should check if () and [].
            
            if ( lookingAt(EOF) )
                return ;
            if ( lookingAt(DOT) )
            {
                nextToken() ;
                return ;
            }

            if ( peekPredicate() )
            {
                predicateObjectList(n) ;
                expectEndOfTriples() ;
                return ;
            }
            exception(peekToken(), "Unexpected token : %s", peekToken()) ;
        }
        exception(peekToken(), "Out of place: %s", peekToken()) ;
    }

    // Must be at least one triple. 
    protected final void triples()
    {
        // Looking at a node.
        Node subject = node() ;
        if ( subject == null )
            exception(peekToken(), "Not recognized: expected node: %s", peekToken().text()) ;
        
        nextToken() ;
        predicateObjectList(subject) ;
        expectEndOfTriples() ;
    }

    // Differs between Trutle and TriG.
    protected abstract void expectEndOfTriples() ;

    protected final void predicateObjectList(Node subject)
    {
        predicateObjectItem(subject) ;

        for(;;)
        {
            if ( ! lookingAt(SEMICOLON) )
                break ;
            // list continues - move over the ";"
            nextToken() ;
            if ( ! peekPredicate() )
                // Trailing (pointless) SEMICOLON, no following predicate/object list.
                break ;
            predicateObjectItem(subject) ;
        }
    }

    protected final void predicateObjectItem(Node subject)
    {
        Node predicate = predicate() ;
        nextToken() ;
        objectList(subject, predicate) ;
    }
    
    static protected final Node nodeSameAs = OWL.sameAs.asNode() ; 
    static protected final Node nodeLogImplies = Node.createURI("http://www.w3.org/2000/10/swap/log#implies") ;
    
    /** Get predicate - maybe null for "illegal" */
    protected final Node predicate()
    {
        Token t = peekToken() ;
        
        if ( t.hasType(TokenType.KEYWORD) )
        {
            Token tErr = peekToken() ;
            String image = peekToken().getImage() ;
            if ( image.equals(KW_A) )
                return NodeConst.nodeRDFType ;
            if ( !strict && image.equals(KW_SAME_AS) )
                return nodeSameAs ;
            if ( !strict && image.equals(KW_LOG_IMPLIES) )
                return NodeConst.nodeRDFType ;
            exception(tErr, "Unrecognized: "+image) ;
        }
        
        Node n = node() ;
        if ( n == null || ! n.isURI() )
            exception(t, "Expected IRI for predicate: got: %s", t) ; 
        return n ; 
    }

    /** Check raw token to see if it might be a predciate */
    protected final boolean peekPredicate()
    {
        if ( lookingAt(TokenType.KEYWORD) )
        {
            String image = peekToken().getImage() ;
            if ( image.equals(KW_A) )
                return true ;
            if ( !strict && image.equals(KW_SAME_AS) )
                return true ;
            if ( !strict && image.equals(KW_LOG_IMPLIES) )
                return true ;
            return false ; 
        }
//        if ( lookingAt(NODE) )
//            return true ; 
        if ( lookingAt(TokenType.IRI) )
            return true ;
        if ( lookingAt(TokenType.PREFIXED_NAME) )
            return true ;
        return false ;
    }
    
    /** Maybe "null" for not-a-node. */
    protected final Node node()
    {
        // Token to Node
        Node n = tokenAsNode(peekToken()) ;
        if ( n == null )
            return null ;
        return n ;
    }
    
    protected final void objectList(Node subject, Node predicate)
    {
        for(;;)
        {
            Node object = triplesNode() ;
            checkEmitTriple(subject, predicate, object) ;

            if ( ! moreTokens() )
                break ;
            if ( ! lookingAt(COMMA) )
                break ;
            // list continues - move over the ","
            nextToken() ;
        }
    }

    // A structure of triples that itself generates a node.  
    // Special checks for [] and (). 
    
    protected final Node triplesNode()
    {
        if ( lookingAt(NODE) )
        {
            Node n = node() ;
            nextToken() ;
            return n ; 
        }

        // Special words.
        if ( lookingAt(TokenType.KEYWORD) )
        {
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

            exception(tErr, "Unrecognized keyword: "+image) ; 
        }
        
        return triplesNodeCompound() ;
    }
        
    protected final boolean peekTriplesNodeCompound()
    {
        if ( lookingAt(LBRACKET) )
            return true ;
        if ( lookingAt(LBRACE) )
            return true ;
        if ( lookingAt(LPAREN) )
            return true ;
        return false ;
    }
    
    protected final Node triplesNodeCompound()
    {
        if ( lookingAt(LBRACKET) )
            return triplesBlankNode() ;
        if ( lookingAt(LBRACE) )
            return triplesFormula() ;
        if ( lookingAt(LPAREN) )
            return triplesList() ;
        exception(peekToken(), "Unrecognized: "+peekToken()) ;
        return null ;
    }
    
    protected final Node triplesBlankNode()
    {
        nextToken() ;        // Skip [
        Node subject = Node.createAnon() ;

        if ( peekPredicate() )
            predicateObjectList(subject) ;

        expect("Triples not terminated properly in []-list", RBRACKET) ;
        // Exit: after the ]
        return subject ;
    }
    
    protected final Node triplesFormula()
    {
        exception(peekToken(), "Not implemented") ;
        return null ;
    }
    
    protected final Node triplesList()
    {
        nextToken() ;
        Node lastCell = null ;
        Node listHead = null ;
        
        startList() ;
        
        for ( ;; )
        {
            Token errorToken = peekToken() ;
            if ( eof() )
                exception (peekToken(), "Unterminated list") ;
            
            if ( lookingAt(RPAREN) ) 
            {
                nextToken(); 
                break ;
            }
            
            // The value.
            Node n = triplesNode() ;
            
            if ( n == null )
                exception(errorToken, "Malformed list") ;
            
            // Node for the list structre.
            Node nextCell = Node.createAnon() ;
            if ( listHead == null )
                listHead = nextCell ;
            if ( lastCell != null )
                checkEmitTriple(lastCell, NodeConst.nodeRest, nextCell) ;
            lastCell = nextCell ;
            
            checkEmitTriple(nextCell, NodeConst.nodeFirst, n) ;

            if ( ! moreTokens() )   // Error.
                break ;
        }
        // On exit, just after the RPARENS
        
        if ( lastCell == null )
            // Simple ()
            return NodeConst.nodeNil ;
        
        // Finish list.
        checkEmitTriple(lastCell, NodeConst.nodeRest, NodeConst.nodeNil) ;

        finishList() ;

        return listHead ;
    }
   
    // Signal start of a list
    protected void finishList()     {}

    // Signal end of a list
    protected void startList()      {}
    

    protected final void checkEmitTriple(Node subject, Node predicate, Node object)
    {
        emit(subject, predicate, object) ;
    }

    protected final Node tokenAsNode(Token token) 
    {
        return profile.create(currentGraph, token) ;
    }
}
