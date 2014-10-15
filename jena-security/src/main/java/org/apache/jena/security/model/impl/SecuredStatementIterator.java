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

import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.util.iterator.Map1;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.jena.security.SecurityEvaluator.Action;
import org.apache.jena.security.model.SecuredModel;
import org.apache.jena.security.model.SecuredStatement;
import org.apache.jena.security.utils.PermStatementFilter;

/**
 * A secured StatementIterator implementation
 */
public class SecuredStatementIterator implements StmtIterator
{

	private class PermStatementMap implements Map1<Statement, Statement>
	{
		private final SecuredModel securedModel;

		public PermStatementMap( final SecuredModel securedModel )
		{
			this.securedModel = securedModel;
		}

		@Override
		public SecuredStatement map1( final Statement o )
		{
			return SecuredStatementImpl.getInstance(securedModel, o);
		}
	}

	private final ExtendedIterator<Statement> iter;

	/**
	 * Constructor.
	 * 
	 * @param securedModel
	 *            The item providing the security context.
	 * @param wrapped
	 *            The iterator to wrap.
	 */
	public SecuredStatementIterator( final SecuredModel securedModel,
			final ExtendedIterator<Statement> wrapped )
	{
		final PermStatementFilter filter = new PermStatementFilter(
				new Action[] { Action.Read }, securedModel);
		final PermStatementMap map1 = new PermStatementMap(securedModel);
		iter = wrapped.filterKeep(filter).mapWith(map1);
	}

	@Override
	public <X extends Statement> ExtendedIterator<Statement> andThen(
			final Iterator<X> other )
	{
		return iter.andThen(other);
	}

	@Override
	public void close()
	{
		iter.close();
	}

	@Override
	public ExtendedIterator<Statement> filterDrop( final Filter<Statement> f )
	{
		return iter.filterDrop(f);
	}

	@Override
	public ExtendedIterator<Statement> filterKeep( final Filter<Statement> f )
	{
		return iter.filterKeep(f);
	}

	@Override
	public boolean hasNext()
	{
		return iter.hasNext();
	}

	@Override
	public <U> ExtendedIterator<U> mapWith( final Map1<Statement, U> map1 )
	{
		return iter.mapWith(map1);
	}

	@Override
	public Statement next()
	{
		return iter.next();
	}

	@Override
	public Statement nextStatement() throws NoSuchElementException
	{
		return next();
	}

	@Override
	public void remove()
	{
		iter.remove();
	}

	@Override
	public Statement removeNext()
	{
		return iter.removeNext();
	}

	@Override
	public List<Statement> toList()
	{
		return iter.toList();
	}

	@Override
	public Set<Statement> toSet()
	{
		return iter.toSet();
	}
}
