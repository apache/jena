/*
 *  (c) Copyright Hewlett-Packard Company 2000 
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
 * ModelCon.java
 *
 * Created on 7 December 2001, 11:00
 */

package com.hp.hpl.jena.rdf.model;


/** Convenience methods which extend the {@link Model} interface.
 * <P>The {@link Model} interface provides a set of primitive operations on
 *    an RDF model.  This interface extends those methods with a 
 *    set of convenience methods.</P>
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
 * @version Release='$Name: not supported by cvs2svn $'
            Revision='$Revision: 1.2 $'
            Date='$Date: 2003-02-20 16:48:30 $'
 */
public interface ModelCon {

/** Return a Resource instance in this model.
 *
 * <p>Subsequent operations on the returned object may modify this model.</p>
 * <p>The resource is assumed to already exist in the model.  If it does not,
 * <CODE>createResource</CODE> should be used instead.</p>
 * @return a resource instance created by the factory provided
 * @param uri the URI of the resource
 * @param f the factory object
 * @throws RDFException Generic RDF Exception 
 */    
    Resource getResource(String uri, ResourceF f) throws RDFException;

/** Return a Property instance in this model.
 *
 * <p>Subsequent operations on the returned property may modify this model.</p>
 * <p>The property is assumed to already exist in the model.  If it does not,
 * <CODE>createProperty</CODE> should be used instead.</p>
 * @return a property object
 * @param uri the URI of the property
 * @throws RDFException Generic RDF Exception 
*/
    Property getProperty(String uri) throws RDFException;

/** Return a Bag instance in this model.
 *
 * <p>Subsequent operations on the returned bag may modify this model.</p>
 * <p>The bag is assumed to already exist in the model.  If it does not,
 * <CODE>createBag</CODE> should be used instead.</p>
 * @return a bag instance
 * @param uri the URI of the bag.
 * @throws RDFException Generic RDF Exception
 */ 
    Bag getBag(String uri) throws RDFException;

/** Return a bag instance based on a given resource.
 *
 * <p> This method enables an application to treat any resource as a bag.
 *     It is in effect an unsafe downcast.</p>
 *
 * <p>Subsequent operations on the returned bag may modify this model.</p>
 * <p>The bag is assumed to already exist in the model.  If it does not,
 * <CODE>createBag</CODE> should be used instead.</p>
 * @return a bag instance
 * @param r an untyped Resource instance 
 * @throws RDFException Generic RDF Exception
 */ 
    Bag getBag(Resource r) throws RDFException;

/** Return an Alt instance in this model.
 *
 * <p>Subsequent operations on the returned object may modify this model.</p>
 * <p>The alt is assumed to already exist in the model.  If it does not,
 * <CODE>createAlt</CODE> should be used instead.</p>
 * @return an alt instance
 * @param uri the URI of the alt
 * @throws RDFException Generic RDF Exception
 */ 
    Alt getAlt(String uri) throws RDFException;

/** Return an Alt instance based on a given resource.
 *
 * <p> This method enables an application to treat any resource as an Alt.
 *     It is in effect an unsafe downcast.</p>
 *
 * <p>Subsequent operations on the returned Alt may modify this model.</p>
 * <p>The bag is assumed to already exist in the model.  If it does not,
 * <CODE>createAlt</CODE> should be used instead.</p>
 * @return an Alt instance
 * @param r an untyped Resource instance
 * @throws RDFException Generic RDF Exception
 */ 
    Alt getAlt(Resource r) throws RDFException;
/** Return a Seq instance in this model.
 *
 * <p>Subsequent operations on the returned bag may modify this model.</p>
 * <p>The seq is assumed to already exist in the model.  If it does not,
 * <CODE>createSeq</CODE> should be used instead.</p>
 * @return a seq instance
 * @param uri the URI of the seq
 * @throws RDFException Generic RDF Exception
 */ 
    Seq getSeq(String uri) throws RDFException;

/** Return a Seq instance based on a given resource.
 *
 * <p> This method enables an application to treat any resource as a Seq.
 *     It is in effect an unsafe downcast.</p>
 *
 * <p>Subsequent operations on the returned Seq may modify this model.</p>
 * <p>The Seq is assumed to already exist in the model.  If it does not,
 * <CODE>createAlt</CODE> should be used instead.</p>
 * @return an Alt instance
 * @param r an untyped Resource instance
 * @throws RDFException Generic RDF Exception
 */ 
    Seq getSeq(Resource r) throws RDFException;

/** Create a new anonymous resource with a given type.
 *
 * <p> Subsequent operations on the returned resource may modify this model.
 * </p>
 * <p> The resource is created and an rdf:type property added to the model
 * to specify its type. </p>
 * @ param type the type of the resource to be created.
 * @return a new anonymous resource linked to this model.
 * @param type A resource representing the RDF type of the new resource.
 * @throws RDFException Generic RDF exception
 */
    public Resource createResource(Resource type) throws RDFException;

/** Create a new resource with a given type.
 *
 * <p> Subsequent operations on the returned resource may modify this model.
 * </p>
 * <p> The resource is created and an rdf:type property added to the model
 * to specify its type. </p>
 * @ param type the type of the resource to be created.
 * @return a new resource linked to this model.
 * @param uri The URI of the new resource.
 * @param type A resource representing the RDF type of the new resource.
 * @throws RDFException Generic RDF exception.
 */
    public Resource createResource(String uri, Resource type)
                                    throws RDFException;

/** Create a new anonymous resource using the supplied factory
 *
 * <p> Subsequent operations on the returned resource may modify this model.
 * </p>
 * @return a new anonymous resource linked to this model.
 * @param f A factory object to create the returned object.
 * @throws RDFException Generic RDF exception.
 */
    public Resource createResource(ResourceF f) throws RDFException;
 
/** Create a new resource using the supplied factory
 *
 * <p> Subsequent operations on the returned resource may modify this model.
 * </p>
 * @return a new resource linked to this model.
 * @param uri the URI of the resource
 * @param f A factory to create the returned object.
 * @throws RDFException Generic RDF exception.
 */   
    public Resource createResource(String uri, ResourceF f) throws RDFException;

/** Create a property
 *
 * <p> Subsequent operations on the returned property may modify this model.
 * </p>
 * @param uri the URI of the property
 * @throws RDFException Generic RDF exception
 * @return a property instance
 */
    public Property createProperty(String uri) throws RDFException;

