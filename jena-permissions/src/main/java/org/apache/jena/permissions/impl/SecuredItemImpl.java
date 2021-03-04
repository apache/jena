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
package org.apache.jena.permissions.impl;

import java.lang.reflect.Proxy;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.SecurityEvaluator.Action;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.NodeUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * An abstract implementation of SecuredItem that caches security checks.
 * <p>
 * Security checks are performed at multiple locations. This implementation
 * ensures that during a single operation the specific check is only evaluated
 * once by caching the result.
 * </p>
 *
 */
public abstract class SecuredItemImpl implements SecuredItem {
    // a key for the secured item.
    private class CacheKey implements Comparable<CacheKey> {
        private final Action action;
        private final Node mNode;
        private final Triple from;
        private final Triple to;
        private Integer hashCode;

        public CacheKey(final Action action, final Node modelNode) {
            this(action, modelNode, null, null);
        }

        public CacheKey(final Action action, final Node modelNode, final Triple to) {
            this(action, modelNode, to, null);
        }

        public CacheKey(final Action action, final Node modelNode, final Triple to, final Triple from) {
            this.action = action;
            this.mNode = modelNode;
            this.to = to;
            this.from = from;
        }

        private int compare(Node n1, Node n2) {
            if (Node.ANY.equals(n1)) {
                if (Node.ANY.equals(n2)) {
                    return Expr.CMP_EQUAL;
                }
                return Expr.CMP_LESS;
            }
            if (Node.ANY.equals(n2)) {
                return Expr.CMP_GREATER;
            }
            return NodeUtils.compareRDFTerms(n1, n2);
        }

        private int compare(Triple t1, Triple t2) {
            if (t1 == null) {
                if (t2 == null) {
                    return Expr.CMP_EQUAL;
                }
                return Expr.CMP_LESS;
            }
            if (t2 == null) {
                return Expr.CMP_GREATER;
            }
            int retval = compare(t1.getSubject(), t2.getSubject());
            if (retval == Expr.CMP_EQUAL) {
                retval = compare(t1.getPredicate(), t2.getPredicate());
            }
            if (retval == Expr.CMP_EQUAL) {
                retval = compare(t1.getObject(), t2.getObject());
            }
            return retval;
        }

        @Override
        public int compareTo(final CacheKey other) {
            int retval = this.action.compareTo(other.action);
            if (retval == Expr.CMP_EQUAL) {
                retval = NodeUtils.compareRDFTerms(this.mNode, other.mNode);
            }
            if (retval == Expr.CMP_EQUAL) {
                retval = compare(this.to, other.to);
            }
            if (retval == Expr.CMP_EQUAL) {
                retval = compare(this.from, other.from);
            }
            return retval;
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof CacheKey) {
                return this.compareTo((CacheKey) o) == 0;
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (hashCode == null) {
                hashCode = new HashCodeBuilder().append(action).append(mNode).append(from).append(to).toHashCode();
            }
            return hashCode;
        }
    }

    // the maximum size of the cache
    public static int MAX_CACHE = 100;
    // the cache for this thread.
    public static final ThreadLocal<LRUMap<CacheKey, Boolean>> CACHE = new ThreadLocal<>();
    // the number of times this thread has recursively called the constructor.
    public static final ThreadLocal<Integer> COUNT = new ThreadLocal<>();

    /**
     * May Convert a Jena Node object into the SecurityEvaluator.VARIABLE instance.
     *
     * @param jenaNode The Jena node to convert.
     * @return The Node that represents the jenaNode.
     */
    private static Node convert(final Node jenaNode) {

        if (jenaNode.isVariable()) {
            return SecurityEvaluator.VARIABLE;
        }
        return jenaNode;
    }

    /**
     * Convert a Jena Triple into a SecTriple.
     *
     * @param jenaTriple The Jena Triple to convert.
     * @return The SecTriple that represents the jenaTriple.
     */
    private static Triple convert(final Triple jenaTriple) {
        if (jenaTriple.getSubject().isVariable() || jenaTriple.getPredicate().isVariable()
                || jenaTriple.getObject().isVariable()) {
            return new Triple(SecuredItemImpl.convert(jenaTriple.getSubject()),
                    SecuredItemImpl.convert(jenaTriple.getPredicate()),
                    SecuredItemImpl.convert(jenaTriple.getObject()));
        }
        return jenaTriple;
    }

