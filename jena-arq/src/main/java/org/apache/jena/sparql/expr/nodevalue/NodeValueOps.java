/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.expr.nodevalue;

import static javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED;
import static org.apache.jena.sparql.expr.ValueSpace.*;

import java.math.BigDecimal;
import java.util.GregorianCalendar;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.TextDirection;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprEvalTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.ValueSpace;
import org.apache.jena.vocabulary.RDF;

/**
 * Operations relating to {@link NodeValue NodeValues}.
 * <ul>
 * <li>The code parts of arithmetic operations on {@link NodeValue}s.
 * <li>Library code such as expression string argument testing
 * </ul>
 * <p>
 * This class is not considered to be part of the ARQ API.
 */
public class NodeValueOps {
    /*
     * Notes:
     * Does not consider whether a duration is dayTime or yearMonth for
     * multiplication and division where the operation can changes the result type.
     * As per the Java (Xerces) implementation, it just work on durations.
     */

    //private static final String dtXSDdateTimeStamp      = XSDDatatype.XSDdateTimeStamp.getURI();
    private static final String dtXSDdayTimeDuration    = XSDDatatype.XSDdayTimeDuration.getURI();
    private static final String dtXSDyearMonthDuration  = XSDDatatype.XSDyearMonthDuration.getURI();

    /** Add two {@link NodeValue NodeValues}, with all the extra datatypes and extensions supported. */
    public static NodeValue additionNV(NodeValue nv1, NodeValue nv2) {
        ValueSpace vs1 = nv1.getValueSpace();
        ValueSpace vs2 = nv2.getValueSpace();

        if ( vs1.equals(VSPACE_NUM) && vs2.equals(VSPACE_NUM) )
            return XSDFuncOp.numAdd(nv1, nv2);

        if ( vs1.equals(VSPACE_STRING) && vs2.equals(VSPACE_STRING) )
            return NodeValue.makeString(nv1.asString() + nv2.asString());
        if ( vs1.equals(VSPACE_DURATION) && vs2.equals(VSPACE_DURATION) ) {
            // A lot of testing to keep it as derived types.
            boolean isDTDur = dtXSDdayTimeDuration.equals(nv1.getDatatypeURI()) && dtXSDdayTimeDuration.equals(nv2.getDatatypeURI());
            boolean isYMDur = dtXSDyearMonthDuration.equals(nv1.getDatatypeURI()) && dtXSDyearMonthDuration.equals(nv2.getDatatypeURI());
            Duration d3 = nv1.getDuration().add(nv2.getDuration());
            String lex = d3.toString();
            Node n;
            if ( isDTDur )
                n = NodeFactory.createLiteralDT(lex, XSDDatatype.XSDdayTimeDuration);
            else if ( isYMDur )
                n = NodeFactory.createLiteralDT(lex, XSDDatatype.XSDyearMonthDuration);
            else
                n = NodeFactory.createLiteralDT(lex, XSDDatatype.XSDduration);
            return NodeValue.makeNodeDuration(d3, n);
        }

        // Loose style. Add any duration to any date or time value.
        if ( vs1.equals(VSPACE_DATETIME) && vs2.equals(VSPACE_DURATION) ) {
            XMLGregorianCalendar cal = nv1.getDateTime();
            XMLGregorianCalendar result = xsd_add(cal, nv2.getDuration());
            NodeValue r = NodeValue.makeDateTime(result);
            return r;
        }
        // Loose style. Add any duration to any date or time value.
        if ( vs1.equals(VSPACE_DATE) && vs2.equals(VSPACE_DURATION) ) {
            XMLGregorianCalendar cal = nv1.getDateTime();
            XMLGregorianCalendar result = xsd_add(cal, nv2.getDuration());
            NodeValue r = NodeValue.makeDate(result);
            return r;
        }
        // Loose style. Add any duration to any date or time value.
        if ( vs1.equals(VSPACE_TIME) && vs2.equals(VSPACE_DURATION) ) {
            // ONLY dayTime.
            XMLGregorianCalendar cal = nv1.getDateTime();
            XMLGregorianCalendar result = xsd_add(cal, nv2.getDuration());
            NodeValue r = NodeValue.makeNode(result.toXMLFormat(), XSDDatatype.XSDtime);
            return r;
        }
        throw new ExprEvalTypeException("Operator '+' : Undefined addition: " + nv1 + " and " + nv2);
    }

