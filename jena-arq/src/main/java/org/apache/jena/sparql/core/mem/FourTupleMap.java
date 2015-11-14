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

import org.apache.jena.atlas.lib.persistent.PMap;
import org.apache.jena.atlas.lib.persistent.PersistentSet;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.mem.FourTupleMap.ThreeTupleMap;

import com.github.andrewoma.dexx.collection.Map;

/**
 * A {@link PMap} of {@link Node}s: {@code Node->Node->Node->PersistentSet<Node>}
 */
public class FourTupleMap extends PMap<Node, ThreeTupleMap, FourTupleMap> {

	private FourTupleMap(final com.github.andrewoma.dexx.collection.Map<Node, ThreeTupleMap> wrappedMap) {
		super(wrappedMap);
	}

	/**
	 * Default constructor.
	 */
	public FourTupleMap() {
		super();
	}

	@Override
	protected FourTupleMap wrap(final Map<Node, ThreeTupleMap> wrappedMap) {
		return new FourTupleMap(wrappedMap);
	}

	/**
	 * A {@link PMap} of {@link Node}s: {@code Node->Node->PersistentSet<Node>}
	 */
	public static class ThreeTupleMap extends PMap<Node, TwoTupleMap, ThreeTupleMap> {
		private ThreeTupleMap(final com.github.andrewoma.dexx.collection.Map<Node, TwoTupleMap> wrappedMap) {
			super(wrappedMap);
		}

		/**
		 * Default constructor.
		 */
		public ThreeTupleMap() {
			super();
		}

		@Override
		protected ThreeTupleMap wrap(final Map<Node, TwoTupleMap> wrappedMap) {
			return new ThreeTupleMap(wrappedMap);
		}
	}

	/**
	 * A {@link PMap} of {@link Node}s: {@code Node->PersistentSet<Node>}
	 */
	public static class TwoTupleMap extends PMap<Node, PersistentSet<Node>, TwoTupleMap> {

		private TwoTupleMap(final com.github.andrewoma.dexx.collection.Map<Node, PersistentSet<Node>> wrappedMap) {
			super(wrappedMap);
		}

		/**
		 * Default constructor.
		 */
		public TwoTupleMap() {
			super();
		}

		@Override
		protected TwoTupleMap wrap(final Map<Node, PersistentSet<Node>> wrappedMap) {
			return new TwoTupleMap(wrappedMap);
		}
	}
}
