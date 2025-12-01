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

package org.apache.jena.sparql.expr.nodevalue;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;
import java.util.function.Function;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeConstants.Field;
import javax.xml.datatype.Duration;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.TextDirection;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIx;
import org.apache.jena.query.ARQ;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.vocabulary.XSD;

/**
 * Implementation of node-centric functions.
 */
public class NodeFunctions {

    // -------- sameTerm

    public static NodeValue sameTerm(NodeValue nv1, NodeValue nv2) {
        return NodeValue.booleanReturn(sameTerm(nv1.asNode(), nv2.asNode()));
    }

    // Jena - up to Jena 4.
    // Language tags were kept in the case form they were given as.
    // Jena5 - language tags are canonicalised.

    /** sameTerm(x,y) */
    public static boolean sameTerm(Node node1, Node node2) {
        return node1.sameTermAs(node2);
    }

// Before jena stored normalized language tags ...
//    /** sameTerm(x,y) */
//    public static boolean sameTerm(Node node1, Node node2) {
//        if ( node1.equals(node2) )
//            return true;
//        if ( Util.isLangString(node1) && Util.isLangString(node2) ) {
//            String lex1 = node1.getLiteralLexicalForm();
//            String lex2 = node2.getLiteralLexicalForm();
//            if ( !lex1.equals(lex2) )
//                return false;
//            return node1.getLiteralLanguage().equalsIgnoreCase(node2.getLiteralLanguage());
//        }
//        if ( node1.isTripleTerm() && node2.isTripleTerm() ) {
//            return sameTriples(node1.getTriple(), node2.getTriple());
//        }
//        return false;
//    }
//
//    private static boolean sameTriples(Triple t1, Triple t2) {
//        return sameTerm(t1.getSubject(), t2.getSubject())
//            && sameTerm(t1.getPredicate(), t2.getPredicate())
//            && sameTerm(t1.getObject(), t2.getObject());
//    }

    // -------- sameValue

    public static boolean sameValue(NodeValue nv1, NodeValue nv2) {
        return NodeValue.sameValueAs(nv1, nv2);
    }

    public static boolean sameValue(Node node1, Node node2) {
        NodeValue nv1 = NodeValue.makeNode(node1);
        NodeValue nv2 = NodeValue.makeNode(node2);
        return NodeValue.sameValueAs(nv1, nv2);
    }

    public static boolean notSameValue(NodeValue nv1, NodeValue nv2) {
        return NodeValue.notSameValueAs(nv1, nv2);
    }

    public static boolean notSameValue(Node node1, Node node2) {
        NodeValue nv1 = NodeValue.makeNode(node1);
        NodeValue nv2 = NodeValue.makeNode(node2);
        return NodeValue.notSameValueAs(nv1, nv2);
    }

    // -------- RDFterm-equals -- raises an exception on "don't know" for literals.

    // Exact as defined by SPARQL spec, when there are no value extensions.
    //   Exception for two literals that might be equal but we don't know because of language tags.

    // THIS IS NOT:
    // SPARQL 1.1 = RDFterm-equals
    // SPARQL 1.2 = sameValue

    public static boolean rdfTermEquals(Node n1, Node n2) {
        if ( n1.equals(n2) )
            return true;

        if ( n1.isLiteral() && n2.isLiteral() ) {
            // Two literals, may be sameTerm by language tag case insensitivity.
            String lang1 = n1.getLiteralLanguage();
            String lang2 = n2.getLiteralLanguage();
            if ( isNotEmpty(lang1) && isNotEmpty(lang2) ) {
                // Two language tags, both not "", equal by case insensitivity => lexical test.
                if ( lang1.equalsIgnoreCase(lang2) ) {
                    boolean b = n1.getLiteralLexicalForm().equals(n2.getLiteralLexicalForm());
                    if ( b )
                        return true;
                }
            }

            // Two literals:
            //   Were not .equals
            //   case 1: At least one language tag., not same lexical form -> unknown.
            //   case 2: No language tags, not .equals -> unknown.
            // Raise error (rather than return false).
            NodeValue.raise(new ExprEvalException("Mismatch in RDFterm-equals: " + n1 + ", " + n2));
        }

        if ( n1.isTripleTerm() && n2.isTripleTerm() ) {
            Triple t1 = n1.getTriple();
            Triple t2 = n2.getTriple();
            return rdfTermEquals(t1.getSubject(), t2.getSubject())
                && rdfTermEquals(t1.getPredicate(), t2.getPredicate())
                && rdfTermEquals(t1.getObject(), t2.getObject());
        }

        // Not both literal nor both triple terms - .equals would have worked.
        return false;
    }

    // -------- str
    public static NodeValue str(NodeValue nv) {
        return NodeValue.makeString(str(nv.asNode()));
    }

