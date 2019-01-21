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

package org.apache.jena.sparql.function;

import java.math.BigDecimal ;
import java.math.BigInteger ;
import java.util.Objects ;

import javax.xml.datatype.DatatypeConstants ;
import javax.xml.datatype.Duration ;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.datatypes.xsd.impl.XSDAbstractDateTimeType ;
import org.apache.jena.datatypes.xsd.impl.XSDBaseNumericType ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.expr.ExprEvalException ;
import org.apache.jena.sparql.expr.ExprEvalTypeException ;
import org.apache.jena.sparql.expr.ExprException ;
import org.apache.jena.sparql.expr.NodeValue ;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp ;

public class CastXSD extends FunctionBase1 implements FunctionFactory {
    
    protected final XSDDatatype castType ;
    
    public CastXSD(XSDDatatype dt)
    {
        this.castType = dt ; 
    }
    
    @Override
    public Function create(String uri)
    {        
        return this ;
    }
    
    @Override
    public NodeValue exec(NodeValue v)
    {
        return cast(v, castType) ;
    }

    
    private static boolean isTemporalDatatype(XSDDatatype datatype) {
        return 
            datatype.equals(XSDDatatype.XSDdateTime) ||
            datatype.equals(XSDDatatype.XSDtime) ||
            datatype.equals(XSDDatatype.XSDdate) ||
            datatype.equals(XSDDatatype.XSDgYear) ||
            datatype.equals(XSDDatatype.XSDgYearMonth) ||
            datatype.equals(XSDDatatype.XSDgMonth) ||
            datatype.equals(XSDDatatype.XSDgMonthDay) ||
            datatype.equals(XSDDatatype.XSDgDay) ;
    }
    
    private static boolean isDurationDatatype(XSDDatatype datatype) {
        return 
            datatype.equals(XSDDatatype.XSDduration) || 
            datatype.equals(XSDDatatype.XSDyearMonthDuration) ||
            datatype.equals(XSDDatatype.XSDdayTimeDuration ) ;
    }
    
