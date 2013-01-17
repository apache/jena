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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.Random;

import org.junit.Assert;

public class IsomorphicTests extends AbstractModelTestBase
{
    // This is not part of the standard test suite 
    // It's not stable enough for inclusion in the automatic test suite.
    // Often, they pass, but there is a significant number of times they don't.
    // It also seems to be machine-dependent - failures are more frequent
    // on Apache Jenkins (hardware influening "random" numbers?)
    
	/**
	 * A theoretical graph for testing purposes.
	 * All nodes are anonymous resources. All edges are labelled
	 * rdf:value.
	 * The basic DiHyperCube consists of the nodes being the
	 * corners of a hypercube (e.g. in 3D a cube)
	 * with the statements being the edges of the cube, directed
	 * from one corner labelled 2^n-1 to the opposite corner
	 * labelled 0. The labels are not present in the model.
	 * This basic graph is then extended, for test purposes
	 * by duplicating a node.
	 */
	static class DiHyperCube extends java.lang.Object
	{
		static int bitCount( final int i )
		{
			return java.math.BigInteger.valueOf(i).bitCount();
		}

		/*
		 * We have two DiHyperCube's
		 * to one we have added N a1's
		 * to the other we have added N b1's
		 * Returns true if they are equal.
		 */
		static boolean equal( final int a1, final int b1 )
		{
			return DiHyperCube.bitCount(a1) == DiHyperCube.bitCount(b1);
		}

		/*
		 * We have two DiHyperCube's
		 * to one we have added N a1's and N a2's.
		 * to the other we have added N b1's and N b2's.
		 * Returns true if they are equal.
		 */
		static boolean equal( final int a1, final int a2, final int b1,
				final int b2 )
		{
			return (DiHyperCube.bitCount(a1 ^ a2) == DiHyperCube.bitCount(b1
					^ b2))
					&& (DiHyperCube.bitCount(a1 & a2) == DiHyperCube
							.bitCount(b1 & b2))
					&& (DiHyperCube.bitCount(a1 | a2) == DiHyperCube
							.bitCount(b1 | b2))
					&& (Math.min(DiHyperCube.bitCount(a1),
							DiHyperCube.bitCount(a2)) == Math.min(
							DiHyperCube.bitCount(b1), DiHyperCube.bitCount(b2)));
		}

		final private Resource corners[];

		final private int dim;

		final private Model model;

		/** Creates new DiHyperCube */
		public DiHyperCube( final int dimension, final Model m )
		{
			dim = dimension;
			model = m;
			corners = new Resource[1 << dim];
			for (int i = 0; i < corners.length; i++)
			{
				corners[i] = m.createResource();
			}
			for (int i = 0; i < corners.length; i++)
			{
				addDown(i, corners[i]);
			}
		}

		private void addDown( final int corner, final Resource r )
		{
			for (int j = 0; j < dim; j++)
			{
				final int bit = 1 << j;
				if ((corner & bit) != 0)
				{
					model.add(r, RDF.value, corners[corner ^ bit]);
				}
			}
		}

		DiHyperCube dupe( final int corner )
		{
			final Resource dup = model.createResource();
			for (int j = 0; j < dim; j++)
			{
				final int bit = 1 << j;
				if ((corner & bit) != 0)
				{
					model.add(dup, RDF.value, corners[corner ^ bit]);
				}
				else
				{
					model.add(corners[corner ^ bit], RDF.value, dup);
				}
			}
			return this;
		}

	}

	/**
	 * A theoretical graph for testing purposes.
	 * All nodes are anonymous resources. All edges are labelled
	 * rdf:value.
	 * The basic HyperCube consists of the nodes being the
	 * corners of a hypercube (e.g. in 3D a cube)
	 * with the statements being the edges of the cube, in both
	 * directions. The labels are not present in the model.
	 * This basic graph is then extended, for test purposes
	 * by duplicating a node. Or by adding/deleting an edge between
	 * two nodes.
	 */
	static class HyperCube extends java.lang.Object
	{
		static int bitCount( final int i )
		{
			return java.math.BigInteger.valueOf(i).bitCount();
		}

		/*
		 * We have two HyperCube's
		 * to one we have added N a1's and M a2's.
		 * to the other we have added N b1's and M b2's.
		 * or we have toggled an edge between a1 and a2, and
		 * between b1 and b2.
		 * Returns true if they are equal.
		 */
		static boolean equal( final int a1, final int a2, final int b1,
				final int b2 )
		{
			return HyperCube.bitCount(a1 ^ a2) == HyperCube.bitCount(b1 ^ b2);
		}

		final private Resource corners[];

		final private int dim;

		final private Model model;

		/** Creates new DiHyperCube */
		public HyperCube( final int dimension, final Model m )
		{
			dim = dimension;
			model = m;
			corners = new Resource[1 << dim];
			for (int i = 0; i < corners.length; i++)
			{
				corners[i] = m.createResource();
			}
			for (int i = 0; i < corners.length; i++)
			{
				add(i, corners[i]);
			}
		}

		private void add( final int corner, final Resource r )
		{
			for (int j = 0; j < dim; j++)
			{
				final int bit = 1 << j;
				model.add(r, RDF.value, corners[corner ^ bit]);
			}
		}

		HyperCube dupe( final int corner )
		{
			final Resource dup = model.createResource();
			add(corner, dup);
			return this;
		}

		HyperCube toggle( final int from, final int to )
		{
			final Resource f = corners[from];
			final Resource t = corners[to];
			final Statement s = model.createStatement(f, RDF.value, t);
			if (model.contains(s))
			{
				model.remove(s);
			}
			else
			{
				model.add(s);
			}
			return this;
		}

	}

