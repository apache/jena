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
package org.apache.jena.permissions.graph.impl;

import org.apache.jena.graph.*;
import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.graph.*;
import org.apache.jena.permissions.impl.ItemHolder;
import org.apache.jena.permissions.impl.SecuredItemImpl;
import org.apache.jena.permissions.utils.PermTripleFilter;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Implementation of SecuredGraph to be used by a SecuredItemInvoker proxy.
 */
public class SecuredGraphImpl extends SecuredItemImpl implements SecuredGraph {

	// the prefixMapping for this graph.
	private SecuredPrefixMapping prefixMapping;
	// the item holder that contains this SecuredGraph
	private final ItemHolder<Graph, SecuredGraphImpl> holder;

	private final SecuredGraphEventManager eventManager;

	/**
	 * Constructor
	 *
	 * @param securityEvaluator
	 *            The security evaluator to use
	 * @param graphIRI
	 *            The IRI for the graph
	 * @param holder
	 *            The item holder that will contain this SecuredGraph.
	 */
	SecuredGraphImpl(final SecuredItem securedItem,
			final ItemHolder<Graph, SecuredGraphImpl> holder) {
		super(securedItem, holder);
		this.holder = holder;
		this.eventManager = new SecuredGraphEventManager(this,
				holder.getBaseItem(), holder.getBaseItem().getEventManager());
	}

	SecuredGraphImpl(final SecurityEvaluator securityEvaluator,
			final String modelURI,
			final ItemHolder<Graph, SecuredGraphImpl> holder) {
		super(securityEvaluator, modelURI, holder);
		this.holder = holder;
		this.eventManager = new SecuredGraphEventManager(this,
				holder.getBaseItem(), holder.getBaseItem().getEventManager());
	}

	@Override
	public void add(final Triple t) throws AddDeniedException,
			UpdateDeniedException, AuthenticationRequiredException {
		checkUpdate();
		checkCreate(t);
		holder.getBaseItem().add(t);
	}

	@Override
	public void close() {
		holder.getBaseItem().close();
	}

	@Override
	public boolean contains(final Node s, final Node p, final Node o)
			throws ReadDeniedException, AuthenticationRequiredException {
		return contains(new Triple(s, p, o));
	}

	@Override
	public boolean contains(final Triple t) throws ReadDeniedException,
			AuthenticationRequiredException {
		checkRead();
		if (canRead(t)) {
			return holder.getBaseItem().contains(t);
		}
		final ExtendedIterator<Triple> iter = holder.getBaseItem().find(t);
		try {
			while (iter.hasNext()) {
				if (canRead(iter.next())) {
					return true;
				}
			}
			return false;
		} finally {
			iter.close();
		}

	}

	private synchronized void createPrefixMapping() {
		if (prefixMapping == null) {
			prefixMapping = org.apache.jena.permissions.graph.impl.Factory
					.getInstance(this, holder.getBaseItem().getPrefixMapping());
		}
	}

	@Override
	public void delete(final Triple t) throws DeleteDeniedException,
			AuthenticationRequiredException {
		checkUpdate();
		checkDelete(t);
		holder.getBaseItem().delete(t);
	}

	@Override
	public boolean dependsOn(final Graph other) throws ReadDeniedException,
			AuthenticationRequiredException {
		checkRead();
		if (other.equals(holder.getBaseItem())) {
			return true;
		}
		return holder.getBaseItem().dependsOn(other);
	}

	@Override
	public ExtendedIterator<Triple> find(final Node s, final Node p,
			final Node o) throws ReadDeniedException,
			AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Triple> retval = holder.getBaseItem().find(s, p, o);
		if (!canRead(Triple.ANY)) {
			retval = retval.filterKeep(new PermTripleFilter(Action.Read, this));
		}
		return retval;
	}

	@Override
	public ExtendedIterator<Triple> find(final Triple m)
			throws ReadDeniedException, AuthenticationRequiredException {
		checkRead();
		ExtendedIterator<Triple> retval = holder.getBaseItem().find(m);
		if (!canRead(Triple.ANY)) {
			retval = retval.filterKeep(new PermTripleFilter(Action.Read, this));
		}
		return retval;
	}

	@Override
	public SecuredCapabilities getCapabilities() {
		return new SecuredCapabilities(getSecurityEvaluator(), getModelIRI(),
				holder.getBaseItem().getCapabilities());
	}

	@Override
	public SecuredGraphEventManager getEventManager() {
		return eventManager;
	}

	@Override
	public SecuredPrefixMapping getPrefixMapping() {
		if (prefixMapping == null) {
			createPrefixMapping();
		}
		return prefixMapping;
	}

	@Override
	public TransactionHandler getTransactionHandler() {
		return holder.getBaseItem().getTransactionHandler();
	}

	@Override
	public boolean isClosed() {
		return holder.getBaseItem().isClosed();
	}

	@Override
	public boolean isEmpty() throws ReadDeniedException,
			AuthenticationRequiredException {
		checkRead();
		return holder.getBaseItem().isEmpty();
	}

	@Override
	public boolean isIsomorphicWith(final Graph g) throws ReadDeniedException,
			AuthenticationRequiredException {
		checkRead();
		if (g.size() != holder.getBaseItem().size()) {
			return false;
		}
		final Triple t = new Triple(Node.ANY, Node.ANY, Node.ANY);
		if (!canRead(t)) {
			final ExtendedIterator<Triple> iter = g.find(t);
			while (iter.hasNext()) {
				checkRead(iter.next());
			}
		}
		return holder.getBaseItem().isIsomorphicWith(g);
	}

	@Override
	public int size() throws ReadDeniedException,
			AuthenticationRequiredException {
		checkRead();
		return holder.getBaseItem().size();
	}

	@Override
	public void clear() throws UpdateDeniedException,
			AuthenticationRequiredException {
		checkUpdate();
		if (!canDelete(Triple.ANY)) {
			ExtendedIterator<Triple> iter = holder.getBaseItem().find(
					Triple.ANY);
			while (iter.hasNext()) {
				checkDelete(iter.next());
			}
		}
		holder.getBaseItem().clear();
	}

	@Override
	public void remove(Node s, Node p, Node o) throws UpdateDeniedException,
			DeleteDeniedException, AuthenticationRequiredException {
		checkUpdate();
		Triple t = new Triple(s, p, o);
		if (t.isConcrete()) {
			checkDelete(t);
		} else {
			ExtendedIterator<Triple> iter = holder.getBaseItem().find(
					Triple.ANY);
			while (iter.hasNext()) {
				checkDelete(iter.next());
			}
		}
		holder.getBaseItem().remove(s, p, o);
	}

}