    /** create a literal from a boolean value.
     *
     * <p> The value is converted to a string using its <CODE>toString</CODE>
     * method. </p>
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createLiteral(boolean v) throws RDFException; 
    /** create a literal from an integer value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */   
    public Literal createLiteral(long v) throws RDFException;
    /** create a literal from a char value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createLiteral(char v) throws RDFException;
    /** create a literal from a float value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createLiteral(float v) throws RDFException;
    /** create a literal from a double value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createLiteral(double v) throws RDFException;
    
    /** create a literal from a String value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createLiteral(String v) throws RDFException;
    
    /** create a literal from an Object.
     *
     * @return a new literal representing the value v
     * @param v the value of the literal.
     * @throws RDFException generic RDF exception 
     */
    public Literal createLiteral(Object v) throws RDFException;
 
    /** create a type literal from a boolean value.
     *
     * <p> The value is converted to a string using its <CODE>toString</CODE>
     * method. </p>
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(boolean v) throws RDFException; 
    
    /** create a typed literal from an integer value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */   
    public Literal createTypedLiteral(int v) throws RDFException;
    
    /** create a typed literal from an integer value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */   
    public Literal createTypedLiteral(long v) throws RDFException;
    
    /** create a typed literal from a char value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(char v) throws RDFException;
    
    /** create a typed literal from a float value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(float v) throws RDFException;
    
    /** create a typed literal from a double value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(double v) throws RDFException;
    
    /** create a typed literal from a String value.
     *
     * @param v the value of the literal
     * @throws RDFException generic RDF exception
     * @return a new literal representing the value v
     */
    public Literal createTypedLiteral(String v) throws RDFException;
    
