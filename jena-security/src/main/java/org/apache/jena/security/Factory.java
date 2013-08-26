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
package org.apache.jena.security;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;

import org.apache.jena.security.graph.SecuredGraph;
import org.apache.jena.security.model.SecuredModel;

/**
 * The factory that can be used to create an instance of a SecuredGraph or a SecuredModel.
 */
public class Factory
{

	/**
	 * Create an instance of the SecuredGraph
	 * 
	 * @param securityEvaluator
	 *            The security evaluator to use
	 * @param graphIRI
	 *            The IRI for the graph.
	 * @param graph
	 *            The graph that we are wrapping.
	 * @return the graph secured under the name graphIRI
	 */
	public static SecuredGraph getInstance(
			final SecurityEvaluator securityEvaluator, final String graphIRI,
			final Graph graph )
	{

		return org.apache.jena.security.graph.impl.Factory.getInstance(
				securityEvaluator, graphIRI, graph);
	}

	/**
	 * Get an instance of SecuredModel
	 * 
	 * @param securityEvaluator
	 *            The security evaluator to use
	 * @param modelIRI
	 *            The securedModel IRI (graph IRI) to evaluate against.
	 * @param model
	 *            The model to secure.
	 * @return the model secured under the name modelIRI
	 */
	public static SecuredModel getInstance(
			final SecurityEvaluator securityEvaluator, final String modelIRI,
			final Model model )
	{
		return org.apache.jena.security.model.impl.SecuredModelImpl.getInstance(
				securityEvaluator, modelIRI, model);
	}
}
