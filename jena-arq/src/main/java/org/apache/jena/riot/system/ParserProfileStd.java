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

package org.apache.jena.riot.system;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.iri.IRI;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.checker.CheckerIRI;
import org.apache.jena.riot.checker.CheckerLiterals;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.TokenType;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.FmtUtils;

/** 
 * {@link ParserProfileStd} uses a {@link FactoryRDF} to 
 * create items in the parsing process.
 */
public class ParserProfileStd implements ParserProfile
{
    private final FactoryRDF   factory;
    private final ErrorHandler errorHandler;
    private final Context      context;
    private       IRIResolver  resolver;
    private final PrefixMap    prefixMap;
    private final boolean      strictMode;
    private final boolean      checking;

    public ParserProfileStd(FactoryRDF factory, ErrorHandler errorHandler, 
                            IRIResolver resolver, PrefixMap prefixMap,
                            Context context, boolean checking, boolean strictMode) {
        this.factory = factory;
        this.errorHandler = errorHandler;
        this.resolver = resolver;
        this.prefixMap = prefixMap;
        this.context = context;
        this.checking = checking;
        this.strictMode = strictMode;
    }
    
    @Override
    public FactoryRDF getFactorRDF() {
        return factory;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public boolean isStrictMode() {
        return strictMode;
    }

    @Override
    public String resolveIRI(String uriStr, long line, long col) {
        return makeIRI(uriStr, line, col).toString();
    }

    @Override
    public void setIRIResolver(IRIResolver resolver) {
        this.resolver = resolver; 
    }

    @Override
    public IRI makeIRI(String uriStr, long line, long col) {
        IRI iri = resolver.resolveSilent(uriStr);
        // Some specific problems and specific error messages,.
        if ( uriStr.contains(" ") ) {
            // Specific check for spaces.
            errorHandler.warning("Bad IRI: <" + uriStr + "> Spaces are not legal in URIs/IRIs.", line, col);
            return iri;
        }

        if ( !checking )
            return iri;

        // At this point, IRI "errors" are warnings.
        // A tuned set of checking.
        CheckerIRI.iriViolations(iri, errorHandler, line, col);
        return iri;
    }

    /** Create a triple - this operation call {@link #checkTriple} if checking is enabled. */ 
    @Override
    public Triple createTriple(Node subject, Node predicate, Node object, long line, long col) {
        if ( checking )
            checkTriple(subject, predicate, object, line, col);
        return factory.createTriple(subject, predicate, object);
    }

    protected void checkTriple(Node subject, Node predicate, Node object, long line, long col) {
        if ( subject == null || (!subject.isURI() && !subject.isBlank()) ) {
            errorHandler.error("Subject is not a URI or blank node", line, col);
            throw new RiotException("Bad subject: " + subject);
        }
        if ( predicate == null || (!predicate.isURI()) ) {
            errorHandler.error("Predicate not a URI", line, col);
            throw new RiotException("Bad predicate: " + predicate);
        }
        if ( object == null || (!object.isURI() && !object.isBlank() && !object.isLiteral()) ) {
            errorHandler.error("Object is not a URI, blank node or literal", line, col);
            throw new RiotException("Bad object: " + object);
        }
    }

    /** Create a quad - this operation call {@link #checkTriple} if checking is enabled. */ 
    @Override
    public Quad createQuad(Node graph, Node subject, Node predicate, Node object, long line, long col) {
        if ( checking )
            checkQuad(graph, subject, predicate, object, line, col);
        return factory.createQuad(graph, subject, predicate, object);
    }

    protected void checkQuad(Node graph, Node subject, Node predicate, Node object, long line, long col) {
        // Allow blank nodes - syntax may restrict more.
        if ( graph != null && !graph.isURI() && !graph.isBlank() ) {
            errorHandler.error("Graph name is not a URI or blank node: " + FmtUtils.stringForNode(graph), line, col);
            throw new RiotException("Bad graph name: " + graph);
        }
        checkTriple(subject, predicate, object, line, col);
    }

    @Override
    public Node createURI(String x, long line, long col) {
        // Special cases that don't resolve.
        //   <_:....> is a blank node.
        //   <::...> is "don't touch" used for a fixed-up prefix name 
        if ( !RiotLib.isBNodeIRI(x) && !RiotLib.isPrefixIRI(x) )
            // Really is an URI!
            x = resolveIRI(x, line, col);
        return factory.createURI(x);
    }

    @Override
    public Node createTypedLiteral(String lexical, RDFDatatype datatype, long line, long col) {
        if ( checking )
            CheckerLiterals.checkLiteral(lexical, datatype, errorHandler, line, col);
        return factory.createTypedLiteral(lexical, datatype);
    }

    @Override
    public Node createLangLiteral(String lexical, String langTag, long line, long col) {
        if ( checking )
            CheckerLiterals.checkLiteral(lexical, langTag, errorHandler, line, col);
        return factory.createLangLiteral(lexical, langTag);
    }

    @Override
    public Node createStringLiteral(String lexical, long line, long col) {
        // No checks
        return factory.createStringLiteral(lexical);
    }

    /** Special token forms */
    @Override
    public Node createNodeFromToken(Node scope, Token token, long line, long col) {
        // OFF - Don't produce Node.ANY by default.
        if ( false && token.getType() == TokenType.KEYWORD ) {
            if ( Token.ImageANY.equals(token.getImage()) )
                return Node.ANY;
        }
        return null;
    }

    @Override
    public Node createBlankNode(Node scope, String label, long line, long col) {
        // No checks
        return factory.createBlankNode(label);
    }

    @Override
    public Node createBlankNode(Node scope, long line, long col) {
        // No checks
        return factory.createBlankNode();
    }

    @Override
    public Node create(Node currentGraph, Token token) {
        // Dispatches to the underlying ParserFactory operation
        long line = token.getLine();
        long col = token.getColumn();
        String str = token.getImage();
        switch (token.getType()) {
            case BNODE :
                return createBlankNode(currentGraph, str, line, col);
            case IRI :
                return createURI(str, line, col);
            case PREFIXED_NAME : {
                String prefix = str;
                String suffix = token.getImage2();
                String expansion = expandPrefixedName(prefix, suffix, token);
                return createURI(expansion, line, col);
            }
            case DECIMAL :
                return createTypedLiteral(str, XSDDatatype.XSDdecimal, line, col);
            case DOUBLE :
                return createTypedLiteral(str, XSDDatatype.XSDdouble, line, col);
            case INTEGER :
                return createTypedLiteral(str, XSDDatatype.XSDinteger, line, col);
            case LITERAL_DT : {
                Token tokenDT = token.getSubToken2();
                String uriStr;

                switch (tokenDT.getType()) {
                    case IRI :
                        uriStr = tokenDT.getImage();
                        break;
                    case PREFIXED_NAME : {
                        String prefix = tokenDT.getImage();
                        String suffix = tokenDT.getImage2();
                        uriStr = expandPrefixedName(prefix, suffix, tokenDT);
                        break;
                    }
                    default :
                        throw new RiotException("Expected IRI for datatype: " + token);
                }

                uriStr = resolveIRI(uriStr, tokenDT.getLine(), tokenDT.getColumn());
                RDFDatatype dt = NodeFactory.getType(uriStr);
                return createTypedLiteral(str, dt, line, col);
            }

            case LITERAL_LANG :
                return createLangLiteral(str, token.getImage2(), line, col);

            case STRING :
                return createStringLiteral(str, line, col);
                
            case BOOLEAN :
                return createTypedLiteral(str, XSDDatatype.XSDboolean, line, col);
                
            default : {
                Node x = createNodeFromToken(currentGraph, token, line, col);
                if ( x != null )
                    return x;
                errorHandler.fatal("Not a valid token for an RDF term: " + token, line, col);
                return null;
            }
        }
    }

    @Override
    public PrefixMap getPrefixMap() {
        return prefixMap;
    }

    private String expandPrefixedName(String prefix, String localPart, Token token) {
        String expansion = prefixMap.expand(prefix, localPart);
        if ( expansion == null ) {
            if ( ARQ.isTrue(ARQ.fixupUndefinedPrefixes) )
                return RiotLib.fixupPrefixIRI(prefix, localPart);
            errorHandler.fatal("Undefined prefix: " + prefix, token.getLine(), token.getColumn());
        }
        return expansion;
    }
}