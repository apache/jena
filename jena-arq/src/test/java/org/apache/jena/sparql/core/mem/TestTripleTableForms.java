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
import static org.apache.jena.sparql.core.mem.TripleTableForm.OSP;
import static org.apache.jena.sparql.core.mem.TripleTableForm.POS;
import static org.apache.jena.sparql.core.mem.TripleTableForm.SPO;
import static org.apache.jena.sparql.core.mem.TupleSlot.OBJECT;
import static org.apache.jena.sparql.core.mem.TupleSlot.PREDICATE;
import static org.apache.jena.sparql.core.mem.TupleSlot.SUBJECT;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;
import org.junit.Test;

public class TestTripleTableForms extends AbstractTestTupleTableForms<TripleTableForm> {

	@Override
	public Stream<Set<TupleSlot>> queryPatterns() {
		return AbstractTestTripleTable.tripleQueryPatterns();
	}

	@Override
	protected Stream<TripleTableForm> tableForms() {
		return TripleTableForm.tableForms();
	}

	@Test
	public void addAndRemoveSomeTriples() {
		tableForms().map(TripleTableForm::get).map(table -> new AbstractTestTripleTable() {

			@Override
			protected TripleTable table() {
				return table;
			}

			@Override
			protected Stream<Triple> tuples() {
				return table.find(ANY, ANY, ANY);
			}
		}).forEach(AbstractTestTupleTable::addAndRemoveSomeTuples);
	}

	@Override
	protected TripleTableForm chooseFrom(final Set<TupleSlot> sample) {
		return TripleTableForm.chooseFrom(sample);
	}

	private final Map<Set<TupleSlot>, Set<TripleTableForm>> answerKey = new HashMap<Set<TupleSlot>, Set<TripleTableForm>>() {
		{
			put(of(SUBJECT), of(SPO));
			put(of(PREDICATE), of(POS));
			put(of(SUBJECT, PREDICATE), of(SPO));
			put(of(OBJECT), of(OSP));
			put(of(SUBJECT, OBJECT), of(OSP));
			put(of(PREDICATE, OBJECT), of(POS));
			put(of(SUBJECT, PREDICATE, OBJECT), of(SPO));
			put(of(), of(SPO));
		}
	};

	@Override
	protected Map<Set<TupleSlot>, Set<TripleTableForm>> answerKey() {
		return answerKey;
	}
}
