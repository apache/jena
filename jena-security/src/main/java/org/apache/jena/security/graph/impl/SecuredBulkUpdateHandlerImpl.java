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
package org.apache.jena.security.graph.impl;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.security.graph.SecuredBulkUpdateHandler;
import org.apache.jena.security.graph.SecuredGraph;
import org.apache.jena.security.impl.ItemHolder;
import org.apache.jena.security.impl.SecuredItemImpl;

/**
 * Implementation of SecuredBulkUpdateHandler to be used by a SecuredItemInvoker
 * proxy.
 */
@SuppressWarnings("deprecation")
public class SecuredBulkUpdateHandlerImpl extends SecuredItemImpl implements
		SecuredBulkUpdateHandler
{
	// the base graph for he secured graph.
	private final Graph baseGraph;
	// the graph this handler is for.
	private final SecuredGraph graph;
	// the item holder holding this SecuredBulkUpdateHandler.
	private final ItemHolder<BulkUpdateHandler, SecuredBulkUpdateHandler> holder;

	/**
	 * Constructor.
	 * 
	 * @param graph
	 *            The graph handler is for.
	 * @param holder
	 *            The item holder that will hold this SecuredBulkUpdateHandler.
	 */
	SecuredBulkUpdateHandlerImpl( final SecuredGraphImpl graph,
			final Graph baseGraph,
			final ItemHolder<BulkUpdateHandler, SecuredBulkUpdateHandler> holder )
	{
		super(graph, holder);
		this.holder = holder;
		this.graph = graph;
		this.baseGraph = baseGraph;
	}

	@Override
	public void add( final Graph g )
	{
		checkUpdate();
		final Graph g2 = g;
		if (!canCreate(Triple.ANY))
		{
			checkCreateTriples(g.find(Triple.ANY));
		}
		holder.getBaseItem().add(g2);
	}

	@Override
	@Deprecated
	public void add( final Graph g, final boolean withReifications )
	{
		checkUpdate();

		if (!canCreate(Triple.ANY))
		{
			checkCreateTriples(g.find(Triple.ANY));
		}
		holder.getBaseItem().add(g, withReifications);
	}

	@Override
	@Deprecated
	public void add( final Iterator<Triple> it )
	{
		checkUpdate();
		if (canCreate(Triple.ANY))
		{
			holder.getBaseItem().add(it);
		}
		else
		{
			final List<Triple> lst = WrappedIterator.create(it).toList();
			for (final Triple t : lst)
			{
				checkCreate(t);
			}
			holder.getBaseItem().add(lst.iterator());
		}
	}

	@Override
	@Deprecated
	public void add( final List<Triple> triples )
	{
		checkUpdate();
		if (!canCreate(Triple.ANY))
		{
			checkCreateTriples(WrappedIterator.create(triples.iterator()));
		}
		holder.getBaseItem().add(triples);
	}

	@Override
	@Deprecated
	public void add( final Triple[] triples )
	{
		checkUpdate();
		if (!canCreate(Triple.ANY))
		{
			checkCreateTriples(WrappedIterator.create(Arrays.asList(triples)
					.iterator()));
		}
		holder.getBaseItem().add(triples);
	}

	private void checkExtendedTripleDelete( final Triple t )
	{
		final ExtendedIterator<Triple> iter = baseGraph.find(t);
		try
		{
			while (iter.hasNext())
			{
				checkDelete(iter.next());
			}
		}
		finally
		{
			iter.close();
		}
	}

	@Override
	public void delete( final Graph g )
	{
		checkUpdate();
		if (!canDelete(Triple.ANY))
		{
			checkDeleteTriples(g.find(Triple.ANY));
		}
		holder.getBaseItem().delete(g);

	}

	@Override
	@Deprecated
	public void delete( final Graph g, final boolean withReifications )
	{
		checkUpdate();
		if (!canDelete(Triple.ANY))
		{
			checkDeleteTriples(g.find(Triple.ANY));
		}
		holder.getBaseItem().delete(g, withReifications);

	}

	@Override
	@Deprecated
	public void delete( final Iterator<Triple> it )
	{
		checkUpdate();
		final List<Triple> lst = WrappedIterator.create(it).toList();
		if (!canDelete(Triple.ANY))
		{
			checkDeleteTriples(WrappedIterator.create(lst.iterator()));
		}
		holder.getBaseItem().delete(lst.iterator());
	}

	@Override
	@Deprecated
	public void delete( final List<Triple> triples )
	{
		checkUpdate();
		if (!canDelete(Triple.ANY))
		{
			checkDeleteTriples(WrappedIterator.create(triples.iterator()));
		}
		holder.getBaseItem().delete(triples);
	}

	@Override
	@Deprecated
	public void delete( final Triple[] triples )
	{
		checkUpdate();
		if (!canDelete(Triple.ANY))
		{
			checkDeleteTriples(WrappedIterator.create(Arrays.asList(triples)
					.iterator()));
		}
		holder.getBaseItem().delete(triples);
	}

    @Override
	public void remove( final Node s, final Node p, final Node o )
	{
		checkUpdate();
		if (!canDelete(Triple.ANY))
		{
			// the remove can be a pattern so expand it.
			checkExtendedTripleDelete(new Triple(s, p, o));
		}
		holder.getBaseItem().remove(s, p, o);
	}

	@Override
	public void removeAll()
	{
		checkUpdate();

		if (!canDelete(Triple.ANY))
		{
			checkDeleteTriples(graph.find(Triple.ANY));
		}

		holder.getBaseItem().removeAll();
	}

}
