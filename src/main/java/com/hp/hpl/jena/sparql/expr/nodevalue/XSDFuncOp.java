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

package com.hp.hpl.jena.sparql.expr.nodevalue;

import static com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions.* ;
import static com.hp.hpl.jena.sparql.expr.nodevalue.NumericType.OP_DECIMAL ;
import static com.hp.hpl.jena.sparql.expr.nodevalue.NumericType.OP_DOUBLE ;
import static com.hp.hpl.jena.sparql.expr.nodevalue.NumericType.OP_FLOAT ;
import static com.hp.hpl.jena.sparql.expr.nodevalue.NumericType.OP_INTEGER ;

import java.math.BigDecimal ;
import java.math.BigInteger ;
import java.text.DecimalFormat ;
import java.util.HashSet ;
import java.util.List ;
import java.util.Set ;
import java.util.regex.Pattern ;

import org.openjena.atlas.lib.StrUtils ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime ;
import com.hp.hpl.jena.datatypes.xsd.XSDDuration ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.expr.* ;
import com.hp.hpl.jena.sparql.util.DateTimeStruct ;
/**
 * Implementation of XQuery/XPath functions and operators.
 * http://www.w3.org/TR/xpath-functions/ */
public class XSDFuncOp
{
    private XSDFuncOp() {}
    
    // The choice of "24" is arbitrary but more than 18 as required by F&O 
    private static final int DIVIDE_PRECISION = 24 ;
    // --------------------------------
    // Numeric operations
    // http://www.w3.org/TR/xpath-functions/#op.numeric
    // http://www.w3.org/TR/xpath-functions/#comp.numeric
    
    public static NodeValue add(NodeValue nv1, NodeValue nv2) // F&O numeric-add
    {
        switch (classifyNumeric("add", nv1, nv2))
        {
            case OP_INTEGER:
                return NodeValue.makeInteger(nv1.getInteger().add(nv2.getInteger())) ;
            case OP_DECIMAL:
                return NodeValue.makeDecimal(nv1.getDecimal().add(nv2.getDecimal())) ;
            case OP_FLOAT:
                return NodeValue.makeFloat(nv1.getFloat() + nv2.getFloat()) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble(nv1.getDouble() + nv2.getDouble()) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : ("+nv1+" ," +nv2+")") ;   
        }
    }
    
    public static NodeValue subtract(NodeValue nv1, NodeValue nv2) // F&O numeric-subtract
    {
        switch (classifyNumeric("subtract", nv1, nv2))
        {
            case OP_INTEGER:
                return NodeValue.makeInteger(nv1.getInteger().subtract(nv2.getInteger())) ;
            case OP_DECIMAL:
                return NodeValue.makeDecimal(nv1.getDecimal().subtract(nv2.getDecimal())) ;
            case OP_FLOAT:
                return NodeValue.makeFloat(nv1.getFloat() - nv2.getFloat()) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble(nv1.getDouble() - nv2.getDouble()) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : ("+nv1+" ," +nv2+")") ;   
        }
    }
    
    public static NodeValue multiply(NodeValue nv1, NodeValue nv2) // F&O numeric-multiply
    {
        switch (classifyNumeric("multiply", nv1, nv2))
        {
            case OP_INTEGER:
                return NodeValue.makeInteger(nv1.getInteger().multiply(nv2.getInteger())) ;
            case OP_DECIMAL:
                return NodeValue.makeDecimal(nv1.getDecimal().multiply(nv2.getDecimal())) ;
            case OP_FLOAT:
                return NodeValue.makeFloat(nv1.getFloat() * nv2.getFloat()) ;
           case OP_DOUBLE:
                return NodeValue.makeDouble(nv1.getDouble() * nv2.getDouble()) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : ("+nv1+" ," +nv2+")") ;   
        }
    }
    
    // Java 1.4 does not have BigDecimal.ZERO
    private static final BigDecimal BigDecimalZero = new BigDecimal(0e0) ;
    
    /* Quote from XQuery/XPath F&O:
        For xs:float or xs:double values, a positive number divided by positive zero returns INF.
        A negative number divided by positive zero returns -INF.
        Division by negative zero returns -INF and INF, respectively.
        Positive or negative zero divided by positive or negative zero returns NaN.
        Also, INF or -INF divided by INF or -INF returns NaN.
     */
    
    public static NodeValue divide(NodeValue nv1, NodeValue nv2) // F&O numeric-divide
    {
        switch (classifyNumeric("divide", nv1, nv2))
        {
            case OP_INTEGER:
            {
                if ( nv2.getInteger().equals(BigInteger.ZERO) )
                    throw new ExprEvalException("Divide by zero in divide") ;
               // Note: result is a decimal
                BigDecimal d1 = new BigDecimal(nv1.getInteger()) ;
                BigDecimal d2 = new BigDecimal(nv2.getInteger()) ;
                return decimalDivide(d1, d2) ;
            }
            case OP_DECIMAL:
            {
                if ( nv2.getDecimal().compareTo(BigDecimalZero) == 0 )
                    throw new ExprEvalException("Divide by zero in decimal divide") ;
                BigDecimal d1 = nv1.getDecimal() ;
                BigDecimal d2 = nv2.getDecimal() ;
                return decimalDivide(d1, d2) ;
            }
            case OP_FLOAT:
                // No need to check for divide by zero
                return NodeValue.makeFloat(nv1.getFloat() / nv2.getFloat()) ;
            case OP_DOUBLE:
                // No need to check for divide by zero
                return NodeValue.makeDouble(nv1.getDouble() / nv2.getDouble()) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : ("+nv1+" ," +nv2+")") ;   
        }
    }
    
    //private static BigDecimal decimalDivide(BigDecimal d1, BigDecimal d2)
    private static NodeValue decimalDivide(BigDecimal d1, BigDecimal d2)
    {
        // Java 1.5-ism BigDecimal.divide(BigDecimal) -- but fails for 1/3 anyway.
        // return d1.divide(d2) ;
        // The one downside here is that the precision is always extended 
        // even when unnecessary.
        try {
            BigDecimal d3 = d1.divide(d2, DIVIDE_PRECISION, BigDecimal.ROUND_FLOOR) ; 
            return messAroundWithBigDecimalFormat(d3) ;
        } catch (ArithmeticException ex)
        {
            Log.warn(XSDFuncOp.class, "ArithmeticException in decimal divide - attempting to treat as doubles") ;
            BigDecimal d3 = new BigDecimal(d1.doubleValue()/d2.doubleValue()) ;
            return NodeValue.makeDecimal(d3) ;
        }
    }
    
