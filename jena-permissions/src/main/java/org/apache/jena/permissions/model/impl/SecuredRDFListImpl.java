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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.permissions.impl.ItemHolder;
import org.apache.jena.permissions.impl.SecuredItemInvoker;
import org.apache.jena.permissions.model.SecuredModel;
import org.apache.jena.permissions.model.SecuredRDFList;
import org.apache.jena.permissions.model.SecuredRDFNode;
import org.apache.jena.permissions.utils.RDFListIterator;
import org.apache.jena.permissions.utils.RDFListSecFilter;
import org.apache.jena.rdf.model.EmptyListException;
import org.apache.jena.rdf.model.EmptyListUpdateException;
import org.apache.jena.rdf.model.InvalidListException;
import org.apache.jena.rdf.model.ListIndexException;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.RDF;

public class SecuredRDFListImpl extends SecuredResourceImpl implements SecuredRDFList {

    /**
     * Extracts the first property of the list as a secured node.
     *
     * @param lst The list item to get the property from.
     * @return the property as a SecuredRDFNode.
     */
    private SecuredRDFNode extractSecuredNode(final RDFList lst) {
        return SecuredRDFNodeImpl.getInstance(getModel(), lst.getRequiredProperty(RDF.first).getObject());
    }

    private static Function<RDFList, RDFNode> valueMapper = new Function<RDFList, RDFNode>() {
        @Override
        public RDFNode apply(RDFList lst) {
            return lst.getRequiredProperty(RDF.first).getObject();
        }
    };

    /**
     * Get an instance of SecuredProperty
     *
     * @param securedModel the Secured Model to use.
     * @param rdfList      The rdfList to secure
     * @return The SecuredProperty
     */
    public static <T extends RDFList> SecuredRDFList getInstance(final SecuredModel securedModel, final T rdfList) {
        if (securedModel == null) {
            throw new IllegalArgumentException("Secured securedModel may not be null");
        }
        if (rdfList == null) {
            throw new IllegalArgumentException("RDFList may not be null");
        }

        // check that property has a securedModel.
        RDFList goodList = rdfList;
        if (goodList.getModel() == null) {
            goodList = securedModel.createList(rdfList.asJavaList().iterator());
        }

        final ItemHolder<RDFList, SecuredRDFList> holder = new ItemHolder<>(goodList);
        final SecuredRDFListImpl checker = new SecuredRDFListImpl(securedModel, holder);
        // if we are going to create a duplicate proxy, just return this
        // one.
        if (goodList instanceof SecuredRDFList) {
            if (checker.isEquivalent((SecuredRDFList) goodList)) {
                return (SecuredRDFList) goodList;
            }
        }
        return holder.setSecuredItem(new SecuredItemInvoker(rdfList.getClass(), checker));
    }

    private final ItemHolder<RDFList, SecuredRDFList> holder;

