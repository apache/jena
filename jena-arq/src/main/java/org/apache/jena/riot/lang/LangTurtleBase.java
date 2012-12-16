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
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.vocabulary.OWL ;

/** The main engine for all things Turtle-ish (Turtle, TriG). */
public abstract class LangTurtleBase<X> extends LangBase
{
    // See http://www.w3.org/TR/turtle/
    // Some predicates (if accepted)
    protected final static String KW_A              = "a" ;
    protected final static String KW_SAME_AS        = "=" ;
    protected final static String KW_LOG_IMPLIES    = "=>" ;
    protected final static String KW_TRUE           = "true" ;
    protected final static String KW_FALSE          = "false" ;
    
    protected final static boolean VERBOSE          = false ;
    //protected final static boolean CHECKING         = true ;
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
    
    protected LangTurtleBase(Tokenizer tokens, ParserProfile profile, RDFParserOutput dest)
    { 
        super(tokens, profile, dest) ;
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
            
            if ( lookingAt(KEYWORD) )
            {
                toplevelkeyword() ;
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

    protected final void toplevelkeyword()
    {
        Token t = peekToken() ; 
        String x = t.getImage() ;
        nextToken() ;
        
        if ( x.equalsIgnoreCase("BASE") )
        {
            directiveBase() ;
            return ;
        }
        
        if ( x.equalsIgnoreCase("PREFIX") )
        {
            directivePrefix() ;
            return ;
        }
        exception(t, "Unrecognized keyword: %s", x) ; 
    }
    
    protected final void directive()
    {
        // It's a directive ...
        Token t = peekToken() ; 
        String x = t.getImage() ;
        nextToken() ;
        
        if ( x.equals("base") )
        {
            directiveBase() ;
            expect("Base directive not terminated by a dot", DOT) ;
            return ;
        }
        
        if ( x.equals("prefix") )
        {
            directivePrefix() ;
            expect("Prefix directive not terminated by a dot", DOT) ;
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
        dest.prefix(prefix, iriStr) ;
        IRI iri = profile.makeIRI(iriStr, currLine, currCol) ;
        profile.getPrologue().getPrefixMap().add(prefix, iri) ;

        nextToken() ;
    }

    protected final void directiveBase()
    {
        Token token = peekToken() ;
        if ( ! lookingAt(IRI) )
            exception(token, "@base requires an IRI (found '"+token+"')") ;

        String baseStr = token.getImage() ;
        dest.base(baseStr) ;
        IRI baseIRI = profile.makeIRI(baseStr, currLine, currCol) ;
        nextToken() ;
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
            
            // Turtle, as spec'ed does not allow 
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
            // predicatelist continues - move over all ";"
            while ( lookingAt(SEMICOLON) )
                nextToken() ;
            if ( ! peekPredicate() )
                // Trailing (pointless) SEMICOLONs, no following predicate/object list.
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
            boolean strict =  profile.isStrictMode() ;
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
            boolean strict =  profile.isStrictMode() ;
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
