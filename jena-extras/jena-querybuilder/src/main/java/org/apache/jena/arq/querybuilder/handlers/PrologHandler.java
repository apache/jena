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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.Query ;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Var ;

/**
 * The prolog handler
 *
 */
public class PrologHandler implements Handler {

	// the query to modify.
	private final Query query;

	/**
	 * Constructor.
	 * @param query The query to handle.
	 */
	public PrologHandler(Query query) {
		this.query = query;
	}

	/**
	 * get the canonical prefix name.  
	 * 
	 * Removes ':' from the end of the name if present.
	 * 
	 * @param x The prefix name
	 * @return The prefix name with the trailing ':' removed.
	 */
	private static String canonicalPfx(String x) {
		if (x.endsWith(":"))
			return x.substring(0, x.length() - 1);
		return x;
	}

	/**
	 * Set the base for the query.  This is the IRI against which relative names will be resolved.
	 * @param resolver The IRIResolver to set the base from.
	 */
	public void setBase(IRIResolver resolver) {
		query.setBaseURI(resolver);

	}

	/**
	 * Set the base for the query.  This is the IRI against which relative names will be resolved.
	 * @param base The string to set the base from.
	 */
	public void setBase(String base) {
		query.setBaseURI(base);
	}

	/**
	 * Add a prefix to the prefix mapping.
	 * @param pfx The prefix to add.
	 * @param uri The uri to resolve the prefix to.
	 */
	public void addPrefix(String pfx, String uri) {
		query.setPrefix(canonicalPfx(pfx), uri);
	}
	
	/**
	 * Clear the prefix mapping.
	 */
	public void clearPrefixes() {
		query.setPrefixMapping( new PrefixMappingImpl() );
	}

	/**
	 * Add the map of prefixes to the query prefixes.
	 * @param prefixes The map of prefixes to URIs.
	 */
	public void addPrefixes(Map<String, String> prefixes) {
		for (Map.Entry<String, String> e : prefixes.entrySet()) {
			addPrefix(e.getKey(), e.getValue());
		}
	}
	
	public PrefixMapping getPrefixes() {
		return query.getPrefixMapping();
	}
	
	public ExprFactory getExprFactory() {
		return new ExprFactory( query.getPrefixMapping() );
	}

	/**
	 * Add prefixes from a prefix mapping.
	 * @param prefixes THe prefix mapping to add from.
	 */
	public void addPrefixes(PrefixMapping prefixes) {
		query.getPrefixMapping().setNsPrefixes(prefixes);
	}

	/**
	 * Add the settings from the prolog handler argument.
	 * @param pfxHandler The PrologHandler to read from
	 */
	public void addAll(PrologHandler pfxHandler) {
		String val = StringUtils.defaultIfEmpty(pfxHandler.query.getBaseURI(),
				query.getBaseURI());
		if (val != null) {
			setBase(val);
		}
		addPrefixes(pfxHandler.query.getPrefixMapping());
	}

	@Override
	public void setVars(Map<Var, Node> values) {
		// nothing to do
	}

	@Override
	public void build() {
		// no special operation to perform
	}
}
