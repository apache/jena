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

package org.apache.jena.riot.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.atlas.lib.Tuple;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.graph.Node;

/**
 * Collector stream for quads.
 * 
 * @see CollectorStreamBase
 */
public class CollectorStreamTuples extends CollectorStreamBase<Tuple<Node>>	implements StreamRDF {
	private List<Tuple<Node>> tuples = new ArrayList<Tuple<Node>>();

	@Override
	public void start() {
		tuples.clear();
	}

	@Override
	public void tuple(Tuple<Node> tuple) {
		tuples.add(tuple);
	}

	@Override
	public Collection<Tuple<Node>> getCollected() {
		return tuples;
	}
}
