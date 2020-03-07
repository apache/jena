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

import java.util.Map;

import org.apache.jena.arq.querybuilder.Converters;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.SingletonIterator;

/**
 * A QuadHolder implementation for a single quad.
 */
public class SingleQuadHolder implements QuadHolder{

	private Quad quad;
	private Quad updated;

	/**
	 * Constructor for a single quad.
	 * @param quad the quad to hold.
	 */
	public SingleQuadHolder( Quad quad )
	{
		if (quad.getGraph().isVariable() || quad.getSubject().isVariable() || quad.getPredicate().isVariable() ||
				quad.getObject().isVariable())
		{
			this.quad = new Quad( Converters.checkVar( quad.getGraph()),
					Converters.checkVar( quad.getSubject() ),
					Converters.checkVar( quad.getPredicate()),
					Converters.checkVar( quad.getObject()));
		} else {
			this.quad = quad;
		}
	}

	/**
	 * Constructor from a triple
	 * Uses  Quad.defaultGraphNodeGenerated for the graph name.
	 * 
	 * @see  Quad#defaultGraphNodeGenerated 
	 * @param triple the triple to convert to a quad.
	 */
	public SingleQuadHolder( Triple triple )
	{
		this.quad = new Quad( Quad.defaultGraphNodeGenerated, 
				Converters.checkVar( triple.getSubject()),
				Converters.checkVar( triple.getPredicate()),
				Converters.checkVar( triple.getObject())				
				);
	}




	/**
	 * Constructor from a triple
	 * @param graph the graph name to use for the  triple 
	 * @param triple the triple to convert to a quad.
	 */
	public SingleQuadHolder( Node graph, Triple triple )
	{
		this.quad = new Quad( graph, triple );
	}

	@Override
	public ExtendedIterator<Quad> getQuads() {
		return new SingletonIterator<Quad>(updated==null?quad:updated);
	}

	// convert variable if in map
	private Node mapValue( Node n, Map<Var, Node> values)
	{
		Node retval = null;
		if (n.isVariable())
		{
			final Var v = Var.alloc(n);
			retval = values.get(v);
		}
		return retval==null?n:retval;
	}

	@Override
	public QuadHolder setValues(Map<Var, Node> values) {
		updated = new Quad( 
				mapValue( quad.getGraph(), values),
				mapValue( quad.getSubject(), values),
				mapValue( quad.getPredicate(), values),
				mapValue( quad.getObject(), values)
				);
		return this;
	}


}
