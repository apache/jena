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

package org.apache.jena.riot.process.normalize;

import java.util.HashMap ;
import java.util.Map ;

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.riot.web.LangTag ;
import org.apache.jena.sparql.util.NodeUtils ;
import org.apache.jena.vocabulary.RDF ;

/**
 * Convert literals to normalized forms. Sometimes called canonicalization. There is
 * one preferred RDFTerm for a given RDFterm "value" ("value" generalized to include
 * URis and blank nodes). Mostly, this affects literals. Only certain datatypes are
 * supported but applications can add normalization for other datatypes.
 * <p>
 * Various policies are provided:
 * <ul>
 * <li>{@link #get() General} (close to Turtle, use the turtle form for long term contract)</li>
 * <li>{@link #getTTL() Turtle}</li>
 *  * <li>{@link #getXSD() XSD}</li>
 * <li>XSD - follows XSD 1.1. Mantissa/exponents are adjusted. xsd:decimal of an
 * integer value does not have a decimal point, and is not suitable for Turtle as a
 * xsd:decimal short form.</li>
 * <li>Turtle - produces decimals, floats and double suitable for Turtle short-form syntax.</li>
 * </ul>
 * <p>
 * XSD Schema 1.1 does not define a canonical form for all cases.
 * <p>
 */

public class NormalizeRDFTerms implements NormalizeTerm {

    enum Style { General, XSD, XSD11, XSD10 }

    /**
     * Convert literals to normalized forms. A number of different policies are
     * <p>
     * Strictly, this is "normalization" - XSD Schema 1.1 does not define a canonical form for all cases.
     * <p>
     * <p>
     * N.B. The normalization does produce forms for decimals and doubles that are correct as Turtle syntactic forms.
     * For doubles, but not floats, zero is "0.0e0", whereas Java produces "0.0".
     * For floats, the Java is returned for values with low precision.
     *
     */
    private static final NormalizeRDFTerms mapGeneral   = mappingGeneral();
    private static final NormalizeRDFTerms mapTTL       = mappingTTL();
    private static final NormalizeRDFTerms mapXSD11     = mappingXSD11();
    private static final NormalizeRDFTerms mapXSD10     = mappingXSD10();

    /** General normalization. */
    public static NormalizeRDFTerms get() { return mapGeneral ; }

    /**
     * Normalization for use in Turtle output syntax.
     * <ul>
     * <li>xsd:decimals always have a decimal point.</li>
     * <li>xsd:doubles always have an exponent. For ones that are less that 10E7, add
     *     "e0", otherwise normalize the mantissa and have an exponent ('E').
     * <li>xsd:floats For ones that are less that 10E7, just the decimal, no expoent.
     *     Otherwise normalize the mantissa and have an expoent ('E').
     * </ul>
     The normalization does produce forms for decimals and doubles that are
     * correct as Turtle syntactic forms. For doubles, but not floats, zero is "0.0e0",
     * whereas Java produces "0.0". For floats, the Java is returned for values with low
     * precision.
     */
    public static NormalizeRDFTerms getTTL() { return mapGeneral ; }

    /**
     * Normalization by XSD 1.1
     * <ul>
     * <li>xsd:double and xsd:float - the mantissa and exponent are adjusted based on
     * value. The Exponent is 'E'.</li>
     * <li>xsd;decimal - an integer value does not have a decimal point and may not be
     * suitable for Turtle as a xsd:decimal short form.</li>
     * </ul>
     */
    public static NormalizeRDFTerms getXSD() { return mapXSD11 ; }

    /** Normalize based on XSD 1.1. */
    public static NormalizeRDFTerms getXSD11() { return mapXSD11 ; }

    /** Normalize based on XSD 1.0 where decimals always have  decimal point. */
    public static NormalizeRDFTerms getXSD10() { return mapXSD10 ; }

    private static NormalizeRDFTerms mappingGeneral() {
       Map<RDFDatatype, DatatypeHandler> mapping = baseMap();
       return new NormalizeRDFTerms(mapping);
    }

