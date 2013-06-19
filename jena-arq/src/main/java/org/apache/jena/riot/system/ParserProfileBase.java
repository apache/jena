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

import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.SysRIOT ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.riot.tokens.Token ;
import org.apache.jena.riot.tokens.TokenType ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Quad ;

/**
 * Basic profile of things, with key operations based on a simple use of the
 * parse elements into Nodes
 */
public class ParserProfileBase implements ParserProfile {
    protected ErrorHandler errorHandler ;
    protected Prologue     prologue ;
    protected LabelToNode  labelMapping ;
    protected boolean      strictMode = SysRIOT.strictMode ;

    public ParserProfileBase(Prologue prologue, ErrorHandler errorHandler) {
        this(prologue, errorHandler, SyntaxLabels.createLabelToNode()) ;
    }

    public ParserProfileBase(Prologue prologue, ErrorHandler errorHandler, LabelToNode labelMapping) {
        this.prologue = prologue ;
        this.errorHandler = errorHandler ;
        this.labelMapping = labelMapping ;
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
    public LabelToNode getLabelToNode() {
        return labelMapping ;
    }

    @Override
    public void setLabelToNode(LabelToNode mapper) {
        labelMapping = mapper ;
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
        return new Quad(g, s, p, o) ;
    }

    @Override
    public Triple createTriple(Node s, Node p, Node o, long line, long col) {
        return new Triple(s, p, o) ;
    }

    @Override
    public Node createURI(String uriStr, long line, long col) {
        return RiotLib.createIRIorBNode(uriStr) ;
    }

    @Override
    public Node createBlankNode(Node scope, String label, long line, long col) {
        return labelMapping.get(scope, label) ;
    }

    @Override
    public Node createBlankNode(Node scope, long line, long col) {
        return labelMapping.create() ;
    }

    @Override
    public Node createTypedLiteral(String lexical, RDFDatatype dt, long line, long col) {
        return NodeFactory.createLiteral(lexical, null, dt) ;
    }

    @Override
    public Node createLangLiteral(String lexical, String langTag, long line, long col) {
        return NodeFactory.createLiteral(lexical, langTag, null) ;
    }

    @Override
    public Node createStringLiteral(String lexical, long line, long col) {
        return NodeFactory.createLiteral(lexical) ;
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
        // Dispatches to the underlying operation
        long line = token.getLine() ;
        long col = token.getColumn() ;
        String str = token.getImage() ;
        switch (token.getType()) {
            case BNODE :
                return createBlankNode(currentGraph, str, line, col) ;
            case IRI :
                return createURI(str, line, col) ;
            case PREFIXED_NAME : {
                String prefix = str ;
                String suffix = token.getImage2() ;
                String expansion = expandPrefixedName(prefix, suffix, token) ;
                return createURI(expansion, line, col) ;
            }
            case DECIMAL :
                return createTypedLiteral(str, XSDDatatype.XSDdecimal, line, col) ;
            case DOUBLE :
                return createTypedLiteral(str, XSDDatatype.XSDdouble, line, col) ;
            case INTEGER :
                return createTypedLiteral(str, XSDDatatype.XSDinteger, line, col) ;
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
                        uriStr = expandPrefixedName(prefix, suffix, tokenDT) ;
                        break ;
                    }
                    default :
                        throw new RiotException("Expected IRI for datatype: " + token) ;
                }

                uriStr = resolveIRI(uriStr, tokenDT.getLine(), tokenDT.getColumn()) ;
                RDFDatatype dt = NodeFactory.getType(uriStr) ;
                return createTypedLiteral(str, dt, line, col) ;
            }

            case LITERAL_LANG :
                return createLangLiteral(str, token.getImage2(), line, col) ;

            case STRING :
            case STRING1 :
            case STRING2 :
            case LONG_STRING1 :
            case LONG_STRING2 :
                return createStringLiteral(str, line, col) ;
            default : {
                Node x = createNodeFromToken(currentGraph, token, line, col) ;
                if (x != null)
                    return x ;
                errorHandler.fatal("Not a valid token for an RDF term: " + token, line, col) ;
                return null ;
            }
        }
    }

    private String expandPrefixedName(String prefix, String localPart, Token token) {
        String expansion = prologue.getPrefixMap().expand(prefix, localPart) ;
        if (expansion == null)
            errorHandler.fatal("Undefined prefix: " + prefix, token.getLine(), token.getColumn()) ;
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
