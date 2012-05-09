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

/**
 	A SlotValuator evaluates to a specific slot in the index values
 	bindings.
 	@author hedgehog
*/

public class SlotValuator implements Valuator
	{
    /**
     	The index in the index values which this SlotValuator
     	looks up.
    */
    private int index;
	
    /**
     	Initialise this SlotValuator with the index to use for lookup.
    */
	public SlotValuator( int index )
	    { this.index = index; }

	/**
	 	Answer the value of the <code>index</code>th element of the
	 	index values bindings.
	*/
	@Override
    public Object evalObject( IndexValues iv )
	    { return iv.get( index ); }
	
	/**
	 	Answer the primitive boolean value of the <code>index</code>th
	 	element of the index value bindings, which must be a 
	 	<code>Boolean</code> value.
	*/
	@Override
    public boolean evalBool( IndexValues iv )
	    { return ((Boolean) evalObject( iv )).booleanValue(); }        
	}
