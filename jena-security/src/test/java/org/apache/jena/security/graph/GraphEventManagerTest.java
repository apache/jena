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
package org.apache.jena.security.graph;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEventManager;
import com.hp.hpl.jena.graph.GraphListener;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.CollectionGraph;
import com.hp.hpl.jena.sparql.graph.GraphFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.security.Factory;
import org.apache.jena.security.MockSecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluatorParameters;
import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.graph.SecuredGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith( value = SecurityEvaluatorParameters.class )
public class GraphEventManagerTest
{
	private class RecordingGraphListener implements GraphListener
	{

		private boolean add;
		private boolean delete;
		private boolean event;

		public boolean isAdd()
		{
			return add;
		}

		public boolean isDelete()
		{
			return delete;
		}

		public boolean isEvent()
		{
			return event;
		}

		@Override
		public void notifyAddArray( final Graph g, final Triple[] triples )
		{
			add = true;
		}

		@Override
		public void notifyAddGraph( final Graph g, final Graph added )
		{
			add = true;
		}

		@Override
		public void notifyAddIterator( final Graph g, final Iterator<Triple> it )
		{
			add = true;
		}

		@Override
		public void notifyAddList( final Graph g, final List<Triple> triples )
		{
			add = true;
		}

		@Override
		public void notifyAddTriple( final Graph g, final Triple t )
		{
			add = true;
		}

		@Override
		public void notifyDeleteArray( final Graph g, final Triple[] triples )
		{
			delete = true;
		}

		@Override
		public void notifyDeleteGraph( final Graph g, final Graph removed )
		{
			delete = true;
		}

		@Override
		public void notifyDeleteIterator( final Graph g,
				final Iterator<Triple> it )
		{
			delete = true;
		}

		@Override
		public void notifyDeleteList( final Graph g, final List<Triple> L )
		{
			delete = true;
		}

		@Override
		public void notifyDeleteTriple( final Graph g, final Triple t )
		{
			delete = true;
		}

		@Override
		public void notifyEvent( final Graph source, final Object value )
		{
			event = true;
		}

		public void reset()
		{
			add = false;
			delete = false;
			event = false;
		}

	}

	private final GraphEventManager manager;
	private final Graph g;
	private final SecuredGraph sg;
	private final SecurityEvaluator securityEvaluator;
	private Triple[] tripleArray;

	private final RecordingGraphListener listener;

	public GraphEventManagerTest( final MockSecurityEvaluator securityEvaluator )
	{
		this.securityEvaluator = securityEvaluator;
		g = GraphFactory.createDefaultGraph();

		sg = Factory.getInstance(securityEvaluator,
				"http://example.com/testGraph", g);
		manager = sg.getEventManager();
		listener = new RecordingGraphListener();
		manager.register(listener);

	}

