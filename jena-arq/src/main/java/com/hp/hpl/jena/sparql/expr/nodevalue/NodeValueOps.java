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

package com.hp.hpl.jena.sparql.expr.nodevalue;

import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSD ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_DATE ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_DATETIME ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_DURATION ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_NUM ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_STRING ;
import static com.hp.hpl.jena.sparql.expr.ValueSpaceClassification.VSPACE_TIME ;
import static javax.xml.datatype.DatatypeConstants.FIELD_UNDEFINED ;

import java.math.BigDecimal ;
import java.util.GregorianCalendar ;

import javax.xml.datatype.Duration ;
import javax.xml.datatype.XMLGregorianCalendar ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.expr.ExprEvalTypeException ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.ValueSpaceClassification ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;

/** The code parts of arithmetic opperations on Nodevlaues.
 */
public class NodeValueOps
{
    /* Date/time/duration coverage
     * add, subtract (duration, duration)
     * 
(xsd:duration, xsd:duration) -> xsd:duration
        10.6.1 op:add-yearMonthDurations
        10.6.2 op:subtract-yearMonthDurations
(xsd:duration, xsd:duration) -> xsd:duration
        10.6.6 op:add-dayTimeDurations
        10.6.7 op:subtract-dayTimeDurations
** Java has general duration subtract 

    * Subtract two date/times

(xsd:dateTime, xsd:dateTime) -> xsd:duration
        10.8.1 op:subtract-dateTimes -> xsd:dayTimeDuration
(xsd:date, xsd:date) -> xsd:duration
        10.8.2 op:subtract-dates
(xsd:time, xsd:time) -> xsd:duration
        10.8.3 op:subtract-times

    * Date/time and duration
** Java has general duration subtract (error needed?)

(xsd:dateTime, xsd:duration) -> xsd:dateTime
        10.8.4 op:add-yearMonthDuration-to-dateTime
        10.8.5 op:add-dayTimeDuration-to-dateTime
        10.8.6 op:subtract-yearMonthDuration-from-dateTime
        10.8.7 op:subtract-dayTimeDuration-from-dateTime

(xsd:date, xsd:duration) -> xsd:date
        10.8.8 op:add-yearMonthDuration-to-date
        10.8.9 op:add-dayTimeDuration-to-date
        10.8.10 op:subtract-yearMonthDuration-from-date
        10.8.11 op:subtract-dayTimeDuration-from-date

(xsd:time, xsd:duration) -> xsd:time
        10.8.12 op:add-dayTimeDuration-to-time
        10.8.13 op:subtract-dayTimeDuration-from-time      

(xsd:duration, xsd:double) -> xsd:duration
        10.6.8 op:multiply-dayTimeDuration
        */
    
        /* Missing:
(xsd:duration, xsd:double) -> xsd:duration
        10.6.9 op:divide-dayTimeDuration
        
(xsd:duration, xsd:duration) -> xsd:decimal
        10.6.10 op:divide-dayTimeDuration-by-dayTimeDuration 
         */
    /* Notes:
     * Does not consider whether a duration is dayTime or yearMonth
     * for addition and subtraction.
     * As per the Java (Xerces) implementation, it just work on durations.    
     */
    
    // Until part of Jena datatypes.
    private static final String dtXSDdateTimeStamp      = XSD+"#dateTimeStamp" ;
    private static final String dtXSDdayTimeDuration    = XSD+"#dayTimeDuration" ;
    private static final String dtXSDyearMonthDuration  = XSD+"#yearMonthDuration" ;

