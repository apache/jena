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
    An Element that matches a single specified value.
    
	@author hedgehog
*/
public class Fixed extends Element 
	{
	private Node value;
	
    /**
        Initialise this element with its single matching value: remember that value.
    */
	public Fixed( Node x ) 
        { this.value = x; }
        
    /**
        Answer true iff we are matched against a node with the same value as ours.
        @param d the domain with bound values (ignored)
        @param x the node we are to match
        @return true iff our value is the same as his
    */
        
    @Override
    public boolean match( Domain d, Node x ) 
        { return x.sameValueAs(value); }
    
    /**
        Answer the Node we represent given the variable-bindings Domain.
        @param d the variable bindings to use (ignored)
        @return our fixed value
    */
    @Override
    public Node asNodeMatch( Domain d ) 
        { return value; }
        
    @Override
    public String toString() 
        { return "<fixed " + value + ">"; }            
	}