    /** create a literal from an Object.
     *
     * @return a new literal representing the value v
     * @param v the value of the literal.
     * @throws RDFException generic RDF exception 
     */
    public Literal createTypedLiteral(Object v) throws RDFException;

    /**
     * Build a typed literal from its lexical form. The
     * lexical form will be parsed now and the value stored. If
     * the form is not legal this will throw an exception.
     * 
     * @param lex the lexical form of the literal
     * @param lang the optional language tag
     * @param typeURI the uri of the type of the literal, null for old style "plain" literals
     * @throws DatatypeFormatException if lex is not a legal form of dtype
     */
    public Literal createTypedLiteral(String lex, String lang, String typeURI) 
                                        throws RDFException;
    
    /**
     * Build a typed literal from its value form.
     * 
     * @param value the value of the literal
     * @param lang the optional language tag
     * @param typeURI the URI of the type of the literal, null for old style "plain" literals
     */
    public Literal createTypedLiteral(Object value, String lang, String typeURI);
    
    /** Create a Statement instance.
     *
     * <p>Subsequent operations on the statement or any of its parts may
     * modify this model.</p>
     * <p>Creating a statement does not add it to the set of statements in the
     * model. </p>
     * <p>The value o will be converted to a Literal.</P>
     * @param s the subject of the statement
     * @param p the predicate of the statement
     * @param o is the value to be the object of the statement
     * @throws RDFException generic RDF exception
     * @return the new statement
     */
    public Statement createStatement(Resource s, Property p, boolean o) 
                                      throws RDFException;
    
    /** Create a Statement instance.
     *
     * <p>Subsequent operations on the statement or any of its parts may
     * modify this model.</p>
     * <p>Creating a statement does not add it to the set of statements in the
     * model. </p>
     * <p>The value o will be converted to a Literal.</P>
     * @param s the subject of the statement
     * @param p the predicate of the statement
     * @param o is the value to be the object of the statement
     * @throws RDFException generic RDF exception
     * @return the new statement
     */
    public Statement createStatement(Resource s, Property p, long o)
                                      throws RDFException;
    
    /** Create a Statement instance.
     *
     * <p>Subsequent operations on the statement or any of its parts may
     * modify this model.</p>
     * <p>Creating a statement does not add it to the set of statements in the
     * model. </p>
     * <p>The value o will be converted to a Literal.</P>
     * @param s the subject of the statement
     * @param p the predicate of the statement
     * @param o is the value to be the object of the statement
     * @throws RDFException generic RDF exception
     * @return the new statement
     */
    public Statement createStatement(Resource s, Property p, char o)
                                      throws RDFException;
    
    /** Create a Statement instance.
     *
     * <p>Subsequent operations on the statement or any of its parts may
     * modify this model.</p>
     * <p>Creating a statement does not add it to the set of statements in the
     * model. </p>
     * <p>The value o will be converted to a Literal.</P>
     * @param s the subject of the statement
     * @param p the predicate of the statement
     * @param o is the value to be the object of the statement
     * @throws RDFException generic RDF exception
     * @return the new statement
     */
    public Statement createStatement(Resource s, Property p, float o)
                                      throws RDFException;
    
    /** Create a Statement instance.
     *
     * <p>Subsequent operations on the statement or any of its parts may
     * modify this model.</p>
     * <p>Creating a statement does not add it to the set of statements in the
     * model. </p>
     * <p>The value o will be converted to a Literal.</P>
     * @param s the subject of the statement
     * @param p the predicate of the statement
     * @param o is the value to be the object of the statement
     * @throws RDFException generic RDF exception
     * @return the new statement
     */
    public Statement createStatement(Resource s, Property p, double o)
                                      throws RDFException;
    
