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

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.xsd.*;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.shared.impl.JenaParameters;

/**
 * Datatype representation for xsd:float. Can't just use XSDBaseNumericType
 * because float, double and decimal are all disjoint in XSD. Can use plain
 * XSDDatatype because the equality function needs overriding.
 */
public class XSDFloat extends XSDDatatype {
    /**
      * Constructor. 
      * @param typeName the name of the XSD type to be instantiated, this is 
      * used to lookup a type definition from the Xerces schema factory.
      */
     public XSDFloat(String typeName) {
         super(typeName);
     }
    
     /**
      * Constructor. 
      * @param typeName the name of the XSD type to be instantiated, this is 
      * used to lookup a type definition from the Xerces schema factory.
      * @param javaClass the java class for which this xsd type is to be
      * treated as the cannonical representation
      */
     public XSDFloat(String typeName, Class<?> javaClass) {
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
     @Override
    public boolean isValidValue(Object valueForm) {
         return (valueForm instanceof Float);
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
     * Parse a validated lexical form. Subclasses which use the default
     * parse implementation and are not convered by the explicit convertValidatedData
     * cases should override this.
     */
    @Override
    public Object parseValidated(String lex) {
        switch ( lex )
        {
            case "INF":
                return Float.POSITIVE_INFINITY;
            case "-INF":
                return Float.NEGATIVE_INFINITY;
            case "NaN":
                return Float.NaN;
            default:
                return Float.valueOf( lex );
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
     @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
         return value1.getDatatype() == value2.getDatatype()
              && value1.getValue().equals(value2.getValue());
     }
}
