/*
 *  (c) Copyright Hewlett-Packard Company 2000-2003
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
 * <p>A Statement is not a Resource, but can produce a ReifiedStatement
 * that represents it and from which the Statement can be recovered.</p>
 *
 * <p>A statement instance tracks which model it is associated with.</p>
 *
 * <p>This interface provides methods supporting typed literals.  This means
 *    that methods are provided which will translate a built in type, or an
 *    object to an RDF Literal.  This translation is done by invoking the
 *    <CODE>toString()</CODE> method of the object, or its built in equivalent.
 *    The reverse translation is also supported.  This is built in for built
 *    in types.  Factory objects, provided by the application, are used
 *    for application objects.</p>
 
 * @author bwm; additions by kers
 * @version $Name: not supported by cvs2svn $ $Revision: 1.8 $ $Date: 2003-07-15 14:44:17 $
 */
public interface Statement 
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
     
     * @return a statement representing an instance of the required
     * property
     */
    public Statement getProperty(Property p) ;
    
    /** Return a property of this statement.
     *
     * <p>The model associated with this statement is searched for a statement with
     *   this statement as subject and the specified property as predicate.  If
     *   such a statement is found it is return.  If more than one exists in the
     *   model, then it is undefined which is returned.  If no such statement
     *   exists, an exception is thrown.</p>
     * @param p the property sought
     
     * @return a statement representing an instance of the specified
     * property.
     */
    public Statement getStatementProperty(Property p) ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a resource.</p>
     * 
     * @return The Resource which is the object of the statement.
     */
    public Resource getResource() ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * 
     * @return The Literal which is the object of the statement.
     */
    public Literal getLiteral() ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * 
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public boolean getBoolean() ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * 
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public byte getByte() ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * 
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public short getShort() ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * 
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public int getInt() ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown iof the object is not a Literal.</p>
     * 
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public long getLong() ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * 
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public char getChar() ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * 
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public float getFloat() ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * 
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public double getDouble() ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * 
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */ 
    public String getString() ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Resource.</p>
     * @return The object of the statement.
     * 
     */    
    public Resource getResource(ResourceF f) ;
    
    /** Return the object of the statement.
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * @return The object of the statement.
     * @param f A factory used to create the returned object.
     * 
     */    
    public Object getObject(ObjectF f) ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Resource.</p>
     * 
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public Bag getBag() ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Resource.</p>
     * 
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public Alt getAlt() ;
    
    /** Return the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Resource.</p>
     * 
     * @return The object of the statement interpreted as a value of the
     * the specified type.
     */    
    public Seq getSeq() ;
    
    /** Return the language of the object of the statement.
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * 
     * @return the language of the object of the statement
     */    
    public String getLanguage() ;
    
    /** Return whether the Literal object is well formed XML as would result
     *         from parsing a property element with parseType="Literal".
     *
     * <p>An exception will be thrown if the object is not a Literal.</p>
     * 
     * @return true if the Literal object is well formed
     */    
    public boolean getWellFormed() ;
    
    /** change the object of the statement (S, P, X) to (S, P, o).
     * @return this object to enable cascading of method calls.
     * @param o The new value to be set.
     *
     */
    public Statement changeObject(boolean o) ;
    
    /** change the object of the statement (S, P, X) to (S, P, o).
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added and returned.</p>
     * @param o The value to be set.
     *
     * @return the new (S, P, o) statement.
     */
    public Statement changeObject(long o) ;
    
    /** change the object of the statement (S, P, X) to (S, P, o).
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added and returned.</p>
     * @param o The value to be set.
     *
     * @return the new (S, P, o) statement.
     */
    public Statement changeObject(char o) ;
    
    /** change the object of the statement (S, P, X) to (S, P, o).
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added and returned.</p>
     * @param o The value to be set.
     *
     * @return the new (S, P, o) statement.
     */
    public Statement changeObject(float o) ;
    
    /** change the object of the statement (S, P, X) to (S, P, o).
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added and returned.</p>
     * @param o The value to be set.
     *
     * @return the new (S, P, o) statement.
     */
    public Statement changeObject(double o) ;
    
    /** change the object of the statement (S, P, X) to (S, P, o).
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added and returned.</p>
     * @param o The value to be set.
     *
     * @return the new (S, P, o) statement.
     */
    public Statement changeObject(String o) ;  
    
    /** change the object of the statement (S, P, X) to (S, P, o).
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added and returned.</p>
     * @param o The value to be set.
     * @param wellFormed true if o is well formed XML
     *
     * @return the new (S, P, o) statement.
     */
    public Statement changeObject(String o, boolean wellFormed) ;
    
    /** change the object of the statement (S, P, X) to (S, P, o).
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     * @param o The value to be set.
     * @param l the language of the String
     *
     * @return the new (S, P, o) statement..
     */
    public Statement changeObject(String o, String l) ;
    
    /** change the object of the statement (S, P, X) to (S, P, o).
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     * @param o The value to be set.
     * @param l the language of the String
     *
     * @return the new (S, P, o) statement.
     */
    public Statement changeObject(String o, String l, boolean wellFormed) 
      ;
    
    /** change the object of the statement (S, P, X) to (S, P, o).
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     * @param o The value to be set
     * @return the new (S, P, o) statement.
     */
    public Statement changeObject(RDFNode o) ;
    
    /** change the object of the statement (S, P, X) to o.
     *  <p>The statement with the old value is removed from the model and 
     *  a new statement with the new value added.</p>
     *  The Object o is converted to a string representation by calling its
     *  <CODE>toString()</CODE> method.
     * @param o The value to be set.
     *
     * @return the new (S, P, o) statement.
     */
    public Statement changeObject(Object o) ;
    
    /** Remove this statement from its associated model.
     *
     *  <p>The statement with the same subject, predicate and object as this
     *  statement will be removed from the model associated with this
     *  statement.</p>
     *
     * @return this statement.
     */
    public Statement remove() ;
    
    /** 
        Determine if this statement is the subject of any statements its associated
        model.
        @return true iff this statement is the subject of a statement in the model.
    */ 
    boolean isReified();
    
    /**
        answer a ReifiedStatement object that embodies this Statement and
        is in the same Model (if any).
    */
    ReifiedStatement createReifiedStatement();
    
    /**
        answer a ReifiedStatement object that embodies this Statement, has
        the same Model, and has the given <code>uri</code>.
    */
    ReifiedStatement createReifiedStatement( String uri );
        
    /**
        answer an iterator which delivers all the reified statements in the model
        this Statement belongs to that match this Statement.
    */
    RSIterator listReifiedStatements();
    
    /**
        answer the Triple containing the appropriate Nodes corrsponding to
        the subject, predicate, and object fields.
    */
    Triple asTriple();
    
    /**
     * Returns a resource representing the reification of this
     * statement. Such a resoruce is created and added to the model
     * if necessary.
     */
    Resource asResource();
    
    /**
        get the Model this Statement was created in.
    */
    Model getModel();
    
    /**
     * Finds all possible resources which are
     * the reification of this statement, and for each
     * removes all four triples of the reification quad.
     */
    void removeReification();
}