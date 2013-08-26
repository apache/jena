/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.security.utils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

import java.util.Collection;
import java.util.HashSet;

/**
 * A simple graph implementaiton that wraps a colleciton of triples.
 * 
 * This is intended to be used in places where a graph is required but
 * iteration is the only expected operation. All graph operations are supported
 * but many are not efficient and will be slow on large collections. In these
 * cases
 * a memory based graph may be more efficient.
 */
public class CollectionGraph extends GraphBase
{

	/**
	 * Finds matching triples
	 */
	private class MatchFilter extends Filter<Triple>
	{
		TripleMatch m;

		public MatchFilter( final TripleMatch m )
		{
			if (m == null)
			{
				throw new IllegalArgumentException("Match must not be null");
			}
			this.m = m;
		}

		@Override
		public boolean accept( final Triple t )
		{
			if (t == null)
			{
				throw new IllegalArgumentException("SecTriple must not be null");
			}
			return matches(t.getMatchSubject(), m.getMatchSubject())
					&& matches(t.getMatchPredicate(), m.getMatchPredicate())
					&& matches(t.getMatchObject(), m.getMatchObject());
		}

		private boolean isWild( final Node n )
		{
			return (n == null) || Node.ANY.equals(n);
		}

		private boolean matches( final Node t, final Node m )
		{
			return isWild(m) || isWild(t) || m.equals(t);
		}

	}

	// the collection
	Collection<Triple> triples;

	/**
	 * Construct an empty graph.
	 */
	public CollectionGraph()
	{
		this(new HashSet<Triple>());
	}

	/**
	 * Construct a graph from a collection.
	 * 
	 * @param triples
	 *            The collection of triples.
	 */
	public CollectionGraph( final Collection<Triple> triples )
	{
		super();
		this.triples = triples;
	}

	@Override
	protected ExtendedIterator<Triple> graphBaseFind( final TripleMatch m )
	{
		return WrappedIterator.create(triples.iterator()).filterKeep(
				new MatchFilter(m));
	}

	@Override
	public void performAdd( final Triple t )
	{
		triples.add(t);
	}

	@Override
	public void performDelete( final Triple t )
	{
		triples.remove(t);
	}
}
