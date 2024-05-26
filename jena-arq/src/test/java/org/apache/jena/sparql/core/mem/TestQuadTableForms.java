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

import static java.util.Set.of;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.sparql.core.mem.QuadTableForm.*;
import static org.apache.jena.sparql.core.mem.TupleSlot.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.sparql.core.Quad;
import org.junit.Test;

public class TestQuadTableForms extends AbstractTestTupleTableForms<QuadTableForm> {

	@Override
	protected Stream<QuadTableForm> tableForms() {
		return QuadTableForm.tableForms();
	}

	@Override
	protected Stream<Set<TupleSlot>> queryPatterns() {
		return AbstractTestQuadTable.quadQueryPatterns();
	}

	private static Map<Set<TupleSlot>, Set<QuadTableForm>> answerKey = new HashMap<Set<TupleSlot>, Set<QuadTableForm>>() {
		{
			put(of(GRAPH), of(GSPO, GOPS));
			put(of(GRAPH, SUBJECT), of(GSPO));
			put(of(GRAPH, SUBJECT, PREDICATE), of(GSPO));
			put(of(GRAPH, SUBJECT, OBJECT), of(OSGP));
			put(of(SUBJECT), of(SPOG));
			put(of(PREDICATE), of(PGSO));
			put(of(GRAPH, PREDICATE), of(PGSO));
			put(of(SUBJECT, PREDICATE), of(SPOG));
			put(of(OBJECT), of(OPSG, OSGP));
			put(of(GRAPH, OBJECT), of(GOPS));
			put(of(SUBJECT, OBJECT), of(OSGP));
			put(of(PREDICATE, OBJECT), of(OPSG));
			put(of(GRAPH, PREDICATE, OBJECT), of(GOPS));
			put(of(SUBJECT, PREDICATE, OBJECT), of(SPOG));
			put(of(SUBJECT, PREDICATE, OBJECT, GRAPH), of(GSPO, GOPS, SPOG, OPSG, OSGP, PGSO));
			put(of(), of(GSPO));
		}
	};

	@Test
	public void addAndRemoveSomeQuads() {
		tableForms().map(QuadTableForm::get).map(table -> new AbstractTestQuadTable() {

			@Override
			protected QuadTable table() {
				return table;
			}

			@Override
			protected Stream<Quad> tuples() {
				return table.find(ANY, ANY, ANY, ANY);
			}
		}).forEach(AbstractTestTupleTable::addAndRemoveSomeTuples);
	}

	@Override
	protected QuadTableForm chooseFrom(final Set<TupleSlot> sample) {
		return QuadTableForm.chooseFrom(sample);
	}

	@Override
	protected Map<Set<TupleSlot>, Set<QuadTableForm>> answerKey() {
		return answerKey;
	}
}