    /** Cast a NodeValue to an XSD datatype.
     * This includes "by value" so 1e0 (an xsd:double) casts to 1 (an xsd:integer) 
     * @param nv
     * @param castType
     * @return NodeValue
     * @throws ExprEvalException
     */
    public static NodeValue cast(NodeValue nv, XSDDatatype castType) {
        // http://www.w3.org/TR/xpath-functions/#casting
        Node n = nv.asNode() ;
    
        if ( n.isBlank() )
            throw exception("Can't cast blank nodes: "+nv) ;
    
        if ( n.isURI() ) {
            if ( castType.equals(XSDDatatype.XSDstring) )
                return cast$(n.getURI(), castType) ;
            else
                throw exception("Can't cast URIs to "+castType.getURI()) ;
        }
    
        if ( ! n.isLiteral() )
            throw exception("Can't cast (not a literal, nor URI to string) "+nv+" : "+castType.getURI()) ;

        // It's a literal.
        
        // Cast to self but may be an  invalid lexical form.
        if ( Objects.equals(nv.getNode().getLiteralDatatype(), castType) ) {
            String lex = nv.getNode().getLiteralLexicalForm() ;
            if ( castType.isValid(lex) )
                return nv ;
            throw exception("Invalid lexical form for "+castType.getURI()) ;  
        }

        
        // Many casts can be done by testing the lexical is valid for the datatype.
        // But some cases need to consider values.
        //  e.g. boolean -> numeric , double -> integer (doubles have "e" in them)
        
        // To a temporal
        if ( isTemporalDatatype(castType) ) {
            return XSDFuncOp.dateTimeCast(nv, castType) ;
        }
        
        if ( isDurationDatatype(castType) ) {
            // Duration cast.
            // yearMonthDuration and TT is xs:dayTimeDuration -> 0.0S
            // xs:dayTimeDuration and TT is yearMonthDuration -> P0M
            
            if ( nv.isDuration() ) {
                Duration d = nv.getDuration() ;
                if ( castType.equals(XSDDatatype.XSDyearMonthDuration) ) {
                    
                    // Include xsd:duration only covering year-month.
                    if ( nv.isDayTimeDuration() )
                        return NodeValue.makeNode("P0M", castType) ;
                    
                    Duration d2 =  NodeValue.xmlDatatypeFactory.newDuration
                        (d.getSign()>=0,
                            (BigInteger)d.getField(DatatypeConstants.YEARS), (BigInteger)d.getField(DatatypeConstants.MONTHS), null,
                        null, null, null) ;
                    return NodeValue.makeNode(d2.toString(), castType) ;
                }
                if ( castType.equals(XSDDatatype.XSDdayTimeDuration) ) {
                    if ( nv.isYearMonthDuration() )
                        return NodeValue.makeNode("PT0S", castType) ;
                    Duration d2 =  NodeValue.xmlDatatypeFactory.newDuration
                        (d.getSign()>=0,
                        null, null, (BigInteger)d.getField(DatatypeConstants.DAYS),
                        (BigInteger)d.getField(DatatypeConstants.HOURS), (BigInteger)d.getField(DatatypeConstants.MINUTES), (BigDecimal)d.getField(DatatypeConstants.SECONDS)) ;
                    // return NodeValue.makeDuration(d2) ;
                    return NodeValue.makeNode(d2.toString(), castType) ;
                }
            }
        }

        // From number, can consider value.
        if ( nv.isNumber() ) {
            if ( castType.equals(XSDDatatype.XSDdecimal) ) {   
                // Number to decimal.
                if ( isDouble(nv) || isFloat(nv) ) {
                    // FP to decimal.
                    double d = nv.getDouble() ;
                    if ( Double.isNaN(d) )
                        throw exception("Can't cast NaN to xsd:decimal") ;
                    if ( Double.isInfinite(d) )
                        throw exception("Can't cast Inf or -Inf to xsd:decimal") ;
                    // BigDecimal.valueOf(d) can lead to trailing zeros
                    // BigDecimal.valueOf(d) goes via strings.
                    String lex = doubleToDecimalString(d) ;
                    return NodeValue.makeDecimal(lex) ;
                }
                // Integer, or derived type -> decimal. 
                return castByLex(nv, castType) ;
            }
            if ( XSDFuncOp.isIntegerType(castType) ) {
                // Number to integer
                if ( isDouble(nv) || isFloat(nv) ) {
                    // FP to integer
                    double d = nv.getDouble() ;
                    boolean isIntegerValue = ( Math.rint(d) == d ) ;
                    if ( isIntegerValue ) {
                        String lex = doubleIntegerToString(d) ;
                        if ( lex != null )
                            return castByLex(lex, castType) ;
                    }
                    throw exception(nv, castType) ;
                } else if ( isDecimal(nv) ) {
                    // Decimal to integer
                    BigDecimal bd = nv.getDecimal() ;
                    try {
                        // Exception on fraction. 
                        BigInteger bi = bd.toBigIntegerExact() ;
                        return castByLex(bi.toString(), castType) ;
                    } catch (ArithmeticException ex) {
                        throw new ExprEvalException("CastXSD: Not a valid cast: '"+nv+"'") ;
                    }
                } else {
                    // Integer derived type -> integer derived type.
                    return castByLex(nv, castType) ;
                }
            }
        }
    
        // Boolean -> xsd:
        if ( nv.isBoolean() ) { 
            boolean b = nv.getBoolean() ;
            // Boolean to boolean covered above.
            String lex ;
            if ( XSDDatatype.XSDfloat.equals(castType) || XSDDatatype.XSDdouble.equals(castType) )
                return cast$( ( b ? "1.0E0" : "0.0E0" ) , castType) ;
            else if ( XSDDatatype.XSDdecimal.equals(castType) )
                return cast$( ( b ? "1.0" : "0.0" ) , castType) ;
            else if ( XSDFuncOp.isIntegerType(castType)) 
                return cast$(  ( b ? "1" : "0" ) , castType ) ;
            else if ( XSDDatatype.XSDstring.equals(castType) ) 
                return cast$( nv.getNode().getLiteralLexicalForm(), castType ) ;
            throw exception("Can't cast xsd:boolean to "+castType) ;
        }

        // Try by lexical
        return castByLex(nv, castType) ;
    }

