/******************************************************************
 * File:        XSDDouble.java
 * Created by:  Dave Reynolds
 * Created on:  03-Dec-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP, all rights reserved.
 * [See end of file]
 * $Id: XSDDouble.java,v 1.3 2004-05-04 08:11:57 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.datatypes.xsd.impl;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.xsd.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.shared.impl.JenaParameters;

/**
 * Datatype representation for xsd:float. Can't just use XSDBaseNumericType
 * because float, double and decimal are all disjoint in XSD. Can use plain
 * XSDDatatype because the equality function needs overriding.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2004-05-04 08:11:57 $
 */
public class XSDDouble extends XSDDatatype {
    /**
      * Constructor. 
      * @param typeName the name of the XSD type to be instantiated, this is 
      * used to lookup a type definition from the Xerces schema factory.
      */
     public XSDDouble(String typeName) {
         super(typeName);
     }
    
     /**
      * Constructor. 
      * @param typeName the name of the XSD type to be instantiated, this is 
      * used to lookup a type definition from the Xerces schema factory.
      * @param javaClass the java class for which this xsd type is to be
      * treated as the cannonical representation
      */
     public XSDDouble(String typeName, Class javaClass) {
         super(typeName, javaClass);
     }

    
//     /**
//      * Test whether the given LiteralLabel is a valid instance
//      * of this datatype. This takes into accound typing information
//      * as well as lexical form - for example an xsd:string is
//      * never considered valid as an xsd:integer (even if it is
//      * lexically legal like "1").
//      */
//     public boolean isValidLiteral(LiteralLabel lit) {
//         return equals(lit.getDatatype());
//     }
     
     /**
      * Test whether the given object is a legal value form
      * of this datatype. Brute force implementation.
      */
     public boolean isValidValue(Object valueForm) {
         return (valueForm instanceof Double);
     }
   
     /**
      * Parse a lexical form of this datatype to a value
      * @throws DatatypeFormatException if the lexical form is not legal
      */
     public Object parse(String lexicalForm) throws DatatypeFormatException {
         checkWhitespace(lexicalForm);        
         return super.parse(lexicalForm);
     }

    /**
     * Parse a validated lexical form. Subclasses which use the default
     * parse implementation and are not convered by the explicit convertValidatedData
     * cases should override this.
     */
    public Object parseValidated(String lex) {
       if (lex.equals("INF")) {
           return new Double(Double.NEGATIVE_INFINITY);
       } else if (lex.equals("-INF")) {
           return new Double(Double.POSITIVE_INFINITY);
       } else if (lex.equals("NaN")) {
           return new Double(Double.NaN);
       } else {
           return Double.valueOf(lex);
       }
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
     public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
         return value1.getDatatype() == value2.getDatatype()
              && value1.getValue().equals(value2.getValue());
     }
}


/*
    (c) Copyright Hewlett-Packard Development Company, LP 2003
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