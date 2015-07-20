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

import java.util.function.Consumer;

/**
 * A reversible record of a series of operations.
 *
 * @param <OpType> the type of operation recorded
 */
public interface ReversibleOperationRecord<OpType extends Operation<?, ?>> extends Consumer<OpType> {

	/**
	 * For each {@link Operation} in this record from the least-recently added to the most-recently added, use
	 * <code>consumer::accept</code> on that operation and then discard that operation from the record.
	 *
	 * @param consumer the consumer to use in consuming this record
	 */
	void consume(Consumer<OpType> consumer);

	/**
	 * Clear this record.
	 */
	default void clear() {
		consume(op -> {}); // /dev/null
	}

	/**
	 * Produces a time-reversed version of this record. If a record is compact (i.e. every operation in it caused a
	 * definite change in the state of the service against which it was run) and this record is run against a service,
	 * and then its reverse is run against a service with each operation inverted, no change should result in the state
	 * of that service. This method may not produce an independent object: in other words, it may not be possible to use
	 * {@link #reverse()} on the result of this method to recover the original.
	 *
	 * @return the reverse of this record
	 */
	ReversibleOperationRecord<OpType> reverse();
}
