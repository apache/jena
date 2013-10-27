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


/** An RDF Container.
 *
 * <p>This interface defines methods for accessing RDF container resources.
 * These methods operate on the RDF statements contained in a model.  The 
 * container implementation may cache state from the underlying model, so
 * objects should not be added to or removed from the container by directly
 * manipulating its properties, whilst the container is being
 * accessed through this interface.</p>
 *
 * <p>When a member is deleted from a container using this interface, or an
 * iterator returned through this interface, all the other members with
 * higher ordinals are renumbered using an algorithm which may depend on the
 * type of the container.</p>
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
 */
public interface Container extends Resource {
    
	/**
	 * Verify that the container is an Alt.
	 * 
	 * This method will return true if the object is in a model and that model 
	 * contains the triple &lt; this, RDF.type, RDF.Alt &gt; otherwise returns
	 * false.
	 */
    public boolean isAlt();
    
    /**
	 * Verify that the container is a Seq.
	 * 
	 * This method will return true if the object is in a model and that model 
	 * contains the triple &lt; this, RDF.type, RDF.Seq &gt; otherwise returns
	 * false.
	 */
    public boolean isSeq();
    
    /**
	 * Verify that the container is a Bag.
	 * 
	 * This method will return true if the object is in a model and that model 
	 * contains the triple &lt; this, RDF.type, RDF.Bag &gt; otherwise returns
	 * false.
	 */
    public boolean isBag();
    /** Add a new value to a container.
     *
     * <p>The size of the container is extended by 1 and the new value is added as
     *   the last element of the container.</p>
     * @param o The value to be added.
     
     * @return this object so that calls may be cascaded.
     */
    public Container add(RDFNode o);
    /** Add a new value to a container.
     *
     * <p>The size of the container is extended by 1 and the new value is added as
     *   the last element of the container.</p>
     * @param o The value to be added.
     
     * @return this object so that calls may be cascaded.
     */
    public Container add(boolean o);
    /** Add a new value to a container.
     *
     * <p>The size of the container is extended by 1 and the new value is added as
     *   the last element of the container.</p>
     * @param o The value to be added.
     
     * @return this object so that calls may be cascaded.
     */
    public Container add(long o);
    /** Add a new value to a container.
     *
     * <p>The size of the container is extended by 1 and the new value is added as
     *   the last element of the container.</p>
     * @param o The value to be added.
     
     * @return this object so that calls may be cascaded.
     */
    public Container add(char o);
    /** Add a new value to a container.
     *
     * <p>The size of the container is extended by 1 and the new value is added as
     *   the last element of the container.</p>
     * @param o The value to be added.
     
     * @return this object so that calls may be cascaded.
     */
    public Container add(float o);
    /** Add a new value to a container.
     *
     * <p>The size of the container is extended by 1 and the new value is added as
     *   the last element of the container.</p>
     * @param o The value to be added.
     
     * @return this object so that calls may be cascaded.
     */
    public Container add(double o);
    /** Add a new value to a container.
     *
     * <p>The size of the container is extended by 1 and the new value is added as
     *   the last element of the container.</p>
     * @param o The value to be added.
     
     * @return this object so that calls may be cascaded.
     */
    public Container add(String o);
    /** Add a new value to a container.
     *
     * <p>The size of the container is extended by 1 and the new value is added as
     *   the last element of the container.</p>
     * @param o The value to be added.
     * @param l The language of the string to be added
     
     * @return this object so that calls may be cascaded.
     */
    public Container add(String o, String l);
    /** Add a new value to a container.
     *
     * <p>The size of the container is extended by 1 and the new value is added as
     *   the last element of the container.</p>
     * @param o The value to be added.
     
     * @return this object so that calls may be cascaded.
     */
    public Container add(Object o);
    
    /** Determine whether the container contains a value
     * @param o the value being tested for
     
     * @return true if and only if the container contains o
     */
    public boolean contains(RDFNode o);
    /** Determine whether the container contains a value
     * @param o the value being tested for
     
     * @return true if and only if the container contains o
     */
    public boolean contains(boolean o);
    /** Determine whether the container contains a value
     * @param o the value being tested for
     
     * @return true if and only if the container contains o
     */
    public boolean contains(long o);
    /** Determine whether the container contains a value
     * @param o the value being tested for
     
     * @return true if and only if the container contains o
     */
    public boolean contains(char o);
    /** Determine whether the container contains a value
     * @param o the value being tested for
     
     * @return true if and only if the container contains o
     */
    public boolean contains(float o);
    /** Determine whether the container contains a value
     * @param o the value being tested for
     
     * @return true if and only if the container contains o
     */
    public boolean contains(double o);
    /** Determine whether the container contains a value
     * @param o the value being tested for
     
     * @return true if and only if the container contains o
     */
    public boolean contains(String o);
    /** Determine whether the container contains a value
     * @param o the value being tested for
     * @param l the language of the string tested
     
     * @return true if and only if the container contains o
     */
    public boolean contains(String o, String l);
    /** Determine whether the container contains a value
     * @param o the value being tested for
     
     * @return true if and only if the container contains o
     */
    public boolean contains(Object o);
    
    /** Remove a value from the container.
     *
     * <p>The predicate of the statement <CODE>s</CODE> identifies the
     * ordinal of the value to be removed.  Once removed, the values in the
     * container with a higher ordinal value are renumbered.  The renumbering
     * algorithm depends on the type of container.<p>
     * @param s The statement to be removed from the model.
     
     * @return this container to enable cascading calls.
     */
    public Container remove(Statement s);
    
    /** Return an iterator over the values.
     *
     * <p><B>Note</B> the interator returned is not a standard java.util.iterator.
     * It has a <CODE>close</CODE> method which SHOULD be called if the
     * application has not completed the iteration, but no longer requires
     * the iterator.  This will enable the freeing of resources in, for
     * example, implementations which store their models in a database.</p>
     .
     * @return Return an iterator over the values.
     */
    public NodeIterator iterator();
    /** return the number values in the container.
     
     * @return the number of values int the container.
     */
    public int size();
}