    private static NodeValue messAroundWithBigDecimalFormat(BigDecimal d)
    {
        String x = d.toPlainString() ;
        
        // The part after the "."
        int dotIdx = x.indexOf('.') ;
        if ( dotIdx < 0 )
            // No DOT.
            return NodeValue.makeNode(x, XSDDatatype.XSDdecimal) ;
        
        // Has a DOT.
        
        int i = x.length()-1;
        while ( i > dotIdx && x.charAt(i) == '0' )
            i -- ;
        if ( i < x.length()-1)
            // And trailing zeros.
            x = x.substring(0, i+1) ;   

        // Avoid as expensive.
        //x = x.replaceAll("0*$", "") ;
        return NodeValue.makeNode(x, XSDDatatype.XSDdecimal) ;
    }
    
    public static NodeValue max(NodeValue nv1, NodeValue nv2)
    {
        int x = compareNumeric(nv1, nv2) ;
        if ( x == Expr.CMP_LESS)
            return nv2 ;
        return nv1 ;
    }

    public static NodeValue min(NodeValue nv1, NodeValue nv2)
    {
        int x = compareNumeric(nv1, nv2) ;
        if ( x == Expr.CMP_GREATER)
            return nv2 ;
        return nv1 ;
    }

    public static NodeValue not(NodeValue nv) // F&O fn:not
    {
        boolean b = XSDFuncOp.booleanEffectiveValue(nv) ; 
        return NodeValue.booleanReturn(!b) ;
    }
    
    
    public static NodeValue booleanEffectiveValueAsNodeValue(NodeValue nv) // F&O fn:boolean
    {
        if ( nv.isBoolean() )   // "Optimization" (saves on object churn)
            return nv ;
        return NodeValue.booleanReturn(booleanEffectiveValue(nv)) ; 
    }
    
    public static boolean booleanEffectiveValue(NodeValue nv) // F&O fn:boolean
    {
        // Apply the "boolean effective value" rules
        //boolean: value of the boolean (strictly, if derived from xsd:boolean) 
        //string: length(string) > 0
        //numeric: number != Nan && number != 0
        // http://www.w3.org/TR/xquery/#dt-ebv
        
        if ( nv.isBoolean() )
            return nv.getBoolean() ;
        if ( nv.isString() )
            return nv.getString().length() > 0 ;
        if ( nv.isInteger() )
            return ! nv.getInteger().equals(NodeValue.IntegerZERO) ;
        if ( nv.isDecimal() )
            return ! nv.getDecimal().equals(NodeValue.DecimalZERO) ;
        if ( nv.isDouble() )
            return nv.getDouble() != 0.0 ;
        NodeValue.raise(new ExprEvalException("Not a boolean effective value (wrong type): "+nv)) ;
        // Does not return
        return false ;
    }

    public static NodeValue unaryMinus(NodeValue nv) // F&O numeric-unary-minus
    {
        switch (classifyNumeric("unaryMinus", nv))
        {
            case OP_INTEGER:
                return NodeValue.makeInteger( nv.getInteger().negate() ) ;
            case OP_DECIMAL:
                return NodeValue.makeDecimal( nv.getDecimal().negate() ) ;
            case OP_FLOAT:
                return NodeValue.makeFloat( - nv.getFloat() ) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble( - nv.getDouble() ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+nv) ;   
        }
    }
    
    public static NodeValue unaryPlus(NodeValue nv) // F&O numeric-unary-plus
    {
        // Not quite a no-op - tests for a number
        NumericType opType = classifyNumeric("unaryPlus", nv) ;
        return nv ;
    }

    public static NodeValue abs(NodeValue nv)
    {
        switch (classifyNumeric("abs", nv))
        {
            case OP_INTEGER:
                return NodeValue.makeInteger(nv.getInteger().abs()) ;
            case OP_DECIMAL:
                return NodeValue.makeDecimal(nv.getDecimal().abs()) ;
            case OP_FLOAT:
                return NodeValue.makeFloat( Math.abs(nv.getFloat()) ) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble( Math.abs(nv.getDouble()) ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+nv) ;   
        }
    }

    public static NodeValue ceiling(NodeValue v)
    {
        switch (classifyNumeric("ceiling", v))
        {
            case OP_INTEGER:
                return v ;
            case OP_DECIMAL:
                BigDecimal dec = v.getDecimal().setScale( 0, BigDecimal.ROUND_CEILING) ;
                return NodeValue.makeDecimal(dec) ;
            case OP_FLOAT:
                return NodeValue.makeFloat( (float)Math.ceil(v.getFloat()) ) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble( Math.ceil(v.getDouble()) ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+v) ;   
        }
    }
    
    public static NodeValue floor(NodeValue v)
    {
        switch (classifyNumeric("floor", v))
        {
            case OP_INTEGER:
                return v ;
            case OP_DECIMAL:
                BigDecimal dec = v.getDecimal().setScale(0, BigDecimal.ROUND_FLOOR) ;
                return NodeValue.makeDecimal(dec) ;
            case OP_FLOAT:
                return NodeValue.makeFloat( (float)Math.floor(v.getFloat()) ) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble( Math.floor(v.getDouble()) ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+v) ;   
        }
    }

    public static NodeValue round(NodeValue v)
    {
        switch (classifyNumeric("round", v))
        {
            case OP_INTEGER:
                return v ;
            case OP_DECIMAL:
                int sgn =  v.getDecimal().signum() ;
                BigDecimal dec ;
                if ( sgn < 0 )
                    dec = v.getDecimal().setScale(0, BigDecimal.ROUND_HALF_DOWN) ;
                else
                    dec = v.getDecimal().setScale(0, BigDecimal.ROUND_HALF_UP) ;
                return NodeValue.makeDecimal(dec) ;
            case OP_FLOAT:
                return NodeValue.makeFloat( Math.round(v.getFloat()) ) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble( Math.round(v.getDouble()) ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+v) ;   
        }
    }
    
    public static NodeValue sqrt(NodeValue v)
    {
        switch (classifyNumeric("sqrt", v))
        {
            case OP_INTEGER:
            case OP_DECIMAL:
                double dec = v.getDecimal().doubleValue() ;
                return NodeValue.makeDecimal( Math.sqrt(dec) ) ;
            case OP_FLOAT:
                // NB - returns a double
                return NodeValue.makeDouble( Math.sqrt(v.getDouble()) ) ;
            case OP_DOUBLE:
                return NodeValue.makeDouble( Math.sqrt(v.getDouble()) ) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : "+v) ;   
        }
    }
   
