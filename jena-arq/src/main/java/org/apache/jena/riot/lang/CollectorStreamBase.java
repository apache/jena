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

package org.apache.jena.riot.lang;

import java.util.Collection;

import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad ;

/**
 * Base class for StreamRDF implementations which store received {@code <T>}
 * objects in a {@link java.util.Collection}.
 *
 * The resulting collection can be retrieved via the {@link #getCollected()}
 * method.
 *
 * Implementations are suitable for single-threaded parsing, for use with small
 * data or distributed computing frameworks (e.g. Hadoop) where the overhead
 * of creating many threads is significant.
 *
 * @param <T> Type of the value stored in the collection
 */
public abstract class CollectorStreamBase<T> implements StreamRDF {
	private final PrefixMap prefixes = PrefixMapFactory.create();
	private String baseIri;

	@Override
	public void finish() {}

	@Override
	public void triple(Triple triple) {}

	@Override
	public void quad(Quad quad) {}

	@Override
	public void start() {}

	@Override
	public void base(String base) {
		this.baseIri = base;
	}

	@Override
	public void prefix(String prefix, String iri) {
		prefixes.add(prefix, iri);
	}

	public PrefixMap getPrefixes() {
		return prefixes;
	}

	public String getBaseIri() {
		return baseIri;
	}

	/**
	 * @return The collection received by this instance.
	 */
	public abstract Collection<T> getCollected();
}