    /** Presentation form of an XSD datatype URI */
    private static String xsdName(String datatype) {
        return datatype.replaceAll(XSDDatatype.XSD+"#", "xsd:") ;
    }

    /** Test to see if a NodeValue is a valid double value and is of datatype xsd:double. */ 
    private static boolean isDouble(NodeValue nv) {
        return nv.isDouble() && nv.getDatatypeURI().equals(XSDDatatype.XSDdouble.getURI()) ;
    }

    /** Test to see if a NodeValue is a valid float value and is of datatype float. */ 
    private static boolean isFloat(NodeValue nv) {
        return nv.isFloat() && nv.getDatatypeURI().equals(XSDDatatype.XSDfloat.getURI()) ;
    }

    /** Test to see if a NodeValue is a valid decimal value and is of datatype decimal. */ 
    private static boolean isDecimal(NodeValue nv) {
        return nv.isDecimal() && nv.getDatatypeURI().equals(XSDDatatype.XSDdecimal.getURI()) ;
    }

    /** Test to see if a NodeValue is a valid numeric value. */ 
    private static boolean isNumeric(NodeValue nv) {
        return nv.isNumber() ;
    }

    private static ExprException exception(NodeValue nv, XSDDatatype dt) {
        return exception("Invalid cast: "+nv+" -> "+xsdName(dt.getURI())) ;
    }
    
    private static ExprException exception(String msg) {
        return new ExprEvalTypeException(msg) ;
    }

    // Cast by lexical form with checking.
    private static NodeValue castByLex(NodeValue nv, XSDDatatype castType) {
        String lex = nv.getNode().getLiteralLexicalForm() ;
        return castByLex(lex, castType) ;
    }

    // Cast by lexical form with checking.
    private static NodeValue castByLex(String lex, XSDDatatype castType) {
        if ( ! castType.isValid(lex) )
            throw exception("Invalid lexical form: '"+lex+"' for "+castType.getURI()) ;
        if ( castType instanceof XSDBaseNumericType || 
            castType.equals(XSDDatatype.XSDfloat) ||
            castType.equals(XSDDatatype.XSDdouble) ||
            castType.equals(XSDDatatype.XSDboolean) ||
            castType instanceof XSDAbstractDateTimeType )   // Includes durations, and Gregorian
        {
            // More helpful error message.
            if ( lex.startsWith(" ") || lex.endsWith(" ") )
                throw exception("Not a valid literal form (has whitespace): '"+lex+"'") ;
        }
        return NodeValue.makeNode(lex, castType) ;

    }

    // Known to work casts.  No checking.
    private static NodeValue cast$(String lex, XSDDatatype castType) {
        return NodeValue.makeNode(lex, castType) ;
    }

    // Return the integer lexical form for a double, where the double is known to be integer valued.
    private static String doubleIntegerToString(double d) {
        // Fast path
        long x = Math.round(d) ;
        if ( x != Long.MAX_VALUE && x != Long.MIN_VALUE )
            return  Long.toString(x) ;

        String lex = BigDecimal.valueOf(d).toPlainString() ;
        int i = lex.indexOf('.') ;
        if ( i >= 0 )
            // Adds .0 for some (small) doubles. 
            lex = lex.substring(0, i) ;
        return lex;
    }

    // Return the decimal lexical form for a double value.
    // Java big decimal allows "E" forms, XSD does not.
    // For RDF purposes, return ".0" forms (which are 
    // short-forms in Turtle and SPARQL).
    private static String doubleToDecimalString(double d) {
        // BigDecimal.valueOf(d) can lead to trailing zeros.
        String lex = BigDecimal.valueOf(d).toPlainString() ;
        // Clean the string. 
        int i = lex.indexOf('.') ;
        if ( i < 0 )
            return lex+".0" ;
        while((i < lex.length()-2) && lex.endsWith("0"))
            lex = lex.substring(0,  lex.length()-1) ;
        return lex ;
    }
}
