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
package org.apache.jena.arq.querybuilder.updatebuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * An QuadHolder that creates quads from a collection or iterator of triples.
 *
 */
public class CollectionQuadHolder implements QuadHolder {

    private final Set<Triple> collection;
    private final Node defaultGraphName;
    private Map<Var, Node> values;

    /**
     * Constructor.
     * 
     * @param graph
     *            the default graph name for the triples
     * @param triples
     *            the collection of triples.
     */
    public CollectionQuadHolder(final Node graph, Collection<Triple> triples) {
        this.collection = new HashSet<Triple>();
        this.collection.addAll( triples );
        defaultGraphName = graph;
    }

    /**
     * Constructor.
     * 
     * @param graph
     *            the default graph name for the triples
     * @param triples
     *            the iterator of triples.
     */
    public CollectionQuadHolder(final Node graph, Iterator<Triple> triples) {
        this.collection = WrappedIterator.create( triples ).toSet();
        defaultGraphName = graph;
    }

    /**
     * Constructor. Uses Quad.defaultGraphNodeGenerated for the graph name.
     * 
     * @see Quad#defaultGraphNodeGenerated
     * @param triples
     *            the collection of triples.
     */
    public CollectionQuadHolder(final Collection<Triple> triples) {
        this( Quad.defaultGraphNodeGenerated, triples );
    }

    /**
     * Constructor.
     * 
     * @param triples
     *            the iterator of triples.
     */
    public CollectionQuadHolder(Iterator<Triple> triples) {
        this.collection = WrappedIterator.create( triples ).toSet();
        defaultGraphName =  Quad.defaultGraphNodeGenerated;
    }
    
    private Node valueMap( Node n )
    {
    	if (n.isVariable())
    	{
    		Var v = Var.alloc(n);
    		Node n2 = values.get( v );
    		return n2==null?n:n2;
    	}
    	return n;
    }

    @Override
    public ExtendedIterator<Quad> getQuads() {
    	if (values == null)
    	{
    		values = Collections.emptyMap();
    	}
        return WrappedIterator.create(collection.iterator())
        		.mapWith( triple -> new Triple( 
        				valueMap(triple.getSubject()),
        				valueMap(triple.getPredicate()),
        				valueMap(triple.getObject())
        				))
        		.mapWith( triple -> new Quad( defaultGraphName, triple ) );
    }
    
    

    /**
     * This implementation does nothing.
     */
    @Override
    public QuadHolder setValues(final Map<Var, Node> values) {
    	this.values = values;
        return this;
    }

}