    private static NormalizeRDFTerms mappingTTL() {
        Map<RDFDatatype, DatatypeHandler> mapping = baseMap();
        mapping.put(XSDDatatype.XSDdecimal, NormalizeValue.dtDecimalTTL);
        mapping.put(XSDDatatype.XSDdouble, NormalizeValue.dtDoubleTTL);
        mapping.put(XSDDatatype.XSDfloat, NormalizeValue.dtFloatTTL);
        return new NormalizeRDFTerms(mapping);
     }

    private static NormalizeRDFTerms mappingXSD11() {
        Map<RDFDatatype, DatatypeHandler> mapping = baseMap();
        mapping.put(XSDDatatype.XSDdecimal, NormalizeValue.dtDecimalXSD);
        mapping.put(XSDDatatype.XSDdouble, NormalizeValue.dtDoubleXSD);
        mapping.put(XSDDatatype.XSDfloat, NormalizeValue.dtFloatXSD);
        return new NormalizeRDFTerms(mapping);
     }

    private static NormalizeRDFTerms mappingXSD10() {
        Map<RDFDatatype, DatatypeHandler> mapping = baseMap();
        mapping.put(XSDDatatype.XSDdecimal, NormalizeValue.dtDecimalXSD10);
        mapping.put(XSDDatatype.XSDdouble, NormalizeValue.dtDoubleXSD);
        mapping.put(XSDDatatype.XSDfloat, NormalizeValue.dtFloatXSD);
        return new NormalizeRDFTerms(mapping);
     }

    private final Map<RDFDatatype, DatatypeHandler> dispatchMapping;

    private NormalizeRDFTerms(Map<RDFDatatype, DatatypeHandler> mapping) {
        this.dispatchMapping = Map.copyOf(mapping);
    }


    /**
     * Canonicalize a literal, both lexical form and language tag
     */
    public static Node normalizeValue(Node node) {
        return get().normalize(node);
    }

    /**
     * Canonicalize a literal, both lexical form and language tag
     */
    @Override
    public Node normalize(Node node) {
        return normalizeTerm(dispatchMapping, node);
    }


    /** Convert the lexical form to a canonical form if one of the known datatypes,
     * otherwise return the node argument. (same object :: {@code ==})
     */
    static Node normalizeTerm(Map<RDFDatatype, DatatypeHandler> dispatchMap, Node node) {
        if ( ! node.isLiteral() )
            return node ;
        if ( NodeUtils.isLangString(node) )
            return canonicalLangtag(node);
        if ( NodeUtils.isSimpleString(node) )
            return node;
        // Is it a valid value?
        // (Can we do this in the normal case code?)
        if ( ! node.getLiteralDatatype().isValid(node.getLiteralLexicalForm()) )
            // Invalid lexical form for the datatype - do nothing.
            return node;

        RDFDatatype dt = node.getLiteralDatatype() ;
        DatatypeHandler handler = dispatchMap.get(dt) ;
        if ( handler == null )
            return node ;
        Node n2 = handler.handle(node, node.getLiteralLexicalForm(), dt) ;
        if ( n2 == null )
            return node ;
        return n2 ;
    }

    /** Convert the language tag of a lexical form to a canonical form if one of the known datatypes,
     * otherwise return the node argument. (same object; compare by {@code ==})
     */
    private static Node canonicalLangtag(Node node) {
        String langTag = node.getLiteralLanguage();
        String langTag2 = LangTag.canonical(langTag);
        if ( langTag2.equals(langTag) )
            return node;
        //String textDir = n.getLiteralTextDirection();
        String lexicalForm = node.getLiteralLexicalForm();
        return NodeFactory.createLiteralLang(lexicalForm, langTag2);
    }

    private static final RDFDatatype dtPlainLiteral = NodeFactory.getType(RDF.PlainLiteral.getURI());

    private static Map<RDFDatatype, DatatypeHandler> baseMap() {
        // Nulls are not allowed in this map.
        Map<RDFDatatype, DatatypeHandler> map = new HashMap<>();
        addBaseAll(map);
        return map;
    }