    public static String str(Node node) {
        if ( node.isLiteral() )
            return node.getLiteral().getLexicalForm();
        if ( node.isURI() )
            return node.getURI();
        if ( node.isBlank() && ! ARQ.isTrue(ARQ.strictSPARQL) )
             return RiotLib.blankNodeToIriString(node);
        if ( node.isTripleTerm() && ! ARQ.isTrue(ARQ.strictSPARQL) ) {
            Triple t = node.getTriple();
            // Recursion. Assumes no cycles!
            Function<Node, String> f = NodeFmtLib::strTTL;
            return "<< " + f.apply(t.getSubject()) + " " + f.apply(t.getPredicate()) + " " + f.apply(t.getObject()) + " >>";
        }
        if ( node.isBlank() )
            NodeValue.raise(new ExprEvalException("Blank node: " + node)) ;
        if ( node.isTripleTerm())
            NodeValue.raise(new ExprEvalException("Triple term: " + node)) ;
        NodeValue.raise(new ExprEvalException("Not valid for STR(): " + node)) ;
        return "[undef]" ;
    }

    // -------- sort key (collation)

    public static NodeValue sortKey(NodeValue nv, String collation) {
        return NodeValue.makeSortKey(str(nv.asNode()), collation);
    }

    // -------- datatype
    public static NodeValue datatype(NodeValue nv) {
        return NodeValue.makeNode(datatype(nv.asNode()));
    }

    public static Node datatype(Node node) {
        if ( !node.isLiteral() ) {
            NodeValue.raise(new ExprTypeException("datatype: Not a literal: " + node));
            return null;
        }

        String s = node.getLiteralDatatypeURI();
        boolean plainLiteral = (s == null || s.equals(""));

        if ( plainLiteral ) {
            boolean simpleLiteral = (node.getLiteralLanguage() == null || node.getLiteralLanguage().equals(""));
            if ( !simpleLiteral )
                return NodeConst.rdfLangString;
            return XSD.xstring.asNode();
        }
        return NodeFactory.createURI(s);
    }

    public static NodeValue isNumeric(NodeValue nv) {
        return NodeValue.booleanReturn( nv.isNumber() );
    }

    // -------- lang

    public static NodeValue hasLang(NodeValue nv) {
        return NodeValue.booleanReturn(hasLang(nv.asNode())) ;
    }

    public static boolean hasLang(Node node) {
        return NodeUtils.hasLang(node);
    }

    public static NodeValue hasLangDir(NodeValue nv) {
        return NodeValue.booleanReturn(hasLangDir(nv.asNode())) ;
    }

    public static boolean hasLangDir(Node node) {
        return NodeUtils.hasLangDir(node);
    }

    public static NodeValue lang(NodeValue nv) {
        if ( nv.isLangString() )
            return NodeValue.makeString(nv.getLang());
        if ( nv.isLiteral() )
            return NodeValue.nvEmptyString;
        NodeValue.raise(new ExprTypeException("lang: Not a literal: " + nv.asQuotedString()));
        return null;
    }

    public static String lang(Node node) {
        if ( !node.isLiteral() )
            NodeValue.raise(new ExprTypeException("lang: Not a literal: " + FmtUtils.stringForNode(node)));

        String s = node.getLiteralLanguage();
        if ( s == null )
            return "" ;
        return s ;
    }

    public static NodeValue langdir(NodeValue nv) {
        if ( nv.isLangString() )
            return NodeValue.makeString(nv.getLangDir()) ;
        if ( nv.isLiteral() )
            return NodeValue.nvEmptyString ;
        NodeValue.raise(new ExprTypeException("lang: Not a literal: " + nv.asQuotedString())) ;
        return null;
    }

    public static String langdir(Node node) {
        if ( !node.isLiteral() )
            NodeValue.raise(new ExprTypeException("lang: Not a literal: " + FmtUtils.stringForNode(node))) ;

        TextDirection textDir = node.getLiteralBaseDirection() ;
        if ( textDir == null )
            return "";
        return textDir.direction();
    }

    // -------- langMatches
    /** LANGMATCHES
     *
     * @param nv The language string
     * @param nvPattern The pattern to match against
     * @return Boolean nodeValue
     */
    public static NodeValue langMatches(NodeValue nv, NodeValue nvPattern) {
        return langMatches(nv, nvPattern.getString());
    }

    /** LANGMATCHES
     *
     * @param nv The language string
     * @param langPattern The pattern to match against
     * @return Boolean nodeValue
     */
    public static NodeValue langMatches(NodeValue nv, String langPattern) {
        Node node = nv.asNode();
        if ( !node.isLiteral() ) {
            NodeValue.raise(new ExprTypeException("langMatches: not a literal: " + node));
            return null;
        }

        String langStr = node.getLiteralLexicalForm();
        return NodeValue.booleanReturn(langMatches(langStr, langPattern));
    }

