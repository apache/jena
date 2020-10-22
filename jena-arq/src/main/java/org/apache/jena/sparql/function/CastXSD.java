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

package org.apache.jena.sparql.function;

import static org.apache.jena.sparql.expr.NodeValue.nvNaN;
import static org.apache.jena.sparql.expr.NodeValue.nvNegZERO;
import static org.apache.jena.sparql.expr.NodeValue.nvZERO;
import static org.apache.jena.sparql.expr.nodevalue.XSDFuncOp.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDAbstractDateTimeType;
import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprEvalTypeException;
import org.apache.jena.sparql.expr.ExprException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;

/**
 * Code for all casting between XSD datatypes.
 * From <a href="https://www.w3.org/TR/xpath-functions/#casting">XPath and XQuery Functions and Operators</a>
 * v3.1
 *
 * @see FunctionCastXSD
 */
public class CastXSD {
    private CastXSD() {}

    /** Cast a NodeValue to an XSD datatype.
     * This includes "by value" so 1e0 (an xsd:double) casts to 1 (an xsd:integer)
     * @param nv
     * @param castType
     * @return NodeValue
     * @throws ExprEvalException
     */
    public static NodeValue cast(NodeValue nv, XSDDatatype castType) {
        // https://www.w3.org/TR/xpath-functions/#casting
        /*
            Casting
            19.1 Casting from primitive types to primitive types
                19.1.1 Casting to xs:string and xs:untypedAtomic
                19.1.2 Casting to numeric types
                19.1.3 Casting to duration types
                19.1.4 Casting to date and time types
                19.1.5 Casting to xs:boolean
                19.1.6 Casting to xs:base64Binary and xs:hexBinary
                19.1.7 Casting to xs:anyURI
                19.1.8 Casting to xs:QName and xs:NOTATION
                19.1.9 Casting to xs:ENTITY
            19.2 Casting from xs:string and xs:untypedAtomic
            19.3 Casting involving non-primitive types
                19.3.1 Casting to derived types
                19.3.2 Casting from derived types to parent types
                19.3.3 Casting within a branch of the type hierarchy
                19.3.4 Casting across the type hierarchy
                19.3.5 Casting to union types
                19.3.6 Casting to list types
         */

        Node n = nv.asNode();

        if ( n.isBlank() )
            throw exception("Can't cast blank nodes: "+nv);

        if ( n.isURI() ) {
            if ( castType.equals(XSDDatatype.XSDstring) )
                return cast$(n.getURI(), castType);
            else
                throw exception("Can't cast URIs to "+castType.getURI());
        }

        if ( ! n.isLiteral() )
            throw exception("Can't cast (not a literal, nor URI to string) "+nv+" : "+castType.getURI());

        // It's a literal.

        // Cast to self but may be an invalid lexical form.
        if ( Objects.equals(nv.getNode().getLiteralDatatype(), castType) ) {
            String lex = nv.getNode().getLiteralLexicalForm();
            if ( castType.isValid(lex) )
                return nv;
            throw exception("Invalid lexical form for "+castType.getURI());
        }

        if ( isNumericDatatype(castType) )
            return castToNumber(nv, castType);

        // To xs:duration, xs:yearMonthDuration, xs:dayTimeDuration
        if ( isDurationDatatype(castType) )
            return castToDuration(nv, castType);

        // To a temporal xs:dateTime, xs:time, xs:g* (Gregorian).
        if ( isTemporalDatatype(castType) )
            return XSDFuncOp.dateTimeCast(nv, castType);

        // To xsd:boolean
        if ( castType.equals(XSDDatatype.XSDboolean) )
            return castToBoolean(nv, castType);

        // To xsd:string
        if ( castType.equals(XSDDatatype.XSDstring) )
            return castToString(nv, castType);

        // 19.1.6 Casting to xs:base64Binary and xs:hexBinary
        if ( isBinaryDatatype(castType) ) {
            try {
                Node nValue = nv.getNode();
                // Already tested for "same datatype"
                byte[] binary = (byte[])nValue.getLiteralValue();
                Node nx = NodeFactory.createLiteralByValue(binary, castType);
                return NodeValue.makeNode(nx);

            } catch (Exception ex) {
                // Keep doing.
            }
        }

        // Values of type xs:base64Binary can be cast as xs:hexBinary and vice versa,
        // since the two types have the same value space. Casting to xs:base64Binary
        // and xs:hexBinary is also supported from the same type and from
        // xs:untypedAtomic, xs:string and subtypes of xs:string using [XML Schema
        // Part 2: Datatypes Second Edition] semantics.

        // 19.1.7 Casting to xs:anyURI

        // Fall through. Try by lexical - may produce junk - includes bad lexicals etc.
        return castByLex(nv, castType);
    }

