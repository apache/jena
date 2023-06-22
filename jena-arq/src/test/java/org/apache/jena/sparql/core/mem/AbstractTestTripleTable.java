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

import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.graph.NodeFactory.createURI;

import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public abstract class AbstractTestTripleTable extends AbstractTestTupleTable<Triple, TripleTable> {

	private static final Node sampleNode = createURI("info:test");

	private static final Triple testTriple = Triple.create(sampleNode, sampleNode, sampleNode);
	private static final Set<Set<TupleSlot>> queryPatternSet =
	        Lib.powerSet( Set.of( TupleSlot.SUBJECT, TupleSlot.PREDICATE, TupleSlot.OBJECT)) ;

	@Override
	protected Triple testTuple() {
		return testTriple;
	}

	@Override
	protected Stream<Triple> tuples() {
		return table().find(ANY, ANY, ANY);
	}

	@Override
	public Stream<Set<TupleSlot>> queryPatterns() {
		return tripleQueryPatterns();
	}

	static Stream<Set<TupleSlot>> tripleQueryPatterns() {
	    return queryPatternSet.stream();
	}
}
