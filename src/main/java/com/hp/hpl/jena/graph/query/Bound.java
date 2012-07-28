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
    An element which represents an already-bound variable.
*/

public class Bound extends Element
	{
    /**
        Initialise a Bound element: remember <code>n</code> as it is the index into the
        Domain at which its value is stored.
    */
	public Bound( int n ) { super( n ); }
	
    /**
        Answer true iff the node <code>x</code> matches the previously-seen value at
        Donain[index]. The matching uses datatype-value semantics, implemented by
        <code>Node::sameValueAs()</code>.
    */  
    @Override
    public boolean match( Domain d, Node x )
        { return x.sameValueAs( d.getElement( index ) ); }
     
    @Override
    public Node asNodeMatch( Domain d ) 
        { return d.getElement( index ); }
        
    @Override
    public String toString()
    	{ return "<Bound " + index + ">"; }
	}
