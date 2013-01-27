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

/** <p>An RDF Alternative container.</p>
 *
 * <p>This interface defines methods for accessing RDF Alternative resources.
 * These methods operate on the RDF statements contained in a model.  The 
 * Alternative implementation may cache state from the underlying model, so
 * objects should not be added to or removed from the Alternative by directly
 * manipulating its properties, whilst the Alternative is being
 * accessed through this interface.</p>
 *
 * <p>When a member is deleted from an Alternative using this interface, or an
 * iterator returned through this interface, all the other members with
 * higher ordinals are renumbered using an implementation dependendent
 * algorithm.</p>
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

public interface Alt extends Container {
        
    /** Set the default value of this container.
     * @param o The value to be set.
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(RDFNode o);
    
    /** Set the default value of this container.
     * @param o The value to be set.
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(boolean o);
    
    /** Set the default value of this container.
     * @param o The value to be set.
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(long o);
    
    /** Set the default value of this container.
     * @param o The value to be set.
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(char o);
    
    /** Set the default value of this container.
     * @param o The value to be set.
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(float o);
    
    /** Set the default value of this container.
     * @param o The value to be set.
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(double o);
    
    /** Set the default value of this container.
     * @param o The value to be set.
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(String o); 
    
    /** Set the default value of this container.
     * @param o The value to be set.
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(String o, String l);
    
    /** Set the default value of this container.
     * @param o The value to be set.
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(Object o);

    /** Return the default value for this resource.
     * @return the default value for this resource.
     */
    public RDFNode getDefault();
    
    /** Return the default value for this resource.
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public Resource getDefaultResource();
    
    /** Return the default value for this resource.
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public Literal getDefaultLiteral();
    
    /** Return the default value for this resource.
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public boolean getDefaultBoolean();
    
    /** Return the default value for this resource.
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public byte getDefaultByte();
    
    /** Return the default value for this resource.
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public short getDefaultShort();
    
    /** Return the default value for this resource.
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public int getDefaultInt();
    
    /** Return the default value for this resource.
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public long getDefaultLong();
    
    /** Return the default value for this resource.
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public char getDefaultChar();
    
    /** Return the default value for this resource.
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public float getDefaultFloat();
    
    /** Return the default value for this resource.
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public double getDefaultDouble();
    
    /** Return the default value for this resource.
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public String getDefaultString();
    
    /** Return the language of the default value for this resource.
     * @return the language of the default value for this resource
     */
    public String getDefaultLanguage();
    
    /** Return the default value for this resource.
     *
     * <p>The factory class f is used to create the object which is returned.
     * </p>
     * @return the default value for this resource interpreted as the return
     * type.
     * @param f A factory class which will be used to create the
     * object returned.
     
     */
    @Deprecated public Resource getDefaultResource(ResourceF f);
    
    /** Return the default value for this resource. 
     * @return the default value for this resource interpreted as the return
     *         type.
     */
    public Alt getDefaultAlt();
    
    /** Return the default value for this resource.
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public Bag getDefaultBag();
    
    /** Return the default value for this resource.
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public Seq getDefaultSeq();
        
    /** Remove a value from the container.
     * <p>The predicate of the statement <CODE>s</CODE> identifies the
     * ordinal of the value to be removed.  Once removed, the values in the
     * container with a higher ordinal value are renumbered.  The renumbering
     * algorithm is implementation dependent.<p>
     * @param s The statement to be removed from the model.
     * @return this container to enable cascading calls.
     */
    @Override
    public Container remove(Statement s);
}
