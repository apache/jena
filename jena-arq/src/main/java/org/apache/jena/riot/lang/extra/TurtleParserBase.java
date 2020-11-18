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

package org.apache.jena.riot.lang.extra;

import org.apache.jena.atlas.AtlasException;
import org.apache.jena.atlas.lib.EscapeStr;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.iri.ViolationCodes;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.lang.ParserBase;
import org.apache.jena.ttl.JenaURIException;
import org.apache.jena.vocabulary.RDF;

@SuppressWarnings("hiding")
public class TurtleParserBase /*temporary*/ extends ParserBase {

    protected final Node XSD_TRUE       = NodeConst.nodeTrue ;
    protected final Node XSD_FALSE      = NodeConst.nodeFalse ;

    protected final Node nRDFtype       = NodeConst.nodeRDFType ;

    protected final Node nRDFnil        = NodeConst.nodeNil ;
    protected final Node nRDFfirst      = NodeConst.nodeFirst ;
    protected final Node nRDFrest       = NodeConst.nodeRest ;

    protected final Node nRDFsubject    = RDF.Nodes.subject ;
    protected final Node nRDFpredicate  = RDF.Nodes.predicate ;
    protected final Node nRDFobject     = RDF.Nodes.object ;

    private StreamRDF stream;
    private ParserProfile profile;

    private static void setErrorWarning(IRIFactory factory, int code, boolean isError, boolean isWarning) {
        factory.setIsWarning(code, isWarning);
        factory.setIsError(code, isError);
    }

    public TurtleParserBase() {
        setPrologue(new Prologue());

        IRIFactory iriFactory = new IRIFactory(IRIFactory.iriImplementation());
//        // These two are from IRIFactory.iriImplementation() ...
//        iriFactory.useSpecificationIRI(true);
//        iriFactory.useSchemeSpecificRules("*", true);
//
//        // Allow relative references for file: URLs.
//        iriFactory.setSameSchemeRelativeReferences("file");
//
        // DOUBLE_WHITESPACE is misnamed - it applies to last character being a space as well (IRI bug?)
        //setErrorWarning(iriFactory, ViolationCodes.DOUBLE_WHITESPACE,  true, true);
        setErrorWarning(iriFactory, ViolationCodes.UNWISE_CHARACTER,   true, true);
        setErrorWarning(iriFactory, ViolationCodes.WHITESPACE,         true, true);

        setErrorWarning(iriFactory, ViolationCodes.UNREGISTERED_IANA_SCHEME, false, false);
        setErrorWarning(iriFactory, ViolationCodes.UNDEFINED_UNICODE_CHARACTER, false, false);

        IRI iri = iriFactory.create(IRIResolver.chooseBaseURI());
        IRIResolver resolver = IRIResolver.create(iri);
        getPrologue().setResolver(IRIResolver.create(iri));
    }

    // Calls back to ParserBase - to separate, replace these
    @Override
    public void setPrologue(Prologue prologue) { super.setPrologue(prologue); }
    @Override
    public Prologue getPrologue() { return super.getPrologue(); }

    @Override
    protected String fixupPrefix(String image, int line, int column) {
        return super.fixupPrefix(image, line, column);
    }

    @Override
    protected Node createNode(String iri) {
        return super.createNode(iri);
    }

    @Override
    protected Node createBNode(int line, int column) {
        return super.createBNode(line, column);
    }

    @Override
    protected Node createBNode(String label, int line, int column) {
        return super.createBNode(label, line, column);
    }
    @Override
    protected Node createListNode(int line, int column) {
        return super.createListNode(line, column);
    }

    @Override
    protected Node createLiteral(String lex, String lang, String uri) {
        return super.createLiteral(lex, lang, uri);
    }

    public static String stripChars(String image, int n) {
        return ParserBase.stripChars(image, n);
    }

    @Override
    protected Node createLiteralInteger(String lexicalForm) {
        return super.createLiteralInteger(lexicalForm);
    }

    @Override
    protected Node createLiteralDecimal(String lexicalForm) {
        return super.createLiteralDecimal(lexicalForm);
    }

    @Override
    protected Node createLiteralDouble(String lexicalForm) {
        return super.createLiteralDouble(lexicalForm);
    }

    public static String unescapeStr(String lex, int line, int column) {
        return ParserBase.unescapeStr(lex, line, column);
    }

    public static String stripQuotes3(String string) {
        return ParserBase.stripQuotes3(string);
    }

    public static String stripQuotes(String string) {
        return ParserBase.stripQuotes(string);
    }

    @Override
    protected String resolvePName(String pname, int line, int column) {
        return super.resolvePName(pname, line, column);
    }

    @Override
    protected String resolveQuotedIRI(String iriStr, int line, int column) {
        iriStr = stripQuotes(iriStr);
        iriStr = unescapeIRI(iriStr);
        try {
            IRIResolver resolver = getPrologue().getResolver();
            IRI iri = resolver.resolve(iriStr) ;
//            IRI iri = resolver.resolveSilent(iriStr) ;
//            if ( true )
//                CheckerIRI.iriViolations(iri, profile.getErrorHandler(), line, column) ;
//            iriStr = iri.toString() ;
        }
        catch (JenaURIException ex) {
            throw new RiotParseException(ex.getMessage(), line, column) ;
        }
        // IRI unwise characters are allowed sometimes but these are really, really
        // unwise (inserted by \ u escaping to by pass syntax).
        if ( iriStr.contains("<") || iriStr.contains(">") )
            throw new RiotParseException("Illegal character '<' or '>' in IRI: '"+iriStr+"'", line, column);
        return getPrologue().getResolver().resolveToString(iriStr);
    }

    public void setProfile(ParserProfile profile) {
        this.profile = profile;
    }

    public void setDest(StreamRDF stream) {
        this.stream = stream;
    }

    @Override
    public void setBase(String iri, int line, int column) {
        super.setBase(iri, line, column);
        stream.base(iri);
    }

    @Override
    public void setPrefix(String prefix, String iri, int line, int column) {
        prefix = fixupPrefix(prefix, line, column);
        super.setPrefix(prefix, iri, line, column);
        stream.prefix(prefix, iri);
    }

    public void emitTriple(int line, int column, Node s, Node p, Node o) {
        stream.triple(Triple.create(s, p, o));
    }

    private String unescapeIRI(String iriStr) {
        try {
            return EscapeStr.unescape(iriStr, '\\', true);
        } catch (AtlasException ex) {
            throw new RiotException(ex.getMessage());
        }
    }

    public void checkIRI(String iriStr) {
//        IRI iri = getPrologue().getResolver().resolveSilent(iriStr) ;
//        if ( true )
//            CheckerIRI.iriViolations(iri, errorHandler, line, column) ;
//        iriStr = iri.toString() ;
    }

    public void listStart(int line, int column) {};
    public void listTriple(int line, int column, Node s, Node p , Node o) { emitTriple(line, column, s, p, o); }
    public void listFinish(int line, int column) {};

}
