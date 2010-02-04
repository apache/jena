/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import atlas.lib.InternalErrorException ;
import atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.riot.Checker ;
import com.hp.hpl.jena.riot.tokens.Token ;
import com.hp.hpl.jena.riot.tokens.TokenType ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** TriG language: http://www4.wiwiss.fu-berlin.de/bizer/TriG/
 *  Generalizations:
 *    Can have multiple default graph blocks.
 *    
 * 
 */
public class LangTriG extends LangTurtleBase<Quad>
{
    /*
     *  TriGDoc     ::=      ws* (statement ws*)*
        statement   ::=     directive ws* '.' | graph
        graph       ::=     graphName? ws* '='? ws* '{' ws* (triples ws* ('.' ws* triples ws*)* '.'? ws*)? '}' ws* '.'?
        graphName   ::=     resource
     */
    public LangTriG(String baseURI, Tokenizer tokens, 
                    Checker checker, 
                    Sink<Quad> sink) 
    {
        super(baseURI, tokens, checker, sink) ;
    }

    Node currentGraph = null ;
    
    @Override
    protected final void oneTopLevelElement()
    {
        // XXX BNode labels.
        // Per graph seen??
        oneNamedGraphBlock() ;
    }
    
    protected final void oneNamedGraphBlock()
    {
        // Directives are only between graphs.  Yipee.
        Node graphNode = null ; 
        Token token = peekToken() ;

        // <foo> = { ... }
        if ( token.isNode() )
        {
            graphNode = node() ;
            nextToken() ;
            token = peekToken() ;

            if ( graphNode.isURI() )
                currentGraph = graphNode ; 
            else
                exception("Not a legal graph name: "+graphNode) ;
        }
        else
            currentGraph = Quad.defaultGraphNodeGenerated ;

        // = is optional
        if ( token.getType() == TokenType.EQUALS )
        {
            // Skip.
            nextToken() ;
            token = peekToken() ;
        }
        
        if ( token.getType() != TokenType.LBRACE )
            exception("Expected start of graph: got %s", peekToken()) ;
        nextToken() ;
        
        // **** Turtle but no directives.
        
        while(true)
        {
            token = peekToken() ;
            if ( token.hasType(TokenType.RBRACE) )
                break ;
            // No - this has Turtle termination rules.
            // Assume this is fixed then ....
            
            // Unlike many operations in this parser suite 
            // this does not assume that we are definitel entering
            // this sttae throws an error if the first token 
            triplesSameSubject() ;
        }
        
        // **** Turtle.
        token = peekToken() ;
        if ( token.getType() != TokenType.RBRACE )
            exception("Expected end of graph: got %s", peekToken()) ;
        nextToken() ;
        currentGraph = null ;
    }

    @Override
    protected void emit(Node subject, Node predicate, Node object)
    {
        if ( currentGraph == null )
            throw new InternalErrorException("Not inside a graph block but trying to generate a quad") ;
        
        Node g = currentGraph ;
        if ( g ==  Quad.defaultGraphNodeGenerated )
            g = null ;
        Quad quad = new Quad(g, subject, predicate, object) ;
        sink.send(quad) ;
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