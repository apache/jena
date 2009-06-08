/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import static com.hp.hpl.jena.riot.tokens.TokenType.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import atlas.event.Event;
import atlas.event.EventManager;
import atlas.iterator.PeekIterator;
import atlas.lib.Sink;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.riot.*;
import com.hp.hpl.jena.riot.tokens.Token;
import com.hp.hpl.jena.riot.tokens.TokenType;
import com.hp.hpl.jena.riot.tokens.Tokenizer;

import com.hp.hpl.jena.sparql.core.NodeConst;
import com.hp.hpl.jena.vocabulary.OWL;

public class LangTurtle
{
    /* See http://www.w3.org/TeamSubmission/turtle/ */

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
    
    private static Logger log = LoggerFactory.getLogger(LangTurtle.class) ;
    
    // Predicates
    private static String KW_A              = "a" ;
    private static String KW_SAME_AS        = "=" ;
    private static String KW_LOG_IMPLIES    = "=>" ;
    private static String KW_TRUE           = "true" ;
    private static String KW_FALSE          = "false" ;

    
    private static final boolean VERBOSE    = false ;
    private static final boolean CHECKING   = true ;
    private final boolean strict            = false ;
    
    private final Prologue prologue ;
    private final Checker checker = new Checker(null) ;

    /** Provide access to the prefix map.  
     * Note this parser uses a custom, lightweight prefix mapping implementation.
     * Use with care.
     */
    public PrefixMap getPrefixMap()        { return prologue.getPrefixMap() ; }
    
    private final Tokenizer tokens ;
    private final PeekIterator<Token> peekIter ;
    private final Sink<Triple> sink ;
    
//    public LangTurtle(Tokenizer tokens)
//    {
//        this("http://example/", tokens, new PrintingSink(log)) ;
//    }
    
    public LangTurtle(String baseURI, Tokenizer tokens, Sink<Triple> sink)
    { 
        this.tokens = tokens ;
        this.peekIter = new PeekIterator<Token>(tokens) ;
        this.sink = sink ;
        this.prologue = new Prologue(new PrefixMap(), new IRIResolver(baseURI)) ;
    }
    
    public void parse()
    {
        EventManager.send(sink, new Event(RIOT.startRead, null)) ;
        while(moreTokens())
        {
            if ( lookingAt(DIRECTIVE) )
            {
                if ( VERBOSE ) log.info(">> directive") ;
                directive() ;
                if ( VERBOSE ) log.info("<< directive") ;
                continue ;
            }
            
            // Triples node.
            
            // TriplesSameSubject -> TriplesNode PropertyList?
            if ( peekTriplesNodeCompound() )
            {
                if ( VERBOSE ) log.info(">> compound") ;
                Node n = triplesNodeCompound() ;
                if ( VERBOSE ) log.info("<< compound") ;
                // May be followed by: 
                //   A predicateObject list
                //   A DOT or EOF.
                if ( lookingAt(EOF) )
                    break ;
                if ( lookingAt(DOT) )
                {
                    move() ;
                    continue ;
                }
                if ( peekPredicate() )
                {
                    predicateObjectList(n) ;
                    expectEndOfTriples() ;
                    continue ;
                }
                exception("Unexpected token : %s", token()) ;
            }

            // TriplesSameSubject -> Term PropertyListNotEmpty 
            if ( lookingAt(NODE) )
            {
                if ( VERBOSE ) log.info(">> triples") ;
                triples() ;
                if ( VERBOSE ) log.info("<< triples") ;
                continue ;
            }
            exception("Out of place: %s", token()) ;
        }
        EventManager.send(sink, new Event(RIOT.finishRead, null)) ;
    }
    
    private void directive()
    {
        String x = token().getImage() ;
        move() ;
        
        if ( x.equals("base") )
        {
            if ( VERBOSE ) log.info("@base") ;
            directiveBase() ;
            return ;
        }
        
        if ( x.equals("prefix") )
        {
            if ( VERBOSE ) log.info("@prefix") ;
            directivePrefix() ;
            return ;
            
        }
        exception("Unregcognized directive: %s", x) ;
    }
    
    private void directivePrefix()
    {
        // Raw - unresolved prefix name.
        if ( ! lookingAt(PREFIXED_NAME) )
            exception("@prefix requires a prefix (found '"+tokenRaw()+"')") ;
        if ( tokenRaw().getImage2().length() != 0 )
            exception("@prefix requires a prefix and no suffix (found '"+tokenRaw()+"')") ;
        String prefix = tokenRaw().getImage() ;
        move() ;
        if ( ! lookingAt(IRI) )
            exception("@prefix requires an IRI (found '"+token()+"')") ;
        String iriStr = tokenRaw().getImage() ;
        // CHECK
        IRI iri = prologue.getResolver().resolveSilent(iriStr) ;
        checker.checkIRI(iri) ;
        prologue.getPrefixMap().add(prefix, iri) ;
        move() ;
        if ( VERBOSE ) log.info("@prefix "+prefix+":  "+iri.toString()) ;
        expect("Prefix directive not terminated by a dot", DOT) ;
    }

