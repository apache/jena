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

package org.apache.jena.graph;

import static org.apache.jena.testing_framework.GraphHelper.graphWith;
import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.apache.jena.testing_framework.GraphHelper.txnBegin;
import static org.apache.jena.testing_framework.GraphHelper.txnCommit;
import static org.apache.jena.testing_framework.GraphHelper.txnRun;
import static org.junit.Assert.*;

import org.junit.After;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractTest;

import org.apache.jena.graph.impl.GraphWithPerform;

import org.xenei.junit.contract.IProducer;

/**
 * GraphWithPerform is an implementation interface that extends Graph with the
 * performAdd and performDelete methods used by GraphBase to invoke
 * non-notifying versions of add and delete.
 */
@Contract(GraphWithPerform.class)
public class GraphWithPerformContractTest<T extends GraphWithPerform>
{

	private IProducer<T> producer;

	// Recording listener for tests
	protected RecordingGraphListener GL = new RecordingGraphListener();

	public GraphWithPerformContractTest()
	{
	}

	@Contract.Inject
	public void setGraphWithPerformContractTestProducer(IProducer<T> producer)
	{
		this.producer = producer;
	}

	@After
	public final void afterGraphWithPerformContractTest()
	{
		producer.cleanUp();
	}

	@ContractTest
	public void testPerformAdd_Triple()
	{
		GraphWithPerform g = (GraphWithPerform) graphWith(
				producer.newInstance(), "S P O; S2 P2 O2");
		g.getEventManager().register(GL);
		txnBegin(g);
		g.performAdd(triple("S3 P3 O3"));
		txnCommit(g);
		GL.assertEmpty();
		txnRun(g, () -> assertTrue(g.contains(triple("S3 P3 O3"))));
	}

	@ContractTest
	public void testPerformDelete_Triple()
	{
		GraphWithPerform g = (GraphWithPerform) graphWith(
				producer.newInstance(), "S P O; S2 P2 O2");
		g.getEventManager().register(GL);
		txnBegin(g);
		g.performDelete(triple("S2 P2 O2"));
		txnCommit(g);
		GL.assertEmpty();
		txnRun(g, () -> assertFalse(g.contains(triple("S2 P2 O2"))));

	}

}