    /** Create a Statement instance.
     *
     * <p>Subsequent operations on the statement or any of its parts may
     * modify this model.</p>
     * <p>Creating a statement does not add it to the set of statements in the
     * model. </p>
     * <p>The Object o will be converted to a Literal.</P>
     * @param s the subject of the statement
     * @param p the predicate of the statement
     * @param o is the value to be the object of the statement
     * @throws RDFException generic RDF exception
     * @return the new statement
     */
    public Statement createStatement(Resource s, Property p, String o)  
                                      throws RDFException;
    
    /** Create a Statement instance.
     *
     * <p>Subsequent operations on the statement or any of its parts may
     * modify this model.</p>
     * <p>Creating a statement does not add it to the set of statements in the
     * model. </p>
     * <p>The Object o will be converted to a Literal.</P>
     * @param s the subject of the statement
     * @param p the predicate of the statement
     * @param o is the value to be the object of the statement
     * @param l the language associated with the object
     * @throws RDFException generic RDF exception
     * @return the new statement
     */
    public Statement createStatement(Resource s, Property p, String o, String l)  
                                      throws RDFException;

    /** Create a Statement instance.
     *
     * <p>Subsequent operations on the statement or any of its parts may
     * modify this model.</p>
     * <p>Creating a statement does not add it to the set of statements in the
     * model. </p>
     * <p>The Object o will be converted to a Literal.</P>
     * @param s the subject of the statement
     * @param p the predicate of the statement
     * @param o is the value to be the object of the statement
     * @param wellFormed true if the string is well formed XML
     * @throws RDFException generic RDF exception
     * @return the new statement
     */
    public Statement createStatement(Resource s, Property p, String o,
                                     boolean wellFormed) throws RDFException;
    
    /** Create a Statement instance.
     *
     * <p>Subsequent operations on the statement or any of its parts may
     * modify this model.</p>
     * <p>Creating a statement does not add it to the set of statements in the
     * model. </p>
     * <p>The Object o will be converted to a Literal.</P>
     * @param s the subject of the statement
     * @param p the predicate of the statement
     * @param o is the value to be the object of the statement
     * @param l the language associated with the object
     * @param wellFormed true of the string is well formed XML
     * @throws RDFException generic RDF exception
     * @return the new statement
     */
    public Statement createStatement(Resource s, Property p, String o, String l,
                                     boolean wellFormed) throws RDFException;
    
    /** Create a Statement instance.
     *
     * <p>Subsequent operations on the statement or any of its parts may
     * modify this model.</p>
     * <p>Creating a statement does not add it to the set of statements in the
     * model. </p>
     * <p>The value o will be converted to a Literal.</P>
     * @param s the subject of the statement
     * @param p the predicate of the statement
     * @param o is the value to be the object of the statement
     * @throws RDFException generic RDF exception
     * @return the new statement
     */
    public Statement createStatement(Resource s, Property p, Object o)  
                                      throws RDFException;
    
    /** Create a new anonymous bag.
     *
     * <p>Subsequent operations on the bag or any of its parts may
     * modify this model.</p>
     * <p>A statement defining the type of the new bag is added to this model.
     * </p>
     * @throws RDFException Generic RDF exception.
     * @return a new anonymous bag.
     */
    public Bag createBag() throws RDFException;
    
    /** Create a new bag.
     *
     * <p>Subsequent operations on the bag or any of its parts may
     * modify this model.</p>
     * <p>A statement defining the type of the new bag is added to this model.
     * </p>
     * @param uri The URI of the new Bag.
     * @throws RDFException Generic RDF exception.
     * @return a new bag.
     */
    public Bag createBag(String uri) throws RDFException;
       
    /** Create a new anonymous alt.
     *
     * <p>Subsequent operations on the alt or any of its parts may
     * modify this model.</p>
     * <p>A statement defining the type of the new alt is added to this model.
     * </p>
     * @throws RDFException Generic RDF exception.
     * @return a new anonymous alt.
     */
    public Alt createAlt() throws RDFException;
    
