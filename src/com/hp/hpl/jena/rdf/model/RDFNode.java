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
 * RDFNode.java
 *
 * Created on 25 July 2000, 13:13
 */

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.graph.Node;
/** An RDF Resource or an RDF Literal.
 *
 * <p><CODE>RDFNode</CODE> represents the methods which RDF Resources and RDF
 * Literals have in common.</p>
 * <p>Chris added the _as_ method to allow RDFNodes to participate in polymorphic
 * conversions.
 * @author bwm
 * @version Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.4 $' Date='$Date: 2003-04-14 10:56:11 $'
 */
public interface RDFNode {
    /** Return a String representation of the node.  The form of the string depends
     * on the type of the node.
     * @return a String representation of this object.
     */
    public String toString();
    
    /**
        a presentation-layer RDFNode is build on top of an SPI-layer Node; get it.
    */
    public Node asNode();
    
    /**
        RDFNodes can be converted to different implementation types. Convert
        this RDFNode to a type supporting the _view_ interface. The resulting
        RDFNode should be an instance of _view_ and should have any
        internal invariants specified.
    <p>
        It is not clear what should happen if this RDFNode cannot be viewed as
        a _view_. Should it deliver null or should it throw an exception? Or
        deliver a half-baked instance?
    */
    public RDFNode as( Class view );
    
    /**
        return true iff this RDFNode can be viewed as a _view_.
    */
    public boolean canAs( Class view );
    
    /**
        returns a .equals() version of this node, except that its in the model m.
        
        @param m a model to move the node to
        @return this, if it's already in m (or no model), a copy in m otherwise
    */
    public RDFNode inModel( Model m );
}