    private static NodeValue castToNumber(NodeValue nv, XSDDatatype castType) {
        if ( castType.equals(XSDDatatype.XSDdecimal) ) {
            // Number to decimal.
            if ( isDouble(nv) || isFloat(nv) ) {
                // FP to decimal.
                double d = nv.getDouble();
                if ( Double.isNaN(d) )
                    throw exception("Can't cast NaN to xsd:decimal");
                if ( Double.isInfinite(d) )
                    throw exception("Can't cast Inf or -Inf to xsd:decimal");
                String lex = doubleToDecimalLex(d);
                if ( lex == null )
                    throw exception(nv, castType);
                return NodeValue.makeDecimal(lex);
            }
            if ( nv.isBoolean() ) {
                boolean b = nv.getBoolean();
                return b ? NodeValue.nvDecimalONE : NodeValue.nvDecimalZERO;
            }
            // Integer, or derived type -> decimal.
            return castByLex(nv, castType);
        }
        if ( isIntegerDatatype(castType) ) {
            // Number to integer
            if ( isDouble(nv) || isFloat(nv) ) {
                // FP to integer
                double d = nv.getDouble();
                if ( Double.isNaN(d) )
                    throw exception("Can't cast NaN to xsd:integer");
                if ( Double.isInfinite(d) )
                    throw exception("Can't cast Inf or -Inf to xsd:integer");
                String lex = doubleToIntegerLex(d);
                if ( lex != null )
                    return castByLex(lex, castType);
                throw exception(nv, castType);
            } else if ( isDecimal(nv) ) {
                // Decimal to integer
                BigDecimal bd = nv.getDecimal();
                String lex = decimalToIntegerLex(bd);
                if ( lex != null )
                    return castByLex(lex, castType);
                throw exception(nv, castType);
            } else if ( nv.isBoolean() ) {
                boolean b = nv.getBoolean();
                return b ? NodeValue.nvONE : NodeValue.nvZERO;
            } else {
                // Integer derived type -> integer derived type.
                return castByLex(nv, castType);
            }
        }

        if ( castType.equals(XSDDatatype.XSDdouble) || castType.equals(XSDDatatype.XSDfloat) ) {
            if ( nv.isBoolean() ) {
                boolean b = nv.getBoolean();
                return cast$( ( b ? "1.0E0" : "0.0E0" ) , castType);
            }
        }
        return castByLex(nv, castType);
    }

    private static NodeValue castToDuration(NodeValue nv, XSDDatatype castType) {
        // Duration cast.
        // yearMonthDuration and TT is xs:dayTimeDuration -> 0.0S
        // xs:dayTimeDuration and TT is yearMonthDuration -> P0M

        if ( isDuration(nv) ) {
            Duration d = nv.getDuration();
            if ( castType.equals(XSDDatatype.XSDyearMonthDuration) ) {
                // Include xsd:duration only covering year-month.
                if ( nv.isDayTimeDuration() )
                    return NodeValue.makeNode("P0M", castType);

                Duration d2 = NodeValue.xmlDatatypeFactory.newDuration
                    (d.getSign()>=0,
                    (BigInteger)d.getField(DatatypeConstants.YEARS), (BigInteger)d.getField(DatatypeConstants.MONTHS), null,
                    null, null, null);
                return NodeValue.makeNode(d2.toString(), castType);
            }
            if ( castType.equals(XSDDatatype.XSDdayTimeDuration) ) {
                if ( nv.isYearMonthDuration() )
                    return NodeValue.makeNode("PT0S", castType);
                Duration d2 =  NodeValue.xmlDatatypeFactory.newDuration
                    (d.getSign()>=0,
                    null, null, (BigInteger)d.getField(DatatypeConstants.DAYS),
                    (BigInteger)d.getField(DatatypeConstants.HOURS), (BigInteger)d.getField(DatatypeConstants.MINUTES), (BigDecimal)d.getField(DatatypeConstants.SECONDS));
                // return NodeValue.makeDuration(d2);
                return NodeValue.makeNode(d2.toString(), castType);
            }
            // Upcast - it wasn't same datatype.
            if ( castType.equals(XSDDatatype.XSDduration) )
                return castByLex(nv, XSDDatatype.XSDduration);
        }
        if ( nv.isString() )
            return castByLex(nv, castType);

        throw exception(nv, castType);
    }

