/*
 *  (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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

import com.hp.hpl.jena.graph.FrontsNode;

/** 
    Interface covering RDF resources and literals. Allows probing whether a
    node is a literal/[blank, URI]resource, moving nodes from model to model,
    and viewing them as different Java types using the .as() polymorphism.

    @author bwm, kers
*/
public interface RDFNode extends FrontsNode
    {
    /** 
        Answer a String representation of the node.  The form of the string 
        depends on the type of the node and is intended for human consumption,
        not machine analysis.
    */
    @Override
    public String toString();
    
    /** 
        Answer true iff this RDFNode is an anonynous resource. Useful for
        one-off tests: see also visitWith() for making literal/anon/URI choices.
    */
    public boolean isAnon();
    
    /** 
        Answer true iff this RDFNode is a literal resource. Useful for
        one-off tests: see also visitWith() for making literal/anon/URI choices.
    */
    public boolean isLiteral();
    
    /** 
        Answer true iff this RDFNode is an named resource. Useful for
        one-off tests: see also visitWith() for making literal/anon/URI choices.
    */
    public boolean isURIResource();
    
    /**
        Answer true iff this RDFNode is a URI resource or an anonynous
        resource (ie is not a literal). Useful for one-off tests: see also 
        visitWith() for making literal/anon/URI choices.
    */
    public boolean isResource();
    
    /**
        RDFNodes can be converted to different implementation types. Convert
        this RDFNode to a type supporting the <code>view</code>interface. The 
        resulting RDFNode should be an instance of <code>view</code> and should 
        have any internal invariants as specified.
    <p>
        If the RDFNode has no Model attached, it can only be .as()ed to
        a type it (this particular RDFNOde) already has.
    <p>
        If the RDFNode cannot be converted, an UnsupportedPolymorphism
        exception is thrown..
    */
    public <T extends RDFNode> T as( Class<T> view );
    
    /**
        Answer true iff this RDFNode can be viewed as an instance of
        <code>view</code>: that is, if it has already been viewed in this
        way, or if it has an attached model in which it has properties that
        permit it to be viewed in this way. If <code>canAs</code> returns
        <code>true</code>, <code>as</code> on the same view should
        deliver an instance of that class. 
    */
    public <T extends RDFNode> boolean canAs( Class<T> view );

    /** 
        Return the model associated with this resource. If the Resource
        was not created by a Model, the result may be null.

        @return The model associated with this resource.
    */
    public Model getModel();
    
    /**
        Answer a .equals() version of this node, except that it's in the model 
        <code>m</code>.
        
        @param m a model to move the node to
        @return this, if it's already in m (or no model), a copy in m otherwise
    */
    public RDFNode inModel( Model m );
    
    /**
        Apply the appropriate method of the visitor to this node's content and
        return the result.
        
        @param rv an RDFVisitor with a method for URI/blank/literal nodes
        @return the result returned by the selected method
    */
    public Object visitWith( RDFVisitor rv );

    /**
       If this node is a Resource, answer that resource; otherwise throw an
       exception. 
    */
    public Resource asResource();
    
    /**
        If this node is a Literal, answer that literal; otherwise throw an
        exception. 
     */
    public Literal asLiteral();
    }