    private void directiveBase()
    {
        String baseStr = tokenRaw().getImage() ;
        // CHECK
        IRI baseIRI = prologue.getResolver().resolve(baseStr) ;
        checker.checkIRI(baseIRI) ;
        
        if ( VERBOSE ) log.info("@base <"+baseIRI+">") ;
        move() ;
        
        expect("Base directive not terminated by a dot", DOT) ;
        prologue.setBaseURI(new IRIResolver(baseIRI)) ;
    }

    // Must be at least one triple.
    private void triples()
    {
        Node subject = node() ;
        if ( subject == null )
            exception("Not recognized: expected directive or triples: %s", token().text()) ;
        
        move() ;
        predicateObjectList(subject) ;
        expectEndOfTriples() ;
    }

    private void expectEndOfTriples()
    {
        // The DOT is required by Turtle (strictly).
        // It is not in N3 and SPARQL.
    
        if ( strict )
            expect("Triples not terminated by DOT", DOT) ;
        else
            expectOrEOF("Triples not terminated by DOT", DOT) ;
    }

    private void predicateObjectList(Node subject)
    {
        if ( VERBOSE ) log.info("predicateObjectList("+subject+")") ;
        predicateObjectItem(subject) ;

        for(;;)
        {
            if ( ! lookingAt(SEMICOLON) )
                break ;
            // list continues - move over the ";"
            move() ;
            if ( ! peekPredicate() )
                // Trailing (pointless) SEMICOLON, no following predicate/object list.
                break ;
            predicateObjectItem(subject) ;
        }
    }

    private void predicateObjectItem(Node subject)
    {
        Node predicate = predicate() ;
        move() ;
        objectList(subject, predicate) ;
    }
    
    static private Node nodeSameAs = OWL.sameAs.asNode() ; 
    static private Node nodeLogImplies = Node.createURI("http://www.w3.org/2000/10/swap/log#implies") ;
    
    /** Get predicate - maybe null for "illegal" */
    private Node predicate()
    {
        if ( lookingAt(TokenType.KEYWORD) )
        {
            String image = tokenRaw().getImage() ;
            if ( image.equals(KW_A) )
                return NodeConst.nodeRDFType ;
            if ( !strict && image.equals(KW_SAME_AS) )
                return nodeSameAs ;
            if ( !strict && image.equals(KW_LOG_IMPLIES) )
                return NodeConst.nodeRDFType ;
            exception("Unrecognized: "+image) ;
        }
            
        // Maybe null
        return node() ; 
    }