    public static NodeValue additionNV(NodeValue nv1, NodeValue nv2)
    {
        ValueSpaceClassification vs1 = nv1.getValueSpace() ;
        ValueSpaceClassification vs2 = nv2.getValueSpace() ;
        
        if ( vs1.equals(VSPACE_NUM) && vs2.equals(VSPACE_NUM) )
            return XSDFuncOp.numAdd(nv1, nv2) ;
        
        if ( vs1.equals(VSPACE_STRING) && vs2.equals(VSPACE_STRING) )
            return NodeValue.makeString(nv1.asString()+nv2.asString()) ;
        if ( vs1.equals(VSPACE_DURATION) && vs2.equals(VSPACE_DURATION) )
        {
            // A lot of testing to keep it as derived types.
            boolean isDTDur = dtXSDdayTimeDuration.equals(nv1.getDatatypeURI()) && 
                              dtXSDdayTimeDuration.equals(nv2.getDatatypeURI()) ;
            boolean isYMDur = dtXSDyearMonthDuration.equals(nv1.getDatatypeURI()) && 
                              dtXSDyearMonthDuration.equals(nv2.getDatatypeURI()) ;
            Duration d3 = nv1.getDuration().add(nv2.getDuration()) ;
            String lex = d3.toString() ;
            Node n ;
            if ( isDTDur )
                n = NodeFactoryExtra.createLiteralNode(lex, null, dtXSDdayTimeDuration) ;
            else if ( isYMDur )
                n = NodeFactoryExtra.createLiteralNode(lex, null, dtXSDyearMonthDuration) ;
            else
                n = com.hp.hpl.jena.graph.NodeFactory.createLiteral(lex, XSDDatatype.XSDduration) ;
            return NodeValue.makeNodeDuration(d3, n) ;
        }
        
        // Loose style. Add any duration to any date or time value.
        if ( vs1.equals(VSPACE_DATETIME) && vs2.equals(VSPACE_DURATION) )
        {
            XMLGregorianCalendar cal = nv1.getDateTime() ;
            XMLGregorianCalendar result = xsd_add(cal, nv2.getDuration()) ;
            NodeValue r = NodeValue.makeDateTime(result) ;
            return r ;
        }
        // Loose style. Add any duration to any date or time value.
        if ( vs1.equals(VSPACE_DATE) && vs2.equals(VSPACE_DURATION) )
        {
            XMLGregorianCalendar cal = nv1.getDateTime() ;
            XMLGregorianCalendar result = xsd_add(cal, nv2.getDuration()) ;
            NodeValue r = NodeValue.makeDate(result) ;
            return r ;
        }
        // Loose style. Add any duration to any date or time value.
        if ( vs1.equals(VSPACE_TIME) && vs2.equals(VSPACE_DURATION) )
        {
            // ONLY dayTime.
            XMLGregorianCalendar cal = nv1.getDateTime() ;
            XMLGregorianCalendar result = xsd_add(cal, nv2.getDuration()) ;
            NodeValue r = NodeValue.makeNode(result.toXMLFormat(), XSDDatatype.XSDtime) ; 
            return r ;
        }
        
        if ( isDT(vs2) && vs1.equals(VSPACE_DURATION) )
            // Carefully ...
            return additionNV(nv2, nv1) ;
        throw new ExprEvalTypeException("Operator '+' : Undefined addition: "+nv1+" and "+nv2) ; 
    }

    // NodeFunctions
    public static NodeValue subtractionNV(NodeValue nv1, NodeValue nv2)
    {
        ValueSpaceClassification vs1 = nv1.getValueSpace() ;
        ValueSpaceClassification vs2 = nv2.getValueSpace() ;
        
        if ( vs1.equals(VSPACE_NUM) && vs2.equals(VSPACE_NUM) )
            return XSDFuncOp.numSubtract(nv1, nv2) ;
        
        if ( vs1.equals(VSPACE_DURATION) && vs2.equals(VSPACE_DURATION) )
        {
         // A lot of testing to keep it as derived types.
            boolean isDTDur = dtXSDdayTimeDuration.equals(nv1.getDatatypeURI()) && 
                              dtXSDdayTimeDuration.equals(nv2.getDatatypeURI()) ;
            boolean isYMDur = dtXSDyearMonthDuration.equals(nv1.getDatatypeURI()) && 
                              dtXSDyearMonthDuration.equals(nv2.getDatatypeURI()) ;
            Duration d3 = nv1.getDuration().subtract(nv2.getDuration()) ;
            String lex = d3.toString() ;
            Node n ;
            if ( isDTDur )
                n = NodeFactoryExtra.createLiteralNode(lex, null, dtXSDdayTimeDuration) ;
            else if ( isYMDur )
                n = NodeFactoryExtra.createLiteralNode(lex, null, dtXSDyearMonthDuration) ;
            else
                n = com.hp.hpl.jena.graph.NodeFactory.createLiteral(lex, XSDDatatype.XSDduration) ;
            return NodeValue.makeNodeDuration(d3, n) ;
        }
        
        if ( isDT(vs1) && isDT(vs2) )
        {
            XMLGregorianCalendar cal1 = nv1.getDateTime() ;
            XMLGregorianCalendar cal2 = nv2.getDateTime() ;
            boolean isDef1 = ( cal1.getTimezone() == FIELD_UNDEFINED ) ; 
            boolean isDef2 = ( cal2.getTimezone() == FIELD_UNDEFINED ) ;
            if ( ( isDef1 && !isDef2 ) || ( !isDef1 && isDef2 ) )
                throw new ExprEvalTypeException("Operator '-': can't substract timezone/non-timezone values") ;
            // Inspect duration and force to better type? xsd:dayTimeDuration
            return NodeValue.makeDuration(xsd_substract(cal1, cal2));
        }
        
        // Loose style. Subtract any duration to any date or time value.
        if ( vs1.equals(VSPACE_DATETIME) && vs2.equals(VSPACE_DURATION) )
        {
            XMLGregorianCalendar cal = nv1.getDateTime() ;
            // add-negation
            XMLGregorianCalendar result = xsd_subtract(cal, nv2.getDuration()) ;
            NodeValue r = NodeValue.makeDateTime(result) ;
            return r ;
        }
        if ( vs1.equals(VSPACE_DATE) && vs2.equals(VSPACE_DURATION) )
        {
            XMLGregorianCalendar cal = nv1.getDateTime() ;
            // add-negation
            XMLGregorianCalendar result = xsd_subtract(cal, nv2.getDuration()) ;
            NodeValue r = NodeValue.makeDate(result) ;
            return r ;
        }
        if ( vs1.equals(VSPACE_TIME) && vs2.equals(VSPACE_DURATION) )
        {
            XMLGregorianCalendar cal = nv1.getDateTime() ;
            // add-negation
            XMLGregorianCalendar result = xsd_subtract(cal, nv2.getDuration()) ;
            NodeValue r = NodeValue.makeNode(result.toXMLFormat(), XSDDatatype.XSDtime) ; 
            return r ;
        }
        
        throw new ExprEvalTypeException("Operator '-' : Undefined subtraction: "+nv1+" and "+nv2) ; 
    }

