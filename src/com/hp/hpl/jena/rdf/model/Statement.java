/*
 *  (c) Copyright Hewlett-Packard Company 2000, 2001
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Statement.java
 *
 * Created on 27 July 2000, 07:03
 */

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.graph.Triple;

/** An RDF Statement.
 *
 * <p>A statement is also itself a resource and can be both the subject and
 * object of other statements.</p>
 *
 * <p>Like other resources, a statement instance tracks which model it is
 * associated with.</p>
 *
 * <p>This interface provides methods supporting typed literals.  This means
 *    that methods are provided which will translate a built in type, or an
 *    object to an RDF Literal.  This translation is done by invoking the
 *    <CODE>toString()</CODE> method of the object, or its built in equivalent.
 *    The reverse translation is also supported.  This is built in for built
 *    in types.  Factory objects, provided by the application, are used
 *    for application objects.</p>
 * <p>This interface provides methods for supporting enhanced resources.  An
 *    enhanced resource is a resource to which the application has added
 *    behaviour.  RDF containers are examples of enhanced resources built in
 *    to this package.  Enhanced resources are supported by encapsulating a
 *    resource created by an implementation in another class which adds
 *    the extra behaviour.  Factory objects are used to construct such
 *    enhanced resources.</p>
 * @author bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.1.1.1 $ $Date: 2002-12-19 19:18:04 $
 */
public interface Statement  //extends Resource 
{
    
    /** determine whether two statements are equal.
     *
     * <p> Two statements are considered to be equal if they have the
     *    the same subject, predicate and object.  A statement can
     *    only be equal to another statement object.
     * </p>
     * @return true if and only if the equality condition is met.
     * @param o the object to be compared
     */
    
    public boolean equals(Object o);
    
    /** Returns asTriple().hashCode()
     */
    public int hashCode();
    
    /** An accessor method to return the subject of the statements.
     * @return The subject of the statement.
     */
    public Resource getSubject();
    
    /** An accessor function to return the predicate of the statement.
     * @return The predicate of the statement.
     */
    public Property getPredicate();
    
    /** An accessor funtion to return the object of the statement.
     * @return Return the object of the statement.
     */
    public RDFNode getObject();
    
    /** Get a property of the object of the statement.
     *
     * <p>There is an unfortunate ambiguity here.  GetProperty would normally
     *  treat the statement as a resource, and return a property about this
     *  statement.  This is not what is wanted in most cases, so getProperty
     *  on a statement is defined to call getProperty on its object.  If
     *  a property of the statement itself is required, getStatementProperty
     *  should be used.</p>
     *
     * <p>If the object of the statement is not a resource, an exception is
     *  thrown.</p>
     * @param p the property sought
     * @throws RDFException Generic RDF Exception
     * @return a statement representing an instance of the required
     * property
     */
    public Statement getProperty(Property p) throws RDFException;
    
    /** Return a property of this statement.
     *
     * <p>The model associated with this statement is searched for a statement with
     *   this statement as subject and the specified property as predicate.  If
     *   such a statement is found it is return.  If more than one exists in the
     *   model, then it is undefined which is returned.  If no such statement
     *   exists, an exception is thrown.</p>
     * @param p the property sought
     * @throws RDFException Generic RDF exception
     * @return a statement representing an instance of the specified
     * property.
     */
    public Statement getStatementProperty(Property p) throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a resource.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The Resource which is the object of the statement.
     */
    public Resource getResource() throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The Literal which is the object of the statement.
     */
    public Literal getLiteral() throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public boolean getBoolean() throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public byte getByte() throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public short getShort() throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public int getInt() throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown iof the object is not a Literal.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public long getLong() throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public char getChar() throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public float getFloat() throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public double getDouble() throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */ 
    public String getString() throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Resource.</p>
     * @return The object of the statement.
     * @throws RDFException Generuc RDF exception.
     */    
    public Resource getResource(ResourceF f) throws RDFException;
    
