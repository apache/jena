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

package com.hp.hpl.jena.rdf.model.test;

import java.util.ArrayList ;
import java.util.List ;

import junit.framework.TestCase ;
import org.junit.Assert ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.GraphMaker ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.test.GraphTestBase ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelMaker ;
import com.hp.hpl.jena.rdf.model.impl.ModelMakerImpl ;
import com.hp.hpl.jena.test.JenaTestBase ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.NullIterator ;

/**
 * Test ModelMakerImpl using a mock GraphMaker.
 */
public class TestModelMakerImpl extends TestCase
{
	static class MockGraphMaker implements GraphMaker
	{
		List<String> history = new ArrayList<>();
		Graph graph;

		public MockGraphMaker( final Graph graph )
		{
			this.graph = graph;
		}

		public Graph addDescription( final Graph desc, final Node self )
		{
			history.add("addDescription()");
			return desc;
		}

		@Override
		public void close()
		{
			history.add("close()");
		}

		@Override
		public Graph createGraph()
		{
			history.add("create()");
			return graph;
		}

		@Override
		public Graph createGraph( final String name )
		{
			history.add("create(" + name + ")");
			return graph;
		}

		@Override
		public Graph createGraph( final String name, final boolean strict )
		{
			history.add("create(" + name + "," + strict + ")");
			return graph;
		}

		public Graph getDescription()
		{
			history.add("getDescription()");
			return GraphTestBase.graphWith("");
		}

		public Graph getDescription( final Node root )
		{
			history.add("getDescription(Node)");
			return GraphTestBase.graphWith("");
		}

		@Override
		public Graph getGraph()
		{
			history.add("get()");
			return graph;
		}

		@Override
		public boolean hasGraph( final String name )
		{
			history.add("has(" + name + ")");
			return false;
		}

		@Override
		public ExtendedIterator<String> listGraphs()
		{
			history.add("listModels()");
			return NullIterator.instance();
		}

		@Override
		public Graph openGraph()
		{

			return null;
		}

		@Override
		public Graph openGraph( final String name )
		{
			history.add("open(" + name + ")");
			return graph;
		}

		@Override
		public Graph openGraph( final String name, final boolean strict )
		{
			history.add("open(" + name + "," + strict + ")");
			return graph;
		}

		@Override
		public void removeGraph( final String name )
		{
			history.add("remove(" + name + ")");
		}
	}

	private ModelMaker maker;
	private Graph graph;
	private GraphMaker graphMaker;

	public TestModelMakerImpl( final String name )
	{
		super(name);
	}

	private void checkHistory( final List<String> expected )
	{
		Assert.assertEquals(expected, history());
	}

	private List<String> history()
	{
		return ((MockGraphMaker) maker.getGraphMaker()).history;
	}

	@Override
	public void setUp()
	{
		graph = GraphTestBase.graphWith("");
		graphMaker = new MockGraphMaker(graph);
		maker = new ModelMakerImpl(graphMaker);
	}

	public void testClose()
	{
		maker.close();
		checkHistory(JenaTestBase.listOfOne("close()"));
	}

	public void testCreateDefaultModel()
	{
		maker.createDefaultModel();
		checkHistory(JenaTestBase.listOfOne("get()"));
	}

	public void testCreateFalse()
	{
		final Model m = maker.createModel("leaf", false);
		checkHistory(JenaTestBase.listOfOne("create(leaf,false)"));
		Assert.assertTrue(m.getGraph() == graph);
	}

	public void testCreateFreshModel()
	{
		maker.createFreshModel();
		checkHistory(JenaTestBase.listOfOne("create()"));
	}

	public void testCreateNamed()
	{
		final Model m = maker.createModel("petal");
		checkHistory(JenaTestBase.listOfOne("create(petal,false)"));
		Assert.assertTrue(m.getGraph() == graph);
	}

	public void testCreateTrue()
	{
		final Model m = maker.createModel("stem", true);
		checkHistory(JenaTestBase.listOfOne("create(stem,true)"));
		Assert.assertTrue(m.getGraph() == graph);
	}

	public void testGetGraphMaker()
	{
		Assert.assertTrue(maker.getGraphMaker() == graphMaker);
	}

	public void testListGraphs()
	{
		maker.listModels().close();
		checkHistory(JenaTestBase.listOfOne("listModels()"));
	}

	public void testOpen()
	{
		final Model m = maker.openModel("trunk");
		checkHistory(JenaTestBase.listOfOne("open(trunk,false)"));
		Assert.assertTrue(m.getGraph() == graph);
	}

	public void testOpenFalse()
	{
		final Model m = maker.openModel("branch", false);
		checkHistory(JenaTestBase.listOfOne("open(branch,false)"));
		Assert.assertTrue(m.getGraph() == graph);
	}

	public void testOpenTrue()
	{
		final Model m = maker.openModel("bark", true);
		checkHistory(JenaTestBase.listOfOne("open(bark,true)"));
		Assert.assertTrue(m.getGraph() == graph);
	}

	public void testRemove()
	{
		maker.removeModel("London");
		checkHistory(JenaTestBase.listOfOne("remove(London)"));
	}
}
