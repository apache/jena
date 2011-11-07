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
    A binding instance of a variable. It accepts any node and records it in the supplied
    Domain at the index allocated to it when it is created.
    
	@author hedgehog
*/
public class Bind extends Element 
	{	
    /**
        Initialise a Bind element: remember the index <code>n</code> which is the
        place in Domain's where it may store its value.
    */
	public Bind( int n ) { super( n ); }
	
    /**
        Answer true after updating the domain to record the value this element binds.
        @param d the domain in which to note this element is bound to <code>x</code>.
        @return true [after side-effecting d]
    */		
    @Override
    public boolean match( Domain d, Node x )
        {
        d.setElement( index, x );
        return true;    
        }
        
    /**
        Answer Node.ANY, as a binding occurance of a variable can match anything.
    */
    @Override
    public Node asNodeMatch( Domain d )
        { return Node.ANY; } 
        
	@Override
    public String toString()
		{ return "<Bind " + index + ">"; }
	}