	@Test
	@SuppressWarnings("deprecation")
	public void notifyAddTest()
	{
		final Set<Action> ADD = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Create, Action.Read });
		g.add(tripleArray[0]);
		if (securityEvaluator.evaluateAny(ADD, sg.getModelNode()))
		{
			Assert.assertTrue("Should recorded add", listener.isAdd());
		}
		else
		{
			Assert.assertFalse("Should not have recorded add", listener.isAdd());
		}
		g.delete(Triple.ANY);
		listener.reset();

		g.getBulkUpdateHandler().add(tripleArray);
		if (securityEvaluator.evaluateAny(ADD, sg.getModelNode()))
		{
			Assert.assertTrue("Should recorded add", listener.isAdd());
		}
		else
		{
			Assert.assertFalse("Should not have recorded add", listener.isAdd());
		}
		g.delete(Triple.ANY);
		listener.reset();

		g.getBulkUpdateHandler().add(Arrays.asList(tripleArray));
		if (securityEvaluator.evaluateAny(ADD, sg.getModelNode()))
		{
			Assert.assertTrue("Should recorded add", listener.isAdd());
		}
		else
		{
			Assert.assertFalse("Should not have recorded add", listener.isAdd());
		}
		g.delete(Triple.ANY);
		listener.reset();

		g.getBulkUpdateHandler().add(Arrays.asList(tripleArray).iterator());
		if (securityEvaluator.evaluateAny(ADD, sg.getModelNode()))
		{
			Assert.assertTrue("Should recorded add", listener.isAdd());
		}
		else
		{
			Assert.assertFalse("Should not have recorded add", listener.isAdd());
		}
		g.delete(Triple.ANY);
		listener.reset();

		g.getBulkUpdateHandler().add(
				new CollectionGraph(Arrays.asList(tripleArray)));
		if (securityEvaluator.evaluateAny(ADD, sg.getModelNode()))
		{
			Assert.assertTrue("Should recorded add", listener.isAdd());
		}
		else
		{
			Assert.assertFalse("Should not have recorded add", listener.isAdd());
		}
		g.delete(Triple.ANY);
		listener.reset();
	}

	@SuppressWarnings("deprecation")
    @Test
	public void notifyDeleteTest()
	{
		final Set<Action> DELETE = SecurityEvaluator.Util.asSet(new Action[] {
				Action.Delete, Action.Read });
		g.delete(tripleArray[0]);
		if (securityEvaluator.evaluateAny(DELETE, sg.getModelNode()))
		{
			Assert.assertTrue("Should have recorded delete",
					listener.isDelete());
		}
		else
		{
			Assert.assertFalse("Should not have recorded delete",
					listener.isDelete());
		}

		listener.reset();

		g.getBulkUpdateHandler().delete(tripleArray);
		if (securityEvaluator.evaluateAny(DELETE, sg.getModelNode()))
		{
			Assert.assertTrue("Should recorded delete", listener.isDelete());
		}
		else
		{
			Assert.assertFalse("Should not have recorded delete",
					listener.isDelete());
		}
		listener.reset();

		g.getBulkUpdateHandler().delete(Arrays.asList(tripleArray));
		if (securityEvaluator.evaluateAny(DELETE, sg.getModelNode()))
		{
			Assert.assertTrue("Should recorded delete", listener.isDelete());
		}
		else
		{
			Assert.assertFalse("Should not have recorded delete",
					listener.isDelete());
		}
		listener.reset();

		g.getBulkUpdateHandler().delete(Arrays.asList(tripleArray).iterator());
		if (securityEvaluator.evaluateAny(DELETE, sg.getModelNode()))
		{
			Assert.assertTrue("Should recorded delete", listener.isDelete());
		}
		else
		{
			Assert.assertFalse("Should not have recorded delete",
					listener.isDelete());
		}
		listener.reset();

		g.getBulkUpdateHandler().delete(
				new CollectionGraph(Arrays.asList(tripleArray)));
		if (securityEvaluator.evaluateAny(DELETE, sg.getModelNode()))
		{
			Assert.assertTrue("Should recorded delete", listener.isDelete());
		}
		else
		{
			Assert.assertFalse("Should not have recorded delete",
					listener.isDelete());
		}
		listener.reset();
	}

	@Test
	public void notifyEventTest()
	{
		g.getEventManager().notifyEvent(g, "Foo");
		Assert.assertTrue("Should recorded delete", listener.isEvent());
		listener.reset();
		// final RecordingGraphListener listener2 = new
		// RecordingGraphListener();
		// g.getEventManager().register(listener2);
		sg.getEventManager().notifyEvent(sg, "Foo");
		Assert.assertTrue("Should recorded delete", listener.isEvent());
		// Assert.assertTrue("Should recorded delete", listener2.isEvent());
		listener.reset();

	}

	@Before
	public void setup()
	{
		tripleArray = new Triple[] {
				new Triple(NodeFactory.createURI("http://example.com/1"),
						NodeFactory.createURI("http://example.com/v"),
						NodeFactory.createAnon()),
				new Triple(NodeFactory.createURI("http://example.com/2"),
						NodeFactory.createURI("http://example.com/v"),
						NodeFactory.createAnon()),
				new Triple(NodeFactory.createURI("http://example.com/3"),
						NodeFactory.createURI("http://example.com/v"),
						NodeFactory.createAnon()) };

	}
}
