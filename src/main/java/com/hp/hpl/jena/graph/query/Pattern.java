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
    A Pattern represents a matching triple; it is composed of S, P, and O Elements.
    
	@author hedgehog
*/

public class Pattern 
	{
	public final Element S;
	public final Element P;
	public final Element O;
	
	public Pattern( Element S, Element P, Element O )
		{
		this.S = S; 
		this.P = P; 
		this.O = O;
		}
	
    /**
        Convert a Pattern into a TripleMatch by making a Triple who's Nodes are the
        conversions of the constituent elements.
    */	
    public TripleMatch asTripleMatch( Domain d )
        { 
        return Triple.createMatch
            ( S.asNodeMatch( d ), P.asNodeMatch( d ), O.asNodeMatch( d ) ); 
        }
    
    /**
        Answer true iff this pattern, given the values for variables as found in a given 
        Domain, matches the given triple; update the Domain with any variable bindings.
        
        @param d the Domain with the current bound variable values (and slots for the rest)
        @param t the concrete triple to match
        @return true iff this pattern matches the triple [and side-effects the domain]
    */
    public boolean match( Domain d, Triple t )
        {
        return S.match( d, t.getSubject() ) 
            && P.match( d, t.getPredicate() ) 
            && O.match( d, t.getObject() );
        }

     @Override
    public String toString()
        { return "<pattern " + S + " @" + P + " " + O + ">"; }
	}
