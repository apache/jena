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

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.impl.JenaParameters;
import com.hp.hpl.jena.test.JenaTestBase;

import org.junit.Assert;
import junit.framework.TestSuite;

/**
 * Test for anonID generation. (Originally test for the debugging hack
 * that switches off anonID generation.)
 */
public class TestAnonID extends JenaTestBase
{

	/**
	 * Boilerplate for junit.
	 * This is its own test suite
	 */
	public static TestSuite suite()
	{
		return new TestSuite(TestAnonID.class);
	}

	/**
	 * Boilerplate for junit
	 */
	public TestAnonID( final String name )
	{
		super(name);
	}

	/**
	 * Check that anonIDs are distinct whichever state the flag is in.
	 */
	public void doTestAnonID()
	{
		final AnonId id1 = AnonId.create();
		final AnonId id2 = AnonId.create();
		final AnonId id3 = AnonId.create();
		final AnonId id4 = AnonId.create();

		JenaTestBase.assertDiffer(id1, id2);
		JenaTestBase.assertDiffer(id1, id3);
		JenaTestBase.assertDiffer(id1, id4);
		JenaTestBase.assertDiffer(id2, id3);
		JenaTestBase.assertDiffer(id2, id4);
	}

	/**
	 * Check that anonIDs are distinct whichever state the flag is in.
	 */
	public void testAnonID()
	{
		final boolean prior = JenaParameters.disableBNodeUIDGeneration;
		try
		{
			JenaParameters.disableBNodeUIDGeneration = false;
			doTestAnonID();
			JenaParameters.disableBNodeUIDGeneration = true;
			doTestAnonID();
		}
		finally
		{
			JenaParameters.disableBNodeUIDGeneration = prior;
		}
	}

	/**
	 * Test that creation of an AnonId from an AnonId string preserves that
	 * string and is equal to the original AnonId.
	 */
	public void testAnonIdPreserved()
	{
		final AnonId anon = AnonId.create();
		final String id = anon.toString();
		Assert.assertEquals(anon, AnonId.create(id));
		Assert.assertEquals(id, AnonId.create(id).toString());
	}

}
