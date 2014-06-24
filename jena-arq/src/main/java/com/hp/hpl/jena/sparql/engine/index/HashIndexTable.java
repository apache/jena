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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * Indexes bindings so that they can be search for quickly when a binding to all the
 * variables is provided. If a binding to only some of the known variables is provided
 * then the index still works, but will search linearly.
 * Contribution from Paul Gearon (quoll)
 */
public class HashIndexTable implements IndexTable {

	final private Set<Key> table ;
	private Map<Var,Integer> varColumns ;
	private boolean missingValue ;

	public HashIndexTable(Set<Var> commonVars, QueryIterator data) throws MissingBindingException
    {
    	initColumnMappings(commonVars) ;
    	if ( commonVars.size() == 0 )
    	{
    		table = null ;
    		return ;
    	}

    	table = new HashSet<>() ;
    	missingValue = false ;

    	while ( data.hasNext() )
        {
            Binding binding = data.nextBinding() ;
            addBindingToTable(binding) ;
        }
    	data.close() ;
    }

    @Override
	public boolean containsCompatibleWithSharedDomain(Binding binding)
    {
    	// no shared variables means no shared domain, and should be ignored
    	if ( table == null )
    		return false ;

    	Key indexKey ;
		indexKey = convertToKey(binding) ;

		if ( table.contains(indexKey) )
			return true ;
		
		if ( anyUnbound(indexKey) )
			return exhaustiveSearch(indexKey) ;
		return false ;
    }

    private boolean anyUnbound(Key mappedBinding)
    {
    	for ( Node n: mappedBinding.getNodes() )
    	{
    		if ( n == null )
    			return true ;
    	}
    	return false ;
    }

    private void initColumnMappings(Set<Var> commonVars)
    {
    	varColumns = new HashMap<>() ;
    	int c = 0 ;
    	for ( Var var: commonVars )
    		varColumns.put(var, c++) ;
    }

    private void addBindingToTable(Binding binding) throws MissingBindingException
    {
    	Key key = convertToKey(binding) ;
		table.add(key) ;
		if ( missingValue )
			throw new MissingBindingException(table, varColumns) ;
    }

    private Key convertToKey(Binding binding)
    {
		Node[] indexKey = new Node[varColumns.size()] ;

		for ( Map.Entry<Var,Integer> varCol : varColumns.entrySet() )
		{
			Node value = binding.get(varCol.getKey()) ;
			if ( value == null )
				missingValue = true ;
			indexKey[varCol.getValue()] = value ;
		}
		return new Key(indexKey) ;
    }

    private boolean exhaustiveSearch(Key mappedBindingLeft)
    {
    	for ( Key mappedBindingRight: table )
    	{
    		if ( mappedBindingLeft.compatibleAndSharedDomain(mappedBindingRight) )
    			return true ;
    	}
    	return false ;
    }

    static class MissingBindingException extends Exception {
    	private final Set<Key> data ;
    	private final Map<Var,Integer> varMappings ;

    	public MissingBindingException(Set<Key> data, Map<Var,Integer> varMappings)
    	{
    		this.data = data ;
    		this.varMappings = varMappings ;
    	}

    	public Set<Key> getData() { return data ; }
    	public Map<Var,Integer> getMap() { return varMappings ; }
    }
    
    static class Key
    {
    	final Node[] nodes;

    	Key(Node[] nodes)
    	{
    		this.nodes = nodes ;
    	}

    	public Node[] getNodes()
    	{
    		return nodes;
    	}

    	@Override
		public String toString()
    	{
    		return Arrays.asList(nodes).toString() ;
    	}

    	@Override
		public int hashCode()
    	{
    		int result = 0 ;
    		for ( Node n: nodes )
    			result ^= (n == null) ? 0 : n.hashCode() ;
    		return result ;
    	}
    	
    	@Override
		public boolean equals(Object o)
    	{
    		if ( ! (o instanceof Key) )
    			return false ;
    		Node[] other = ((Key)o).nodes ;

    		for ( int i = 0 ; i < nodes.length ; i++ )
    		{
    			if ( nodes[i] == null)
    			{
    				if ( other[i] != null )
        				return false ;
    			}
    			else
    			{
	    			if ( ! nodes[i].equals(other[i]) )
	    				return false ;
    			}
    		}
    		return true ;
    	}

        public boolean compatibleAndSharedDomain(Key mappedBindingR)
        {
        	Node[] nodesRight = mappedBindingR.getNodes() ;

        	boolean sharedDomain = false ;
        	for ( int c = 0 ; c < nodes.length ; c++ )
            {
                Node nLeft  = nodes[c] ; 
                Node nRight = nodesRight[c] ;
                
                if ( nLeft != null && nRight != null )
            	{
            		if ( nLeft.equals(nRight) )
            			return false ;
            		sharedDomain = true ;
            	}
            }
            return sharedDomain ;
        }
    }
}

