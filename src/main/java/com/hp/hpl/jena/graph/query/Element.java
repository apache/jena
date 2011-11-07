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

package com.hp.hpl.jena.graph.query;

import com.hp.hpl.jena.graph.*;

/**
    An Element of a matching triple. Elements have associated indexes, their place
    in the Domain storing the matching values. Subclasses represent constants,
    binding occurances of variables, and bound instances of variables.
    
	@author hedgehog
*/

public abstract class Element 
	{
	protected final int index;
	
    /**
        Answer this Element's index in the Domains it is compiled for.
    */
	public int getIndex()
		{ return index; }
		
    /**
        Initialise this Element with its allocated index.
    */
	protected Element( int index )
		{ this.index = index; }
		
    /**
        Initialiser invoked by sub-classes which need no index.
    */
	protected Element() 
		{ this( -1 ); }
		
    /**
        The constant ANY matches anything and binds nothing
    */
	public static final Element ANY = new Element()
        {
        @Override
        public boolean match( Domain d, Node n ) { return true; }
        @Override
        public Node asNodeMatch( Domain d ) { return Node.ANY; }
        @Override
        public String toString() { return "<any>"; }
        };
        
    /**
        Answer true if this Element matches x given the bindings in d. May side-effect d
        by (re)binding if this element is a variable.
        @param d the variable bindings to read/update for variables
        @param x the value to match
        @return true if the match succeeded
    */
    public abstract boolean match( Domain d, Node x );
        
    /**
        Answer a Node suitable as a pattern-match element in a TripleMatch approximating
        this Element. Thus Bind elements map to null (or Node.ANY).
        @param d the domain holding the variable bindings
        @return the matched value (null if none, ie binding occurance or ANY)
    */
    public abstract Node asNodeMatch( Domain d );
        
    @Override
    public String toString()
    	{ return "<" + this.getClass() + " element>"; }
	}