    private static Map<RDFDatatype, DatatypeHandler> turtleMap() {
        Map<RDFDatatype, DatatypeHandler> map = new HashMap<>();
        addBaseAll(map);
        map.put(XSDDatatype.XSDdecimal,     NormalizeValue.dtDecimalTTL ) ;
        map.put(XSDDatatype.XSDfloat,       NormalizeValue.dtFloatTTL ) ;
        map.put(XSDDatatype.XSDdouble,      NormalizeValue.dtDoubleTTL ) ;
        return map;
    }

    private static Map<RDFDatatype, DatatypeHandler> xsdMap() {
        Map<RDFDatatype, DatatypeHandler> map = new HashMap<>();
        addBaseAll(map);
        map.put(XSDDatatype.XSDdecimal,     NormalizeValue.dtDecimalXSD ) ;
        map.put(XSDDatatype.XSDfloat,       NormalizeValue.dtFloatXSD ) ;
        map.put(XSDDatatype.XSDdouble,      NormalizeValue.dtDoubleXSD ) ;
        return map;
    }


    /*
     * Add the standard set of datatype handlers.
     * This is the general policy.
     * Decimals have a decimal point.
     * Doubles and floats have mantissa and exponent normalized to one digit, the decimal place.
     *
     * There are variation for specific use in Turtle and XSD with value-normalization.
     */
    private static void addBaseAll(Map<RDFDatatype, DatatypeHandler> map) {
        map.put(XSDDatatype.XSDinteger,             NormalizeValue.dtInteger) ;
        map.put(XSDDatatype.XSDdecimal,             NormalizeValue.dtDecimalTTL) ;
        map.put(XSDDatatype.XSDfloat,               NormalizeValue.dtFloatTTL ) ;
        map.put(XSDDatatype.XSDdouble,              NormalizeValue.dtDoubleTTL ) ;

        map.put(XSDDatatype.XSDint,                 NormalizeValue.dtInteger) ;
        map.put(XSDDatatype.XSDlong,                NormalizeValue.dtInteger) ;
        map.put(XSDDatatype.XSDshort,               NormalizeValue.dtInteger) ;
        map.put(XSDDatatype.XSDbyte,                NormalizeValue.dtInteger) ;

        map.put(XSDDatatype.XSDunsignedInt,         NormalizeValue.dtInteger) ;
        map.put(XSDDatatype.XSDunsignedLong,        NormalizeValue.dtInteger) ;
        map.put(XSDDatatype.XSDunsignedShort,       NormalizeValue.dtInteger) ;
        map.put(XSDDatatype.XSDunsignedByte,        NormalizeValue.dtInteger) ;

        map.put(XSDDatatype.XSDnonPositiveInteger,  NormalizeValue.dtInteger) ;
        map.put(XSDDatatype.XSDnonNegativeInteger,  NormalizeValue.dtInteger) ;
        map.put(XSDDatatype.XSDpositiveInteger,     NormalizeValue.dtInteger) ;
        map.put(XSDDatatype.XSDnegativeInteger,     NormalizeValue.dtInteger) ;

        // Only fractional seconds part can vary for the same value.
        map.put(XSDDatatype.XSDdateTime,            NormalizeValue.dtDateTime) ;
        map.put(XSDDatatype.XSDboolean,             NormalizeValue.dtBoolean) ;

        // Not covered.
        //map.put(XSDDatatype.XSDduration,   null) ;

        // These are fixed format
//        map.put(XSDDatatype.XSDdate,       null) ;
//        map.put(XSDDatatype.XSDtime,       null) ;
//        map.put(XSDDatatype.XSDgYear,      null) ;
//        map.put(XSDDatatype.XSDgYearMonth, null) ;
//        map.put(XSDDatatype.XSDgMonth,     null) ;
//        map.put(XSDDatatype.XSDgMonthDay,  null) ;
//        map.put(XSDDatatype.XSDgDay,       null) ;

        // Convert (illegal) rdf:PlainLiteral to a legal RDF term.
        //map.put(dtPlainLiteral,            NormalizeValue.dtPlainLiteral) ;


    }
}
