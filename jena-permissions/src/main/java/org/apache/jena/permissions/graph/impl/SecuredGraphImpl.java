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

import org.apache.commons.collections4.IteratorUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.graph.SecuredCapabilities;
import org.apache.jena.permissions.graph.SecuredGraph;
import org.apache.jena.permissions.graph.SecuredGraphEventManager;
import org.apache.jena.permissions.graph.SecuredPrefixMapping;
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
     * @param securityEvaluator The security evaluator to use
     * @param graphIRI          The IRI for the graph
     * @param holder            The item holder that will contain this SecuredGraph.
     */
    SecuredGraphImpl(final SecurityEvaluator securityEvaluator, final String modelURI,
            final ItemHolder<Graph, SecuredGraphImpl> holder) {
        super(securityEvaluator, modelURI, holder);
        this.holder = holder;
        this.eventManager = new SecuredGraphEventManager(this, holder.getBaseItem(),
                holder.getBaseItem().getEventManager());
    }

    /**
     * @sec.graph Update
     * @sec.triple Create
     * @throws AddDeniedException
     * @throws UpdateDeniedException           if the graph can not be updated.
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public void add(final Triple t) throws AddDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(t);
        holder.getBaseItem().add(t);
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete for every triple
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public void clear() throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        if (!canDelete(Triple.ANY)) {
            ExtendedIterator<Triple> iter = holder.getBaseItem().find(Triple.ANY);
            while (iter.hasNext()) {
                checkDelete(iter.next());
            }
        }
        holder.getBaseItem().clear();
    }

    @Override
    public void close() {
        holder.getBaseItem().close();
    }

    /**
     * @sec.graph Read
     * @sec.triple Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final Node s, final Node p, final Node o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return contains(Triple.create(s, p, o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final Triple t) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
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
        return false;
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete
     * @throws DeleteDeniedException
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public void delete(final Triple t) throws DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkDelete(t);
        holder.getBaseItem().delete(t);
    }

    /**
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true then this
     *            method returns false.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean dependsOn(final Graph other) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (other.equals(holder.getBaseItem())) {
                return true;
            }
            return holder.getBaseItem().dependsOn(other);
        }
        return false;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read, otherwise filtered from iterator.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then an empty iterator will be
     *             returned.
     *
     * @throws ReadDeniedException             on read not allowed
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public ExtendedIterator<Triple> find() throws ReadDeniedException, AuthenticationRequiredException {
        return createIterator(() -> holder.getBaseItem().find(Triple.ANY),
                () -> new PermTripleFilter(Action.Read, this));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read, otherwise filtered from iterator.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then an empty iterator will be
     *             returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public ExtendedIterator<Triple> find(final Node s, final Node p, final Node o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return createIterator(() -> holder.getBaseItem().find(s, p, o), () -> new PermTripleFilter(Action.Read, this));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read, otherwise filtered from iterator.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true then an
     *             empty iterator will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public ExtendedIterator<Triple> find(final Triple t) throws ReadDeniedException, AuthenticationRequiredException {
        return createIterator(() -> holder.getBaseItem().find(t), () -> new PermTripleFilter(Action.Read, this));
    }

    @Override
    public SecuredCapabilities getCapabilities() {
        return new SecuredCapabilities(getSecurityEvaluator(), getModelIRI(), holder.getBaseItem().getCapabilities());
    }

    @Override
    public SecuredGraphEventManager getEventManager() {
        return eventManager;
    }

    @Override
    public SecuredPrefixMapping getPrefixMapping() {
        if (prefixMapping == null) {
            synchronized (this) {
                if (prefixMapping == null) {
                    prefixMapping = org.apache.jena.permissions.graph.impl.Factory.getInstance(this,
                            holder.getBaseItem().getPrefixMapping());
                }
            }
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

    /**
     * @sec.graph Read
     *
     *            If {@link SecurityEvaluator#isHardReadError()} is false then this
     *            method will return 0.
     *
     * @throws ReadDeniedException             if graph can not be read.
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean isEmpty() throws ReadDeniedException, AuthenticationRequiredException {
        return checkSoftRead() ? holder.getBaseItem().isEmpty() : true;
    }

    /**
     * @sec.graph Read
     *
     *            If {@link SecurityEvaluator#isHardReadError()} is false then this
     *            method will return false unless {@code g} is empty.
     *
     * @throws ReadDeniedException             if graph can not be read.
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean isIsomorphicWith(final Graph g) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (g.size() != holder.getBaseItem().size()) {
                return false;
            }
            final Triple t = Triple.create(Node.ANY, Node.ANY, Node.ANY);
            if (!canRead(t)) {
                final ExtendedIterator<Triple> iter = g.find(t);
                while (iter.hasNext()) {
                    if (!checkRead(iter.next())) {
                        return false;
                    }
                }
            }
            return holder.getBaseItem().isIsomorphicWith(g);
        }
        return g.isEmpty();
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete (s, p, o )
     * @throws DeleteDeniedException
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public void remove(Node s, Node p, Node o)
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        Triple t = Triple.create(s, p, o);
        if (t.isConcrete()) {
            checkDelete(t);
        } else {
            ExtendedIterator<Triple> iter = holder.getBaseItem().find(t);
            while (iter.hasNext()) {
                checkDelete(iter.next());
            }
        }
        holder.getBaseItem().remove(s, p, o);
    }

    /**
     * @sec.graph Read
     *
     *            If {@link SecurityEvaluator#isHardReadError()} is false then this
     *            method will return 0.
     *
     * @throws ReadDeniedException             if graph can not be read.
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int size() throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                return holder.getBaseItem().size();
            }
            return IteratorUtils.size(find(Triple.ANY));
        }
        return 0;
    }
}