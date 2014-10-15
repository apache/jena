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
package org.apache.jena.security.model.impl;

import com.hp.hpl.jena.rdf.model.ReifiedStatement;

import org.apache.jena.security.impl.ItemHolder;
import org.apache.jena.security.impl.SecuredItemInvoker;
import org.apache.jena.security.model.SecuredModel;
import org.apache.jena.security.model.SecuredReifiedStatement;
import org.apache.jena.security.model.SecuredStatement;

/**
 * Implementation of SecuredReifiedStatement to be used by a SecuredItemInvoker
 * proxy.
 */
public class SecuredReifiedStatementImpl extends SecuredResourceImpl implements
		SecuredReifiedStatement
{
	/**
	 * Get an instance of SecuredReifiedStatement
	 * 
	 * @param securedModel
	 *            the Secured Model to use.
	 * @param stmt
	 *            The ReifiedStatement to secure.
	 * @return SecuredReifiedStatement
	 */
	public static SecuredReifiedStatement getInstance(
			final SecuredModel securedModel, final ReifiedStatement stmt )
	{
		if (securedModel == null)
		{
			throw new IllegalArgumentException(
					"Secured securedModel may not be null");
		}
		if (stmt == null)
		{
			throw new IllegalArgumentException("Statement may not be null");
		}
		final ItemHolder<ReifiedStatement, SecuredReifiedStatement> holder = new ItemHolder<ReifiedStatement, SecuredReifiedStatement>(
				stmt);
		final SecuredReifiedStatementImpl checker = new SecuredReifiedStatementImpl(
				securedModel, holder);
		// if we are going to create a duplicate proxy, just return this
		// one.
		if (stmt instanceof SecuredReifiedStatement)
		{
			if (checker.isEquivalent((SecuredReifiedStatement) stmt))
			{
				return (SecuredReifiedStatement) stmt;
			}
		}
		return holder.setSecuredItem(new SecuredItemInvoker(stmt.getClass(),
				checker));
	}

	// the item holder that contains this SecuredResource
	private final ItemHolder<? extends ReifiedStatement, ? extends SecuredReifiedStatement> holder;

	/**
	 * Constructor
	 * 
	 * @param securedModel
	 *            The secured model to use
	 * @param holder
	 *            the item holder that will contain this SecuredReifiedStatement
	 */
	protected SecuredReifiedStatementImpl(
			final SecuredModel securedModel,
			final ItemHolder<? extends ReifiedStatement, ? extends SecuredReifiedStatement> holder )
	{
		super(securedModel, holder);
		this.holder = holder;
	}

	@Override
	public SecuredStatement getStatement()
	{
		checkRead();
		return SecuredStatementImpl.getInstance(getModel(), holder
				.getBaseItem().getStatement());
	}

}
