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

/**
 * A slow "index" that looks for data by searching linearly through a set.
 * Only used when the indexed data contains fewer bound variables than expected.
 * Note that this class is only used for a MINUS operation that is removing data
 * with potentially unbound values, and is therefore rarely used.
 * 
 * TODO: If this index starts to be used more often then consider various options for
 *       indexing on the known bound variables.
 *       One possibility is for each variable (found in commonVars) to take
 *       the value of a var/value pair and TreeMap this to a set of Bindings that it occurs in.
 *       This would offer a reduced set to search, and set intersections may also work
 *       (intersections like this could be done on Binding reference equality rather than value).
 *       TreeMap is suggested here, since there would be commonVars.size() maps, which would take
 *       a lot of heap, particularly since performance of this class is only an issue when the
 *       data to search is significant.
 * <p>Contribution from Paul Gearon
 */

public class LinearIndex implements IndexTable {

	final Set<Var> commonVars ;
	List<Binding> table = new ArrayList<>() ;

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

	static Binding toBinding(HashIndexTable.Key key, Map<Var,Integer> mappings)
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