    private static NodeValue castToBoolean(NodeValue nv, XSDDatatype castType) {
        /* https://www.w3.org/TR/xpath-functions/#casting-boolean
         * SV = source value
         * ST = source datatype
         * TV = target value
         * When a value of any ·primitive type· is cast as xs:boolean, the xs:boolean value TV is derived from ST and SV as follows:
         *   If ST is xs:boolean, then TV is SV.
         *   If ST is xs:float, xs:double, xs:decimal or xs:integer and SV is 0, +0, -0, 0.0, 0.0E0 or NaN, then TV is false.
         *   If ST is xs:float, xs:double, xs:decimal or xs:integer and SV is not one of the above values, then TV is true.
         *   If ST is xs:untypedAtomic or xs:string, see 19.2 Casting from xs:string and xs:untypedAtomic.
         */
        if ( nv.isBoolean() )
            return nv;
        if ( nv.isNumber() ) {
            if ( NodeValue.sameAs(nv, nvZERO) || NodeValue.sameAs(nv, nvNaN) || NodeValue.sameAs(nv, nvNegZERO) )
                return NodeValue.FALSE;
            return NodeValue.TRUE;
        }
        if ( nv.isString() ) {
            String str = nv.getString();
            switch (str) {
                case "0":
                case "false":
                    return NodeValue.FALSE;
                case "1":
                case "true":
                    return NodeValue.TRUE;
                default:
                    throw exception(nv, castType);
            }
        }
        return castByLex(nv, castType);
    }

    private static NodeValue castToString(NodeValue nv, XSDDatatype castType) {
        // https://www.w3.org/TR/xpath-functions/#casting-to-string
        if ( isDecimal(nv) ) {
            BigDecimal bd = nv.getDecimal();
            String str = XSDFuncOp.canonicalDecimalStrNoIntegerDot(bd);
            return NodeValue.makeString(str);
        }

        if ( isBoolean(nv) ) {
            boolean b = nv.getBoolean();
            String str = b ? "true" : "false";
            return NodeValue.makeString(str);
        }

        if ( isDouble(nv) || isFloat(nv) ) {
            double dValue = nv.getDouble();

            if ( dValue == 0d ) {
                int cmp = Double.compare(dValue, 0.0d);
                if ( cmp >= 0 )
                    return NodeValue.makeString("0");
                else
                    return NodeValue.makeString("-0");
            }
            if ( Double.isNaN(dValue) )
                return NodeValue.makeString("NaN");
            if ( dValue == Double.POSITIVE_INFINITY )
                return NodeValue.makeString("INF");
            if ( dValue == Double.NEGATIVE_INFINITY )
                return NodeValue.makeString("-INF");

            if ( inSmallAboluteRange(dValue)) {
                // Convert to decimal
                BigDecimal bd = BigDecimal.valueOf(dValue);
                return NodeValue.makeString(XSDFuncOp.canonicalDecimalStrNoIntegerDot(bd));
            }
            // Canonical lexical.
            return castByLex(nv, castType);
        }

        // isTemporal -- no correction necessary
        // isDuration -- no correction necessary

//        if ( isTemporal(nv) ) {
//            // If ST is xs:dateTime, xs:date or xs:time, TV is the local value. The components of
//            // TV are individually cast to xs:string using the functions described in
//            // [casting-to-datetimes] and the results are concatenated together. The year
//            // component is cast to xs:string using eg:convertYearToString. The month, day, hour
//            // and minute components are cast to xs:string using eg:convertTo2CharString. The
//            // second component is cast to xs:string using eg:convertSecondsToString. The
//            // timezone component, if present, is cast to xs:string using eg:convertTZtoString.
//
//            // Note that the hours component of the resulting string will never be "24". Midnight
//            // is always represented as "00:00:00".
//        }
//
//        if ( isDuration(nv) ) {
//            // If ST is xs:yearMonthDuration or xs:dayTimeDuration, TV is the
//            // canonical representation of SV as defined in [Schema 1.1 Part 2].
//            //
//            // If ST is xs:duration then let SYM be SV cast as xs:yearMonthDuration,
//            // and let SDT be SV cast as xs:dayTimeDuration; Now, let the next
//            // intermediate value, TYM, be SYM cast as TT , and let TDT be SDT cast
//            // as TT . If TYM is "P0M", then TV is TDT. Otherwise, TYM and TDT are
//            // merged according to the following rules:
//            //
//            //    1. If TDT is "PT0S", then TV is TYM.
//            //
//            //    2. Otherwise, TV is the concatenation of all the characters in TYM
//            //       and all the characters except the first "P" and the optional
//            //       negative sign in TDT.
//        }
        return castByLex(nv, castType);
    }

    // Casting rules for double and floats.
    private static boolean inSmallAboluteRange(double d) {
        return ( d >= 0.000001d && d < 1000000d ) || ( d <= -0.000001d && d > -1000000d );
    }

    /** Test to see if a BuigDecimal is integer valued  */
    private static boolean isIntegerValue(BigDecimal bd) {
        return bd.signum() == 0 || bd.scale() <= 0 || bd.stripTrailingZeros().scale() <= 0;
      }

