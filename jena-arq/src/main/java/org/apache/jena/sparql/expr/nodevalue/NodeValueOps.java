/**
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

import static javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED;
import static org.apache.jena.sparql.expr.ValueSpace.*;

import java.math.BigDecimal;
import java.util.GregorianCalendar;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.ValueSpace;

/** The code parts of arithmetic operations on {@link NodeValue}s.
 */
public class NodeValueOps
{
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
        return 12*dur.getYears() + dur.getMonths();
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
            default:
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
}