    /**
     * Decrement the number of instances of SecuredItem.
     */
    public static void decrementUse() {
        final Integer i = SecuredItemImpl.COUNT.get();
        if (i == null) {
            throw new IllegalStateException("No count on exit");
        }
        if (i < 1) {
            throw new IllegalStateException("No count less than 1");
        }
        if (i == 1) {
            SecuredItemImpl.CACHE.remove();
            SecuredItemImpl.COUNT.remove();
        } else {
            SecuredItemImpl.COUNT.set(i - 1);
        }
    }

    /**
     * Increment the number of instances of SecuredItem.
     */
    public static void incrementUse() {
        final Integer i = SecuredItemImpl.COUNT.get();
        if (i == null) {
            SecuredItemImpl.CACHE.set(new LRUMap<CacheKey, Boolean>(Math.max(SecuredItemImpl.MAX_CACHE, 100)));
            SecuredItemImpl.COUNT.set(1);
        } else {
            SecuredItemImpl.COUNT.set(i + 1);
        }
    }

    // the evaluator we are using
    private final SecurityEvaluator securityEvaluator;

    // the secured node for that names the graph.
    private final Node modelNode;

    // the item holder that we are evaluating.
    private final ItemHolder<?, ?> itemHolder;

    /**
     * Create the SecuredItemImpl.
     *
     * @param securedItem The securedItem.
     * @param holder      The Item holder for the securedItem.
     * @throws IllegalArgumentException if securedItem is null or
     *                                  securedItem.getSecurityEvaluator() returns
     *                                  null, or the holder is null.
     */
    protected SecuredItemImpl(final SecuredItem securedItem, final ItemHolder<?, ?> holder) {
        if (securedItem == null) {
            throw new IllegalArgumentException("Secured item may not be null");
        }
        if (securedItem.getSecurityEvaluator() == null) {
            throw new IllegalArgumentException("Security evaluator in secured item may not be null");
        }
        if (holder == null) {
            throw new IllegalArgumentException("ItemHolder may not be null");
        }
        this.securityEvaluator = securedItem.getSecurityEvaluator();
        this.modelNode = securedItem.getModelNode();
        this.itemHolder = holder;
    }

    /**
     * Create the SecuredItemImpl.
     *
     * @param securityEvaluator the secured evaluator to use.
     * @param modelURI          the URI for the model.
     * @param holder            The holder to use.
     * @throws IllegalArgumentException if security evaluator is null, modelURI is
     *                                  null or empty, or holder is null.
     */
    protected SecuredItemImpl(final SecurityEvaluator securityEvaluator, final String modelURI,
            final ItemHolder<?, ?> holder) {
        if (securityEvaluator == null) {
            throw new IllegalArgumentException("Security evaluator may not be null");
        }
        if (StringUtils.isEmpty(modelURI)) {
            throw new IllegalArgumentException("ModelURI may not be empty or null");
        }
        if (holder == null) {
            throw new IllegalArgumentException("ItemHolder may not be null");
        }
        this.securityEvaluator = securityEvaluator;
        this.modelNode = NodeFactory.createURI(modelURI);
        this.itemHolder = holder;
    }

    /**
     * Return a string representation of the resource.
     *
     * Returns the URI of the resource unless the resource is anonymous in which
     * case it returns the id of the resource enclosed in square brackets.
     *
     * If the user can not read the graph the toString() returns the hashcode in hex
     *
     * @return Return a string representation of the resource.
     */
    @Override
    public final String toString() throws AuthenticationRequiredException {
        Object o = itemHolder.getBaseItem();
        boolean ok = canRead();
        if (ok) {
            if (o instanceof Triple) {
                ok = canRead((Triple) o);
            } else if (o instanceof FrontsTriple) {
                ok = canRead(((FrontsTriple) o).asTriple());
            }
        }
        return (ok) ? itemHolder.getBaseItem().toString()
                : String.format("SecuredItem[0x%X]", itemHolder.getBaseItem().hashCode());
    }

