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
import com.hp.hpl.jena.shared.PropertyNotFoundException;


/** An RDF Resource.

    Resource instances when created may be associated with a specific model.
    Resources created <i>by</i> a model will refer to that model, and support
    a range of methods, such as <code>getProperty()</code> and
    <code>addProperty()</code> which will access or modify that model.  This
    enables the programmer to write code in a compact and easy style.
    
<p>
    Resources created by ResourceFactory will not refer to any model, and will 
    not permit operations which require a model. Such resources are useful
    as general constants.
 
  <p>This interface provides methods supporting typed literals.  This means
     that methods are provided which will translate a built in type, or an
     object to an RDF Literal.  This translation is done by invoking the
     <CODE>toString()</CODE> method of the object, or its built in equivalent.
     The reverse translation is also supported.  This is built in for built
     in types.  Factory objects, provided by the application, are used
     for application objects.</p>
  <p>This interface provides methods for supporting enhanced resources.  An
     enhanced resource is a resource to which the application has added
     behaviour.  RDF containers are examples of enhanced resources built in
     to this package.  Enhanced resources are supported by encapsulating a
     resource created by an implementation in another class which adds
     the extra behaviour.  Factory objects are used to construct such
     enhanced resources.</p>
*/
public interface Resource extends RDFNode {

    /** Returns an a unique identifier for anonymous resources.
     *
     * <p>The id is unique within the scope of a particular implementation.  All
     * models within an implementation will use the same id for the same anonymous
     * resource.</p>
     *
     * <p>This method is undefined if called on resources which are not anonymous
     * and may raise an exception.</p>
     * @return A unique id for an anonymous resource.
     */
    public AnonId getId();
    
    /**
        Override RDFNode.inModel() to produce a staticly-typed Resource
        in the given Model.
    */
    @Override
    public Resource inModel( Model m );

    /**
        Answer true iff this Resource is a URI resource with the given URI.
        Using this is preferred to using getURI() and .equals().
    */
    public boolean hasURI( String uri );

    /** Return the URI of the resource, or null if it's a bnode.
     * @return The URI of the resource, or null if it's a bnode.
     */
    public String getURI();

    /** Returns the namespace associated with this resource.
     * @return The namespace for this property.
     */
    public String getNameSpace();

    /** Returns the name of this resource within its namespace.
     * @return The name of this property within its namespace.
     */
    public String getLocalName();

    /** Return a string representation of the resource.
     *
     * Returns the URI of the resource unless the resource is anonymous
     * in which case it returns the id of the resource enclosed in square
     * brackets.
     * @return Return a string representation of the resource.
     * if it is anonymous.
     */
    @Override
    public String toString();

    /** Determine whether two objects represent the same resource.
     *
     * <p>A resource can only be equal to another resource.
     * If both resources are not anonymous, then they are equal if the URI's are
     * equal.  If both resources are anonymous, they are equal only if there Id's
     * are the same.  If one resource is anonymous and the other is not, then they
     * are not equal.</p>
     * @param o The object to be compared.
     * @return true if and only if both objects are equal
     */
    @Override
    public boolean equals( Object o );

    /** Get a property value of this resource.
     *
     * <p>The model associated with the resource instance is searched for statements
     * whose subject is this resource and whose predicate is p.  If such a statement
     * is found, it is returned.  If several such statements are found, any one may
     * be returned.  If no such statements are found, an exception is thrown.</p>
     * @param p The property sought.
     * @return some (this, p, ?O) statement if one exists
     * @throws PropertyNotFoundException if no such statement found
     */
    public Statement getRequiredProperty( Property p );

    /**
     Answer some statement (this, p, O) in the associated model. If there are several
     such statements, any one of them may be returned. If no such statements exist,
     null is returned - in this is differs from getRequiredProperty.
     @param p the property sought
     @return a statement (this, p, O), or null if no such statements exist here
     */
    public Statement getProperty( Property p );

    /** List all the values of the property p.
     *
     * <p>Returns an iterator over all the statements in the associated model whose
     * subject is this resource and whose predicate is p.</p>
     * @param p The predicate sought.
     * @return An iterator over the statements.
     */
    public StmtIterator listProperties( Property p );

    /** Return an iterator over all the properties of this resource.
     *
     * <p>The model associated with this resource is search and an iterator is
     * returned which iterates over all the statements which have this resource
     * as a subject.</p>
     * @return An iterator over all the statements about this object.
     */
    public StmtIterator listProperties();

    /**
        Add the property <code>p</code> with the typed-literal value <code>o</code>
        to this resource, <i>ie</i> add (this, p, typed(o)) to this's model. Answer
        this resource. The typed literal is equal to one constructed by using
        <code>this.getModel().createTypedLiteral(o)</code>.
    */
    public Resource addLiteral( Property p, boolean o );
    
    /**
        Add the property <code>p</code> with the typed-literal value <code>o</code>
        to this resource, <i>ie</i> add (this, p, typed(o)) to this's model. Answer
        this resource. The typed literal is equal to one constructed by using
        <code>this.getModel().createTypedLiteral(o)</code>.
    */
    public Resource addLiteral( Property p, long o );
    
    /**
        Add the property <code>p</code> with the typed-literal value <code>o</code>
        to this resource, <i>ie</i> add (this, p, typed(o)) to this's model. Answer
        this resource. The typed literal is equal to one constructed by using
        <code>this.getModel().createTypedLiteral(o)</code>.
    */
   public Resource addLiteral( Property p, char o );

   /**
       Add the property <code>p</code> with the typed-literal value <code>o</code>
       to this resource, <i>ie</i> add (this, p, typed(o)) to this's model. Answer
       this resource. The typed literal is equal to one constructed by using
       <code>this.getModel().createTypedLiteral(o)</code>.
    */
    public Resource addLiteral( Property value, double d );