    public static NodeValue multiplicationNV(NodeValue nv1, NodeValue nv2)
    {
        ValueSpaceClassification vs1 = nv1.getValueSpace() ;
        ValueSpaceClassification vs2 = nv2.getValueSpace() ;
        
        if ( vs1.equals(VSPACE_NUM) && vs2.equals(VSPACE_NUM) )
            return XSDFuncOp.numMultiply(nv1, nv2) ;

        if ( vs1.equals(VSPACE_DURATION) && vs2.equals(VSPACE_NUM) )
        {
            // ONLY defined for dayTime.
            Duration dur = nv1.getDuration() ;
            boolean valid = XSDFuncOp.isDayTime(dur) ;
            if ( ! valid )
                throw new ExprEvalTypeException("Operator '*': only dayTime duration.  Got: "+nv1) ;
            BigDecimal dec = nv2.getDecimal() ;
            Duration r = dur.multiply(dec) ;
            Node n = NodeFactoryExtra.createLiteralNode(r.toString(), null, dtXSDdayTimeDuration) ;
            return NodeValue.makeNodeDuration(r, n) ; 
        }
        throw new ExprEvalTypeException("Operator '*' : Undefined multiply: "+nv1+" and "+nv2) ; 
    }
    
    public static NodeValue divisionNV(NodeValue nv1, NodeValue nv2)
    {
        ValueSpaceClassification vs1 = nv1.getValueSpace() ;
        ValueSpaceClassification vs2 = nv2.getValueSpace() ;
        
        if ( vs1.equals(VSPACE_NUM) && vs2.equals(VSPACE_NUM) )
            return XSDFuncOp.numDivide(nv1, nv2) ;

        throw new ExprEvalTypeException("Operator '/' : Undefined division: "+nv1+" and "+nv2) ; 
    }


    private static boolean isDT(ValueSpaceClassification vs)
    {
        switch (vs)
        {
            case VSPACE_DATETIME: 
            case VSPACE_DATE:
            case VSPACE_TIME:
            case VSPACE_G_YEAR:
            case VSPACE_G_YEARMONTH: 
            case VSPACE_G_MONTHDAY:
            case VSPACE_G_MONTH: 
            case VSPACE_G_DAY:
                return true ;
            default:
                return false ;
        }
    }
    
    private static XMLGregorianCalendar xsd_add(XMLGregorianCalendar cal, Duration duration)
    {
        //if ( ! isYearMonth(duration) && ! isDayTime(duration) )

        XMLGregorianCalendar result = (XMLGregorianCalendar)cal.clone() ;
        result.add(duration) ;
        return result ;
    }

    private static XMLGregorianCalendar xsd_subtract(XMLGregorianCalendar cal, Duration duration)
    {
        return xsd_add(cal, duration.negate()) ;
    }

    private static Duration xsd_substract(XMLGregorianCalendar cal1, XMLGregorianCalendar cal2)
    {
        GregorianCalendar gcal1 = cal1.toGregorianCalendar() ;
        GregorianCalendar gcal2 = cal2.toGregorianCalendar() ;
        long x1 = gcal1.getTimeInMillis() ;
        long x2 = gcal2.getTimeInMillis() ;
        return NodeValue.xmlDatatypeFactory.newDuration(x1-x2) ;
    }

    
}