    /**
     * get the cached value.
     *
     * @param key The key to look for.
     * @return the value of the security check or <code>null</code> if the value has
     *         not been cached.
     */
    private Boolean cacheGet(final CacheKey key) {
        final LRUMap<CacheKey, Boolean> cache = SecuredItemImpl.CACHE.get();
        return (cache == null) ? null : (Boolean) cache.get(key);
    }

    /**
     * set the cache value.
     *
     * @param key   The key to set the value for.
     * @param value The value to set.
     */
    private void cachePut(final CacheKey key, final boolean value) {
        final LRUMap<CacheKey, Boolean> cache = SecuredItemImpl.CACHE.get();
        if (cache != null) {
            cache.put(key, value);
            SecuredItemImpl.CACHE.set(cache);
        }
    }

    @Override
    public boolean canCreate() throws AuthenticationRequiredException {
        final CacheKey key = new CacheKey(Action.Create, modelNode);
        Boolean retval = cacheGet(key);
        if (retval == null) {
            retval = securityEvaluator.evaluate(securityEvaluator.getPrincipal(), Action.Create, modelNode);
            cachePut(key, retval);
        }
        return retval;
    }

    @Override
    public boolean canCreate(final Triple triple) throws AuthenticationRequiredException {
        Triple t = convert(triple);
        final CacheKey key = new CacheKey(Action.Create, modelNode, t);
        Boolean retval = cacheGet(key);
        if (retval == null) {
            retval = securityEvaluator.evaluate(securityEvaluator.getPrincipal(), Action.Create, modelNode, t);
            cachePut(key, retval);
        }
        return retval;
    }

    @Override
    public boolean canCreate(final FrontsTriple frontsTriple) throws AuthenticationRequiredException {
        return canCreate(frontsTriple.asTriple());
    }

    @Override
    public boolean canDelete() throws AuthenticationRequiredException {
        final CacheKey key = new CacheKey(Action.Delete, modelNode);
        Boolean retval = cacheGet(key);
        if (retval == null) {
            retval = securityEvaluator.evaluate(securityEvaluator.getPrincipal(), Action.Delete, modelNode);
            cachePut(key, retval);
        }
        return retval;
    }

    @Override
    public boolean canDelete(final Triple triple) throws AuthenticationRequiredException {
        Triple t = convert(triple);
        final CacheKey key = new CacheKey(Action.Delete, modelNode, t);
        Boolean retval = cacheGet(key);
        if (retval == null) {
            retval = securityEvaluator.evaluate(securityEvaluator.getPrincipal(), Action.Delete, modelNode, t);
            cachePut(key, retval);
        }
        return retval;
    }

    @Override
    public boolean canDelete(final FrontsTriple frontsTriple) throws AuthenticationRequiredException {
        return canDelete(frontsTriple.asTriple());
    }

    @Override
    public boolean canRead() throws AuthenticationRequiredException {
        final CacheKey key = new CacheKey(Action.Read, modelNode);
        Boolean retval = cacheGet(key);
        if (retval == null) {
            retval = securityEvaluator.evaluate(securityEvaluator.getPrincipal(), Action.Read, modelNode);
            cachePut(key, retval);
        }
        return retval;
    }

    @Override
    public boolean canRead(final Triple triple) throws AuthenticationRequiredException {
        Triple t = convert(triple);
        final CacheKey key = new CacheKey(Action.Read, modelNode, t);
        Boolean retval = cacheGet(key);
        if (retval == null) {
            retval = securityEvaluator.evaluate(securityEvaluator.getPrincipal(), Action.Read, modelNode, t);
            cachePut(key, retval);
        }
        return retval;
    }

    @Override
    public boolean canRead(final FrontsTriple frontsTriple) throws AuthenticationRequiredException {
        return canRead(frontsTriple.asTriple());
    }

    @Override
    public boolean canUpdate() throws AuthenticationRequiredException {
        final CacheKey key = new CacheKey(Action.Update, modelNode);
        Boolean retval = cacheGet(key);
        if (retval == null) {
            retval = securityEvaluator.evaluate(securityEvaluator.getPrincipal(), Action.Update, modelNode);
            cachePut(key, retval);
        }
        return retval;
    }

