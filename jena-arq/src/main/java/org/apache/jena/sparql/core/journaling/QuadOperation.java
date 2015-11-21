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

import static java.util.Objects.hash;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.journaling.Operation.InvertibleOperation;

/**
 * An {@link Operation} sending a {@link Quad} against a {@link DatasetGraph}.
 *
 * @param <SelfType> the selftype of this operation
 * @param <InverseType> the type of this operation's inverse
 *
 */
public abstract class QuadOperation<SelfType extends QuadOperation<SelfType, InverseType>, InverseType extends QuadOperation<InverseType, SelfType>>
		implements InvertibleOperation<Quad, DatasetGraph, SelfType, InverseType> {

	protected final Quad quad;

	/**
	 * @param q the quad that is being operated with
	 */
	public QuadOperation(final Quad q) {
		this.quad = q;
	}

	/**
	 * The addition of a {@link Quad} to a {@link DatasetGraph}
	 */
	public static class QuadAddition extends QuadOperation<QuadAddition, QuadDeletion> {

		/**
		 * @param q the quad that is being operated with
		 */
		public QuadAddition(final Quad q) {
			super(q);
		}

		@Override
		public Quad data() {
			return quad;
		}

		@Override
		public QuadDeletion inverse() {
			return new QuadDeletion(data());
		}

		@Override
		public void actOn(final DatasetGraph dsg) {
			dsg.add(data());
		}

		@Override
		public String toString() {
			return "ADD " + super.toString();
		}

		@Override
		public int hashCode() {
			return hash(data());
		}

		@Override
		public boolean equals(final Object other) {
			if (other instanceof QuadAddition) return data().equals(((QuadAddition) other).data());
			return false;
		}
	}

	/**
	 * The deletion of a {@link Quad} from a {@link DatasetGraph}
	 */
	public static class QuadDeletion extends QuadOperation<QuadDeletion, QuadAddition> {

		/**
		 * @param q the quad that is being operated with
		 */
		public QuadDeletion(final Quad q) {
			super(q);
		}

		@Override
		public Quad data() {
			return quad;
		}

		@Override
		public QuadAddition inverse() {
			return new QuadAddition(data());
		}

		@Override
		public void actOn(final DatasetGraph dsg) {
			dsg.delete(data());
		}

		@Override
		public String toString() {
			return "DELETE " + super.toString();
		}

		@Override
		public int hashCode() {
			return hash(data());
		}

		@Override
		public boolean equals(final Object other) {
			if (other instanceof QuadDeletion) return data().equals(((QuadDeletion) other).data());
			return false;
		}
	}
}