    /** The algorithm for the SPARQL function "LANGMATCHES".
     * Matching in SPARQL is defined to be the language tag matching of
     *  <a href="https://tools.ietf.org/html/rfc4647">RFC 4647</a>, part of
     *  <a href="https://tools.ietf.org/html/bcp47">BCP 47</a>.
     *  <p>
     *  SPARQL uses basic matching which is single "*" or a prefix of subtags.
     *  <p>
     *  This code does not implement extended matching correctly.
     *
     * @param langStr The language string
     * @param langPattern The pattern to match against
     * @return Whether there is a match.
     */
    public static boolean langMatches(String langStr, String langPattern) {
        // Nowadays there is JDK support for language tags:
        //   List<Locale.LanguageRange> parse = Locale.LanguageRange.parse(langPattern);
        //   List<String> strings = Locale.filterTags(parse, Collections.singletonList(langTag));
        //   return !strings.isEmpty();
        // which churns quite a few small objects so compiling fixed langPattern would be sensible.

        if ( langPattern.equals("*") ) {
            // Not a legal lang string.
            if ( langStr == null || langStr.equals("") )
                return false;
            return true;
        }

        // Basic Language Range
        //     language-range   = (1*8ALPHA *("-" 1*8alphanum)) / "*"
        //     alphanum         = ALPHA / DIGIT

        // Extended Language Range
        //     extended-language-range = (1*8ALPHA / "*")
        //     *("-" (1*8alphanum / "*"))

        String[] langElts = langStr.split("-");
        String[] langRangeElts = langPattern.split("-");

        /*
         * Here is the logic to compare language code. There is a match if the
         * language matches the parts of the pattern - the language may be
         * longer than the pattern.
         */

        /*
         * RFC 4647 basic filtering.
         *
         * Notes for extended:
         *  1. Remove any "-*" (but not *)
         *  2. Compare primary tags.
         *  3. Is the remaining range a subsequence of the remaining language tag?
         */
        if ( langRangeElts.length > langElts.length )
            // Lang tag longer than pattern tag => can't match
            return false;
        for ( int i = 0; i < langRangeElts.length; i++ ) {
            String range = langRangeElts[i];
            if ( range == null )
                break;
            // Language longer than range
            if ( i >= langElts.length )
                break;
            String lang = langElts[i];
            if ( range.equals("*") )
                continue;
            if ( !range.equalsIgnoreCase(lang) )
                return false;
        }
        return true;
    }

    // -------- isURI/isIRI

    public static NodeValue isIRI(NodeValue nv) {
        return NodeValue.booleanReturn(isIRI(nv.asNode()));
    }

    public static boolean isIRI(Node node) {
        if ( node.isURI() )
            return true;
        return false;
    }

    public static NodeValue isURI(NodeValue nv) {
        return NodeValue.booleanReturn(isIRI(nv.asNode()));
    }

    public static boolean isURI(Node node) {
        return isIRI(node);
    }

    // -------- isBlank
    public static NodeValue isBlank(NodeValue nv) {
        return NodeValue.booleanReturn(isBlank(nv.asNode()));
    }

    public static boolean isBlank(Node node) {
        return node.isBlank();
    }

    // -------- isLiteral
    public static NodeValue isLiteral(NodeValue nv) {
        return NodeValue.booleanReturn(isLiteral(nv.asNode()));
    }

    public static boolean isLiteral(Node node) {
        return node.isLiteral();
    }

    /** NodeValue to NodeValue, skolemizing, and converting strings to URIs. */
    public static NodeValue iri(NodeValue nv, String baseIRI) {
        if ( isIRI(nv.asNode()) )
            return nv;
        Node n2 = iri(nv.asNode(), baseIRI);
        return NodeValue.makeNode(n2);
    }

    /** Node to Node, skolemizing, and converting strings to URIs. */
    public static Node iri(Node n, String baseIRI) {
        Node node = RiotLib.blankNodeToIri(n);
        if ( node.isURI() )
            return node;
        // Literals.
        // Simple literal or xsd:string
        String str = simpleLiteralOrXSDString(node);
        if ( str == null )
            throw new ExprEvalException("Can't make an IRI from " + node);

        String iriStr = node.getLiteralLexicalForm();
        if ( RiotLib.isBNodeIRI(iriStr) ) {
            // Jena's "Blank node URI" <_:...>
            // Pass through as an IRI.
            return NodeFactory.createURI(iriStr);
        }
        String iri = resolveCheckIRI(baseIRI, iriStr);
        return NodeFactory.createURI(iri);
    }

