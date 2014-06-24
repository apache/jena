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

import com.hp.hpl.jena.rdf.model.Resource;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import org.apache.jena.security.SecurityEvaluator;

public class MockSecurityEvaluator implements SecurityEvaluator
{

	private final boolean loggedIn;
	private final boolean create;
	private final boolean read;
	private final boolean update;
	private final boolean delete;
	private final boolean forceTripleChecks;
	
	public static MockSecurityEvaluator getInstance()
	{
		return new MockSecurityEvaluator( true, true, true, true, true, true );
	}


	public MockSecurityEvaluator( final boolean loggedIn, final boolean create,
			final boolean read, final boolean update, final boolean delete,
			final boolean forceTripleChecks )
	{
		this.loggedIn = loggedIn;
		this.create = create;
		this.read = read;
		this.update = update;
		this.delete = delete;
		this.forceTripleChecks = forceTripleChecks;
	}

	public boolean evaluate( final Action action )
	{
		switch (action)
		{
			case Read:
				return read;
			case Create:
				return create;
			case Update:
				return update;
			case Delete:
				return delete;
			default:
				throw new IllegalArgumentException();
		}
	}

	/**
	 * Answers the question. can the logged in user perform action on the
	 * object.
	 * 
	 * if there is no logged in user then anonymous access is assumed.
	 * 
	 * @param action
	 * @param object
	 * @return boolean
	 */
	public boolean evaluate( final Action action, final Resource object )
	{

		return evaluate(action);
	}

	@Override
	public boolean evaluate( final Action action, final SecNode uri )
	{
		return evaluate(action);
	}

	@Override
	public boolean evaluate( final Action action, final SecNode graphIRI,
			final SecTriple triple )
	{
		if (forceTripleChecks)
		{
			if (triple.getSubject().equals(SecNode.ANY)
					|| triple.getPredicate().equals(SecNode.ANY)
					|| triple.getObject().equals(SecNode.ANY))
			{
				return false;
			}
		}
		return evaluate(action);
	}

	public boolean evaluate( final Action[] actions )
	{
		for (final Action a : actions)
		{
			if (!evaluate(a))
			{
				return false;
			}
		}
		return true;
	}

	public boolean evaluate( final Set<Action> action )
	{
		boolean result = true;
		for (final Action a : action)
		{
			result &= evaluate(a);
		}
		return result;
	}

	public boolean evaluate( final Set<Action> action, final Resource object )
	{
		boolean result = true;
		for (final Action a : action)
		{
			result &= evaluate(a);
		}
		return result;
	}

	@Override
	public boolean evaluate( final Set<Action> action, final SecNode uri )
	{
		return evaluate(action);
	}

	@Override
	public boolean evaluate( final Set<Action> action, final SecNode graphIRI,
			final SecTriple triple )
	{
		for (final Action a : action)
		{
			if (!evaluate(a))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean evaluateAny( final Set<Action> action, final SecNode graphIRI )
	{
		for (final Action a : action)
		{
			if (evaluate(a))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean evaluateAny( final Set<Action> action,
			final SecNode graphIRI, final SecTriple triple )
	{
		return evaluateAny(action, graphIRI);
	}

	@Override
	public boolean evaluateUpdate( final SecNode graphIRI,
			final SecTriple from, final SecTriple to )
	{
		return evaluate(Action.Update);
	}

	public Set<Action> getPermissions( final Resource resourceID )
	{
		return Collections.emptySet();
	}

	public Set<Action> getPermissions( final SecNode uri )
	{
		return Collections.emptySet();
	}

	@Override
	public Principal getPrincipal()
	{
		if (loggedIn)
		{
			return new Principal() {

				@Override
				public String getName()
				{
					return "TestingPrincipal";
				}
			};
		}
		return null;
	}

	public boolean isLoggedIn()
	{
		return loggedIn;
	}

	@Override
	public String toString()
	{
		return String.format("C:%s R:%s U:%s D:%s force:%s", create, read,
				update, delete, forceTripleChecks);
	}

}
