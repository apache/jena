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
package org.apache.jena.security.model;

import com.hp.hpl.jena.rdf.model.Bag;

import org.apache.jena.security.MockSecurityEvaluator;
import org.apache.jena.security.SecurityEvaluatorParameters;
import org.apache.jena.security.model.impl.SecuredBagImpl;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith( value = SecurityEvaluatorParameters.class )
public class SecuredBagTest extends SecuredContainerTest
{

	public SecuredBagTest( final MockSecurityEvaluator securityEvaluator )
	{
		super(securityEvaluator);
	}

	@Override
	@Before
	public void setup()
	{
		super.setup();
		final Bag bag = baseModel.getBag("http://example.com/testContainer");
		bag.add("SomeDummyItem");
		setSecuredRDFNode(SecuredBagImpl.getInstance(securedModel, bag), bag);
	}

}
