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

package org.apache.jena.sparql.core.mem;

import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.DatasetPrefixStorage;

/**
 * A simple {@link DatasetPrefixStorage} for in-memory datasets.
 */
public class DatasetPrefixStorageInMemory implements DatasetPrefixStorage {

	private Map<String, PrefixMapping> prefixMappings = new ConcurrentHashMap<>();

	/**
	 * A mapping from graph name to {@link PrefixMapping} for that graph.
	 */
	private Map<String, PrefixMapping> prefixMappings() {
		return prefixMappings;
	}

	@Override
	public void close() {
		prefixMappings = null;
	}

	@Override
	public void sync() {
		// NO OP
	}

	@Override
	public Set<String> graphNames() {
		return prefixMappings().keySet();
	}

	@Override
	public String readPrefix(final String graphName, final String prefix) {
		return getPrefixMapping(graphName).getNsPrefixURI(prefix);
	}

	@Override
	public String readByURI(final String graphName, final String uriStr) {
		return getPrefixMapping(graphName).getNsURIPrefix(uriStr);
	}

	@Override
	public Map<String, String> readPrefixMap(final String graphName) {
		return getPrefixMapping(graphName).getNsPrefixMap();
	}

	@Override
	public void insertPrefix(final String graphName, final String prefix, final String uri) {
		getPrefixMapping(graphName).setNsPrefix(prefix, uri);
	}

	@Override
	public void loadPrefixMapping(final String graphName, final PrefixMapping pmap) {
		getPrefixMapping(graphName).setNsPrefixes(pmap);
	}

	@Override
	public void removeFromPrefixMap(final String graphName, final String prefix) {
		getPrefixMapping(graphName).removeNsPrefix(prefix);
	}

	@Override
	public PrefixMapping getPrefixMapping() {
		return getPrefixMapping(defaultGraphIRI.getURI());
	}

	@Override
	public PrefixMapping getPrefixMapping(final String graphName) {
		return prefixMappings().computeIfAbsent(graphName, x -> new PrefixMappingImpl());
	}
}
