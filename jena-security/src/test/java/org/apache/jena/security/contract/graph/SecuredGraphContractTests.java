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
package org.apache.jena.security.contract.graph;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.test.MetaTestGraph;

import org.apache.jena.security.MockSecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator;

public class SecuredGraphContractTests extends MetaTestGraph
{

	private final SecurityEvaluator eval;

	public SecuredGraphContractTests( final Class<? extends Graph> graphClass,
			final String name )
	{
		super(graphClass, name);
		eval = new MockSecurityEvaluator(true, true, true, true, true, true);
	}

	public SecuredGraphContractTests( final String name )
	{
		super(name);
		eval = new MockSecurityEvaluator(true, true, true, true, true, true);
	}

	@Override
	public Graph getGraph()
	{
		return org.apache.jena.security.Factory.getInstance(eval, getName(),
				Factory.createDefaultGraph());
	}

}
