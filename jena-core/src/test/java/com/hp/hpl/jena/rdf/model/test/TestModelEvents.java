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

import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.rdf.listeners.ChangedListener;
import com.hp.hpl.jena.rdf.listeners.NullListener;
import com.hp.hpl.jena.rdf.listeners.ObjectListener;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.StmtIteratorImpl;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.RecordingModelListener;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

/**
 * Tests for model events and listeners.
 */
public class TestModelEvents extends AbstractModelTestBase
{
	static class OL extends ObjectListener
	{
		private Object recorded;
		private String how;

		@Override
		public void added( final Object x )
		{
			recorded = x;
			how = "add";
		}

		private Object comparable( final Object x )
		{
			if (x instanceof Statement[])
			{
				return Arrays.asList((Statement[]) x);
			}
			if (x instanceof Iterator<?>)
			{
				return GraphTestBase.iteratorToList((Iterator<?>) x);
			}
			return x;
		}

		public void recent( final String wantHow, final Object value )
		{
			Assert.assertTrue(RecordingModelListener.checkEquality(
					comparable(value), comparable(recorded)));
			// Assert.assertEquals(comparable(value), comparable(recorded));
			Assert.assertEquals(wantHow, how);
			recorded = how = null;
		}

		@Override
		public void removed( final Object x )
		{
			recorded = x;
			how = "rem";
		}
	}

	/**
	 * Local test class to see that a StatementListener funnels all the changes
	 * through
	 * add/remove a single ModelHelper.statement
	 */
	public static class WatchStatementListener extends StatementListener
	{
		List<Statement> statements = new ArrayList<>();
		String addOrRem = "<unset>";

		@Override
		public void addedStatement( final Statement s )
		{
			statements.add(s);
			addOrRem = "add";
		}

		public List<Statement> contents()
		{
			try
			{
				return statements;
			}
			finally
			{
				statements = new ArrayList<>();
			}
		}

		public String getAddOrRem()
		{
			return addOrRem;
		}

		@Override
		public void removedStatement( final Statement s )
		{
			statements.add(s);
			addOrRem = "rem";
		}
	}

	protected RecordingModelListener SL;

