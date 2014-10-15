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
package org.apache.jena.security.graph;

import com.hp.hpl.jena.graph.Capabilities;

import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.SecurityEvaluator.SecNode;
import org.apache.jena.security.SecurityEvaluator.SecTriple;
import org.apache.jena.security.SecurityEvaluator.SecNode.Type;

/**
 * The interface for secured Capabilities instances.
 * 
 */
public class SecuredCapabilities implements Capabilities
{
	// the security evaluator in use
	private final SecurityEvaluator securityEvaluator;
	// the graphIRI that the capabilities belong to.
	private final SecNode graphIRI;
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
		this.graphIRI = new SecNode(Type.URI, graphURI);
		this.capabilities = capabilities;
	}

	/**
	 * @sec.graph Update
	 */
	@Override
	public boolean addAllowed()
	{
		return securityEvaluator.evaluate(Action.Update, graphIRI)
				&& capabilities.addAllowed();
	}

	/**
	 * @sec.graph Update
	 * @sec.triple Create (if everyTriple is true)
	 */
	@Override
	public boolean addAllowed( final boolean everyTriple )
	{
		boolean retval = securityEvaluator.evaluate(Action.Update, graphIRI)
				&& capabilities.addAllowed(everyTriple);
		if (retval && everyTriple)
		{
			// special security check
			retval = securityEvaluator.evaluate(Action.Create, graphIRI,
					SecTriple.ANY);
		}
		return retval;
	}

	@Override
	public boolean canBeEmpty()
	{
		return capabilities.canBeEmpty();
	}

	/**
	 * @sec.graph Update
	 */
	@Override
	public boolean deleteAllowed()
	{
		return securityEvaluator.evaluate(Action.Update, graphIRI)
				&& capabilities.deleteAllowed();
	}

	/**
	 * @sec.graph Update
	 * @sec.triple Delete (if everyTriple is true)
	 */
	@Override
	public boolean deleteAllowed( final boolean everyTriple )
	{
		boolean retval = securityEvaluator.evaluate(Action.Update, graphIRI)
				&& capabilities.addAllowed(everyTriple);
		if (retval && everyTriple)
		{
			// special security check
			retval = securityEvaluator.evaluate(Action.Delete, graphIRI,
					SecTriple.ANY);
		}
		return retval;
	}

	@Override
	public boolean findContractSafe()
	{
		return capabilities.findContractSafe();
	}

	@Override
	public boolean handlesLiteralTyping()
	{
		return capabilities.handlesLiteralTyping();
	}

	/**
	 * @sec.graph Update
	 */
	@Override
	public boolean iteratorRemoveAllowed()
	{
		return securityEvaluator.evaluate(Action.Update, graphIRI)
				&& capabilities.iteratorRemoveAllowed();
	}

	@Override
	public boolean sizeAccurate()
	{
		return capabilities.sizeAccurate();
	}
}