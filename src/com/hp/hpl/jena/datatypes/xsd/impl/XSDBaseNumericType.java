/******************************************************************
 * File:        XSDBaseNumericType.java
 * Created by:  Dave Reynolds
 * Created on:  09-Feb-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: XSDBaseNumericType.java,v 1.9 2003-11-08 15:53:56 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes.xsd.impl;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.datatypes.xsd.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.shared.impl.JenaParameters;

/**
 * Base implementation for all numeric datatypes derinved from
 * xsd:decimal. The only purpose of this place holder is
 * to support the isValidLiteral tests across numeric types. Note
 * that float and double are not included in this set.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.9 $ on $Date: 2003-11-08 15:53:56 $
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
    public XSDBaseNumericType(String typeName, Class javaClass) {
        super(typeName, javaClass);
    }

    
    /**
     * Test whether the given LiteralLabel is a valid instance
     * of this datatype. This takes into accound typing information
     * as well as lexical form - for example an xsd:string is
     * never considered valid as an xsd:integer (even if it is
     * lexically legal like "1").
     */
    public boolean isValidLiteral(LiteralLabel lit) {
        RDFDatatype dt = lit.getDatatype();
        if (this.equals(dt)) return true;
        if (dt instanceof XSDBaseNumericType) {
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
    public boolean isValidValue(Object valueForm) {
        if (valueForm instanceof Number) {
            return isValid(valueForm.toString());
        } else {
            return false;
        }
    }
   
    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    public Object parse(String lexicalForm) throws DatatypeFormatException {        
        if (JenaParameters.enableWhitespaceCheckingOfTypedLiterals) {
            if ( ! lexicalForm.trim().equals(lexicalForm)) {
                throw new DatatypeFormatException(lexicalForm, this, "whitespace violation");
            }
        }
        return super.parse(lexicalForm);
    }
    
    /**
     * Compares two instances of values of the given datatype.
     * This ignores lang tags and just uses the java.lang.Number 
     * equality.
     */
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        Object o1 = value1.getValue();
        Object o2 = value2.getValue();
        if (!(o1 instanceof Number) || !(o2 instanceof Number)) {
            return false;
        }
        if (o1 instanceof Float || o1 instanceof Double) {
            return (((Number)o1).doubleValue() == ((Number)o2).doubleValue());
        } else if (o1 instanceof BigInteger || o1 instanceof BigDecimal) {
            return o1.equals(o2);
        } else {
            return (((Number)o1).longValue() == ((Number)o2).longValue());
        }
    }
}

/*
    (c) Copyright 2002 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
