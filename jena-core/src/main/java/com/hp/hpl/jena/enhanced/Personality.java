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

import java.util.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.CollectionFactory;

/**
 * Defines a set of permitted mappings from [interface] Class objects to 
 * {@link Implementation} factories that can generate instances of the facet represented 
 * by the Class.
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
        { return new Personality<>( this ); }
    
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