    /** Create a new alt.
     *
     * <p>Subsequent operations on the alt or any of its parts may
     * modify this model.</p>
     * <p>A statement defining the type of the new alt is added to this model.
     * </p>
     * @param uri The URI of the new alt.
     * @throws RDFException Generic RDF exception.
     * @return a new alt.
     */
    public Alt createAlt(String uri) throws RDFException;
       
    /** Create a new anonymous seq.
     *
     * <p>Subsequent operations on the seq or any of its parts may
     * modify this model.</p>
     * <p>A statement defining the type of the new seq is added to this model.
     * </p>
     * @throws RDFException Generic RDF exception.
     * @return a new anonymous seq.
     */
    public Seq createSeq() throws RDFException;
    
    /** Create a new seq.
     *
     * <p>Subsequent operations on the seq or any of its parts may
     * modify this model.</p>
     * <p>A statement defining the type of the new seq is added to this model.
     * </p>
     * @param uri The URI of the new seq.
     * @throws RDFException Generic RDF exception.
     * @return a new seq.
     */
    public Seq createSeq(String uri) throws RDFException;

/** add a statement to this model
 * @return this model
 * @param s the subject of the statement to add
 * @param p the predicate of the statement to add
 * @param o the object of the statement to add
 * @throws RDFException Generic RDF Exception
 */ 
    Model add(Resource s, Property p, RDFNode o)     throws RDFException;

/** add a statement to this model
 *
 * @return this model
 * @param s the subject of the statement to add
 * @param p the predicate of the statement to add
 * @param o the object of the statement to add
 * @throws RDFException Generic RDF Exception
 */ 
    Model add(Resource s, Property p, boolean o) throws RDFException;
/** add a statement to this model
 *
 * @return this model
 * @param s the subject of the statement to add
 * @param p the predicate of the statement to add
 * @param o the object of the statement to add
 * @throws RDFException Generic RDF Exception
 */ 
    Model add(Resource s, Property p, long o) throws RDFException;
/** add a statement to this model
 *
 * @return this model
 * @param s the subject of the statement to add
 * @param p the predicate of the statement to add
 * @param o the object of the statement to add
 * @throws RDFException Generic RDF Exception
 */ 
    Model add(Resource s, Property p, char o) throws RDFException;
/** add a statement to this model
 *
 * @return this model
 * @param s the subject of the statement to add
 * @param p the predicate of the statement to add
 * @param o the object of the statement to add
 * @throws RDFException Generic RDF Exception
 */ 
    Model add(Resource s, Property p, float o) throws RDFException;
/** add a statement to this model
 *
 * @return this model
 * @param s the subject of the statement to add
 * @param p the predicate of the statement to add
 * @param o the object of the statement to add
 * @throws RDFException Generic RDF Exception
 */ 
    Model add(Resource s, Property p, double o) throws RDFException;

/** add a statement to this model
 *
 * @return this model
 * @param s the subject of the statement to add
 * @param p the predicate of the statement to add
 * @param o the object of the statement to add
 * @throws RDFException Generic RDF Exception
 */ 
    Model add(Resource s, Property p, String o) throws RDFException;
 
/** add a statement to this model
 *
 * @return this model
 * @param s the subject of the statement to add
 * @param p the predicate of the statement to add
 * @param o the object of the statement to add
 * @param wellFormed true if o is well formed XML
 * @throws RDFException Generic RDF Exception
 */ 
    Model add(Resource s, Property p, String o, boolean wellFormed)
      throws RDFException;

/** add a statement to this model
 *
 * @return this model
 * @param s the subject of the statement to add
 * @param p the predicate of the statement to add
 * @param o the object of the statement to add
 * @param l the language associated with the object
 * @throws RDFException Generic RDF Exception
 */ 
    Model add(Resource s, Property p, String o, String l) throws RDFException;

/** add a statement to this model
 *
 * @return this model
 * @param s the subject of the statement to add
 * @param p the predicate of the statement to add
 * @param o the object of the statement to add
 * @param l the language associated with the object
 * @param wellFormed true if o is well formed XML
 * @throws RDFException Generic RDF Exception
 */ 
    Model add(Resource s, Property p, String o, String l, boolean wellFormed)
      throws RDFException;

/** add a statement to this model
 *
 * @return this model
 * @param s the subject of the statement to add
 * @param p the predicate of the statement to add
 * @param o the object of the statement to add
 * @throws RDFException Generic RDF Exception
 */ 
    Model add(Resource s, Property p, Object o) throws RDFException;

/** Remove all the Statements returned by an iterator.
 * @return this model
 * @param iter the iterator which returns the statements to be removed.
 * @throws RDFException Generic RDF Exception
 */ 
    Model remove(StmtIterator iter) throws RDFException;

/** Remove all the Statements in a given model.
 * @return this model
 * @param m the model containing the statements to be removed.
 * @throws RDFException Generic RDF Exception
 */ 
    Model remove(Model m) throws RDFException;

/** Find all the statements matching a pattern.
 * <p>Return an iterator over all the statements in a model
 *  that match a pattern.  The statements selected are those
 *  whose subject matches the <code>subject</code> argument,
 *  whose predicate matches the <code>predicate</code> argument
 *  and whose object matchesthe <code>object</code> argument.
 *  If an argument is <code>null</code> it matches anything.</p>
 * @return an iterator over the subjects
 * @param subject   The subject sought
 * @param predicate The predicate sought
 * @param object    The value sought
 * @throws RDFException Generic RDF Exception
 */ 
    StmtIterator listStatements(Resource subject,
                                Property predicate,
                                RDFNode  object)
                                           throws RDFException;

/** Find all the statements matching a pattern.
 * <p>Return an iterator over all the statements in a model
 *  that match a pattern.  The statements selected are those
 *  whose subject matches the <code>subject</code> argument,
 *  whose predicate matches the <code>predicate</code> argument
 *  and whose object matchesthe <code>object</code> argument.
 *  If an argument is <code>null</code> it matches anything.</p>
 * @return an iterator over the subjects
 * @param subject   The subject sought
 * @param predicate The predicate sought
 * @param object    The value sought
 * @throws RDFException Generic RDF Exception
 */ 
    StmtIterator listStatements(Resource subject,
                                Property predicate,
                                boolean object)
                                           throws RDFException;

/** Find all the statements matching a pattern.
 * <p>Return an iterator over all the statements in a model
 *  that match a pattern.  The statements selected are those
 *  whose subject matches the <code>subject</code> argument,
 *  whose predicate matches the <code>predicate</code> argument
 *  and whose object matchesthe <code>object</code> argument.
 *  If an argument is <code>null</code> it matches anything.</p>
 * @return an iterator over the subjects
 * @param subject   The subject sought
 * @param predicate The predicate sought
 * @param object    The value sought
 * @throws RDFException Generic RDF Exception
 */ 
    StmtIterator listStatements(Resource subject,
                                Property predicate,
                                long     object)
                                           throws RDFException;

/** Find all the statements matching a pattern.
 * <p>Return an iterator over all the statements in a model
 *  that match a pattern.  The statements selected are those
 *  whose subject matches the <code>subject</code> argument,
 *  whose predicate matches the <code>predicate</code> argument
 *  and whose object matchesthe <code>object</code> argument.
 *  If an argument is <code>null</code> it matches anything.</p>
 * @return an iterator over the subjects
 * @param subject   The subject sought
 * @param predicate The predicate sought
 * @param object    The value sought
 * @throws RDFException Generic RDF Exception
 */ 
    StmtIterator listStatements(Resource subject,
                                Property predicate,
                                char    object)
                                           throws RDFException;

/** Find all the statements matching a pattern.
 * <p>Return an iterator over all the statements in a model
 *  that match a pattern.  The statements selected are those
 *  whose subject matches the <code>subject</code> argument,
 *  whose predicate matches the <code>predicate</code> argument
 *  and whose object matchesthe <code>object</code> argument.
 *  If an argument is <code>null</code> it matches anything.</p>
 * @return an iterator over the subjects
 * @param subject   The subject sought
 * @param predicate The predicate sought
 * @param object    The value sought
 * @throws RDFException Generic RDF Exception
 */ 
    StmtIterator listStatements(Resource subject,
                                Property predicate,
                                float    object)
                                           throws RDFException;

/** Find all the statements matching a pattern.
 * <p>Return an iterator over all the statements in a model
 *  that match a pattern.  The statements selected are those
 *  whose subject matches the <code>subject</code> argument,
 *  whose predicate matches the <code>predicate</code> argument
 *  and whose object matchesthe <code>object</code> argument.
 *  If an argument is <code>null</code> it matches anything.</p>
 * @return an iterator over the subjects
 * @param subject   The subject sought
 * @param predicate The predicate sought
 * @param object    The value sought
 * @throws RDFException Generic RDF Exception
 */ 
    StmtIterator listStatements(Resource subject,
                                Property predicate,
                                double  object)
                                           throws RDFException;

/** Find all the statements matching a pattern.
 * <p>Return an iterator over all the statements in a model
 *  that match a pattern.  The statements selected are those
 *  whose subject matches the <code>subject</code> argument,
 *  whose predicate matches the <code>predicate</code> argument
 *  and whose object matchesthe <code>object</code> argument.
 *  If an argument is <code>null</code> it matches anything.</p>
 * @return an iterator over the subjects
 * @param subject   The subject sought
 * @param predicate The predicate sought
 * @param object    The value sought
 * @throws RDFException Generic RDF Exception
 */ 
    StmtIterator listStatements(Resource subject,
                                Property predicate,
                                String   object)
                                           throws RDFException;

/** Find all the statements matching a pattern.
 * <p>Return an iterator over all the statements in a model
 *  that match a pattern.  The statements selected are those
 *  whose subject matches the <code>subject</code> argument,
 *  whose predicate matches the <code>predicate</code> argument
 *  and whose object matchesthe <code>object</code> argument.
 *  If an argument is <code>null</code> it matches anything.</p>
 * @return an iterator over the subjects
 * @param subject   The subject sought
 * @param predicate The predicate sought
 * @param object    The value sought
 * @param lang      The lang code ofthe string.
 * @throws RDFException Generic RDF Exception
 */ 
    StmtIterator listStatements(Resource subject,
                                Property predicate,
                                String   object,
                                String   lang)
                                           throws RDFException;

/** List all subjects with a given property and property value
 * @return an iterator over the subjects
 * @param p The predicate sought
 * @param o The value sought
 * @throws RDFException Generic RDF Exception
 */ 
    ResIterator listSubjectsWithProperty(Property p, boolean o)
                                           throws RDFException;

/** List all the subjects with a given property and property value
 * @return an iterator over the subjects
 * @param p The predicate sought
 * @param o The value sought
 * @throws RDFException Generic RDF Exception
 */ 
    ResIterator listSubjectsWithProperty(Property p, long o)
                                           throws RDFException;

/** List all subjects with a given property and property value
 * @return an iterator over the subjects
 * @param p The predicate sought
 * @param o The value sought
 * @throws RDFException Generic RDF Exception
 */ 
    ResIterator listSubjectsWithProperty(Property p, char o)
                                          throws RDFException;

/** List all subjects with a given property and property value
 * @return an iterator over the subjects
 * @param p The predicate sought
 * @param o The value sought
 * @throws RDFException Generic RDF Exception
 */ 
    ResIterator listSubjectsWithProperty(Property p, float o)
                                           throws RDFException;
/** lists all subjects with a given property and property value
 * @return an iterator over the set of subjects
 * @param p The property sought.
 * @param o The property value sought.
 * @throws RDFException Generic RDF Exception
 */ 
    ResIterator listSubjectsWithProperty(Property p, double o)
                                           throws RDFException;
/** lists all subjects with a given property and property value
 * @return an iterator over the set of subjects
 * @param p The predicate sought.
 * @param o The property value sought.
 * @throws RDFException Generic RDF Exception
 */ 
    ResIterator listSubjectsWithProperty(Property p, String o)
                                          throws RDFException;
/** lists all subjects with a given property and property value
 * @return an iterator over the set of subjects
 * @param p The predicate sought.
 * @param o The property value sought.
 * @param l the language associated with the object
 * @throws RDFException Generic RDF Exception
 */ 
    ResIterator listSubjectsWithProperty(Property p, String o, String l)
                                          throws RDFException;

/** List all subjects with a given property and property value
 * @return an iterator over the subjects
 * @param p The predicate sought
 * @param o The value sought
 * @throws RDFException Generic RDF Exception
 */ 
    ResIterator listSubjectsWithProperty(Property p, Object o)
                                           throws RDFException;

/** Determine if a statement is present in this model.
 * @return true if the statement with subject s, property p and object o
 * is in the model, false otherwise
 * @param s The subject of the statment tested.
 * @param p The predicate of the statement tested.
 * @param o The object of the statement tested.
 * @throws RDFException Generic RDF Exception
 */ 
    boolean contains(Resource s, Property p, boolean o) throws RDFException;

/** Determine if a statement is present in this model.
 * @return true if the statement with subject s, property p and object o
 * is in the model, false otherwise
 * @param s The subject of the statment tested.
 * @param p The predicate of the statement tested.
 * @param o The object of the statement tested.
 * @throws RDFException Generic RDF Exception
 */ 
    boolean contains(Resource s, Property p, long o) throws RDFException;

/** Determine if a statement is present in this model.
 * @return true if the statement with subject s, property p and object o
 * is in the model, false otherwise
 * @param s The subject of the statment tested.
 * @param p The predicate of the statement tested.
 * @param o The object of the statement tested.
 * @throws RDFException Generic RDF Exception
 */ 
    boolean contains(Resource s, Property p, char o) throws RDFException;

/** Determine if a statement is present in this model.
 * @return true if the statement with subject s, property p and object o
 * is in the model, false otherwise
 * @param s The subject of the statment tested.
 * @param p The predicate of the statement tested.
 * @param o The object of the statement tested.
 * @throws RDFException Generic RDF Exception
 */ 
    boolean contains(Resource s, Property p, float o) throws RDFException;

/** Determine if a statement is present in this model.
 * @return true if the statement with subject s, property p and object o
 * is in the model, false otherwise
 * @param s The subject of the statment tested.
 * @param p The predicate of the statement tested.
 * @param o The object of the statement tested.
 * @throws RDFException Generic RDF Exception
 */ 
    boolean contains(Resource s, Property p, double o) throws RDFException;

/** Determine if a statement is present in this model.
 * @return true if the statement with subject s, property p and object o
 * is in the model, false otherwise
 * @param s The subject of the statment tested.
 * @param p The predicate of the statement tested.
 * @param o The object of the statement tested.
 * @throws RDFException Generic RDF Exception
 */ 
    boolean contains(Resource s, Property p, String o) throws RDFException;

/** Determine if a statement is present in this model.
 * @return true if the statement with subject s, property p and object o
 * is in the model, false otherwise
 * @param s The subject of the statment tested.
 * @param p The predicate of the statement tested.
 * @param o The object of the statement tested.
 * @param l the language associated with the object
 * @throws RDFException Generic RDF Exception
 */ 
    boolean contains(Resource s, Property p, String o, String l)
       throws RDFException;

/** Determine if a statement is present in this model.
 * @return true if the statement with subject s, property p and object o
 * is in the model, false otherwise
 * @param s The subject of the statment tested.
 * @param p The predicate of the statement tested.
 * @param o The object of the statement tested.
 * @throws RDFException Generic RDF Exception
 */ 
    boolean contains(Resource s, Property p, Object o) throws RDFException;
}