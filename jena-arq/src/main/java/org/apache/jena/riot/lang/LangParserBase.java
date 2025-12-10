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

import org.apache.jena.atlas.AtlasException;
import org.apache.jena.atlas.lib.EscapeStr;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.lang.QueryParserBase;
import org.apache.jena.vocabulary.RDF;

/**
 * Base for JavaCC parser of RDF languages.
 * See {@link QueryParserBase} for the base class for SPARQL.
 */
public class LangParserBase {

    protected final Node XSD_TRUE       = NodeConst.nodeTrue ;
    protected final Node XSD_FALSE      = NodeConst.nodeFalse ;

    protected final Node nRDFtype       = NodeConst.nodeRDFType ;

    protected final Node nRDFnil        = NodeConst.nodeNil ;
    protected final Node nRDFfirst      = NodeConst.nodeFirst ;
    protected final Node nRDFrest       = NodeConst.nodeRest ;

    protected final Node nRDFsubject    = RDF.Nodes.subject ;
    protected final Node nRDFpredicate  = RDF.Nodes.predicate ;
    protected final Node nRDFobject     = RDF.Nodes.object ;

    protected final Node nRDFreifies = RDF.Nodes.reifies;

    protected StreamRDF stream;
    protected ParserProfile profile;

    public LangParserBase() { }

    // These are essential calls unless the parser takes over the functions.
    // They can't easily be in the constructor because this class is inherited
    // by the JavaCC generated parser.
    public void setProfile(ParserProfile profile) {
        this.profile = profile;
    }

    public void setDest(StreamRDF stream) {
        this.stream = stream;
    }

    // ----

    /**
     * Standardize a prefix - prefixes do not include the ":".
     */
    protected String canonicalPrefix(String prefix, int line, int column) {
        if ( prefix.endsWith(":") )
            prefix = prefix.substring(0, prefix.length() - 1) ;
        return prefix ;
    }

    protected Node createURI(String iriStr, int line, int column) {
        checkRDFString(iriStr, line, column);
        return profile.createURI(iriStr, line, column);
    }

    protected Node createBNode(int line, int column) {
        return profile.createBlankNode(null, line, column);
    }

    protected Node createBNode(String label, int line, int column) {
        return profile.createBlankNode(null, label, line, column);
    }

    protected Node createListNode(int line, int column) {
        return createBNode(line, column);
    }

    /**
     * Apply any checks for "RDF String" to a string that has already had escape processing applied.
     * An RDF String is a sequence of codepoints in the range U+0000 to U+10FFFF, excluding surrogates.
     * Because this is java, we test for no non-paired surrogates.
     * A surrogate pair is high-low.
     */
    protected static void checkRDFString(String string, int line, int column) {
        for ( int i = 0 ; i < string.length() ; i++ ) {
            // Not "codePointAt" which does surrogate processing.
            char ch = string.charAt(i);

            if ( ! Character.isValidCodePoint(ch) )
                throw new RiotParseException(String.format("Illegal code point in \\U sequence value: 0x%08X", ch), line, column);

            // Check surrogate pairs are pairs.
            if ( Character.isHighSurrogate(ch) ) {
                i++;
                if ( i == string.length() )
                    throw new RiotParseException("Bad surrogate pair (end of string)", line, column);
                char ch1 = string.charAt(i);
                if ( ! Character.isLowSurrogate(ch1) ) {
                    throw new RiotParseException("Bad surrogate pair (high surrogate not followed by low surrogate)", line, column);
                }
            } else if ( Character.isLowSurrogate(ch) ) {
                throw new RiotParseException("Bad surrogate pair (low surrogate without high surrogate)", line, column);
            }
        }
    }

    protected Node createLiteral(String lexicalForm, String langTag, String datatypeURI, int line, int column) {
        Node n = null ;
        // Can't have type and lang tag in parsing.
        if ( datatypeURI != null ) {
            RDFDatatype dType = TypeMapper.getInstance().getSafeTypeByName(datatypeURI) ;
            n = profile.createTypedLiteral(lexicalForm, dType, line, column) ;
        } else if ( langTag != null && !langTag.isEmpty() )
            n = profile.createLangLiteral(lexicalForm, langTag, line, column) ;
        else
            n = profile.createStringLiteral(lexicalForm, line, column) ;
        return n ;
    }

