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

import java.math.BigDecimal ;
import java.math.BigInteger ;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.xml.datatype.XMLGregorianCalendar ;

import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.graph.NodeConst ;
import org.apache.jena.sparql.util.DateTimeStruct ;
import org.apache.jena.sparql.util.XSDNumUtils;

/** Operations to convert the given Node to a normalized form */
class NormalizeValue
{
    /** Handler that makes no changes and returns the input node */
    private static DatatypeHandler identity = (Node node, String lexicalForm, RDFDatatype datatype) -> node ;

    // See Normalizevalue2 for "faster" versions (less parsing overhead).

    static DatatypeHandler dtBoolean = (Node node, String lexicalForm, RDFDatatype datatype) -> {
        return switch(lexicalForm) {
            case "1" -> NodeConst.nodeTrue ;
            case "0" -> NodeConst.nodeFalse ;
            default ->  node;
        };
    } ;

    static DatatypeHandler dtAnyDateTime = (Node node, String lexicalForm, RDFDatatype datatype) -> {
        // Fast test:
        if ( lexicalForm.indexOf('.') < 0 )
            // No fractional seconds.
            return node ;

        // Could use XMLGregorianCalendar but still need to canonicalize fractional seconds.
        // Record for history.
        if ( false )
        {
            XMLGregorianCalendar xcal = NodeValue.xmlDatatypeFactory.newXMLGregorianCalendar(lexicalForm) ;
            if ( xcal.getFractionalSecond() != null )
            {
                if ( xcal.getFractionalSecond().compareTo(BigDecimal.ZERO) == 0 )
                    xcal.setFractionalSecond(null) ;
                else
                    // stripTrailingZeros does the right thing on fractional values.
                    xcal.setFractionalSecond(xcal.getFractionalSecond().stripTrailingZeros()) ;
            }
            String lex2 = xcal.toXMLFormat() ;
            if ( lex2.equals(lexicalForm) )
                return node ;
            return NodeFactory.createLiteralDT(lex2, datatype) ;
        }
        // The only variablity for a valid date/dateTime/g* type is:
        //   Second part can have fractional seconds '.' s+ (if present) represents the fractional seconds;
        DateTimeStruct dts = DateTimeStruct.parseDateTime(lexicalForm) ;
        int idx = dts.second.indexOf('.') ;     // We have already tested for the existence of '.'
        int i = dts.second.length()-1 ;
        for ( ; i > idx ; i-- )
        {
            if ( dts.second.charAt(i) != '0' )
                break ;
        }
        if ( i == dts.second.length() )
            return node ;

        if ( i == idx )
            // All trailings zeros, drop the '.' as well.
            dts.second = dts.second.substring(0, idx) ;
        else
            dts.second = dts.second.substring(0, i+1) ;

        String lex2 = dts.toString() ;
        // Can't happen.  We munged dts.second.
//            if ( lex2.equals(lexicalForm) )
//                return node ;
        return NodeFactory.createLiteralDT(lex2, datatype) ;
    } ;

    static DatatypeHandler dtDateTime = dtAnyDateTime ;

    static DatatypeHandler dtInteger = (Node node, String lexicalForm, RDFDatatype datatype) -> {
        char[] chars = lexicalForm.toCharArray() ;
        if ( chars.length == 0 )
            // Illegal lexical form.
            return node ;

        // If valid and one char, it must be legal.
        // If valid, and two chars and not leading 0, it must be valid.
        String lex2 = lexicalForm ;

        if ( lex2.startsWith("+") )
            lex2 = lex2.substring(1) ;

        if ( lex2.length() > 8 )
            // Maybe larger than an int so do carefully.
            lex2 = new BigInteger(lexicalForm).toString() ;
        else
        {
            // Avoid object churn.
            int x = Integer.parseInt(lex2) ;
            lex2 = Integer.toString(x) ;
        }

        if ( lex2.equals(lexicalForm) )
            return node ;
        return NodeFactory.createLiteralDT(lex2, datatype) ;
    } ;

    static DatatypeHandler dtDecimalTTL = (Node node, String lexicalForm, RDFDatatype datatype) -> {




        BigDecimal bd = new BigDecimal(lexicalForm).stripTrailingZeros() ;
        String lex2 = bd.toPlainString() ;

        // XSD canonical is "1"
        // but in Turtle the ".0" is need for short print form.

        // Ensure there is a "."
        if ( lex2.indexOf('.') == -1 )
            // Must contain .0
            lex2 = lex2+".0" ;
        if ( lex2.equals(lexicalForm) )
            return node ;
        return NodeFactory.createLiteralDT(lex2, datatype) ;
    } ;

