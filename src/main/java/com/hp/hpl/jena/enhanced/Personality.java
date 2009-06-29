/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Personality.java,v 1.1 2009-06-29 08:55:56 castagna Exp $
*/

package com.hp.hpl.jena.enhanced;

import java.util.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.CollectionFactory;

/**
 * Defines a set of permitted mappings from [interface] Class objects to 
 * {@link Implementation} factories that can generate instances of the facet represented 
 * by the Class.
 * 
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a> (original code)<br>
 *         <a href="mailto:Chris.Dollin@hp.com">Chris Dollin</a> (original code)
*/
public class Personality<T> {

    // Instance variables
    /** Records the bindings from type specifications to implementations.  */
    private Map<Class<? extends T>, Implementation> types = CollectionFactory.createHashedMap();
    
    // Constructors
    
    /** base constructor, does nothing [except implicitly create _types_] */
    public Personality()
        {}
        
    /** initialise this personality with the bindings from _other_ */
    public Personality( Personality<T> other )
        {
        this();
        this.add( other );
        }

    // External contract methods
            
    /** Add a new interface and its implementation to this Personality.
        @param interf The interface to add, expressed as a Type object.
        @param impl A way of implementing _interf_.
     */
    public <X extends T> Personality<T> add( Class<X> interf, Implementation impl )
        { 
        types.put( interf, impl ); 
        return this;
        }

    /**
        create a new Personality copying this one; the _types_ state is
        copied, not shared.
    */
    public Personality<T> copy() 
        { return new Personality<T>( this ); }
    
    /** 
        get the implemementation for the specified type, returning null if there
        isn't one available. 
    */
    public <X extends T> Implementation getImplementation( Class<X> t )
        { return types.get( t ); }
    
    /**
    	extend this personality by adding in all the mappins from the argument _p_.
    	return _this_ (for call chaining).
    */
    public Personality<T> add( Personality<T> p ) {
        types.putAll( p.types );    
        return this;
    }
    
    /**
        make a new instance of a type _interf_ based on the node _n_ and the
        polymorphic _that_; use the implementation wrapper for _interf_ in
        _types_. 
    */
    public <X extends T> X newInstance( Class<X> interf, Node n, EnhGraph that ) 
        {
        Implementation impl = types.get( interf );
        if (impl == null) throw new PersonalityConfigException( interf + " not in Personality." );
        EnhNode result = impl.wrap(  n, that  );
        if (!interf.isInstance(result))
        	throw new PersonalityConfigException( interf + " misconfigured." );
        return interf.cast( result );
        }
    
    protected Map<Class<? extends T>, Implementation> getMap() {return types;}
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
