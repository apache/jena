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
 * Alt.java
 *
 * Created on 26 July 2000, 15:24
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
 * @author bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.2 $' Date='$Date: 2003-02-01 14:35:32 $'
 */

import com.hp.hpl.jena.enhanced.*;

public interface Alt extends Container {
        
        public static final Type type = new AltType();
        
    /** Set the default value of this container.
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(RDFNode o) throws RDFException;
    /** Set the default value of this container.
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(boolean o) throws RDFException;
    /** Set the default value of this container.
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(long o) throws RDFException;
    /** Set the default value of this container.
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(char o) throws RDFException;
    /** Set the default value of this container.
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(float o) throws RDFException;
    /** Set the default value of this container.
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(double o) throws RDFException;
    /** Set the default value of this container.
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(String o) throws RDFException; 
    /** Set the default value of this container.
     * @param o The value to be set.
     * @throws RDFException Generic RDF exception
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(String o, String l) throws RDFException;
    /** Set the default value of this container.
     * @param o The value to be set.
     * @param l The language of the value to be set
     * @throws RDFException Generic RDF exception
     * @return This object to permit cascading calls.
     */
    public Alt setDefault(Object o) throws RDFException;

    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource.
     */
    public RDFNode getDefault() throws RDFException;
    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public Resource getDefaultResource() throws RDFException;
    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public Literal getDefaultLiteral() throws RDFException;
    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public boolean getDefaultBoolean() throws RDFException;
    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public byte getDefaultByte() throws RDFException;
    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public short getDefaultShort() throws RDFException;
    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public int getDefaultInt() throws RDFException;
    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public long getDefaultLong() throws RDFException;
    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public char getDefaultChar() throws RDFException;
    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public float getDefaultFloat() throws RDFException;
    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public double getDefaultDouble() throws RDFException;
    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public String getDefaultString() throws RDFException;
    /** Return the language of the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the language of the default value for this resource
     */
    public String getDefaultLanguage() throws RDFException;
    /** Return the default value for this resource.
     *
     * <p>The factory class f is used to create the object which is returned.
     * </p>
     * @return the default value for this resource interpreted as the return
     * type.
     * @param f A factory class which will be used to create the
     * object returned.
     * @throws RDFException Generic RDF Exception
     */
    public Resource getDefaultResource(ResourceF f) throws RDFException;
    /** Return the default value for this resource.
     *
     * <p>The object returned is created by calling the
     * <CODE>createObject</CODE> method of the factory object <CODE>f</CODE>.
     * </p>
     * @return the default value for this resource as an object created
     * by the factory object f.
     * @param f A factory object used to create the object returned.
     * @throws RDFException Generic RDF Exception
     */
    public Object getDefaultObject(ObjectF f) throws RDFException;
    /** Return the default value for this resource. 
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *         type.
     */
    public Alt getDefaultAlt() throws RDFException;
    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public Bag getDefaultBag() throws RDFException;
    /** Return the default value for this resource.
     * @throws RDFException Generic RDF Exception
     * @return the default value for this resource interpreted as the return
     *  type.
     */
    public Seq getDefaultSeq() throws RDFException;
        
    /** Remove a value from the container.
     *
     * <p>The predicate of the statement <CODE>s</CODE> identifies the
     * ordinal of the value to be removed.  Once removed, the values in the
     * container with a higher ordinal value are renumbered.  The renumbering
     * algorithm is implementation dependent.<p>
     * @param s The statement to be removed from the model.
     * @throws RDFException Generic RDF Exception
     * @return this container to enable cascading calls.
     */
    public Container remove(Statement s) throws RDFException;
}

class AltType implements Type
	
        {
        public boolean accepts( Polymorphic p ) { return p instanceof Alt;}
      //  public Polymorphic coerce( Polymorphic p ) { return new AltImpl( (Resource) p ); }
        public boolean supportedBy( Polymorphic p ) { return p instanceof Alt; }
        public String toString() { return "Alt.type"; }
        }