    // NB Java string start from zero and uses start/end
    // F&O strings start from one and uses start/length

    public static NodeValue javaSubstring(NodeValue v1, NodeValue v2)
    {
        return javaSubstring(v1, v2, null) ; 
    }

    public static NodeValue javaSubstring(NodeValue nvString, NodeValue nvStart, NodeValue nvFinish)
    {
        try {
            String string = nvString.getString() ;
            int start = nvStart.getInteger().intValue() ;
            if ( nvFinish == null )
                return NodeValue.makeString(string.substring(start)) ;
            
            int finish = nvFinish.getInteger().intValue() ;
            return NodeValue.makeString(string.substring(start, finish)) ;
        } catch (IndexOutOfBoundsException ex)
        {
            throw new ExprEvalException("IndexOutOfBounds", ex) ;
        }
    }
    
    public static NodeValue strlen(NodeValue nvString)
    {
        Node n = checkAndGetStringLiteral("strlen", nvString) ;
        int len = n.getLiteralLexicalForm().length() ;
        return NodeValue.makeInteger(len) ;
    }
    
    public static NodeValue strReplace(NodeValue nvStr, NodeValue nvPattern, NodeValue nvReplacement, NodeValue nvFlags)
    {
        String pat = checkAndGetStringLiteral("replace", nvPattern).getLiteralLexicalForm() ;
        int flags = 0 ;
        if ( nvFlags != null )
        {
            String flagsStr = checkAndGetStringLiteral("replace", nvFlags).getLiteralLexicalForm() ;
            flags = RegexJava.makeMask(flagsStr) ;
        }
        
        return strReplace(nvStr, Pattern.compile(pat, flags), nvReplacement) ;
    }

    public static NodeValue strReplace(NodeValue nvStr, Pattern pattern, NodeValue nvReplacement)
    {
        String n = checkAndGetStringLiteral("replace", nvStr).getLiteralLexicalForm() ;
        String rep = checkAndGetStringLiteral("replace", nvReplacement).getLiteralLexicalForm() ;
        String x = pattern.matcher(n).replaceAll(rep) ;
        return calcReturn(x, nvStr.asNode()) ;
    }

    public static NodeValue strReplace(NodeValue nvStr, NodeValue nvPattern, NodeValue nvReplacement)
    {
        return strReplace(nvStr, nvPattern, nvReplacement, null) ;
    }    

    public static NodeValue substring(NodeValue v1, NodeValue v2)
    {
        return substring(v1, v2, null) ; 
    }

    public static NodeValue substring(NodeValue nvString, NodeValue nvStart, NodeValue nvLength)
    {
        Node n = checkAndGetStringLiteral("substring", nvString) ;
        RDFDatatype dt = n.getLiteralDatatype() ;
        String lang = n.getLiteralLanguage() ;
        
        // A string of some kind.
        
        // XSD F&O: 
        try {
            // NaN, float and double.
            
            String string = n.getLiteralLexicalForm() ;
            int start = intValueStr(nvStart, string.length()+1) ;
            int length ;
            
            if ( nvLength != null )
                length = intValueStr(nvLength, 0) ;
            else
            {
                length = string.length() ;
                if ( start < 0 )
                    length = length-start ; // Address to end of string.
            }
           
            int finish = start + length ;
            
            // Adjust for zero and negative rules for XSD.
            // Calculate the finish, regardless of whether start is zero of negative ...

            // Adjust to java - and ensure within the string.
            // F&O strings are one-based ; convert to java, 0 based.
            
            // java needs indexes in-bounds.
            if ( start <= 0 )
                start = 1 ;
            start-- ; 
            finish -- ;
            if ( finish > string.length() )
                finish = string.length() ; // Java index must be within bounds.
            if ( finish < start )
                finish = start ;
            
            if ( finish < 0 )
                finish = 0 ;
            
            if ( string.length() == 0 )
                return calcReturn("", n) ;
            
            String lex2 = string.substring(start, finish) ;
            return calcReturn(lex2, n) ;
        } catch (IndexOutOfBoundsException ex)
        {
            throw new ExprEvalException("IndexOutOfBounds", ex) ;
        }
    }
    
    private static int intValueStr(NodeValue nv, int valueNan) 
    {
        if ( nv.isInteger() ) return nv.getInteger().intValue() ;
        if ( nv.isDecimal() )
            // No decimal round in Java 1.4
            return (int)Math.round(nv.getDecimal().doubleValue()) ;
        
        if ( nv.isFloat() ) 
        {
            float f = nv.getFloat() ;
            if ( Float.isNaN(f)) return valueNan ; 
            return Math.round(f) ;
        }
        if ( nv.isDouble() )
        {
            double d = nv.getDouble() ;
            if ( Double.isNaN(d)) return valueNan ; 
            return (int)Math.round(d) ;
        }
        throw new ExprEvalException("Not a number:"+nv) ;
    }
 
    public static NodeValue strContains(NodeValue string, NodeValue match)
    {
        checkTwoArgumentStringLiterals("contains", string, match) ;
        String lex1 = string.asNode().getLiteralLexicalForm() ;
        String lex2 = match.asNode().getLiteralLexicalForm() ;
        boolean x = StrUtils.contains(lex1, lex2) ;
        return NodeValue.booleanReturn(x) ;
    }
    
    public static NodeValue strStartsWith(NodeValue string, NodeValue match)
    {
        checkTwoArgumentStringLiterals("strStarts", string, match) ;
        String lex1 = string.asNode().getLiteralLexicalForm() ;
        String lex2 = match.asNode().getLiteralLexicalForm() ;
        return NodeValue.booleanReturn(lex1.startsWith(lex2)) ;
    }
    
    public static NodeValue strEndsWith(NodeValue string, NodeValue match)
    {
        checkTwoArgumentStringLiterals("strEnds", string, match) ;
        String lex1 = string.asNode().getLiteralLexicalForm() ;
        String lex2 = match.asNode().getLiteralLexicalForm() ;
        return NodeValue.booleanReturn(lex1.endsWith(lex2)) ;
    }
    
