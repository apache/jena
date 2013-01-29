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

package com.hp.hpl.jena.datatypes;

import com.hp.hpl.jena.graph.impl.LiteralLabel;

/**
 * Interface on a datatype representation. An instance of this
 * interface is needed to convert typed literals between lexical
 * and value forms. 
 */
public interface RDFDatatype {

    /**
     * Return the URI which is the label for this datatype
     */
    public String getURI();
    
    /**
     * Convert a value of this datatype out
     * to lexical form.
     */
    public String unparse(Object value);
    
    /**
     * Parse a lexical form of this datatype to a value
     * @throws DatatypeFormatException if the lexical form is not legal
     */
    public Object parse(String lexicalForm) throws DatatypeFormatException;
    
    /**
     * Test whether the given string is a legal lexical form
     * of this datatype.
     */
    public boolean isValid(String lexicalForm);
    
    /**
     * Test whether the given object is a legal value form
     * of this datatype.
     */
    public boolean isValidValue(Object valueForm);
    
    /**
     * Test whether the given LiteralLabel is a valid instance
     * of this datatype. This takes into account typing information
     * as well as lexical form - for example an xsd:string is
     * never considered valid as an xsd:integer (even if it is
     * lexically legal like "1").
     */
    public boolean isValidLiteral(LiteralLabel lit);
    
    /**
     * Compares two instances of values of the given datatype.
     * This defaults to just testing equality of the java value
     * representation but datatypes can override this. We pass the
     * entire LiteralLabel to allow the equality function to take
     * the xml:lang tag and the datatype itself into account.
     */
    public boolean isEqual(LiteralLabel value1, LiteralLabel value2);
    
    /**
         Gets the hash code of a given value. This defaults to
         lit.getValue().hashCode(), but datatypes can overide this, and array types 
         must.
    */
    public int getHashCode( LiteralLabel lit );
    
    /**
     * If this datatype is used as the cannonical representation
     * for a particular java datatype then return that java type,
     * otherwise returns null.
     */
    public Class<?> getJavaClass();
    
    /**
     * Cannonicalise a java Object value to a normal form.
     * Primarily used in cases such as xsd:integer to reduce
     * the Java object representation to the narrowest of the Number
     * subclasses to ensure that indexing of typed literals works. 
     */
    public Object cannonicalise( Object value );
    
    /**
     * Returns an object giving more details on the datatype.
     * This is type system dependent. In the case of XSD types
     * this will be an instance of 
     * <code>org.apache.xerces.impl.xs.dv.XSSimpleType</code>.
     */
    public Object extendedTypeDefinition();
    
    /**
     * Normalization. If the value is narrower than the current data type
     * (e.g. value is xsd:date but the time is xsd:datetime) returns
     * the narrower type for the literal. 
     * If the type is narrower than the value then it may normalize
     * the value (e.g. set the mask of an XSDDateTime)
     * Currently only used to narrow gener XSDDateTime objects
     * to the minimal XSD date/time type.
     * @param value the current object value
     * @param dt the currently set data type
     * @return a narrower version of the datatype based on the actual value range
     */
    public RDFDatatype normalizeSubType(Object value, RDFDatatype dt);
}
