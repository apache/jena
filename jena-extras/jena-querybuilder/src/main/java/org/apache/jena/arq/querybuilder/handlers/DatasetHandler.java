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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node ;
import org.apache.jena.query.Query ;
import org.apache.jena.sparql.core.Var ;

/**
 * Handler for a dataset.
 *
 */
public class DatasetHandler implements Handler {

	// the query to manage
	private final Query query;

	/**
	 * Constructor.
	 * @param query The query the handler will manage.
	 */
	public DatasetHandler(Query query) {
		this.query = query;
	}

	/**
	 * Add all the dataset information from the handler argument.
	 * @param datasetHandler The handler to copy from.
	 */
	public void addAll(DatasetHandler datasetHandler) {
		from(datasetHandler.query.getGraphURIs());
		fromNamed(datasetHandler.query.getNamedGraphURIs());
	}

	/**
	 * Add a graph name to the from named list.
	 * @param graphName The graph name to add.
	 */
	public void fromNamed(String graphName) {
		query.addNamedGraphURI(graphName);
	}

	/**
	 * Add the graph names to the from named list.
	 * 
	 * The names are ordered in as defined in the collection.
	 * 
	 * @param graphNames The from names to add.
	 */
	public void fromNamed(Collection<String> graphNames) {
		for (String uri : graphNames) {
			query.addNamedGraphURI(uri);
		}
	}

	/**
	 * Add the graph names to the from list.
	 * @param graphName the name to add.
	 */
	public void from(String graphName) {
		query.addGraphURI(graphName);
	}

	/**
	 * Add the graph names to the named list. 
	 * 
	 * The names are ordered in as defined in the collection.
	 * 
	 * @param graphNames The names to add.
	 */
	public void from(Collection<String> graphNames) {
		for (String uri : graphNames) {
			query.addGraphURI(uri);
		}
	}

	/**
	 * Set the variables for field names that contain lists of strings.
	 * 
	 * Strings that start with "?" are assumed to be variable names and will be replaced
	 * with matching node.toString() values. 
	 * 
	 * @param values The values to set.
	 * @param fieldName The field name in Query that contain a list of strings.
	 */
	private void setVars(Map<Var, Node> values, String fieldName) {
		if (values.isEmpty()) {
			return;
		}
		try {
			Field f = Query.class.getDeclaredField(fieldName);
			f.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<String> orig = (List<String>) f.get(query);
			List<String> lst = null;
			if (orig != null) {
				lst = new ArrayList<String>();
				for (String s : orig) {
					Node n = null;
					if (s.startsWith("?")) {
						Var v = Var.alloc(s.substring(1));
						n = values.get(v);
					}
					lst.add(n == null ? s : n.toString());
				}
				f.set(query, lst);
			}
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException(e.getMessage(), e);
		} catch (SecurityException e) {
			throw new IllegalStateException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	@Override
	public void setVars(Map<Var, Node> values) {
		setVars(values, "namedGraphURIs");
		setVars(values, "graphURIs");
	}

	@Override
	public void build() {
		// nothing special to do
	}

}
