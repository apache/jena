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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.test.helpers.TestingModelFactory;

import org.junit.Assert;

public class TestModelPolymorphism extends AbstractModelTestBase
{

	public TestModelPolymorphism( final TestingModelFactory modelFactory,
			final String name )
	{
		super(modelFactory, name);
	}

	public void testPoly()
	{
		final Resource r = model
				.createResource("http://www.electric-hedgehog.net/a-o-s.html");
		Assert.assertFalse("the Resouce should not be null", r == null);
		Assert.assertTrue("the Resource can be a Property",
				r.canAs(Property.class));
		final Property p = r.as(Property.class);
		Assert.assertFalse("the Property should not be null", p == null);
		Assert.assertFalse("the Resource and Property should not be identical",
				r == p);
	}
}