    private static NodeValue calcReturn(String result, Node arg)
    {
        Node n2 = Node.createLiteral(result, arg.getLiteralLanguage(), arg.getLiteralDatatype()) ; 
        return NodeValue.makeNode(n2) ;
        
//        if ( arg.getLiteralDatatype() != null )
//        {
//            if ( arg.getLiteralDatatype() != XSDDatatype.XSDstring )
//                throw new ARQInternalErrorException("Excepted only xsd:string: "+arg) ; 
//            // Must be xsd:string
//            return NodeValue.makeNode(result, XSDDatatype.XSDstring ) ;
//        }
//        String lang = arg.getLiteralLanguage() ;
//        if ( lang == null ) lang = "" ;
//        if ( lang.equals("") ) return NodeValue.makeString(result) ;
//        return NodeValue.makeNode(result, lang, (String)null) ;
    }
    
    public static NodeValue strBefore(NodeValue string, NodeValue match)
    {
        checkTwoArgumentStringLiterals("strBefore", string, match) ;
        String lex1 = string.asNode().getLiteralLexicalForm() ;
        String lex2 = match.asNode().getLiteralLexicalForm() ;
        Node mainArg = string.asNode() ;
        
        if ( lex2.length() == 0 )
            return calcReturn("", mainArg) ;
        
        int i = lex1.indexOf(lex2) ;
        if ( i < 0 )
            return calcReturn("", mainArg) ;
        
        String s = lex1.substring(0, i) ;
        return calcReturn(s, string.asNode()) ;
    }
    
    public static NodeValue strAfter(NodeValue string, NodeValue match)
    {
        checkTwoArgumentStringLiterals("strAfter", string, match) ;
        String lex1 = string.asNode().getLiteralLexicalForm() ;
        String lex2 = match.asNode().getLiteralLexicalForm() ;
        Node mainArg = string.asNode() ;
        
        if ( lex2.length() == 0 )
            return calcReturn("", mainArg) ;
        
        int i = lex1.indexOf(lex2) ;
        if ( i < 0 )
            return calcReturn("", mainArg) ;
        i += lex2.length() ;
        String s = lex1.substring(i) ;
        return calcReturn(s, string.asNode()) ;
    }

    public static NodeValue strLowerCase(NodeValue string)
    {
        Node n = checkAndGetStringLiteral("lcase", string) ;
        String lex = n.getLiteralLexicalForm() ;
        String lex2 = lex.toLowerCase() ;
        return calcReturn(lex2, string.asNode()) ;
    }

    public static NodeValue strUpperCase(NodeValue string)
    {
        Node n = checkAndGetStringLiteral("ucase", string) ;
        String lex = n.getLiteralLexicalForm() ;
        String lex2 = lex.toUpperCase() ;
        return calcReturn(lex2, string.asNode()) ;
    }
    
    // F&O fn;concat (implicit cast to strings).
    public static NodeValue fnConcat(List<NodeValue> args)
    {
        StringBuilder sb = new StringBuilder() ;
        
        for ( NodeValue arg : args )
        {
            String x = arg.asString() ;
            sb.append(x) ;
        }
        return NodeValue.makeString(sb.toString()) ;
    }
    
    // SPARQL CONCAT
    public static NodeValue strConcat(List<NodeValue> args)
    {
        // Step 1 : Choose type.
        // One lang tag -> that lang tag
        String lang = null ;
        boolean mixedLang = false ;
        boolean xsdString = false ;
        boolean simpleLiteral = false ;
        
        StringBuilder sb = new StringBuilder() ;
        
        for ( NodeValue nv : args )
        {
            Node n = checkAndGetStringLiteral("CONCAT", nv) ;
            String lang1 = n.getLiteralLanguage() ;
            if ( ! lang1.equals("") )
            {
                if ( lang != null && ! lang1.equals(lang) )
                    //throw new ExprEvalException("CONCAT: Mixed language tags: "+args) ;
                    mixedLang = true ;
                lang = lang1 ;
            }
            else if ( n.getLiteralDatatype() != null )
                xsdString = true ;
            else
                simpleLiteral = true ;
            
            sb.append(n.getLiteralLexicalForm()) ;
        }
        
        if ( mixedLang )
            return NodeValue.makeString(sb.toString()) ;
        
        // Must be all one lang.
        if ( lang != null )
        {
            if ( ! xsdString && ! simpleLiteral )
                return NodeValue.makeNode(sb.toString(), lang, (String)null) ;
            else
                // Lang and one or more of xsd:string or simpleLiteral.
                return NodeValue.makeString(sb.toString()) ;
        }       
        
        if ( simpleLiteral && xsdString )
            return NodeValue.makeString(sb.toString()) ;
        // All xsdString
        if ( xsdString )
            return NodeValue.makeNode(sb.toString(), XSDDatatype.XSDstring) ;
        if ( simpleLiteral )
            return NodeValue.makeString(sb.toString()) ;
        
        // No types - i.e. no arguments
        return NodeValue.makeString(sb.toString()) ;
        
    }
    
    public static NumericType classifyNumeric(String fName, NodeValue nv1, NodeValue nv2)
    {
        if ( !nv1.isNumber() )
            throw new ExprEvalTypeException("Not a number (first arg to "+fName+"): "+nv1) ;
        if ( !nv2.isNumber() )
            throw new ExprEvalTypeException("Not a number (second arg to "+fName+"): "+nv2) ;
        
        if ( nv1.isInteger() )
        {
            if (nv2.isInteger() )
                return OP_INTEGER ;
            if ( nv2.isDecimal() )
                return OP_DECIMAL ;
            if ( nv2.isFloat() )
                return OP_FLOAT ;
            if ( nv2.isDouble() )
                return OP_DOUBLE ;
            throw new ARQInternalErrorException("Numeric op unrecognized (second arg to "+fName+"): "+nv2) ;
        }
        
        if ( nv1.isDecimal() )
        {
            if ( nv2.isDecimal() )
                return OP_DECIMAL ;
            if ( nv2.isFloat() )
                return OP_FLOAT ;
            if ( nv2.isDouble() )
                return OP_DOUBLE ;
            throw new ARQInternalErrorException("Numeric op unrecognized (second arg to "+fName+"): "+nv2) ;
        }

        if ( nv1.isFloat() )
        {
            if ( nv2.isFloat() )
                return OP_FLOAT ;
            if ( nv2.isDouble() )
                return OP_DOUBLE ;
            throw new ARQInternalErrorException("Numeric op unrecognized (second arg to "+fName+"): "+nv2) ;
        }            
        
        if ( nv1.isDouble() )
        {
            if ( nv2.isDouble() )
                return OP_DOUBLE ;
            throw new ARQInternalErrorException("Numeric op unrecognized (second arg to "+fName+"): "+nv2) ;
        }
            
        throw new ARQInternalErrorException("Numeric op unrecognized (first arg to "+fName+"): "+nv1) ;
    }
    
