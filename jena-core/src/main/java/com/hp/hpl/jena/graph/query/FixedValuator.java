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
 	A FixedValuator is a Valuator that delivers a constant value
 	(supplied when it is constructed).
 	
 	@author hedgehog
 */
public class FixedValuator implements Valuator
	{
	private Object value;
	
	/**
	 	Initialise this FixedValuator with a specific value
	*/
	public FixedValuator( Object value )
	    { this.value = value; }
	
	/**
	 	Answer this FixedValuator's value, which must be a Boolean
	 	object, as a <code>boolean</code>. The index values
	 	are irrelevant.
	*/
	@Override
    public boolean evalBool( IndexValues iv )
	    { return ((Boolean) evalObject( iv )).booleanValue(); }
	        
	/**
	 	Answer this FixedValuator's value, as supplied when it was constructed.
	*/
	@Override
    public Object evalObject( IndexValues iv )
	    { return value; }
	}
