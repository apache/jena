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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.CollectionGraph;
import org.apache.jena.permissions.SecuredItem;
import org.apache.jena.permissions.SecurityEvaluator;
import org.apache.jena.permissions.graph.SecuredGraph;
import org.apache.jena.permissions.graph.SecuredPrefixMapping;
import org.apache.jena.permissions.impl.ItemHolder;
import org.apache.jena.permissions.impl.SecuredItemImpl;
import org.apache.jena.permissions.impl.SecuredItemInvoker;
import org.apache.jena.permissions.model.SecuredAlt;
import org.apache.jena.permissions.model.SecuredBag;
import org.apache.jena.permissions.model.SecuredLiteral;
import org.apache.jena.permissions.model.SecuredModel;
import org.apache.jena.permissions.model.SecuredProperty;
import org.apache.jena.permissions.model.SecuredRDFList;
import org.apache.jena.permissions.model.SecuredRDFNode;
import org.apache.jena.permissions.model.SecuredReifiedStatement;
import org.apache.jena.permissions.model.SecuredResource;
import org.apache.jena.permissions.model.SecuredSeq;
import org.apache.jena.permissions.model.SecuredStatement;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NsIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFReaderF;
import org.apache.jena.rdf.model.RDFReaderI;
import org.apache.jena.rdf.model.RDFWriterI;
import org.apache.jena.rdf.model.RSIterator;
import org.apache.jena.rdf.model.ReifiedStatement;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceF;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.NsIteratorImpl;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.rdf.model.impl.StmtIteratorImpl;
import org.apache.jena.shared.AccessDeniedException;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.AuthenticationRequiredException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.Lock;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.shared.ReadDeniedException;
import org.apache.jena.shared.UpdateDeniedException;
import org.apache.jena.shared.WrappedIOException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * Implementation of SecuredModel to be used by a SecuredItemInvoker proxy.
 */
@SuppressWarnings("deprecation")
public class SecuredModelImpl extends SecuredItemImpl implements SecuredModel {

    /**
     * implements ModelChangedListener with premissions.
     */
    private class SecuredModelChangedListener implements ModelChangedListener {
        private final ModelChangedListener wrapped;

        private SecuredModelChangedListener(final ModelChangedListener wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void addedStatement(final Statement s) throws AuthenticationRequiredException {
            if (canRead(s)) {
                wrapped.addedStatement(s);
            }
        }

        @Override
        public void addedStatements(final List<Statement> statements) throws AuthenticationRequiredException {
            if (canRead(Triple.ANY)) {
                wrapped.addedStatements(statements);
            } else {
                final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(),
                        WrappedIterator.create(statements.iterator()));
                try {
                    wrapped.addedStatements(iter.toList());
                } finally {
                    iter.close();
                }
            }
        }

        @Override
        public void addedStatements(final Model m) throws AuthenticationRequiredException {
            if (canRead(Triple.ANY)) {
                wrapped.addedStatements(m);
            } else {
                wrapped.addedStatements(SecuredModelImpl.getInstance(holder.getSecuredItem(), m));
            }
        }

        @Override
        public void addedStatements(final Statement[] statements) throws AuthenticationRequiredException {
            if (canRead(Triple.ANY)) {
                wrapped.addedStatements(statements);
            } else {
                final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(),
                        WrappedIterator.create(Arrays.asList(statements).iterator()));
                try {
                    final List<Statement> stmts = iter.toList();
                    wrapped.addedStatements(stmts.toArray(new Statement[stmts.size()]));
                } finally {
                    iter.close();
                }
            }
        }