    public static NumericType classifyNumeric(String fName, NodeValue nv)
    {
        if ( ! nv.isNumber() )
            throw new ExprEvalTypeException("Not a number: ("+fName+") "+nv) ;
        if ( nv.isInteger() )
            return OP_INTEGER ;
        if ( nv.isDecimal() )
            return OP_DECIMAL ;
        if ( nv.isFloat() )
            return OP_FLOAT ;
        if ( nv.isDouble() )
            return OP_DOUBLE ;
        throw new ARQInternalErrorException("Numeric op unrecognized ("+fName+"): "+nv) ;
    }
    
    private static Set<XSDDatatype> integerSubTypes = new HashSet<XSDDatatype>() ;
    static { 
//        decimalSubTypes.add(XSDDatatype.XSDfloat) ;
//        decimalSubTypes.add(XSDDatatype.XSDdouble) ;
        integerSubTypes.add(XSDDatatype.XSDint) ;
        integerSubTypes.add(XSDDatatype.XSDlong) ;
        integerSubTypes.add(XSDDatatype.XSDshort) ;
        integerSubTypes.add(XSDDatatype.XSDbyte) ;
        integerSubTypes.add(XSDDatatype.XSDunsignedByte) ;
        integerSubTypes.add(XSDDatatype.XSDunsignedShort) ;
        integerSubTypes.add(XSDDatatype.XSDunsignedInt) ;
        integerSubTypes.add(XSDDatatype.XSDunsignedLong) ;
//        integerSubTypes.add(XSDDatatype.XSDdecimal) ;
        integerSubTypes.add(XSDDatatype.XSDinteger) ;
        integerSubTypes.add(XSDDatatype.XSDnonPositiveInteger) ;
        integerSubTypes.add(XSDDatatype.XSDnonNegativeInteger) ;
        integerSubTypes.add(XSDDatatype.XSDpositiveInteger) ;
        integerSubTypes.add(XSDDatatype.XSDnegativeInteger) ;
    }
    
    public static boolean isNumericType(XSDDatatype xsdDatatype )
    {
        if ( XSDDatatype.XSDfloat.equals(xsdDatatype) ) return true ;
        if ( XSDDatatype.XSDdouble.equals(xsdDatatype) ) return true ;
        return isDecimalType(xsdDatatype) ;
    }

    public static boolean isDecimalType(XSDDatatype xsdDatatype )
    {
        if ( XSDDatatype.XSDdecimal.equals(xsdDatatype) ) return true ;
        return isIntegerType(xsdDatatype) ;
    }

    public static boolean isIntegerType(XSDDatatype xsdDatatype )
    {
        return integerSubTypes.contains(xsdDatatype) ;
    }

    // --------------------------------
    // Comparisons operations
    // Do not confuse with sameValueAs/notSamevalueAs
    
    private static int calcReturn(int x)
    {
        if ( x < 0 ) return Expr.CMP_LESS ;
        if ( x > 0 ) return Expr.CMP_GREATER ;
        return Expr.CMP_EQUAL ;
    }
    
    
    public static int compareNumeric(NodeValue nv1, NodeValue nv2)
    {
        NumericType opType = classifyNumeric("compareNumeric", nv1, nv2) ;

        switch (opType)
        {
            case OP_INTEGER:
                return calcReturn(nv1.getInteger().compareTo(nv2.getInteger())) ;
            case OP_DECIMAL:
                return calcReturn(nv1.getDecimal().compareTo(nv2.getDecimal())) ;
            case OP_FLOAT:
                return calcReturn(Float.compare(nv1.getFloat(), nv2.getFloat())) ;
            case OP_DOUBLE:
                return calcReturn(Double.compare(nv1.getDouble(), nv2.getDouble())) ;
            default:
                throw new ARQInternalErrorException("Unrecognized numeric operation : ("+nv1+" ," +nv2+")") ;   
        }
    }
    
    //public static int compareDatetime(NodeValue nv1, NodeValue nv2) 
    
    // --------------------------------
    // Functions on strings
    // http://www.w3.org/TR/xpath-functions/#d1e2222
    // http://www.w3.org/TR/xpath-functions/#substring.functions
    
    // String operations
    //  stringCompare = fn:compare
    //  fn:length
    //  fn:string-concat
    //  fn:substring
    // langMatch
    
    public static int compareString(NodeValue nv1, NodeValue nv2)
    { 
        return calcReturn(nv1.getString().compareTo(nv2.getString())) ;
    }
        
    
    // --------------------------------
    // Date/DateTime operations
    // http://www.w3.org/TR/xpath-functions/#comp.duration.datetime
    //  dateTimeCompare
    // works for dates as well because they are implemented as dateTimes on their start point.

    /**
     * Under strict F&O, dateTimes and dates with no timezone have one magically applied. 
     * This default tiemzoine is implementation dependent and can lead to different answers
     *  to queries depending on the timezone. Normally, ARQ uses XMLSchema dateTime comparions,
     *  which an yield "indeterminate", which in turn is an evaluation error. 
     *  F&O insists on true/false so can lead to false positves and negatives. 
     */
    public static boolean strictDateTimeFO = false ;

    public static int compareDateTime(NodeValue nv1, NodeValue nv2)
    { 
        if ( strictDateTimeFO )
            return compareDateTimeFO(nv1, nv2) ;
        return compareXSDDateTime(nv1.getDateTime(), nv2.getDateTime()) ;
    }

//    public static int compareDate(NodeValue nv1, NodeValue nv2)
//    { 
//        if ( strictDateTimeFO )
//            return compareDateFO(nv1, nv2) ;
//        return compareXSDDateTime(nv1.getDateTime(), nv2.getDateTime()) ;
//    }
//    
//    public static int compareTime(NodeValue nv1, NodeValue nv2)
//    { 
//        if ( strictDateTimeFO )
//            return compareDateFO(nv1, nv2) ;
//        return compareXSDDateTime(nv1.getDateTime(), nv2.getDateTime()) ;
//    }
    
    public static int compareDuration(NodeValue nv1, NodeValue nv2)
    { 
        return compareXSDDuration(nv1.getDuration(), nv2.getDuration()) ;
    }

