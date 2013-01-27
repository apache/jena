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

package com.hp.hpl.jena.datatypes.xsd.impl;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.datatypes.xsd.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.shared.impl.JenaParameters;

/**
 * Base implementation for all numeric datatypes derived from
 * xsd:decimal. The only purpose of this place holder is
 * to support the isValidLiteral tests across numeric types. Note
 * that float and double are not included in this set.
 */
public class XSDBaseNumericType extends XSDDatatype {

    /**
     * Constructor. 
     * @param typeName the name of the XSD type to be instantiated, this is 
     * used to lookup a type definition from the Xerces schema factory.
     */
    public XSDBaseNumericType(String typeName) {
        super(typeName);
    }
    
    /**
     * Constructor. 
     * @param typeName the name of the XSD type to be instantiated, this is 
     * used to lookup a type definition from the Xerces schema factory.
     * @param javaClass the java class for which this xsd type is to be
     * treated as the cannonical representation
     */
    public XSDBaseNumericType(String typeName, Class<?> javaClass) {
        super(typeName, javaClass);
    }

    
    /**
     * Test whether the given LiteralLabel is a valid instance
     * of this datatype. This takes into accound typing information
     * as well as lexical form - for example an xsd:string is
     * never considered valid as an xsd:integer (even if it is
     * lexically legal like "1").
     */
    @Override
    public boolean isValidLiteral(LiteralLabel lit) {
        if (isBaseTypeCompatible(lit)) {
            String lex = lit.getLexicalForm();
            if (JenaParameters.enableWhitespaceCheckingOfTypedLiterals) {
                if (lex.trim().equals(lex)) {
                    return isValid(lit.getLexicalForm());
                } else {
                    return false;
                }
            } else {
                return isValid(lit.getLexicalForm());
            }
        } else {
            return false;
        }
    }
     
    /**
     * Test whether the given object is a legal value form
     * of this datatype. Brute force implementation.
     */
    @Override
    public boolean isValidValue(Object valueForm) {
        if (valueForm instanceof Number) {
            return isValid(valueForm.toString());
        } else {
            return false;
        }
    }
    
    /**
     * Cannonicalise a java Object value to a normal form.
     * Primarily used in cases such as xsd:integer to reduce
     * the Java object representation to the narrowest of the Number
     * subclasses to ensure that indexing of typed literals works. 
     */
    @Override
    public Object cannonicalise( Object value ) {
        
        if (value instanceof BigInteger) {
            return cannonicalizeInteger( (BigInteger)value );
        } else if (value instanceof BigDecimal) {
            return cannonicalizeDecimal( (BigDecimal)value );
        }
        return suitableInteger( ((Number)value).longValue() );
    }
    
    private static final BigInteger ten = new BigInteger("10");
    private static final int QUOT = 0;
    private static final int REM = 1;
    /**
     * Cannonicalize a big decimal
     */
    private Object cannonicalizeDecimal(BigDecimal value) {
        // This could can be simplified by using toBigIntegerExact
        // once we drop Java 1.4 support
        if (value.scale() > 0) {
            // Check if we can strip off any trailing zeros after decimal point
            BigInteger i = value.unscaledValue();
            int limit = value.scale();
            int nshift = 0;
            for (nshift = 0; nshift < limit; nshift++) {
                BigInteger[] quotRem = i.divideAndRemainder(ten);
                if (quotRem[REM].intValue() != 0) break;
                i = quotRem[QUOT];
            }
            if (nshift > 0) {
               value = new BigDecimal(i, limit - nshift);
               if (value.scale() <= 0) {
                   return cannonicalizeInteger( value.toBigInteger() );
               }
            }
            return value;
        } else {
            return cannonicalizeInteger( value.toBigInteger() );
        }
    }
    
    /**
     * Cannonicalize a big integer
     */
    private Object cannonicalizeInteger( BigInteger value) {
        if (value.bitLength() > 63) {
            return value;
        } else {
            return suitableInteger( value.longValue() );
        }
    }
   
    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    @Override
    public Object parse(String lexicalForm) throws DatatypeFormatException {
        checkWhitespace(lexicalForm);        
        return super.parse(lexicalForm);
    }
    
    /**
     * Check for whitespace violations.
     * Turned off by default.
     */
    protected void checkWhitespace(String lexicalForm) {
        if (JenaParameters.enableWhitespaceCheckingOfTypedLiterals) {
            if ( ! lexicalForm.trim().equals(lexicalForm)) {
                throw new DatatypeFormatException(lexicalForm, this, "whitespace violation");
            }
        }
    }
    
    /**
     * Compares two instances of values of the given datatype.
     * This ignores lang tags and just uses the java.lang.Number 
     * equality.
     */
    @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        if (value1.getDatatype() instanceof XSDBaseNumericType && value2.getDatatype() instanceof XSDBaseNumericType) {
            Number n1 = (Number)value1.getValue();
            Number n2 = (Number)value2.getValue();
            // The cannonicalization step should take care of all cross-type cases, leaving
            // us just that equals doesn't work on BigDecimals in the way you expect
            if (n1 instanceof BigDecimal && n2 instanceof BigDecimal) {
                return  ((BigDecimal)n1).compareTo((BigDecimal)n2) == 0;
            } 
            return n1.equals(n2);
        } else {
            // At least one arg is not part of the integer hierarchy
            return false;
        }
    }
}
