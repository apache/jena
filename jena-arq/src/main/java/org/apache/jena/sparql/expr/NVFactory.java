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

package org.apache.jena.sparql.expr;

import static org.apache.jena.sparql.expr.NVDatatypes.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.expr.nodevalue.*;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.sparql.util.RomanNumeral;
import org.apache.jena.sparql.util.RomanNumeralDatatype;
import org.apache.jena.vocabulary.RDF;

class NVFactory {
    @FunctionalInterface
    interface ToNodeValue { NodeValue create(RDFDatatype datatype, Node node); }

    private static Map<RDFDatatype, ToNodeValue> mapper = dtSetup();

    // Called from NodeValue.
    static NodeValue create(Node node) {
        if ( ! node.isLiteral() )
            // Not a literal - no value to extract
            return new NodeValueNode(node);

        // Special cases: LangString and DirLangString, well-formed.
        RDFDatatype datatype = node.getLiteralDatatype();

        boolean hasLangTag = NodeUtils.hasLang(node);  // hasLang - covers rdf:langString and rdf:dirLangString
        if ( hasLangTag ) {
            if ( NodeUtils.hasLangDir(node) ) {
                if ( ! RDF.dtDirLangString.equals(datatype) )
                    throw new InternalErrorException("Wrong type for literal with a text direction");
                return new NodeValueLangDir(node);
            } else {
                if ( ! RDF.dtLangString.equals(datatype) )
                    throw new InternalErrorException("Wrong type for literal with a langugae tag");
                return new NodeValueLang(node);
            }
        }
        // Includes literal datatype rdf:langString or rdf:dirLangString without the proper special components
        ToNodeValue function = mapper.get(datatype);
        if ( function == null )
            return new NodeValueNode(node);
        NodeValue nv = function.create(datatype, node);
        if ( nv == null )
            return new NodeValueNode(node);
        return nv;
    }

    public static NodeValue create(RDFDatatype datatype, Node node) {
        return mapper.get(datatype).create(datatype, node);
    }

    /** Create an immutable map of datatype to NodeValue maker */
    private static Map<RDFDatatype, ToNodeValue> dtSetup() {
        Map<RDFDatatype, ToNodeValue> mapper = new HashMap<>();

        entry(mapper, XSDdecimal,            NVFactory::decimalMaker);
        entry(mapper, XSDfloat,              NVFactory::floatMaker);
        entry(mapper, XSDdouble,             NVFactory::doubleMaker);

        entry(mapper, XSDinteger,            NVFactory::integerMaker);
        entry(mapper, XSDnonPositiveInteger, NVFactory::integerMaker);
        entry(mapper, XSDnonNegativeInteger, NVFactory::integerMaker);
        entry(mapper, XSDpositiveInteger,    NVFactory::integerMaker);
        entry(mapper, XSDnegativeInteger,    NVFactory::integerMaker);

        entry(mapper, XSDbyte,               NVFactory::integerMaker);
        entry(mapper, XSDshort,              NVFactory::integerMaker);
        entry(mapper, XSDint,                NVFactory::integerMaker);
        entry(mapper, XSDlong,               NVFactory::integerMaker);

        entry(mapper, XSDunsignedByte,       NVFactory::integerMaker);
        entry(mapper, XSDunsignedShort,      NVFactory::integerMaker);
        entry(mapper, XSDunsignedInt,        NVFactory::integerMaker);
        entry(mapper, XSDunsignedLong,       NVFactory::integerMaker);

        entry(mapper, XSDboolean,            NVFactory::booleanMaker);

        entry(mapper, XSDstring,             NVFactory::stringMaker);
        entry(mapper, XSDnormalizedString,  NVFactory::stringMaker);
        // XXX May be xsd;token, xsd:language

        entry(mapper, RDF.dtLangString,      NVFactory::langStringMaker);
        entry(mapper, RDF.dtDirLangString,   NVFactory::dirLangStringMaker);

//        entry(mapper, XSDhexBinary, null);
//        entry(mapper, XSDbase64Binary, null);

        entry(mapper, XSDdate,               NVFactory::dateTimeMaker);
        entry(mapper, XSDtime,               NVFactory::dateTimeMaker);
        entry(mapper, XSDdateTime,           NVFactory::dateTimeMaker);
        entry(mapper, XSDdateTimeStamp,      NVFactory::dateTimeMaker);

        entry(mapper, XSDgDay,               NVFactory::dateTimeMaker);
        entry(mapper, XSDgMonth,             NVFactory::dateTimeMaker);
        entry(mapper, XSDgYear,              NVFactory::dateTimeMaker);
        entry(mapper, XSDgYearMonth,         NVFactory::dateTimeMaker);
        entry(mapper, XSDgMonthDay,          NVFactory::dateTimeMaker);

        entry(mapper, XSDduration,           NVFactory::durationMaker);
        entry(mapper, XSDdayTimeDuration,    NVFactory::durationMaker);
        entry(mapper, XSDyearMonthDuration,  NVFactory::durationMaker);

        if ( SystemARQ.EnableRomanNumerals )
            entry(mapper, RomanNumeralDatatype.get(), NVFactory::romanNumeralMaker);

        return Map.copyOf(mapper);
    }

