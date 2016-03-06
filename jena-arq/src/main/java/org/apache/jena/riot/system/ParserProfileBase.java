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

package org.apache.jena.riot.system ;

import java.util.Objects ;

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.SysRIOT ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;
import org.apache.jena.sparql.core.Quad ;

/**
 * Basic profile of things, with key operations based on a simple use of the
 * parse elements into Nodes
 */
public class ParserProfileBase implements ParserProfile {
    protected ErrorHandler errorHandler ;
    protected Prologue     prologue ;
    protected boolean      strictMode = SysRIOT.isStrictMode() ;
    protected FactoryRDF   factory ;

    public ParserProfileBase(Prologue prologue, ErrorHandler errorHandler) {
        this(prologue, errorHandler, RiotLib.factoryRDF()) ;
    }

    public ParserProfileBase(Prologue prologue, ErrorHandler errorHandler, FactoryRDF factory) {
        Objects.requireNonNull(prologue) ;
        Objects.requireNonNull(errorHandler) ;
        Objects.requireNonNull(factory) ;
        this.prologue = prologue ;
        this.errorHandler = errorHandler ;
        this.factory = factory ;
    }

    @Override
    public ErrorHandler getHandler() {
        return errorHandler ;
    }

    @Override
    public void setHandler(ErrorHandler handler) {
        errorHandler = handler ;
    }

    @Override
    public Prologue getPrologue() {
        return prologue ;
    }

    @Override
    public void setPrologue(Prologue p) {
        prologue = p ;
    }

    @Override
    public FactoryRDF getFactoryRDF() {
        return factory;
    }

    @Override
    public void setFactoryRDF(FactoryRDF factory) {
        this.factory = factory;
    }
   
    @Override
    public String resolveIRI(String uriStr, long line, long col) {
        return prologue.getResolver().resolveToString(uriStr) ;
    }

    @Override
    public IRI makeIRI(String uriStr, long line, long col) {
        return prologue.getResolver().resolve(uriStr) ;
    }

    @Override
    public Quad createQuad(Node g, Node s, Node p, Node o, long line, long col) {
        return factory.createQuad(g, s, p, o);
    }

    @Override
    public Triple createTriple(Node s, Node p, Node o, long line, long col) {
        return factory.createTriple(s, p, o);
    }

    @Override
    public Node createURI(String uriStr, long line, long col) {
        return factory.createURI(uriStr);
    }

    @Override
    public Node createBlankNode(Node scope, String label, long line, long col) {
        return factory.createBlankNode(label);
    }

    @Override
    public Node createBlankNode(Node scope, long line, long col) {
        return factory.createBlankNode();
    }

    @Override
    public Node createTypedLiteral(String lexical, RDFDatatype dt, long line, long col) {
        return factory.createTypedLiteral(lexical, dt);
    }

    @Override
    public Node createLangLiteral(String lexical, String langTag, long line, long col) {
        return factory.createLangLiteral(lexical, langTag);
    }

    @Override
    public Node createStringLiteral(String lexical, long line, long col) {
        return factory.createStringLiteral(lexical);
    }
  
    /** Special token forms */
    @Override
    public Node createNodeFromToken(Node scope, Token token, long line, long col) {
        // OFF - Don't produce Node.ANY by default.
        if (false && token.getType() == TokenType.KEYWORD) {
            if (Token.ImageANY.equals(token.getImage()))
                return Node.ANY ;
        }
        return null ;
    }

    @Override
    public Node create(Node currentGraph, Token token) {
        return create(this, currentGraph, token) ;
    }
        
    private static Node create(ParserProfile pp, Node currentGraph, Token token) {
        // Dispatches to the underlying ParserProfile operation
        long line = token.getLine() ;
        long col = token.getColumn() ;
        String str = token.getImage() ;
        switch (token.getType()) {
            case BNODE :
                return pp.createBlankNode(currentGraph, str, line, col) ;
            case IRI :
                return pp.createURI(str, line, col) ;
            case PREFIXED_NAME : {
                String prefix = str ;
                String suffix = token.getImage2() ;
                String expansion = expandPrefixedName(pp, prefix, suffix, token) ;
                return pp.createURI(expansion, line, col) ;
            }
            case DECIMAL :
                return pp.createTypedLiteral(str, XSDDatatype.XSDdecimal, line, col) ;
            case DOUBLE :
                return pp.createTypedLiteral(str, XSDDatatype.XSDdouble, line, col) ;
            case INTEGER :
                return pp.createTypedLiteral(str, XSDDatatype.XSDinteger, line, col) ;
            case LITERAL_DT : {
                Token tokenDT = token.getSubToken2() ;
                String uriStr ;

                switch (tokenDT.getType()) {
                    case IRI :
                        uriStr = tokenDT.getImage() ;
                        break ;
                    case PREFIXED_NAME : {
                        String prefix = tokenDT.getImage() ;
                        String suffix = tokenDT.getImage2() ;
                        uriStr = expandPrefixedName(pp, prefix, suffix, tokenDT) ;
                        break ;
                    }
                    default :
                        throw new RiotException("Expected IRI for datatype: " + token) ;
                }

                uriStr = pp.resolveIRI(uriStr, tokenDT.getLine(), tokenDT.getColumn()) ;
                RDFDatatype dt = NodeFactory.getType(uriStr) ;
                return pp.createTypedLiteral(str, dt, line, col) ;
            }

            case LITERAL_LANG :
                return pp.createLangLiteral(str, token.getImage2(), line, col) ;

            case STRING :
            case STRING1 :
            case STRING2 :
            case LONG_STRING1 :
            case LONG_STRING2 :
                return pp.createStringLiteral(str, line, col) ;
            default : {
                Node x = pp.createNodeFromToken(currentGraph, token, line, col) ;
                if (x != null)
                    return x ;
                pp.getHandler().fatal("Not a valid token for an RDF term: " + token, line, col) ;
                return null ;
            }
        }
    }

    private static String expandPrefixedName(ParserProfile pp, String prefix, String localPart, Token token) {
        String expansion = pp.getPrologue().getPrefixMap().expand(prefix, localPart) ;
        if (expansion == null)
            pp.getHandler().fatal("Undefined prefix: " + prefix, token.getLine(), token.getColumn()) ;
        return expansion ;
    }

    @Override
    public boolean isStrictMode() {
        return strictMode ;
    }

    @Override
    public void setStrictMode(boolean mode) {
        strictMode = mode ;
    }

}