    /** Test to see if a {@link NodeValue} is a valid double value and is of datatype xsd:double. */
    private static boolean isDouble(NodeValue nv) {
        return nv.isDouble() && nv.getDatatypeURI().equals(XSDDatatype.XSDdouble.getURI());
    }

    /** Test to see if a {@link NodeValue} is a valid float value and is of datatype float. */
    private static boolean isFloat(NodeValue nv) {
        return nv.isFloat() && nv.getDatatypeURI().equals(XSDDatatype.XSDfloat.getURI());
    }

    /** Test to see if a {@link NodeValue} is a valid decimal value and is of datatype decimal. */
    private static boolean isDecimal(NodeValue nv) {
        return nv.isDecimal() && nv.getDatatypeURI().equals(XSDDatatype.XSDdecimal.getURI());
    }

    /** Test to see if a {@link NodeValue} is a valid numeric value. */
    private static boolean isNumeric(NodeValue nv) {
        return nv.isNumber();
    }

    /** Test to see if a {@link NodeValue} is a temporal includes Gregorian.. */
    private static boolean isTemporal(NodeValue nv) {
        return nv.hasDateTime();
    }

    /** Test to see if a {@link NodeValue} is a duration */
    private static boolean isDuration(NodeValue nv) {
        return nv.isDuration();
    }

    /** Test to see if a {@link NodeValue} is a valid numeric value. */
    private static boolean isBoolean(NodeValue nv) {
        return nv.isBoolean();
    }

    /** Presentation form of an XSD datatype URI */
    private static String xsdName(XSDDatatype datatype) {
        return datatype.getURI().replaceAll(XSDDatatype.XSD+"#", "xsd:");
    }

    private static ExprException exception(NodeValue nv, XSDDatatype dt) {
        return exception("Invalid cast: "+nv+" -> "+xsdName(dt));
    }

    private static ExprException exception(String msg) {
        return new ExprEvalTypeException(msg);
    }

    // Cast by lexical form with checking.
    private static NodeValue castByLex(NodeValue nv, XSDDatatype castType) {
        String lex = nv.getNode().getLiteralLexicalForm();
        return castByLex(lex, castType);
    }

    // Cast by lexical form with checking.
    private static NodeValue castByLex(String lex, XSDDatatype castType) {
        if ( ! castType.isValid(lex) )
            throw exception("Invalid lexical form: '"+lex+"' for "+castType.getURI());
        if ( castType instanceof XSDBaseNumericType ||
            castType.equals(XSDDatatype.XSDfloat) ||
            castType.equals(XSDDatatype.XSDdouble) ||
            castType.equals(XSDDatatype.XSDboolean) ||
            castType instanceof XSDAbstractDateTimeType )   // Includes durations, and Gregorian
        {
            // More helpful error message.
            if ( lex.startsWith(" ") || lex.endsWith(" ") )
                throw exception("Not a valid literal form (has whitespace): '"+lex+"'");
        }
        NodeValue nv2 = NodeValue.makeNode(lex, castType);
        RDFDatatype dt = nv2.getNode().getLiteralDatatype();
        if ( castType.equals(dt) )
            return nv2;
        throw exception("Can not cast '"+lex+"' to a "+castType);
    }

    // Known to work casts.  No checking.
    private static NodeValue cast$(String lex, XSDDatatype castType) {
        return NodeValue.makeNode(lex, castType);
    }

    // Return the integer lexical form for a double
    private static String doubleToIntegerLex(double d) {
        // Fast path
        long x = Math.round(d);
        if ( x == d && x != Long.MAX_VALUE && x != Long.MIN_VALUE )
            return Long.toString(x);
        // Discard fractional part.
        String lex = BigDecimal.valueOf(d).toPlainString();
        int i = lex.indexOf('.');
        if ( i >= 0 )
            lex = lex.substring(0, i);
        return lex;
    }

    // Return the decimal lexical form for a double value.
    // Java big decimal allows "E" forms, XSD does not.
    // For RDF purposes, return ".0" forms (which are
    // short-forms in Turtle and SPARQL).
    private static String doubleToDecimalLex(double d) {
        // BigDecimal.valueOf(d) can lead to trailing zeros.
        String lex = BigDecimal.valueOf(d).toPlainString();
        // Clean the string.
        int i = lex.indexOf('.');
        if ( i < 0 )
            return lex+".0";
        while((i < lex.length()-2) && lex.endsWith("0"))
            lex = lex.substring(0,  lex.length()-1);
        return lex;
    }

    private static String decimalToIntegerLex(BigDecimal d) {
        String lex = d.toPlainString();
        // Clean the string.
        int i = lex.indexOf('.');
        if ( i >= 0 )
            lex = lex.substring(0, i);
        return lex;
    }
}

