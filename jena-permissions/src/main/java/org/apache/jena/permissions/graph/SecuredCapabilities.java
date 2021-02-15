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
package org.apache.jena.permissions.graph;

import org.apache.jena.graph.Capabilities ;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;

/**
 * The interface for secured Capabilities instances.
 *
 */
public class SecuredCapabilities implements Capabilities
{
	// the security evaluator in use
	private final SecurityEvaluator securityEvaluator;
	// the graphIRI that the capabilities belong to.
	private final Node graphIRI;
	// the unsecured capabilities.
	private final Capabilities capabilities;

	/**
	 * Constructor.
	 *
	 * @param securityEvaluator
	 *            The security evaluator in use.
	 * @param graphURI
	 *            The graphIRI that the capabilities describe.
	 * @param capabilities
	 *            The unsecured capabilities.
	 */
	public SecuredCapabilities( final SecurityEvaluator securityEvaluator,
			final String graphURI, final Capabilities capabilities )
	{
		this.securityEvaluator = securityEvaluator;
		this.graphIRI = NodeFactory.createURI(graphURI);
		this.capabilities = capabilities;
	}

	/**
	 * @sec.graph Update
	 */
	@Override
	public boolean addAllowed()
	{
		return securityEvaluator.evaluate(securityEvaluator.getPrincipal(), Action.Update, graphIRI)
				&& capabilities.addAllowed();
	}

	/**
	 * @sec.graph Update
	 */
	@Override
	public boolean deleteAllowed()
	{
		return securityEvaluator.evaluate(securityEvaluator.getPrincipal(), Action.Update, graphIRI)
				&& capabilities.deleteAllowed();
	}

	@Override
	public boolean handlesLiteralTyping()
	{
	    return capabilities.handlesLiteralTyping();
	}

	@Override
	public boolean sizeAccurate()
	{
		return capabilities.sizeAccurate();
	}
}