    private static void entry( Map<RDFDatatype, ToNodeValue> map, RDFDatatype rdfDatatype, ToNodeValue toNodeValue) {
        map.put(rdfDatatype, toNodeValue);
    }

    private static NodeValue integerMaker(RDFDatatype datatype, Node node) {
        if ( ! node.getLiteral().isWellFormed() )
            return null;
        String trimmedLexical = node.getLiteralLexicalForm().trim();
        if ( ! datatype.isValid(trimmedLexical) )
            return null;
        BigInteger bigInteger = new BigInteger(trimmedLexical);
        return new NodeValueInteger(bigInteger, node);
    }

    private static NodeValue floatMaker(RDFDatatype datatype, Node node) {
        if ( ! node.getLiteral().isWellFormed() )
            return null;
        // Uses getValue - no harm using isWellformed.
        LiteralLabel lit = node.getLiteral();
        float f = ((Number)lit.getValue()).floatValue();
        return new NodeValueFloat(f, node);
    }

    private static NodeValue doubleMaker(RDFDatatype datatype, Node node) {
        LiteralLabel lit = node.getLiteral();
        if ( ! lit.isWellFormed() )
            return null;
        double d = ((Number)lit.getValue()).doubleValue();
        return new NodeValueDouble(d, node);
    }

    private static NodeValue decimalMaker(RDFDatatype datatype, Node node) {
        LiteralLabel lit = node.getLiteral();
        if ( ! lit.isWellFormed() )
            return null;
        String trimmedLexical = node.getLiteralLexicalForm().trim();
        // jena-core narrows dataypes.
        BigDecimal decimal = new BigDecimal(trimmedLexical);
        return new NodeValueDecimal(decimal, node);
    }

    private static NodeValue booleanMaker(RDFDatatype datatype, Node node) {
        LiteralLabel lit = node.getLiteral();
        if ( ! lit.isWellFormed() )
            return null;
        boolean b = (Boolean) lit.getValue();
        return new NodeValueBoolean(b, node);
    }

    private static NodeValue stringMaker(RDFDatatype datatype, Node node) {
        return new NodeValueString(node.getLiteralLexicalForm(), node);
    }

    private static NodeValue langStringMaker(RDFDatatype datatype, Node node) {
        return new NodeValueLang(node);
    }

    private static NodeValue dirLangStringMaker(RDFDatatype datatype, Node node) {
        return new NodeValueLangDir(node);
    }

    private static NodeValue dateTimeMaker(RDFDatatype datatype, Node node) {
        String trimmedLexical = node.getLiteralLexicalForm().trim();
        try {
            XMLGregorianCalendar gCal = createXMLGregorianCalendar(trimmedLexical, node);
            // Check the expected fields.
            boolean isCorrect = NVOps.checkCalendarInstance(gCal, datatype);
            if (! isCorrect )
                return null;
            return new NodeValueDateTime(gCal, node);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    // Fixup
    private static XMLGregorianCalendar createXMLGregorianCalendar(String lex, Node n) {
        // Java bug : gMonth with a timezone of Z causes IllegalArgumentException
        if ( XSDgMonth.equals(n.getLiteralDatatype()) ) {
            if ( lex.endsWith("Z") ) {
                String lex2 = lex.substring(0, lex.length() - 1);
                XMLGregorianCalendar gCal = NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar(lex2);
                gCal.setTimezone(0);
                return gCal;
            }
        }
        XMLGregorianCalendar gCal = NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar(lex);
        return gCal;
    }

    private static NodeValue durationMaker(RDFDatatype datatype, Node node) {
        String trimmedLexical = node.getLiteralLexicalForm().trim();
        try {
            Duration duration = NodeValue.xmlDatatypeFactory.newDuration(trimmedLexical);
            boolean isCorrect = NVOps.checkDurationInstance(duration, datatype);
            if (! isCorrect )
                return null;
            return new NodeValueDuration(duration, node);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    // Test extension type!
    private static NodeValue romanNumeralMaker(RDFDatatype datatype, Node node) {
        LiteralLabel lit = node.getLiteral();
        Object obj = RomanNumeralDatatype.get().parse(lit.getLexicalForm());
        if ( obj instanceof Integer )
            return new NodeValueInteger(((Integer)obj).longValue());
        if ( obj instanceof RomanNumeral )
            return new NodeValueInteger( ((RomanNumeral)obj).intValue() );
        throw new ARQInternalErrorException("DatatypeFormatException: Roman numeral is unknown class");
    }

    /**
     * Converts a hexBinary literal node to a NodeValueNode.
     * Assumes the node is a valid hexBinary literal.
     */
    private static NodeValue hexBinaryToNodeValue(RDFDatatype datatype, Node node) {
        // No conversion, just wrap the node as a NodeValueNode
        return new NodeValueNode(node);
    }
}