    /** Return the object of the statement.
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * @return The object of the statement.
     * @param f A factory used to create the returned object.
     * @throws RDFException Generuc RDF exception.
     */    
    public Object getObject(ObjectF f) throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Resource.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public Bag getBag() throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Resource.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public Alt getAlt() throws RDFException;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Resource.</p>
     * @throws RDFException Generuc RDF exception.
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public Seq getSeq() throws RDFException;
    
    /** Return the language of the object of the statement
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * @throws RDFException Generuc RDF exception.
     * @return the language of the object of the statement
     */    
    public String getLanguage() throws RDFException;
    
    /** Return whether the Literal object is well formed XML as would result
     *         from parsing a property element with parseType="Literal"
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * @throws RDFException Generuc RDF exception.
     * @return true if the Literal object is well formed
     */    
    public boolean getWellFormed() throws RDFException;
    
    /** Set the object of the statement.
     * @return this object to enable cascading of method calls.
     * @param o The new value to be set.
     * @throws RDFException Generic RDF exception.
     */
    public Statement set(boolean o) throws RDFException;
    
    /** Set the object of the statement.
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception.
     * @return this object to enable cascading of method calls.
     */
    public Statement set(long o) throws RDFException;
    
    /** Set the object of the statement.
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception.
     * @return this object to enable cascading of method calls.
     */
    public Statement set(char o) throws RDFException;
    
    /** Set the object of the statement.
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception.
     * @return this object to enable cascading of method calls.
     */
    public Statement set(float o) throws RDFException;
    
    /** Set the object of the statement.
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception.
     * @return this object to enable cascading of method calls.
     */
    public Statement set(double o) throws RDFException;
    
    /** Set the object of the statement.
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception.
     * @return this object to enable cascading of method calls.
     */
    public Statement set(String o) throws RDFException;  
    
    /** Set the object of the statement.
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     * @param o The value to be set.
     * @param wellFormed true if o is well formed XML
     * @throws RDFException Generic RDF exception.
     * @return this object to enable cascading of method calls.
     */
    public Statement set(String o, boolean wellFormed) throws RDFException;
    
    /** Set the object of the statement.
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     * @param o The value to be set.
     * @param l the language of the String
     * @throws RDFException Generic RDF exception.
     * @return this object to enable cascading of method calls.
     */
    public Statement set(String o, String l) throws RDFException;
    
    /** Set the object of the statement.
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     * @param o The value to be set.
     * @param l the language of the String
     * @throws RDFException Generic RDF exception.
     * @return this object to enable cascading of method calls.
     */
    public Statement set(String o, String l, boolean wellFormed) 
      throws RDFException;
    
    /** Set the object of the statement.
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     * @param o The value to be set
     * @param wellFormed true if o is well formed XML
     * @throws RDFException Generic RDF exception.
     * @return this object to enable cascading of method calls.
     */
    public Statement set(RDFNode o) throws RDFException;
    
    /** Set the object of the statement.
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     *  The Object o is converted to a string representation by calling its
     *  <CODE>toString()</CODE> method.
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception.
     * @return this object to enable cascading of method calls.
     */
    public Statement set(Object o) throws RDFException;
    
    /** Remove this statement from its associated model.
     *
     *  <p>The statement with the same subject, predicate and object as this
     *  statement will be removed from the model associated with this
     *  statement.</p>
     * @throws RDFException Generic RDF exception.
     * @return this statement.
     */
    public Statement remove() throws RDFException;
    
/** Determine if this statement is the subject of any statements its associated
 *  model.
 * @param s The statement tested.
 * @throws RDFException Generic RDF Exception
 * @return true if the statement s is the subject of a statement in the model,
             false otherwise
*/ 
    boolean isReified();
    
  
    
    Triple asTriple();
    /**
     * Returns a resource representing the reification of this
     * statement. Such a resoruce is created and added to the model
     * if necessary.
     */
    Resource asResource();
    
    Model getModel();
    
    /**
     * Finds all possible resources which are
     * the reification of this statement, and for each
     * removes all four triples of the reification quad.
     */
    void removeReification();
}