    @Override
    public boolean canUpdate(final Triple f, final Triple t) throws AuthenticationRequiredException {
        Triple from = convert(f);
        Triple to = convert(t);
        final CacheKey key = new CacheKey(Action.Update, modelNode, from, to);
        Boolean retval = cacheGet(key);
        if (retval == null) {
            retval = securityEvaluator.evaluateUpdate(securityEvaluator.getPrincipal(), modelNode, from, to);
            cachePut(key, retval);
        }
        return retval;
    }

    @Override
    public boolean canUpdate(final FrontsTriple from, final FrontsTriple to) throws AuthenticationRequiredException {
        return canUpdate(from.asTriple(), to.asTriple());
    }

    /**
     * check that create on the securedModel is allowed,
     *
     * @throws AddDeniedException              on failure
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkCreate() throws AddDeniedException, AuthenticationRequiredException {
        if (!canCreate()) {
            throw new AddDeniedException(SecuredItem.Util.modelPermissionMsg(modelNode));
        }
    }

    /**
     * check that the triple can be created in the securedModel.,
     *
     * @throws AddDeniedException              on failure
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkCreate(final Triple t) throws AddDeniedException, AuthenticationRequiredException {
        if (!canCreate(t)) {
            throw new AddDeniedException(SecuredItem.Util.triplePermissionMsg(modelNode), t);
        }
    }

    /**
     * check that the statement can be created.
     *
     * @param s The statement.
     * @throws AddDeniedException              on failure
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkCreate(final FrontsTriple frontsTriple)
            throws AddDeniedException, AuthenticationRequiredException {
        checkCreate(frontsTriple.asTriple());
    }

    /**
     * Check that a triple can be reified.
     *
     * @param uri   The URI for the reification subject. May be null.
     * @param front the frontstriple that is to be reified.
     * @throws AddDeniedException              on failure to add triple
     * @throws UpdateDeniedException           if the updates of the graph are not
     *                                         allowed.
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkCreateReified(final String uri, final FrontsTriple front)
            throws AddDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        Triple t = front.asTriple();
        final Node n = uri == null ? SecurityEvaluator.FUTURE : NodeFactory.createURI(uri);
        checkCreate(new Triple(n, RDF.subject.asNode(), t.getSubject()));
        checkCreate(new Triple(n, RDF.predicate.asNode(), t.getPredicate()));
        checkCreate(new Triple(n, RDF.object.asNode(), t.getObject()));
    }

//	/**
//	 * Check that all the triples can be created.
//	 *
//	 * @param FrontsTripleIter
//	 *            an iterator of FrontsTriple objects.
//	 * @throws AddDeniedException
//	 *             if a triple can not be added.
//	 * @throws AuthenticationRequiredException
//	 *             if user is not authenticated and is required to be.
//	 */
//	protected void checkCreateFrontsTriples(
//			final ExtendedIterator<? extends FrontsTriple> FrontsTripleIter)
//			throws AddDeniedException, AuthenticationRequiredException {
//		if (!canCreate(Triple.ANY)) {
//			try {
//				while (FrontsTripleIter.hasNext()) {
//					checkCreate(FrontsTripleIter.next());
//				}
//			} finally {
//				FrontsTripleIter.close();
//			}
//		}
//	}

//	/**
//	 * Check that all the triples can be created.
//	 *
//	 * @param triples
//	 *            an iterator of triples.
//	 * @throws AddDeniedException
//	 *             if a triple can not be added.
//	 * @throws AuthenticationRequiredException
//	 *             if user is not authenticated and is required to be.
//	 */
//	protected void checkCreateTriples(final ExtendedIterator<Triple> triples)
//			throws AddDeniedException, AuthenticationRequiredException {
//		if (!canCreate(Triple.ANY)) {
//			try {
//				while (triples.hasNext()) {
//					checkCreate(triples.next());
//				}
//			} finally {
//				triples.close();
//			}
//		}
//	}

