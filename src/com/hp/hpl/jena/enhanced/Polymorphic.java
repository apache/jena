/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Polymorphic.java,v 1.4 2003-04-02 13:26:35 jeremy_carroll Exp $
*/

package com.hp.hpl.jena.enhanced;


/**
 * Abstract base class for all polymorphic RDF objects, especially enhanced node and enhanced graph.
 * 
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a> (original code)<br>
 *         <a href="mailto:Chris.Dollin@hp.com">Chris Dollin</a> (original code)<br>
 *         <a href="mailto:Ian.Dickinson@hp.com">Ian Dickinson</a> (tidying up and comments)
 */
public abstract class Polymorphic {
    
    /** Each new polymorphic object is in a ring of views */
    private Polymorphic ring;
    
    /**
        initially we're in the singleton ring.
     */
    Polymorphic() 
        { this.ring = this; }

    // External contract methods
    
    /**
     * Answer the personality object bound to this polymorphic instance
     * @return The personality object
     */
    protected abstract Personality getPersonality();
        
    /** 
     * Answer true iff <i>this</i> is already acceptable to the given type t. 
     * Delegates to t to test if this class is an acceptable implementation.
     * @param t A type to test
     * @return True iff this object is already an acceptable implementation of t
     */
    protected boolean already(Class t) 
        { return t.isInstance( this ); }

    /**
        return _true_ iff this polymorphic object supports the specified interface.
        Synonymous with "does the argument class have this as an instance".
        Actually it shouldn't be. Review.
    */
    public boolean supports( Class t )
        {
        Polymorphic supporter = findExistingView( t );
        return supporter != null || this.canSupport( t );
        }
        
    /** 
     * Answer a polymorphic object that presents <i>this</i> in a way which satisfies type
     * t.
     * @param t A type
     * @return A polymorphic instance, possibly but not necessarily this, that conforms to t.
     */
    protected final Polymorphic asInternal( Class t )
        {
        Polymorphic other = findExistingView( t );
        return other == null ? this.convertTo( t ) : other;
        }
        
    /**
        find an existing view in the ring which is an instance of _t_ and
        return it; otherwise return null. If _this_ is an instance, the
        search takes care to find it first.
    */
    private Polymorphic findExistingView( Class t )
        {
        Polymorphic r = this;
        for (;;)
            {
            if (t.isInstance( r ) && r.isValid()) return r;
            r = r.ring;
            if (r == this) return null;
            }
        }
        
    /**
        answer true iff this enhanced node is still underpinned in the graph
        by triples appropriate to its type.
    */
    public abstract boolean isValid();
        
    /**
        subclasses must provide a method for converting _this_, if
        possible, into an instance of _t_. It will only be called if _this_
        doesn't already have (or be) a suitable ring-sibling.
    */    
    protected abstract Polymorphic convertTo( Class t );
    
    /**
        subclasses must provide a method for testing if _this_ can be
        converted to an instance of _t_. 
    */
    protected abstract boolean canSupport( Class t );
    
    /**
        subclasses must override equals. Actually they may not have
        to nowadays ... I have expunged the clever facet-identity test
        (and indeed facets).
    */
    public abstract boolean equals( Object o );
    
    /**
        add another view for this object. _other_ must be freshly constructed.
        To be called by subclasses when they have constructed a new view
        for this object.
    */
    void addView( Polymorphic other )
        {
        if (other.ring == other)
            {
            other.ring = this.ring;
            this.ring = other;
            }
        else
            throw new RuntimeException( "oops: stale 'other' view" );
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
