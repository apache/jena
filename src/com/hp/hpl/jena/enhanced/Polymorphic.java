/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Polymorphic.java,v 1.16 2009-01-26 10:28:22 chris-dollin Exp $
*/

package com.hp.hpl.jena.enhanced;


/**
 * Abstract base class for all polymorphic RDF objects, especially enhanced node and enhanced graph.
 * 
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a> (original code)<br>
 *         <a href="mailto:Chris.Dollin@hp.com">Chris Dollin</a> (original code)<br>
 *         <a href="mailto:Ian.Dickinson@hp.com">Ian Dickinson</a> (tidying up and comments)
 */
public abstract class Polymorphic<T> {
    
    /** Each new polymorphic object is in a ring of views */
    private Polymorphic<T> ring;
    
    /**
        initially we're in the singleton ring.
     */
    Polymorphic() 
        { this.ring = this; }
    
    /**
     * Answer the personality object bound to this polymorphic instance
     * @return The personality object
     */
    protected abstract Personality<T> getPersonality();

    /**
        return _true_ iff this polymorphic object supports the specified interface.
        Synonymous with "does the argument class have this as an instance".
        Actually it shouldn't be. Review.
    */
    public <X extends T> boolean supports( Class<X> t )
        {
        X supporter = findExistingView( t );
        return supporter != null || this.canSupport( t );
        }
        
    /** 
     * Answer a polymorphic object that presents <i>this</i> in a way which satisfies type
     * t.
     * @param t A type
     * @return A polymorphic instance, possibly but not necessarily this, that conforms to t.
     */
    protected final <X extends T> X asInternal( Class<X> t )
        {
        X other = findExistingView( t );
        return other == null ? this.convertTo( t ) : other;
        }
        
    /**
        find an existing view in the ring which is an instance of _t_ and
        return it; otherwise return null. If _this_ is an instance, the
        search takes care to find it first.
    */
    private <X extends T> X findExistingView( Class<X> t )
        {
        Polymorphic<T> r = this;
        for (;;)
            {
            if (t.isInstance( r ) && r.isValid()) return t.cast( r );
            r = r.ring;
            if (r == this) return null;
            }
        }
    
    /**
        Answer true iff this polymorphic object already has a valid view of
        type <code>t</code> in its ring (so .as()ing it doesn't need to
        construct a new object).
    */
    protected <X extends T> boolean alreadyHasView( Class<X> t )
        { return findExistingView( t ) != null; }
        
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
    protected abstract <X extends T> X convertTo( Class<X> t );
    
    /**
        subclasses must provide a method for testing if _this_ can be
        converted to an instance of _t_. 
    */
    protected abstract <X extends T> boolean canSupport( Class<X> t );
    
    /**
        subclasses must override equals. Actually they may not have
        to nowadays ... I have expunged the clever facet-identity test
        (and indeed facets).
    */
    @Override public abstract boolean equals( Object o );
    
    /**
        add another view for this object. <code>other</code> must be freshly 
        constructed. To be called by subclasses when they have constructed a 
        new view for this object. 
        
        <p>The method is synchronised because addView is an update operation
        that may happen in a read context (because of .as()). Synchronising
        it ensures that simultaneous updates don't end up leaving the rings
        in an inconsistent state. (It's not clear whether this would actually
        lead to any problems; it's hard to write tests to expose these issues.)
        
        This method is public ONLY so that it can be tested.
        TODO find a better way to make it testable.
    */
    public synchronized void addView( Polymorphic<T> other )
        {
        if (other.ring == other)
            {
            other.ring = this.ring;
            this.ring = other;
            }
        else
            throw new AlreadyLinkedViewException( other );
        }

}

/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
