/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Polymorphic.java,v 1.1.1.1 2002-12-19 19:13:12 bwm Exp $
*/

package com.hp.hpl.jena.enhanced;

import java.util.*;

import com.hp.hpl.jena.util.*;

/**
 * Abstract base class for all polymorphic RDF objects, especially enhanced node and enhanced graph.
 * 
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a> (original code)<br>
 *         <a href="mailto:Chris.Dollin@hp.com">Chris Dollin</a> (original code)<br>
 *         <a href="mailto:Ian.Dickinson@hp.com">Ian Dickinson</a> (tidying up and comments)
 */
public abstract class Polymorphic {
    // instance variables
    
    // At any one time, only one of these two variables is needed.
    // On creation the myTypes field is set to the implementedTypes()
    // value of the Implementation.
    // When it is necessary to convert this object to another one,
    // the shared state is held in the facet Type => Polymorhic map.
    
    /** Maps the different polymorphic objects that present different views of this object, indexed by type */
    private Map facets;
    
    /** The set of types that this polymorphic object is an implementation for */
    private Type[] myTypes;
    
    // Constructors
    
    /**
     * Construct a polymorphic RDF object that accepts the given set of types.
     * @param myTypes The set of types that this class provides an implementation for
     */
    Polymorphic(Type myTypes[]) {
        this.myTypes = myTypes;
    }


    // External contract methods
    
    /**
     * Answer the personality object bound to this polymorphic instance
     * 
     * @return The personality object
     */
    protected abstract Personality getPersonality();
    
    
    /** 
     * Ansewr true iff <i>this</i> is already acceptable to the given type t. 
     * Delegates to t to test if this class is an acceptable implementation.
     * @param t A type to test
     * @return True iff this object is already an acceptable implementation of t
     */
    protected boolean already(Type t) {
        return t.accepts(this);
    }

    /** 
     * Answer a polyorphic object that presents <i>this</i> in a way which satisfies type
     * t.
     * @param t A type
     * @return A polymorphic instance, possibly but not necessarily this, that conforms to t.
     */
    protected abstract Polymorphic asInternal(Type t);
    
    
    /**
     * Set the set of types that this polymorphic provides an realisation for.
     * @todo Should copy the array, not reference it -ijd
     * @todo Is it legal for clients to update this array once the object has been created? -ijd
     * 
     * @param m A set of type objects
     */
    protected void setTypes(Type m[]) {
        myTypes = m;
    }


    /**
     * Equality over polymorphic objects is defined as identity <b>or</b> sharing
     * the same <i>facets</i> map.
     * @param o An object to test for equality with this
     * @return True if o is == this, or o and this have the identical facets map
     */
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o != null
            && o instanceof Polymorphic
            && facets != null
            && ((Polymorphic) o).facets == facets)
            return true;
        return false;
    }
    
    // Internal implementation methods
    
    /** setFacets is a phase2 initializer invoked when we know that
     *  this Polymorphic object has more than one face.
     *  This may happen arbitrarily later than the initial creation,
     *  or may not happen at all, but it happens at most once.
     */
    void setFacets(Map hm) {
        if (facets != null)
            Log.severe( "Internal error: invariant violation.", getClass().getName(), "setFacets" );
            
        facets = hm;
        for (int i = 0; i < myTypes.length; i++) {
            if (!myTypes[i].accepts(this))
                Log.severe( "Internal error: personality misconfigured.", getClass().getName(), "setFacets");
            else 
                facets.put(myTypes[i], this);
        }
    }
    
    
    Polymorphic getFacet( Type t ) {
        if (facets == null) {
            setFacets( new HashMap() );
        }
        
        return (Polymorphic) facets.get( t );
    }
    
    Map getFacets() {
        return facets;
    }

}

/*
    (c) Copyright Hewlett-Packard Company 2002
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
