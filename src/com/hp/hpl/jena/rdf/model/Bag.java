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
 * Bag.java
 *
 * Created on 26 July 2000, 15:23
 */

package com.hp.hpl.jena.rdf.model;

/** An RDF Bag container.
 *
 * <p>This interface defines methods for accessing RDF Bag resources.
 * These methods operate on the RDF statements contained in a model.  The 
 * Bag implementation may cache state from the underlying model, so
 * objects should not be added to or removed a the Bag by directly
 * manipulating its properties, whilst the Bag is being
 * accessed through this interface.</p 
 *
 * <p>When a member is deleted from a Bag using this interface, or an
 * iterator returned through this interface, all the other members with
 * higher ordinals are renumbered using an implementation dependendent
 * algorithm.</p>
 *
 * @author bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.1.1.1 $' Date='$Date: 2002-12-19 19:17:34 $'
 */

import com.hp.hpl.jena.enhanced.*;

public interface Bag extends Container {
    
    public static final Type type = new Type()
        {
        public boolean accepts( Polymorphic p ) { return p instanceof Bag;}
      //  public Polymorphic coerce( Polymorphic p ) { return new BagImpl( (Resource) p ); }
        public boolean supportedBy( Polymorphic p ) { return p instanceof Bag; }
        public String toString() { return "Bag.type"; }
        };
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
    
    /** Remove a value from the container.
     *
     * <p>Any statement with an ordinal predicate and object <CODE>v</CODE>
     * may be selected and removed.  Once removed, the values in the
     * container with a higher ordinal value are renumbered.  The renumbering
     * algorithm is implementation dependent.<p>
     * @param v The value to be removed from the bag.
     * @throws RDFException Generic RDF Exception
     * @return this container to enable cascading calls.
     */
//TODO    public Container remove(String v) throws RDFException;
}
