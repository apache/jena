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
package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A simple graph implementation that wraps a collection of triples.
 * 
 * This is intended to be used in places where a graph is required but
 * iteration is the only expected operation. All graph operations are supported
 * but many are not efficient and will be slow on large collections. In these
 * cases a memory based graph will be more efficient.
 * <p>
 * This implementation:
 * <ul>
 * <li>
 * Does not support deleting triples from the iterator
 * </li><li>
 * Does not handle literal typing
 * </li></ul>
 */
public class CollectionGraph extends GraphBase
{

	// override methods that need to be false off.
	private Capabilities cgCapabilities = new AllCapabilities() {

		@Override
		public boolean iteratorRemoveAllowed() {
			return iteratorDeleteAllowed;
		}

		@Override
		public boolean handlesLiteralTyping() {
			return false;
		}
		
	};
	
	static class TripleMatchFilterEquality extends Filter<Triple>
    {
        final protected Triple tMatch;
    
        /** Creates new TripleMatchFilter */
        public TripleMatchFilterEquality(Triple tMatch) 
            { this.tMatch = tMatch; }
        
        @Override
        public boolean accept(Triple t)
        {
            return tripleContained(tMatch, t) ;
        }
        
    }
	static boolean tripleContained(Triple patternTriple, Triple dataTriple)
    {
        return
            equalNode(patternTriple.getSubject(),   dataTriple.getSubject()) &&
            equalNode(patternTriple.getPredicate(), dataTriple.getPredicate()) &&
            equalNode(patternTriple.getObject(),    dataTriple.getObject()) ;
    }
    
    private static boolean equalNode(Node m, Node n)
    {
        // m should not be null unless .getMatchXXXX used to get the node.
        // Language tag canonicalization
        n = fixupNode(n) ;
        m = fixupNode(m) ;
        return (m==null) || (m == Node.ANY) || m.equals(n) ;
    }
    
    private static Node fixupNode(Node node)
    {
        if ( node == null || node == Node.ANY )
            return node ;

        // RDF says ... language tags should be canonicalized to lower case.
        if ( node.isLiteral() )
        {
            String lang = node.getLiteralLanguage() ;
            if ( lang != null && ! lang.equals("") )
                node = NodeFactory.createLiteral(node.getLiteralLexicalForm(),
                                          lang.toLowerCase(Locale.ROOT),
                                          node.getLiteralDatatype()) ;
        }
        return node ; 
    }
    
	// the collection
	private final Collection<Triple> triples;
	private final boolean uniqueOnly;
	private final boolean iteratorDeleteAllowed;
	
	/**
	 * Construct an empty graph using an empty HashSet.
	 * Iterator deletion is supported.
	 */
	public CollectionGraph()
	{
		this(new HashSet<Triple>(), true);
	}

	/**
	 * Construct a graph from a collection.
	 * 
	 * Iterator deletion is not supported.
	 * @param triples
	 *            The collection of triples.
	 */
	public CollectionGraph( final Collection<Triple> triples )
	{
		this(triples, false);
	}
	
	/**
	 * Construct a graph from a collection.
	 * @param triples The collection of triples.
	 * @param iteratorDeleteAllowed if true iterator on triple supports deletion and we want to enable iterator deletion.
	 */
	public CollectionGraph( final Collection<Triple> triples, boolean iteratorDeleteAllowed)
	{
		this.triples = triples;
		this.uniqueOnly = triples instanceof Set;
		this.iteratorDeleteAllowed = iteratorDeleteAllowed;
	}

	@Override
	protected ExtendedIterator<Triple> graphBaseFind( final TripleMatch m )
	{
		ExtendedIterator<Triple> iter =null;
		if (iteratorDeleteAllowed)
		{
			
	        iter =
	            SimpleEventManager.notifyingRemove( this, triples.iterator() );
		}
		else
		{
			iter = WrappedIterator.createNoRemove( triples.iterator() );
		}
		return iter 
	            .filterKeep ( new TripleMatchFilterEquality( m.asTriple() ) );
	}

	@Override
	public void performAdd( final Triple t )
	{
		if (uniqueOnly || !triples.contains(t))
		{
			triples.add(t);
		}
	}

	@Override
	public void performDelete( final Triple t )
	{
		triples.remove(t);
	}

	@Override
	public Capabilities getCapabilities() {
		return cgCapabilities;
	}
	
}