	private static int QUANTITY = 10;
	private static int DIMENSION = 6;
	private final int sz = 1 << IsomorphicTests.DIMENSION;

	private Random random;

	private Model model2;

	public IsomorphicTests( final TestingModelFactory modelFactory, final String name )
	{
		super(modelFactory, name);
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		random = new Random();
		model2 = createModel();
	}

	@Override
	public void tearDown() throws Exception
	{
		model.close();
		super.tearDown();
	}

	private void test2DiHyperCube( int quantity, final boolean type )
	{
		if (IsomorphicTests.QUANTITY < 6)
		{
		    // (Guessing) If the number is too small, the probability
		    // of passing the test is too small.
			return;
		}
		for (int i = 0; i < quantity; i++)
		{
			int a1, b1;
			do
			{
				a1 = random.nextInt(sz);
				b1 = random.nextInt(sz);
			} while (type != DiHyperCube.equal(a1, b1));

			new DiHyperCube(IsomorphicTests.DIMENSION, model).dupe(a1).dupe(a1)
					.dupe(a1);

			new DiHyperCube(IsomorphicTests.DIMENSION, model2).dupe(b1).dupe(b1)
					.dupe(b1);

			Assert.assertEquals(type, model.isIsomorphicWith(model2));
		}
	}

	public void test2DiHyperCubeFalse()
	{
		test2DiHyperCube(IsomorphicTests.QUANTITY, false);
	}

	public void test2DiHyperCubeTrue()
	{
		test2DiHyperCube(IsomorphicTests.QUANTITY, true);
	}

	public void test2HyperCube()
	{

		for (int i = 0; i < IsomorphicTests.QUANTITY; i++)
		{
			int a1, b1;
			a1 = random.nextInt(sz);
			b1 = random.nextInt(sz);
			new HyperCube(IsomorphicTests.DIMENSION, model).dupe(a1).dupe(a1)
					.dupe(a1);
			new HyperCube(IsomorphicTests.DIMENSION, model2).dupe(b1).dupe(b1)
					.dupe(b1);
			Assert.assertTrue("Models not isomorphic",
					model.isIsomorphicWith(model2));
		}
	}

	private void test4DiHyperCube( int quantity, final boolean type )
	{

		for (int i = 0; i < quantity; i++)
		{
			int a1, b1, a2, b2;
			do
			{
				a1 = random.nextInt(sz);
				b1 = random.nextInt(sz);
				a2 = random.nextInt(sz);
				b2 = random.nextInt(sz);
			} while (type != DiHyperCube.equal(a1, a2, b1, b2));

			new DiHyperCube(IsomorphicTests.DIMENSION, model).dupe(a1).dupe(a1)
					.dupe(a1).dupe(a2).dupe(a2).dupe(a2);

			new DiHyperCube(IsomorphicTests.DIMENSION, model2).dupe(b1).dupe(b1)
					.dupe(b1).dupe(b2).dupe(b2).dupe(b2);
			final String msg = "(" + a1 + "," + a2 + "),(" + b1 + "," + b2
					+ ")";
			Assert.assertEquals(msg, type, model.isIsomorphicWith(model2));
		}

	}

	public void test4DiHyperCubeFalse()
	{
		test4DiHyperCube(IsomorphicTests.QUANTITY, false);
	}

	public void test4DiHyperCubeTrue()
	{
		test4DiHyperCube(IsomorphicTests.QUANTITY, true);
	}

	private void test4HyperCube( int quantity, final boolean type )
	{

		for (int i = 0; i < quantity; i++)
		{
			int a1, b1, a2, b2;
			do
			{
				a1 = random.nextInt(sz);
				b1 = random.nextInt(sz);
				a2 = random.nextInt(sz);
				b2 = random.nextInt(sz);
			} while (type != HyperCube.equal(a1, a2, b1, b2));

			new HyperCube(IsomorphicTests.DIMENSION, model).dupe(a1).dupe(a1)
					.dupe(a1).dupe(a2).dupe(a2).dupe(a2);
			new HyperCube(IsomorphicTests.DIMENSION, model2).dupe(b1).dupe(b1)
					.dupe(b1).dupe(b2).dupe(b2).dupe(b2);

			final String msg = "(" + a1 + "," + a2 + "),(" + b1 + "," + b2
					+ ")";
			Assert.assertEquals(msg, type, model.isIsomorphicWith(model2));
		}
	}

	public void test4HyperCubeFalse()
	{
	    // Pragmatically, needs more loops
		test4HyperCube(2 * IsomorphicTests.QUANTITY, false);
	}

	public void test4HyperCubeTrue()
	{
		test4HyperCube(IsomorphicTests.QUANTITY, true);
	}

	private void test4ToggleHyperCube( int quantity, final boolean type )
	{

		for (int i = 0; i < quantity; i++)
		{
			int a1, b1, a2, b2;
			do
			{
				a1 = random.nextInt(sz);
				b1 = random.nextInt(sz);
				a2 = random.nextInt(sz);
				b2 = random.nextInt(sz);
			} while (type != HyperCube.equal(a1, a2, b1, b2));
			new HyperCube(IsomorphicTests.DIMENSION, model).toggle(a1, a2);

			new HyperCube(IsomorphicTests.DIMENSION, model2).toggle(b1, b2);

			final String msg = "(" + a1 + "," + a2 + "),(" + b1 + "," + b2
					+ ")";
			Assert.assertEquals(msg, type, model.isIsomorphicWith(model2));
		}
	}

	public void test4ToggleHyperCubeFalse()
	{
		test4ToggleHyperCube(2*IsomorphicTests.QUANTITY, false);
	}

	public void test4ToggleHyperCubeTrue()
	{
		test4ToggleHyperCube(IsomorphicTests.QUANTITY, true);
	}

}
