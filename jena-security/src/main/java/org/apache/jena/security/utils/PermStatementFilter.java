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
package org.apache.jena.security.utils;

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.Filter;

import java.util.Collection;
import java.util.Set;

import org.apache.jena.security.SecurityEvaluator;
import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.SecurityEvaluator.SecNode;
import org.apache.jena.security.impl.SecuredItem;
import org.apache.jena.security.impl.SecuredItemImpl;

/**
 * A filter for to filter ExtendedIterators on Statements.
 * This filter removes any triple that the user can not perform all
 * the actions on.
 */
public class PermStatementFilter extends Filter<Statement>
{
	private final SecurityEvaluator evaluator;
	private final SecNode modelNode;
	private final Set<Action> actions;

	/**
	 * Creates a filter that requires that the user have all the permissions
	 * listed in the actions parameter
	 * 
	 * @param action
	 *            The action the user must be permitted to perform.
	 * @param securedItem
	 *            The secured item that secures this iterator.
	 */
	public PermStatementFilter( final Action action,
			final SecuredItem securedItem )
	{
		this.modelNode = securedItem.getModelNode();
		this.actions = SecurityEvaluator.Util.asSet(new Action[] { action });
		this.evaluator = securedItem.getSecurityEvaluator();
	}

	/**
	 * Creates a filter that requires that the user have all the permissions
	 * listed in the actions parameter
	 * 
	 * @param action
	 *            The action the user must be permitted to perform.
	 * @param securedItem
	 *            The secured item that secures this iterator.
	 * @param evaluator
	 *            The security evaluator to evaluate the security queries.
	 */
	public PermStatementFilter( final Action action,
			final SecuredItem securedItem, final SecurityEvaluator evaluator )
	{
		this.modelNode = securedItem.getModelNode();
		this.actions = SecurityEvaluator.Util.asSet(new Action[] { action });
		this.evaluator = evaluator;
	}

	/**
	 * Creates a filter that requires that the user have all the permissions
	 * listed in the actions parameter
	 * 
	 * @param actions
	 *            The actions the user must be permitted to perform.
	 * @param securedItem
	 *            The secured item that secures this iterator.
	 */
	public PermStatementFilter( final Action[] actions,
			final SecuredItem securedItem )
	{
		this.modelNode = securedItem.getModelNode();
		this.actions = SecurityEvaluator.Util.asSet(actions);
		this.evaluator = securedItem.getSecurityEvaluator();
	}

	/**
	 * Creates a filter that requires that the user have all the permissions
	 * listed in the actions parameter
	 * 
	 * @param actions
	 *            The actions the user must be permitted to perform.
	 * @param securedItem
	 *            The secured item that secures this iterator.
	 * @param evaluator
	 *            The security evaluator to evaluate the security queries.
	 */
	public PermStatementFilter( final Action[] actions,
			final SecuredItem securedItem, final SecurityEvaluator evaluator )
	{
		this.modelNode = securedItem.getModelNode();
		this.actions = SecurityEvaluator.Util.asSet(actions);
		this.evaluator = evaluator;
	}

	/**
	 * Creates a filter that requires that the user have all the permissions
	 * listed in the actions parameter
	 * 
	 * @param actions
	 *            The actions the user must be permitted to perform.
	 * @param securedItem
	 *            The secured item that secures this iterator.
	 */
	public PermStatementFilter( final Collection<Action> actions,
			final SecuredItem securedItem )
	{
		this.modelNode = securedItem.getModelNode();
		this.actions = SecurityEvaluator.Util.asSet(actions);
		this.evaluator = securedItem.getSecurityEvaluator();
	}

	/**
	 * Creates a filter that requires that the user have all the permissions
	 * listed in the actions parameter
	 * 
	 * @param actions
	 *            The actions the user must be permitted to perform.
	 * @param securedItem
	 *            The secured item that secures this iterator.
	 * @param evaluator
	 *            The security evaluator to evaluate the security queries.
	 */
	public PermStatementFilter( final Collection<Action> actions,
			final SecuredItem securedItem, final SecurityEvaluator evaluator )
	{
		this.modelNode = securedItem.getModelNode();
		this.actions = SecurityEvaluator.Util.asSet(actions);
		this.evaluator = evaluator;
	}

	@Override
	public boolean accept( final Statement s )
	{
		return evaluator.evaluateAny(actions, modelNode,
				SecuredItemImpl.convert(s.asTriple()));
	}

}