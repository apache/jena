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

package org.apache.jena.graph.impl;

import static org.apache.jena.testing_framework.GraphHelper.memGraph;

import java.util.HashSet;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.testing_framework.AbstractGraphProducer;
import org.junit.runner.RunWith;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractImpl;
import org.xenei.junit.contract.ContractSuite;
import org.xenei.junit.contract.IProducer;

@RunWith(ContractSuite.class)
@ContractImpl(CollectionGraph.class)
public class CollectionGraph_CS {
	 
	protected IProducer<CollectionGraph> graphProducer;
	
	public CollectionGraph_CS() {
		graphProducer = new AbstractGraphProducer<CollectionGraph>() {
			
			@Override
			protected CollectionGraph createNewGraph() {
				return new CollectionGraph( new HashSet<Triple>() );
			}

			@Override
			public Graph[] getDependsOn(Graph d) {
				return null;
			}

			@Override
			public Graph[] getNotDependsOn(Graph g) {
				return new Graph[] { memGraph() };
			}

		};
	}

	@Contract.Inject
	public final IProducer<CollectionGraph> getCollectionTestProducer() {
		return graphProducer;
	}

}
