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

package com.hp.hpl.jena.rdf.model;
import com.hp.hpl.jena.datatypes.RDFDatatype;

/** An RDF Literal.
 * 
 * <p>In RDF2003 literals can be typed. If typed then the literal comprises a
 * datatype, a lexical form and a value (together with an optional xml:lang
 * string). Old style literals have no type and are termed "plain" literals.</p>
 * 
 * <p>Implementations of this interface should be able to support both plain
 * and typed literals. In the case of typed literals the primitive accessor methods 
 * such as getInt()  determine if the literal value can be coerced to an appropriate
 * java wrapper class. If so then the class is unwrapped to extract the primitive
 * value returned. If the coercion fails then a runtime DatatypeFormatException is 
 * thrown.</p>
 * 
 * <p>In the case of plain literals then the primitve accessor methods duplicate
 * the behvaiour of jena1. The literal is internally stored in lexical form but
 * the accessor methods such as getInt will attempt to parse the lexical form
 * and if successful will return the primitive value.</p>
 * 
 * <p>Object (i.e. non-primitive) values are supported. In the case of typed literals
 * then a global TypeMapper registry determines what datatype representation to
 * use for a given Object type. In the case of plain literals then the object
 * will be stored in the lexical form given by its <CODE>toString</CODE> method. 
 * Factory objects, provided by the application, are needed in that case to covert
 * the lexical form back into the appropriate object type.</p>
 */
public interface Literal extends RDFNode {
    
    /**
        Answer a version of this literal in the model m. If this literal is
        already in m, answer itself rather than create a new literal. This
        method covariantly overrides the method in RDFNode.
    */
    @Override
    public Literal inModel( Model m );
    
    /**
     * Return the value of the literal. In the case of plain literals
     * this will return the literal string. In the case of typed literals
     * it will return a java object representing the value. In the case
     * of typed literals representing a java primitive then the appropriate
     * java wrapper class (Integer etc) will be returned.
     */
    public Object getValue();
    
    /**
     * Return the datatype of the literal. This will be null in the
     * case of plain literals.
     */
    public RDFDatatype getDatatype();
     
    /**
     * Return the uri of the datatype of the literal. This will be null in the
     * case of plain literals.
     */
    public String getDatatypeURI();
    
    /**
     * Return the lexical form of the literal.
     */
    public String getLexicalForm();
    
    /**
     * If the literal is interpretable as a Boolean return its value
     * as a boolean. Plain literals are interpreted by parsing their
     * lexical representation, typed literals are interpreted by coercion
     * of the java object representing their value.
     * 
     
     * @return the literal interpeted as a boolean
     */
    public boolean getBoolean() ;
    
    /**
     * If the literal is interpretable as a Byte return its value.
     * Plain literals are interpreted by parsing their
     * lexical representation, typed literals are interpreted by coercion
     * of the java object representing their value.
     * 
     
     * @return the literal interpeted as a byte
     */
    public byte getByte() ;
    
    /**
     * If the literal is interpretable as a Short return its value.
     * Plain literals are interpreted by parsing their
     * lexical representation, typed literals are interpreted by coercion
     * of the java object representing their value.
     * 
     
     * @return the literal interpeted as a short
     */
    public short getShort() ;
    
    /**
     * If the literal is interpretable as a Integer return its value.
     * Plain literals are interpreted by parsing their
     * lexical representation, typed literals are interpreted by coercion
     * of the java object representing their value.
     * 
     
     * @return the literal interpeted as an int
     */
    public int getInt() ;
    
    /**
     * If the literal is interpretable as a Long return its value.
     * Plain literals are interpreted by parsing their
     * lexical representation, typed literals are interpreted by coercion
     * of the java object representing their value.
     * 
     
     * @return the literal interpeted as a long
     */
    public long getLong() ;
    
    /**
     * If the literal is interpretable as a Char return its value.
     * Plain literals are interpreted by parsing their
     * lexical representation, typed literals are interpreted by coercion
     * of the java object representing their value.
     * 
     
     * @return the literal interpeted as a char
     */
    public char getChar() ;
    
    /**
     * If the literal is interpretable as a Float return its value.
     * Plain literals are interpreted by parsing their
     * lexical representation, typed literals are interpreted by coercion
     * of the java object representing their value.
     * 
     
     * @return the literal interpeted as a float
     */
    public float getFloat() ;
    
    /**
     * If the literal is interpretable as a Double return its value.
     * Plain literals are interpreted by parsing their
     * lexical representation, typed literals are interpreted by coercion
     * of the java object representing their value.
     * 
     
     * @return the literal interpeted as a double
     */
    public double getDouble() ;
    
    /**
     * If the literal is interpretable as a string return its value.
     * For typed literals this will throw an error for non string
     * literals and one needs to use getLexicalForm to return the 
     * string form of other datatypes.
     * 
     
     * @return the literal string
     */
    // TODO is this the right approach, could make getString synonomous 
    //       with getLexicalForm
    public String getString() ;
    
    /** 
         If a language is defined for this literal return it
         @return the language for this literal if it exists, or empty string if none
    */    
    public String getLanguage();
    
    /**
        Answer true iff this literal is (or claims to be) well-formed XML.
    */
    public boolean isWellFormedXML();
    
    /** Test whether another object is equal to this object.
     *
     * <p>A plain Literal is equal to another object only if the object is 
     *    also a plain Literal and the string value and language of both 
     *    literals are equal. In the case of a typed literal equality is 
     *    defined by the datatype equality function on the value spaces and
     *    may or may not include comparison of the lang strings.</p>
     * @param o The object to test against
     * @return true if the the objects are equal, false otherwise
     */    
    @Override
    public boolean equals(Object o);
   
    /**
     * Test that two literals are semantically equivalent.
     * In some cases this may be the sames as equals, in others
     * equals is stricter. For example, two xsd:int literals with
     * the same value but different language tag are semantically
     * equivalent but distinguished by the java equality function
     * in order to support round tripping.
     */
    public boolean sameValueAs(Literal other);
}
