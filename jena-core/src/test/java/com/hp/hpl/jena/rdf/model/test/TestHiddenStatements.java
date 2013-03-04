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

import org.junit.Assert ;

import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory ;
import com.hp.hpl.jena.shared.PrefixMapping ;

public class TestHiddenStatements extends AbstractModelTestBase
{
	public TestHiddenStatements( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	public void assertSameMapping( final PrefixMapping L, final PrefixMapping R )
	{
		if (sameMapping(L, R) == false)
		{
			Assert.fail("wanted " + L + " but got " + R);
		}
	}

	public boolean sameMapping( final PrefixMapping L, final PrefixMapping R )
	{
		// System.err.println( ">> " + L.getNsPrefixMap() );
		// System.err.println( ">> " + R.getNsPrefixMap() );
		return L.getNsPrefixMap().equals(R.getNsPrefixMap());
	}

	/**
	 * Test that withHiddenStatements copies the prefix mapping
	 * TODO add some extra prefixs for checking; should check for non-
	 * default models.
	 */
	public void testPrefixCopied()
	{
		model.setNsPrefixes(PrefixMapping.Standard);
		assertSameMapping(PrefixMapping.Standard, model) ;
	}
}
