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

package org.apache.jena.mem;

import org.junit.runner.RunWith;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractImpl;
import org.xenei.junit.contract.ContractSuite;

import org.apache.jena.graph.Graph;
import org.xenei.junit.contract.IProducer;

@RunWith(ContractSuite.class)
@ContractImpl(GraphTripleStoreMem.class)
public class GraphTripleStoreMem_CS {

	private IProducer<GraphTripleStoreMem> producer = new IProducer<GraphTripleStoreMem>() {

		@Override
		public GraphTripleStoreMem newInstance() {
			return new GraphTripleStoreMem(Graph.emptyGraph);
		}

		@Override
		public void cleanUp() {
		}

	};

	@Contract.Inject
	public IProducer<GraphTripleStoreMem> getTripleStore() {
		return producer;
	}
}