    private static String resolveCheckIRI(String baseIRI, String iriStr) {
        try {
            IRIx iri = IRIx.create(iriStr);
            if ( ! iri.isRelative() )
                    return iriStr;
            IRIx base = ( baseIRI != null ) ? IRIx.create(baseIRI) : IRIs.getSystemBase();
            if ( base.isRelative() /*&& iri.isRelative()*/ )
                throw new ExprEvalException("Relative IRI for base: " + iriStr);
            IRIx result = base.resolve(iri);
            if ( ! result.isReference() )
                throw new IRIException("Not suitable: "+result.str());
            return result.str();
        } catch (IRIException ex) {
            throw new ExprEvalException("Bad IRI: " + iriStr);
        }
    }

    public static NodeValue struuid() {
        return NodeValue.makeString(uuidString());
    }

    public static NodeValue uuid() {
        String str = "urn:uuid:" + uuidString();
        Node n = NodeFactory.createURI(str);
        return NodeValue.makeNode(n);
    }

    private static String uuidString() {
        return UUID.randomUUID().toString();
    }

    private static String simpleLiteralOrXSDString(Node n) {
        if ( !n.isLiteral() )
            return null;

        if ( n.getLiteralDatatype() == null ) {
            if ( n.getLiteralLanguage().equals("") )
                return n.getLiteralLexicalForm();
        } else if ( n.getLiteralDatatype().equals(XSDDatatype.XSDstring) )
            return n.getLiteralLexicalForm();
        return null;
    }

    public static NodeValue strDatatype(NodeValue v1, NodeValue v2) {
        if ( !v1.isString() )
            throw new ExprEvalException("Not a string (arg 1): " + v1);
        if ( !v2.isIRI() )
            throw new ExprEvalException("Not an IRI (arg 2): " + v2);

        String lex = v1.asString();
        Node dt = v2.asNode();
        // Check?

        Node n = NodeFactory.createLiteralDT(lex, NodeFactory.getType(dt.getURI()));
        return NodeValue.makeNode(n);
    }

    public static NodeValue strLang(NodeValue v1, NodeValue v2) {
        if ( !v1.isString() )
            throw new ExprEvalException("Not a string (arg 1): " + v1);
        if ( !v2.isString() )
            throw new ExprEvalException("Not a string (arg 2): " + v2);

        String lex = v1.asString();
        String lang = v2.asString();
        if ( lang.isEmpty() )
            throw new ExprEvalException("Empty lang tag");
        return NodeValue.makeLangString(lex, lang);
    }

    public static NodeValue strLangDir(NodeValue v1, NodeValue v2, NodeValue v3) {
        if ( !v1.isString() )
            throw new ExprEvalException("Not a string (arg 1): " + v1) ;
        if ( !v2.isString() )
            throw new ExprEvalException("Not a string (arg 2): " + v2) ;
        if ( !v2.isString() )
            throw new ExprEvalException("Not a string (arg 3): " + v2) ;
        String lex = v1.asString() ;
        String lang = v2.asString() ;
        if ( lang.isEmpty() )
            throw new ExprEvalException("Empty lang tag") ;
        String textDirStr = v3.asString();
        if ( textDirStr.isEmpty() )
            throw new ExprEvalException("Empty base direction") ;
        TextDirection textDir = TextDirection.createOrNull(v3.asString());
        if ( textDir == null )
            throw new ExprEvalException("Invalid base direction: '"+textDirStr+"'") ;
        return NodeValue.makeDirLangString(lex, lang, textDir);
    }

    /** Canonical duration of 0 -- "PT0S" */
    private static Duration zeroDuration = NodeValue.xmlDatatypeFactory.newDuration(true, null, null, null, null, null, BigDecimal.ZERO) ;
    /** A duration, tided */
    public static Duration duration(int seconds) {
        if ( seconds == 0 )
            return zeroDuration;
        Duration dur = NodeValue.xmlDatatypeFactory.newDuration(1000L*seconds);
        // Neaten the duration. Not all the fields are zero.
        dur = NodeValue.xmlDatatypeFactory.newDuration(dur.getSign()>=0,
                                                       field(dur, DatatypeConstants.YEARS),
                                                       field(dur, DatatypeConstants.MONTHS),
                                                       field(dur, DatatypeConstants.DAYS),
                                                       field(dur, DatatypeConstants.HOURS),
                                                       field(dur, DatatypeConstants.MINUTES),
                                                       field2(dur, DatatypeConstants.SECONDS));
        return dur;
    }

    //Don't set field if zero.
    private static BigInteger field(Duration dur, Field field) {
        BigInteger i = (BigInteger)dur.getField(field);
        if ( i == null || i.equals(BigInteger.ZERO) )
            return null;
        return i;
    }

    private static BigDecimal field2(Duration dur, Field field) {
        BigDecimal x = (BigDecimal)dur.getField(field);
        //x = x.setScale(0);
        if ( x.compareTo(BigDecimal.ZERO) == 0 )
            return null;
        return x;
    }
}
