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
package org.apache.jena.permissions.graph;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.Factory;
import org.apache.jena.permissions.StaticSecurityEvaluator;
import org.apache.jena.permissions.graph.SecuredGraph;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Assert;
import org.junit.Test;

public class CrossIDGraphEventManagerTest {

	private final GraphEventManager manager;
	private final Graph g;
	private final SecuredGraph sg;
	private final StaticSecurityEvaluator securityEvaluator;

	private final RecordingGraphListener annListener;
	private final RecordingGraphListener bobListener;

	public CrossIDGraphEventManagerTest() {
		this.securityEvaluator = new StaticSecurityEvaluator("ann");

		g = GraphFactory.createDefaultGraph();
		g.add(new Triple(NodeFactory.createURI("urn:ann"), NodeFactory
				.createURI("http://example.com/v"), NodeFactory.createBlankNode()));
		g.add(new Triple(NodeFactory.createURI("urn:bob"), NodeFactory
				.createURI("http://example.com/v"), NodeFactory.createBlankNode()));
		g.add(new Triple(NodeFactory.createURI("urn:ann"), NodeFactory
				.createURI("http://example.com/v2"), NodeFactory.createBlankNode()));

		sg = Factory.getInstance(securityEvaluator,
				"http://example.com/testGraph", g);
		manager = sg.getEventManager();
		annListener = new RecordingGraphListener();
		manager.register(annListener);
		this.securityEvaluator.setUser("bob");
		bobListener = new RecordingGraphListener();
		manager.register(bobListener);
	}

	@Test
	public void notificationsTest() {
		sg.add(new Triple(NodeFactory.createURI("urn:bob"), NodeFactory
				.createURI("http://example.com/v2"), NodeFactory.createBlankNode()));

		Assert.assertTrue("Should recorded add", bobListener.isAdd());
		Assert.assertFalse("Should not have recorded add", annListener.isAdd());

		sg.delete(new Triple(NodeFactory.createURI("urn:bob"), NodeFactory
				.createURI("http://example.com/v2"), NodeFactory.createBlankNode()));

		Assert.assertTrue("Should recorded delete", bobListener.isDelete());
		Assert.assertFalse("Should not have recorded delete",
				annListener.isDelete());
	}

}