    /** Subtract two {@link NodeValue NodeValues}, with all the extra datatypes and extensions supported. */
    public static NodeValue subtractionNV(NodeValue nv1, NodeValue nv2) {
        ValueSpace vs1 = nv1.getValueSpace();
        ValueSpace vs2 = nv2.getValueSpace();

        if ( vs1.equals(VSPACE_NUM) && vs2.equals(VSPACE_NUM) )
            return XSDFuncOp.numSubtract(nv1, nv2);

        if ( vs1.equals(VSPACE_DURATION) && vs2.equals(VSPACE_DURATION) ) {
            // A lot of testing to keep it as derived types.
            boolean isDTDur = dtXSDdayTimeDuration.equals(nv1.getDatatypeURI()) && dtXSDdayTimeDuration.equals(nv2.getDatatypeURI());
            boolean isYMDur = dtXSDyearMonthDuration.equals(nv1.getDatatypeURI()) && dtXSDyearMonthDuration.equals(nv2.getDatatypeURI());
            Duration d3 = nv1.getDuration().subtract(nv2.getDuration());
            String lex = d3.toString();
            Node n;
            if ( isDTDur )
                n = NodeFactory.createLiteralDT(lex, XSDDatatype.XSDdayTimeDuration);
            else if ( isYMDur )
                n = NodeFactory.createLiteralDT(lex, XSDDatatype.XSDyearMonthDuration);
            else
                n = org.apache.jena.graph.NodeFactory.createLiteralDT(lex, XSDDatatype.XSDduration);
            return NodeValue.makeNodeDuration(d3, n);
        }

        if ( isDT(vs1) && isDT(vs2) ) {
            XMLGregorianCalendar cal1 = nv1.getDateTime();
            XMLGregorianCalendar cal2 = nv2.getDateTime();
            boolean isDef1 = (cal1.getTimezone() == FIELD_UNDEFINED);
            boolean isDef2 = (cal2.getTimezone() == FIELD_UNDEFINED);
            if ( (isDef1 && !isDef2) || (!isDef1 && isDef2) )
                throw new ExprEvalTypeException("Operator '-': can't substract timezone/non-timezone values");
            // Inspect duration and force to better type? xsd:dayTimeDuration
            return NodeValue.makeDuration(xsd_substract(cal1, cal2));
        }

        // Loose style. Subtract any duration to any date or time value.
        if ( vs1.equals(VSPACE_DATETIME) && vs2.equals(VSPACE_DURATION) ) {
            XMLGregorianCalendar cal = nv1.getDateTime();
            // add-negation
            XMLGregorianCalendar result = xsd_subtract(cal, nv2.getDuration());
            NodeValue r = NodeValue.makeDateTime(result);
            return r;
        }
        if ( vs1.equals(VSPACE_DATE) && vs2.equals(VSPACE_DURATION) ) {
            XMLGregorianCalendar cal = nv1.getDateTime();
            // add-negation
            XMLGregorianCalendar result = xsd_subtract(cal, nv2.getDuration());
            NodeValue r = NodeValue.makeDate(result);
            return r;
        }
        if ( vs1.equals(VSPACE_TIME) && vs2.equals(VSPACE_DURATION) ) {
            XMLGregorianCalendar cal = nv1.getDateTime();
            // add-negation
            XMLGregorianCalendar result = xsd_subtract(cal, nv2.getDuration());
            NodeValue r = NodeValue.makeNode(result.toXMLFormat(), XSDDatatype.XSDtime);
            return r;
        }

        throw new ExprEvalTypeException("Operator '-' : Undefined subtraction: " + nv1 + " and " + nv2);
    }

    /** Multiple two {@link NodeValue NodeValues}, with all the extra datatypes and extensions supported. */
    public static NodeValue multiplicationNV(NodeValue nv1, NodeValue nv2) {
        ValueSpace vs1 = nv1.getValueSpace();
        ValueSpace vs2 = nv2.getValueSpace();

        if ( vs1.equals(VSPACE_NUM) && vs2.equals(VSPACE_NUM) )
            return XSDFuncOp.numMultiply(nv1, nv2);

        if ( vs1.equals(VSPACE_DURATION) && vs2.equals(VSPACE_NUM) ) {
            // ONLY defined for dayTime.
            Duration dur = nv1.getDuration();
            boolean valid = XSDFuncOp.isDayTime(dur);
            if ( !valid )
                throw new ExprEvalTypeException("Operator '*': only dayTime duration.  Got: " + nv1);
            BigDecimal dec = nv2.getDecimal();
            Duration r = dur.multiply(dec);
            Node n = NodeFactory.createLiteralDT(r.toString(), XSDDatatype.XSDduration);
            return NodeValue.makeNodeDuration(r, n);
        }
        throw new ExprEvalTypeException("Operator '*' : Undefined multiply: " + nv1 + " and " + nv2);
    }

