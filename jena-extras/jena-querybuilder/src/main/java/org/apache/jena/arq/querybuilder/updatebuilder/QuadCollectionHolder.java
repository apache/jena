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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * An QuadHolder that contains Quads from a collection or iterator..
 *
 */
public class QuadCollectionHolder implements QuadHolder {

    private final Set<Quad> collection;
    private Map<Var, Node> values;

    /**
     * Constructor.
     * 
     * @param quads
     *            the collection of quads.
     */
    public QuadCollectionHolder( final Collection<Quad> quads) {
        this.collection = new HashSet<Quad>();
        this.collection.addAll( quads );
    }

    /**
     * Constructor.
     * 
     * @param quads
     *            the collection of quads.
     */
    public QuadCollectionHolder( final Iterator<Quad> quads) {
        this.collection = new HashSet<Quad>();
        quads.forEachRemaining( collection::add );
    }
    
    private Node valueMap( Node n )
    {
    	if (n.isVariable())
    	{
    		Var v = Var.alloc(n);
    		return values.getOrDefault(v, n);
    	}
    	return n;
    }

    @Override
    public ExtendedIterator<Quad> getQuads() {
    	ExtendedIterator<Quad> retval = 
    			WrappedIterator.create( collection.iterator() );
    
    	if (values != null)
    	{
    		retval = retval.mapWith( q -> new Quad(
        				valueMap(q.getGraph()),
        				valueMap(q.getSubject()),
        				valueMap(q.getPredicate()),
        				valueMap(q.getObject())
        				));
    	}
    	return retval;
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
