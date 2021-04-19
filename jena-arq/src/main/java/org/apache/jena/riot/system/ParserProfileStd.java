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

import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.*;
import org.apache.jena.iri.IRI;
import org.apache.jena.irix.*;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.RiotException;
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
    private       IRIxResolver resolver;
    private final PrefixMap    prefixMap;
    private final boolean      strictMode;
    private final boolean      checking;
    private static int DftCacheSize = 500;
    private final Cache<String, IRI> iriCache;

    private boolean allowNodeExtentions;

    public ParserProfileStd(FactoryRDF factory, ErrorHandler errorHandler,
                            IRIxResolver resolver, PrefixMap prefixMap,
                            Context context, boolean checking, boolean strictMode) {
        this.factory = factory;
        this.errorHandler = errorHandler;
        this.resolver = resolver;
        this.prefixMap = prefixMap;
        this.context = context;
        this.checking = checking;
        this.iriCache = checking ? CacheFactory.createCache(DftCacheSize) : null;
        this.strictMode = strictMode;
        this.allowNodeExtentions = true; //(context.isTrue(RIOT.ALLOW_NODE_EXT)) ;
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
        return internalMakeIRI(uriStr, line, col).toString();
    }

    @Override
    public void setBaseIRI(String baseIRIstr) {
        IRIx newBase = resolver.resolve(baseIRIstr);
        this.resolver = resolver.resetBase(newBase);
    }

    private IRIx internalMakeIRI(String uriStr, long line, long col) {
        if ( uriStr.contains(" ") ) {
            // Specific check for spaces.
            errorHandler.warning("Bad IRI: <" + uriStr + "> Spaces are not legal in URIs/IRIs.", line, col);
            return IRIx.createAny(uriStr);
        }

        try {
            IRIx iri = resolver.resolve(uriStr);
            if ( checking )
                doChecking(iri, iri.str(), line, col);
            return iri;
        } catch (IRIException ex) {
            // This should only be errors and the errorHandler may be set to "don't continue".
            if ( SystemIRIx.getProvider() instanceof IRIProviderJenaIRI )
                // Checking did this error/warning.
                // Puts the IRI in the message.
                errorHandler.error("Bad IRI: "+ex.getMessage(), line, col);
            else
                // Does not put the IRI in the message.
                errorHandler.error("Bad IRI: <" + uriStr + "> : "+ex.getMessage(), line, col);
//            // Error handler let it pass, but should have printed something so don't out duplicate messages.
//            if ( checking )
//                doChecking(null, uriStr, line, col);
            return IRIx.createAny(uriStr);
        }
    }

    private void doChecking(IRIx irix, String uriStr, long line, long col) {
        IRI iri;
        if ( irix instanceof IRIProviderJenaIRI.IRIxJena )
            iri = (IRI)irix.getImpl();
        else
            iri = iriCache.getOrFill(uriStr, ()->SetupJenaIRI.iriCheckerFactory().create(uriStr));
        Checker.iriViolations(iri, errorHandler, false, true, line, col);
    }

    /** Create a triple - this operation call {@link #checkTriple} if checking is enabled. */
    @Override
    public Triple createTriple(Node subject, Node predicate, Node object, long line, long col) {
        if ( checking )
            checkTriple(subject, predicate, object, line, col);
        return factory.createTriple(subject, predicate, object);
    }

    private boolean allowSpecialNode(Node node) {
        return allowNodeExtentions && node instanceof Node_Triple;
    }

    protected void checkTriple(Node subject, Node predicate, Node object, long line, long col) {
        if ( subject == null || (!subject.isURI() && !subject.isBlank()) ) {
            if ( ! allowSpecialNode(subject) ) {
                errorHandler.error("Subject is not a URI or blank node", line, col);
                throw new RiotException("Bad subject: " + subject);
            }
        }
        if ( predicate == null || (!predicate.isURI()) ) {
            errorHandler.error("Predicate not a URI", line, col);
            throw new RiotException("Bad predicate: " + predicate);
        }
        if ( object == null || (!object.isURI() && !object.isBlank() && !object.isLiteral()) ) {
            if ( ! allowSpecialNode(object) ) {
                errorHandler.error("Object is not a URI, blank node or literal", line, col);
                throw new RiotException("Bad object: " + object);
            }
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
            Checker.checkLiteral(lexical, datatype, errorHandler, line, col);
        return factory.createTypedLiteral(lexical, datatype);
    }

    @Override
    public Node createLangLiteral(String lexical, String langTag, long line, long col) {
        if ( checking )
            Checker.checkLiteral(lexical, langTag, errorHandler, line, col);
        return factory.createLangLiteral(lexical, langTag);
    }

    @Override
    public Node createStringLiteral(String lexical, long line, long col) {
        // No checks
        return factory.createStringLiteral(lexical);
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
    public Node createTripleNode(Node subject, Node predicate, Node object, long line, long col) {
        return NodeFactory.createTripleNode(subject, predicate, object);
    }

    @Override
    public Node createTripleNode(Triple triple, long line, long col) {
        return NodeFactory.createTripleNode(triple);
    }

    @Override
    public Node createGraphNode(Graph graph, long line, long col) {
        return NodeFactory.createGraphNode(graph);
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