    public static int compareGYear(NodeValue nv1, NodeValue nv2)
    {
        return -99 ;
    }
    public static int compareGYearMonth(NodeValue nv1, NodeValue nv2)
    {
        return -99 ;
    }
    public static int compareGMonth(NodeValue nv1, NodeValue nv2)
    {
        return -99 ;
    }
    public static int compareGMonthDay(NodeValue nv1, NodeValue nv2)
    {
        return -99 ;
    }
    public static int compareGDay(NodeValue nv1, NodeValue nv2)
    {
        return -99 ;
    }
    
    public static final String defaultTimezone = "Z" ;
    
    private static int compareDateTimeFO(NodeValue nv1, NodeValue nv2)
    {
        XSDDateTime dt1 = nv1.getDateTime() ;
        XSDDateTime dt2 = nv2.getDateTime() ;
        
        int x = compareXSDDateTime(dt1, dt2) ;
        
        if ( x == XSDDateTime.INDETERMINATE )
        {
            NodeValue nv3 = fixupDateTime(nv1) ;
            if ( nv3 != null )
            {
                XSDDateTime dt3 = nv3.getDateTime() ; 
                x =  compareXSDDateTime(dt3, dt2) ;
                if ( x == XSDDateTime.INDETERMINATE )
                    throw new ARQInternalErrorException("Still get indeterminate comparison") ;
                return x ;
            }
            
            nv3 = fixupDateTime(nv2) ;
            if ( nv3 != null )
            {
                XSDDateTime dt3 = nv3.getDateTime() ; 
                x =  compareXSDDateTime(dt1, dt3) ;
                if ( x == XSDDateTime.INDETERMINATE )
                    throw new ARQInternalErrorException("Still get indeterminate comparison") ;
                return x ;
            }
            
            throw new ARQInternalErrorException("Failed to fixup dateTimes") ;
        }
        return x ;
        
    }
    
    // XXX Remove??
    // This only differs by some "dateTime" => "date" 
    private static int compareDateFO(NodeValue nv1, NodeValue nv2)
    {
        XSDDateTime dt1 = nv1.getDateTime() ;
        XSDDateTime dt2 = nv2.getDateTime() ;

        int x = compareXSDDateTime(dt1, dt2) ;    // Yes - compareDateTIme
        if ( x == XSDDateTime.INDETERMINATE )
        {
            NodeValue nv3 = fixupDate(nv1) ;
            if ( nv3 != null )
            {
                XSDDateTime dt3 = nv3.getDateTime() ; 
                x =  compareXSDDateTime(dt3, dt2) ;
                if ( x == XSDDateTime.INDETERMINATE )
                    throw new ARQInternalErrorException("Still get indeterminate comparison") ;
                return x ;
            }
            
            nv3 = fixupDate(nv2) ;
            if ( nv3 != null )
            {
                XSDDateTime dt3 = nv3.getDateTime() ; 
                x =  compareXSDDateTime(dt1, dt3) ;
                if ( x == XSDDateTime.INDETERMINATE )
                    throw new ARQInternalErrorException("Still get indeterminate comparison") ;
                return x ;
            }
            
            throw new ARQInternalErrorException("Failed to fixup dateTimes") ;
        }
        return x ;
    }
    
    private static NodeValue fixupDateTime(NodeValue nv)
    {
        DateTimeStruct dts = DateTimeStruct.parseDateTime(nv.asNode().getLiteralLexicalForm()) ;
        if ( dts.timezone != null )
            return null ;
        dts.timezone = defaultTimezone ;
        nv = NodeValue.makeDateTime(dts.toString()) ;
        if ( ! nv.isDateTime() )
            throw new ARQInternalErrorException("Failed to reform an xsd:dateTime") ;
        return nv ;
    }
    
    private static NodeValue fixupDate(NodeValue nv)
    {
        DateTimeStruct dts = DateTimeStruct.parseDate(nv.asNode().getLiteralLexicalForm()) ;
        if ( dts.timezone != null )
            return null ;
        dts.timezone = defaultTimezone ;
        nv = NodeValue.makeDate(dts.toString()) ;
        if ( ! nv.isDate() )
            throw new ARQInternalErrorException("Failed to reform an xsd:date") ;
        return nv ;
    }

    private static int compareXSDDateTime(XSDDateTime dt1 , XSDDateTime dt2)
    {
        // Returns codes are -1/0/1 but also 2 for "Indeterminate"
        // which occurs when one has a timezone and one does not
        // and they are less then 14 hours apart.
        
        // F&O has an "implicit timezone" - this code implements the XMLSchema compare algorithm.  

        int x = dt1.compare(dt2) ;
        if ( x == XSDDateTime.EQUAL )
            return Expr.CMP_EQUAL ;
        if ( x == XSDDateTime.LESS_THAN )
            return Expr.CMP_LESS ;
        if ( x == XSDDateTime.GREATER_THAN )
            return Expr.CMP_GREATER ;
        if ( x == XSDDateTime.INDETERMINATE )
            return Expr.CMP_INDETERMINATE ;
        throw new ARQInternalErrorException("Unexpected return from XSDDateTime.compare: "+x) ;
    }

    private static int compareXSDDuration(XSDDuration duration1 , XSDDuration duration2)
    {
        // Returns codes are -1/0/1 but also 2 for "Indeterminate"
        // Not fully sure when Indeterminate is returned with regards to a duration

        int x = duration1.compare(duration2) ;
        if ( x == XSDDuration.EQUAL )
            return Expr.CMP_EQUAL ;
        if ( x == XSDDuration.LESS_THAN )
            return Expr.CMP_LESS ;
        if ( x == XSDDuration.GREATER_THAN )
            return Expr.CMP_GREATER ;
        if ( x == XSDDuration.INDETERMINATE )
            return Expr.CMP_INDETERMINATE ;
        throw new ARQInternalErrorException("Unexpected return from XSDDuration.compare: "+x) ;
    }

    // --------------------------------
    // Boolean operations
    
    /* Logical OR and AND is special with respect to handling errors truth table.
     * AND they take effective boolean values, not boolean 
     * 
    A       B   |   NOT A   A && B  A || B
    -------------------------------------
    E       E   |   E       E       E
    E       T   |   E       E       T
    E       F   |   E       F       E
    T       E   |   F       E       T
    T       T   |   F       T       T
    T       F   |   F       F       T
    F       E   |   T       F       E
    F       T   |   T       F       T
    F       F   |   T       F       F
    */

    // Not possible because of error masking.
    // public static NodeValue logicalOr(NodeValue x, NodeValue y)
    // public static NodeValue logicalAnd(NodeValue x, NodeValue y)
    
