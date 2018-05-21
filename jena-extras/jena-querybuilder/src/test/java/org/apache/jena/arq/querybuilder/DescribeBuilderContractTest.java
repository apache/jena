/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder;

import org.junit.runner.RunWith;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractImpl;
import org.xenei.junit.contract.ContractSuite;
import org.xenei.junit.contract.IProducer;

@RunWith(ContractSuite.class)
@ContractImpl(DescribeBuilder.class)
public class DescribeBuilderContractTest {

	// create the producer to inject
	private IProducer<DescribeBuilder> producer = new IProducer<DescribeBuilder>() {

		@Override
		public DescribeBuilder newInstance() {
			return new DescribeBuilder();
		}

		@Override
		public void cleanUp() {
			// no cleanup required
		}

	};

	public DescribeBuilderContractTest() {
	}

	@Contract.Inject
	public IProducer<DescribeBuilder> getProducer() {
		return producer;
	}

}
