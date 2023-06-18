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

import static java.util.EnumSet.allOf;
import static org.apache.jena.graph.NodeFactory.createURI;

import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Quad;

public abstract class AbstractTestQuadTable extends AbstractTestTupleTable<Quad, QuadTable> {

	protected static final Node sampleNode = createURI("info:test");
	private static final Quad q = Quad.create(sampleNode, sampleNode, sampleNode, sampleNode);
	private static final Set<Set<TupleSlot>> queryPatterns = Lib.powerSet(allOf(TupleSlot.class));

	@Override
	protected Quad testTuple() {
		return q;
	}

	@Override
	public Stream<Set<TupleSlot>> queryPatterns() {
		return quadQueryPatterns();
	}

	static Stream<Set<TupleSlot>> quadQueryPatterns() {
		return queryPatterns.stream();
	}
}
