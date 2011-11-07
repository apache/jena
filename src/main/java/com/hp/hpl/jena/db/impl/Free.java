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

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.Element;
import com.hp.hpl.jena.shared.JenaException;

/**
    A binding instance of a variable. It accepts any node and records it in the supplied
    Domain at the index allocated to it when it is created.
    
	@author hedgehog
*/
public class Free extends Element 
	{
		
    /**
        Track a (free) variable element: it will later be bound during compilation.
    */
    
	private Node_Variable var;
	private int listIx;            // index of variable in varList.
	private int mapIx;            // index of (arg) variable in Mapping	
	private boolean isListed;  // true when variable is in varList
	private boolean isArgument;  // true when variable can be bound by an argument
	
	public Free( Node n ) {
		super( );
		var = (Node_Variable) n;
		isArgument = false;
		isListed = false;
	}
	
	public int getListing()
	{
		if ( isListed ) return listIx;
		else throw new JenaException("UnListed variable");
	}

	public void setListing ( int ix )
		{
		if ( isListed )
			throw new JenaException("Relisting of variable");
		isListed = true;
		listIx = ix;
		}
		
	public void setIsArg ( int ix ) { isArgument = true; mapIx = ix; }
	public boolean isArg() { return isArgument; }
	
	public int getMapping()
	{
		if ( isArgument ) return mapIx;
		else throw new JenaException("Unmapped variable");
	}

	public boolean isListed() { return isListed; }
	
	public Node_Variable var() {
		return var;
	}

	@Override
    public boolean match( Domain d, Node x )
		{throw new JenaException("Attempt to match a free variable");		
		}
	
	@Override
    public Node asNodeMatch( Domain d ) {
		throw new JenaException("asNodeMatch not supported");
	}
	
	@Override
    public String toString()
		{ return "<Free " + listIx + ">"; }
	}
