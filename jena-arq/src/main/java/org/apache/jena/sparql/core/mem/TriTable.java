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

import static java.lang.ThreadLocal.withInitial;
import static java.util.EnumSet.noneOf;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.apache.jena.sparql.core.mem.TripleTableForm.chooseFrom;
import static org.apache.jena.sparql.core.mem.TripleTableForm.tableForms;
import static org.apache.jena.sparql.core.mem.TupleSlot.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ReadWrite;

/**
 * A three-way {@link TripleTable} using all of the available forms in {@link TripleTableForm}.
 *
 */
public class TriTable implements TripleTable {

	private final Map<TripleTableForm, TripleTable> indexBlock = new EnumMap<TripleTableForm, TripleTable>(
			tableForms().collect(toMap(x -> x, TripleTableForm::get)));

	/**
	 * A block of three indexes to which we provide access as though they were one.
	 */
	protected Map<TripleTableForm, TripleTable> indexBlock() {
		return indexBlock;
	}

	private final ThreadLocal<Boolean> isInTransaction = withInitial(() -> false);

	@Override
	public boolean isInTransaction() {
		return isInTransaction.get();
	}

	protected void isInTransaction(final boolean b) {
		isInTransaction.set(b);
	}

	@Override
	public void commit() {
		indexBlock().values().forEach(TripleTable::commit);
		end();
	}

	@Override
	public void abort() {
		indexBlock().values().forEach(TripleTable::abort);
		end();
	}

	@Override
	public void end() {
		indexBlock().values().forEach(TripleTable::end);
		isInTransaction.remove();
	}

	@Override
	public Stream<Triple> find(final Node s, final Node p, final Node o) {
		final Set<TupleSlot> pattern = noneOf(TupleSlot.class);
		if (isConcrete(s)) pattern.add(SUBJECT);
		if (isConcrete(p)) pattern.add(PREDICATE);
		if (isConcrete(o)) pattern.add(OBJECT);
		final TripleTableForm choice = chooseFrom(pattern);
		return indexBlock().get(choice).find(s, p, o);
	}

	private static boolean isConcrete(final Node n) {
		return nonNull(n) && n.isConcrete();
	}

	@Override
	public void add(final Triple t) {
		indexBlock().values().forEach(index -> index.add(t));
	}

	@Override
	public void delete(final Triple t) {
		indexBlock().values().forEach(index -> index.delete(t));
	}

	@Override
	public void begin(final ReadWrite rw) {
		isInTransaction(true);
		indexBlock().values().forEach(table -> table.begin(rw));
	}

	@Override
	public void clear() {
		indexBlock().values().forEach(TripleTable::clear);
	}
}
