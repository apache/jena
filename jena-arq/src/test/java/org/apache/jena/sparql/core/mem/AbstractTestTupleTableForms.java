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

import static java.util.stream.Collectors.toSet;
import static org.apache.jena.sparql.core.mem.AbstractTestTupleTable.allWildcardQuery;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractTestTupleTableForms<TableForm extends Predicate<Set<TupleSlot>>> extends Assert {

	protected abstract Stream<TableForm> tableForms();

	protected abstract Stream<Set<TupleSlot>> queryPatterns();

	@Test
	public void anAllWildcardQueryCannotAvoidTraversal() {
		assertTrue(tableForms().noneMatch(form -> form.test(allWildcardQuery)));
	}

	@Test
	public void anyIndexCanAnswerAnEntirelyConcretePattern() {
		tableForms().allMatch(form -> form.test(allWildcardQuery));
	}

	protected boolean canAvoidTraversal(final Set<TupleSlot> pattern) {
		return tableForms().anyMatch(form -> form.test(pattern));
	}

	@Test
	public void allQueriesWithAtLeastOneConcreteNodeCanAvoidTraversal() {
		assertTrue(queryPatterns().filter(p -> !allWildcardQuery.equals(p)).allMatch(this::canAvoidTraversal));
	}

	protected void avoidsTraversal(final Predicate<Set<TupleSlot>> indexForm,
			final Set<Set<TupleSlot>> correctAnswers) {
		final Set<Set<TupleSlot>> answers = queryPatterns().filter(indexForm::test).collect(toSet());
		assertEquals(correctAnswers, answers);
	}

	@Test
	public void aCorrectIndexIsChosenForEachPattern() {
		answerKey().forEach((sample, correctAnswers) -> {
			assertTrue(correctAnswers.contains(chooseFrom(sample)));
		});
	}

	protected abstract TableForm chooseFrom(Set<TupleSlot> sample);

	protected abstract Map<Set<TupleSlot>, Set<TableForm>> answerKey();
}
