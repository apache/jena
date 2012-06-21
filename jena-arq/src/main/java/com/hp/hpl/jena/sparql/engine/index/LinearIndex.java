/**
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

package com.hp.hpl.jena.sparql.engine.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;

public class LinearIndex implements IndexTable {

	final Set<Var> commonVars ;
	List<Binding> table = new ArrayList<Binding>() ;

	public LinearIndex(Set<Var> commonVars, QueryIterator data)
	{
		this.commonVars = commonVars ;
		while ( data.hasNext() )
			table.add(data.next()) ;
		data.close() ;
	}

	public LinearIndex(Set<Var> commonVars, QueryIterator data, Set<HashIndexTable.Key> loadedData, Map<Var,Integer> mappings)
	{
		this.commonVars = commonVars ;
		for ( HashIndexTable.Key key: loadedData )
			table.add(toBinding(key, mappings)) ;

		while ( data.hasNext() )
			table.add(data.next()) ;
		data.close() ;
	}

	@Override
	public boolean containsCompatibleWithSharedDomain(Binding bindingLeft)
	{
		if ( commonVars.size() == 0 )
			return false ;

		for ( Binding bindingRight: table )
    	{
			if ( hasCommonVars(bindingLeft, bindingRight)
					&& Algebra.compatible(bindingLeft, bindingRight) )
    			return true ;
    	}
    	return false ;
	}

	private boolean hasCommonVars(Binding left, Binding right)
	{
		for ( Var v: commonVars )
		{
			if ( left.contains(v) && right.contains(v) )
				return true ;
		}
		return false;
	}

	private static Binding toBinding(HashIndexTable.Key key, Map<Var,Integer> mappings)
	{
		Node[] values = key.getNodes() ;
		BindingHashMap b = new BindingHashMap() ;
		for (Map.Entry<Var,Integer> mapping: mappings.entrySet())
		{
			Node value = values[mapping.getValue()] ;
			if ( value != null )
				b.add(mapping.getKey(), value) ;
		}
		return b ;
	}
}