    /**
        Add the property <code>p</code> with the typed-literal value <code>o</code>
        to this resource, <i>ie</i> add (this, p, typed(o)) to this's model. Answer
        this resource. The typed literal is equal to one constructed by using
        <code>this.getModel().createTypedLiteral(o)</code>.
    */
    public Resource addLiteral( Property value, float d );

    /**
        Add the property <code>p</code> with the typed-literal value <code>o</code>
        to this resource, <i>ie</i> add (this, p, typed(o)) to this's model. Answer
        this resource. The typed literal is equal to one constructed by using
        <code>this.getModel().createTypedLiteral(o)</code>.
    */
    public Resource addLiteral( Property p, Object o );

    /**
        Add the property <code>p</code> with the pre-constructed Literal value 
        <code>o</code> to this resource, <i>ie</i> add (this, p, o) to this's 
        model. Answer this resource. <b>NOTE</b> thjat this is distinct from the
        other addLiteral methods in that the Literal is not turned into a Literal.
    */    
    public Resource addLiteral( Property p, Literal o );
    
    /** Add a property to this resource.
     *
     * <p>A statement with this resource as the subject, p as the predicate and o
     * as the object is added to the model associated with this resource.</p>
     * @param p The property to be added.
     * @param o The value of the property to be added.
     * @return This resource to allow cascading calls.
     */
    public Resource addProperty( Property p, String o );

    /** Add a property to this resource.
     *
     * <p>A statement with this resource as the subject, p as the predicate and o
     * as the object is added to the model associated with this resource.</p>
     * @param p The property to be added.
     * @param o The value of the property to be added.
     * @param l the language of the property
     * @return This resource to allow cascading calls.
     */
    public Resource addProperty( Property p, String o, String l );

    /** Add a property to this resource.
    *
    * <p>A statement with this resource as the subject, p as the predicate and o
    * as the object is added to the model associated with this resource.</p>
    * @param p The property to be added.
    * @param lexicalForm  The lexical form of the literal
    * @param datatype     The datatype
    * @return This resource to allow cascading calls.
    */
   public Resource addProperty( Property p, String lexicalForm, RDFDatatype datatype );

    /** Add a property to this resource.
     *
     * <p>A statement with this resource as the subject, p as the predicate and o
     * as the object is added to the model associated with this resource.</p>
     * @param p The property to be added.
     * @param o The value of the property to be added.
     * @return This resource to allow cascading calls.
     */
    public Resource addProperty( Property p, RDFNode o );

    /** Determine whether this resource has any values for a given property.
     * @param p The property sought.
     * @return true if and only if this resource has at least one
     * value for the property.
     */
    public boolean hasProperty( Property p );

    /**
        Answer true iff this resource has the value <code>o</code> for
        property <code>p</code>. <code>o</code> is interpreted as
        a typed literal with the appropriate RDF type.
    */
    public boolean hasLiteral( Property p, boolean o );
    
    /**
        Answer true iff this resource has the value <code>o</code> for
        property <code>p</code>. <code>o</code> is interpreted as
        a typed literal with the appropriate RDF type.
    */
    public boolean hasLiteral( Property p, long o );

    /**
        Answer true iff this resource has the value <code>o</code> for
        property <code>p</code>. <code>o</code> is interpreted as
        a typed literal with the appropriate RDF type.
    */
    public boolean hasLiteral( Property p, char o );
    
    /**
        Answer true iff this resource has the value <code>o</code> for
        property <code>p</code>. <code>o</code> is interpreted as
        a typed literal with the appropriate RDF type.
    */
    public boolean hasLiteral( Property p, double o );
    
    /**
        Answer true iff this resource has the value <code>o</code> for
        property <code>p</code>. <code>o</code> is interpreted as
        a typed literal with the appropriate RDF type.
     */
    public boolean hasLiteral( Property p, float o );

    /**
        Answer true iff this resource has the value <code>o</code> for
        property <code>p</code>. <code>o</code> is interpreted as
        a typed literal with the appropriate RDF type.
     */
    public boolean hasLiteral( Property p, Object o );

    /** Test if this resource has a given property with a given value.
     * @param p The property sought.
     * @param o The value of the property sought.
     * @return true if and only if this resource has property p with
     * value o.
     */
    public boolean hasProperty( Property p, String o );

    /** Test if this resource has a given property with a given value.
     * @param p The property sought.
     * @param o The value of the property sought.
     * @param l The language of the property sought.
     * @return true if and only if this resource has property p with
     * value o.
     */
    public boolean hasProperty( Property p, String o, String l );

    /** Test if this resource has a given property with a given value.
     * @param p The property sought.
     * @param o The value of the property sought.
     * @return true if and only if this resource has property p with
     * value o.
     */
    public boolean hasProperty( Property p, RDFNode o );

    /** Delete all the properties for this resource from the associated model.
     * @return This resource to permit cascading.
     */
    public Resource removeProperties();

    /**
     Delete all the statements with predicate <code>p</code> for this resource
     from its associated model.

     @param p the property to remove
     @return this resource, to permit cascading
     */
    public Resource removeAll( Property p );

    /** Begin a transaction in the associated model.
     * @return This resource to permit cascading.
     */
    public Resource begin();

    /** Abort the  transaction in the associated model.
     * @return This resource to permit cascading.
     */
    public Resource abort();

    /** Commit the transaction in the associated model.
     * @return This resource to permit cascading.
     */
    public Resource commit();

    /**
       Answer some resource R for which this.hasProperty( p, R ),
       or null if no such R exists.
    */
    public Resource getPropertyResourceValue( Property p );
}