    /** Check raw token to see if it might be a predciate */
    private boolean peekPredicate()
    {
        if ( lookingAt(TokenType.KEYWORD) )
        {
            String image = tokenRaw().getImage() ;
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
    
    private Node node()
    {
        // Resolve
        // CHECK
        // This is the only place where Nodes are created for triples.
        Node n = token().asNode() ; 
        checker.check(n) ; 
        return n ;
    }
    
    private void objectList(Node subject, Node predicate)
    {
        if ( VERBOSE ) log.info("objectList("+subject+", "+predicate+")") ;
        for(;;)
        {
            Node object = triplesNode() ;
            emit(subject, predicate, object) ;

            if ( ! moreTokens() )
                break ;
            if ( ! lookingAt(COMMA) )
                break ;
            // list continues - move over the ","
            move() ;
        }
    }

    // A structure of triples that itself generates a node.  [] and (). 
    
    private Node triplesNode()
    {
        if ( lookingAt(NODE) )
        {
            Node n = node() ;
            move() ;
            return n ; 
        }

        // Special words.
        if ( lookingAt(TokenType.KEYWORD) )
        {
            // Location independent node words
            String image = tokenRaw().getImage() ;
            move() ;
            if ( image.equals(KW_TRUE) )
                return NodeConst.nodeTrue ;
            if ( image.equals(KW_FALSE) )
                return NodeConst.nodeFalse ;
            exception("Unrecognized keyword: "+image) ; 
        }
        
        return triplesNodeCompound() ;
    }
        
    private boolean peekTriplesNodeCompound()
    {
        if ( lookingAt(LBRACKET) )
            return true ;
        if ( lookingAt(LBRACE) )
            return true ;
        if ( lookingAt(LPAREN) )
            return true ;
        return false ;
    }
    
    private Node triplesNodeCompound()
    {
        if ( lookingAt(LBRACKET) )
            return triplesBlankNode() ;
        if ( lookingAt(LBRACE) )
            return triplesFormula() ;
        if ( lookingAt(LPAREN) )
            return triplesList() ;
        exception("Unrecognized: "+token()) ;
        return null ;
    }
    
    private Node triplesBlankNode()
    {
        move() ;        // Skip [
        Node subject = Node.createAnon() ;

        if ( peekPredicate() )
            predicateObjectList(subject) ;

        expect("Triples not terminated properly in []-list", RBRACKET) ;
        // Exit: after the ]
        return subject ;
    }
    
    private Node triplesFormula()
    {
        exception("Not implemented") ;
        return null ;
    }
    
    private Node triplesList()
    {
        move() ;
        Node lastCell = null ;
        Node listHead = null ;
        
        for ( ;; )
        {
            if ( eof() )
                exception ("Unterminated list") ;
            
            if ( lookingAt(RPAREN) ) 
            {
                move(); 
                break ;
            }
            
            // The value.
            Node n = triplesNode() ;
            
            if ( n == null )
                exception("Malformed list") ;
            
            // Node for the list structre.
            Node nextCell = Node.createAnon() ;
            if ( listHead == null )
                listHead = nextCell ;
            if ( lastCell != null )
                emit(lastCell, NodeConst.nodeRest, nextCell) ;
            lastCell = nextCell ;
            
            emit(nextCell, NodeConst.nodeFirst, n) ;

            if ( ! moreTokens() )   // Error.
                break ;
        }
        // On exit, just after the RPARENS
        
        if ( lastCell == null )
            // Simple ()
            return NodeConst.nodeNil ;
        
        // Finish list.
        emit(lastCell, NodeConst.nodeRest, NodeConst.nodeNil) ;
        return listHead ;
    }
    
    // ---- Wrapping the peekIter.
    
    private final Token tokenRaw()
    {
        // Avoid repeating.
        if ( eof() ) return tokenEOF ;
        return peekIter.peek() ;
    }
    
    private final Token token()
    {
        return convert(tokenRaw()) ;
    }

    
    private Token convert(Token token)
    {
        if ( token.hasType(PREFIXED_NAME) )
        {
            String prefix = token.getImage() ;
            String suffix   = token.getImage2() ;
            String expansion = prologue.getPrefixMap().expand(prefix, suffix) ;
            if ( expansion == null )
                exceptionDirect("Undefined prefix: "+prefix, token.getLine(), token.getColumn()) ;
            token.setType(IRI) ;
            token.setImage(expansion) ;
            token.setImage2(null) ;
        } 
        else if ( token.hasType(IRI) )
        {
            token.setImage(prologue.getResolver().resolve(token.getImage()).toString()) ;
        }
        else if ( token.hasType(LITERAL_DT) )
        {
            Token t = token.getSubToken() ;
            t = convert(t) ;
            token.setSubToken(t) ;
        }
        return token ;
    }
    
    // Set when we get to EOF to record line/col of the EOF.
    private Token tokenEOF = null ;

    private boolean eof()
    {
        if ( tokenEOF != null )
            return true ;
        
        if ( ! moreTokens() )
        {
            tokenEOF = new Token(tokens.getLine(), tokens.getColumn()) ;
            return true ;
        }
        return false ;
    }

    private boolean moreTokens() 
    {
        return peekIter.hasNext() ;
    }
    
    // See triplesNode
    private boolean lookingAt(TokenType tokenType)
    {
        if ( eof() )
            return tokenType == EOF ;
        if ( tokenType == NODE )
            return tokenRaw().isNode() ;
//        if ( tokenType == KEYWORD )
//        {
//            String image = tokenRaw().getImage() ;
//            if ( image.equals(KW_TRUE) )
//                return true ;
//            if ( image.equals(KW_FALSE) )
//                return true ;
//            return false ; 
//        }
        // NB IRIs and PREFIXED_NAMEs
        return tokenRaw().hasType(tokenType) ;
    }
    
    private Token move()
    {
        if ( eof() )
        {
            if ( VERBOSE ) log.info("Move: EOF") ;
            return tokenEOF ;
        }
        
        Token t = peekIter.next() ;
        if ( VERBOSE ) log.info("Move: " + t) ;
        return t ;
    }
    
    private void expectOrEOF(String msg, TokenType tokenType)
    {
        // DOT or EOF
        if ( eof() )
            return ;
        expect(msg, tokenType) ;
    }
    
    private void expect(String msg, TokenType ttype)
    {
        if ( ! lookingAt(ttype) )
            exception(msg) ;
        move() ;
    }

    private void exception(String msg, Object... args)
    { 
        exceptionDirect(String.format(msg, args), tokenRaw().getLine(), tokenRaw().getColumn()) ;
    }

    private void exceptionDirect(String msg, long line, long col)
    { throw new ParseException(msg, line, col) ; }
    
    private void emit(Node subject, Node predicate, Node object)
    {
        if ( CHECKING )
        {
            if ( subject == null || ( ! subject.isURI() && ! subject.isBlank() ) )
                exception("Subject is not a URI or blank node") ;
            if ( predicate == null || ( ! predicate.isURI() ) )
                exception("Predicate not a URI") ;
            if ( object == null || ( ! object.isURI() && ! object.isBlank() && ! object.isLiteral() ) )
                exception("Object is not a URI, blank node or literal") ;
        }
        Triple t = new Triple(subject, predicate, object) ;
        if ( VERBOSE ) 
            log.info(PrintingSink.strForTriple(t)) ;
        sink.send(new Triple(subject, predicate, object)) ;
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */