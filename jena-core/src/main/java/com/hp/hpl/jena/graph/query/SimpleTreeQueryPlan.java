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
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;

/**
    Incomplete class. Do not use.
*/
public class SimpleTreeQueryPlan implements TreeQueryPlan
	{
	private Graph pattern;
	private Graph target;
	
	public SimpleTreeQueryPlan( Graph target, Graph pattern )
		{
		this.target = target;
		this.pattern = pattern;
		}
		
	@Override
    public Graph executeTree() 
		{ 
		Graph result = Factory.createGraphMem();
		Set<Node> roots = getRoots( pattern );
		for (Iterator<Node> it = roots.iterator(); it.hasNext(); handleRoot( result, it.next(), new HashSet<Triple>() )) {}
		return result;
		}
		
	private Iterator<Triple> findFromTriple( Graph g, Triple t )
		{
		return g.find( asPattern( t.getSubject() ), asPattern( t.getPredicate() ), asPattern( t.getObject() ) );
		}
		
	private Node asPattern( Node x )
		{ return x.isBlank() ? null : x; }
		
	private void handleRoot( Graph result, Node root, Set<Triple> pending )
		{
		ClosableIterator<Triple> it = pattern.find( root, null, null );
		if (!it.hasNext())
			{
			absorb( result, pending );
			return;
			}
		while (it.hasNext())
			{
			Triple base = it.next();
			Iterator<Triple> that = findFromTriple( target, base ); 
			while (that.hasNext())
				{
				Triple x = that.next();
				pending.add( x );
				handleRoot( result, base.getObject(), pending );
				}
			}
		}
		
	private void absorb( Graph result, Set<Triple> triples )
		{
		for (Iterator<Triple> it = triples.iterator(); it.hasNext(); result.add( it.next())) {}
		triples.clear(); 
		}
		
	public static Set<Node> getRoots( Graph pattern )
		{
		Set<Node> roots = CollectionFactory.createHashedSet();
		ClosableIterator<Triple> sub = GraphUtil.findAll( pattern );
		while (sub.hasNext()) roots.add( sub.next().getSubject() );
		ClosableIterator<Triple> obj = GraphUtil.findAll( pattern );
		while (obj.hasNext()) roots.remove( obj.next().getObject() );
		return roots;
		}
	}