    protected SecuredRDFListImpl(final SecuredModel securedModel, final ItemHolder<RDFList, SecuredRDFList> holder) {
        super(securedModel, holder);
        this.holder = holder;
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple(SecNode.FUTURE, RDF.first, value)
     * @sec.triple Create SecTriple(SecNode.FUTURE, RDF.first, RDF.nil)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public void add(final RDFNode value)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreateNewList(value, RDF.nil);
        holder.getBaseItem().add(value);
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, value )
     * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.rest, this )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public RDFList append(final Iterator<? extends RDFNode> nodes)
            throws ReadDeniedException, AuthenticationRequiredException {
        // copy checks update
        SecuredRDFList copy = copy();
        if (nodes.hasNext()) {
            if (((RDFList) copy.getBaseItem()).size() > 0) {
                copy.concatenate(copy.getModel().createList(nodes));
            } else {
                copy = (SecuredRDFList) copy.getModel().createList(nodes);
            }
        }
        return copy;
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, value )
     * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.rest, this )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public RDFList append(final RDFList list) throws ReadDeniedException, AuthenticationRequiredException {
        return append(list.iterator());
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then the function is not
     *            applied to any list items.
     * @sec.graph Read
     * @sec.triple Read (to be included in the calculation)
     * @sec.triple other permissions required by the function.
     * @throws ReadDeniedException             graph Read or other permissions are
     *                                         not met
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public void apply(final ApplyFn fn) throws ReadDeniedException, AuthenticationRequiredException {
        // iterator() checks Read
        final ExtendedIterator<RDFNode> i = iterator();
        try {
            while (i.hasNext()) {
                fn.apply(i.next());
            }
        } finally {
            i.close();
        }
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then the function will not be
     *            applied to any of the elements.
     * @sec.triple Read and constraints
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public void apply(final Set<Action> perms, final ApplyFn fn)
            throws ReadDeniedException, AuthenticationRequiredException {
        // iterator() checks Read
        final ExtendedIterator<RDFNode> i = iterator(perms);
        try {
            while (i.hasNext()) {
                fn.apply(i.next());
            }
        } finally {
            i.close();
        }
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then an empty list is returned.
     * @sec.triple Read for triples containing the returned RDFNodes.
     * @return List&lt;SecuredRDFNode&gt;
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public List<RDFNode> asJavaList() throws ReadDeniedException, AuthenticationRequiredException {
        // iterator() checks Read
        return iterator().toList();
    }

    /**
     * Removes val from underlying list.
     *
     * @param val
     * @return the modified RDFList.
     */
    private RDFList baseRemove(final RDFList val) {

        RDFList prev = null;
        RDFList cell = holder.getBaseItem();
        final boolean searching = true;

        while (searching && !cell.isEmpty()) {
            if (cell.equals(val)) {
                // found the value to be removed
                final RDFList tail = cell.getTail();
                if (prev != null) {
                    prev.setTail(tail);
                }

                cell.removeProperties();

                // return this unless we have removed the head element
                return (prev == null) ? tail : this;
            }
            // not found yet
            prev = cell;
            cell = cell.getTail();
        }

        // not found
        return this;
    }

    private void checkCreateNewList(final RDFNode value, final Resource tail)
            throws AddDeniedException, AuthenticationRequiredException {
        checkCreate(Triple.create(SecurityEvaluator.FUTURE, RDF.first.asNode(), value.asNode()));
        checkCreate(Triple.create(SecurityEvaluator.FUTURE, RDF.rest.asNode(), tail.asNode()));
    }

    private Set<Statement> collectStatements(final Set<Action> actions) {
        final Set<Statement> stmts = new HashSet<>();
        final ExtendedIterator<RDFList> iter = WrappedIterator.create(new RDFListIterator(holder.getBaseItem()))
                .filterKeep(new RDFListSecFilter<>(this, actions));
        try {
            while (iter.hasNext()) {
                stmts.addAll(iter.next().listProperties().toSet());
            }
            return stmts;
        } finally {
            iter.close();
        }
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, node ) for each node
     *             in nodes.
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public void concatenate(final Iterator<? extends RDFNode> nodes)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        if (holder.getBaseItem().isEmpty()) {
            // concatenating list onto the empty list is an error
            throw new EmptyListUpdateException("Tried to concatenate onto the empty list");
        }
        Triple t = Triple.create(SecurityEvaluator.FUTURE, RDF.first.asNode(), Node.ANY);
        if (!canCreate(t)) {
            final List<RDFNode> list = new ArrayList<>();
            while (nodes.hasNext()) {
                final RDFNode n = nodes.next();
                t = Triple.create(SecurityEvaluator.FUTURE, RDF.first.asNode(), n.asNode());
                checkCreate(t);
                list.add(n);
            }
            holder.getBaseItem().concatenate(list.iterator());
        } else {
            holder.getBaseItem().concatenate(nodes);
        }
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, node ) for each node
     *             in nodes.
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public void concatenate(final RDFList list)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        if (holder.getBaseItem().isEmpty()) {
            // concatenating list onto the empty list is an error
            throw new EmptyListUpdateException("Tried to concatenate onto the empty list");
        }
        Triple t = Triple.create(SecurityEvaluator.FUTURE, RDF.first.asNode(), Node.ANY);
        if (!canCreate(t)) {
            final ExtendedIterator<RDFNode> iter = list.iterator();
            try {
                while (iter.hasNext()) {
                    t = Triple.create(SecurityEvaluator.FUTURE, RDF.first.asNode(), iter.next().asNode());
                    checkCreate(t);
                }
            } finally {
                iter.close();
            }
        }
        holder.getBaseItem().concatenate(list);
    }

    /**
     * @sec.graph Update
     * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, value )
     * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.rest, this )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredRDFList cons(final RDFNode value)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreateNewList(value, holder.getBaseItem());
        return SecuredRDFListImpl.getInstance(getModel(), holder.getBaseItem().cons(value));
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then {@code false} is returned.
     * @sec.triple Read for triple containing value.
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final RDFNode value) throws ReadDeniedException, AuthenticationRequiredException {
        // iterator() checks Read
        final ExtendedIterator<RDFNode> iter = iterator();
        try {
            while (iter.hasNext()) {
                if (value.equals(iter.next())) {
                    return true;
                }
            }
            return false;
        } finally {
            iter.close();
        }
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then an empty list is returned.
     * @sec.graph Update
     * @sec.triple Read on each triple to be read.
     * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.first, value )
     * @sec.triple Create SecTriple( SecNode.FUTURE, RDF.rest, this )
     * @throws ReadDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredRDFList copy() throws ReadDeniedException, AuthenticationRequiredException {
        checkUpdate();
        SecuredRDFList retval = null;
        if (checkSoftRead()) {
            final ExtendedIterator<RDFNode> iter = getFilteredRDFListIterator(Action.Read).mapWith(valueMapper);
            if (iter.hasNext()) {
                retval = (SecuredRDFList) getModel().createList(iter);
            } else {
                retval = (SecuredRDFList) getModel().createList();
            }
        } else {
            retval = (SecuredRDFList) getModel().createList();
        }
        return retval;
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then a
     *            {@link ListIndexException} is thrown. The indexing of the
     *            underlying list may be shifted by permission constraints.
     * @throws ReadDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public RDFNode get(final int i) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final ExtendedIterator<RDFList> iter = getFilteredRDFListIterator(Action.Read);
            int idx = 0;
            try {
                while (iter.hasNext()) {
                    if (i == idx) {
                        return extractSecuredNode(iter.next());
                    }
                    idx++;
                    iter.next();
                }
                throw new ListIndexException();
            } finally {
                iter.close();
            }
        }
        throw new ListIndexException();
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then a
     *            {@link ListIndexException} is thrown. The head may be shifted from
     *            the underlying list by permission constraints.
     * @sec.triple Read for triple containing value.
     * @throws ReadDeniedException
     * @throws EmptyListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public RDFNode getHead() throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            Statement s = holder.getBaseItem().getRequiredProperty(RDF.first);
            checkRead(s);
            return SecuredRDFNodeImpl.getInstance(getModel(), s.getObject());
        }
        throw new ListIndexException();

    }

    /**
     * Creates an iterator on the base (unsecured) RDFList elements that the user
     * has permissions on.
     *
     * @param perm the permission required for access.
     * @return An extended iterator on the base RDF list.
     */
    private ExtendedIterator<RDFList> getFilteredRDFListIterator(final Action perm) {
        return WrappedIterator.create(new RDFListIterator(holder.getBaseItem()))
                .filterKeep(new RDFListSecFilter<>(this, perm));
    }

    /**
     * Creates an iterator on the base (unsecured) RDFList elements that the user
     * has permissions on.
     *
     * @param perm the set of permissions required for access.
     * @return An extended iterator on the base RDF list.
     */
    private ExtendedIterator<RDFList> getFilteredRDFListIterator(final Set<Action> perm) {
        return WrappedIterator.create(new RDFListIterator(holder.getBaseItem()))
                .filterKeep(new RDFListSecFilter<>(this, perm));
    }

    @Override
    public boolean getStrict() {
        return holder.getBaseItem().getStrict();
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then a
     *            {@link ListIndexException} is thrown. The tail may be shifted from
     *            the underlying list by permission constraints.
     * @sec.triple Read for triple containing value.
     * @throws ReadDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredRDFList getTail() throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            Statement s = holder.getBaseItem().getRequiredProperty(RDF.rest);
            checkRead(s);
            return SecuredRDFListImpl.getInstance(getModel(), s.getObject().as(RDFList.class));
        }
        throw new ListIndexException();
    }

    /**
     * @sec.graph Read- if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then null is returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String getValidityErrorMessage() throws ReadDeniedException, AuthenticationRequiredException {
        return checkSoftRead() ? holder.getBaseItem().getValidityErrorMessage() : null;
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then -1 is returned.
     * @sec.triple Read for triple containing value.
     * @throws ReadDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int indexOf(final RDFNode value) throws ReadDeniedException, AuthenticationRequiredException {
        return indexOf(value, 0);
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then -1 is returned
     * @sec.triple Read for triple containing value.
     * @throws ReadDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int indexOf(final RDFNode value, final int start)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final ExtendedIterator<RDFList> iter = getFilteredRDFListIterator(Action.Read);
            try {
                int retval = 0;
                while (iter.hasNext() && (retval < start)) {
                    iter.next();
                    retval++;
                }
                while (iter.hasNext()) {
                    if (value.equals(valueMapper.apply(iter.next()))) {
                        return retval;
                    }
                    retval++;
                }
                return -1;
            } finally {
                iter.close();
            }
        }
        return -1;
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then {@code true} is returned.
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean isEmpty() throws ReadDeniedException, AuthenticationRequiredException {
        final ExtendedIterator<RDFNode> iter = iterator();
        try {
            return !iter.hasNext();
        } finally {
            iter.close();
        }
    }

    /**
     * @sec.graph Read
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean isValid() throws ReadDeniedException, AuthenticationRequiredException {
        checkRead();
        return holder.getBaseItem().isValid();
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then an empty iterator is
     *            returned.
     * @sec.triple Read for triple containing value to be included in the result.
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public ExtendedIterator<RDFNode> iterator() throws ReadDeniedException, AuthenticationRequiredException {
        return iterator(Set.of());
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then an empty iterator is
     *            returned.
     * @sec.triple Read for triple containing value to be included in the result.
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public ExtendedIterator<RDFNode> iterator(final Set<Action> constraints)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final Set<Action> req = new HashSet<>(constraints);
            req.add(Action.Read);
            return getFilteredRDFListIterator(req).mapWith(valueMapper)
                    .mapWith(r -> SecuredRDFNodeImpl.getInstance(getModel(), r));
        }
        return NiceIterator.emptyIterator();
    }

    @Override
    public <T> ExtendedIterator<T> mapWith(final Function<RDFNode, T> fn)
            throws ReadDeniedException, AuthenticationRequiredException {
        return iterator().mapWith(fn);
    }

    /**
     * @sec.graph Read - Only readable triples will be passed to the function. if
     *            {@link SecurityEvaluator#isHardReadError()} is true and the user
     *            does not have read access then no items will be passed to the
     *            function..
     * @sec.triple Read for triple containing value to be included in the result.
     * @sec.triple Read for triple containing value.
     * @throws ReadDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Object reduce(final ReduceFn fn, final Object initial)
            throws ReadDeniedException, AuthenticationRequiredException {
        Object acc = initial;

        for (final Iterator<RDFNode> i = iterator(); i.hasNext();) {
            acc = fn.reduce(i.next(), acc);
        }

        return acc;
    }

    /**
     * @param requiredActions the permission set required to execute the reduce.
     * @param fn              the reduction function.
     * @param initial         the initial accumulation value.
     * @return the accumulator after execution.
     * @sec.graph Read - Only readable triples will be passed to the function. if
     *            {@link SecurityEvaluator#isHardReadError()} is true and the user
     *            does not have read access then no items will be passed to the
     *            function.
     * @sec.triple Read for triple containing value to be included in the result.
     * @sec.triple Read for triple containing value.
     * @throws ReadDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Object reduce(final Set<Action> requiredActions, final ReduceFn fn, final Object initial)
            throws EmptyListException, ListIndexException, InvalidListException, ReadDeniedException,
            AuthenticationRequiredException {
        Object acc = initial;
        final Set<Action> perms = new HashSet<>(requiredActions);
        perms.add(Action.Read);
        for (final Iterator<RDFNode> i = iterator(perms); i.hasNext();) {
            acc = fn.reduce(i.next(), acc);
        }
        return acc;
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete for triple containing value.
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public RDFList remove(final RDFNode val)
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();

        if (!canDelete(Triple.create(Node.ANY, RDF.first.asNode(), val.asNode()))) {
            RDFList cell = null;
            final ExtendedIterator<RDFList> iter = getFilteredRDFListIterator(Action.Delete);
            while (iter.hasNext()) {
                cell = iter.next();
                if (val.equals(valueMapper.apply(cell))) {
                    return SecuredRDFListImpl.getInstance(getModel(), baseRemove(cell));
                }
            }
            throw new DeleteDeniedException(SecuredItem.Util.triplePermissionMsg(getModelNode()));
        }
        return SecuredRDFListImpl.getInstance(getModel(), holder.getBaseItem().remove(val));

    }

    /**
     * @sec.graph Update
     * @sec.triple Delete for the head triple.
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredRDFList removeHead()
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final ExtendedIterator<RDFList> iter = getFilteredRDFListIterator(Action.Read);
        try {
            if (!iter.hasNext()) {
                throw new EmptyListException("Attempted to delete the head of a nil list");
            }
            final RDFList cell = iter.next();
            final Statement s = cell.getRequiredProperty(RDF.first);
            checkDelete(s);
            return SecuredRDFListImpl.getInstance(getModel(), baseRemove(cell));
        } finally {
            iter.close();
        }
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete for triple containing value.
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public void removeList() throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final Triple t = Triple.create(Node.ANY, RDF.first.asNode(), Node.ANY);

        // have to be able to read and delete to delete all.
        final Set<Action> perms = SecurityEvaluator.Util.asSet(new Action[] { Action.Delete, Action.Read });
        if (getSecurityEvaluator().evaluate(getSecurityEvaluator().getPrincipal(), perms, this.getModelNode(), t)) {
            holder.getBaseItem().removeList();
        } else {
            for (final Statement s : collectStatements(perms)) {
                if (canDelete(s)) {
                    s.remove();
                }
            }
        }
    }

    /**
     * @sec.graph Update
     * @sec.triple Update for triple i, and value.
     * @throws UpdateDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredRDFNode replace(final int i, final RDFNode value)
            throws UpdateDeniedException, AuthenticationRequiredException, ListIndexException {
        checkUpdate();
        // get all the ones we can see since the replace must be against one we could
        // see.
        final ExtendedIterator<RDFList> iter = getFilteredRDFListIterator(Action.Read);
        int idx = 0;
        try {
            while (iter.hasNext()) {
                // seek to the proper position.
                if (i == idx) {
                    // verify we can delete and if so delete.
                    final RDFList list = iter.next();
                    final RDFNode retval = list.getRequiredProperty(RDF.first).getObject();
                    final Triple t = Triple.create(list.asNode(), RDF.first.asNode(), retval.asNode());
                    final Triple t2 = Triple.create(list.asNode(), RDF.first.asNode(), value.asNode());
                    checkUpdate(t, t2);
                    list.getRequiredProperty(RDF.first).changeObject(value);
                    return SecuredRDFNodeImpl.getInstance(getModel(), retval);
                }
                idx++;
                iter.next();
            }
            throw new ListIndexException();
        } finally {
            iter.close();
        }
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then the the lists will be the
     *            same if the list parameter is an empty list.
     * @sec.triple Read for triples included in the comparison.
     * @throws ReadDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean sameListAs(final RDFList list) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            ExtendedIterator<RDFNode> thisIter = null;
            ExtendedIterator<RDFNode> thatIter = null;
            try {
                thisIter = iterator();
                thatIter = list.iterator();
                while (thisIter.hasNext() && thatIter.hasNext()) {
                    final RDFNode thisN = thisIter.next();
                    final RDFNode thatN = thatIter.next();
                    if ((thisN == null) || !thisN.equals(thatN)) {
                        // not equal at this position
                        return false;
                    }
                }
                return !(thisIter.hasNext() || thatIter.hasNext());
            } finally {
                if (thisIter != null) {
                    thisIter.close();
                }
                if (thatIter != null) {
                    thatIter.close();
                }
            }
        }
        return list.isEmpty();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create for triple containing value.
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredRDFNode setHead(final RDFNode value) throws EmptyListException, AuthenticationRequiredException {
        try {
            return replace(0, value);
        } catch (ListIndexException e) {
            throw new EmptyListException("Tried to set the head of an empty list");
        }
    }

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public void setStrict(final boolean strict) throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        holder.getBaseItem().setStrict(strict);
    }

    @Override
    public SecuredRDFList setTail(final RDFList tail) throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();

        final Statement rest = holder.getBaseItem().getRequiredProperty(RDF.rest);
        final RDFNode retval = rest.getObject();
        final Triple t = Triple.create(holder.getBaseItem().asNode(), RDF.rest.asNode(), retval.asNode());
        final Triple t2 = Triple.create(holder.getBaseItem().asNode(), RDF.rest.asNode(), tail.asNode());
        checkUpdate(t, t2);
        rest.changeObject(tail);
        return SecuredRDFListImpl.getInstance(getModel(), retval.as(RDFList.class));
    }

    /**
     * @sec.graph Read - if {@link SecurityEvaluator#isHardReadError()} is true and
     *            the user does not have read access then zero (0) will be returned.
     * @sec.triple Read for triples counted in the result.
     * @throws ReadDeniedException
     * @throws EmptyListException
     * @throws ListIndexException
     * @throws InvalidListException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int size() throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            final Triple t = Triple.create(Node.ANY, RDF.first.asNode(), Node.ANY);
            if (canRead(t)) {
                return holder.getBaseItem().size();
            }
            final ExtendedIterator<RDFNode> iter = iterator();
            int i = 0;
            while (iter.hasNext()) {
                i++;
                iter.next();
            }
            return i;
        }
        return 0;
    }

    /**
     * @sec.graph Update
     * @sec.triple Create for triple containing value.
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredRDFList with(final RDFNode value)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(Triple.create(SecurityEvaluator.FUTURE, RDF.first.asNode(), value.asNode()));
        return SecuredRDFListImpl.getInstance(getModel(), holder.getBaseItem().with(value));
    }

}