    static DatatypeHandler dtDoubleTTL = (Node node, String lexicalForm, RDFDatatype datatype) -> {
        double d = XSDNumUtils.xsdParseDouble(lexicalForm) ;
        String lex2 = XSDNumUtils.stringForm(d);
        if ( lex2.equals(lexicalForm) )
            return node ;
        return NodeFactory.createLiteralDT(lex2, datatype) ;
    } ;

    static DatatypeHandler dtFloatTTL = (Node node, String lexicalForm, RDFDatatype datatype) -> {
        float f = XSDNumUtils.xsdParseFloat(lexicalForm) ;
        String lex2 = XSDNumUtils.stringForm(f);
        if ( lex2.equals(lexicalForm) )
            return node ;
        return NodeFactory.createLiteralDT(lex2, datatype) ;
    } ;

    // --- XSD, more closely.
    /*
     * Format floats and double by using {@link DecimalFormat}.
     * This can move the decimal point and change the exponent value.
     * All numbers are "n.nnnExxx".
     * For "smaller" floats and double, Java formatting as used by
     * {@link XSDNumUtils#stringForm(double)} or {@link XSDNumUtils#stringForm(float)}
     * leaves the number in "common" form, with the mantissa (significand) having the decimal point
     * in the place for an exponent of zero if possible.
     */
    static private DecimalFormatSymbols decimalNumberSymbols = new DecimalFormatSymbols(Locale.ROOT) ;
    static private NumberFormat fmtFloat = new DecimalFormat("0.0#####E0", decimalNumberSymbols) ;
    static private NumberFormat fmtDouble = new DecimalFormat("0.0#################E0", decimalNumberSymbols) ;

    /*package*/static DatatypeHandler dtDoubleXSD = (Node node, String lexicalForm, RDFDatatype datatype) -> {
        double d = XSDNumUtils.xsdParseDouble(lexicalForm);
        String lex2;
        if ( Double.isInfinite(d) ) {
            lex2 = d < 0 ? "-INF" : "INF";
        } else {
            lex2 = fmtDouble.format(d) ;
        }
        if ( lex2.equals(lexicalForm) )
            return node ;
        return NodeFactory.createLiteralDT(lex2, datatype) ;
    } ;

    /*package*/static DatatypeHandler dtFloatXSD = (Node node, String lexicalForm, RDFDatatype datatype) -> {
        float f = XSDNumUtils.xsdParseFloat(lexicalForm);
        String lex2;
        if ( Float.isInfinite(f) ) {
            lex2 = f < 0 ? "-INF" : "INF";
        } else {
            lex2 = fmtFloat.format(f) ;
        }
        if ( lex2.equals(lexicalForm) )
            return node ;
        return NodeFactory.createLiteralDT(lex2, datatype) ;
    } ;

    /*package*/static DatatypeHandler dtDecimalXSD = (Node node, String lexicalForm, RDFDatatype datatype) -> {
        BigDecimal decimal = XSDNumUtils.xsdParseDecimal(lexicalForm);
        String lex2 = XSDNumUtils.canonicalDecimalStrNoIntegerDot(decimal);
        if ( lex2.equals(lexicalForm) )
            return node ;
        return NodeFactory.createLiteralDT(lex2, datatype) ;
    } ;

    /** XSD 1.0 - always has a decimal point.
    /*package*/static DatatypeHandler dtDecimalXSD10 = (Node node, String lexicalForm, RDFDatatype datatype) -> {
        BigDecimal decimal = XSDNumUtils.xsdParseDecimal(lexicalForm);
        String lex2 = XSDNumUtils.canonicalDecimalStrWithDot(decimal);
        if ( lex2.equals(lexicalForm) )
            return node ;
        return NodeFactory.createLiteralDT(lex2, datatype) ;
    } ;

//    private static DatatypeHandler dtPlainLiteral = (Node node, String lexicalForm, RDFDatatype datatype) -> {
//        int idx = lexicalForm.lastIndexOf('@') ;
//        if ( idx == -1 )
//            // Bad rdf:PlainLiteral
//            return node ;
//
//        String lex = lexicalForm.substring(0, idx) ;
//        if ( idx == lexicalForm.length()-1 )
//            return NodeFactory.createLiteralString(lex) ;
//        String lang = lexicalForm.substring(idx+1) ;
//        return NodeFactory.createLiteralLang(lex, lang) ;
//    } ;
}