    protected Node createTripleTerm(Node s, Node p, Node o, int line, int column) {
        return profile.createTripleTerm(s, p, o, line, column);
    }

    protected Node createLiteralInteger(String lexicalForm, int line, int column) {
        return profile.createTypedLiteral(lexicalForm, XSDDatatype.XSDinteger, line, column);
    }

    protected Node createLiteralDecimal(String lexicalForm, int line, int column) {
        return profile.createTypedLiteral(lexicalForm, XSDDatatype.XSDdecimal, line, column);
    }

    protected Node createLiteralDouble(String lexicalForm, int line, int column) {
        return profile.createTypedLiteral(lexicalForm, XSDDatatype.XSDdouble, line, column);
    }

    protected Var createVariable(String varName, int line, int column) {
        varName = varName.substring(1) ; // Drop the marker
        return Var.alloc(varName) ;
    }

    protected String resolvePName(String pname, int line, int column) {
        int idx = pname.indexOf(':');
        String prefix = pname.substring(0, idx);
        String localPart = pname.substring(idx+1);
        localPart = LangParserLib.unescapePName(localPart, line, column);
        String expansion = profile.getPrefixMap().expand(prefix, localPart);
        if ( expansion == null ) {
            if ( ARQ.isTrue(ARQ.fixupUndefinedPrefixes) )
                return RiotLib.fixupPrefixIRI(prefix, localPart);
            profile.getErrorHandler().fatal("Undefined prefix: " + prefix, line, column);
        }
        return expansion;
    }

    protected String resolveQuotedIRI(String iriStr, int line, int column) {
        iriStr = LangParserLib.stripQuotes(iriStr);
        iriStr = unescapeIRI(iriStr);
        checkRDFString(iriStr, line, column);
        // Check
        if ( iriStr.contains("<") || iriStr.contains(">") )
            throw new RiotParseException("Illegal character '<' or '>' in IRI: '"+iriStr+"'", line, column);
        return profile.resolveIRI(iriStr, line, column);
    }

    protected void setBase(String iri, int line, int column) {
        profile.setBaseIRI(iri);
        stream.base(iri);
    }

    protected void setPrefix(String prefix, String iri, int line, int column) {
        prefix = canonicalPrefix(prefix, line, column);
        profile.getPrefixMap().add(prefix,iri);
        stream.prefix(prefix, iri);
    }

    protected void declareVersion(String version, int line, int column) {
        stream.version(version);
    }

    protected void emitTriple(int line, int column, Node s, Node p, Node o) {
        stream.triple(Triple.create(s, p, o));
    }

    protected Node emitTripleReifier(int line, int column, Node reifierId, Node s, Node p, Node o) {
        Node tripleTerm = createTripleTerm(s, p, o, line, column);
        if ( reifierId == null )
            reifierId = createBNode(line, column);
        Triple reifiedTriple = Triple.create(reifierId, nRDFreifies, tripleTerm);
        stream.triple(reifiedTriple);
        return reifierId;
    }

    private Node annotationReifierId = null;

    protected void setReifierId(Node reifId) {
        annotationReifierId = reifId;
    }

    protected Node getOrAllocReifierId(Node s, Node p, Node o, int line, int column) {
        if ( annotationReifierId != null )
            return annotationReifierId;
        Node reifierId = createBNode(line, column);
        emitTripleReifier(line, column, reifierId, s, p, o);
        return reifierId;
    }

    protected void clearReifierId() {
        annotationReifierId = null;
    }

    protected String unescapeIRI(String iriStr) {
        try {
            return EscapeStr.unescape(iriStr, '\\', true);
        } catch (AtlasException ex) {
            throw new RiotException(ex.getMessage());
        }
    }

    protected void listStart(int line, int column) {};
    protected void listTriple(int line, int column, Node s, Node p , Node o) { emitTriple(line, column, s, p, o); }
    protected void listFinish(int line, int column) {};
}
