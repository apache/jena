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

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Selector;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.test.helpers.ModelHelper;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;

import org.junit.Assert;

public class TestSelectors extends AbstractModelTestBase
{

	public TestSelectors( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	public void check( final Resource S, final Property P, final RDFNode O )
	{
		final Selector s = new SimpleSelector(S, P, O);
		Assert.assertTrue(s.isSimple());
		Assert.assertEquals(S, s.getSubject());
		Assert.assertEquals(P, s.getPredicate());
		Assert.assertEquals(O, s.getObject());
	}

	public void testSelectors()
	{

		check(null, null, null);
		check(ModelHelper.resource(model, "A"), null, null);
		check(null, ModelHelper.property(model, "B"), null);
		check(null, null, ModelHelper.literal(model, "10"));
		check(ModelHelper.resource(model, "C"),
				ModelHelper.property(model, "D"),
				ModelHelper.resource(model, "_E"));
	}
}
