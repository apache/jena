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

import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * A simplex or multiplex table of {@link Triple}s.
 *
 */
public interface TripleTable extends TupleTable<Triple> {

	/**
	 * Search the table using a pattern of slots. {@link Node#ANY} or <code>null</code> will work as a wildcard.
	 *
	 * @param s the subject node of the pattern
	 * @param p the predicate node of the pattern
	 * @param o the object node of the pattern
	 * @return an {@link Stream} of matched triples
	 */
	Stream<Triple> find(Node s, Node p, Node o);

	@Override
	default void clear() {
		find(null, null, null).forEach(this::delete);
	}
}
