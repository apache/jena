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

package com.hp.hpl.jena.datatypes.xsd;

import java.util.Arrays ;

import com.hp.hpl.jena.graph.impl.LiteralLabel ;

/** Root class for XSD datatypes with binary values, xsd:hexBinary and xsd:base64Binary.
 * The binary value is stored as a byte[] in the LiteralLabel.
 */

public abstract class XSDbinary extends XSDDatatype {
    /**
     * @param typeName the name of the XSD type to be instantiated, this is 
     * used to lookup a type definition from the Xerces schema factory.
     */
    protected XSDbinary(String typeName,  boolean register) {
        super(typeName, register ? byte[].class : null );
    }
         
    /**
     * Test whether the given object is a legal value form
     * of this datatype. Brute force implementation.
     */
    @Override
    public boolean isValidValue(Object valueForm) {
        return (valueForm instanceof byte[]);
    }
    
    /**
     * Compares two instances of values of the given datatype.
     * This ignores lang tags and just uses the java.lang.Number 
     * equality.
     */
    @Override
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2) {
        return value1.getDatatype() == value2.getDatatype()
            && Arrays.equals((byte[])value1.getValue(), (byte[])value2.getValue());
            //      && value1.getLexicalForm().equals(value2.getLexicalForm());  // bug tracking, not real code
    }
   
    @Override
    public int getHashCode( LiteralLabel lit )
    {
        // Can't use super.getHashCode as that does "value.hashCode"
        // Java arrays are not equal by value and their hash code of the sameValue array are different. 
        if ( lit.isWellFormed() )
            return getHashCode( (byte []) lit.getValue() );
        else
            return lit.getLexicalForm().hashCode() ;
    }
}