	public TestModelEvents( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	public void another( final Map<Object, Integer> m, final Object x )
	{
		Integer n = m.get(x);
		if (n == null)
		{
			n = new Integer(0);
		}
		m.put(x, new Integer(n.intValue() + 1));
	}

	public Map<Object, Integer> asBag( final List<Statement> l )
	{
		final Map<Object, Integer> result = new HashMap<>();
        for ( Statement aL : l )
        {
            another( result, aL.asTriple() );
        }
		return result;
	}

	protected StmtIterator asIterator( final Statement[] statements )
	{
		return new StmtIteratorImpl(Arrays.asList(statements).iterator());
	}

	public void assertSameBag( final List<Statement> wanted,
			final List<Statement> got )
	{

		Assert.assertEquals(asBag(wanted), asBag(got));
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		SL = new RecordingModelListener();
	}

	public void testAddInPieces()
	{
		model.register(SL);
		model.add(ModelHelper.resource(model, "S"),
				ModelHelper.property(model, "P"),
				ModelHelper.resource(model, "O"));
		SL.assertHas(new Object[] { "add",
				ModelHelper.statement(model, "S P O") });
	}

	public void testAddModel()
	{
		model.register(SL);
		final Model m = ModelHelper.modelWithStatements(this,
				"NT beats S; S beats H; H beats D");
		model.add(m);
		SL.assertHas(new Object[] { "addModel", m });
	}

	public void testAddSingleStatements()
	{
		final Statement S1 = ModelHelper.statement(model, "S P O");
		final Statement S2 = ModelHelper.statement(model, "A B C");
		Assert.assertFalse(SL.has(new Object[] { "add", S1 }));
		model.register(SL);
		model.add(S1);
		SL.assertHas(new Object[] { "add", S1 });
		model.add(S2);
		SL.assertHas(new Object[] { "add", S1, "add", S2 });
		model.add(S1);
		SL.assertHas(new Object[] { "add", S1, "add", S2, "add", S1 });
	}

	public void testAddStatementArray()
	{
		model.register(SL);
		final Statement[] s = ModelHelper.statements(model, "a P b; c Q d");
		model.add(s);
		SL.assertHas(new Object[] { "add[]", Arrays.asList(s) });
	}

	public void testAddStatementIterator()
	{
		model.register(SL);
		final Statement[] sa = ModelHelper.statements(model,
				"x R y; a P b; x R y");
		final StmtIterator it = asIterator(sa);
		model.add(it);
		SL.assertHas(new Object[] { "addIterator", Arrays.asList(sa) });
	}

	public void testAddStatementList()
	{
		model.register(SL);
		final List<Statement> L = Arrays.asList(ModelHelper.statements(model,
				"b I g; model U g"));
		model.add(L);
		SL.assertHas(new Object[] { "addList", L });
	}

	public void testChangedListener()
	{
		final ChangedListener CL = new ChangedListener();
		model.register(CL);
		Assert.assertFalse(CL.hasChanged());
		model.add(ModelHelper.statement(model, "S P O"));
		Assert.assertTrue(CL.hasChanged());
		Assert.assertFalse(CL.hasChanged());
		model.remove(ModelHelper.statement(model, "ab CD ef"));
		Assert.assertTrue(CL.hasChanged());
		model.add(ModelHelper.statements(model, "gh IJ kl"));
		Assert.assertTrue(CL.hasChanged());
		model.remove(ModelHelper.statements(model, "mn OP qr"));
		Assert.assertTrue(CL.hasChanged());
		model.add(asIterator(ModelHelper.statements(model, "st UV wx")));
		Assert.assertTrue(CL.hasChanged());
		Assert.assertFalse(CL.hasChanged());
		model.remove(asIterator(ModelHelper.statements(model, "yz AB cd")));
		Assert.assertTrue(CL.hasChanged());
		model.add(ModelHelper.modelWithStatements(this, "ef GH ij"));
		Assert.assertTrue(CL.hasChanged());
		model.remove(ModelHelper.modelWithStatements(this, "kl MN op"));
		Assert.assertTrue(CL.hasChanged());
		model.add(Arrays.asList(ModelHelper.statements(model, "rs TU vw")));
		Assert.assertTrue(CL.hasChanged());
		model.remove(Arrays.asList(ModelHelper.statements(model, "xy wh q")));
		Assert.assertTrue(CL.hasChanged());
	}

	public void testDeleteModel()
	{
		model.register(SL);
		final Model m = ModelHelper.modelWithStatements(this,
				"NT beats S; S beats H; H beats D");
		model.remove(m);
		SL.assertHas(new Object[] { "removeModel", m });
	}

	public void testDeleteStatementArray()
	{
		model.register(SL);
		final Statement[] s = ModelHelper.statements(model, "a P b; c Q d");
		model.remove(s);
		SL.assertHas(new Object[] { "remove[]", Arrays.asList(s) });
	}

	public void testDeleteStatementIterator()
	{
		model.register(SL);
		final Statement[] sa = ModelHelper.statements(model,
				"x R y; a P b; x R y");
		final StmtIterator it = asIterator(sa);
		model.remove(it);
		SL.assertHas(new Object[] { "removeIterator", Arrays.asList(sa) });
	}

	public void testDeleteStatementList()
	{
		model.register(SL);
		final List<Statement> lst = Arrays.asList(ModelHelper.statements(model,
				"b I g; model U g"));
		model.remove(lst);
		SL.assertHas(new Object[] { "removeList", lst });
	}

	public void testGeneralEvent()
	{
		model.register(SL);
		final Object e = new int[] {};
		model.notifyEvent(e);
		SL.assertHas(new Object[] { "someEvent", model, e });
	}

	public void testGot( final WatchStatementListener sl, final String how,
			final String template )
	{
		assertSameBag(Arrays.asList(ModelHelper.statements(model, template)),
				sl.contents());
		Assert.assertEquals(how, sl.getAddOrRem());
		Assert.assertTrue(sl.contents().size() == 0);
	}

	/**
	 * Test that the null listener doesn't appear to do anything. Or at least
	 * doesn't crash ....
	 */
	public void testNullListener()
	{
		final ModelChangedListener NL = new NullListener();
		model.register(NL);
		model.add(ModelHelper.statement(model, "S P O "));
		model.remove(ModelHelper.statement(model, "X Y Z"));
		model.add(ModelHelper.statements(model, "a B c; d E f"));
		model.remove(ModelHelper.statements(model, "g H i; j K l"));
		model.add(asIterator(ModelHelper.statements(model, "model N o; p Q r")));
		model.remove(asIterator(ModelHelper.statements(model, "s T u; v W x")));
		model.add(ModelHelper.modelWithStatements(this, "leaves fall softly"));
		model.remove(ModelHelper.modelWithStatements(this,
				"water drips endlessly"));
		model.add(Arrays.asList(ModelHelper.statements(model, "xx RR yy")));
		model.remove(Arrays.asList(ModelHelper.statements(model, "aa VV rr")));
	}

	public void testObjectListener()
	{
		final OL ll = new OL();
		model.register(ll);
		final Statement s = ModelHelper.statement(model, "aa BB cc"), s2 = ModelHelper
				.statement(model, "dd EE ff");
		model.add(s);
		ll.recent("add", s);
		model.remove(s2);
		ll.recent("rem", s2);
		/* */
		final List<Statement> sList = Arrays.asList(ModelHelper.statements(
				model, "gg HH ii; jj KK ll"));
		model.add(sList);
		ll.recent("add", sList);
		final List<Statement> sList2 = Arrays.asList(ModelHelper.statements(
				model, "mm NN oo; pp QQ rr; ss TT uu"));
		model.remove(sList2);
		ll.recent("rem", sList2);
		/* */
		final Model m1 = ModelHelper.modelWithStatements(this,
				"vv WW xx; yy ZZ aa");
		model.add(m1);
		ll.recent("add", m1);
		final Model m2 = ModelHelper.modelWithStatements(this, "a B g; d E z");
		model.remove(m2);
		ll.recent("rem", m2);
		/* */
		final Statement[] sa1 = ModelHelper.statements(model,
				"th i k; l model n");
		model.add(sa1);
		ll.recent("add", sa1);
		final Statement[] sa2 = ModelHelper.statements(model, "x o p; r u ch");
		model.remove(sa2);
		ll.recent("rem", sa2);
		/* */
		final Statement[] si1 = ModelHelper.statements(model,
				"u ph ch; psi om eh");
		model.add(asIterator(si1));
		ll.recent("add", asIterator(si1));
		final Statement[] si2 = ModelHelper.statements(model,
				"at last the; end of these; tests ok guv");
		model.remove(asIterator(si2));
		ll.recent("rem", asIterator(si2));
	}

	public void testRegistrationCompiles()
	{
		Assert.assertSame(model, model.register(new RecordingModelListener()));
	}

	public void testRemoveSingleStatements()
	{
		final Statement S = ModelHelper.statement(model, "D E F");
		model.register(SL);
		model.add(S);
		model.remove(S);
		SL.assertHas(new Object[] { "add", S, "remove", S });
	}

	public void testTripleListener()
	{
		final WatchStatementListener sl = new WatchStatementListener();
		model.register(sl);
		model.add(ModelHelper.statement(model, "b C d"));
		testGot(sl, "add", "b C d");
		model.remove(ModelHelper.statement(model, "e F g"));
		testGot(sl, "rem", "e F g");
		/* */
		model.add(ModelHelper.statements(model, "h I j; k L model"));
		testGot(sl, "add", "h I j; k L model");
		model.remove(ModelHelper.statements(model, "n O p; q R s"));
		testGot(sl, "rem", "n O p; q R s");
		/* */
		model.add(Arrays.asList(ModelHelper.statements(model, "t U v; w X y")));
		testGot(sl, "add", "t U v; w X y");
		model.remove(Arrays.asList(ModelHelper
				.statements(model, "z A b; c D e")));
		testGot(sl, "rem", "z A b; c D e");
		/* */
		model.add(asIterator(ModelHelper.statements(model, "f G h; i J k")));
		testGot(sl, "add", "f G h; i J k");
		model.remove(asIterator(ModelHelper.statements(model, "l M n; o P q")));
		testGot(sl, "rem", "l M n; o P q");
		/* */
		model.add(ModelHelper.modelWithStatements(this, "r S t; u V w; x Y z"));
		testGot(sl, "add", "r S t; u V w; x Y z");
		model.remove(ModelHelper.modelWithStatements(this, "a E i; o U y"));
		testGot(sl, "rem", "a E i; o U y");
	}

	public void testTwoListeners()
	{
		final Statement S = ModelHelper.statement(model, "S P O");
		final RecordingModelListener SL1 = new RecordingModelListener();
		final RecordingModelListener SL2 = new RecordingModelListener();
		model.register(SL1).register(SL2);
		model.add(S);
		SL2.assertHas(new Object[] { "add", S });
		SL1.assertHas(new Object[] { "add", S });
	}

	public void testUnregisterWorks()
	{
		model.register(SL);
		model.unregister(SL);
		model.add(ModelHelper.statement(model, "X R Y"));
		SL.assertHas(new Object[] {});
	}

	public void testUnregistrationCompiles()
	{
		model.unregister(new RecordingModelListener());
	}
}