    /**
     * Check that all the triples can be created.
     *
     * @param supplier the Suplier of the triple iterator.
     * @throws AddDeniedException              if a triple can not be added.
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkCreate(Supplier<ExtendedIterator<Triple>> supplier)
            throws AddDeniedException, AuthenticationRequiredException {
        if (!canCreate(Triple.ANY)) {
            ExtendedIterator<Triple> iter = supplier.get();
            try {
                while (iter.hasNext()) {
                    checkCreate(iter.next());
                }
            } finally {
                iter.close();
            }
        }
    }

    /**
     * check that delete on the securedModel is allowed,
     *
     * @throws DeleteDeniedException           on failure
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkDelete() throws DeleteDeniedException, AuthenticationRequiredException {
        if (!canDelete()) {
            throw new DeleteDeniedException(SecuredItem.Util.modelPermissionMsg(modelNode));
        }
    }

    /**
     * check that the triple can be deleted in the securedModel.,
     *
     * @param triple The triple to check.
     * @throws DeleteDeniedException           on failure
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkDelete(final Triple triple) throws DeleteDeniedException, AuthenticationRequiredException {
        if (!canDelete(triple)) {
            throw new DeleteDeniedException(SecuredItem.Util.triplePermissionMsg(modelNode), triple);
        }
    }

    /**
     * check that the triple can be deleted in the securedModel.,
     *
     * @param frontsTriple An object fronting the triple to check.
     * @throws DeleteDeniedException           on failure
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkDelete(final FrontsTriple frontsTriple)
            throws DeleteDeniedException, AuthenticationRequiredException {
        checkDelete(frontsTriple.asTriple());
    }

    /**
     * check that the triples can be deleted in the securedModel.,
     *
     * @param frontsTripleIter An iterator of objects fronting triples to check.
     * @throws DeleteDeniedException           on failure
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkDeleteFrontsTriples(final ExtendedIterator<? extends FrontsTriple> frontsTriplesIter)
            throws DeleteDeniedException, AuthenticationRequiredException {
        if (!canDelete(Triple.ANY)) {
            try {
                while (frontsTriplesIter.hasNext()) {
                    checkDelete(frontsTriplesIter.next());
                }
            } finally {
                frontsTriplesIter.close();
            }
        }
    }

    /**
     * check that the triples can be deleted in the securedModel.,
     *
     * @param triples An iterator of triples to check.
     * @throws DeleteDeniedException           on failure
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkDeleteTriples(final ExtendedIterator<Triple> triples)
            throws DeleteDeniedException, AuthenticationRequiredException {
        if (!canDelete(Triple.ANY)) {
            try {
                while (triples.hasNext()) {
                    checkDelete(triples.next());
                }
            } finally {
                triples.close();
            }
        }
    }

    /**
     * Checks that read on the securedModel/securedGraph is allowed.
     *
     * @throws ReadDeniedException             on failure
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkRead() throws ReadDeniedException, AuthenticationRequiredException {
        if (!canRead()) {
            throw new ReadDeniedException(SecuredItem.Util.modelPermissionMsg(modelNode));
        }
    }

    /**
     * Checks that read on the securedModel/securedGraph is allowed.
     * <ul>
     * <li>If the securedModel/securedGraph can not be read and the
     * {@code SecurityEvaluator.isHardReadError()} returns true, throws an
     * exception.</li>
     * <li>If the securedModel/securedGraph can not be read and the
     * {@code SecurityEvaluator.isHardReadError()} returns false, returns false.
     * </li>
     * <li>Otherwise returns true.</li>
     * </ul>
     *
     * @return true if the model/graph can be read.
     * @throws ReadDeniedException             on failure
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected boolean checkSoftRead() throws ReadDeniedException, AuthenticationRequiredException {
        if (!canRead()) {
            if (securityEvaluator.isHardReadError()) {
                throw new ReadDeniedException(SecuredItem.Util.modelPermissionMsg(modelNode));
            }
            return false;
        }
        return true;
    }

    /**
     * check that the triple can be read in the securedModel.,
     *
     * @param triple The triple to check.
     * @returns True if the triple can be read, false if not.
     * @throws ReadDeniedException             on failure if HardReadErrors is true.
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected boolean checkRead(final Triple triple) throws ReadDeniedException, AuthenticationRequiredException {
        if (!canRead(triple)) {
            if (securityEvaluator.isHardReadError()) {
                throw new ReadDeniedException(SecuredItem.Util.triplePermissionMsg(modelNode), triple);
            }
            return false;
        }
        return true;
    }

    /**
     * check that the triple can be read in the securedModel.,
     *
     * @returns True if the triple can be read, false if not.
     * @param frontsTriple The object fronting the triple to check.
     * @throws ReadDeniedException             on failure if HardReadErrors are
     *                                         enabled.
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected boolean checkRead(final FrontsTriple frontsTriple)
            throws ReadDeniedException, AuthenticationRequiredException {
        return checkRead(frontsTriple.asTriple());
    }

    /**
     * check that the triple can be read in the securedModel.,
     *
     * @param frontsTripleIter The iterator of fronts triple objects to check.
     * @param returns          true if all triples can be read, false if not.
     * @throws ReadDeniedException             on failure if HardReadErrors are
     *                                         enabled.
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected boolean checkReadFrontsTriples(final ExtendedIterator<FrontsTriple> frontsTripleIter)
            throws ReadDeniedException, AuthenticationRequiredException {
        try {
            while (frontsTripleIter.hasNext()) {
                if (!checkRead(frontsTripleIter.next())) {
                    return false;
                }
            }
            return true;
        } finally {
            frontsTripleIter.close();
        }
    }

    /**
     * check that the triple can be read in the securedModel.,
     *
     * @param triples The iterator of triples to check.
     * @returns true if all the triples can be read, false if not.
     * @throws ReadDeniedException             on failure if HardReadErrors is
     *                                         enabled.
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected boolean checkReadTriples(final ExtendedIterator<Triple> triples)
            throws ReadDeniedException, AuthenticationRequiredException {
        try {
            while (triples.hasNext()) {
                if (!checkRead(triples.next())) {
                    return false;
                }
            }
            return true;
        } finally {
            triples.close();
        }
    }

    /**
     * check that update on the securedModel is allowed,
     *
     * @throws UpdateDeniedException           on failure
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkUpdate() throws UpdateDeniedException, AuthenticationRequiredException {
        if (!canUpdate()) {
            throw new UpdateDeniedException(SecuredItem.Util.modelPermissionMsg(modelNode));
        }
    }

    /**
     * check that the triple can be updated in the securedModel.,
     *
     * @param from the starting triple
     * @param to   the final triple.
     * @throws UpdateDeniedException           on failure
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    protected void checkUpdate(final Triple from, final Triple to)
            throws UpdateDeniedException, AuthenticationRequiredException {
        if (!canUpdate(from, to)) {
            throw new UpdateDeniedException(
                    String.format("%s: %s to %s", SecuredItem.Util.modelPermissionMsg(modelNode), from, to));
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (Proxy.isProxyClass(o.getClass())) {
            return o.equals(itemHolder.getSecuredItem());
        }
        if (o instanceof SecuredItemImpl) {
            return itemHolder.getBaseItem().equals(((SecuredItemImpl) o).getBaseItem());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return itemHolder.getBaseItem().hashCode();
    }

    @Override
    public Object getBaseItem() {
        return itemHolder.getBaseItem();
    }

    @Override
    public String getModelIRI() {
        return modelNode.getURI();
    }

    /**
     * get the name of the model.
     */
    @Override
    public Node getModelNode() {
        return modelNode;
    }

    @Override
    public SecurityEvaluator getSecurityEvaluator() {
        return securityEvaluator;
    }

    @Override
    public boolean isEquivalent(final SecuredItem securedItem) {
        return SecuredItem.Util.isEquivalent(this, securedItem);
    }

    /**
     * Executes the find with permissions
     *
     * @param find
     * @return
     */
    protected <T> ExtendedIterator<T> createIterator(Supplier<ExtendedIterator<T>> find,
            Supplier<Predicate<T>> filter) {
        if (checkSoftRead()) {
            ExtendedIterator<T> retval = find.get();
            if (!canRead(Triple.ANY)) {
                retval = retval.filterKeep(filter.get());
            }
            return retval;
        }
        return NullIterator.instance();
    }

}