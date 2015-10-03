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
package org.apache.jena.permissions.model.impl;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.permissions.model.SecuredModel;
import org.apache.jena.permissions.model.SecuredRDFNode;
import org.apache.jena.rdf.model.NodeIterator ;
import org.apache.jena.rdf.model.RDFNode ;
import org.apache.jena.util.iterator.ExtendedIterator ;

/**
 * A secured RDFNode iterator implementation
 */
public class SecuredNodeIterator<T extends RDFNode> implements NodeIterator
{
	private class PermNodeMap<N extends RDFNode> implements Function<N, RDFNode>
	{
		private final SecuredModel securedModel;

		public PermNodeMap( final SecuredModel securedModel )
		{
			this.securedModel = securedModel;
		}

		@Override
		public SecuredRDFNode apply( final RDFNode o )
		{
			return SecuredRDFNodeImpl.getInstance(securedModel, o);
		}
	}

	private final ExtendedIterator<RDFNode> iter;

	/**
	 * Constructor
	 * 
	 * @param securedItem
	 *            the item defining the security context
	 * @param wrapped
	 *            the iterator to be wrapped.
	 */
	SecuredNodeIterator( final SecuredModel securedModel,
			final ExtendedIterator<T> wrapped )
	{
		final PermNodeMap<T> map1 = new PermNodeMap<T>(securedModel);
		iter = wrapped.mapWith(map1);
	}

	@Override
	public <X extends RDFNode> ExtendedIterator<RDFNode> andThen(
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
	public ExtendedIterator<RDFNode> filterDrop( final Predicate<RDFNode> f )
	{
		return iter.filterDrop(f);
	}

	@Override
	public ExtendedIterator<RDFNode> filterKeep( final Predicate<RDFNode> f )
	{
		return iter.filterKeep(f);
	}

	@Override
	public boolean hasNext()
	{
		return iter.hasNext();
	}

	@Override
	public <U> ExtendedIterator<U> mapWith( final Function<RDFNode, U> map1 )
	{
		return iter.mapWith(map1);
	}

	@Override
	public RDFNode next()
	{
		return iter.next();
	}

	@Override
	public RDFNode nextNode() throws NoSuchElementException
	{
		return next();
	}

	@Override
	public void remove()
	{
		iter.remove();
	}

	@Override
	public RDFNode removeNext()
	{
		return iter.removeNext();
	}

	@Override
	public List<RDFNode> toList()
	{
		return iter.toList();
	}

	@Override
	public Set<RDFNode> toSet()
	{
		return iter.toSet();
	}
}