    /** Divide two {@link NodeValue NodeValues}, with all the extra datatypes and extensions supported. */
    public static NodeValue divisionNV(NodeValue nv1, NodeValue nv2) {
        ValueSpace vs1 = nv1.getValueSpace();
        ValueSpace vs2 = nv2.getValueSpace();

        if ( vs1.equals(VSPACE_NUM) && vs2.equals(VSPACE_NUM) )
            return XSDFuncOp.numDivide(nv1, nv2);

        // Duration divided by number
        if ( vs1.equals(VSPACE_DURATION) && vs2.equals(VSPACE_NUM) ) {
            Duration dur = nv1.getDuration();
            // Multiply by 1/number.
            BigDecimal dec = nv2.getDecimal();
            if ( dec.equals(BigDecimal.ZERO) )
                throw new ExprEvalTypeException("Divide by zero in duration division");

            BigDecimal dec1 = BigDecimal.ONE.divide(dec);
            Duration r = dur.multiply(dec1);
            // Should normalize but not Duration.normalizeWith for a general duration.
            // DT or YM specific normalization could be done. e.g. days can go over 31.
            Node n = NodeFactory.createLiteralDT(r.toString(), XSDDatatype.XSDduration);
            return NodeValue.makeNodeDuration(r, n);
        }

        // Duration divided by duration
        if ( vs1.equals(VSPACE_DURATION) && vs2.equals(VSPACE_DURATION) ) {
            // Ratio as a BigDecimal
            Duration dur1 = nv1.getDuration();
            Duration dur2 = nv2.getDuration();
            if ( XSDFuncOp.isDayTime(dur1) && XSDFuncOp.isDayTime(dur2) ) {
                // In seconds. Ignores fractional seconds.
                double x1 = durationDayTimeAsSeconds(dur1);
                double x2 = durationDayTimeAsSeconds(dur2);
                if ( x2 == 0 )
                    throw new ExprEvalTypeException("Divide by zero duration in xsd:dayTimeDuration division");
                return NodeValue.makeDecimal(x1/x2);
            }
            if ( XSDFuncOp.isYearMonth(dur1) && XSDFuncOp.isYearMonth(dur2) ) {
                // In months
                double x1 = durationYearMonthAsMonths(dur1);
                double x2 = durationYearMonthAsMonths(dur2);
                if ( x2 == 0 )
                    throw new ExprEvalTypeException("Divide by zero duration in xsd:YearMonthDuration division");
                return NodeValue.makeDecimal(x1/x2);
            }
            throw new ExprEvalTypeException("Durations not both day-time nor year-month: " + nv1 + " and " + nv2);
        }

        throw new ExprEvalTypeException("Operator '/' : Undefined division: " + nv1 + " and " + nv2);
    }

    private static double durationYearMonthAsMonths(Duration dur) {
        return 12 * dur.getYears() + dur.getMonths();
    }

    private static double durationDayTimeAsSeconds(Duration dur) {
        double x = dur.getDays();
        x = x * 24;
        x = x + dur.getHours();
        x = x * 60;
        x = x + dur.getMinutes();
        x = x * 60;
        x = x + dur.getSeconds();
        return x;
    }

    private static boolean isDT(ValueSpace vs) {
        switch (vs) {
            case VSPACE_DATETIME:
            case VSPACE_DATE:
            case VSPACE_TIME:
//            case VSPACE_G_YEAR:
//            case VSPACE_G_YEARMONTH:
//            case VSPACE_G_MONTHDAY:
//            case VSPACE_G_MONTH:
//            case VSPACE_G_DAY:
                return true;
            default :
                return false;
        }
    }

    private static XMLGregorianCalendar xsd_add(XMLGregorianCalendar cal, Duration duration) {
        // if ( ! isYearMonth(duration) && ! isDayTime(duration) )

        XMLGregorianCalendar result = (XMLGregorianCalendar)cal.clone();
        result.add(duration);
        return result;
    }

