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
package org.apache.jena.arq.querybuilder.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;

public class ValuesHandler implements Handler {

	private final List<Var> variables;
	
	private final List<Collection<Node>> values;
	
	private Map<Var,Node> valueMap;
	
	// the query to modify
		private final Query query;

		/**
		 * Constructor.
		 * 
		 * @param query
		 *            The query to manipulate.
		 */
		public ValuesHandler(Query query) {
			this.query = query;
			this.variables = new ArrayList<Var>();
			this.values  = new ArrayList<Collection<Node>>();
			this.valueMap = Collections.emptyMap();
		}

	
	@Override
	public void setVars(Map<Var, Node> values) {
		valueMap = values;
	}

	@Override
	public void build() {
		List<Var> vars = new ArrayList<Var>( variables );
		vars.removeAll( valueMap.keySet());
		if (vars.isEmpty())
		{
			return;
		}
		
		List<Binding> bindings = new ArrayList<Binding>();
		for (Collection<Node> col : values)
		{
			BindingHashMap b = new BindingHashMap();
			if (col.size() != variables.size())
			{
				throw new QueryBuildException(
						String.format( "The number of variables (%s) does not match the number of nodes in the data block (%s): %s",
								variables.size(), col.size(), variables)) ;
			}
			Iterator<Node> iter = col.iterator();
			for (int i=0;i<variables.size();i++)
			{
				Var v = variables.get(i);
				Node n = iter.next();
				if (valueMap.containsKey(v))
				{
					continue;
				}
				if (valueMap.containsKey(n))
				{
					n = valueMap.get(n);
				}
				b.add(v, n);
			}
			if (!b.isEmpty())
			{
				bindings.add(b);
			}
		}
		if (!bindings.isEmpty()) {
			query.setValuesDataBlock(vars, bindings);
		}
	}

	/**
	 * Add a variable to the value statement.
	 * 
	 * A variable may only be added once. Attempting to add the same variable
	 * multiple times will be silently ignored.
	 * 
	 * @param var
	 *            The variable to add.
	 */
	public void addValueVar(Var var) {
		variables.add(var);
	}

	/**
	 * Add the values for the variables.  There must be one value for each value var.
	 * 
	 * @param values
	 *            the collection of values to add.
	 * @return The builder for chaining.
	 */
	public void addDataBlock(Collection<Node> values) {
		this.values.add(values);
	}
	
	/**
	 * Add the ValuesHandler values to this values Handler.
	 * @param handler the handler that has the values to add.
	 */
	public void addAll( ValuesHandler handler )
	{
		this.values.addAll( handler.values );
		this.variables.addAll( handler.variables );
	}

}
