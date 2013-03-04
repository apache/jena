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

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphExtract;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TripleBoundary;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelExtract;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StatementBoundary;
import com.hp.hpl.jena.rdf.model.StatementBoundaryBase;
import com.hp.hpl.jena.rdf.model.StatementTripleBoundary;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;

import org.junit.Assert;

public class TestModelExtract extends AbstractModelTestBase
{
	static class MockModelExtract extends ModelExtract
	{
		Node root;
		Graph result;
		Graph subject;

		public MockModelExtract( final StatementBoundary b )
		{
			super(b);
		}

		@Override
		protected GraphExtract getGraphExtract( final TripleBoundary b )
		{
			return new GraphExtract(b) {
				@Override
				public Graph extractInto( final Graph toUpdate, final Node n,
						final Graph source )
				{
					root = n;
					return result = super.extractInto(toUpdate, n,
							subject = source);
				}
			};
		}

		public StatementBoundary getStatementBoundary()
		{
			return boundary;
		}
	}

	protected static final StatementBoundary sbTrue = new StatementBoundaryBase() {
		@Override
		public boolean stopAt( final Statement s )
		{
			return true;
		}
	};

	protected static final StatementBoundary sbFalse = new StatementBoundaryBase() {
		@Override
		public boolean stopAt( final Statement s )
		{
			return false;
		}
	};

	public TestModelExtract( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hp.hpl.jena.rdf.model.StatementBoundary#asTripleBoundary(com.hp.hpl
	 * .jena.rdf.model.Model)
	 */
	public TripleBoundary asTripleBoundary( final Model m )
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hp.hpl.jena.rdf.model.StatementBoundary#stopAt(com.hp.hpl.jena.rdf
	 * .model.Statement)
	 */
	public boolean stopAt( final Statement s )
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void testAsTripleBoundary()
	{
		final Model m = ModelFactory.createDefaultModel();
		Assert.assertTrue(TestModelExtract.sbTrue.asTripleBoundary(m).stopAt(
				GraphTestBase.triple("x R y")));
		Assert.assertFalse(TestModelExtract.sbFalse.asTripleBoundary(m).stopAt(
				GraphTestBase.triple("x R y")));
	}

	public void testInvokesExtract()
	{
		final MockModelExtract mock = new MockModelExtract(
				TestModelExtract.sbTrue);
		final Model source = ModelHelper.modelWithStatements(this, "a R b");
		final Model m = mock.extract(ModelHelper.resource("a"), source);
		Assert.assertEquals(GraphTestBase.node("a"), mock.root);
		Assert.assertSame(mock.result, m.getGraph());
		Assert.assertSame(mock.subject, source.getGraph());
	}

	public void testRemembersBoundary()
	{
		Assert.assertSame(TestModelExtract.sbTrue, new MockModelExtract(
				TestModelExtract.sbTrue).getStatementBoundary());
		Assert.assertSame(TestModelExtract.sbFalse, new MockModelExtract(
				TestModelExtract.sbFalse).getStatementBoundary());
	}

	public void testStatementContinueWith()
	{
		final StatementBoundary sb = new StatementBoundaryBase() {
			@Override
			public boolean continueWith( final Statement s )
			{
				return false;
			}
		};
		Assert.assertTrue(sb.stopAt(ModelHelper.statement("x pings y")));
	}

	public void testStatementTripleBoundaryAnon()
	{
		final TripleBoundary anon = TripleBoundary.stopAtAnonObject;
		Assert.assertSame(anon,
				new StatementTripleBoundary(anon).asTripleBoundary(null));
		Assert.assertFalse(new StatementTripleBoundary(anon).stopAt(ModelHelper
				.statement("s P o")));
		Assert.assertTrue(new StatementTripleBoundary(anon).stopAt(ModelHelper
				.statement("s P _o")));
	}

	public void testStatementTripleBoundaryNowhere()
	{
		final TripleBoundary nowhere = TripleBoundary.stopNowhere;
		Assert.assertSame(nowhere,
				new StatementTripleBoundary(nowhere).asTripleBoundary(null));
		Assert.assertFalse(new StatementTripleBoundary(nowhere)
				.stopAt(ModelHelper.statement("s P _o")));
		Assert.assertFalse(new StatementTripleBoundary(nowhere)
				.stopAt(ModelHelper.statement("s P o")));
	}
}