    public static int compareBoolean(NodeValue nv1, NodeValue nv2)
    {
        boolean b1 = nv1.getBoolean() ;
        boolean b2 = nv2.getBoolean() ;
        if ( b1 == b2 )
            return Expr.CMP_EQUAL ;
        
        if ( !b1 && b2 )
            return Expr.CMP_LESS ;
        if ( b1 && !b2 )
            return Expr.CMP_GREATER ;
        throw new ARQInternalErrorException("Weird boolean comparison: "+nv1+", "+nv2) ; 
    }

    public static boolean dateTimeCastCompatible(NodeValue nv, XSDDatatype xsd)
    {
        return nv.hasDateTime() ;
    }
    
    /** Cast a NodeValue to a date/time type (xsd dateTime, date, time, g*) according to F&O
     *  <a href="http://www.w3.org/TR/xpath-functions/#casting-to-datetimes">17.1.5 Casting to date and time types</a>
     *  Throws an exception on incorrect case.
     *   
     *  @throws ExprEvalTypeException  
     */
    
    public static NodeValue dateTimeCast(NodeValue nv, String typeURI)
    {
       RDFDatatype t = Node.getType(typeURI) ;
       return dateTimeCast(nv, t) ;
    }

    /** Cast a NodeValue to a date/time type (xsd dateTime, date, time, g*) according to F&O
     *  <a href="http://www.w3.org/TR/xpath-functions/#casting-to-datetimes">17.1.5 Casting to date and time types</a>
     *  Throws an exception on incorrect case.
     *   
     *  @throws ExprEvalTypeException  
     */
    
    public static NodeValue dateTimeCast(NodeValue nv, RDFDatatype rdfDatatype)
    {
       if ( ! ( rdfDatatype instanceof XSDDatatype ) )
           throw new ExprEvalTypeException("Can't cast to XSDDatatype: "+nv) ;
       XSDDatatype xsd = (XSDDatatype)rdfDatatype ;
       return dateTimeCast(nv, xsd) ;
    }

    /** Cast a NodeValue to a date/time type (xsd dateTime, date, time, g*) according to F&O
     *  <a href="http://www.w3.org/TR/xpath-functions/#casting-to-datetimes">17.1.5 Casting to date and time types</a>
     *  Throws an exception on incorrect case.
     *   
     *  @throws ExprEvalTypeException  
     */
    
    public static NodeValue dateTimeCast(NodeValue nv, XSDDatatype xsd)
    {
        // http://www.w3.org/TR/xpath-functions/#casting-to-datetimes
        if ( ! nv.hasDateTime() )
            throw new ExprEvalTypeException("Not a date/time type: "+nv) ;
        
        XSDDateTime xsdDT = nv.getDateTime() ;
        
        if ( XSDDatatype.XSDdateTime.equals(xsd) )
        {
            // ==> DateTime
            if ( nv.isDateTime() ) return nv ;
            if ( ! nv.isDate() ) throw new ExprEvalTypeException("Can't cast to XSD:dateTime: "+nv) ;
            // DateTime with time 00:00:00
            String x = String.format("%04d-%02d-%02dT00:00:00", xsdDT.getYears(), xsdDT.getMonths(),xsdDT.getDays()) ;
            return NodeValue.makeNode(x, xsd) ;
        }
    
        if ( XSDDatatype.XSDdate.equals(xsd) )
        {
            // ==> Date
            if ( nv.isDate() ) return nv ;
            if ( ! nv.isDateTime() ) throw new ExprEvalTypeException("Can't cast to XSD:date: "+nv) ;
            String x = String.format("%04d-%02d-%02d", xsdDT.getYears(), xsdDT.getMonths(),xsdDT.getDays()) ;
            return NodeValue.makeNode(x, xsd) ;
        }
    
        if ( XSDDatatype.XSDtime.equals(xsd) )
        {
            // ==> time
            if ( nv.isTime() ) return nv ;
            if ( ! nv.isDateTime() ) throw new ExprEvalTypeException("Can't cast to XSD:time: "+nv) ;
            // Careful foratting 
            DecimalFormat nf = new DecimalFormat("00.####") ;
            nf.setDecimalSeparatorAlwaysShown(false) ;
            String x = nf.format(xsdDT.getSeconds()) ;
            x = String.format("%02d:%02d:%s", xsdDT.getHours(), xsdDT.getMinutes(),x) ;
            return NodeValue.makeNode(x, xsd) ;
        }
    
        if ( XSDDatatype.XSDgYear.equals(xsd) )
        {
            // ==> Year
            if ( nv.isGYear() ) return nv ;
            if ( ! nv.isDateTime() && ! nv.isDate() ) throw new ExprEvalTypeException("Can't cast to XSD:gYear: "+nv) ;
            String x = String.format("%04d", xsdDT.getYears()) ;
            return NodeValue.makeNode(x, xsd) ;
        }
    
        if ( XSDDatatype.XSDgYearMonth.equals(xsd) )
        {
            // ==> YearMonth
            if ( nv.isGYearMonth() ) return nv ;
            if ( ! nv.isDateTime() && ! nv.isDate() ) throw new ExprEvalTypeException("Can't cast to XSD:gYearMonth: "+nv) ;
            String x = String.format("%04d-%02d", xsdDT.getYears(), xsdDT.getMonths()) ;
            return NodeValue.makeNode(x, xsd) ;
        }
    
        if ( XSDDatatype.XSDgMonth.equals(xsd) )
        {
            // ==> Month
            if ( nv.isGMonth() ) return nv ;
            if ( ! nv.isDateTime() && ! nv.isDate() ) throw new ExprEvalTypeException("Can't cast to XSD:gMonth: "+nv) ;
            String x = String.format("--%02d", xsdDT.getMonths()) ;
            return NodeValue.makeNode(x, xsd) ;
        }
    
        if ( XSDDatatype.XSDgMonthDay.equals(xsd) )
        {
            // ==> MonthDay
            if ( nv.isGMonthDay() ) return nv ;
            if ( ! nv.isDateTime() && ! nv.isDate() ) throw new ExprEvalTypeException("Can't cast to XSD:gMonthDay: "+nv) ;
            String x = String.format("--%02d-%02d", xsdDT.getMonths(), xsdDT.getDays()) ;
            return NodeValue.makeNode(x, xsd) ;
        }
    
        if ( XSDDatatype.XSDgDay.equals(xsd) )
        {
            // Day
            if ( nv.isGDay() ) return nv ;
            if ( ! nv.isDateTime() && ! nv.isDate() ) throw new ExprEvalTypeException("Can't cast to XSD:gDay: "+nv) ;
            String x = String.format("---%02d", xsdDT.getDays()) ;
            return NodeValue.makeNode(x, xsd) ;
        }
    
        throw new ExprEvalTypeException("Can't case to <"+xsd.getURI()+">: "+nv) ;
    }
    