        @Override
        public void addedStatements(final StmtIterator statements) throws AuthenticationRequiredException {
            if (canRead(Triple.ANY)) {
                wrapped.addedStatements(statements);
            } else {
                final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(), statements);
                try {
                    wrapped.addedStatements(iter);
                } finally {
                    iter.close();
                }
            }
        }

        @Override
        public void notifyEvent(final Model m, final Object event) {
            wrapped.notifyEvent(m, event);
        }

        @Override
        public void removedStatement(final Statement s) throws AuthenticationRequiredException {
            if (canRead(s)) {
                wrapped.removedStatement(s);
            }
        }

        @Override
        public void removedStatements(final List<Statement> statements) throws AuthenticationRequiredException {

            if (canRead(Triple.ANY)) {
                wrapped.removedStatements(statements);
            } else {
                final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(),
                        WrappedIterator.create(statements.iterator()));
                try {
                    wrapped.removedStatements(iter.toList());
                } finally {
                    iter.close();
                }
            }
        }

        @Override
        public void removedStatements(final Model m) throws AuthenticationRequiredException {
            if (canRead(Triple.ANY)) {
                wrapped.removedStatements(m);
            } else {
                wrapped.removedStatements(SecuredModelImpl.getInstance(holder.getSecuredItem(), m));
            }
        }

        @Override
        public void removedStatements(final Statement[] statements) throws AuthenticationRequiredException {
            if (canRead(Triple.ANY)) {
                wrapped.removedStatements(statements);
            } else {
                final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(),
                        WrappedIterator.create(Arrays.asList(statements).iterator()));
                try {
                    final List<Statement> stmts = iter.toList();
                    wrapped.removedStatements(stmts.toArray(new Statement[stmts.size()]));
                } finally {
                    iter.close();
                }
            }
        }

        @Override
        public void removedStatements(final StmtIterator statements) throws AuthenticationRequiredException {
            if (canRead(Triple.ANY)) {
                wrapped.removedStatements(statements);
            } else {
                final SecuredStatementIterator iter = new SecuredStatementIterator(holder.getSecuredItem(), statements);
                try {
                    wrapped.removedStatements(iter);
                } finally {
                    iter.close();
                }
            }
        }
    }

    private static final RDFReaderF readerFactory = new RDFReaderFImpl();

    /**
     * Get an instance of SecuredModel
     *
     * @param securedItem the item providing the security context.
     * @param model       the Model to secure.
     * @return The SecuredModel
     */
    public static SecuredModel getInstance(final SecuredItem securedItem, final Model model) {
        return org.apache.jena.permissions.Factory.getInstance(securedItem.getSecurityEvaluator(),
                securedItem.getModelIRI(), model);
    }

    /**
     * Get an instance of SecuredModel
     *
     * @param securityEvaluator The security evaluator to use
     * @param modelIRI          The IRI (graph IRI) to name this model.
     * @param model             The Model to secure.
     * @return the SecuredModel
     */
    public static SecuredModel getInstance(final SecurityEvaluator securityEvaluator, final String modelIRI,
            final Model model) {
        final ItemHolder<Model, SecuredModel> holder = new ItemHolder<>(model);

        final SecuredModelImpl checker = new SecuredModelImpl(securityEvaluator, modelIRI, holder);
        // if we are going to create a duplicate proxy, just return this
        // one.
        if (model instanceof SecuredModel) {
            if (checker.isEquivalent((SecuredModel) model)) {
                return (SecuredModel) model;
            }
        }
        return holder.setSecuredItem(new SecuredItemInvoker(model.getClass(), checker));
    }

    // the item holder that contains this SecuredModel.
    private final ItemHolder<Model, SecuredModel> holder;

    // The secured graph that this securedModel contains.
    private final SecuredGraph graph;

    private Map<ModelChangedListener, SecuredModelChangedListener> listeners = new HashMap<>();

    /**
     * Constructor.
     *
     * @param securityEvaluator The security evaluator to use
     * @param modelURI          The securedModel IRI to verify against.
     * @param holder            The item holder that will contain this SecuredModel.
     */
    private SecuredModelImpl(final SecurityEvaluator securityEvaluator, final String modelURI,
            final ItemHolder<Model, SecuredModel> holder) {
        super(securityEvaluator, modelURI, holder);
        this.graph = org.apache.jena.permissions.Factory.getInstance(securityEvaluator, modelURI,
                holder.getBaseItem().getGraph());
        this.holder = holder;
    }

    /**
     * Create secured statement iterator from Supplier.
     *
     * if {@link SecurityEvaluator#isHardReadError()} is true and the user does not
     * have read access then an empty iterator will be returned.
     *
     * @param supplier the supplier for the ExtendedIterator of Statements.
     * @return the SecuredStatementIterator
     * @throws ReadDeniedException             if HardReadErrors is enabled and user
     *                                         can not read.
     * @throws AuthenticationRequiredException if the user is not authenticated.
     */
    private SecuredStatementIterator stmtIterator(Supplier<ExtendedIterator<Statement>> supplier)
            throws ReadDeniedException, AuthenticationRequiredException {
        ExtendedIterator<Statement> iter = checkSoftRead() ? supplier.get() : NiceIterator.emptyIterator();
        return new SecuredStatementIterator(holder.getSecuredItem(), iter);
    }

    /**
     * Create secured resource iterator from Supplier.
     *
     * if {@link SecurityEvaluator#isHardReadError()} is true and the user does not
     * have read access then an empty iterator will be returned.
     *
     * @param supplier the supplier for the ExtendedIterator of Resources.
     * @return the SecuredResIterator
     * @throws ReadDeniedException             if HardReadErrors is enabled and user
     *                                         can not read.
     * @throws AuthenticationRequiredException if the user is not authenticated.
     */
    private SecuredResIterator resIterator(Supplier<ExtendedIterator<Resource>> supplier, ResourceFilter filter) {
        ExtendedIterator<Resource> rIter = null;
        if (checkSoftRead()) {
            rIter = supplier.get();
            if (!canRead(Triple.ANY)) {
                rIter = rIter.filterKeep(filter);
            }
        } else {
            rIter = NiceIterator.emptyIterator();
        }
        return new SecuredResIterator(holder.getSecuredItem(), rIter);
    }

    private RDFNode asObject(Object o) {
        return o instanceof RDFNode ? (RDFNode) o : ResourceFactory.createTypedLiteral(o);
    }

    @Override
    public SecuredModel abort() {
        holder.getBaseItem().abort();
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create for each statement as a triple.
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel add(final List<Statement> statements)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(() -> WrappedIterator.create(statements.iterator()).mapWith(s -> s.asTriple()));
        holder.getBaseItem().add(statements);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create for each statement in the securedModel as a triple.
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel add(final Model m)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(() -> m.listStatements().mapWith(s -> s.asTriple()));
        holder.getBaseItem().add(m);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create the triple Triple(s,p,o)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel add(final Resource s, final Property p, final RDFNode o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(Triple.create(s.asNode(), p.asNode(), o.asNode()));
        holder.getBaseItem().add(s, p, o);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create the triple Triple(s,p,o)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel add(final Resource s, final Property p, final String o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return add(s, p, o, false);
    }

    /**
     * @sec.graph Update
     * @sec.triple Create the triple Triple(s,p,o)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel add(final Resource s, final Property p, final String o, final boolean wellFormed)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(Triple.create(s.asNode(), p.asNode(), NodeFactory.createLiteral(o, "", wellFormed)));
        holder.getBaseItem().add(s, p, o, wellFormed);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create the triple Triple(s,p,literal(lex,datatype))
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel add(final Resource s, final Property p, final String lex, final RDFDatatype datatype)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(Triple.create(s.asNode(), p.asNode(), NodeFactory.createLiteral(lex, datatype)));
        holder.getBaseItem().add(s, p, lex, datatype);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create the triple Triple(s,p,literal(o,l,false))
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel add(final Resource s, final Property p, final String o, final String l)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(Triple.create(s.asNode(), p.asNode(), NodeFactory.createLiteral(o, l, false)));
        holder.getBaseItem().add(s, p, o, l);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create the statement as a triple
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel add(final Statement s)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(s);
        holder.getBaseItem().add(s);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create all the statements as triples.
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel add(final Statement[] statements)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(() -> WrappedIterator.create(Arrays.asList(statements).iterator()).mapWith(s -> s.asTriple()));
        holder.getBaseItem().add(statements);
        return holder.getSecuredItem();

    }

    /**
     * @sec.graph Update
     * @sec.triple Create all the statements as triples.
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel add(final StmtIterator iter)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        StmtIterator updateFrom = iter;
        if (!canCreate(Triple.ANY)) {
            // checkCreate will throw exception on first failure
            List<Statement> stmt = iter.filterKeep(s -> {
                checkCreate(s);
                return true;
            }).toList();
            // now just add the list to the base
            updateFrom = new StmtIteratorImpl(stmt.iterator());
        }
        holder.getBaseItem().add(updateFrom);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Create triple(s,p,o)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel addLiteral(final Resource s, final Property p, final boolean o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return add(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create triple(s,p,o)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel addLiteral(final Resource s, final Property p, final char o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return add(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create triple(s,p,o)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel addLiteral(final Resource s, final Property p, final double o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return add(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create triple(s,p,o)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel addLiteral(final Resource s, final Property p, final float o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return add(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create triple(s,p,o)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel addLiteral(final Resource s, final Property p, final int o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return add(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create triple(s,p,o)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel addLiteral(final Resource s, final Property p, final Literal o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return add(s, p, o);
    }

    /**
     * @sec.graph Update
     * @sec.triple Create triple(s,p,o)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel addLiteral(final Resource s, final Property p, final long o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return add(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create triple(s,p,o)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    @Deprecated
    public SecuredModel addLiteral(final Resource s, final Property p, final Object o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return add(s, p, asObject(o));
    }

    @Override
    public SecuredRDFNode asRDFNode(final Node n) {
        return SecuredRDFNodeImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().asRDFNode(n));
    }

    /**
     * @sec.graph Read if t does exist
     * @sec.graph Update it t does not exist or can not be read
     * @sec.triple Read if t does exist
     * @sec.triple Create if t does not exist or can not be read
     * @throws AccessDeniedException           on restriction
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement asStatement(final Triple t)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        final ExtendedIterator<Triple> iter = holder.getBaseItem().getGraph().find(t);
        final boolean exists = iter.hasNext();
        iter.close();

        // we can proceed if we can read what exists or if we can create one
        if ((exists && canRead() && canRead(t)) || (canUpdate() && canCreate(t))) {
            return SecuredStatementImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().asStatement(t));
        }
        throw new AccessDeniedException(String.format("Converting %s to Statement", t));
    }

    @Override
    public SecuredModel begin() {
        holder.getBaseItem().begin();
        return holder.getSecuredItem();
    }

    @Override
    public void close() {
        holder.getBaseItem().close();
    }

    @Override
    public SecuredModel commit() {
        holder.getBaseItem().commit();
        return holder.getSecuredItem();
    }

    /**
     * Determines if there is a first element in the supplied iterator after
     * filtering by ability to read.
     *
     * if {@link SecurityEvaluator#isHardReadError()} is true and the user does not
     * have read access then false will be returned.
     *
     * @param supplier The extended iterator supplier
     * @return true if there is an enclosed element.
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    private boolean containsAny(Supplier<ExtendedIterator<Statement>> supplier) {
        if (checkSoftRead()) {
            ExtendedIterator<Statement> iter = supplier.get();
            try {
                return iter.filterKeep(stmt -> canRead(stmt)).hasNext();
            } finally {
                iter.close();
            }
        }
        return false;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( s, p, SecNode.ANY )
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final Resource s, final Property p)
            throws ReadDeniedException, AuthenticationRequiredException {
        return containsAny(() -> holder.getBaseItem().listStatements(s, p, (RDFNode) null));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( s, p, SecNode.ANY )
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final Resource s, final Property p, final RDFNode o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return containsAny(() -> holder.getBaseItem().listStatements(s, p, o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( s, p, SecNode.ANY )
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final Resource s, final Property p, final String o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return containsAny(() -> holder.getBaseItem().listStatements(s, p, o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( s, p, SecNode.ANY )
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final Resource s, final Property p, final String o, final String l)
            throws ReadDeniedException, AuthenticationRequiredException {
        return containsAny(() -> holder.getBaseItem().listStatements(s, p, o, l));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( s, p, SecNode.ANY )
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean contains(final Statement s) throws ReadDeniedException, AuthenticationRequiredException {
        return (checkSoftRead() && canRead(s)) ? holder.getBaseItem().contains(s) : false;
    }

    /**
     * Verifies that all the items in the iterator can be read.
     *
     * @param supplier
     * @return
     */
    private boolean containsAll(Supplier<StmtIterator> supplier) {
        StmtIterator iter = supplier.get();
        try {
            while (iter.hasNext()) {
                Statement stmt = iter.next();
                if (!canRead(stmt) || !holder.getBaseItem().contains(stmt)) {
                    return false;
                }
            }
        } finally {
            iter.close();
        }
        return true;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read every statement in securedModel.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean containsAll(final Model model) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                return holder.getBaseItem().containsAll(model);
            }
            // check every statement against this model
            return containsAll(() -> model.listStatements());
        }
        return false;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read every statement in securedModel.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean containsAll(final StmtIterator iter) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                return holder.getBaseItem().containsAll(iter);
            }
            return containsAll(() -> iter);
        }
        return false;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read any statement in securedModel to be included in check, if no
     *             statement in securedModel can be read will return false;
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean containsAny(final Model model) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                return holder.getBaseItem().containsAll(model);
            }
            containsAny(() -> model.listStatements());
        }
        return false;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read any statement in securedModel to be included in check, if no
     *             statement in securedModel can be read will return false;
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean containsAny(final StmtIterator iter) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                return holder.getBaseItem().containsAny(iter);
            }
            containsAny(() -> iter);
        }
        return false;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( s, p, literal(o) )
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean containsLiteral(final Resource s, final Property p, final boolean o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return contains(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( s, p, literal(o) )
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean containsLiteral(final Resource s, final Property p, final char o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return contains(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( s, p, literal(o) )
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean containsLiteral(final Resource s, final Property p, final double o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return contains(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( s, p, literal(o) )
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean containsLiteral(final Resource s, final Property p, final float o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return contains(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( s, p, literal(o) )
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean containsLiteral(final Resource s, final Property p, final int o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return contains(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( s, p, literal(o) )
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean containsLiteral(final Resource s, final Property p, final long o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return contains(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( s, p, literal(o) )
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean containsLiteral(final Resource s, final Property p, final Object o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return contains(s, p, asObject(o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( s, p, resource) where Triple(s,p,resource) is in the
     *             securedModel.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean containsResource(final RDFNode r) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                return holder.getBaseItem().containsResource(r);
            }
            ExtendedIterator<Statement> iter = listStatements(null, null, r);
            if (r.isResource()) {
                if (r.isURIResource()) {
                    iter = iter.andThen(
                            listStatements(null, ResourceFactory.createProperty(r.asNode().getURI()), (RDFNode) null));
                } else {
                    iter = iter.andThen(listStatements(null,
                            ResourceFactory.createProperty(r.asNode().getBlankNodeLabel()), (RDFNode) null));
                }
                iter = iter.andThen(listStatements(r.asResource(), null, (RDFNode) null));
            }
            try {
                return iter.hasNext();
            } finally {
                iter.close();
            }
        }
        return false;
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( SecNode.ANY, RDF.type, Rdf.Alt)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredAlt createAlt() throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(Triple.create(SecurityEvaluator.FUTURE, RDF.type.asNode(), RDF.Alt.asNode()));
        return SecuredAltImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createAlt());
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( SecNode.ANY, RDF.type, Rdf.Alt)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredAlt createAlt(final String uri)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(Triple.create(NodeFactory.createURI(uri), RDF.type.asNode(), RDF.Alt.asNode()));
        return SecuredAltImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createAlt(uri));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( SecNode.ANY, RDF.type, Rdf.Bag)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredBag createBag() throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(Triple.create(SecurityEvaluator.FUTURE, RDF.type.asNode(), RDF.Bag.asNode()));
        return SecuredBagImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createBag());
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( uri, RDF.type, Rdf.Bag)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredBag createBag(final String uri)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(Triple.create(NodeFactory.createURI(uri), RDF.type.asNode(), RDF.Bag.asNode()));
        return SecuredBagImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createBag(uri));
    }

    /**
     * Makes an independent copy of all statements the user can read in the model.
     *
     * @return an independeny copy of the model.
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException
     */
    private Model createCopy() throws ReadDeniedException, AuthenticationRequiredException {
        return ModelFactory.createDefaultModel().add(holder.getSecuredItem());
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( SecurityEvaluator.FUTURE, RDF.type, Rdf.List)
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredRDFList createList()
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(Triple.create(SecurityEvaluator.FUTURE, RDF.type.asNode(), RDF.List.asNode()));
        return SecuredRDFListImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createList());
    }

    private SecuredRDFList createList(Supplier<ExtendedIterator<? extends RDFNode>> supplier) {
        checkUpdate();
        checkCreate(Triple.create(SecurityEvaluator.FUTURE, RDF.type.asNode(), RDF.List.asNode()));
        ExtendedIterator<? extends RDFNode> iter = supplier.get();
        List<RDFNode> lst = new ArrayList<RDFNode>();
        try {
            // if the iterator is empty there are no first/rest nodes.
            if (iter.hasNext()) {
                checkCreate(Triple.create(SecurityEvaluator.FUTURE, RDF.rest.asNode(), RDF.nil.asNode()));
                checkCreate(Triple.create(SecurityEvaluator.FUTURE, RDF.rest.asNode(), SecurityEvaluator.FUTURE));
            }
            while (iter.hasNext()) {
                RDFNode n = iter.next();
                checkCreate(Triple.create(SecurityEvaluator.FUTURE, RDF.first.asNode(), n.asNode()));
                lst.add(n);
            }
            return SecuredRDFListImpl.getInstance(holder.getSecuredItem(),
                    holder.getBaseItem().createList(lst.iterator()));
        } finally {
            iter.close();
        }
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( RDF.nil, SecNode.IGNORE, SecNode.IGNORE)
     * @sec.triple Create for each member Triple(SecNode.ANY, RDF.first.asNode(),
     *             member.asNode())
     * @sec.triple Create Triple(SecNode.ANY, RDF.rest.asNode(), SecNode.ANY)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredRDFList createList(final Iterator<? extends RDFNode> members)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return createList(() -> WrappedIterator.create(members));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( RDF.nil, SecNode.IGNORE, SecNode.IGNORE)
     * @sec.triple Create for each member Triple(SecNode.ANY, RDF.first.asNode(),
     *             member.asNode())
     * @sec.triple Create Triple(SecNode.ANY, RDF.rest.asNode(), SecNode.ANY)
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredRDFList createList(RDFNode... members)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return createList(() -> WrappedIterator.create(Arrays.asList(members).iterator()));
    }

    @Override
    public SecuredLiteral createLiteral(final String v) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createLiteral(v));
    }

    @Override
    public SecuredLiteral createLiteral(final String v, final boolean wellFormed) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(),
                holder.getBaseItem().createLiteral(v, wellFormed));

    }

    @Override
    public SecuredLiteral createLiteral(final String v, final String language) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createLiteral(v, language));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( s,p,o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement createLiteralStatement(final Resource s, final Property p, final boolean o) {
        return createStatement(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( s,p,o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement createLiteralStatement(final Resource s, final Property p, final char o) {
        return createStatement(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( s,p,o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement createLiteralStatement(final Resource s, final Property p, final double o) {
        return createStatement(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( s,p,o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement createLiteralStatement(final Resource s, final Property p, final float o) {
        return createStatement(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( s,p,o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement createLiteralStatement(final Resource s, final Property p, final int o) {
        return createStatement(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( s,p,o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement createLiteralStatement(final Resource s, final Property p, final long o) {
        return createStatement(s, p, ResourceFactory.createTypedLiteral(o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( s,p,o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement createLiteralStatement(final Resource s, final Property p, final Object o) {
        return createStatement(s, p, asObject(o));
    }

    @Override
    public SecuredProperty createProperty(final String uri) {
        return SecuredPropertyImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createProperty(uri));
    }

    @Override
    public SecuredProperty createProperty(final String nameSpace, final String localName) {
        return SecuredPropertyImpl.getInstance(holder.getSecuredItem(),
                holder.getBaseItem().createProperty(nameSpace, localName));
    }

    /**
     * @sec.graph Update
     * @sec.triple Read s as a triple
     * @sec.triple Create Triple( SecNode.Future, RDF.subject, t.getSubject() )
     * @sec.triple Create Triple( SecNode.Future, RDF.subject, t.getPredicate() )
     * @sec.triple create Triple( SecNode.Future, RDF.subject, t.getObject() )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredReifiedStatement createReifiedStatement(final Statement s)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        // checkCreateReified does Update check
        checkCreateReified(null, s);
        return SecuredReifiedStatementImpl.getInstance(holder.getSecuredItem(),
                holder.getBaseItem().createReifiedStatement(s));
    }

    /**
     * @sec.graph Update
     * @sec.triple Read s as a triple
     * @sec.triple create Triple( uri, RDF.subject, t.getSubject() )
     * @sec.triple create Triple( uri, RDF.subject, t.getPredicate() )
     * @sec.triple create Triple( uri, RDF.subject, t.getObject() )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredReifiedStatement createReifiedStatement(final String uri, final Statement s)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        // checkCreateReified does Update check
        checkCreateReified(uri, s);
        return SecuredReifiedStatementImpl.getInstance(holder.getSecuredItem(),
                holder.getBaseItem().createReifiedStatement(uri, s));
    }

    @Override
    public SecuredResource createResource() {
        return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createResource());
    }

    @Override
    public SecuredResource createResource(final AnonId id) {
        return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createResource(id));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( SecNode.FUTURE, RDF.type, type )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource createResource(final Resource type)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        final Triple t = Triple.create(SecurityEvaluator.FUTURE, RDF.type.asNode(), type.asNode());
        checkCreate(t);

        return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createResource(type));
    }

    @Override
    @Deprecated
    public SecuredResource createResource(final ResourceF f) throws AuthenticationRequiredException {
        return createResource(null, f);
    }

    @Override
    public SecuredResource createResource(final String uri) {
        return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createResource(uri));

    }

    @Override
    public Resource createResource(Statement statement) {
        return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createResource(statement));
    }

    private void checkReadOrUpdate(Resource s, Property p, RDFNode o) {
        if (!canReadOrUpdate(s, p, o)) {
            throw new AddDeniedException(String.format("Can not create Statement [ %s ,%s %s ]", s, p, o));
        }

    }

    private boolean canReadOrUpdate(Resource s, Property p, RDFNode o) {
        Triple t = Triple.create(s.asNode(), p.asNode(), o.asNode());
        boolean canExecute = canUpdate() && canCreate(t);
        if (!canExecute && holder.getBaseItem().contains(s, p, o)) {
            canExecute |= (canRead() && canRead(t));
        }
        return canExecute;
    }

    /**
     * @sec.graph Update if uri exists
     * @sec.graph Create if uri does not exist
     * @sec.triple Read if Triple( uri, RDF.type, type ) exists
     * @sec.triple Create if Triple( uri, RDF.type, type ) does not exist
     * @throws UpdateDeniedException
     * @throws ReadDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredResource createResource(final String uri, final Resource type)
            throws AccessDeniedException, AuthenticationRequiredException {
        checkReadOrUpdate(ResourceFactory.createResource(uri), RDF.type, type);
        return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createResource(uri, type));
    }

    @Override
    @Deprecated
    public SecuredResource createResource(final String uri, final ResourceF f) {
        return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createResource(uri, f));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( SecNode.FUTURE, RDF.type, RDF.Alt )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq createSeq() throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkCreate(Triple.create(SecurityEvaluator.FUTURE, RDF.type.asNode(), RDF.Alt.asNode()));
        return SecuredSeqImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createSeq());
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( uri, RDF.type, RDF.Alt )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq createSeq(final String uri)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkReadOrUpdate(ResourceFactory.createResource(uri), RDF.type, RDF.Alt);
        return SecuredSeqImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createSeq(uri));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( s, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement createStatement(final Resource s, final Property p, final RDFNode o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkReadOrUpdate(s, p, o);
        return SecuredStatementImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createStatement(s, p, o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( s, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement createStatement(final Resource s, final Property p, final String o)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        checkReadOrUpdate(s, p, ResourceFactory.createProperty(o));
        return SecuredStatementImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createStatement(s, p, o));
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( s, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement createStatement(final Resource s, final Property p, final String o,
            final boolean wellFormed)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return createStatement(s, p, o, "", wellFormed);
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( s, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement createStatement(final Resource s, final Property p, final String o, final String l)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        return createStatement(s, p, o, l, false);
    }

    /**
     * @sec.graph Update
     * @sec.triple Create Triple( s, p, o )
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement createStatement(final Resource s, final Property p, final String o, final String l,
            final boolean wellFormed)
            throws UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        Node n = NodeFactory.createLiteral(o, l, wellFormed);
        checkReadOrUpdate(s, p, holder.getBaseItem().getRDFNode(n));
        return SecuredStatementImpl.getInstance(holder.getSecuredItem(),
                holder.getBaseItem().createStatement(s, p, o, l, wellFormed));
    }

    @Override
    public SecuredLiteral createTypedLiteral(final boolean v) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
    }

    @Override
    public SecuredLiteral createTypedLiteral(final Calendar d) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(d));
    }

    @Override
    public SecuredLiteral createTypedLiteral(final char v) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
    }

    @Override
    public SecuredLiteral createTypedLiteral(final double v) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
    }

    @Override
    public SecuredLiteral createTypedLiteral(final float v) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
    }

    @Override
    public SecuredLiteral createTypedLiteral(final int v) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
    }

    @Override
    public SecuredLiteral createTypedLiteral(final long v) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
    }

    @Override
    public SecuredLiteral createTypedLiteral(final Object value) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(value));
    }

    @Override
    public SecuredLiteral createTypedLiteral(final Object value, final RDFDatatype dtype) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(),
                holder.getBaseItem().createTypedLiteral(value, dtype));
    }

    @Override
    public SecuredLiteral createTypedLiteral(final Object value, final String typeURI) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(),
                holder.getBaseItem().createTypedLiteral(value, typeURI));
    }

    @Override
    public SecuredLiteral createTypedLiteral(final String v) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().createTypedLiteral(v));
    }

    @Override
    public SecuredLiteral createTypedLiteral(final String lex, final RDFDatatype dtype) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(),
                holder.getBaseItem().createTypedLiteral(lex, dtype));
    }

    @Override
    public SecuredLiteral createTypedLiteral(final String lex, final String typeURI) {
        return SecuredLiteralImpl.getInstance(holder.getSecuredItem(),
                holder.getBaseItem().createTypedLiteral(lex, typeURI));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read for every triple contributed to the difference.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then an model will be returned.
     *
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Model difference(final Model model) throws AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                return holder.getBaseItem().difference(model);
            }
            return createCopy().difference(model);
        }
        return ModelFactory.createDefaultModel();
    }

    @Override
    public void enterCriticalSection(final boolean readLockRequested)
            throws UpdateDeniedException, ReadDeniedException, AuthenticationRequiredException {
        holder.getBaseItem().enterCriticalSection(readLockRequested);
    }

    @Override
    public void executeInTxn(Runnable action) {
        holder.getBaseItem().executeInTxn(action);
    }

    @Override
    public <T> T calculateInTxn(Supplier<T> action) {
        return holder.getBaseItem().calculateInTxn(action);
    }

    /**
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then original argument will be
     *            returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String expandPrefix(final String prefixed) throws ReadDeniedException, AuthenticationRequiredException {
        return checkSoftRead() ? holder.getBaseItem().expandPrefix(prefixed) : prefixed;
    }

    @Override
    public SecuredAlt getAlt(final Resource r) throws ReadDeniedException, AuthenticationRequiredException {
        checkReadOrUpdate(r, RDF.type, RDF.Alt);
        return SecuredAltImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getAlt(r));
    }

    @Override
    public SecuredAlt getAlt(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
        checkReadOrUpdate(ResourceFactory.createResource(uri), RDF.type, RDF.Alt);
        return SecuredAltImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getAlt(uri));
    }

    /**
     * @sec.graph Read if statement exists
     * @sec.graph Update if statement does not exist
     * @sec.triple Read s as a triple
     * @sec.triple Read Triple( result, RDF.subject, s.getSubject() ) if reification
     *             existed
     * @sec.triple Read Triple( result, RDF.predicate, s.getPredicate() ) if
     *             reification existed
     * @sec.triple Read Triple( result, RDF.object, s.getObject() ) if reification
     *             existed
     * @sec.triple Create Triple( result, RDF.subject, s.getSubject() ) if
     *             reification did not exist.
     * @sec.triple Create Triple( result, RDF.predicate, s.getPredicate() ) if
     *             reification did not exist
     * @sec.triple Create Triple( result, RDF.object, s.getObject() ) if reification
     *             did not exist
     *
     * @throws ReadDeniedException
     * @throws UpdateDeniedException
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredReifiedStatement getAnyReifiedStatement(final Statement s)
            throws ReadDeniedException, UpdateDeniedException, AddDeniedException, AuthenticationRequiredException {
        // read if we are allowed to (ignore hardReadError flag)
        if (canRead()) {
            final RSIterator it = listReifiedStatements(s);
            if (it.hasNext()) {
                try {
                    return SecuredReifiedStatementImpl.getInstance(holder.getSecuredItem(), it.nextRS());
                } finally {
                    it.close();
                }
            }
        }
        /*
         * either we are not allowed to read or there are no reified statements so
         * create them
         */
        return createReifiedStatement(s);

    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( resource, RDF.type, RDF.Bag )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredBag getBag(final Resource r) throws ReadDeniedException, AuthenticationRequiredException {
        checkReadOrUpdate(r, RDF.type, RDF.Bag);
        return SecuredBagImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getBag(r));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read Triple( ure, RDF.type, RDF.Bag )
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredBag getBag(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
        checkReadOrUpdate(ResourceFactory.createResource(uri), RDF.type, RDF.Bag);
        return SecuredBagImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getBag(uri));
    }

    /**
     *
     * @sec.graph Read
     * @sec.triple Read on Triple(resource, RDF.type, RDF.Seq)
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq getSeq(final Resource r) throws ReadDeniedException, AuthenticationRequiredException {
        checkReadOrUpdate(r, RDF.type, RDF.Seq);
        return SecuredSeqImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getSeq(r));
    }

    /**
     *
     * @sec.graph Read
     * @sec.triple Read on Triple(uri, RDF.type, RDF.Seq)
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredSeq getSeq(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
        checkReadOrUpdate(ResourceFactory.createResource(uri), RDF.type, RDF.Seq);
        return SecuredSeqImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getSeq(uri));
    }

    /**
     * Return a RDF List instance in this model.
     *
     * <p>
     * Subsequent operations on the returned list may modify this model.
     * </p>
     * <p>
     * The list is assumed to already exist in the model. If it does not,
     * <CODE>createList</CODE> should be used instead.
     * </p>
     *
     * @return a list instance
     * @param uri the URI of the list
     */
    @Override
    public SecuredRDFList getList(String uri) throws ReadDeniedException, AuthenticationRequiredException {
        checkReadOrUpdate(ResourceFactory.createResource(uri), RDF.type, RDF.List);
        return SecuredRDFListImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getList(uri));
    }

    /**
     * Return a RDF List based on a given resource.
     *
     * <p>
     * This method enables an application to treat any resource as a list. It is in
     * effect an unsafe downcast.
     * </p>
     *
     * <p>
     * Subsequent operations on the returned list may modify this model.
     * </p>
     * <p>
     * The list is assumed to already exist in the model. If it does not,
     * <CODE>createList</CODE> should be used instead.
     * </p>
     *
     * @return a list instance
     * @param r the resource of the list
     */
    @Override
    public SecuredRDFList getList(Resource r) throws ReadDeniedException, AuthenticationRequiredException {
        checkReadOrUpdate(r, RDF.type, RDF.List);
        return SecuredRDFListImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getList(r));
    }

    @Override
    public SecuredGraph getGraph() {
        return graph;
    }

    @Override
    public Lock getLock() {
        return holder.getBaseItem().getLock();
    }

    /**
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then an empty map will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Map<String, String> getNsPrefixMap() throws ReadDeniedException, AuthenticationRequiredException {
        return checkSoftRead() ? holder.getBaseItem().getNsPrefixMap() : Collections.emptyMap();
    }

    /**
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then @{code null} will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String getNsPrefixURI(final String prefix) throws ReadDeniedException, AuthenticationRequiredException {
        return checkSoftRead() ? holder.getBaseItem().getNsPrefixURI(prefix) : null;
    }

    /**
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then @{code null} will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String getNsURIPrefix(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
        return checkSoftRead() ? holder.getBaseItem().getNsURIPrefix(uri) : null;
    }

    /**
     * Returns the first statement from the iterator.
     *
     * @param the supplier of the iterator.
     * @return the statement or null if no statements exist or can be read.
     */
    private SecuredStatement _getProperty(Supplier<StmtIterator> supplier) {
        if (checkSoftRead()) {
            ExtendedIterator<Statement> iter = supplier.get().filterKeep(s -> canRead(s));
            try {
                return iter.hasNext() ? SecuredStatementImpl.getInstance(holder.getSecuredItem(), iter.next()) : null;
            } finally {
                iter.close();
            }
        }
        return null;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on the returned statement.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code null} will be
     *             returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement getProperty(final Resource s, final Property p)
            throws ReadDeniedException, AuthenticationRequiredException {
        return _getProperty(() -> holder.getBaseItem().listStatements(s, p, (RDFNode) null));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on the returned statement.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then @{code null} will be
     *             returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement getProperty(Resource s, Property p, String lang)
            throws ReadDeniedException, AuthenticationRequiredException {
        return _getProperty(() -> holder.getBaseItem().listStatements(s, p, null, lang));
    }

    @Override
    public SecuredProperty getProperty(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
        return SecuredPropertyImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().getProperty(uri));
    }

    @Override
    public SecuredProperty getProperty(final String nameSpace, final String localName)
            throws ReadDeniedException, AuthenticationRequiredException {
        return SecuredPropertyImpl.getInstance(holder.getSecuredItem(),
                holder.getBaseItem().getProperty(nameSpace, localName));
    }

    @Override
    public SecuredRDFNode getRDFNode(final Node n)
            throws ReadDeniedException, UpdateDeniedException, AuthenticationRequiredException {
        if (n.isLiteral()) {
            return SecuredLiteralImpl.getInstance(holder.getSecuredItem(),
                    holder.getBaseItem().getRDFNode(n).asLiteral());
        } else if (n.isURI() || n.isBlank()) {
            return SecuredResourceImpl.getInstance(holder.getSecuredItem(),
                    holder.getBaseItem().getRDFNode(n).asResource());
        }
        throw new IllegalArgumentException("Illegal Node type: " + n.getClass());
    }

    @Override
    public RDFReaderI getReader() {
        return holder.getBaseItem().getReader();
    }

    @Override
    public RDFReaderI getReader(final String lang) {
        return holder.getBaseItem().getReader(lang);
    }

    /**
     * . If the PropertyNotFoundException was thrown by the enclosed securedModel
     * and the user can not read Triple(s, p, SecNode.ANY) AccessDeniedException is
     * thrown, otherwise the PropertyNotFoundException will be thrown.
     *
     * @sec.graph Read
     * @sec.triple Read on the returned statement
     * @sec.triple Read on Triple(s, p, SecNode.ANY) if PropertyNotFoundException
     *             was thrown
     * @throws ReadDeniedException
     * @throws PropertyNotFoundException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement getRequiredProperty(final Resource s, final Property p)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                return SecuredStatementImpl.getInstance(holder.getSecuredItem(),
                        holder.getBaseItem().getRequiredProperty(s, p));
            }
            ExtendedIterator<Statement> iter = holder.getBaseItem().listStatements(s, p, (RDFNode) null)
                    .filterKeep(f -> canRead(f));
            try {
                if (iter.hasNext()) {
                    return SecuredStatementImpl.getInstance(holder.getSecuredItem(), iter.next());
                }
            } finally {
                iter.close();
            }
        }
        throw new PropertyNotFoundException(p);
    }

    /**
     * . If the PropertyNotFoundException was thrown by the enclosed securedModel
     * and the user can not read Triple(s, p, SecNode.ANY) AccessDeniedException is
     * thrown, otherwise the PropertyNotFoundException will be thrown.
     *
     * @sec.graph Read
     * @sec.triple Read on the returned statement
     * @sec.triple Read on Triple(s, p, SecNode.ANY) if PropertyNotFoundException
     *             was thrown
     * @throws ReadDeniedException
     * @throws PropertyNotFoundException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredStatement getRequiredProperty(final Resource s, final Property p, String lang)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                return SecuredStatementImpl.getInstance(holder.getSecuredItem(),
                        holder.getBaseItem().getRequiredProperty(s, p, lang));
            }
            ExtendedIterator<Statement> iter = holder.getBaseItem().listStatements(s, p, null, lang)
                    .filterKeep(f -> canRead(f));
            try {
                if (iter.hasNext()) {
                    return SecuredStatementImpl.getInstance(holder.getSecuredItem(), iter.next());
                }
            } finally {
                iter.close();
            }
        }
        throw new PropertyNotFoundException(p);
    }

    @Override
    public SecuredResource getResource(final String uri) {
        return createResource(uri);
    }

    @Override
    @Deprecated
    public SecuredResource getResource(final String uri, final ResourceF f) {
        return createResource(uri, f);
    }

    @Override
    public RDFWriterI getWriter() {
        return holder.getBaseItem().getWriter();
    }

    @Override
    public RDFWriterI getWriter(final String lang) {
        return holder.getBaseItem().getWriter(lang);
    }

    @Override
    public boolean independent() {
        return false;
    }

    /**
     *
     * @sec.graph Read
     * @sec.triple Read on all triples contributed to the new securedModel.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then an empty model will be
     *             returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Model intersection(final Model model) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            return canRead(Triple.ANY) ? holder.getBaseItem().intersection(model) : createCopy().intersection(model);
        }
        return ModelFactory.createDefaultModel();
    }

    @Override
    public boolean isClosed() {
        return holder.getBaseItem().isClosed();
    }

    /**
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then true will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean isEmpty() throws ReadDeniedException, AuthenticationRequiredException {
        return checkSoftRead() ? !holder.getBaseItem().contains(holder.getBaseItem().asStatement(Triple.ANY)) : true;
    }

    /**
     * @sec.graph Read
     * @sec.triple Read all compared triples. Triples that can not be read will not
     *             be compared.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then true will be returned if g is
     *             empty.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean isIsomorphicWith(final Model g) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                return holder.getBaseItem().isIsomorphicWith(g);
            }
            return createCopy().isIsomorphicWith(g);
        }
        return g.isEmpty();
    }

    /**
     *
     * @sec.graph Read
     * @sec.triple Read on s as triple
     * @sec.triple Read on at least one set reified statements.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then false will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean isReified(final Statement s) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead() && checkRead(s)) {
            return holder.getBaseItem().isReified(s);
        }
        return false;
    }

    @Override
    public void leaveCriticalSection() {
        holder.getBaseItem().leaveCriticalSection();
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all triples returned.
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
    public SecuredStatementIterator listLiteralStatements(final Resource subject, final Property predicate,
            final boolean object) throws ReadDeniedException, AuthenticationRequiredException {
        return stmtIterator(() -> holder.getBaseItem().listLiteralStatements(subject, predicate, object));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all triples returned.
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
    public SecuredStatementIterator listLiteralStatements(final Resource subject, final Property predicate,
            final char object) throws ReadDeniedException, AuthenticationRequiredException {
        return stmtIterator(() -> holder.getBaseItem().listLiteralStatements(subject, predicate, object));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all triples returned.
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
    public SecuredStatementIterator listLiteralStatements(final Resource subject, final Property predicate,
            final double object) throws ReadDeniedException, AuthenticationRequiredException {
        return stmtIterator(() -> holder.getBaseItem().listLiteralStatements(subject, predicate, object));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all triples returned.
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
    public SecuredStatementIterator listLiteralStatements(final Resource subject, final Property predicate,
            final float object) throws ReadDeniedException, AuthenticationRequiredException {
        return stmtIterator(() -> holder.getBaseItem().listLiteralStatements(subject, predicate, object));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all triples returned.
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
    public SecuredStatementIterator listLiteralStatements(final Resource subject, final Property predicate,
            final long object) throws ReadDeniedException, AuthenticationRequiredException {
        return stmtIterator(() -> holder.getBaseItem().listLiteralStatements(subject, predicate, object));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all triples returned.
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
    public SecuredStatementIterator listLiteralStatements(final Resource subject, final Property predicate,
            final int object) throws ReadDeniedException, AuthenticationRequiredException {
        return stmtIterator(() -> holder.getBaseItem().listLiteralStatements(subject, predicate, object));
    }

    /**
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then an empty iterator will be
     *            returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public NsIterator listNameSpaces() throws ReadDeniedException, AuthenticationRequiredException {
        return checkSoftRead() ? holder.getBaseItem().listNameSpaces()
                : new NsIteratorImpl(Collections.emptyIterator(), null);
    }

    private SecuredNodeIterator<RDFNode> nodeIterator(Supplier<ExtendedIterator<RDFNode>> supplier,
            Predicate<RDFNode> filter) {
        ExtendedIterator<RDFNode> nIter = null;
        if (checkSoftRead()) {
            nIter = supplier.get();
            if (!canRead(Triple.ANY)) {
                nIter = nIter.filterKeep(filter);
            }
        } else {
            nIter = NiceIterator.emptyIterator();
        }
        return new SecuredNodeIterator<>(holder.getSecuredItem(), nIter);
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on each RDFNode returned
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
    public SecuredNodeIterator<RDFNode> listObjects() throws ReadDeniedException, AuthenticationRequiredException {
        return nodeIterator(() -> holder.getBaseItem().listObjects(), new ObjectFilter());
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on each RDFNode returned
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
    public SecuredNodeIterator<RDFNode> listObjectsOfProperty(final Property p)
            throws ReadDeniedException, AuthenticationRequiredException {
        return nodeIterator(() -> holder.getBaseItem().listObjectsOfProperty(p), new ObjectFilter(p));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on each RDFNode returned
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
    public SecuredNodeIterator<RDFNode> listObjectsOfProperty(final Resource s, final Property p)
            throws ReadDeniedException, AuthenticationRequiredException {
        return nodeIterator(() -> holder.getBaseItem().listObjectsOfProperty(s, p), new ObjectFilter(p));
    }

    private SecuredRSIterator reifiedIterator(Supplier<ExtendedIterator<ReifiedStatement>> supplier) {
        ExtendedIterator<ReifiedStatement> iter = checkSoftRead() ? supplier.get() : NiceIterator.emptyIterator();
        return new SecuredRSIterator(holder.getSecuredItem(), iter);
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on each Reified statement returned
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
    public SecuredRSIterator listReifiedStatements() throws ReadDeniedException, AuthenticationRequiredException {
        return reifiedIterator(() -> holder.getBaseItem().listReifiedStatements());
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on each Reified statement returned
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
    public SecuredRSIterator listReifiedStatements(final Statement st)
            throws ReadDeniedException, AuthenticationRequiredException {
        return reifiedIterator(() -> holder.getBaseItem().listReifiedStatements(st));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read at least one Triple( resource, p, o ) for each resource
     *             returned.
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
    public SecuredResIterator listResourcesWithProperty(final Property p)
            throws ReadDeniedException, AuthenticationRequiredException {
        return resIterator(() -> holder.getBaseItem().listResourcesWithProperty(p), new ResourceFilter(p));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read at least one Triple( resource, p, o ) for each resource
     *             returned.
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
    public SecuredResIterator listResourcesWithProperty(final Property p, final boolean o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return resIterator(() -> holder.getBaseItem().listResourcesWithProperty(p, o),
                new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read at least one Triple( resource, p, o ) for each resource
     *             returned.
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
    public SecuredResIterator listResourcesWithProperty(final Property p, final char o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return resIterator(() -> holder.getBaseItem().listResourcesWithProperty(p, o),
                new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read at least one Triple( resource, p, o ) for each resource
     *             returned.
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
    public SecuredResIterator listResourcesWithProperty(final Property p, final double o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return resIterator(() -> holder.getBaseItem().listResourcesWithProperty(p, o),
                new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read at least one Triple( resource, p, o ) for each resource
     *             returned.
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
    public SecuredResIterator listResourcesWithProperty(final Property p, final float o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return resIterator(() -> holder.getBaseItem().listResourcesWithProperty(p, o),
                new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read at least one Triple( resource, p, o ) for each resource
     *             returned.
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
    public SecuredResIterator listResourcesWithProperty(final Property p, final long o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return resIterator(() -> holder.getBaseItem().listResourcesWithProperty(p, o),
                new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read at least one Triple( resource, p, o ) for each resource
     *             returned.
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
    public SecuredResIterator listResourcesWithProperty(final Property p, final Object o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return resIterator(() -> holder.getBaseItem().listResourcesWithProperty(p, o),
                new ResourceFilter(p, ResourceFactory.createTypedLiteral(o)));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read at least one Triple( resource, p, o ) for each resource
     *             returned.
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
    public SecuredResIterator listResourcesWithProperty(final Property p, final RDFNode o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return resIterator(() -> holder.getBaseItem().listResourcesWithProperty(p, o), new ResourceFilter(p, o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all triples returned
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
    public SecuredStatementIterator listStatements() throws ReadDeniedException, AuthenticationRequiredException {
        return stmtIterator(() -> holder.getBaseItem().listStatements());
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all triples returned
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
    public SecuredStatementIterator listStatements(final Resource s, final Property p, final RDFNode o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return stmtIterator(() -> holder.getBaseItem().listStatements(s, p, o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all triples returned
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
    public SecuredStatementIterator listStatements(final Resource subject, final Property predicate,
            final String object) throws ReadDeniedException, AuthenticationRequiredException {
        return stmtIterator(() -> holder.getBaseItem().listStatements(subject, predicate, object));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all triples returned
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
    public SecuredStatementIterator listStatements(final Resource subject, final Property predicate,
            final String object, final String lang) throws ReadDeniedException, AuthenticationRequiredException {
        return stmtIterator(() -> holder.getBaseItem().listStatements(subject, predicate, object, lang));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all triples returned
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
    public SecuredStatementIterator listStatements(final Selector s)
            throws ReadDeniedException, AuthenticationRequiredException {
        return stmtIterator(() -> holder.getBaseItem().listStatements(s));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read at least one Triple( s, p, o ) for each resource returned
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
    public SecuredResIterator listSubjects() throws ReadDeniedException, AuthenticationRequiredException {
        return resIterator(() -> holder.getBaseItem().listSubjects(), new ResourceFilter());
    }

    /**
     * @sec.graph Read
     * @sec.triple Read at least one Triple( resource, p, o ) for each resource
     *             returned
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
    public SecuredResIterator listSubjectsWithProperty(final Property p)
            throws ReadDeniedException, AuthenticationRequiredException {
        return resIterator(() -> holder.getBaseItem().listSubjectsWithProperty(p), new ResourceFilter(p));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read at least one Triple( resource, p, o ) for each resource
     *             returned
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
    public SecuredResIterator listSubjectsWithProperty(final Property p, final RDFNode o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return resIterator(() -> holder.getBaseItem().listSubjectsWithProperty(p, o), new ResourceFilter(p, o));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read at least one Triple( resource, p, o ) for each resource
     *             returned
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
    public SecuredResIterator listSubjectsWithProperty(final Property p, final String o)
            throws ReadDeniedException, AuthenticationRequiredException {
        return resIterator(() -> holder.getBaseItem().listSubjectsWithProperty(p, o),
                new ResourceFilter(p, ResourceFactory.createPlainLiteral(o)));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read at least one Triple( resource, p, o ) for each resource
     *             returned
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
    public SecuredResIterator listSubjectsWithProperty(final Property p, final String o, final String l)
            throws ReadDeniedException, AuthenticationRequiredException {
        return resIterator(() -> holder.getBaseItem().listSubjectsWithProperty(p, o, l),
                new ResourceFilter(p, ResourceFactory.createLangLiteral(o, l)));
    }

    /**
     * @sec.graph Update
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredPrefixMapping lock() throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        holder.getBaseItem().lock();
        return holder.getSecuredItem();
    }

    @Override
    public SecuredModel notifyEvent(final Object e) {
        holder.getBaseItem().notifyEvent(e);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then {@code null} will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String qnameFor(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
        return checkSoftRead() ? holder.getBaseItem().qnameFor(uri) : null;
    }

    /**
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then an empty model will be
     *            returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Model query(final Selector s) throws ReadDeniedException, AuthenticationRequiredException {
        return checkSoftRead() ? holder.getBaseItem().query(new SecuredSelector(holder.getSecuredItem(), s))
                : ModelFactory.createDefaultModel();
    }

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel read(final InputStream in, final String base)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        SecuredModelImpl.readerFactory.getReader().read(holder.getSecuredItem(), in, base);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel read(final InputStream in, final String base, final String lang)
            throws ReadDeniedException, AuthenticationRequiredException {
        checkUpdate();
        SecuredModelImpl.readerFactory.getReader(lang).read(holder.getSecuredItem(), in, base);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel read(final Reader reader, final String base)
            throws ReadDeniedException, AuthenticationRequiredException {
        checkUpdate();
        SecuredModelImpl.readerFactory.getReader().read(holder.getSecuredItem(), reader, base);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel read(final Reader reader, final String base, final String lang)
            throws ReadDeniedException, AuthenticationRequiredException {
        checkUpdate();
        SecuredModelImpl.readerFactory.getReader(lang).read(holder.getSecuredItem(), reader, base);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel read(final String url) throws ReadDeniedException, AuthenticationRequiredException {
        checkUpdate();
        SecuredModelImpl.readerFactory.getReader().read(holder.getSecuredItem(), url);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel read(final String url, final String lang)
            throws ReadDeniedException, AuthenticationRequiredException {
        checkUpdate();
        SecuredModelImpl.readerFactory.getReader(lang).read(holder.getSecuredItem(), url);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel read(final String url, final String base, final String lang)
            throws ReadDeniedException, AuthenticationRequiredException {
        try (InputStream is = new URL(url).openStream()) {
            read(is, base, lang);
        } catch (final IOException e) {
            throw new WrappedIOException(e);
        }
        return holder.getSecuredItem();
    }

    /**
     * Listener will be filtered to only report events that the user can see.
     *
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then listener will not be
     *            registered but no exception will be thrown
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel register(final ModelChangedListener listener)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (!listeners.containsKey(listener)) {
                final SecuredModelChangedListener secL = new SecuredModelChangedListener(listener);
                listeners.put(listener, secL);
                holder.getBaseItem().register(secL);
            }
            return holder.getSecuredItem();
        }
        throw new ReadDeniedException(SecuredItem.Util.modelPermissionMsg(getModelNode()));
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete on every statement in statements.
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel remove(final List<Statement> statements)
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        if (!canDelete(Triple.ANY)) {
            for (final Statement s : statements) {
                checkDelete(s);
            }
        }
        holder.getBaseItem().remove(statements);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete on every statement in baseModel.
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel remove(final Model m)
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        if (!canDelete(Triple.ANY)) {
            final StmtIterator iter = m.listStatements();
            try {
                while (iter.hasNext()) {
                    final Statement stmt = iter.next();
                    checkDelete(stmt);
                }
            } finally {
                iter.close();
            }
        }
        holder.getBaseItem().remove(m);

        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete on Triple( s, p, o )
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel remove(final Resource s, final Property p, final RDFNode o)
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkDelete(Triple.create(s.asNode(), p.asNode(), o.asNode()));
        holder.getBaseItem().remove(s, p, o);
        return holder.getSecuredItem();
    }

    /**
     *
     * @sec.graph Update
     * @sec.triple Delete on statement.
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel remove(final Statement s)
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        checkDelete(wildCardTriple(s));
        holder.getBaseItem().remove(s);
        return holder.getSecuredItem();
    }

    /**
     *
     * @sec.graph Update
     * @sec.triple Delete on every statement in statements.
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel remove(final Statement[] statements)
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        if (!canDelete(Triple.ANY)) {
            for (final Statement s : statements) {
                checkDelete(s);
            }
        }
        holder.getBaseItem().remove(statements);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete on every statement in iter.
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel remove(final StmtIterator iter)
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        if (!canDelete(Triple.ANY)) {
            final List<Triple> lst = new ArrayList<>();
            try {
                while (iter.hasNext()) {
                    final Statement s = iter.next();
                    checkDelete(s);
                    lst.add(s.asTriple());
                }
                final Model m = ModelFactory.createModelForGraph(new CollectionGraph(lst));
                holder.getBaseItem().remove(m.listStatements());
            } finally {
                iter.close();
            }
        } else {
            holder.getBaseItem().remove(iter);
        }
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete on every statement in the securedModel
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel removeAll()
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        if (!canDelete(Triple.ANY)) {
            final StmtIterator iter = holder.getBaseItem().listStatements();
            try {
                while (iter.hasNext()) {
                    checkDelete(iter.next());
                }
            } finally {
                iter.close();
            }
        }
        holder.getBaseItem().removeAll();
        return holder.getSecuredItem();
    }

    @Override
    public SecuredModel removeAll(final Resource s, final Property p, final RDFNode r)
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        if (!canDelete(Triple.create(wildCardNode(s), wildCardNode(p), wildCardNode(r)))) {
            final StmtIterator iter = holder.getBaseItem().listStatements(s, p, r);
            try {
                while (iter.hasNext()) {
                    checkDelete(iter.next());
                }
            } finally {
                iter.close();
            }
        }
        holder.getBaseItem().removeAll(s, p, r);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete on every reification statement for each statement in
     *             statements.
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public void removeAllReifications(final Statement s)
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        if (canDelete(Triple.create(Node.ANY, RDF.subject.asNode(), wildCardNode(s.getSubject())))
                && canDelete(Triple.create(Node.ANY, RDF.predicate.asNode(), wildCardNode(s.getPredicate())))
                && canDelete(Triple.create(Node.ANY, RDF.object.asNode(), wildCardNode(s.getObject())))) {
            holder.getBaseItem().removeAllReifications(s);
        } else {
            final RSIterator iter = holder.getBaseItem().listReifiedStatements(s);
            try {
                while (iter.hasNext()) {
                    final ReifiedStatement rs = iter.next();
                    checkDelete(Triple.create(rs.asNode(), RDF.subject.asNode(), wildCardNode(s.getSubject())));
                    checkDelete(Triple.create(rs.asNode(), RDF.predicate.asNode(), wildCardNode(s.getPredicate())));
                    checkDelete(Triple.create(rs.asNode(), RDF.object.asNode(), wildCardNode(s.getObject())));
                }
                holder.getBaseItem().removeAllReifications(s);
            } finally {
                iter.close();
            }

        }
    }

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel removeNsPrefix(final String prefix)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        holder.getBaseItem().removeNsPrefix(prefix);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @throws UpdateDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel clearNsPrefixMap() {
        checkUpdate();
        holder.getBaseItem().clearNsPrefixMap();
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @sec.triple Delete on every reification statement fore each statement in rs.
     * @throws UpdateDeniedException
     * @throws DeleteDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public void removeReification(final ReifiedStatement rs)
            throws UpdateDeniedException, DeleteDeniedException, AuthenticationRequiredException {
        checkUpdate();
        if (!canDelete(Triple.ANY)) {
            final StmtIterator stmtIter = rs.listProperties();
            try {
                while (stmtIter.hasNext()) {
                    checkDelete(stmtIter.next());
                }
            } finally {
                stmtIter.close();
            }
        }
        holder.getBaseItem().removeReification(rs);
    }

    /**
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then @{code false} will be returned
     *            if the other has no prefix mappings.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean samePrefixMappingAs(final PrefixMapping other)
            throws ReadDeniedException, AuthenticationRequiredException {
        return checkSoftRead() ? holder.getBaseItem().samePrefixMappingAs(other) : other.hasNoMappings();
    }

    /**
     * @sec.graph Update
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel setNsPrefix(final String prefix, final String uri)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        holder.getBaseItem().setNsPrefix(prefix, uri);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel setNsPrefixes(final Map<String, String> map)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        holder.getBaseItem().setNsPrefixes(map);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Update
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel setNsPrefixes(final PrefixMapping other)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        holder.getBaseItem().setNsPrefixes(other);
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then @{code uri} will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public String shortForm(final String uri) throws ReadDeniedException, AuthenticationRequiredException {
        return checkSoftRead() ? holder.getBaseItem().shortForm(uri) : uri;
    }

    /**
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then @{code true} will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public boolean hasNoMappings() {
        return checkSoftRead() ? holder.getBaseItem().hasNoMappings() : true;
    }

    /**
     * @sec.graph Read
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then 0 will be returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public int numPrefixes() {
        return checkSoftRead() ? holder.getBaseItem().numPrefixes() : 0;
    }

    /**
     * @sec.graph Read
     *
     *
     *            if {@link SecurityEvaluator#isHardReadError()} is true and the
     *            user does not have read access then 0 will be returned.
     *
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public long size() throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                return holder.getBaseItem().size();
            }
            return createCopy().size();
        }
        return 0;
    }

    @Override
    public boolean supportsSetOperations() {
        return holder.getBaseItem().supportsSetOperations();
    }

    @Override
    public boolean supportsTransactions() {
        return holder.getBaseItem().supportsTransactions();
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all statements contributed to the union.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then an empty model will be
     *             returned.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public Model union(final Model model) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                return holder.getBaseItem().union(model);
            }
            return createCopy().union(model);
        }
        return ModelFactory.createDefaultModel().add(model);
    }

    @Override
    public SecuredModel unregister(final ModelChangedListener listener) {
        if (listeners.containsKey(listener)) {
            final SecuredModelChangedListener secL = listeners.get(listener);
            holder.getBaseItem().unregister(secL);
            listeners.remove(listener);
        }
        return holder.getSecuredItem();
    }

    private Node wildCardNode(final RDFNode node) {
        return node == null ? Node.ANY : node.asNode();
    }

    private Triple wildCardTriple(final Statement s) {
        return Triple.create(wildCardNode(s.getSubject()), wildCardNode(s.getPredicate()), wildCardNode(s.getObject()));
    }

    /**
     * @sec.graph Update
     * @throws AddDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel withDefaultMappings(final PrefixMapping map)
            throws UpdateDeniedException, AuthenticationRequiredException {
        checkUpdate();
        holder.getBaseItem().withDefaultMappings(map);
        return holder.getSecuredItem();
    }

    @Override
    public SecuredResource wrapAsResource(final Node n) {
        return SecuredResourceImpl.getInstance(holder.getSecuredItem(), holder.getBaseItem().wrapAsResource(n));
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all statements that are written.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then no data will be written.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel write(final OutputStream out) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                holder.getBaseItem().write(out);
            } else {
                getWriter().write(holder.getSecuredItem(), out, "");
            }
        }
        return holder.getSecuredItem();

    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all statements that are written.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then no data will be written.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel write(final OutputStream out, final String lang)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                holder.getBaseItem().write(out, lang);
            } else {
                getWriter(lang).write(holder.getSecuredItem(), out, "");
            }
        }
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all statements that are written.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then no data will be written.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel write(final OutputStream out, final String lang, final String base)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                holder.getBaseItem().write(out, lang, base);
            } else {
                getWriter(lang).write(holder.getSecuredItem(), out, base);
            }
        }
        return holder.getSecuredItem();

    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all statements that are written.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then no data will be written.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel write(final Writer writer) throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                holder.getBaseItem().write(writer);
            } else {
                getWriter().write(holder.getSecuredItem(), writer, "");
            }
        }
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all statements that are written.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then no data will be written.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel write(final Writer writer, final String lang)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                holder.getBaseItem().write(writer, lang);
            } else {
                getWriter(lang).write(holder.getSecuredItem(), writer, "");
            }
        }
        return holder.getSecuredItem();
    }

    /**
     * @sec.graph Read
     * @sec.triple Read on all statements that are written.
     *
     *             if {@link SecurityEvaluator#isHardReadError()} is true and the
     *             user does not have read access then no data will be written.
     *
     * @throws ReadDeniedException
     * @throws AuthenticationRequiredException if user is not authenticated and is
     *                                         required to be.
     */
    @Override
    public SecuredModel write(final Writer writer, final String lang, final String base)
            throws ReadDeniedException, AuthenticationRequiredException {
        if (checkSoftRead()) {
            if (canRead(Triple.ANY)) {
                holder.getBaseItem().write(writer, lang, base);
            } else {
                getWriter(lang).write(holder.getSecuredItem(), writer, base);
            }
        }
        return holder.getSecuredItem();

    }

    /**
     * A private class that filters items resources to only those resources that can
     * be seen as subjects in statements.
     */
    private class ResourceFilter implements Predicate<Resource> {
        Property p;
        RDFNode o;

        ResourceFilter() {
            this(null, null);
        }

        ResourceFilter(Property p) {
            this(p, null);
        }

        ResourceFilter(Property p, RDFNode o) {
            this.p = p;
            this.o = o;
        }

        @Override
        public boolean test(Resource s) {
            StmtIterator iter = listStatements(s, p, o);
            try {
                return iter.hasNext();
            } finally {
                iter.close();
            }
        }

    }

    /**
     * A private class that filters items resources to only those resources that can
     * be seen as objects in statements.
     */
    private class ObjectFilter implements Predicate<RDFNode> {
        Resource s;
        Property p;

        ObjectFilter() {
            this(null, null);
        }

        ObjectFilter(Property p) {
            this(null, p);
        }

        ObjectFilter(Resource s, Property p) {
            this.s = s;
            this.p = p;
        }

        @Override
        public boolean test(RDFNode o) {
            StmtIterator iter = listStatements(s, p, o);
            try {
                return iter.hasNext();
            } finally {
                iter.close();
            }
        }
    }
}
