/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.system;

import org.openjena.riot.ErrorHandler ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.lang.LabelToNode ;
import org.openjena.riot.tokens.Token ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.iri.IRI ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Basic profile of things, with key operations based on a simple
 *  use of the parse elements into Nodes (e.g. no URI resolution). 
 */
public class ParserProfileBase implements ParserProfile
{
    protected ErrorHandler errorHandler ;
    protected final Prologue prologue ;
    protected final LabelToNode labelMapping ;

    public ParserProfileBase(Prologue prologue, ErrorHandler errorHandler)
    { 
        this.prologue = prologue ;
        this.errorHandler = errorHandler ; 
        this.labelMapping = SyntaxLabels.createLabelToNode() ;
    }
    
    //@Override
    public ErrorHandler getHandler()    { return errorHandler ; }

    //@Override
    public void setHandler(ErrorHandler handler) { errorHandler = handler ; }

    //@Override
    public Prologue getPrologue()       { return prologue ; }      
    
    //@Override
    public LabelToNode getLabelToNode() { return labelMapping ; }

    //@Override
    public String resolveIRI(String uriStr, long line, long col)
    {
        return makeIRI(uriStr, line, col).toString() ;
    }
    
    //@Override
    public IRI makeIRI(String uriStr, long line, long col)
    {
        IRI iri = prologue.getResolver().resolve(uriStr) ;
        return iri ;
    }

    //@Override
    public Quad createQuad(Node g, Node s, Node p, Node o, long line, long col)
    {
        return new Quad(g,s,p,o) ;
    }

    //@Override
    public Triple createTriple(Node s, Node p, Node o, long line, long col)
    {
        return new Triple(s,p,o) ;
    }

    //@Override
    public Node createURI(String uriStr, long line, long col)
    {
        return Node.createURI(uriStr) ;
    }

    //@Override
    public Node createBlankNode(Node scope, String label, long line, long col)
    {
        return labelMapping.get(scope, label) ;
    }

//    //@Override
//    public Node createTypedLiteral(String lexical, String datatype, long line, long col)
//    {
//        RDFDatatype dt = Node.getType(datatype) ;
//        return createTypedLiteral(lexical, dt, line, col) ;
//    }
    
    //@Override
    public Node createTypedLiteral(String lexical, RDFDatatype dt, long line, long col)
    {
        return Node.createLiteral(lexical, null, dt)  ;
    }

    //@Override
    public Node createLangLiteral(String lexical, String langTag, long line, long col)
    {
        return Node.createLiteral(lexical, langTag, null)  ;
    }

    //@Override
    public Node createPlainLiteral(String lexical, long line, long col)
    {
        return Node.createLiteral(lexical) ;
    }

    //@Override
    public Node create(Node currentGraph, Token token)
    {
        // Dispatches to the underlying operation
        long line = token.getLine() ;
        long col = token.getColumn() ;
        String str = token.getImage() ;
        switch(token.getType())
        {
            case BNODE:         return createBlankNode(currentGraph, str, line, col) ;
            case IRI:           return createURI(str, line, col) ;
            case PREFIXED_NAME:
            {
                String prefix = str ;
                String suffix   = token.getImage2() ;
                String expansion = expandPrefixedName(prefix, suffix, token) ;
                return createURI(expansion, line, col) ;
            }
            case DECIMAL :
                return createTypedLiteral(str, XSDDatatype.XSDdecimal, line, col) ;
            case DOUBLE :
                return createTypedLiteral(str, XSDDatatype.XSDdouble, line, col) ;
            case INTEGER:
                return createTypedLiteral(str, XSDDatatype.XSDinteger, line, col) ;
            case LITERAL_DT :
            {
                
                Token tokenDT = token.getSubToken() ;
                String uriStr ;
                
                switch(tokenDT.getType())
                {
                    case IRI:               uriStr = tokenDT.getImage() ; break ;
                    case PREFIXED_NAME:
                    {
                        String prefix = tokenDT.getImage() ;
                        String suffix   = tokenDT.getImage2() ;
                        uriStr = expandPrefixedName(prefix, suffix, tokenDT) ;
                        break ;
                    }
                    default:
                        throw new RiotException("Expected IRI for datatype: "+token) ;
                }
                
                uriStr = resolveIRI(uriStr, tokenDT.getLine(), tokenDT.getColumn()) ;
                RDFDatatype dt = Node.getType(uriStr) ;
                return createTypedLiteral(str, dt, line, col) ;
            }
            
            case LITERAL_LANG : 
                return createLangLiteral(str, token.getImage2(), line, col)  ;
                
            case STRING:                
            case STRING1:
            case STRING2:
            case LONG_STRING1:
            case LONG_STRING2:
                return createPlainLiteral(str, line, col) ;
            // XXX Centralize exceptions
            default: 
                errorHandler.fatal("Not a valid token for an RDF term", line , col) ;
                return null ;
        }
    }
    
    private String expandPrefixedName(String prefix, String localPart, Token token)
    {
        String expansion = prologue.getPrefixMap().expand(prefix, localPart) ;
        if ( expansion == null )
            errorHandler.fatal("Undefined prefix: "+prefix, token.getLine(), token.getColumn()) ;
        return expansion ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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