    private static XMLGregorianCalendar xsd_subtract(XMLGregorianCalendar cal, Duration duration) {
        return xsd_add(cal, duration.negate());
    }

    private static Duration xsd_substract(XMLGregorianCalendar cal1, XMLGregorianCalendar cal2) {
        GregorianCalendar gcal1 = cal1.toGregorianCalendar();
        GregorianCalendar gcal2 = cal2.toGregorianCalendar();
        long x1 = gcal1.getTimeInMillis();
        long x2 = gcal2.getTimeInMillis();
        return NodeValue.xmlDatatypeFactory.newDuration(x1 - x2);
    }

    /**
     * check and get a string (may be a simple literal, literal with language tag or
     * an XSD string).
     */
    public static Node checkAndGetStringLiteral(String label, NodeValue nv) {
        Node n = nv.asNode();
        if ( !n.isLiteral() )
            throw new ExprEvalException(label + ": Not a literal: " + nv);

        if ( nv.isString() )
            // Includes derived types of xsd:string.
            return n;

        RDFDatatype dt = n.getLiteralDatatype();
        if ( ! RDF.dtLangString.equals(dt) && ! RDF.dtDirLangString.equals(dt) )
            throw new ExprEvalException(label + ": Not a string literal: " + nv);

        // Check for malformed:
        // e.g. "abc"^^rdf:langString, and "abc"^^rdf:dirLangString

        // Must have a language.
        String lang = n.getLiteralLanguage();
        if ( lang == null || lang.isEmpty() )
            throw new ExprEvalException(label + ": Not a string literal (no langtag): " + nv);
        if ( RDF.dtLangString.equals(dt) ) {
            // Must not have a text direction
            if ( n.getLiteralBaseDirection() != null )
                throw new ExprEvalException(label + ": Not a string literal (rdf:langString + text direction): " + nv);
            return n;
        }
        if ( RDF.dtDirLangString.equals(dt) ) {
            // Must have a text direction
            if ( n.getLiteralBaseDirection() == null )
                throw new ExprEvalException(label + ": Not a string literal (no text direction): " + nv);
            return n;
        }

        // Should not get here.
        throw new ExprEvalException(label + ": Not a string literal: " + nv);
    }

    /**
     * Check for string operations with primary first arg and second arg (e.g.
     * CONTAINS). The arguments are not used in the same way and the check operation
     * is not symmetric.
     * <ul>
     * <li>"abc"@en is compatible with "abc"
     * <li>"abc"@en--ltr is compatible with "abc"
     * <li>"abc" is NOT compatible with "abc"@en
     * <li>"abc"@en--ltr is NOT compatible with "abc"@en
     * </ul>
     */
    public static void checkTwoArgumentStringLiterals(String label, NodeValue arg1, NodeValue arg2) {
        /* Compatibility of two arguments:
         *    The arguments are both xsd:string
         *    The arguments are rdf:langString with identical language tags
         *    The arguments are rdf:dirLangString with identical language tags and text direction
         *    The first argument a string literal (rdf:langString, rdf:dirLangString) and the second argument is an xsd:string
         *
         * which simplifies to
         *     Both arguments are string literals
         *     The second argument is an xsd:string.
         *     The first and second arguments have the same lang and text direction.
         */

        // Common case
        if ( arg1.isString() && arg2.isString() )
            // Includes derived datatypes of xsd:string.
            return;

        // Robust checking.
        Node n1 = checkAndGetStringLiteral(label, arg1);
        Node n2 = checkAndGetStringLiteral(label, arg2);
        if ( arg2.isString() )
            // args1 is some kind of string literal.
            return;
        // same lane, same text direction.
        String lang1 = n1.getLiteralLanguage();
        String lang2 = n2.getLiteralLanguage();
        if ( lang1 == null )
            lang1 = "";
        if ( lang2 == null )
            lang2 = "";

        if ( ! Lib.equalsOrNulls(lang1, lang2) )
            // Different languages.
            throw new ExprEvalException(label + ": Incompatible: " + arg1 + " and " + arg2);

        TextDirection textDir1 = n1.getLiteralBaseDirection();
        TextDirection textDir2 = n2.getLiteralBaseDirection();
        if ( ! Lib.equalsOrNulls(textDir1, textDir2) )
            throw new ExprEvalException(label + ": Incompatible: " + arg1 + " and " + arg2);
    }
}
