/**
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

package org.apache.jena.sparql.core.journaling;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.sparql.core.journaling.Operation.InvertibleOperation;

/**
 * A {@link ReversibleOperationRecord} using a {@link List} to record operations.
 *
 * @param <OpType> the type of {@link Operation} contained in this record
 */
public class ListBackedOperationRecord<OpType extends InvertibleOperation<?, ?, ?, ?>>
		implements ReversibleOperationRecord<OpType> {

	/**
	 * A {@link List} into which we will record operations. The iterator of this list _must_ implement {@link Iterator#remove()}
	 * for {@link #consume(Consumer)} to operate correctly!
	 */
	private final List<OpType> operations;

	/**
	 * @param ops a list into which we will record operations. The iterator of this list _must_ implement
	 *        {@link Iterator#remove()} for {@link #consume(Consumer)} to operate correctly!
	 */
	public ListBackedOperationRecord(final List<OpType> ops) {
		operations = ops;
	}

	@Override
	public void accept(final OpType op) {
		operations.add(op);
	}

	@Override
	public ListBackedOperationRecord<OpType> reverse() {
		return new ListBackedOperationRecord<>(Lists.reverse(operations));
	}

	@Override
	public void consume(final Consumer<OpType> consumer) {
		final Iterator<OpType> i = operations.iterator();
		while (i.hasNext()) {
			consumer.accept(i.next());
			i.remove();
		}
	}

	@Override
	public void clear() {
		operations.clear();
	}
}
