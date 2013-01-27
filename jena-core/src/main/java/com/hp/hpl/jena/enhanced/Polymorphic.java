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

package com.hp.hpl.jena.enhanced;


/**
 * Abstract base class for all polymorphic RDF objects, especially enhanced node and enhanced graph.
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