    public static NodeValue dtGetYear(NodeValue nv)
    {
        if ( nv.isDateTime() || nv.isDate() || nv.isGYear() || nv.isGYearMonth() )
        {
            DateTimeStruct dts = parseAnyDT(nv) ;
            return  NodeValue.makeNode(dts.year, XSDDatatype.XSDinteger) ;
        }
//        else if (nv.isGMonth() )
//            dts = DateTimeStruct.parseGMonth(lex) ;
//        else if (nv.isGMonthDay() )
//            dts = DateTimeStruct.parseGMonthDay(lex) ;
//        else if (nv.isGDay() )
//            dts = DateTimeStruct.parseGDay(lex) ;

            
        throw new ExprEvalException("Not a year datatype") ;
    }

    public static NodeValue dtGetMonth(NodeValue nv)
    {
        if ( nv.isDateTime() || nv.isDate() || nv.isGYearMonth() || nv.isGMonth() || nv.isGMonthDay() )
        {
            DateTimeStruct dts = parseAnyDT(nv) ;
            return NodeValue.makeNode(dts.month, XSDDatatype.XSDinteger) ;
        }
        throw new ExprEvalException("Not a month datatype") ;
    }

    public static NodeValue dtGetDay(NodeValue nv)
    {
        if ( nv.isDateTime() || nv.isDate() || nv.isGMonthDay() || nv.isGDay() )
        {
            DateTimeStruct dts = parseAnyDT(nv) ;
            return NodeValue.makeNode(dts.day, XSDDatatype.XSDinteger) ;
        }
        throw new ExprEvalException("Not a month datatype") ;
    }

    private static DateTimeStruct parseAnyDT(NodeValue nv)
    {
        String lex = nv.getNode().getLiteralLexicalForm() ;
        if ( nv.isDateTime() )
            return DateTimeStruct.parseDateTime(lex) ;
        if ( nv.isDate() ) 
            return DateTimeStruct.parseDate(lex) ;
        if ( nv.isGYear() )
            return DateTimeStruct.parseGYear(lex) ;
        if (nv.isGYearMonth() )
            return DateTimeStruct.parseGYearMonth(lex) ;
        if (nv.isGMonth() )
            return DateTimeStruct.parseGMonth(lex) ;
        if (nv.isGMonthDay() )
            return DateTimeStruct.parseGMonthDay(lex) ;
        if (nv.isGDay() )
            return DateTimeStruct.parseGDay(lex) ;
        if ( nv.isTime() )
            return DateTimeStruct.parseTime(lex) ;
        return null ;
    }

    private static DateTimeStruct parseTime(NodeValue nv)
    {
        String lex = nv.getNode().getLiteralLexicalForm() ;
        if ( nv.isDateTime() )
            return DateTimeStruct.parseDateTime(lex) ;
        else if ( nv.isTime() )
            return DateTimeStruct.parseTime(lex) ;
        else
            throw new ExprEvalException("Not a datatype for time") ;    
    }
    
    public static NodeValue dtGetHours(NodeValue nv)
    {
        DateTimeStruct dts = parseTime(nv) ;
        return NodeValue.makeNode(dts.hour, XSDDatatype.XSDinteger) ;
    }

    public static NodeValue dtGetMinutes(NodeValue nv)
    {
        DateTimeStruct dts = parseTime(nv) ;
        return NodeValue.makeNode(dts.minute, XSDDatatype.XSDinteger) ;
    }

    public static NodeValue dtGetSeconds(NodeValue nv)
    {
        DateTimeStruct dts = parseTime(nv) ;
        return NodeValue.makeNode(dts.second, XSDDatatype.XSDdecimal) ;
    }

    public static NodeValue dtGetTZ(NodeValue nv)
    {
        DateTimeStruct dts = parseAnyDT(nv) ;
        if ( dts == null )
            throw new ExprEvalException("Not a data/time value: "+nv) ;
        if ( dts.timezone == null)
            return NodeValue.nvEmptyString ;
        return  NodeValue.makeString(dts.timezone) ;
    }
    
    public static NodeValue dtGetTimezone(NodeValue nv)
    {
        DateTimeStruct dts = parseAnyDT(nv) ;
        if ( dts == null || dts.timezone == null )
            throw new ExprEvalException("Not a datatype with a timezone: "+nv) ;
        if ( "".equals(dts.timezone) )
            return null ;
        if ("Z".equals(dts.timezone) )
        {
            Node n = Node.createLiteral("PT0S", null, Node.getType(XSDDatatype.XSD+"#dayTimeDuration")) ;
            return NodeValue.makeNode(n) ;
        }
        if ("+00:00".equals(dts.timezone) )
        {
            Node n = Node.createLiteral("PT0S", null, Node.getType(XSDDatatype.XSD+"#dayTimeDuration")) ;
            return NodeValue.makeNode(n) ;
        }
        if ("-00:00".equals(dts.timezone) )
        {
            Node n = Node.createLiteral("-PT0S", null, Node.getType(XSDDatatype.XSD+"#dayTimeDuration")) ;
            return NodeValue.makeNode(n) ;
        }
        
        String s = dts.timezone ;
        int idx = 0;
        StringBuilder sb = new StringBuilder() ;
        if ( s.charAt(0) == '-' )
            sb.append('-') ;
        idx++ ;     // Skip '-' or '+'
        sb.append("PT") ;
        digitsTwo(s, idx, sb, 'H') ;
        idx+= 2 ;
        idx++ ;     // The ":"
        digitsTwo(s, idx, sb, 'M') ;
        idx+= 2 ;
        return NodeValue.makeNode(sb.toString(), null, XSDDatatype.XSD+"#dayTimeDuration") ;
    }
    
    private static void digitsTwo(String s, int idx, StringBuilder sb, char indicator)
    {
        if ( s.charAt(idx) == '0' ) 
        {
            idx ++ ;
            if ( s.charAt(idx) != '0' )
            {
                sb.append(s.charAt(idx)) ;
                sb.append(indicator) ;
            }
            idx++ ;
        }
        else
        {
            sb.append(s.charAt(idx)) ;
            idx++ ;
            sb.append(s.charAt(idx)) ;
            idx++ ;
            sb.append(indicator) ;
        }
    }
}
